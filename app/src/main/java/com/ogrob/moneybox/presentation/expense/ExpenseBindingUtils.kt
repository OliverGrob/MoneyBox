package com.ogrob.moneybox.presentation.expense

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ogrob.moneybox.persistence.model.Expense

@BindingAdapter("additionDateString")
fun TextView.setAdditionDateString(expense: Expense) {
    text = expense.additionDate.dayOfMonth.toString()
}

@BindingAdapter("expenseAmountFormatted")
fun TextView.setExpenseAmountFormatted(expense: Expense) {
    val expenseAmount = expense.amount
    text = if (expenseAmount == expenseAmount.toInt().toDouble()) expenseAmount.toInt().toString() else expenseAmount.toString()
}
