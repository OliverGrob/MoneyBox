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
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_MAX_AMOUNT_PER_MONTH_DEFAULT_VALUE
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_MAX_AMOUNT_PER_MONTH_KEY

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val _categoriesWithExpenses: LiveData<List<CategoryWithExpenses>> = ExpenseRepository(application).getCategoriesWithExpenses()

    lateinit var categoriesWithExpenses: List<CategoryWithExpenses>
    lateinit var allCategories: List<Category>
    private lateinit var selectedCategories: MutableList<Category>


    fun setCategoriesWithExpensesAndSelectedCategories(categoriesWithExpenses: List<CategoryWithExpenses>) {
        this.categoriesWithExpenses = categoriesWithExpenses
        this.allCategories = categoriesWithExpenses
            .map(CategoryWithExpenses::category)
        this.selectedCategories = this.allCategories
            .toMutableList()
    }

    fun getAllCategoriesWithExpenses(): LiveData<List<CategoryWithExpenses>> = this._categoriesWithExpenses

    fun calculateTotalAverage(): Int {
        val allExpenses = categoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)

        return allExpenses
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }
            .div(allExpenses.size)
            .toInt()
    }

    fun sortExpensesByYearAndMonth(categoriesWithExpenses: List<CategoryWithExpenses>): List<SortedExpensesByYearAndMonth> {
        return categoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)
            .groupBy { expense -> expense.additionDate.year }
            .map { currentExpensesSortedByYear -> SortedExpensesByYearAndMonth(currentExpensesSortedByYear.key, currentExpensesSortedByYear.value) }
            .sortedBy(SortedExpensesByYearAndMonth::year)
    }

    fun getTotalMoneySpent(expensesSelected: List<Expense>): Double {
        return expensesSelected
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }
    }

    fun formatMoneySpent(currentTotal: Double): String {
        return if (currentTotal == currentTotal.toInt().toDouble()) currentTotal.toInt().toString() else currentTotal.toString()
    }

    fun getTextColorBasedOnSetMaxExpense(totalMoneySpentInMonth: Double): Int {
        val maxAmountPerMonth = _application.getSharedPreferences("moneybox_preferences", Context.MODE_PRIVATE)
            .getFloat(
                SHARED_PREFERENCES_MAX_AMOUNT_PER_MONTH_KEY,
                SHARED_PREFERENCES_MAX_AMOUNT_PER_MONTH_DEFAULT_VALUE)

        return if (maxAmountPerMonth < totalMoneySpentInMonth) Color.RED else Color.rgb(0, 160, 0)
    }

    fun getAllCategoryNames(): Array<String> {
        return allCategories
            .map(Category::name)
            .toTypedArray()
    }

    fun updateSelectedCategories(which: Int, checked: Boolean) {
        if (checked) {
            selectedCategories.add(allCategories[which])
        } else {
            selectedCategories.remove(allCategories[which])
        }
    }

    fun filterForSelectedCategories(): List<CategoryWithExpenses> {
        return categoriesWithExpenses
            .filter { categoryWithExpenses -> selectedCategories.contains(categoryWithExpenses.category) }
    }

    fun isCategoryChecked(index: Int): Boolean {
        return selectedCategories.contains(allCategories[index])
    }

}