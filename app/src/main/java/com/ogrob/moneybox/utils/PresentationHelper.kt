package com.ogrob.moneybox.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlin.math.ln
import kotlin.math.pow

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun withSuffix(value: Double): String {
    if (value < 1000) return String.format("%.2f", value)

    val exp = (ln(value) / ln(1000.0)).toInt()
    return String.format(
        "%.2f %c",
        value / 1000.0.pow(exp.toDouble()),
        "kMBTQ"[exp - 1]
    )
}

fun formatExpenseCounterText(expenseCount: Int) =
    if (expenseCount == 1)
        "$expenseCount Item"
    else
        "$expenseCount Items"