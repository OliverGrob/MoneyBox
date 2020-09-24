package com.ogrob.moneybox.ui.helper

import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Currency
import com.ogrob.moneybox.persistence.model.Expense

class UpdatedFilterValueCalculator(
    private val unfilteredExpenses: List<CategoryWithExpenses>,
    private val selectedCategoryIds: List<Long>,
    private val selectedCurrencyIds: List<Long>
) {

    val totalCategoryWithExpenseCount: Map<Category, Int>
    val filteredCategoryWithExpenseCount: Map<Category, Int>

    val totalCurrencyWithExpenseCount: Map<Currency, Int>
    val filteredCurrencyWithExpenseCount: Map<Currency, Int>


    init {
        totalCategoryWithExpenseCount = unfilteredExpenses
            .map { categoryWithExpenses -> Pair(categoryWithExpenses.category, categoryWithExpenses.expenses.size) }
            .toMap()
        filteredCategoryWithExpenseCount = unfilteredExpenses
            .map { categoryWithExpenses -> Pair(categoryWithExpenses.category, categoryWithExpenses.expenses.filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }.size) }
            .toMap()

        totalCurrencyWithExpenseCount = unfilteredExpenses
            .flatMap(CategoryWithExpenses::expenses)
            .groupBy(Expense::currency)
            .map { entry -> Pair(entry.key, entry.value.size) }
            .toMap()
        filteredCurrencyWithExpenseCount = unfilteredExpenses
            .flatMap(CategoryWithExpenses::expenses)
            .groupBy(Expense::currency)
            .map { entry -> Pair(entry.key, entry.value.filter { expense -> selectedCategoryIds.contains(expense.categoryId) }.size) }
            .toMap()
    }

}