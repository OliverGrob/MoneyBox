package com.ogrob.moneybox.persistence

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ogrob.moneybox.persistence.converters.BigDecimalConverter
import com.ogrob.moneybox.persistence.converters.CurrencyConverter
import com.ogrob.moneybox.persistence.converters.LocalDateConverter
import com.ogrob.moneybox.persistence.converters.LocalDateTimeConverter
import com.ogrob.moneybox.persistence.dao.CategoryDao
import com.ogrob.moneybox.persistence.dao.ExpenseDao
import com.ogrob.moneybox.persistence.dao.HistoricalExchangeRateDao
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.Currency
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.persistence.model.HistoricalExchangeRate
import com.ogrob.moneybox.utils.NO_CATEGORY_ID
import com.ogrob.moneybox.utils.NO_CATEGORY_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream

@Database(entities = [Expense::class, Category::class, HistoricalExchangeRate::class], version = 1)
@TypeConverters(LocalDateTimeConverter::class, LocalDateConverter::class, BigDecimalConverter::class, CurrencyConverter::class)
abstract class ExpenseRoomDatabase: RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun historicalExchangeRateDao(): HistoricalExchangeRateDao


    companion object {
        @Volatile
        private var INSTANCE: ExpenseRoomDatabase? = null

        fun getDatabase(
            context: Context,
            coroutineScope: CoroutineScope
        ) : ExpenseRoomDatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                        .databaseBuilder(
                            context.applicationContext,
                            ExpenseRoomDatabase::class.java,
                            "moneyBoxDB"
                        )
//                        .addCallback(ExpenseDatabaseTestCallback(coroutineScope))
                        .addCallback(ExpenseDatabaseCallback(coroutineScope))
                        .build()
                }
                return INSTANCE as ExpenseRoomDatabase
            }
        }

        fun destroyInstance() {
            if (Objects.nonNull(INSTANCE) && INSTANCE!!.isOpen)
                INSTANCE!!.close()

            INSTANCE = null
        }
    }


    private class ExpenseDatabaseCallback(
        private val coroutineScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        private val TAG = "ExpenseDatabaseCallback"


        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                coroutineScope.launch{
                    val categoryDao = database.categoryDao()
                    prePopulateDatabase(categoryDao)
                }
            }
        }

        private suspend fun prePopulateDatabase(categoryDao: CategoryDao) {
            Log.i(TAG, "Prepopulate start")

            Log.i(TAG, "Creating default category")
            val defaultCategory = Category(NO_CATEGORY_ID, NO_CATEGORY_NAME, Color.GREEN)
            Log.i(TAG, "Default category created")

            Log.i(TAG, "Inserting default category")
            categoryDao.insert(defaultCategory)
            Log.i(TAG, "Default category inserted")

            Log.i(TAG, "Prepopulate end")
        }

    }


    private class ExpenseDatabaseTestCallback(
        private val coroutineScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        private val TAG = "ExpenseDatabaseTestCallback"


        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                coroutineScope.launch{
                    val expenseDao = database.expenseDao()
                    val categoryDao = database.categoryDao()
                    val historicalExchangeRateDao = database.historicalExchangeRateDao()
                    prePopulateDatabase(expenseDao, categoryDao, historicalExchangeRateDao)
                }
            }
        }

        private suspend fun prePopulateDatabase(
            expenseDao: ExpenseDao,
            categoryDao: CategoryDao,
            historicalExchangeRateDao: HistoricalExchangeRateDao
        ) {
            Log.i(TAG, "Prepopulate start")

            Log.i(TAG, "Creating categories")
            val categories = arrayOf(
                Category(NO_CATEGORY_ID, NO_CATEGORY_NAME, Color.GREEN),
                Category(2, "Food", Color.RED),
                Category(3, "Clothing", Color.BLUE),
                Category(4, "Hobby", Color.rgb(200, 20, 40)),
                Category(5, "Games", Color.WHITE),
                Category(6, "Shoes", Color.LTGRAY),
                Category(7, "Electronics", Color.DKGRAY),
                Category(8, "Party", Color.MAGENTA),
                Category(9, "Food Supplements", Color.GRAY),
                Category(10, "Gym", Color.CYAN),
                Category(11, "Meats", Color.YELLOW)
            )
            Log.i(TAG, "Categories created")
            Log.i(TAG, "Inserting categories")
            categoryDao.insertAll(*categories)
            Log.i(TAG, "Categories inserted")


            Log.i(TAG, "Creating 5k expenses")
            val sameExpenses: Array<Expense> = IntStream
                .range(1, 5000)
                .mapToObj { Expense(2300.0, "belozzo", LocalDateTime.of(2019, 10, 2, 1, 1), Currency.HUF, 2) }
                .collect(Collectors.toList())
                .toTypedArray()
            Log.i(TAG, "5k expenses created")
            Log.i(TAG, "Insert 5k expenses")
            expenseDao.insertAll(*sameExpenses)
            Log.i(TAG, "5k expenses inserted")


            Log.i(TAG, "Creating other expenses")
            val randomExpenses = arrayOf(
                Expense(2300.0, "belozzo", LocalDateTime.of(2019, 10, 2, 1, 1), Currency.HUF, 2),
                Expense(1650.0, "games", LocalDateTime.of(2019, 10, 2, 1, 1), Currency.EUR, 5),
                Expense(1800.0, "spar gyümi", LocalDateTime.of(2019, 10, 2, 1, 1), Currency.HUF, 2),
                Expense(3000.0, "CC ivás", LocalDateTime.of(2019, 10, 3, 1, 1), Currency.HUF, 1),
                Expense(3600.0, "mogyesz tala", LocalDateTime.of(2019, 10, 5, 1, 1), Currency.HUF, 1),
                Expense(3340.0, "telefonszámla", LocalDateTime.of(2019, 10, 6, 1, 1), Currency.HUF, 4),
                Expense(2000.0, "belozzo", LocalDateTime.of(2019, 10, 8, 1, 1), Currency.HUF, 1),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 11, 9, 1, 1), Currency.HUF, 4),
                Expense(1500.0, "spar fagyi", LocalDateTime.of(2019, 11, 11, 1, 1), Currency.HUF, 7),
                Expense(3800.0, "steam: anno 1404", LocalDateTime.of(2019, 11, 13, 1, 1), Currency.HUF, 8),
                Expense(2000.0, "steam: anno 2070", LocalDateTime.of(2019, 11, 13, 1, 1), Currency.HUF, 8),
                Expense(4000.0, "steam: anno 2205", LocalDateTime.of(2019, 12, 13, 1, 1), Currency.HUF, 1),
                Expense(2000.0, "kfc", LocalDateTime.of(2019, 10, 15, 1, 1), Currency.HUF, 2),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(13500.0, "kondi bérlet", LocalDateTime.of(2018, 10, 16, 1, 1), Currency.HUF, 10),
                Expense(2400.0, "spar: rágó + gyümi", LocalDateTime.of(2018, 10, 17, 1, 1), Currency.HUF, 11),
                Expense(1000.0, "spar", LocalDateTime.of(2018, 10, 18, 1, 1), Currency.HUF, 4),
                Expense(1650.0, "balozzo", LocalDateTime.of(2018, 10, 20, 1, 1), Currency.HUF, 6),
                Expense(3300.0, "media markt - pendrive", LocalDateTime.of(2019, 10, 20, 1, 1), Currency.HUF, 7),
                Expense(660.0, "meki", LocalDateTime.of(2019, 10, 21, 1, 1), Currency.HUF, 1),
                Expense(4000.0, "wow sub", LocalDateTime.of(2019, 10, 21, 1, 1), Currency.HUF, 2),
                Expense(4000.0, "wow sub", LocalDateTime.of(2019, 10, 21, 1, 1), Currency.HUF, 2),
                Expense(4000.0, "wow sub", LocalDateTime.of(2019, 10, 21, 1, 1), Currency.HUF, 2),
                Expense(4000.0, "wow sub", LocalDateTime.of(2019, 10, 21, 1, 1), Currency.HUF, 2),
                Expense(1500.0, "buli", LocalDateTime.of(2019, 1, 24, 1, 1), Currency.HUF, 3),
                Expense(1500.0, "buli", LocalDateTime.of(2019, 1, 24, 1, 1), Currency.HUF, 3),
                Expense(1500.0, "buli", LocalDateTime.of(2019, 1, 24, 1, 1), Currency.HUF, 3),
                Expense(1500.0, "buli", LocalDateTime.of(2019, 1, 24, 1, 1), Currency.HUF, 3),
                Expense(1500.0, "buli", LocalDateTime.of(2019, 1, 24, 1, 1), Currency.HUF, 3),
                Expense(1500.0, "buli", LocalDateTime.of(2019, 1, 24, 1, 1), Currency.HUF, 3),
                Expense(1500.0, "buli", LocalDateTime.of(2019, 1, 24, 1, 1), Currency.HUF, 3),
                Expense(1500.0, "buli", LocalDateTime.of(2019, 1, 24, 1, 1), Currency.HUF, 3),
                Expense(850.0, "gyros", LocalDateTime.of(2019, 4, 25, 1, 1), Currency.HUF, 6),
                Expense(850.0, "gyros", LocalDateTime.of(2019, 4, 25, 1, 1), Currency.HUF, 6),
                Expense(850.0, "gyros", LocalDateTime.of(2019, 4, 25, 1, 1), Currency.HUF, 6),
                Expense(850.0, "gyros", LocalDateTime.of(2019, 4, 25, 1, 1), Currency.HUF, 6),
                Expense(850.0, "gyros", LocalDateTime.of(2019, 4, 25, 1, 1), Currency.HUF, 6),
                Expense(850.0, "gyros", LocalDateTime.of(2019, 4, 25, 1, 1), Currency.HUF, 6),
                Expense(850.0, "gyros", LocalDateTime.of(2019, 4, 25, 1, 1), Currency.HUF, 6),
                Expense(850.0, "gyros", LocalDateTime.of(2019, 4, 25, 1, 1), Currency.HUF, 6),
                Expense(1800.0, "kfc", LocalDateTime.of(2019, 7, 26, 1, 1), Currency.HUF, 2),
                Expense(12000.0, "berlin", LocalDateTime.of(2019, 8, 26, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 3, 28, 1, 1), Currency.HUF, 4),
                Expense(1240.0, "CC ivás", LocalDateTime.of(2018, 1, 30, 1, 1), Currency.HUF, 1),
                Expense(1240.0, "CC ivás", LocalDateTime.of(2018, 1, 30, 1, 1), Currency.HUF, 1),
                Expense(1240.0, "CC ivás", LocalDateTime.of(2018, 1, 30, 1, 1), Currency.HUF, 1),
                Expense(1240.0, "CC ivás", LocalDateTime.of(2018, 1, 30, 1, 1), Currency.HUF, 1),
                Expense(1240.0, "CC ivás", LocalDateTime.of(2018, 1, 30, 1, 1), Currency.HUF, 1),
                Expense(60000.0, "zara: öltöny + cipő", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 2),
                Expense(60000.0, "zara: öltöny + cipő", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 2),
                Expense(60000.0, "zara: öltöny + cipő", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 2),
                Expense(60000.0, "zara: öltöny + cipő", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 2),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(5000.0, "sticza szülinap", LocalDateTime.of(2018, 1, 31, 1, 1), Currency.HUF, 1),

                Expense(5000.0, "currency test huf", LocalDateTime.of(2020, 1, 31, 1, 1), Currency.HUF, 1),
                Expense(35.0, "currency test hrk", LocalDateTime.of(2020, 1, 31, 1, 1), Currency.HRK, 1),
                Expense(700.0, "currency test eur", LocalDateTime.of(2020, 1, 31, 1, 1), Currency.EUR, 1)
            )
            Log.i(TAG, "Other expenses created")
            Log.i(TAG, "Inserting other expenses")
            expenseDao.insertAll(*randomExpenses)
            Log.i(TAG, "Other expenses inserted")


//            Log.i(TAG, "Creating 4k historical exchange rates")
//            val sameHistoricalExchangeRates: Array<HistoricalExchangeRate> = IntStream
//                .range(1, 4000)
//                .mapToObj { HistoricalExchangeRate(
//                    LocalDate.of(2020, 6, 7),
//                    "10.0",
//                    "11.0",
//                    "12.0",
//                    "13.0",
//                    "14.0",
//                    "15.0",
//                    "16.0",
//                    "17.0",
//                    "18.0",
//                    "19.0",
//                    "20.0",
//                    "21.0",
//                    "22.0",
//                    "23.0",
//                    "24.0",
//                    "25.0",
//                    "26.0",
//                    "27.0",
//                    "28.0",
//                    "29.0",
//                    "30.0",
//                    "31.0",
//                    "32.0",
//                    "33.0",
//                    "34.0",
//                    "35.0",
//                    "36.0",
//                    "37.0",
//                    "38.0",
//                    "39.0",
//                    "40.0",
//                    "41.0") }
//                .collect(Collectors.toList())
//                .toTypedArray()
//            Log.i(TAG, "4k historical exchange rates created")
//            Log.i(TAG, "Insert 4k historical exchange rates")
//            historicalExchangeRateDao.insertAll(*sameHistoricalExchangeRates)
//            Log.i(TAG, "4k historical exchange rates inserted")

            Log.i(TAG, "Prepopulate end")
        }

    }

}