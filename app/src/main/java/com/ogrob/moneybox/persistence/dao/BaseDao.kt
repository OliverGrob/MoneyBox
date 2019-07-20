package com.ogrob.moneybox.persistence.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(vararg obj: T)

    @Delete
    fun delete(vararg obj: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg obj: T)
}