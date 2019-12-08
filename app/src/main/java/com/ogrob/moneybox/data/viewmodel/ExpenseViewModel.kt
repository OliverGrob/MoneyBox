package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.ogrob.moneybox.data.repository.CategoryRepository
import com.ogrob.moneybox.data.repository.ExpenseRepository
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_NAME
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository =
        ExpenseRepository(application)
    private val categoryRepository: CategoryRepository =
        CategoryRepository(application)

    private val categoriesWithExpenses: LiveData<List<CategoryWithExpenses>> = this.expenseRepository.getCategoriesWithExpenses()
    private val categories: LiveData<List<Category>> = this.categoryRepository.getCategories()


    fun getAllCategoriesWithExpenses(): LiveData<List<CategoryWithExpenses>> = this.categoriesWithExpenses

    fun getAllCategories(): LiveData<List<Category>> = this.categories

    fun addNewExpense(expenseAmount: String, expenseDescription: String, expenseDate: String, categoryId: Long) {
        this.expenseRepository.addNewExpense(Expense(
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            categoryId))
    }

    fun addNewCategory(categoryName: String, categoryColor: Int) {
        this.categoryRepository.addNewCategory(Category(
            categoryName,
            categoryColor))
    }

    fun updateExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, categoryId: Long) {
        this.expenseRepository.updateExpense(Expense(
            expenseId,
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            categoryId))
    }

    fun updateCategory(categoryId: Long, categoryName: String, categoryColor: Int) {
        this.categoryRepository.updateCategory(Category(
            categoryId,
            categoryName,
            categoryColor))
    }

    fun deleteExpense(expense: Expense) {
        this.expenseRepository.deleteExpense(expense)
    }

    fun deleteCategory(category: Category) {
        this.categoryRepository.deleteCategory(category)
    }

    fun getAllExpensesDescription(categoriesWithExpenses: List<CategoryWithExpenses>): List<String> = categoriesWithExpenses
        .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
        .map(Expense::description)
        .distinct()

    fun getYearsWithExpense(): List<Int> {
        return this.categoriesWithExpenses.value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .map(Expense::additionDate)
            .map(LocalDateTime::getYear)
            .distinct()
            .sorted()
    }

    fun getMonthsInYearWithExpense(yearSelected: Int): List<Month> {
        return this.categoriesWithExpenses.value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .asSequence()
            .filter { expense -> expense.additionDate.year == yearSelected }
            .map(Expense::additionDate)
            .map(LocalDateTime::getMonth)
            .distinct()
            .sorted()
            .toList()
    }

    fun getExpensesForSelectedMonthInSelectedYear(yearSelected: Int, monthSelected: Month): List<Expense> {
        return this.categoriesWithExpenses.value!!
            .flatMap { categoryWithExpenses ->
                filterExpensesForSelectedMonthInSelectedYear(categoryWithExpenses.expenses, yearSelected, monthSelected) }
            .sortedBy(Expense::additionDate)
    }

    private fun filterExpensesForSelectedMonthInSelectedYear(expenses: List<Expense>, yearSelected: Int, monthSelected: Month): List<Expense> {
        return expenses
            .filter { expense -> expense.additionDate.year == yearSelected && expense.additionDate.month == monthSelected }
    }

    fun addOrEditExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, categoryId: Long) {
        if (expenseId == NEW_EXPENSE_PLACEHOLDER_ID)
            this.addNewExpense(
                expenseAmount,
                expenseDescription,
                expenseDate,
                categoryId)

        else
            this.updateExpense(
                expenseId,
                expenseAmount,
                expenseDescription,
                expenseDate,
                categoryId)
    }

    fun addOrEditCategory(categoryId: Long, categoryName: String, currentTextColor: Int) {
        if (categoryId == NEW_CATEGORY_PLACEHOLDER_ID)
            this.addNewCategory(
                categoryName,
                currentTextColor)

        else
            this.updateCategory(
                categoryId,
                categoryName,
                currentTextColor)
    }

    fun getTotalMoneySpentFormatted(expensesSelected: List<Expense>): String {
        val totalMoneySpentWithoutFormatting = expensesSelected
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }

        return this.formatMoneySpent(totalMoneySpentWithoutFormatting)
    }

    private fun formatMoneySpent(currentTotal: Double): String {
        return if (currentTotal == currentTotal.toInt().toDouble()) currentTotal.toInt().toString() else currentTotal.toString()
    }

    fun getExpenseByDescription(description: String): Expense {
        return this.getAllCategoriesWithExpenses().value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .single { expense -> expense.description == description }
    }

    fun deleteUnusedCategories(categoriesWithExpenses: List<CategoryWithExpenses>) {
        categoriesWithExpenses
            .filter { categoryWithExpenses -> categoryWithExpenses.expenses.isEmpty() }
            .map(CategoryWithExpenses::category)
            .forEach(this::deleteCategory)
    }

    fun retrievePreferenceFromSharedPreferences(context: Context, key: String): String {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(key, EMPTY_STRING)!!
    }

    fun updatePreferenceInSharedPreferences(context: Context, key: String, value: String) {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }

}
