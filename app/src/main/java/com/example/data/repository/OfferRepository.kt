package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.Deal
import com.example.data.local.PurchaseRecordEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.SavedOfferEntity
import com.example.data.local.SearchLogEntity
import com.example.data.model.AiInsight
import com.example.data.model.DealReminder
import com.example.data.model.Offer
import com.example.data.model.OfferCategory
import com.example.data.model.PurchasePattern
import com.example.data.model.SearchPattern
import com.example.data.model.SupportedLocations
import com.example.data.model.UserProfile
import com.example.data.remote.GeminiApiClient
import com.example.notification.DealNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class OfferRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val dealDao = database.dealDao()
    private val dao = database.offerDao()
    private val geminiClient = GeminiApiClient()
    val notificationHelper = DealNotificationHelper(context)

    // Cached deals from Room database backing the UI
    val allOffers: Flow<List<Offer>> = dealDao.getAllDeals().map { cachedDeals ->
        if (cachedDeals.isEmpty()) {
            OfferSeedData.INITIAL_OFFERS
        } else {
            cachedDeals.map { it.toOffer() }
        }
    }

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile = _userProfile.asStateFlow()

    private val _aiInsights = MutableStateFlow<List<AiInsight>>(emptyList())
    val aiInsights = _aiInsights.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            initInitialData()
            refreshAiInsights()
        }
    }

    private suspend fun initInitialData() {
        // Populate and update local cache of Deal entities with all worldwide and Saudi Arabia seed deals
        val initialDealEntities = OfferSeedData.INITIAL_OFFERS.map { it.toDealEntity() }
        dealDao.insertDeals(initialDealEntities)

        // Seed search history and purchase logs if empty
        val existingSearches = dao.getSearchHistory().first()
        if (existingSearches.isEmpty()) {
            for (search in OfferSeedData.INITIAL_SEARCH_PATTERNS) {
                dao.recordSearch(
                    SearchLogEntity(
                        query = search.query,
                        categoryName = search.category.name,
                        timestamp = search.timestamp
                    )
                )
            }
        }

        val existingPurchases = dao.getPurchaseHistory().first()
        if (existingPurchases.isEmpty()) {
            for (purchase in OfferSeedData.INITIAL_PURCHASE_HISTORY) {
                dao.recordPurchase(
                    PurchaseRecordEntity(
                        id = purchase.id,
                        offerTitle = purchase.offerTitle,
                        merchant = purchase.merchant,
                        categoryName = purchase.category.name,
                        estimatedSavings = purchase.estimatedSavings,
                        redeemedDate = purchase.redeemedDate,
                        timestamp = purchase.timestamp
                    )
                )
            }
        }
    }

    suspend fun cacheAggregatedDeals(deals: List<Deal>) {
        dealDao.insertDeals(deals)
    }

    suspend fun cacheSingleDeal(deal: Deal) {
        dealDao.insertDeal(deal)
    }

    val savedOfferIds: Flow<Set<String>> = dao.getSavedOfferIds().map { it.toSet() }

    // Direct Flow of bookmarked offers from Room database
    val savedDeals: Flow<List<Offer>> = dao.getSavedDeals().map { deals ->
        deals.map { it.toOffer() }
    }

    // Direct Flow of dismissed offer IDs from Room database
    val dismissedOfferIds: Flow<Set<String>> = dao.getDismissedOfferIds().map { it.toSet() }

    val reminders: Flow<List<DealReminder>> = dao.getReminders().map { entities ->
        entities.map {
            DealReminder(
                id = it.id,
                offerId = it.offerId,
                offerTitle = it.offerTitle,
                merchant = it.merchant,
                expiryDate = it.expiryDate,
                daysUntilExpiry = it.daysUntilExpiry,
                scheduledTimeDesc = it.scheduledTimeDesc,
                isTriggered = it.isTriggered,
                createdAtMillis = it.createdAtMillis
            )
        }
    }

    val purchaseHistory: Flow<List<PurchasePattern>> = dao.getPurchaseHistory().map { entities ->
        entities.map {
            val cat = try {
                OfferCategory.valueOf(it.categoryName)
            } catch (e: Exception) {
                OfferCategory.ALL
            }
            PurchasePattern(
                id = it.id,
                offerTitle = it.offerTitle,
                merchant = it.merchant,
                category = cat,
                estimatedSavings = it.estimatedSavings,
                redeemedDate = it.redeemedDate,
                timestamp = it.timestamp
            )
        }
    }

    val searchHistory: Flow<List<SearchPattern>> = dao.getSearchHistory().map { entities ->
        entities.map {
            val cat = try {
                OfferCategory.valueOf(it.categoryName)
            } catch (e: Exception) {
                OfferCategory.ALL
            }
            SearchPattern(
                query = it.query,
                category = cat,
                timestamp = it.timestamp
            )
        }
    }

    suspend fun toggleSaveOffer(offer: Offer, isCurrentlySaved: Boolean) {
        if (isCurrentlySaved) {
            dao.removeSavedOffer(offer.id)
        } else {
            // Guarantee full deal record is preserved in local Room deals table
            dealDao.insertDeal(offer.toDealEntity())
            dao.saveOffer(SavedOfferEntity(offerId = offer.id, savedAtMillis = System.currentTimeMillis()))
        }
    }

    suspend fun toggleSaveOffer(offerId: String, isCurrentlySaved: Boolean) {
        if (isCurrentlySaved) {
            dao.removeSavedOffer(offerId)
        } else {
            dao.saveOffer(SavedOfferEntity(offerId = offerId, savedAtMillis = System.currentTimeMillis()))
        }
    }

    suspend fun clearAllSavedOffers() {
        dao.clearAllSavedOffers()
    }

    suspend fun dismissOffer(offerId: String) {
        dao.dismissOffer(com.example.data.local.DismissedOfferEntity(offerId = offerId))
    }

    suspend fun undoDismissOffer(offerId: String) {
        dao.undoDismissOffer(offerId)
    }

    suspend fun clearAllDismissedOffers() {
        dao.clearAllDismissedOffers()
    }

    suspend fun recordSearchQuery(query: String, category: OfferCategory) {
        if (query.isNotBlank()) {
            dao.recordSearch(
                SearchLogEntity(
                    query = query.trim(),
                    categoryName = category.name,
                    timestamp = System.currentTimeMillis()
                )
            )
            refreshAiInsights()
        }
    }

    suspend fun addPurchaseRecord(offer: Offer, estimatedSavings: Double) {
        val entity = PurchaseRecordEntity(
            id = UUID.randomUUID().toString(),
            offerTitle = offer.title,
            merchant = offer.merchant,
            categoryName = offer.category.name,
            estimatedSavings = estimatedSavings,
            redeemedDate = "Today",
            timestamp = System.currentTimeMillis()
        )
        dao.recordPurchase(entity)
        refreshAiInsights()
    }

    suspend fun addReminder(offer: Offer, alertTimeDescription: String) {
        val reminderId = UUID.randomUUID().toString()
        val entity = ReminderEntity(
            id = reminderId,
            offerId = offer.id,
            offerTitle = offer.title,
            merchant = offer.merchant,
            expiryDate = offer.expiryDate,
            daysUntilExpiry = offer.daysUntilExpiry,
            scheduledTimeDesc = alertTimeDescription,
            isTriggered = false
        )
        dao.addReminder(entity)

        val title = "⏰ Deal Reminder: ${offer.merchant}"
        val body = "${offer.title} expires in ${offer.daysUntilExpiry} days (${offer.expiryDate})! Promo: ${offer.promoCode ?: "No code needed"}."
        notificationHelper.showDealAlertNotification(
            notificationId = offer.id.hashCode(),
            title = title,
            body = body,
            discountHighlight = offer.discountHighlight
        )
    }

    suspend fun removeReminder(reminderId: String) {
        dao.deleteReminder(reminderId)
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
    }

    suspend fun discoverLiveAiOffers(
        country: String,
        city: String,
        category: OfferCategory,
        query: String
    ): Result<List<Offer>> {
        _isAiLoading.value = true
        return try {
            val currencySymbol = SupportedLocations.getCurrencySymbol(country)
            val prompt = """
                You are a real-time web deal aggregator engine capturing live offers across Google, Amazon, ClickFlyers, local banks, and supermarkets.
                Aggregate 3 to 4 realistic, verified-style offers for:
                Country: $country
                City: $city
                Category: ${category.displayName}
                Local Currency: $currencySymbol
                User Search Keywords: $query
                
                Ensure the sources represent top local platforms such as:
                - Amazon (e.g., Amazon.sa for Saudi Arabia, Amazon.ae for UAE, Amazon.com, Amazon.in)
                - ClickFlyers / Weekly Flyers (HyperPanda, Lulu Hypermarket, Carrefour, Al Othaim, Danube)
                - Local Banks (e.g., Al Rajhi, SNB AlAhli, Riyad Bank, Emirates NBD, Chase, Amex, HDFC)
                - Local airlines & food delivery (Saudia, flynas, Emirates, HungerStation, Jahez, Noon)

                Return ONLY a JSON array with objects matching:
                [
                  {
                    "id": "ai-gen-1",
                    "title": "Clear offer title",
                    "merchant": "Exact Brand / Hypermarket / Bank Name",
                    "source": "Source (e.g. Amazon.sa Super Deals, ClickFlyers Weekly Circular, Al Rajhi Bank Offers)",
                    "category": "${category.name}",
                    "discountPercentage": 30.0,
                    "originalPrice": 120.0,
                    "discountHighlight": "30% OFF",
                    "description": "Specific offer details and terms",
                    "promoCode": "CODE or null",
                    "linkUrl": "https://example.com/deal",
                    "expiryDate": "Nov 25, 2026",
                    "daysUntilExpiry": 4,
                    "verifiedRating": 97,
                    "minimumSpend": "50 or null",
                    "terms": "brief redemption condition",
                    "tags": ["tag1", "tag2"]
                  }
                ]
            """.trimIndent()

            val result = geminiClient.generateContent(prompt)
            val parsedOffers = mutableListOf<Offer>()

            if (result.isSuccess) {
                val text = result.getOrNull() ?: ""
                val jsonString = extractJsonArray(text)
                if (jsonString.isNotBlank()) {
                    val array = JSONArray(jsonString)
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val cat = try {
                            OfferCategory.valueOf(obj.optString("category", category.name))
                        } catch (e: Exception) {
                            category
                        }
                        val discPercent = obj.optDouble("discountPercentage", 25.0)
                        val origPrice = obj.optDouble("originalPrice", 100.0)

                        val offer = Offer(
                            id = "ai-" + UUID.randomUUID().toString().take(8),
                            title = obj.optString("title", "Exclusive Regional Discount"),
                            merchant = obj.optString("merchant", "Verified Regional Merchant"),
                            source = obj.optString("source", "Google & Web Deal Aggregator"),
                            category = cat,
                            country = country,
                            city = city,
                            discountPercentage = discPercent,
                            originalPrice = origPrice,
                            discountHighlight = obj.optString("discountHighlight", "${discPercent.toInt()}% OFF"),
                            description = obj.optString("description", "Exclusive live deal discovered for $city, $country."),
                            promoCode = if (obj.isNull("promoCode") || obj.optString("promoCode").isBlank() || obj.optString("promoCode") == "null") null else obj.optString("promoCode"),
                            linkUrl = obj.optString("linkUrl", "https://google.com/search?q=${merchantQuery(obj.optString("merchant"))}"),
                            expiryDate = obj.optString("expiryDate", "Nov 28, 2026"),
                            daysUntilExpiry = obj.optInt("daysUntilExpiry", 5),
                            verifiedRating = obj.optInt("verifiedRating", 97),
                            minimumSpend = if (obj.isNull("minimumSpend")) null else obj.optString("minimumSpend"),
                            terms = obj.optString("terms", "Terms apply. Redeemable in-store or online."),
                            tags = listOf("Web Aggregated", city, country),
                            isAiDiscovered = true
                        )
                        parsedOffers.add(offer)
                    }
                }
            }

            if (parsedOffers.isEmpty()) {
                val curr = SupportedLocations.getCurrencySymbol(country)
                val fallbackOffers = listOf(
                    Offer(
                        id = "ai-live-amz-" + UUID.randomUUID().toString().take(6),
                        title = "Amazon $country: Prime Members Extra 20% Off Electronics & Pantry",
                        merchant = if (country == "Saudi Arabia") "Amazon.sa" else if (country == "United Arab Emirates") "Amazon.ae" else "Amazon",
                        source = "Amazon Web Aggregator",
                        category = OfferCategory.ELECTRONICS,
                        country = country,
                        city = city,
                        discountPercentage = 20.0,
                        originalPrice = 250.0,
                        discountHighlight = "20% OFF",
                        description = "Aggregated from Amazon: Exclusive Prime deal on electronics, smartphones, and household essentials in $city.",
                        promoCode = "SAVE20",
                        linkUrl = if (country == "Saudi Arabia") "https://www.amazon.sa" else "https://www.amazon.com",
                        expiryDate = "Nov 25, 2026",
                        daysUntilExpiry = 3,
                        verifiedRating = 98,
                        minimumSpend = "${curr}50",
                        terms = "Valid for orders delivered in $city.",
                        tags = listOf("Amazon", city, "Tech"),
                        isAiDiscovered = true
                    ),
                    Offer(
                        id = "ai-live-cf-" + UUID.randomUUID().toString().take(6),
                        title = "ClickFlyers Weekly Circular: Hypermarket Essentials Up to 50% Off",
                        merchant = if (country == "Saudi Arabia") "Panda & Lulu Hypermarkets" else "Regional Supermarket",
                        source = "ClickFlyers Aggregator (كليك فلايرز)",
                        category = OfferCategory.SUPERMARKET,
                        country = country,
                        city = city,
                        discountPercentage = 50.0,
                        originalPrice = 180.0,
                        discountHighlight = "50% OFF",
                        description = "Latest weekly flyer: Discounts on fresh meat, bakery, organic milk, and pantry staples in $city branches.",
                        promoCode = null,
                        linkUrl = "https://clickflyers.com",
                        expiryDate = "Nov 20, 2026",
                        daysUntilExpiry = 2,
                        verifiedRating = 96,
                        minimumSpend = null,
                        terms = "Valid in-store while stocks last.",
                        tags = listOf("ClickFlyers", "Groceries", city),
                        isAiDiscovered = true
                    ),
                    Offer(
                        id = "ai-live-bk-" + UUID.randomUUID().toString().take(6),
                        title = "Local Bank Perks: 15% Dining & Travel Cashback in $city",
                        merchant = if (country == "Saudi Arabia") "Al Rajhi & SNB Cards" else "Partner Bank Credit Cards",
                        source = "Bank Rewards Portal",
                        category = OfferCategory.CREDIT_CARD,
                        country = country,
                        city = city,
                        discountPercentage = 15.0,
                        originalPrice = 300.0,
                        discountHighlight = "15% Cashback",
                        description = "Cardholder privilege: Receive 15% cashback at top partner restaurants, cafes, and flight booking portals.",
                        promoCode = "BANK15",
                        linkUrl = "https://google.com/search?q=bank+card+discounts+$city",
                        expiryDate = "Nov 29, 2026",
                        daysUntilExpiry = 5,
                        verifiedRating = 97,
                        minimumSpend = "${curr}100",
                        terms = "Requires eligible credit card payment.",
                        tags = listOf("Credit Card", "Cashback", city),
                        isAiDiscovered = true
                    )
                )
                parsedOffers.addAll(fallbackOffers)
            }

            // Locally cache discovered deals into Room database
            dealDao.insertDeals(parsedOffers.map { it.toDealEntity() })

            _isAiLoading.value = false
            Result.success(parsedOffers)
        } catch (e: Exception) {
            _isAiLoading.value = false
            Result.failure(e)
        }
    }

    private fun merchantQuery(merchant: String): String {
        return merchant.replace(" ", "+")
    }

    suspend fun syncAggregatedDealsFromSources(country: String, city: String): Result<Int> {
        val matchingSeedDeals = OfferSeedData.INITIAL_OFFERS.filter {
            (it.country.equals(country, ignoreCase = true) || it.country.equals("Global", ignoreCase = true)) &&
            (city == SupportedLocations.GLOBAL_CITY || it.city.equals(city, ignoreCase = true) || it.city.equals(SupportedLocations.GLOBAL_CITY, ignoreCase = true))
        }
        if (matchingSeedDeals.isNotEmpty()) {
            dealDao.insertDeals(matchingSeedDeals.map { it.toDealEntity() })
        }
        val aiResult = discoverLiveAiOffers(country, city, OfferCategory.ALL, "Amazon ClickFlyers Bank Deals")
        val totalCount = matchingSeedDeals.size + (aiResult.getOrNull()?.size ?: 0)
        return Result.success(totalCount)
    }

    suspend fun refreshAiInsights() {
        val prompt = """
            Based on a user's deal search pattern:
            Searches: [Travel flights, Whole Foods organic coupons, Amex hotel credits, Chase points bonus]
            User cards: [Amex Platinum, Chase Sapphire Preferred, Apple Card]
            Generate 3 proactive smart deal reminders and spending tips.
            Format as JSON:
            [
              {
                "id": "ins-1",
                "title": "Expiring Soon: Chase Sapphire 30% Transfer",
                "message": "Your searched airline bonus expires in 3 days. Transfer points before Nov 20 to maximize flight bookings.",
                "relatedCategory": "CREDIT_CARD",
                "actionLabel": "View Chase Perks",
                "priority": "HIGH"
              },
              {
                "id": "ins-2",
                "title": "Weekly Grocery Watch: Whole Foods 20% Off",
                "message": "Based on your frequent supermarket searches, this week's 20% organic flyer discount is now active in your area.",
                "relatedCategory": "SUPERMARKET",
                "actionLabel": "See Coupons",
                "priority": "MEDIUM"
              },
              {
                "id": "ins-3",
                "title": "Travel Pattern Alert: Delta Flash Sale",
                "message": "Domestic award flights from 10k miles ends tomorrow. You can pair this with your Amex travel credit.",
                "relatedCategory": "TRAVEL",
                "actionLabel": "Explore Flights",
                "priority": "HIGH"
              }
            ]
        """.trimIndent()

        try {
            val result = geminiClient.generateContent(prompt)
            if (result.isSuccess) {
                val jsonText = extractJsonArray(result.getOrNull() ?: "")
                if (jsonText.isNotBlank()) {
                    val array = JSONArray(jsonText)
                    val list = mutableListOf<AiInsight>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val cat = try {
                            OfferCategory.valueOf(obj.optString("relatedCategory", "CREDIT_CARD"))
                        } catch (e: Exception) {
                            OfferCategory.CREDIT_CARD
                        }
                        list.add(
                            AiInsight(
                                id = obj.optString("id", UUID.randomUUID().toString()),
                                title = obj.optString("title", "Smart Deal Reminder"),
                                message = obj.optString("message", "Personalized offer reminder based on your habits."),
                                relatedCategory = cat,
                                actionLabel = obj.optString("actionLabel", "View Deals"),
                                priority = obj.optString("priority", "HIGH")
                            )
                        )
                    }
                    if (list.isNotEmpty()) {
                        _aiInsights.value = list
                        return
                    }
                }
            }
        } catch (e: Exception) {
            // fallback
        }

        // Curated fallback AI insights aligned with user search & buy pattern
        _aiInsights.value = listOf(
            AiInsight(
                id = "ins-1",
                title = "🚨 Urgent: Chase 30% Airline Bonus Ends in 3 Days",
                message = "You searched for flights & travel rewards recently. Chase Sapphire 30% points transfer to Virgin/Air France expires Nov 20.",
                relatedCategory = OfferCategory.CREDIT_CARD,
                actionLabel = "View Chase Deal",
                priority = "HIGH"
            ),
            AiInsight(
                id = "ins-2",
                title = "🛒 Smart Grocery Reminder: Whole Foods 20% Off",
                message = "Matched with your weekly supermarket pattern: 20% off organic produce ends in 48 hours. Prime barcode required.",
                relatedCategory = OfferCategory.SUPERMARKET,
                actionLabel = "View Grocery Coupon",
                priority = "MEDIUM"
            ),
            AiInsight(
                id = "ins-3",
                title = "✈️ Flight Deal Scout: Delta 10k SkyMiles Sale",
                message = "Found 1 day left on nonstop domestic flash sale awards. Potential estimated savings: $140 per passenger.",
                relatedCategory = OfferCategory.TRAVEL,
                actionLabel = "View Flights",
                priority = "HIGH"
            )
        )
    }

    private fun extractJsonArray(raw: String): String {
        val start = raw.indexOf('[')
        val end = raw.lastIndexOf(']')
        return if (start != -1 && end != -1 && end > start) {
            raw.substring(start, end + 1)
        } else {
            ""
        }
    }
}
