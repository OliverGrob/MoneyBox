package com.ogrob.moneybox.presentation.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.ogrob.moneybox.data.CategoryRepository
import com.ogrob.moneybox.data.ExpenseRepository
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter

class ExpenseActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository = ExpenseRepository(application)
    private val categoryRepository: CategoryRepository = CategoryRepository(application)

    private val categoriesWithExpenses: LiveData<List<CategoryWithExpenses>> = this.expenseRepository.getExpenses()
    private val categories: LiveData<List<Category>> = this.categoryRepository.getCategories()


    fun getAllCategoryWithExpenses(): LiveData<List<CategoryWithExpenses>> = this.categoriesWithExpenses

    fun getAllCategories(): LiveData<List<Category>> = this.categories

    fun addNewExpense(expenseValue: String, expenseDescription: String, expenseDate: String, categoryId: Int) {
        this.expenseRepository.addNewExpense(Expense(
            expenseValue.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            categoryId))
    }

    fun addNewCategory(categoryName: String) {
        this.categoryRepository.addNewCategory(Category(categoryName))
    }

    fun updateExpense(expenseId: Int, expenseValue: String, expenseDescription: String, expenseDate: String, categoryId: Int) {
        this.expenseRepository.updateExpense(Expense(
            expenseId,
            expenseValue.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            categoryId))
    }

    fun updateCategory(categoryId: Int, categoryName: String) {
        this.categoryRepository.updateCategory(Category(
            categoryId,
            categoryName))
    }

    fun deleteExpense(expense: Expense) {
        this.expenseRepository.deleteExpense(expense)
    }

    fun deleteCategory(category: Category) {
        this.categoryRepository.deleteCategory(category)
    }

    fun getAllExpensesDescription(): List<String> = this.categoriesWithExpenses.value!!
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

    fun getExpensesForSelectedMonthInSelectedYear(yearSelected: Int, monthSelected: Month): List<CategoryWithExpenses> {
        return this.categoriesWithExpenses.value!!
            .map { categoryWithExpenses -> CategoryWithExpenses(
                categoryWithExpenses.category,
                this.filterExpensesForSelectedMonthInSelectedYear(categoryWithExpenses.expenses, yearSelected, monthSelected)) }
    }

    private fun filterExpensesForSelectedMonthInSelectedYear(expenses: List<Expense>, yearSelected: Int, monthSelected: Month): List<Expense> {
        return expenses
            .filter { expense -> expense.additionDate.year == yearSelected && expense.additionDate.month == monthSelected }
    }

    fun getTotalMoneySpentFormatted(expensesSelected: List<CategoryWithExpenses>): String {
        val totalMoneySpentWithoutFormatting = expensesSelected
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }

        return this.formatMoneySpent(totalMoneySpentWithoutFormatting)
    }

    fun formatMoneySpent(currentTotal: Double): String {
        return if (currentTotal == currentTotal.toInt().toDouble()) currentTotal.toInt().toString() else currentTotal.toString()
    }

    fun getExpenseByDescription(description: String): Expense {
        return this.getAllCategoryWithExpenses().value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .single { expense -> expense.description == description }
    }

    fun deleteUnusedCategories(categoriesWithExpenses: List<CategoryWithExpenses>) {
        categoriesWithExpenses
            .filter { categoryWithExpenses -> categoryWithExpenses.expenses.isEmpty() }
            .map(CategoryWithExpenses::category)
            .forEach(this::deleteCategory)
    }

}
