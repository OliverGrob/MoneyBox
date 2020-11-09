package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ogrob.moneybox.data.repository.CategoryRepository
import com.ogrob.moneybox.data.repository.ExpenseRepository
import com.ogrob.moneybox.data.repository.HistoricalExchangeRateRepository
import com.ogrob.moneybox.data.retrofit.ExchangeRates
import com.ogrob.moneybox.data.retrofit.ExchangeRatesEndpoints
import com.ogrob.moneybox.data.retrofit.HistoricalExchangeRateConverter
import com.ogrob.moneybox.data.retrofit.ServiceBuilder
import com.ogrob.moneybox.persistence.model.*
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ExpenseAddAndEditViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository =
        ExpenseRepository(application, viewModelScope)
    private val categoryRepository: CategoryRepository =
        CategoryRepository(application, viewModelScope)
    private val exchangeRateRepository: HistoricalExchangeRateRepository =
        HistoricalExchangeRateRepository(application, viewModelScope)


    private val _allCategories: MutableLiveData<List<Category>> = MutableLiveData()
    val allCategories: LiveData<List<Category>> = _allCategories

    private val _unfilteredExpenses: MutableLiveData<List<CategoryWithExpenses>> = MutableLiveData()
    val unfilteredExpenses: LiveData<List<CategoryWithExpenses>> = _unfilteredExpenses

    private val _expensesCategory: MutableLiveData<Category> = MutableLiveData()
    val expensesCategory: LiveData<Category> = _expensesCategory

    private val _isExchangeRateNotAdded: MutableLiveData<Boolean> = MutableLiveData()
    val isExchangeRateNotAdded: LiveData<Boolean> = _isExchangeRateNotAdded


    fun getAllCategories() {
        viewModelScope.launch {
            _allCategories.value = categoryRepository.getAllCategories()
        }
    }

    fun getAllCategoriesWithExpenses() {
        viewModelScope.launch {
            _unfilteredExpenses.value = categoryRepository.getAllCategoriesWithExpenses()
        }
    }

    fun getAllExpensesDescription(categoriesWithExpenses: List<CategoryWithExpenses>): List<String> = categoriesWithExpenses
        .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
        .map(Expense::description)
        .distinct()

    fun getExpenseByDescription(description: String): Expense {
        return _unfilteredExpenses.value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .distinctBy { expense -> expense.description }
            .single { expense -> expense.description == description }
    }

    fun addOrEditExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, currency: String, categoryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (expenseId == NEW_EXPENSE_PLACEHOLDER_ID)
                addNewExpense(
                    expenseAmount,
                    expenseDescription,
                    expenseDate,
                    currency,
                    categoryId
                )
            else
                updateExpense(
                    expenseId,
                    expenseAmount,
                    expenseDescription,
                    expenseDate,
                    currency,
                    categoryId
                )
        }
    }

    private suspend fun addNewExpense(expenseAmount: String, expenseDescription: String, expenseDate: String, currency: String, categoryId: Long) {
        expenseRepository.addNewExpense(Expense(
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            Currency.valueOf(currency),
            categoryId))
    }

    private suspend fun updateExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, currency: String, categoryId: Long) {
        expenseRepository.updateExpense(Expense(
            expenseId,
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            Currency.valueOf(currency),
            categoryId))
    }

    fun deleteExpenseById(expenseId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.deleteExpenseById(expenseId)
        }
    }

    fun getExpensesCategory(categoryId: Long) {
        viewModelScope.launch {
            _expensesCategory.value = categoryRepository.getCategory(categoryId)
        }
    }

    fun checkIfExchangeRateForDateIsAlreadyAdded(date: LocalDate) {
        viewModelScope.launch {
            val exchangeRates = checkIfExchangeRateForDateIsAlreadyAddedOnUIThread(date)
            _isExchangeRateNotAdded.value = exchangeRates.isEmpty()
        }
    }

    private suspend fun checkIfExchangeRateForDateIsAlreadyAddedOnUIThread(date: LocalDate): List<HistoricalExchangeRate> {
        return withContext(Dispatchers.IO) {
            exchangeRateRepository.getExchangeRateForDate(date)
        }
    }

    fun getExchangeRatesForDateFromApi(date: LocalDate) {
        viewModelScope.launch {
            getExchangeRatesForDateFromApiOnUIThread(date)
//            _isExchangeRateNotAdded.value = false
        }
    }


    private val service = ServiceBuilder.buildService(ExchangeRatesEndpoints::class.java)

    private suspend fun getExchangeRatesForDateFromApiOnUIThread(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = service.getExchangeRatesForDate(date.toString())

            call.enqueue(
                object : Callback<ExchangeRates> {
                    override fun onResponse(
                        call: Call<ExchangeRates>,
                        response: Response<ExchangeRates>
                    ) {
                        response.body()?.let {
                            val dateFromApi = LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)

                            if (dateFromApi == date)
                                HistoricalExchangeRateConverter.convertToHistoricalExchangeRate(it).also {
                                    viewModelScope.launch { exchangeRateRepository.addExchangeRate(it) }
                                }
                            else
                                HistoricalExchangeRateConverter.convertToHistoricalExchangeRateWithCustomDate(it, date).also {
                                    viewModelScope.launch { exchangeRateRepository.addExchangeRate(it) }
                                }
                        }
                    }

                    override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                        t.printStackTrace()
                        Log.i("asd", "onFailure", t)
                    }

                }
            )
//            exchangeRateRepository.getExchangeRatesForDateFromApi(date)
        }
    }

}