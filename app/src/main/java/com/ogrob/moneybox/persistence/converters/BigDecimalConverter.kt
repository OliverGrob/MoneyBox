package com.ogrob.moneybox.persistence.converters

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalConverter {

    @TypeConverter
    fun stringToBigDecimal(string: String?): BigDecimal? {
        return string?.let { BigDecimal(string) }
    }

    @TypeConverter
    fun bigDecimalToString(bigDecimal: BigDecimal?): String? {
        return bigDecimal?.let { bigDecimal.toString() }
    }

}