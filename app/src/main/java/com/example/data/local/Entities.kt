package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Offer
import com.example.data.model.OfferCategory

@Entity(tableName = "deals")
data class Deal(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val merchant: String,
    val discountPercentage: Double,
    val expiryDate: String,
    val originalPrice: Double,
    // Supporting fields for comprehensive local caching of aggregated offers
    val country: String = "United States",
    val city: String = "All Cities / Nationwide",
    val source: String = "OfferScout",
    val discountHighlight: String = "${discountPercentage.toInt()}% OFF",
    val description: String = "",
    val promoCode: String? = null,
    val linkUrl: String = "",
    val daysUntilExpiry: Int = 5,
    val verifiedRating: Int = 96,
    val minimumSpend: String? = null,
    val terms: String = "",
    val tags: String = "",
    val isFeatured: Boolean = false,
    val isAiDiscovered: Boolean = false,
    val cachedAtMillis: Long = System.currentTimeMillis()
) {
    fun toOffer(): Offer = Offer(
        id = id,
        title = title,
        merchant = merchant,
        source = source,
        category = try {
            OfferCategory.valueOf(category)
        } catch (e: Exception) {
            OfferCategory.ALL
        },
        country = country,
        city = city,
        discountPercentage = discountPercentage,
        originalPrice = originalPrice,
        discountHighlight = discountHighlight,
        description = description,
        promoCode = promoCode,
        linkUrl = linkUrl,
        expiryDate = expiryDate,
        daysUntilExpiry = daysUntilExpiry,
        verifiedRating = verifiedRating,
        minimumSpend = minimumSpend,
        terms = terms,
        tags = if (tags.isNotBlank()) tags.split(",") else emptyList(),
        isFeatured = isFeatured,
        isAiDiscovered = isAiDiscovered
    )
}

@Entity(tableName = "saved_offers")
data class SavedOfferEntity(
    @PrimaryKey val offerId: String,
    val savedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "dismissed_offers")
data class DismissedOfferEntity(
    @PrimaryKey val offerId: String,
    val dismissedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val categoryName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_history")
data class PurchaseRecordEntity(
    @PrimaryKey val id: String,
    val offerTitle: String,
    val merchant: String,
    val categoryName: String,
    val estimatedSavings: Double,
    val redeemedDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "deal_reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val offerId: String,
    val offerTitle: String,
    val merchant: String,
    val expiryDate: String,
    val daysUntilExpiry: Int,
    val scheduledTimeDesc: String,
    val isTriggered: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
