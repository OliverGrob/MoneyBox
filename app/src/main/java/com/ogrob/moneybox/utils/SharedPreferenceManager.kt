package com.ogrob.moneybox.utils

import android.content.Context
import androidx.preference.PreferenceManager

object SharedPreferenceManager {

    fun getStringSharedPreference(context: Context, key: String, defaultValue: String): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(key, defaultValue)
            .orEmpty()
    }

    fun putStringSharedPreference(context: Context, key: String, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key, value)
            .apply()
    }

    fun getFloatSharedPreference(context: Context, key: String, defaultValue: Float): Float {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getFloat(key, defaultValue)
    }

    fun putFloatSharedPreference(context: Context, key: String, value: Float) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putFloat(key, value)
            .apply()
    }

}