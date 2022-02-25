package irimi.currencyconverterdemo;

import irimi.currencyconverterdemo.entity.BankData;
import retrofit2.Call;

public class BankDataRepo {

    private BankDataService service;

    public BankDataRepo(BankDataService service) {
        this.service = service;
    }

    public Call<BankData> get() {
        Call<BankData> call = service.get();
        return call;
    }
}
