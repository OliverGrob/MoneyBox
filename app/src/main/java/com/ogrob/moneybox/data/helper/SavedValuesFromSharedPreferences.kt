package com.ogrob.moneybox.data.helper

data class SavedValuesFromSharedPreferences(
    val selectedCategoryIds: Set<String>,
    val selectedAmountRange: Set<String>,
    val selectedCurrencyIds: Set<String>,
    val selectedFixedInterval: String
)