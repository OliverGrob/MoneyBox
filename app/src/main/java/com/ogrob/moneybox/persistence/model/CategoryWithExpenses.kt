package com.ogrob.moneybox.persistence.model

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithExpenses(

    @Embedded
    val category: Category,

    @Relation(entity = Expense::class, parentColumn = "id", entityColumn = "category_id")
    val expenses: List<Expense>
)