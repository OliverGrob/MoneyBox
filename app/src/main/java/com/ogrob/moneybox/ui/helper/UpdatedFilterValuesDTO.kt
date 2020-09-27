package com.ogrob.moneybox.ui.helper

import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.Currency

data class UpdatedFilterValuesDTO(
    val filteredCategoryWithExpenseCount: Map<Category, Int>,
    val filteredCurrencyWithExpenseCount: Map<Currency, Int>,
    val totalAndSelectedCategoryWithExpenseCount: Pair<Int, Int>,
    val totalAndSelectedCurrencyWithExpenseCount: Pair<Int, Int>
)