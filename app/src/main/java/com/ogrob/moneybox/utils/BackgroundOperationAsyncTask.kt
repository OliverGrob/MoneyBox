package com.ogrob.moneybox.utils

import android.os.AsyncTask
import kotlin.reflect.KFunction1

class BackgroundOperationAsyncTask<T>(private val operation: KFunction1<T, Unit>) : AsyncTask<T, Void, Void>() {

    override fun doInBackground(vararg params: T): Void? {
        params.forEach { operation.invoke(it) }
        return null
    }

}