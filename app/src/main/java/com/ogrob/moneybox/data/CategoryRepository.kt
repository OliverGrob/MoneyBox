package com.ogrob.moneybox.data

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.ogrob.moneybox.persistence.ExpenseRoomDatabase
import com.ogrob.moneybox.persistence.dao.CategoryDao
import com.ogrob.moneybox.persistence.model.Category

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
        InsertAsyncTask(this.categoryDao).execute(category)
    }

    fun deleteCategory(category: Category) {
        DeleteAsyncTask(this.categoryDao).execute(category)
    }



    private class InsertAsyncTask (private val asyncTaskDao: CategoryDao) : AsyncTask<Category, Void, Void>() {

        override fun doInBackground(vararg params: Category): Void? {
            asyncTaskDao.insert(params[0])
            return null
        }
    }

    private class DeleteAsyncTask (private val asyncTaskDao: CategoryDao) : AsyncTask<Category, Void, Void>() {

        override fun doInBackground(vararg params: Category): Void? {
            asyncTaskDao.delete(params[0])
            return null
        }
    }
}