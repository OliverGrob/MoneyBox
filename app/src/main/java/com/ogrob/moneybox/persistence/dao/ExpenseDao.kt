package com.ogrob.moneybox.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import java.time.LocalDateTime

@Dao
interface ExpenseDao : BaseDao<Expense> {

    /** delete this 2020.09.19 */
    @Transaction
    @Query("SELECT * FROM category")
    fun getAllCategoriesWithExpenses(): LiveData<List<CategoryWithExpenses>>










    @Query("SELECT * FROM expense WHERE expense.addition_date BETWEEN :startDate AND :endDate AND expense.amount BETWEEN 100 AND 5000 AND expense.currency IN (:currencies) ORDER BY expense.addition_date")
    suspend fun speedQueryB(startDate: LocalDateTime, endDate: LocalDateTime, currencies: List<String>) : List<Expense>

}