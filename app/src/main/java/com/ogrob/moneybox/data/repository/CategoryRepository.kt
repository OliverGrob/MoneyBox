package com.ogrob.moneybox.data.repository

import android.app.Application
import com.ogrob.moneybox.persistence.ExpenseRoomDatabase
import com.ogrob.moneybox.persistence.dao.CategoryDao
import com.ogrob.moneybox.persistence.model.Category
import kotlinx.coroutines.CoroutineScope

class CategoryRepository(
    application: Application,
    coroutineScope: CoroutineScope
) {

    private val categoryDao: CategoryDao


    init {
        val expenseRoomDatabase = ExpenseRoomDatabase.getDatabase(application, coroutineScope)
        this.categoryDao= expenseRoomDatabase.categoryDao()
    }


    suspend fun getAllCategoriesWithExpenses() = categoryDao.getAllCategoriesWithExpenses()

    suspend fun getAllCategories() = categoryDao.getAllCategories()

    suspend fun addNewCategory(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun deleteCategoryById(categoryId: Long) {
        categoryDao.deleteCategoryById(categoryId)
    }

}