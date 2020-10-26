package com.ogrob.moneybox.data.helper

import com.ogrob.moneybox.persistence.model.Currency

data class CurrencyFilterInfo(
    val selectedCurrencyIds: MutableSet<Long>,
    var currenciesWithExpenseCount: Map<Currency, Int>,
    var updateFilterOption: UpdateFilterOption
)