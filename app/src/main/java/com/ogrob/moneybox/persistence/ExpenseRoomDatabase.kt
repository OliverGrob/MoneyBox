package com.ogrob.moneybox.persistence

import android.content.Context
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
            categoryDao.insert(Category(NO_CATEGORY_ID, NO_CATEGORY_NAME))
            categoryDao.insert(Category(2, "Food"))
            categoryDao.insert(Category(3, "Clothing"))
            categoryDao.insert(Category(4, "Hobby"))
            expenseDao.insert(Expense(1500.0, "KFC", LocalDateTime.now(), 1))
            expenseDao.insert(Expense(4000.0, "2 pars of slippers", LocalDateTime.now(), 2))
            return null
        }
    }
}