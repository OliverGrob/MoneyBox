package com.ogrob.moneybox.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses

@Dao
interface CategoryDao: BaseDao<Category> {

    @Query("SELECT * FROM category")
    suspend fun getAllCategories(): List<Category>

    @Transaction
    @Query("SELECT * FROM category")
    suspend fun getAllCategoriesWithExpenses(): List<CategoryWithExpenses>

    @Query("DELETE FROM category WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

}