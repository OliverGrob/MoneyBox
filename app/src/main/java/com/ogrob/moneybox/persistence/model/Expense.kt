package com.ogrob.moneybox.persistence.model

import androidx.room.*
import androidx.room.ForeignKey.SET_DEFAULT
import java.time.LocalDateTime

@Entity(
    tableName = "expense",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["category_id"],
        onDelete = SET_DEFAULT)],
    indices = [Index(value = ["category_id"])])
data class Expense(

    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "addition_date")
    val additionDate: LocalDateTime,

    @ColumnInfo(name = "currency")
    val currency: Currency,

    @ColumnInfo(name = "category_id", defaultValue = "1")
    val categoryId: Long = 1
) {

    @Ignore
    constructor(amount: Double, description: String, additionDate: LocalDateTime, currency: Currency, categoryId: Long):
            this(0, amount, description, additionDate, currency, categoryId)
}