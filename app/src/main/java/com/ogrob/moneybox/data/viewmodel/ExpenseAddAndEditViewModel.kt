package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ogrob.moneybox.data.repository.CategoryRepository
import com.ogrob.moneybox.data.repository.ExpenseRepository
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Currency
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ExpenseAddAndEditViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository =
        ExpenseRepository(application, viewModelScope)
    private val categoryRepository: CategoryRepository =
        CategoryRepository(application, viewModelScope)

    private val _allCategories: MutableLiveData<List<Category>> = MutableLiveData()
    val allCategories: LiveData<List<Category>> = _allCategories

    private val categoriesWithExpenses: LiveData<List<CategoryWithExpenses>> = expenseRepository.getAllCategoriesWithExpenses()

    private var categoryDropdownIsOpen = false


    fun getAllCategoriesWithExpenses_OLD(): LiveData<List<CategoryWithExpenses>> = this.categoriesWithExpenses

    fun getAllExpensesDescription(categoriesWithExpenses: List<CategoryWithExpenses>): List<String> = categoriesWithExpenses
        .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
        .map(Expense::description)
        .distinct()

    fun getAllCategories() {
        viewModelScope.launch {
            _allCategories.value = categoryRepository.getAllCategories()
        }
    }

    fun getExpenseByDescription(description: String): Expense {
        return this.getAllCategoriesWithExpenses_OLD().value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .distinctBy { expense -> expense.description }
            .single { expense -> expense.description == description }
    }

    fun isCategoryDropdownOpen(): Boolean = categoryDropdownIsOpen

    fun openCategoryDropdown() {
        categoryDropdownIsOpen = true
    }

    fun closeCategoryDropdown() {
        categoryDropdownIsOpen = false
    }

    fun addOrEditExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, currency: String, categoryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (expenseId == NEW_EXPENSE_PLACEHOLDER_ID)
                addNewExpense(
                    expenseAmount,
                    expenseDescription,
                    expenseDate,
                    currency,
                    categoryId
                )
            else
                updateExpense(
                    expenseId,
                    expenseAmount,
                    expenseDescription,
                    expenseDate,
                    currency,
                    categoryId
                )
        }
    }

    suspend fun addNewExpense(expenseAmount: String, expenseDescription: String, expenseDate: String, currency: String, categoryId: Long) {
        this.expenseRepository.addNewExpense(Expense(
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            Currency.valueOf(currency),
            categoryId))
    }

    suspend fun updateExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, currency: String, categoryId: Long) {
        this.expenseRepository.updateExpense(Expense(
            expenseId,
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            Currency.valueOf(currency),
            categoryId))
    }

    suspend fun deleteExpense(expense: Expense) {
        this.expenseRepository.deleteExpense(expense)
    }

}