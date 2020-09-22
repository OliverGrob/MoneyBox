package com.ogrob.moneybox.ui.helper

import com.ogrob.moneybox.persistence.model.Expense

data class ExpensesByYear(val year: Int, val expenses: List<Expense>) {

    val expensesByMonth: List<ExpensesByMonth>
    val totalMoneySpentInYear: Double


    init {
        expensesByMonth = expenses
            .groupBy { expense -> expense.additionDate.month }
            .map { entry -> ExpensesByMonth(year, entry.key, entry.value) }

        totalMoneySpentInYear = expenses
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }
    }
}