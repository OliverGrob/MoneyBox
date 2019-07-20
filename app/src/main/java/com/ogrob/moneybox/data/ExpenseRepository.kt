package com.ogrob.moneybox.data

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.ogrob.moneybox.persistence.ExpenseRoomDatabase
import com.ogrob.moneybox.persistence.dao.ExpenseDao
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense

class ExpenseRepository(application: Application) {

    private val expenseDao: ExpenseDao

    private val allExpenses: LiveData<List<CategoryWithExpenses>>


    init {
        val expenseRoomDatabase = ExpenseRoomDatabase.getInstance(application)!!
        this.expenseDao = expenseRoomDatabase.expenseDao()
        this.allExpenses = this.expenseDao.getAllExpenses()
    }


    fun getExpenses() = this.allExpenses

    fun addNewExpense(expense: Expense) {
        InsertAsyncTask(this.expenseDao).execute(expense)
    }

    fun updateExpense(expense: Expense) {
        UpdateAsyncTask(this.expenseDao).execute(expense)
    }

    fun deleteExpense(expense: Expense) {
        DeleteAsyncTask(this.expenseDao).execute(expense)
    }



    private class InsertAsyncTask (private val asyncTaskDao: ExpenseDao) : AsyncTask<Expense, Void, Void>() {

        override fun doInBackground(vararg params: Expense): Void? {
            asyncTaskDao.insert(params[0])
            return null
        }
    }

    private class DeleteAsyncTask (private val asyncTaskDao: ExpenseDao) : AsyncTask<Expense, Void, Void>() {

        override fun doInBackground(vararg params: Expense): Void? {
            asyncTaskDao.delete(params[0])
            return null
        }
    }

    private class UpdateAsyncTask (private val asyncTaskDao: ExpenseDao) : AsyncTask<Expense, Void, Void>() {

        override fun doInBackground(vararg params: Expense): Void? {
            asyncTaskDao.update(params[0])
            return null
        }
    }
}