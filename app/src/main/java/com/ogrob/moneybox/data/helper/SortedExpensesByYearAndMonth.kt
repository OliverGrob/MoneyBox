package com.ogrob.moneybox.data.helper

import com.ogrob.moneybox.persistence.model.Expense
import java.time.Month

data class SortedExpensesByYearAndMonth(val year: Int, private val expenses: List<Expense>) {

    val expensesSortedByMonth: Map<Month, List<Expense>>
    val totalMoneySpentInYear: Double


    init {
        expensesSortedByMonth = expenses
            .groupBy { expense -> expense.additionDate.month }

        totalMoneySpentInYear = expenses
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }
    }
}