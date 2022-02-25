package irimi.currencyconverterdemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity
        extends AppCompatActivity {

    private static String[] currencyCodes = null;
    private static int[] currencyDenominations = null;
    private static String[] currencyNames = null;
    private static double[] currencyRates = null;

    //  0 - waiting for data
    //  1 - data received
    // -1 - data inaccessible
    private static int dataState = 0;

    public static synchronized void setCurrencies(
            String[] newCurrencyCodes,
            int[] newCurrencyDenominations,
            String[] newCurrencyNames,
            double[] newCurrencyRates) {
        if (newCurrencyCodes == null) {
            dataState = -1;
            return;
        }
        currencyCodes = newCurrencyCodes;
        currencyDenominations = newCurrencyDenominations;
        currencyNames = newCurrencyNames;
        currencyRates = newCurrencyRates;
        dataState = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BankDataCallingThread bankDataCallingThread = new BankDataCallingThread();
        bankDataCallingThread.start();
        setContentView(R.layout.activity_main);
        getCurrencies();
    }

    private void getCurrencies() {

        // TODO Get currency values from the local storage

        // Get currency values from the bank
        while (true) {
            if (dataState == 1) {
                showCurrencies();
                return;
            } else if (dataState == -1)
                break;
        }
    }

    private void showCurrencies() {

        // Remove old currency values from the main fragment
        LinearLayout layout = findViewById(R.id.layout_main);
        int layoutChildIndex = layout.getChildCount();
        for ( ; layoutChildIndex > -1; layoutChildIndex--) {
            View layoutChild = layout.getChildAt(layoutChildIndex);
            layout.removeView(layoutChild);
        }

        // Show new currency values at the main fragment
        LinearLayout.LayoutParams currencyViewSize = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        currencyViewSize.setMargins(8, 4, 8, 4);
        DecimalFormat currencyRateFormat = new DecimalFormat("#.##########");
        for (layoutChildIndex = 0; layoutChildIndex < currencyCodes.length; layoutChildIndex++) {
            TextView currencyView = new TextView(this);
            currencyView.setLayoutParams(currencyViewSize);
            currencyView.setBackgroundColor(getResources().getColor(R.color.blue_50));
            currencyView.setPadding(8, 8, 8, 8);
            currencyView.setGravity(Gravity.START);
            currencyView.setTextSize(9F);
            currencyView.setTextColor(getResources().getColor(R.color.purple_900));
            StringBuilder currency_asMutableText = new StringBuilder();
            currency_asMutableText.append(currencyDenominations[layoutChildIndex]);
            currency_asMutableText.append(" ");
            currency_asMutableText.append(currencyNames[layoutChildIndex]);
            currency_asMutableText.append(" = ");
            String currencyRate_asText = currencyRateFormat.format(currencyRates[layoutChildIndex]);
            currency_asMutableText.append(currencyRate_asText);
            currency_asMutableText.append(" рублей");
            String currencyConverterDialogText = currency_asMutableText.toString();
            currencyView.setText(currencyConverterDialogText);

            // Turn currency views into links to the currency converter
            currencyView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View currencyView) {
                    LinearLayout currencyViewParent = (LinearLayout) currencyView.getParent();
                    int currencyViewIndex = currencyViewParent.indexOfChild(currencyView);
                    showCurrencyConverter(currencyViewIndex);
                }
            });

            layout.addView(currencyView);
        }
    }

    private void showCurrencyConverter(int currencyIndex) {
        AlertDialog.Builder currencyConverterDialogBuilder = new AlertDialog.Builder(this);
        StringBuilder currencyConverterDialogText_asMutable = new StringBuilder();
        currencyConverterDialogText_asMutable.append("1 ");
        currencyConverterDialogText_asMutable.append(currencyCodes[currencyIndex]);
        currencyConverterDialogText_asMutable.append(" = ");
        double currencyRate = currencyRates[currencyIndex] / currencyDenominations[currencyIndex];
        DecimalFormat currencyRateFormat = new DecimalFormat("#.##########");
        String currencyRate_asText = currencyRateFormat.format(currencyRate);
        currencyConverterDialogText_asMutable.append(currencyRate_asText);
        currencyConverterDialogText_asMutable.append(" RUB");
        String currency_asText = currencyConverterDialogText_asMutable.toString();
        currencyConverterDialogBuilder.setTitle(currency_asText);

        // What the fuck is that i?
        currencyConverterDialogBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface currencyConverterDialogInterface, int i) {
                currencyConverterDialogInterface.dismiss();
            }
        });
        AlertDialog currencyConverterDialog = currencyConverterDialogBuilder.create();
        currencyConverterDialog.show();
    }
}
