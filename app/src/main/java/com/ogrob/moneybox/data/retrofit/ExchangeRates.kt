package com.ogrob.moneybox.data.retrofit

import com.google.gson.annotations.SerializedName

class ExchangeRates(

    @SerializedName("base")
    val baseCurrency: String,
    val date: String,
    val rates: ExchangeRate

)

class ExchangeRate(

    @SerializedName("CAD")
    val CAD: Double,

    @SerializedName("HKD")
    val HKD: Double,

    @SerializedName("ISK")
    val ISK: Double,

    @SerializedName("PHP")
    val PHP: Double,

    @SerializedName("DKK")
    val DKK: Double,

    @SerializedName("HUF")
    val HUF: Double,

    @SerializedName("CZK")
    val CZK: Double,

    @SerializedName("AUD")
    val AUD: Double,

    @SerializedName("RON")
    val RON: Double,

    @SerializedName("SEK")
    val SEK: Double,

    @SerializedName("IDR")
    val IDR: Double,

    @SerializedName("INR")
    val INR: Double,

    @SerializedName("BRL")
    val BRL: Double,

    @SerializedName("RUB")
    val RUB: Double,

    @SerializedName("HRK")
    val HRK: Double,

    @SerializedName("JPY")
    val JPY: Double,

    @SerializedName("THB")
    val THB: Double,

    @SerializedName("CHF")
    val CHF: Double,

    @SerializedName("SGD")
    val SGD: Double,

    @SerializedName("PLN")
    val PLN: Double,

    @SerializedName("BGN")
    val BGN: Double,

    @SerializedName("TRY")
    val TRY: Double,

    @SerializedName("CNY")
    val CNY: Double,

    @SerializedName("NOK")
    val NOK: Double,

    @SerializedName("NZD")
    val NZD: Double,

    @SerializedName("ZAR")
    val ZAR: Double,

    @SerializedName("USD")
    val USD: Double,

    @SerializedName("MXN")
    val MXN: Double,

    @SerializedName("ILS")
    val ILS: Double,

    @SerializedName("GBP")
    val GBP: Double,

    @SerializedName("KRW")
    val KRW: Double,

    @SerializedName("MYR")
    val MYR: Double,

)