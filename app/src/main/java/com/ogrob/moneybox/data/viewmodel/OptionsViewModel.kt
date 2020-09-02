package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.ogrob.moneybox.utils.*

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

}