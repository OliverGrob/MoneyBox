package com.ogrob.moneybox.presentation.expense

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.NO_CATEGORY_ID

@BindingAdapter("additionDateString")
fun TextView.setAdditionDateString(expense: Expense) {
    text = expense.additionDate.dayOfMonth.toString()
}

@BindingAdapter("expenseAmountFormatted")
fun TextView.setExpenseAmountFormatted(expense: Expense) {
    val expenseAmount = expense.amount
    text = if (expenseAmount == expenseAmount.toInt().toDouble()) expenseAmount.toInt().toString() else expenseAmount.toString()
}

@BindingAdapter("categoryDisplayName")
fun TextView.setCategoryDisplayText(category: Category) {
    text = if (category.id == NO_CATEGORY_ID) EMPTY_STRING else category.name
}
