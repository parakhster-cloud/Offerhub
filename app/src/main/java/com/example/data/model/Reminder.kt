package com.example.data.model

data class DealReminder(
    val id: String,
    val offerId: String,
    val offerTitle: String,
    val merchant: String,
    val expiryDate: String,
    val daysUntilExpiry: Int,
    val scheduledTimeDesc: String,
    val isTriggered: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
