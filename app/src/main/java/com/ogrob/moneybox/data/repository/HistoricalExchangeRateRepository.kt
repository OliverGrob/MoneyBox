package com.ogrob.moneybox.data.repository

import android.app.Application
import android.util.Log
import com.ogrob.moneybox.data.retrofit.ExchangeRates
import com.ogrob.moneybox.data.retrofit.ExchangeRatesEndpoints
import com.ogrob.moneybox.data.retrofit.HistoricalExchangeRateConverter
import com.ogrob.moneybox.data.retrofit.ServiceBuilder
import com.ogrob.moneybox.persistence.ExpenseRoomDatabase
import com.ogrob.moneybox.persistence.dao.HistoricalExchangeRateDao
import com.ogrob.moneybox.persistence.model.HistoricalExchangeRate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HistoricalExchangeRateRepository(
    application: Application,
    private val coroutineScope: CoroutineScope
) {

    private val exchangeRateDao: HistoricalExchangeRateDao

    private val service = ServiceBuilder.buildService(ExchangeRatesEndpoints::class.java)


    init {
        val expenseRoomDatabase = ExpenseRoomDatabase.getDatabase(application, coroutineScope)
        this.exchangeRateDao = expenseRoomDatabase.historicalExchangeRateDao()
    }


    suspend fun getExchangeRateForDate(date: LocalDate): List<HistoricalExchangeRate> = exchangeRateDao.getHistoricalExchangeRateForDate(date)

    suspend fun addExchangeRate(historicalExchangeRate: HistoricalExchangeRate) {
        exchangeRateDao.insert(historicalExchangeRate)
    }

    suspend fun getExchangeRatesForDatesFromApi(dates: List<LocalDate>) {
        dates.map { date -> getExchangeRatesForDateFromApi(date) }
    }

    /**
     * https://exchangeratesapi.io/{date} -> there might be no exchange rates for 'date', in that case the api gives back the latest rates,
     * so we have to check if the date is the same, and modify if not
     */
    suspend fun getExchangeRatesForDateFromApi(additionDate: LocalDate) {
        val call = service.getExchangeRatesForDate(additionDate.toString())

        call.enqueue(
            object : Callback<ExchangeRates> {
                override fun onResponse(
                    call: Call<ExchangeRates>,
                    response: Response<ExchangeRates>
                ) {
                    response.body()?.let {
                        val dateFromApi = LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)

                        if (dateFromApi == additionDate)
                            HistoricalExchangeRateConverter.convertToHistoricalExchangeRate(it).also {
                                coroutineScope.launch { exchangeRateDao.insert(it) }
                            }
                        else
                            HistoricalExchangeRateConverter.convertToHistoricalExchangeRateWithCustomDate(it, additionDate).also {
                                coroutineScope.launch { exchangeRateDao.insert(it) }
                            }
                    }
                }

                override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                    t.printStackTrace()
                    Log.i("asd", "onFailure", t)
                }

            }
        )
    }

}