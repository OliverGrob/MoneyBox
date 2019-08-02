package com.ogrob.moneybox.persistence.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(obj: T)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg obj: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(obj: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(vararg obj: T)

    @Delete
    fun delete(obj: T)

    @Delete
    fun deleteAll(vararg obj: T)
}