package com.ogrob.moneybox.data

import android.app.Application
import androidx.lifecycle.LiveData
import com.ogrob.moneybox.persistence.ExpenseRoomDatabase
import com.ogrob.moneybox.persistence.dao.CategoryDao
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.utils.BackgroundOperationAsyncTask

class CategoryRepository(application: Application) {

    private val categoryDao: CategoryDao

    private val allCategories: LiveData<List<Category>>


    init {
        val expenseRoomDatabase = ExpenseRoomDatabase.getInstance(application)!!
        this.categoryDao= expenseRoomDatabase.categoryDao()
        this.allCategories = this.categoryDao.getAllCategories()
    }


    fun getCategories() = this.allCategories

    fun addNewCategory(category: Category) {
        BackgroundOperationAsyncTask(categoryDao::insert).execute(category)
    }

    fun updateCategory(category: Category) {
        BackgroundOperationAsyncTask(categoryDao::update).execute(category)
    }

    fun deleteCategory(category: Category) {
        BackgroundOperationAsyncTask(categoryDao::delete).execute(category)
    }

}