package com.ogrob.moneybox.persistence.converters

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeConverter {

    @TypeConverter
    fun timestampToDate(timestamp: Long?): LocalDateTime? {
        return timestamp?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }

}