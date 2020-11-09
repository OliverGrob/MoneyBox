package com.ogrob.moneybox.data.retrofit

import com.ogrob.moneybox.persistence.model.HistoricalExchangeRate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object HistoricalExchangeRateConverter {

    fun convertToHistoricalExchangeRates(exchangeRates: List<ExchangeRates>): List<HistoricalExchangeRate> =
        exchangeRates
            .asSequence()
            .map(HistoricalExchangeRateConverter::convertToHistoricalExchangeRate)
            .toList()

    fun convertToHistoricalExchangeRate(exchangeRates: ExchangeRates): HistoricalExchangeRate =
        convertToHistoricalExchangeRateWithCustomDate(exchangeRates, LocalDate.parse(exchangeRates.date, DateTimeFormatter.ISO_LOCAL_DATE))

    fun convertToHistoricalExchangeRateWithCustomDate(exchangeRates: ExchangeRates, date: LocalDate): HistoricalExchangeRate =
        HistoricalExchangeRate(
            date,
            exchangeRates.baseCurrency,
            exchangeRates.rates.CAD,
            exchangeRates.rates.HKD,
            exchangeRates.rates.ISK,
            exchangeRates.rates.PHP,
            exchangeRates.rates.DKK,
            exchangeRates.rates.HUF,
            exchangeRates.rates.CZK,
            exchangeRates.rates.AUD,
            exchangeRates.rates.RON,
            exchangeRates.rates.SEK,
            exchangeRates.rates.IDR,
            exchangeRates.rates.INR,
            exchangeRates.rates.BRL,
            exchangeRates.rates.RUB,
            exchangeRates.rates.HRK,
            exchangeRates.rates.JPY,
            exchangeRates.rates.THB,
            exchangeRates.rates.CHF,
            exchangeRates.rates.SGD,
            exchangeRates.rates.PLN,
            exchangeRates.rates.BGN,
            exchangeRates.rates.TRY,
            exchangeRates.rates.CNY,
            exchangeRates.rates.NOK,
            exchangeRates.rates.NZD,
            exchangeRates.rates.ZAR,
            exchangeRates.rates.USD,
            exchangeRates.rates.MXN,
            exchangeRates.rates.ILS,
            exchangeRates.rates.GBP,
            exchangeRates.rates.KRW,
            exchangeRates.rates.MYR
        )

}