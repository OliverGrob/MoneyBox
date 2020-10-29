package com.ogrob.moneybox.data.repository

import android.app.Application
import com.ogrob.moneybox.persistence.ExpenseRoomDatabase
import com.ogrob.moneybox.persistence.dao.ExpenseDao
import com.ogrob.moneybox.persistence.model.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.Month

class ExpenseRepository(
    application: Application,
    coroutineScope: CoroutineScope
) {

    private val expenseDao: ExpenseDao


    init {
        val expenseRoomDatabase = ExpenseRoomDatabase.getDatabase(application, coroutineScope)
        this.expenseDao = expenseRoomDatabase.expenseDao()
    }


    fun getAllCategoriesWithExpenses() = this.expenseDao.getAllCategoriesWithExpenses()

    suspend fun addNewExpense(expense: Expense) {
        expenseDao.insert(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.update(expense)
    }

    suspend fun deleteExpenseById(expenseId: Long) {
        expenseDao.deleteExpenseById(expenseId)
    }








    suspend fun speedQuery(year: Int, month: Month) : Int {
        delay(3000)
        val startDate = LocalDateTime.of(year, month, 1, 0, 0, 0)
        val endDate = LocalDateTime.of(year, month, month.length(false), 23, 59, 59)
        return expenseDao.speedQueryB(startDate, endDate, listOf("HUF", "EUR")).size
    }

}