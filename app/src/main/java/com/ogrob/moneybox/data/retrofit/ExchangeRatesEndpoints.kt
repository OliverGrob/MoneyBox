package com.ogrob.moneybox.data.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRatesEndpoints {

    @GET("{date}")
    fun getExchangeRatesForDate(@Path("date") date: String): Call<ExchangeRates>

}