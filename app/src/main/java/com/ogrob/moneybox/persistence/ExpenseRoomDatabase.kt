package com.ogrob.moneybox.persistence

import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ogrob.moneybox.persistence.converters.LocalDateTimeConverter
import com.ogrob.moneybox.persistence.dao.CategoryDao
import com.ogrob.moneybox.persistence.dao.ExpenseDao
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.NO_CATEGORY_ID
import com.ogrob.moneybox.utils.NO_CATEGORY_NAME
import java.time.LocalDateTime
import java.util.*


@Database(entities = [Expense::class, Category::class], version = 1)
@TypeConverters(LocalDateTimeConverter::class)
abstract class ExpenseRoomDatabase: RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao


    companion object {
        @Volatile
        private var INSTANCE: ExpenseRoomDatabase? = null

        fun getInstance(context: Context): ExpenseRoomDatabase? {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                        .databaseBuilder(
                            context.applicationContext,
                            ExpenseRoomDatabase::class.java,
                            "moneyBoxDB"
                        )
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                PopulateDbAsync(INSTANCE!!).execute()
                            }
                        })
                        .build()
                }
                return INSTANCE
            }
        }

        fun destroyInstance() {
            if (Objects.nonNull(INSTANCE) && INSTANCE!!.isOpen)
                INSTANCE!!.close()

            INSTANCE = null
        }
    }


    private class PopulateDbAsync internal constructor(db: ExpenseRoomDatabase) : AsyncTask<Void, Void, Void>() {

        private val expenseDao: ExpenseDao = db.expenseDao()
        private val categoryDao: CategoryDao = db.categoryDao()


        override fun doInBackground(vararg params: Void): Void? {
            categoryDao.insert(Category(NO_CATEGORY_ID, NO_CATEGORY_NAME, Color.GREEN))
            categoryDao.insert(Category(2, "Food", Color.RED))
            categoryDao.insert(Category(3, "Clothing", Color.BLUE))
            categoryDao.insert(Category(4, "Hobby", Color.rgb(200, 20, 40)))
            categoryDao.insert(Category(5, "Games", Color.WHITE))
            categoryDao.insert(Category(6, "Shoes", Color.LTGRAY))
            categoryDao.insert(Category(7, "Electronics", Color.DKGRAY))
            categoryDao.insert(Category(8, "Jewelry", Color.MAGENTA))
            categoryDao.insert(Category(9, "Food Supplements", Color.GRAY))
            categoryDao.insert(Category(10, "Sweats", Color.CYAN))
            categoryDao.insert(Category(11, "Meats", Color.YELLOW))
            expenseDao.insert(Expense(2300.0, "belozzo", LocalDateTime.of(2019, 10, 2, 1, 1), 2))
            expenseDao.insert(Expense(1800.0, "spar gyümi", LocalDateTime.of(2019, 10, 2, 1, 1), 2))
            expenseDao.insert(Expense(3000.0, "CC ivás", LocalDateTime.of(2019, 10, 3, 1, 1), 1))
            expenseDao.insert(Expense(3600.0, "mogyesz tala", LocalDateTime.of(2019, 10, 5, 1, 1), 1))
            expenseDao.insert(Expense(3340.0, "telefonszámla", LocalDateTime.of(2019, 10, 6, 1, 1), 4))
            expenseDao.insert(Expense(2000.0, "belozzo", LocalDateTime.of(2019, 10, 8, 1, 1), 1))
            expenseDao.insert(Expense(3450.0, "bkv bérlet", LocalDateTime.of(2019, 10, 9, 1, 1), 4))
            expenseDao.insert(Expense(1500.0, "spar fagyi", LocalDateTime.of(2019, 10, 11, 1, 1), 7))
            expenseDao.insert(Expense(3800.0, "steam: anno 1404", LocalDateTime.of(2019, 10, 13, 1, 1), 8))
            expenseDao.insert(Expense(2000.0, "steam: anno 2070", LocalDateTime.of(2019, 10, 13, 1, 1), 8))
            expenseDao.insert(Expense(4000.0, "steam: anno 2205", LocalDateTime.of(2019, 10, 13, 1, 1), 1))
            expenseDao.insert(Expense(2000.0, "kfc", LocalDateTime.of(2019, 10, 15, 1, 1), 2))
            expenseDao.insert(Expense(13500.0, "kondi bérlet", LocalDateTime.of(2019, 10, 16, 1, 1), 10))
            expenseDao.insert(Expense(2400.0, "spar: rágó + gyümi", LocalDateTime.of(2019, 10, 17, 1, 1), 11))
            expenseDao.insert(Expense(1000.0, "spar", LocalDateTime.of(2019, 10, 18, 1, 1), 4))
            expenseDao.insert(Expense(1650.0, "balozzo", LocalDateTime.of(2019, 10, 20, 1, 1), 6))
            expenseDao.insert(Expense(3300.0, "media markt - pendrive", LocalDateTime.of(2019, 10, 20, 1, 1), 7))
            expenseDao.insert(Expense(660.0, "meki", LocalDateTime.of(2019, 10, 21, 1, 1), 1))
            expenseDao.insert(Expense(4000.0, "wow sub", LocalDateTime.of(2019, 10, 21, 1, 1), 2))
            expenseDao.insert(Expense(1500.0, "buli", LocalDateTime.of(2019, 10, 24, 1, 1), 3))
            expenseDao.insert(Expense(850.0, "gyros", LocalDateTime.of(2019, 10, 25, 1, 1), 6))
            expenseDao.insert(Expense(1800.0, "kfc", LocalDateTime.of(2019, 10, 26, 1, 1), 2))
            expenseDao.insert(Expense(12000.0, "berlin", LocalDateTime.of(2019, 10, 26, 1, 1), 4))
            expenseDao.insert(Expense(20140.0, "scitec vitamin + akció", LocalDateTime.of(2019, 10, 28, 1, 1), 4))
            expenseDao.insert(Expense(1240.0, "CC ivás", LocalDateTime.of(2019, 10, 30, 1, 1), 1))
            expenseDao.insert(Expense(60000.0, "zara: öltöny + cipő", LocalDateTime.of(2019, 10, 31, 1, 1), 2))
            expenseDao.insert(Expense(5000.0, "sticza szülinap", LocalDateTime.of(2019, 10, 31, 1, 1), 1))
            return null
        }
    }
}