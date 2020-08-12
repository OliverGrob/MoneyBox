package com.ogrob.moneybox.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.ogrob.moneybox.persistence.model.HistoricalExchangeRate

@Dao
interface HistoricalExchangeRateDao : BaseDao<HistoricalExchangeRate> {

    @Query("SELECT * FROM historical_exchange_rate")
    fun getAllHistoricalExchangeRates(): LiveData<List<HistoricalExchangeRate>>

}