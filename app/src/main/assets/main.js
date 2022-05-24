// Create a map
let map = new ol.Map({
    target: 'map',
    layers: [
        new ol.layer.Tile({
            source: new ol.source.OSM()
        })
    ],
    view: new ol.View({
        center: ol.proj.fromLonLat([0, 0]),
        zoom: 4
    })
});

function createMapPolygonStyle(mapPolygonBorderColor, mapPolygonBgColor) {
    return new ol.style.Style({
        stroke: new ol.style.Stroke({
            width: 2.0,
            color: mapPolygonBorderColor
        }),
        fill: new ol.style.Fill({
            color: 'rgba(' +
                    parseInt(mapPolygonBgColor.slice(1, 3), 16) +
                    ',' +
                    parseInt(mapPolygonBgColor.slice(3, 5), 16) +
                    ',' +
                    parseInt(mapPolygonBgColor.slice(5, 7), 16) +
                    ',0.2)'
        })
    });
}

// Create appearance settings for a country and its regions
let mapCountryStyle = createMapPolygonStyle('#E91E63', '#311B92');
let mapCountryRegionStyle = createMapPolygonStyle('#FF5722', '#880E4F');
let mapCountryHiddenRegionStyle = new ol.style.Style(null);

function setMapPolygon(mapLayerSource, mapPolygonPointCoordinates, mapPolygonStyle) {
    let mapPolygon = new ol.geom.Polygon([mapPolygonPointCoordinates]);
    mapPolygon.applyTransform(ol.proj.getTransform('EPSG:4326', 'EPSG:3857'));
    let mapPolygonFeature = new ol.Feature(mapPolygon);
    mapPolygonFeature.setStyle(mapPolygonStyle);
    mapLayerSource.addFeature(mapPolygonFeature);
    return mapPolygonFeature;
}

// A number of the visible country region
let mapCountryVisibleRegionIndex = -1;

function drawMapCountryRegions(mapCountryBorderPointCoordinates, mapCountryRegionBorderPointCoordinates) {
    let mapLayerSource = new ol.source.Vector();

    // Set a country
    let mapCountryFeatures = [];
    for (mapCountrySegmentIndex = 0; mapCountrySegmentIndex < mapCountryBorderPointCoordinates.length; mapCountrySegmentIndex++) {
        mapCountryFeatures.push(setMapPolygon(mapLayerSource, mapCountryBorderPointCoordinates[mapCountrySegmentIndex], mapCountryStyle));
    }

    // Set country regions
    let mapCountryRegionFeatures = [];
    for (mapCountryRegionIndex = 0; mapCountryRegionIndex < mapCountryRegionBorderPointCoordinates.length; mapCountryRegionIndex++) {
        mapCountryRegionFeatures.push(setMapPolygon(mapLayerSource, mapCountryRegionBorderPointCoordinates[mapCountryRegionIndex], mapCountryHiddenRegionStyle));
    };

    // Add the country and its regions onto the map
    map.addLayer(new ol.layer.Vector({
        source: mapLayerSource
    }));

    map.on('click', mapClickingEvent => {

        // Find a selected country region
        let mapCountrySelectedRegionIndex = 0;
        for ( ; mapCountrySelectedRegionIndex < mapCountryRegionFeatures.length; mapCountrySelectedRegionIndex++) {
            if (mapCountryRegionFeatures[mapCountrySelectedRegionIndex].getGeometry().intersectsCoordinate(mapClickingEvent.coordinate)) {
                break;
            }
        }

        if (mapCountrySelectedRegionIndex === mapCountryRegionFeatures.length) {

            // No one of the country regions has been selected
            if (mapCountryVisibleRegionIndex !== -1) {
                mapCountryRegionFeatures[mapCountryVisibleRegionIndex].setStyle(mapCountryHiddenRegionStyle);
                for (mapCountrySegmentIndex = 0; mapCountrySegmentIndex < mapCountryFeatures.length; mapCountrySegmentIndex++) {
                    mapCountryFeatures[mapCountrySegmentIndex].setStyle(mapCountryStyle);
                }
                mapCountryVisibleRegionIndex = -1;
            }
        } else if (mapCountrySelectedRegionIndex !== mapCountryVisibleRegionIndex) {

            // One of the country regions has been selected, and it's not visible
            if (mapCountryVisibleRegionIndex === -1) {
                for (mapCountrySegmentIndex = 0; mapCountrySegmentIndex < mapCountryFeatures.length; mapCountrySegmentIndex++) {
                    mapCountryFeatures[mapCountrySegmentIndex].setStyle(mapCountryHiddenRegionStyle);
                }
            } else {
                mapCountryRegionFeatures[mapCountryVisibleRegionIndex].setStyle(mapCountryHiddenRegionStyle);
            }
            mapCountryRegionFeatures[mapCountrySelectedRegionIndex].setStyle(mapCountryRegionStyle);
            mapCountryVisibleRegionIndex = mapCountrySelectedRegionIndex;
        }
    });
}
