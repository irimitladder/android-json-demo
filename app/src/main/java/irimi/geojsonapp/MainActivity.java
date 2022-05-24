package irimi.geojsonapp;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity
        extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GeoJsonProcessTask geoJsonProcessTask = new GeoJsonProcessTask();
        geoJsonProcessTask.execute("https://waadsu.com/api/russia.geo.json", this);

        WebView mapView = findViewById(R.id.mapview);
        mapView.getSettings().setJavaScriptEnabled(true);
    }

    private static class GeoJsonProcessTask
            extends AsyncTask<Object, Void, Polygon[]> {

        private static final String TAG = "GeoJsonDownloadTask";
        private MainActivity mainActivity;

        private final GeometryFactory countryFactory = new GeometryFactory();
        private Polygon[] countrySegments;

        private static String countryRegionsToString(Polygon[] countryRegions) {
            StringBuilder countryRegions_asText = new StringBuilder();
            countryRegions_asText.append('[');
            boolean countryRegionFirst = true;
            for (Polygon countryRegion : countryRegions) {
                if (countryRegionFirst)
                    countryRegionFirst = false;
                else
                    countryRegions_asText.append(',');
                countryRegions_asText.append('[');

                Coordinate[] countryRegionPoints = countryRegion.getCoordinates();
                boolean countryRegionPointFirst = true;
                for (Coordinate countryRegionPoint : countryRegionPoints) {
                    if (countryRegionPointFirst)
                        countryRegionPointFirst = false;
                    else
                        countryRegions_asText.append(',');
                    countryRegions_asText.append('[');
                    countryRegions_asText.append(countryRegionPoint.x);
                    countryRegions_asText.append(',');
                    countryRegions_asText.append(countryRegionPoint.y);
                    countryRegions_asText.append(']');
                }

                countryRegions_asText.append(']');
            }
            countryRegions_asText.append(']');
            return countryRegions_asText.toString();
        }

        @Override
        protected Polygon[] doInBackground(Object... params) {
            mainActivity = (MainActivity) params[1];
            Polygon[] countryRegions = download((String) params[0]);
            postDownload(countryRegions);
            return countryRegions;
        }

        // Man, I hate text parsing tasks, they're the most fucked up always turning code into
        // a disgusting mess. Tho I ain't gonna use any third-party libraries here to make this demo
        // more illustrative.
        // Then, I'm mad at that guy who wrote this problem's formulation: what 'bout to explain
        // whaddaya mean by a country region? O you unknown person, go to hell along with your shit.
        private Polygon[] download(String url) {
            List<Polygon> polygons = new ArrayList<>();
            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                urlConnection = (HttpsURLConnection) new URL(url).openConnection();
                urlConnection.setRequestProperty("User-Agent", "irimi-geojsonapp");
                if (
                        (urlConnection.getResponseCode() != 200) &&
                        (urlConnection.getResponseCode() != 203))
                    throw new IOException("GeoJSON is unavailable");
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                // Prepare for GeoJSON parsing
                List<Coordinate> polygonPointCoordinates = new ArrayList<>();
                StringBuilder polygonPointCoordinateChars = new StringBuilder();
                double polygonPointLongitude = Double.MAX_VALUE;
                int readerLevel = 0;
                char[] readerChars = new char[500];
                int readerCharCount;

                // Start GeoJSON parsing
                parsingStart:
                while (true) {
                    readerCharCount = reader.read(readerChars);
                    if (readerCharCount < 0)

                        // An error! GeoJSON data is incomplete or broken
                        throw new IOException("GeoJSON is incomplete or broken");
                    if (readerCharCount > 0) {
                        for (int charIndex = 0; charIndex < readerCharCount; charIndex++)
                            if (readerChars[charIndex] == '[') {
                                if (readerLevel == 5)

                                    // An error! GeoJSON data has too many nesting levels
                                    throw new IOException("GeoJSON has too many nesting levels");

                                readerLevel++;
                            } else if (readerChars[charIndex] == ']') {
                                if (readerLevel == 1)

                                    // Finish GeoJSON parsing
                                    break parsingStart;
                                if (readerLevel == 4) {

                                    // Finish a map polygon parsing
                                    polygons.add(countryFactory.createPolygon(countryFactory.createLinearRing(polygonPointCoordinates.toArray(new Coordinate[0]))));
                                    polygonPointCoordinates.clear();
                                } else if (readerLevel == 5) {
                                    // polygonPointCoordinates.add(new Coordinate(polygonPointLongitude + (polygonPointLongitude < 0 ? 360.0 : 0.0), Double.parseDouble(polygonPointCoordinateChars.toString())));
                                    polygonPointCoordinates.add(new Coordinate(polygonPointLongitude, Double.parseDouble(polygonPointCoordinateChars.toString())));
                                    polygonPointCoordinateChars.setLength(0);
                                    polygonPointLongitude = Double.MAX_VALUE;
                                }
                                readerLevel--;
                            } else if (readerLevel == 5) {
                                if (readerChars[charIndex] == ',') {
                                    if (polygonPointLongitude != Double.MAX_VALUE)

                                        // An error! GeoJSON points have an extra coordinate
                                        throw new IOException("GeoJSON points have an extra coordinate");
                                    polygonPointLongitude = Double.parseDouble(polygonPointCoordinateChars.toString());
                                    polygonPointCoordinateChars.setLength(0);
                                } else
                                    polygonPointCoordinateChars.append(readerChars[charIndex]);
                            }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "download: " + e.getMessage());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "download: " + e.getMessage());
                    }
                }
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            // TODO: Probably, drawing many country regions on the map at once exceeds mapView's JS memory limit
            // [ERROR:tile_manager.cc(793)] WARNING: tile memory limits exceeded, some content may not draw
            // Just for demonstration:
            /* if (polygons.size() > 20)
                for (int polygonIndex = polygons.size() - 1; polygonIndex >= 20; polygonIndex--)
                    polygons.remove(polygonIndex); */

            return polygons.size() == 0 ? null : polygons.toArray(new Polygon[0]);
        }

        private void postDownload(Polygon[] countryRegions) {
            /* if (
                    (countryRegions == null) ||
                    (countryRegions.length == 0))
                return;
            if (countryRegions.length == 1) {
                countrySegments = countryRegions;
                return;
            }

            List<Polygon> countrySegments = new ArrayList<>();
            boolean[] countryRegionsNotAdded = new boolean[countryRegions.length];
            Arrays.fill(countryRegionsNotAdded, true);
            for (int countryRegionIndex = 0; countryRegionIndex < countryRegions.length; countryRegionIndex++)
                if (countryRegionsNotAdded[countryRegionIndex]) {
                    Polygon countrySegment = countryRegions[countryRegionIndex];
                    for (int countryAddingRegionIndex = countryRegionIndex; countryAddingRegionIndex < countryRegions.length; countryAddingRegionIndex++)
                        if (
                                (countrySegment.intersects(countryRegions[countryAddingRegionIndex])) &&
                                !(countrySegment.touches(countryRegions[countryAddingRegionIndex]))) {
                            countrySegment = countryFactory.createPolygon(countryFactory.createLinearRing(countrySegment.union(countryRegions[countryAddingRegionIndex]).getCoordinates()));
                            countryRegionsNotAdded[countryRegionIndex] = false;
                        }
                    countrySegments.add(countrySegment);
                }
            this.countrySegments = countrySegments.toArray(new Polygon[0]); */
            this.countrySegments = countryRegions;
        }

        @Override
        protected void onPostExecute(Polygon[] countryRegions) {
            WebView mapView = mainActivity.findViewById(R.id.mapview);
            final WebViewAssetLoader mapViewAssetLoader = new WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(mainActivity))
                    .build();
            MapViewLocalContentClient mapViewLocalContentClient = new MapViewLocalContentClient(mapViewAssetLoader);
            mapViewLocalContentClient.setCountry(countryRegionsToString(countrySegments), countryRegionsToString(countryRegions));
            mapView.setWebViewClient(mapViewLocalContentClient);
            mapView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
        }
    }

    private static class MapViewLocalContentClient
            extends WebViewClientCompat {

        private final WebViewAssetLoader assetLoader;

        private String country;
        private String countryRegions;

        MapViewLocalContentClient(WebViewAssetLoader assetLoader) {
            this.assetLoader = assetLoader;
        }

        public void setCountry(
                String country,
                String countryRegions) {
            this.country = country;
            this.countryRegions = countryRegions;
        }

        @Override
        @RequiresApi(21)
        public WebResourceResponse shouldInterceptRequest(
                WebView view,
                WebResourceRequest request) {
            return assetLoader.shouldInterceptRequest(request.getUrl());
        }

        @Override
        @SuppressWarnings("deprecation")
        public WebResourceResponse shouldInterceptRequest(
                WebView view,
                String url) {
            return assetLoader.shouldInterceptRequest(Uri.parse(url));
        }

        public void onPageFinished(
                WebView view,
                String mapPageUrl) {
            view.loadUrl("javascript:drawMapCountryRegions(" + country + "," + countryRegions + ")");
        }
    }
}
