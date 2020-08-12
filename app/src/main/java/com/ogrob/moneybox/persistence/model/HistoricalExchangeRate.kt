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

    @ColumnInfo(name = "exchange_rate_cad")
    val exchangeRateCad: String,

    @ColumnInfo(name = "exchange_rate_hkd")
    val exchangeRateHkd: String,

    @ColumnInfo(name = "exchange_rate_isk")
    val exchangeRateIsk: String,

    @ColumnInfo(name = "exchange_rate_php")
    val exchangeRatePhp: String,

    @ColumnInfo(name = "exchange_rate_dkk")
    val exchangeRateDkk: String,

    @ColumnInfo(name = "exchange_rate_huf")
    val exchangeRateHuf: String,

    @ColumnInfo(name = "exchange_rate_czk")
    val exchangeRateCzk: String,

    @ColumnInfo(name = "exchange_rate_aud")
    val exchangeRateAud: String,

    @ColumnInfo(name = "exchange_rate_ron")
    val exchangeRateRon: String,

    @ColumnInfo(name = "exchange_rate_sek")
    val exchangeRateSek: String,

    @ColumnInfo(name = "exchange_rate_idr")
    val exchangeRateIdr: String,

    @ColumnInfo(name = "exchange_rate_inr")
    val exchangeRateInr: String,

    @ColumnInfo(name = "exchange_rate_brl")
    val exchangeRateBrl: String,

    @ColumnInfo(name = "exchange_rate_rub")
    val exchangeRateRub: String,

    @ColumnInfo(name = "exchange_rate_hrk")
    val exchangeRateHrk: String,

    @ColumnInfo(name = "exchange_rate_jpy")
    val exchangeRateJpy: String,

    @ColumnInfo(name = "exchange_rate_thb")
    val exchangeRateThb: String,

    @ColumnInfo(name = "exchange_rate_chf")
    val exchangeRateChf: String,

    @ColumnInfo(name = "exchange_rate_sgd")
    val exchangeRateSgd: String,

    @ColumnInfo(name = "exchange_rate_pln")
    val exchangeRatePln: String,

    @ColumnInfo(name = "exchange_rate_bgn")
    val exchangeRateBgn: String,

    @ColumnInfo(name = "exchange_rate_try")
    val exchangeRateTry: String,

    @ColumnInfo(name = "exchange_rate_cny")
    val exchangeRateCny: String,

    @ColumnInfo(name = "exchange_rate_nok")
    val exchangeRateNok: String,

    @ColumnInfo(name = "exchange_rate_nzd")
    val exchangeRateNzd: String,

    @ColumnInfo(name = "exchange_rate_zar")
    val exchangeRateZar: String,

    @ColumnInfo(name = "exchange_rate_usd")
    val exchangeRateUsd: String,

    @ColumnInfo(name = "exchange_rate_mxn")
    val exchangeRateMxn: String,

    @ColumnInfo(name = "exchange_rate_ils")
    val exchangeRateIls: String,

    @ColumnInfo(name = "exchange_rate_gbp")
    val exchangeRateGbp: String,

    @ColumnInfo(name = "exchange_rate_krw")
    val exchangeRateKrw: String,

    @ColumnInfo(name = "exchange_rate_myr")
    val exchangeRateMyr: String
) {

    @Ignore
    constructor(date: LocalDate,
                exchangeRateCad: String,
                exchangeRateHkd: String,
                exchangeRateIsk: String,
                exchangeRatePhp: String,
                exchangeRateDkk: String,
                exchangeRateHuf: String,
                exchangeRateCzk: String,
                exchangeRateAud: String,
                exchangeRateRon: String,
                exchangeRateSek: String,
                exchangeRateIdr: String,
                exchangeRateInr: String,
                exchangeRateBrl: String,
                exchangeRateRub: String,
                exchangeRateHrk: String,
                exchangeRateJpy: String,
                exchangeRateThb: String,
                exchangeRateChf: String,
                exchangeRateSgd: String,
                exchangeRatePln: String,
                exchangeRateBgn: String,
                exchangeRateTry: String,
                exchangeRateCny: String,
                exchangeRateNok: String,
                exchangeRateNzd: String,
                exchangeRateZar: String,
                exchangeRateUsd: String,
                exchangeRateMxn: String,
                exchangeRateIls: String,
                exchangeRateGbp: String,
                exchangeRateKrw: String,
                exchangeRateMyr: String):
            this(0,
                date,
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