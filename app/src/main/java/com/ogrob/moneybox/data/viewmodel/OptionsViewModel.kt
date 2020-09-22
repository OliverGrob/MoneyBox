package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ogrob.moneybox.data.repository.ExpenseRepository
import com.ogrob.moneybox.utils.*
import kotlinx.coroutines.*
import java.time.Month

class OptionsViewModel(application: Application) : AndroidViewModel(application) {

    fun retrieveAmountGoalFromSharedPreferences(context: Context): Float {
        return SharedPreferenceManager.getFloatSharedPreference(
            context,
            SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY,
            SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE)
    }

    fun updateAmountGoalInSharedPreferences(context: Context, newGoalAmount: Float) {
        SharedPreferenceManager.putFloatSharedPreference(
            context,
            SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY,
            newGoalAmount)
    }

    fun retrieveDefaultCurrencyFromSharedPreferences(context: Context): String {
        return SharedPreferenceManager.getStringSharedPreference(
            context,
            SHARED_PREFERENCES_CURRENCY_KEY,
            SHARED_PREFERENCES_DEFAULT_CURRENCY)
    }

    fun updateDefaultCurrencyInSharedPreferences(context: Context, newDefaultCurrency: String) {
        SharedPreferenceManager.putStringSharedPreference(
            context,
            SHARED_PREFERENCES_CURRENCY_KEY,
            newDefaultCurrency)
    }


    private val expenseRepository: ExpenseRepository =
        ExpenseRepository(application, viewModelScope)


    private val _buttonEnabled = MutableLiveData<Boolean>()
    val buttonEnabled: LiveData<Boolean> = _buttonEnabled

    private val _size = MutableLiveData<Int>()
    val size: LiveData<Int> = _size


    init {
        _buttonEnabled.value = true
    }


    fun startSpeedQuery(year: Int, month: Month) {
        viewModelScope.launch {
            _buttonEnabled.value = false
            try {
                _size.value = expenseRepository.speedQuery(year, month)
            }
            finally {
                _buttonEnabled.value = true
            }
        }
    }

}