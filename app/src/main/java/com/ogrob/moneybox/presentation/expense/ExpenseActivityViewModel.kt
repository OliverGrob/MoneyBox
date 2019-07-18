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
import java.util.stream.Collectors

class ExpenseActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository = ExpenseRepository(application)
    private val categoryRepository: CategoryRepository = CategoryRepository(application)

    private val expenses: LiveData<List<CategoryWithExpenses>> = this.expenseRepository.getExpenses()
    private val categories: LiveData<List<Category>> = this.categoryRepository.getCategories()


    fun getAllCategoryWithExpenses(): LiveData<List<CategoryWithExpenses>> = this.expenses

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

    fun getAllExpensesDescription(): List<String> = this.expenses.value!!
        .stream()
        .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.stream() }
        .map(Expense::description)
        .collect(Collectors.toList())

    fun getYearsWithExpense(): List<Int> {
        return this.expenses.value!!
            .stream()
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.stream() }
            .map(Expense::additionDate)
            .map(LocalDateTime::getYear)
            .distinct()
            .sorted()
            .collect(Collectors.toList())
    }

    fun getMonthsInYearWithExpense(yearSelected: Int): List<Month> {
        return this.expenses.value!!
            .stream()
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.stream() }
            .filter { expense -> expense.additionDate.year == yearSelected }
            .map(Expense::additionDate)
            .map(LocalDateTime::getMonth)
            .distinct()
            .sorted()
            .collect(Collectors.toList())
    }

    fun getExpensesForSelectedMonthInSelectedYear(yearSelected: Int, monthSelected: Month): List<CategoryWithExpenses> {
        return this.expenses.value!!
            .stream()
            .map { categoryWithExpenses -> CategoryWithExpenses(
                categoryWithExpenses.category,
                this.filterExpensesForSelectedMonthInSelectedYear(categoryWithExpenses.expenses, yearSelected, monthSelected)) }
            .collect(Collectors.toList())
    }

    private fun filterExpensesForSelectedMonthInSelectedYear(expenses: List<Expense>, yearSelected: Int, monthSelected: Month): List<Expense> {
        return expenses
            .stream()
            .filter { expense -> expense.additionDate.year == yearSelected && expense.additionDate.month == monthSelected }
//            .sorted { expense1, expense2 -> expense1.additionDate.compareTo(expense2.additionDate) }
            .collect(Collectors.toList())
    }

    fun getTotalMoneySpent(expensesSelected: List<CategoryWithExpenses>): Double {
        return expensesSelected
            .stream()
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.stream() }
            .map(Expense::amount)
            .reduce { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }
//            .map(Double::toInt)
            .orElse(0.0)
    }

}
