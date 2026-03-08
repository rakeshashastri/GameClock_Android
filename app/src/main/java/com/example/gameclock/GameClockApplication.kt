package com.example.gameclock

import android.app.Application
import com.example.gameclock.billing.BillingManager

class GameClockApplication : Application() {

    val billingManager: BillingManager by lazy { BillingManager(this) }

    override fun onCreate() {
        super.onCreate()
        billingManager.startConnection()
    }
}