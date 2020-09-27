package com.ogrob.moneybox.ui.helper

import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense

class UpdatedFilterValueCalculator(
    unfilteredExpenses: List<CategoryWithExpenses>,
    private val selectedCategoryIds: List<Long>,
    private val selectedCurrencyIds: List<Long>
) {

    val updatedFilterValuesDTO: UpdatedFilterValuesDTO


    init {
        val totalAndSelectedCategoryWithExpenseCount = Pair(unfilteredExpenses.count(), selectedCategoryIds.size)
        val filteredCategoryWithExpenseCount = unfilteredExpenses
            .asSequence()
            .map { categoryWithExpenses -> Pair(categoryWithExpenses.category, categoryWithExpenses.expenses.filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }.size) }
            .toMap()

        val totalAndSelectedCurrencyWithExpenseCount = Pair(
            unfilteredExpenses
                .asSequence()
                .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                .groupBy(Expense::currency)
                .count(),
            selectedCurrencyIds.size
        )
        val filteredCurrencyWithExpenseCount = unfilteredExpenses
            .asSequence()
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
            .groupBy(Expense::currency)
            .map { entry -> Pair(entry.key, entry.value.filter { expense -> selectedCategoryIds.contains(expense.categoryId) }.size) }
            .toMap()

        updatedFilterValuesDTO = UpdatedFilterValuesDTO(
            filteredCategoryWithExpenseCount,
            filteredCurrencyWithExpenseCount,
            totalAndSelectedCategoryWithExpenseCount,
            totalAndSelectedCurrencyWithExpenseCount
        )
    }

}