package com.ogrob.moneybox.persistence.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "historical_exchange_rate")
data class HistoricalExchangeRate(

    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "base_currency")
    val baseCurrency: String,

    @ColumnInfo(name = "exchange_rate_cad")
    val exchangeRateCad: Double,

    @ColumnInfo(name = "exchange_rate_hkd")
    val exchangeRateHkd: Double,

    @ColumnInfo(name = "exchange_rate_isk")
    val exchangeRateIsk: Double,

    @ColumnInfo(name = "exchange_rate_php")
    val exchangeRatePhp: Double,

    @ColumnInfo(name = "exchange_rate_dkk")
    val exchangeRateDkk: Double,

    @ColumnInfo(name = "exchange_rate_huf")
    val exchangeRateHuf: Double,

    @ColumnInfo(name = "exchange_rate_czk")
    val exchangeRateCzk: Double,

    @ColumnInfo(name = "exchange_rate_aud")
    val exchangeRateAud: Double,

    @ColumnInfo(name = "exchange_rate_ron")
    val exchangeRateRon: Double,

    @ColumnInfo(name = "exchange_rate_sek")
    val exchangeRateSek: Double,

    @ColumnInfo(name = "exchange_rate_idr")
    val exchangeRateIdr: Double,

    @ColumnInfo(name = "exchange_rate_inr")
    val exchangeRateInr: Double,

    @ColumnInfo(name = "exchange_rate_brl")
    val exchangeRateBrl: Double,

    @ColumnInfo(name = "exchange_rate_rub")
    val exchangeRateRub: Double,

    @ColumnInfo(name = "exchange_rate_hrk")
    val exchangeRateHrk: Double,

    @ColumnInfo(name = "exchange_rate_jpy")
    val exchangeRateJpy: Double,

    @ColumnInfo(name = "exchange_rate_thb")
    val exchangeRateThb: Double,

    @ColumnInfo(name = "exchange_rate_chf")
    val exchangeRateChf: Double,

    @ColumnInfo(name = "exchange_rate_sgd")
    val exchangeRateSgd: Double,

    @ColumnInfo(name = "exchange_rate_pln")
    val exchangeRatePln: Double,

    @ColumnInfo(name = "exchange_rate_bgn")
    val exchangeRateBgn: Double,

    @ColumnInfo(name = "exchange_rate_try")
    val exchangeRateTry: Double,

    @ColumnInfo(name = "exchange_rate_cny")
    val exchangeRateCny: Double,

    @ColumnInfo(name = "exchange_rate_nok")
    val exchangeRateNok: Double,

    @ColumnInfo(name = "exchange_rate_nzd")
    val exchangeRateNzd: Double,

    @ColumnInfo(name = "exchange_rate_zar")
    val exchangeRateZar: Double,

    @ColumnInfo(name = "exchange_rate_usd")
    val exchangeRateUsd: Double,

    @ColumnInfo(name = "exchange_rate_mxn")
    val exchangeRateMxn: Double,

    @ColumnInfo(name = "exchange_rate_ils")
    val exchangeRateIls: Double,

    @ColumnInfo(name = "exchange_rate_gbp")
    val exchangeRateGbp: Double,

    @ColumnInfo(name = "exchange_rate_krw")
    val exchangeRateKrw: Double,

    @ColumnInfo(name = "exchange_rate_myr")
    val exchangeRateMyr: Double
) {

    @Ignore
    constructor(
        date: LocalDate,
        baseCurrency: String,
        exchangeRateCad: Double,
        exchangeRateHkd: Double,
        exchangeRateIsk: Double,
        exchangeRatePhp: Double,
        exchangeRateDkk: Double,
        exchangeRateHuf: Double,
        exchangeRateCzk: Double,
        exchangeRateAud: Double,
        exchangeRateRon: Double,
        exchangeRateSek: Double,
        exchangeRateIdr: Double,
        exchangeRateInr: Double,
        exchangeRateBrl: Double,
        exchangeRateRub: Double,
        exchangeRateHrk: Double,
        exchangeRateJpy: Double,
        exchangeRateThb: Double,
        exchangeRateChf: Double,
        exchangeRateSgd: Double,
        exchangeRatePln: Double,
        exchangeRateBgn: Double,
        exchangeRateTry: Double,
        exchangeRateCny: Double,
        exchangeRateNok: Double,
        exchangeRateNzd: Double,
        exchangeRateZar: Double,
        exchangeRateUsd: Double,
        exchangeRateMxn: Double,
        exchangeRateIls: Double,
        exchangeRateGbp: Double,
        exchangeRateKrw: Double,
        exchangeRateMyr: Double):
            this(0,
                date,
                baseCurrency,
                exchangeRateCad,
                exchangeRateHkd,
                exchangeRateIsk,
                exchangeRatePhp,
                exchangeRateDkk,
                exchangeRateHuf,
                exchangeRateCzk,
                exchangeRateAud,
                exchangeRateRon,
                exchangeRateSek,
                exchangeRateIdr,
                exchangeRateInr,
                exchangeRateBrl,
                exchangeRateRub,
                exchangeRateHrk,
                exchangeRateJpy,
                exchangeRateThb,
                exchangeRateChf,
                exchangeRateSgd,
                exchangeRatePln,
                exchangeRateBgn,
                exchangeRateTry,
                exchangeRateCny,
                exchangeRateNok,
                exchangeRateNzd,
                exchangeRateZar,
                exchangeRateUsd,
                exchangeRateMxn,
                exchangeRateIls,
                exchangeRateGbp,
                exchangeRateKrw,
                exchangeRateMyr)
}