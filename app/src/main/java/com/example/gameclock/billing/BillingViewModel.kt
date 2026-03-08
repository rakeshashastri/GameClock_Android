package com.example.gameclock.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class BillingViewModel(private val billingManager: BillingManager) : ViewModel() {

    val isPremium: StateFlow<Boolean> = billingManager.isPremium
    val products = billingManager.products
    val purchaseInProgress: StateFlow<Boolean> = billingManager.purchaseInProgress

    fun purchase(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    fun restore() {
        billingManager.restorePurchases()
    }
}
