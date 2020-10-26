package com.ogrob.moneybox.ui.helper

import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense

class UpdatedFilterValueCalculator(
    unfilteredExpenses: List<CategoryWithExpenses>,
    private val selectedCategoryIds: List<Long>,
    private val selectedCurrencyIds: List<Long>,
    private val selectedExpenseAmountRange: Pair<Double, Double>
) {

    val updatedFilterValuesDTO: UpdatedFilterValuesDTO


    init {
        val allCategoryIdsFromFilteredExpenses = unfilteredExpenses
            .map { categoryWithExpenses -> categoryWithExpenses.category.id }

        val totalAndSelectedCategoryWithExpenseCount = Pair(
            unfilteredExpenses.count(),
            selectedCategoryIds
                .filter { selectedCategoryId -> allCategoryIdsFromFilteredExpenses.contains(selectedCategoryId) }
                .size
        )
        val filteredCategoryWithExpenseCount = unfilteredExpenses
            .asSequence()
            .map { categoryWithExpenses ->
                Pair(
                    categoryWithExpenses.category,
                    categoryWithExpenses.expenses
                        .filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }
                        .filter { expense -> isExpenseAmountInSelectedAmountRange(expense.amount) }
                        .size
                )
            }
            .toMap()


        val allCurrencyIdsFromFilteredExpenses = unfilteredExpenses
            .asSequence()
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
            .map { expense -> expense.currency.id }
            .distinct()

        val totalAndSelectedCurrencyWithExpenseCount = Pair(
            unfilteredExpenses
                .asSequence()
                .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                .groupBy(Expense::currency)
                .count(),
            selectedCurrencyIds
                .filter { selectedCurrencyId -> allCurrencyIdsFromFilteredExpenses.contains(selectedCurrencyId) }
                .size
        )
        val filteredCurrencyWithExpenseCount = unfilteredExpenses
            .asSequence()
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
            .groupBy(Expense::currency)
            .map { entry ->
                Pair(
                    entry.key,
                    entry.value
                        .filter { expense -> selectedCategoryIds.contains(expense.categoryId) }
                        .filter { expense -> isExpenseAmountInSelectedAmountRange(expense.amount) }
                        .size
                )
            }
            .toMap()

        updatedFilterValuesDTO = UpdatedFilterValuesDTO(
            filteredCategoryWithExpenseCount,
            filteredCurrencyWithExpenseCount,
            totalAndSelectedCategoryWithExpenseCount,
            totalAndSelectedCurrencyWithExpenseCount,
            selectedExpenseAmountRange.first,
            selectedExpenseAmountRange.second
        )
    }

    private fun isExpenseAmountInSelectedAmountRange(amount: Double) =
        selectedExpenseAmountRange.first <= amount && selectedExpenseAmountRange.second >= amount

}