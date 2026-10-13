package com.example.data.model

data class SearchPattern(
    val query: String,
    val category: OfferCategory,
    val timestamp: Long = System.currentTimeMillis()
)

data class PurchasePattern(
    val id: String,
    val offerTitle: String,
    val merchant: String,
    val category: OfferCategory,
    val estimatedSavings: Double,
    val redeemedDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
    val name: String = "Alex Morgan",
    val email: String = "alex.deals@example.com",
    val isLoggedIn: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val homeCountry: String = "United States",
    val homeCity: String = "New York",
    val myCards: List<String> = listOf("American Express Platinum", "Chase Sapphire Preferred", "Apple Card"),
    val favoriteStores: List<String> = listOf("Whole Foods", "Costco", "Delta Airlines", "Apple"),
    val notificationsEnabled: Boolean = true,
    val pushDaysBeforeExpiry: Int = 2
)

data class AiInsight(
    val id: String,
    val title: String,
    val message: String,
    val relatedCategory: OfferCategory,
    val actionLabel: String = "View Deals",
    val priority: String = "HIGH" // HIGH, MEDIUM, NORMAL
)
