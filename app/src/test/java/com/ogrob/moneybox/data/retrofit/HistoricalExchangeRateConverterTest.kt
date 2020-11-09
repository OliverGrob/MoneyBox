package com.ogrob.moneybox.data.retrofit

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class HistoricalExchangeRateConverterTest {

    private val exchangeRatesJson = JsonObject().apply {
        addProperty( "CAD", 1.5556)
        addProperty("HKD", 9.0706)
        addProperty("ISK", 164.4)
        addProperty("PHP", 56.635)
        addProperty("DKK", 7.4466)
        addProperty("HUF", 367.45)
        addProperty("CZK", 27.251)
        addProperty("AUD", 1.6563)
        addProperty("RON", 4.8725)
        addProperty("SEK", 10.365)
        addProperty("IDR", 17108.33)
        addProperty("INR", 87.1115)
        addProperty("BRL", 6.7607)
        addProperty("RUB", 92.4606)
        addProperty("HRK", 7.5748)
        addProperty("JPY", 122.36)
        addProperty("THB", 36.439)
        addProperty("CHF", 1.0698)
        addProperty("SGD", 1.5952)
        addProperty("PLN", 4.6222)
        addProperty("BGN", 1.9558)
        addProperty("TRY", 9.794)
        addProperty("CNY", 7.8158)
        addProperty("NOK", 11.094)
        addProperty("NZD", 1.7565)
        addProperty("ZAR", 19.0359)
        addProperty("USD", 1.1698)
        addProperty("MXN", 24.8416)
        addProperty("ILS", 3.9881)
        addProperty("GBP", 0.90208)
        addProperty("KRW", 1324.2)
        addProperty("MYR", 4.8588)
    }
    private val jsonString = JsonObject().apply {
        addProperty("base", "EUR")
        addProperty("date", "2020-10-30")
        add("rates", exchangeRatesJson)
    }

    @Test
    fun `test convert exchange rate string to historical exchange rate`() {
        val exchangeRates = Gson().fromJson(jsonString, ExchangeRates::class.java)

        val historicalExchangeRates = HistoricalExchangeRateConverter.convertToHistoricalExchangeRates(listOf(exchangeRates))
        val historicalExchangeRate = historicalExchangeRates[0]

        assertEquals(historicalExchangeRate.date, LocalDate.of(2020, 10, 30))
        assertEquals(historicalExchangeRate.baseCurrency, "EUR")
        assertEquals(historicalExchangeRate.exchangeRateCad, 1.5556)
        assertEquals(historicalExchangeRate.exchangeRateHkd, 9.0706)
        assertEquals(historicalExchangeRate.exchangeRateIsk, 164.4)
        assertEquals(historicalExchangeRate.exchangeRatePhp, 56.635)
        assertEquals(historicalExchangeRate.exchangeRateDkk, 7.4466)
        assertEquals(historicalExchangeRate.exchangeRateHuf, 367.45)
        assertEquals(historicalExchangeRate.exchangeRateCzk, 27.251)
        assertEquals(historicalExchangeRate.exchangeRateAud, 1.6563)
        assertEquals(historicalExchangeRate.exchangeRateRon, 4.8725)
        assertEquals(historicalExchangeRate.exchangeRateSek, 10.365)
        assertEquals(historicalExchangeRate.exchangeRateIdr, 17108.33)
        assertEquals(historicalExchangeRate.exchangeRateInr, 87.1115)
        assertEquals(historicalExchangeRate.exchangeRateBrl, 6.7607)
        assertEquals(historicalExchangeRate.exchangeRateRub, 92.4606)
        assertEquals(historicalExchangeRate.exchangeRateHrk, 7.5748)
        assertEquals(historicalExchangeRate.exchangeRateJpy, 122.36)
        assertEquals(historicalExchangeRate.exchangeRateThb, 36.439)
        assertEquals(historicalExchangeRate.exchangeRateChf, 1.0698)
        assertEquals(historicalExchangeRate.exchangeRateSgd, 1.5952)
        assertEquals(historicalExchangeRate.exchangeRatePln, 4.6222)
        assertEquals(historicalExchangeRate.exchangeRateBgn, 1.9558)
        assertEquals(historicalExchangeRate.exchangeRateTry, 9.794)
        assertEquals(historicalExchangeRate.exchangeRateCny, 7.8158)
        assertEquals(historicalExchangeRate.exchangeRateNok, 11.094)
        assertEquals(historicalExchangeRate.exchangeRateNzd, 1.7565)
        assertEquals(historicalExchangeRate.exchangeRateZar, 19.0359)
        assertEquals(historicalExchangeRate.exchangeRateUsd, 1.1698)
        assertEquals(historicalExchangeRate.exchangeRateMxn, 24.8416)
        assertEquals(historicalExchangeRate.exchangeRateIls, 3.9881)
        assertEquals(historicalExchangeRate.exchangeRateGbp, 0.90208)
        assertEquals(historicalExchangeRate.exchangeRateKrw, 1324.2)
        assertEquals(historicalExchangeRate.exchangeRateMyr, 4.8588)
    }

}