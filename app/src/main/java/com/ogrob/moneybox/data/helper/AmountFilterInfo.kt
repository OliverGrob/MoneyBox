package com.ogrob.moneybox.data.helper

data class AmountFilterInfo(
    val amountMinValue: Double,
    val amountMaxValue: Double,
    var selectedAmountMinValue: Double,
    var selectedAmountMaxValue: Double,
    var createRangeBar: Boolean
)