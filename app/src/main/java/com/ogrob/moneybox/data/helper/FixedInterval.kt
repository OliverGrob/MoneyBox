package com.ogrob.moneybox.data.helper

enum class FixedInterval {
    YEAR, MONTH, DAY;

    override fun toString(): String =
        name
            .toLowerCase()
            .capitalize()
}