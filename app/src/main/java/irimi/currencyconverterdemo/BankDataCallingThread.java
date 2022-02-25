package irimi.currencyconverterdemo;

import java.io.IOException;

import irimi.currencyconverterdemo.entity.BankData;
import irimi.currencyconverterdemo.entity.Currencies;
import irimi.currencyconverterdemo.entity.Currency;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BankDataCallingThread
        extends Thread {

    @Override
    public void run() {

        // Get currency values from the bank
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.cbr-xml-daily.ru/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BankDataService bankDataService = retrofit.create(BankDataService.class);
        Call<BankData> bankDataCall = bankDataService.get();
        try {
            Response<BankData> bankDataCallResponse = bankDataCall.execute();
            BankData bankData = bankDataCallResponse.body();
            Currencies bankDataCurrencies_asObject = bankData.getValute();
            Currency[] bankDataCurrencies = new Currency[] {
                    bankDataCurrencies_asObject.getAud(),
                    bankDataCurrencies_asObject.getAzn(),
                    bankDataCurrencies_asObject.getGbp(),
                    bankDataCurrencies_asObject.getAmd(),
                    bankDataCurrencies_asObject.getByn(),
                    bankDataCurrencies_asObject.getBgn(),
                    bankDataCurrencies_asObject.getBrl(),
                    bankDataCurrencies_asObject.getHuf(),
                    bankDataCurrencies_asObject.getHkd(),
                    bankDataCurrencies_asObject.getDkk(),
                    bankDataCurrencies_asObject.getUsd(),
                    bankDataCurrencies_asObject.getEur(),
                    bankDataCurrencies_asObject.getInr(),
                    bankDataCurrencies_asObject.getKzt(),
                    bankDataCurrencies_asObject.getCad(),
                    bankDataCurrencies_asObject.getKgs(),
                    bankDataCurrencies_asObject.getCny(),
                    bankDataCurrencies_asObject.getMdl(),
                    bankDataCurrencies_asObject.getNok(),
                    bankDataCurrencies_asObject.getPln(),
                    bankDataCurrencies_asObject.getRon(),
                    bankDataCurrencies_asObject.getXdr(),
                    bankDataCurrencies_asObject.getSgd(),
                    bankDataCurrencies_asObject.getTjs(),
                    bankDataCurrencies_asObject.getTry(),
                    bankDataCurrencies_asObject.getTmt(),
                    bankDataCurrencies_asObject.getUzs(),
                    bankDataCurrencies_asObject.getUah(),
                    bankDataCurrencies_asObject.getCzk(),
                    bankDataCurrencies_asObject.getSek(),
                    bankDataCurrencies_asObject.getChf(),
                    bankDataCurrencies_asObject.getZar(),
                    bankDataCurrencies_asObject.getKrw(),
                    bankDataCurrencies_asObject.getJpy()
            };
            String[] bankDataCurrencyCodes = new String[34];
            int[] bankDataCurrencyDenominations = new int[34];
            String[] bankDataCurrencyNames = new String[34];
            double[] bankDataCurrencyRates = new double[34];
            for (int bankDataCurrencyIndex = 0; bankDataCurrencyIndex < 34; bankDataCurrencyIndex++) {
                bankDataCurrencyCodes[bankDataCurrencyIndex] = bankDataCurrencies[bankDataCurrencyIndex].getCharCode();
                bankDataCurrencyDenominations[bankDataCurrencyIndex] = bankDataCurrencies[bankDataCurrencyIndex].getNominal();
                String bankDataCurrencyName = bankDataCurrencies[bankDataCurrencyIndex].getName();
                int bankDataCurrencyNameSecondCharacter = bankDataCurrencyName.charAt(1);
                if (bankDataCurrencyNameSecondCharacter != 'Д') {
                    StringBuilder bankDataCurrencyName_asMutable = new StringBuilder(bankDataCurrencyName);
                    char bankDataCurrencyNameFirstCharacter = bankDataCurrencyName_asMutable.charAt(0);
                    bankDataCurrencyNameFirstCharacter = Character.toLowerCase(bankDataCurrencyNameFirstCharacter);
                    bankDataCurrencyName_asMutable.delete(0, 1);
                    bankDataCurrencyName_asMutable.insert(0, bankDataCurrencyNameFirstCharacter);
                    bankDataCurrencyName = bankDataCurrencyName_asMutable.toString();
                }
                bankDataCurrencyNames[bankDataCurrencyIndex] = bankDataCurrencyName;
                bankDataCurrencyRates[bankDataCurrencyIndex] = bankDataCurrencies[bankDataCurrencyIndex].getValue();
            }

            // Send currency values to the main thread
            MainActivity.setCurrencies(bankDataCurrencyCodes, bankDataCurrencyDenominations, bankDataCurrencyNames, bankDataCurrencyRates);
        } catch (IOException exception) {
            MainActivity.setCurrencies(null, null, null, null);
        }
    }
}
