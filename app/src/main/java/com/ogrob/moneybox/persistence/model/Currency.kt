package com.ogrob.moneybox.persistence.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "currency")
data class Currency(

    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "iso_code")
    val isoCode: String,

    @ColumnInfo(name = "symbol")
    val symbol: String,

    @ColumnInfo(name = "fraction_digits")
    val fractionDigits: Int
) {

    @Ignore
    constructor(name: String, isoCode: String, symbol: String, fractionDigits: Int):
            this(0, name, isoCode, symbol, fractionDigits)
}