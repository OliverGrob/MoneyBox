package com.ogrob.moneybox.data.retrofit

import com.ogrob.moneybox.utils.CURRENCY_EXCHANGE_RATES_BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {

    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(CURRENCY_EXCHANGE_RATES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()


    fun <T> buildService(service: Class<T>): T = retrofit.create(service)

}