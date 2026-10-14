package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Offer
import com.example.data.model.OfferCategory

/**
 * Constants and classifier for deal organizational types.
 */
object DealType {
    const val TRAVEL = "Travel"
    const val CREDIT_CARD = "Credit Card"
    const val LOCAL = "Local"

    val ALL_TYPES = listOf(TRAVEL, CREDIT_CARD, LOCAL)

    /**
     * Determines whether an offer is categorized as Travel, Credit Card, or Local.
     */
    fun classify(offer: Offer): String {
        return when {
            offer.category == OfferCategory.TRAVEL ||
            offer.merchant.contains("Airline", true) ||
            offer.merchant.contains("Fly", true) ||
            offer.merchant.contains("Emirates", true) ||
            offer.merchant.contains("Saudia", true) ||
            offer.merchant.contains("Hotel", true) ||
            offer.merchant.contains("Booking", true) -> TRAVEL

            offer.category == OfferCategory.CREDIT_CARD ||
            offer.merchant.contains("Bank", true) ||
            offer.merchant.contains("Visa", true) ||
            offer.merchant.contains("Mastercard", true) ||
            offer.merchant.contains("American Express", true) ||
            offer.merchant.contains("Amex", true) ||
            offer.merchant.contains("Chase", true) ||
            offer.merchant.contains("AlRajhi", true) ||
            offer.merchant.contains("SNB", true) ||
            offer.merchant.contains("ENBD", true) -> CREDIT_CARD

            else -> LOCAL
        }
    }
}

/**
 * Room entity representing an offer categorized into types like 'Travel', 'Credit Card', and 'Local'.
 */
@Entity(
    tableName = "categorized_deals",
    indices = [
        Index(value = ["dealType"]),
        Index(value = ["category"]),
        Index(value = ["country", "city"])
    ]
)
data class CategorizedDealEntity(
    @PrimaryKey val id: String,
    val title: String,
    val merchant: String,
    val dealType: String, // 'Travel', 'Credit Card', 'Local'
    val category: String,
    val discountPercentage: Double,
    val discountHighlight: String,
    val description: String,
    val promoCode: String? = null,
    val linkUrl: String = "",
    val expiryDate: String,
    val daysUntilExpiry: Int = 7,
    val country: String = "United States",
    val city: String = "All Cities / Nationwide",
    val originalPrice: Double = 0.0,
    val source: String = "OfferScout",
    val verifiedRating: Int = 96,
    val terms: String = "",
    val tags: String = "",
    val isFeatured: Boolean = false,
    val isAiDiscovered: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toOffer(): Offer = Offer(
        id = id,
        title = title,
        merchant = merchant,
        source = source,
        category = try {
            OfferCategory.valueOf(category)
        } catch (e: Exception) {
            when (dealType) {
                DealType.TRAVEL -> OfferCategory.TRAVEL
                DealType.CREDIT_CARD -> OfferCategory.CREDIT_CARD
                else -> OfferCategory.SUPERMARKET
            }
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
        terms = terms,
        tags = if (tags.isNotBlank()) tags.split(",") else emptyList(),
        isFeatured = isFeatured,
        isAiDiscovered = isAiDiscovered
    )

    companion object {
        fun fromOffer(offer: Offer, explicitType: String? = null): CategorizedDealEntity {
            val type = explicitType ?: DealType.classify(offer)
            return CategorizedDealEntity(
                id = offer.id,
                title = offer.title,
                merchant = offer.merchant,
                dealType = type,
                category = offer.category.name,
                discountPercentage = offer.discountPercentage,
                discountHighlight = offer.discountHighlight,
                description = offer.description,
                promoCode = offer.promoCode,
                linkUrl = offer.linkUrl,
                expiryDate = offer.expiryDate,
                daysUntilExpiry = offer.daysUntilExpiry,
                country = offer.country,
                city = offer.city,
                originalPrice = offer.originalPrice,
                source = offer.source,
                verifiedRating = offer.verifiedRating,
                terms = offer.terms,
                tags = offer.tags.joinToString(","),
                isFeatured = offer.isFeatured,
                isAiDiscovered = offer.isAiDiscovered
            )
        }
    }
}
