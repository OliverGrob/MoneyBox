package com.ogrob.moneybox.persistence.helper

import com.ogrob.moneybox.persistence.model.Category

class ExpenseFilteringQueryBuilder {

    private var filterCounter = 0

    private lateinit var selectedCategories: MutableList<Category>
    private lateinit var selectedCurrencies: MutableList<String>
    private lateinit var selectedExpenseAmountRange: Pair<Double, Double>


    fun selectedCategories(selectedCategories: MutableList<Category>): ExpenseFilteringQueryBuilder {
        this.selectedCategories = selectedCategories
        return this
    }

    fun selectedCurrencies(selectedCurrencies: MutableList<String>): ExpenseFilteringQueryBuilder {
        this.selectedCurrencies = selectedCurrencies
        return this
    }

    fun selectedExpenseAmountRange(selectedExpenseAmountRange: Pair<Double, Double>): ExpenseFilteringQueryBuilder {
        this.selectedExpenseAmountRange = selectedExpenseAmountRange
        return this
    }

    fun buildQuery(): String {
        val stringBuilder = StringBuilder("SELECT * FROM expense ")

        if (selectedCategories.isNotEmpty())
            stringBuilder.append(createSelectedCategoriesFilterQueryPart())

        if (selectedCurrencies.isNotEmpty())
            stringBuilder.append(createSelectedCurrenciesFilterQueryPart())

        if (selectedExpenseAmountRange.first != -1.0 && selectedExpenseAmountRange.first != -1.0)
            stringBuilder.append(createSelectedExpenseAmountRangeFilterQueryPart())

        return stringBuilder
            .append("ORDER BY expense.addition_date")
            .toString()
    }

    private fun createSelectedCategoriesFilterQueryPart(): String {
        val stringBuilder = StringBuilder("WHERE expense.category_id IN ")

        filterCounter++

        val categoriesStringWithParenthesis = selectedCategories
            .map(Category::id)
            .toString()
            .replace("[", "(")
            .replace("]", ")")

        return stringBuilder
            .append(categoriesStringWithParenthesis)
            .append(" ")
            .toString()
    }

    private fun createSelectedCurrenciesFilterQueryPart(): String {
        val stringBuilder = StringBuilder()

        if (isFirstFilterInQuery())
            stringBuilder.append("WHERE ")
        else
            stringBuilder.append("AND ")

        filterCounter++

        // Field name here
        stringBuilder.append("expense.currency_id IN ")

        val currenciesStringWithParenthesis = selectedCurrencies
//            .map(Currency::id)
            .toString()
            .replace("[", "(")
            .replace("]", ")")

        return stringBuilder
            .append(currenciesStringWithParenthesis)
            .append(" ")
            .toString()
    }

    private fun createSelectedExpenseAmountRangeFilterQueryPart(): String {
        val stringBuilder = StringBuilder()

        if (isFirstFilterInQuery())
            stringBuilder.append("WHERE ")
        else
            stringBuilder.append("AND ")

        filterCounter++

        return stringBuilder
            .append("expense.currency_id BETWEEN ${selectedExpenseAmountRange.first} AND ${selectedExpenseAmountRange.second} ")
            .toString()
    }

    private fun isFirstFilterInQuery(): Boolean {
        return filterCounter == 0
    }

}