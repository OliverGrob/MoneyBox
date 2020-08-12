package com.ogrob.moneybox.data.repository

import android.app.Application
import com.ogrob.moneybox.persistence.ExpenseRoomDatabase
import com.ogrob.moneybox.persistence.dao.ExpenseDao
import com.ogrob.moneybox.persistence.model.Expense
import kotlinx.coroutines.CoroutineScope

class ExpenseRepository(
    application: Application,
    coroutineScope: CoroutineScope
) {

    private val expenseDao: ExpenseDao


    init {
        val expenseRoomDatabase = ExpenseRoomDatabase.getDatabase(application, coroutineScope)
        this.expenseDao = expenseRoomDatabase.expenseDao()
    }


    fun getCategoriesWithExpenses() = this.expenseDao.getAllCategoriesWithExpenses()

    suspend fun addNewExpense(expense: Expense) {
        expenseDao.insert(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.update(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense)
    }

}