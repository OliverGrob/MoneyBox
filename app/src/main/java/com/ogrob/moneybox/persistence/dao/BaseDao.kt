package com.ogrob.moneybox.persistence.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy

interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(vararg obj: T)
}