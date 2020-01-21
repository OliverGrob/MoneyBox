package com.ogrob.moneybox.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense

@Dao
interface ExpenseDao : BaseDao<Expense> {

    @Transaction
    @Query("SELECT * FROM category")
    fun getAllCategoriesWithExpenses(): LiveData<List<CategoryWithExpenses>>

}