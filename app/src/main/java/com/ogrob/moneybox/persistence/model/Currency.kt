package com.ogrob.moneybox.persistence.model

enum class Currency(
    val id: Long,
    val niceName: String,
    val symbol: String,
    val fractionDigits: Int
) {
    AUD(1, "Australian dollar", "AU$", 2),
    BGN(2, "Bulgarian lev", "лв.", 2),
    BRL(3, "Brazilian real", "R$", 2),
    CAD(4, "Canadian dollar", "CA$", 2),
    CHF(5, "Swiss franc", "CHF", 2),
    CNY(6, "Renminbi", "元/¥", 2),
    CZK(7, "Czech koruna", "Kč", 2),
    DKK(8, "Danish krone", "kr.", 2),
    EUR(9, "Euro", "€", 2),
    GBP(10, "Pound", "£", 2),
    HKD(11, "Hong Kong dollar", "HK$", 2),
    HRK(12, "Croatian kuna", "kn", 2),
    HUF(13, "Hungarian forint", "Ft", 0),
    IDR(14, "Indonesian rupiah", "Rp", 0),
    ILS(15, "Israeli new shekel", "₪", 2),
    INR(16, "Indian rupee", "₹", 2),
    ISK(17, "Icelandic króna", "kr", 0),
    JPY(18, "Japanese yen", "¥", 0),
    KRW(19, "South Korean won", "₩", 0),
    MXN(20, "Mexican peso", "Mex$", 2),
    MYR(21, "Malaysian ringgit", "RM", 2),
    NOK(22, "Norwegian krone", "kr", 2),
    NZD(23, "New Zealand dollar", "NZ$", 2),
    PHP(24, "Philippine peso", "₱", 2),
    PLN(25, "Polish złoty", "zł", 2),
    RON(26, "Romanian leu", "L", 2),
    RUB(27, "Russian ruble", "₽", 2),
    SEK(28, "Swedish krona", "kr", 2),
    SGD(29, "Singapore dollar", "S$", 2),
    THB(30, "Thai baht", "฿", 2),
    TRY(31, "Turkish lira", "₺", 2),
    USD(32, "United States dollar", "$", 2),
    ZAR(33, "South African rand", "R", 2)
}