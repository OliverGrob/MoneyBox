package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_NAME

class OptionsViewModel(application: Application) : AndroidViewModel(application) {

    fun retrieveAmountGoalFromSharedPreferences(context: Context): Float {
        return context
            .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getFloat(
                SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY,
                SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE)
    }

    fun updateAmountGoalInSharedPreferences(context: Context, goalAmount: Float) {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_KEY, goalAmount)
            .apply()
    }

}