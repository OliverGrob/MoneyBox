package com.ogrob.moneybox.persistence.converters

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverter {

    @TypeConverter
    fun timestampToDate(timestamp: Long?): LocalDate? {
        return timestamp?.let { LocalDate.ofEpochDay(timestamp) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.let { date.toEpochDay() }
    }

}