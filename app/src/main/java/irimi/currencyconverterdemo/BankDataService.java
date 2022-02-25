package irimi.currencyconverterdemo;

import irimi.currencyconverterdemo.entity.BankData;

import retrofit2.Call;
import retrofit2.http.GET;

public interface BankDataService {

    @GET("/daily_json.js")
    Call<BankData> get();
}
