package com.ogrob.moneybox.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import com.ogrob.moneybox.persistence.model.Expense
import java.time.LocalDateTime

@Dao
interface ExpenseDao : BaseDao<Expense> {

    @Query("DELETE FROM expense WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)










    @Query("SELECT * FROM expense WHERE expense.addition_date BETWEEN :startDate AND :endDate AND expense.amount BETWEEN 100 AND 5000 AND expense.currency IN (:currencies) ORDER BY expense.addition_date")
    suspend fun speedQueryB(startDate: LocalDateTime, endDate: LocalDateTime, currencies: List<String>) : List<Expense>

}