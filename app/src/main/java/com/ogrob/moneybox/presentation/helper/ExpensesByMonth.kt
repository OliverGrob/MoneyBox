package com.ogrob.moneybox.presentation.helper

import com.ogrob.moneybox.persistence.model.Expense
import java.time.Month

data class ExpensesByMonth(val year: Int, val month: Month, val expenses: List<Expense>) {

    val totalMoneySpentInMonth: Double


    init {
        totalMoneySpentInMonth = expenses
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }
    }

}