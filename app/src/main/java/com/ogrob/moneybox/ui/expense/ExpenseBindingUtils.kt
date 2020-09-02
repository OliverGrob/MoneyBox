package com.ogrob.moneybox.ui.expense

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ogrob.moneybox.R
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
    val amountString = if (expenseAmount == expenseAmount.toInt().toDouble()) expenseAmount.toInt().toString() else expenseAmount.toString()
    text =  resources.getString(
        R.string.money_amount_with_currency,
        amountString,
        expense.currency.name
    )
}

@BindingAdapter("categoryDisplayName")
fun TextView.setCategoryDisplayText(category: Category) {
    text = if (category.id == NO_CATEGORY_ID) EMPTY_STRING else category.name
}
