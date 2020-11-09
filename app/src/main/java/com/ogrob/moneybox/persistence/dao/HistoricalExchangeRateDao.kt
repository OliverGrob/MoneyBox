package com.ogrob.moneybox.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import com.ogrob.moneybox.persistence.model.HistoricalExchangeRate
import java.time.LocalDate

@Dao
interface HistoricalExchangeRateDao : BaseDao<HistoricalExchangeRate> {

    @Query("SELECT * FROM historical_exchange_rate")
    suspend fun getAllHistoricalExchangeRates(): List<HistoricalExchangeRate>

    @Query("SELECT * FROM historical_exchange_rate WHERE date = :date")
    suspend fun getHistoricalExchangeRateForDate(date: LocalDate): List<HistoricalExchangeRate>

}