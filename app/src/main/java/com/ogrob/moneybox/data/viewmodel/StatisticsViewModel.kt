package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.ogrob.moneybox.data.helper.SortedExpensesByYearAndMonth
import com.ogrob.moneybox.data.repository.ExpenseRepository
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_NAME
import java.time.Month
import kotlin.math.roundToInt

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository = ExpenseRepository(application)

    private val _application = application
    private val _categoriesWithExpenses: LiveData<List<CategoryWithExpenses>> =
        expenseRepository.getCategoriesWithExpenses()

    private lateinit var categoriesWithExpenses: List<CategoryWithExpenses>
    private lateinit var allCategories: List<Category>
    lateinit var selectedCategoriesWithExpenses: MutableList<CategoryWithExpenses>


    fun setCategoriesWithExpensesAndSelectedCategories(categoriesWithExpenses: List<CategoryWithExpenses>) {
        this.categoriesWithExpenses = categoriesWithExpenses
        this.allCategories = categoriesWithExpenses
            .map(CategoryWithExpenses::category)
        this.selectedCategoriesWithExpenses = this.categoriesWithExpenses
            .toMutableList()
    }

    fun getAllCategoriesWithExpenses(): LiveData<List<CategoryWithExpenses>> =
        this._categoriesWithExpenses

    fun getSelectedExpensesForStatistics(selectedYear: Int, selectedMonth: Month): List<Expense> = selectedCategoriesWithExpenses
        .flatMap(CategoryWithExpenses::expenses)
        .filter { expense -> expense.additionDate.year == selectedYear && expense.additionDate.month == selectedMonth }

    fun calculateTotalAverage(): Int {
        val allExpenses = selectedCategoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)

        return calculateTotalAmount(allExpenses)
            .div(selectedCategoriesWithExpenses.size)
            .toInt()
    }

    fun calculateTotalAmount(expenses: List<Expense>): Double = expenses
        .map(Expense::amount)
        .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }

    fun sortExpensesByYearAndMonth(categoriesWithExpenses: List<CategoryWithExpenses>): List<SortedExpensesByYearAndMonth> =
        categoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)
            .groupBy { expense -> expense.additionDate.year }
            .map { currentExpensesSortedByYear ->
                SortedExpensesByYearAndMonth(
                    currentExpensesSortedByYear.key,
                    currentExpensesSortedByYear.value
                )
            }
            .sortedBy(SortedExpensesByYearAndMonth::year)

    fun getTotalMoneySpent(expensesSelected: List<Expense>): Double = expensesSelected
        .map(Expense::amount)
        .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }

    fun formatMoneySpent(currentTotal: Double): String =
        if (currentTotal == currentTotal.toInt().toDouble()) currentTotal.toInt().toString() else currentTotal.toString()

    fun getTextColorBasedOnSetMaxExpense(totalMoneySpentInMonth: Double): Int {
        val maxAmountPerMonth =
            _application.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getFloat(
                    SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY,
                    SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE
                )

        return if (maxAmountPerMonth < totalMoneySpentInMonth) Color.RED else Color.rgb(0, 160, 0)
    }

    fun getAllCategoryNames(): Array<String> = allCategories
        .map { category -> "${category.name} (${categoriesWithExpenses
            .first { categoryWithExpenses -> categoryWithExpenses.category == category }
            .expenses
            .size})" }
        .toTypedArray()

    fun updateSelectedCategories(which: Int, checked: Boolean) {
        if (checked) {
            selectedCategoriesWithExpenses.add(categoriesWithExpenses[which])
        } else {
            selectedCategoriesWithExpenses.remove(categoriesWithExpenses[which])
        }
    }

    fun isCategoryChecked(index: Int): Boolean =
        selectedCategoriesWithExpenses.contains(categoriesWithExpenses[index])

    fun calculatePercentageOfTotalAmount(
        expense: Expense,
        totalAmountFromSelectedExpenses: Double
    ): Double = expense.amount
        .div(totalAmountFromSelectedExpenses)
        .times(10000.0)
        .roundToInt()
        .div(100.0)

}