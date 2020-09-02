package com.ogrob.moneybox.persistence.converters

import androidx.room.TypeConverter
import com.ogrob.moneybox.persistence.model.Currency

class CurrencyConverter {

    @TypeConverter
    fun stringToCurrency(string: String?): Currency? {
        return string?.let { Currency.valueOf(string) }
    }

    @TypeConverter
    fun currencyToString(currency: Currency?): String? {
        return currency?.let { currency.name }
    }

}