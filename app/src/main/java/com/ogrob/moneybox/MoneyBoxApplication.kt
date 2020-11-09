package com.ogrob.moneybox

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MoneyBoxApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

}