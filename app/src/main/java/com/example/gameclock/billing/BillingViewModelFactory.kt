package com.example.gameclock.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BillingViewModelFactory(
    private val billingManager: BillingManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillingViewModel(billingManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
