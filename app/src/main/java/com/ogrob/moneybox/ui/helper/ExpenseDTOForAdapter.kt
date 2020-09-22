package com.ogrob.moneybox.ui.helper

import com.ogrob.moneybox.persistence.model.Expense

data class ExpenseDTOForAdapter(val expense: Expense, val isHeaderExpense: Boolean = false)