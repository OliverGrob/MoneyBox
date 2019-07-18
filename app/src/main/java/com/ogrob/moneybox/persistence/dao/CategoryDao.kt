package com.ogrob.moneybox.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.ogrob.moneybox.persistence.model.Category

@Dao
interface CategoryDao: BaseDao<Category> {

    @Query("SELECT * FROM category")
    fun getAllCategories(): LiveData<List<Category>>
}