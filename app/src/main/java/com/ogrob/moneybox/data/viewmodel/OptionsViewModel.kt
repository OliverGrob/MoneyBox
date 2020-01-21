package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY
import com.ogrob.moneybox.utils.SharedPreferenceManager

class OptionsViewModel(application: Application) : AndroidViewModel(application) {

    fun retrieveAmountGoalFromSharedPreferences(context: Context): Float {
        return SharedPreferenceManager.getFloatSharedPreference(
            context,
            SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY,
            SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE)
    }

    fun updateAmountGoalInSharedPreferences(context: Context, goalAmount: Float) {
        SharedPreferenceManager.putFloatSharedPreference(
            context,
            SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY,
            goalAmount)
    }

}