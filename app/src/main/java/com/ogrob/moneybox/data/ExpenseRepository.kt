package com.ogrob.moneybox.data

import android.app.Application
import androidx.lifecycle.LiveData
import com.ogrob.moneybox.persistence.ExpenseRoomDatabase
import com.ogrob.moneybox.persistence.dao.ExpenseDao
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.BackgroundOperationAsyncTask

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
        BackgroundOperationAsyncTask(expenseDao::insert).execute(expense)
    }

    fun updateExpense(expense: Expense) {
        BackgroundOperationAsyncTask(expenseDao::update).execute(expense)
    }

    fun deleteExpense(expense: Expense) {
        BackgroundOperationAsyncTask(expenseDao::delete).execute(expense)
    }

}