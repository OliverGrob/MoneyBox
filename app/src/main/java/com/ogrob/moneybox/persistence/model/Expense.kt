package com.ogrob.moneybox.persistence.model

import androidx.room.*
import java.time.LocalDateTime

@Entity(
    tableName = "expense",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["category_id"])],
    indices = [Index(value = ["category_id"])])
class Expense(

    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "addition_date")
    val additionDate: LocalDateTime,

    @ColumnInfo(name = "category_id")
    val categoryId: Int
) {

    @Ignore
    constructor(amount: Double, description: String, additionDate: LocalDateTime, categoryId: Int):
            this(0, amount, description, additionDate, categoryId)
}