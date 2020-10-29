package com.ogrob.moneybox.data.helper

data class FixedIntervalWithAverage(
    val fixedInterval: FixedInterval,
    val average: Double
)

enum class FixedInterval {
    YEAR,
    MONTH,
    DAY;

    override fun toString(): String =
        name
            .toLowerCase()
            .capitalize()
}