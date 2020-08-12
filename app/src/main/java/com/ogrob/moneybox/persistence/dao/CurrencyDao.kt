package com.ogrob.moneybox.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.ogrob.moneybox.persistence.model.Currency

@Dao
interface CurrencyDao : BaseDao<Currency> {

    @Query("SELECT * FROM currency")
    fun getAllCurrencies(): LiveData<List<Currency>>

}