package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.data.model.AiInsight
import com.example.data.model.DealReminder
import com.example.data.model.DealSortOption
import com.example.data.model.Offer
import com.example.data.model.OfferCategory
import com.example.data.model.PurchasePattern
import com.example.data.model.SearchPattern
import com.example.data.model.SupportedLocations
import com.example.data.model.UserProfile
import com.example.data.repository.FirebaseSyncRepository
import com.example.data.repository.OfferRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class AppScreen(val title: String) {
    HOME("Explore"),
    WISHLIST("Wishlist"),
    AI_SCOUT("AI Deal Scout"),
    REMINDERS("Reminders"),
    PATTERNS("Spend Patterns"),
    PROFILE("Profile & Cards")
}

data class FilterState(
    val selectedCountry: String = SupportedLocations.DEFAULT_COUNTRY,
    val selectedCity: String = SupportedLocations.DEFAULT_CITY,
    val selectedCategory: OfferCategory = OfferCategory.ALL,
    val selectedSource: String = "All Sources",
    val searchQuery: String = "",
    val onlyExpiringSoon: Boolean = false,
    val onlyWithPromoCode: Boolean = false,
    val onlyWishlisted: Boolean = false,
    val sortOption: DealSortOption = DealSortOption.RECOMMENDED
)

class OfferViewModel(application: Application) : AndroidViewModel(application) {

    val repository = OfferRepository(application)
    val firebaseSyncRepo = FirebaseSyncRepository(application)

    val firebaseUser: StateFlow<FirebaseUser?> = firebaseSyncRepo.currentUser.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Firebase.auth.currentUser
    )

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen = _currentScreen.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    private val _selectedOffer = MutableStateFlow<Offer?>(null)
    val selectedOffer = _selectedOffer.asStateFlow()

    private val _reminderTargetOffer = MutableStateFlow<Offer?>(null)
    val reminderTargetOffer = _reminderTargetOffer.asStateFlow()

    private val _redeemTargetOffer = MutableStateFlow<Offer?>(null)
    val redeemTargetOffer = _redeemTargetOffer.asStateFlow()

    private val _showLocationDialog = MutableStateFlow(false)
    val showLocationDialog = _showLocationDialog.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    val savedOfferIds: StateFlow<Set<String>> = repository.savedOfferIds.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptySet()
    )

    val reminders: StateFlow<List<DealReminder>> = repository.reminders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val purchaseHistory: StateFlow<List<PurchasePattern>> = repository.purchaseHistory.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val searchHistory: StateFlow<List<SearchPattern>> = repository.searchHistory.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allOffers: StateFlow<List<Offer>> = repository.allOffers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val aiInsights: StateFlow<List<AiInsight>> = repository.aiInsights
    val isAiLoading: StateFlow<Boolean> = repository.isAiLoading

    // Computed filtered list of offers
    val displayedOffers: StateFlow<List<Offer>> = combine(
        repository.allOffers,
        _filterState,
        repository.savedOfferIds
    ) { offers, filter, savedIds ->
        val filtered = offers.filter { offer ->
            // Country match: offer matches selected country or is nationwide/global
            val countryMatches = offer.country.equals(filter.selectedCountry, ignoreCase = true) ||
                    offer.country.equals("Global", ignoreCase = true)

            // City match: if user chose specific city, match city or match Global/Nationwide
            val cityMatches = if (filter.selectedCity == SupportedLocations.GLOBAL_CITY) {
                true
            } else {
                offer.city.equals(filter.selectedCity, ignoreCase = true) ||
                        offer.city.equals(SupportedLocations.GLOBAL_CITY, ignoreCase = true)
            }

            // Category match
            val categoryMatches = filter.selectedCategory == OfferCategory.ALL ||
                    offer.category == filter.selectedCategory

            // Source match (Amazon, ClickFlyers, Banks, Supermarkets)
            val sourceMatches = when (filter.selectedSource) {
                "Amazon" -> offer.source.contains("Amazon", ignoreCase = true) || offer.merchant.contains("Amazon", ignoreCase = true) || offer.tags.any { it.contains("Amazon", ignoreCase = true) }
                "ClickFlyers" -> offer.source.contains("ClickFlyer", ignoreCase = true) || offer.tags.any { it.contains("ClickFlyer", ignoreCase = true) }
                "Banks & Cards" -> offer.category == OfferCategory.CREDIT_CARD || offer.source.contains("Bank", ignoreCase = true) || offer.merchant.contains("Bank", ignoreCase = true) || offer.tags.any { it.contains("Bank", ignoreCase = true) || it.contains("Card", ignoreCase = true) }
                "Supermarkets" -> offer.category == OfferCategory.SUPERMARKET
                else -> true
            }

            // Search query
            val query = filter.searchQuery.trim().lowercase()
            val queryMatches = if (query.isEmpty()) {
                true
            } else {
                offer.title.lowercase().contains(query) ||
                        offer.merchant.lowercase().contains(query) ||
                        offer.source.lowercase().contains(query) ||
                        offer.description.lowercase().contains(query) ||
                        offer.tags.any { it.lowercase().contains(query) }
            }

            // Urgency filter
            val urgencyMatches = !filter.onlyExpiringSoon || offer.isExpiringSoon

            // Promo code filter
            val promoMatches = !filter.onlyWithPromoCode || !offer.promoCode.isNullOrBlank()

            // Wishlist / Saved filter
            val wishlistMatches = !filter.onlyWishlisted || savedIds.contains(offer.id)

            countryMatches && cityMatches && categoryMatches && sourceMatches && queryMatches && urgencyMatches && promoMatches && wishlistMatches
        }

        // Apply deal sorting (by discount percentage, expiry date, or recommended)
        when (filter.sortOption) {
            DealSortOption.DISCOUNT_DESC -> filtered.sortedByDescending { it.discountPercentage }
            DealSortOption.DISCOUNT_ASC -> filtered.sortedBy { it.discountPercentage }
            DealSortOption.EXPIRY_SOONEST -> filtered.sortedWith(
                compareBy<Offer> { it.daysUntilExpiry }
                    .thenBy { it.expiryDate }
                    .thenByDescending { it.discountPercentage }
            )
            DealSortOption.EXPIRY_LATEST -> filtered.sortedWith(
                compareByDescending<Offer> { it.daysUntilExpiry }
                    .thenByDescending { it.expiryDate }
            )
            DealSortOption.RECOMMENDED -> filtered.sortedWith(
                compareByDescending<Offer> { it.isFeatured }
                    .thenBy { it.daysUntilExpiry }
                    .thenByDescending { it.discountPercentage }
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Saved offers list
    val savedOffers: StateFlow<List<Offer>> = combine(
        repository.allOffers,
        savedOfferIds
    ) { offers, ids ->
        offers.filter { ids.contains(it.id) }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Total savings computation
    val totalSavings: StateFlow<Double> = purchaseHistory.combine(repository.allOffers) { purchases, _ ->
        purchases.sumOf { it.estimatedSavings }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.0
    )

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        _selectedOffer.value = null
    }

    fun selectOffer(offer: Offer?) {
        _selectedOffer.value = offer
    }

    fun openReminderDialog(offer: Offer) {
        _reminderTargetOffer.value = offer
    }

    fun closeReminderDialog() {
        _reminderTargetOffer.value = null
    }

    fun openRedeemDialog(offer: Offer) {
        _redeemTargetOffer.value = offer
    }

    fun closeRedeemDialog() {
        _redeemTargetOffer.value = null
    }

    fun setLocationDialog(show: Boolean) {
        _showLocationDialog.value = show
    }

    fun updateCountry(country: String) {
        val location = SupportedLocations.LOCATIONS.find { it.country.equals(country, ignoreCase = true) }
        val defaultCity = if (country.equals("Saudi Arabia", ignoreCase = true)) {
            "Riyadh"
        } else {
            location?.cities?.getOrNull(1) ?: location?.cities?.firstOrNull() ?: SupportedLocations.GLOBAL_CITY
        }
        _filterState.value = _filterState.value.copy(
            selectedCountry = country,
            selectedCity = defaultCity
        )
        viewModelScope.launch {
            repository.syncAggregatedDealsFromSources(country, defaultCity)
            val info = SupportedLocations.getCountryInfo(country)
            _userMessage.value = "Location updated to $defaultCity, $country • Currency: ${info.currencySymbol} (${info.currencyCode})"
        }
    }

    fun updateCity(city: String) {
        _filterState.value = _filterState.value.copy(selectedCity = city)
        viewModelScope.launch {
            repository.syncAggregatedDealsFromSources(_filterState.value.selectedCountry, city)
        }
    }

    fun updateCategory(category: OfferCategory) {
        _filterState.value = _filterState.value.copy(selectedCategory = category)
    }

    fun toggleCategory(category: OfferCategory) {
        val newCategory = if (_filterState.value.selectedCategory == category) {
            OfferCategory.ALL
        } else {
            category
        }
        _filterState.value = _filterState.value.copy(selectedCategory = newCategory)
    }

    fun updateSortOption(sortOption: DealSortOption) {
        _filterState.value = _filterState.value.copy(sortOption = sortOption)
        val sortLabel = when (sortOption) {
            DealSortOption.DISCOUNT_DESC -> "Sorted: Highest Discount %"
            DealSortOption.DISCOUNT_ASC -> "Sorted: Lowest Discount %"
            DealSortOption.EXPIRY_SOONEST -> "Sorted: Expiring Soonest"
            DealSortOption.EXPIRY_LATEST -> "Sorted: Valid Longer"
            DealSortOption.RECOMMENDED -> "Sorted: Recommended Deals"
        }
        _userMessage.value = sortLabel
    }

    fun updateSource(source: String) {
        _filterState.value = _filterState.value.copy(selectedSource = source)
    }

    fun syncLiveDeals() {
        viewModelScope.launch {
            val country = _filterState.value.selectedCountry
            val city = _filterState.value.selectedCity
            _userMessage.value = "Scanning Google, Amazon, ClickFlyers & Banks for $city..."
            val result = repository.syncAggregatedDealsFromSources(country, city)
            val count = result.getOrNull() ?: 0
            _userMessage.value = "Synced $count deals for $city ($country) from Amazon, ClickFlyers & Banks!"
        }
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
        if (query.length >= 3) {
            viewModelScope.launch {
                repository.recordSearchQuery(query, _filterState.value.selectedCategory)
            }
        }
    }

    fun toggleExpiringSoonFilter() {
        _filterState.value = _filterState.value.copy(
            onlyExpiringSoon = !_filterState.value.onlyExpiringSoon
        )
    }

    fun togglePromoCodeFilter() {
        _filterState.value = _filterState.value.copy(
            onlyWithPromoCode = !_filterState.value.onlyWithPromoCode
        )
    }

    fun toggleWishlistFilter() {
        val nextVal = !_filterState.value.onlyWishlisted
        _filterState.value = _filterState.value.copy(onlyWishlisted = nextVal)
        _userMessage.value = if (nextVal) "Filter active: Wishlist & Saved Deals only" else "Showing All Deals"
    }

    fun toggleSaveOffer(offer: Offer) {
        viewModelScope.launch {
            val isSaved = savedOfferIds.value.contains(offer.id)
            repository.toggleSaveOffer(offer, isSaved)
            _userMessage.value = if (isSaved) "Removed from Wishlist" else "Saved to Wishlist (Local Database)!"
        }
    }

    fun clearAllSavedOffers() {
        viewModelScope.launch {
            repository.clearAllSavedOffers()
            _userMessage.value = "Cleared all deals from Wishlist"
        }
    }

    fun scheduleReminder(offer: Offer, alertOption: String) {
        viewModelScope.launch {
            repository.addReminder(offer, alertOption)
            _reminderTargetOffer.value = null
            _userMessage.value = "Reminder set for ${offer.merchant}! Push alert configured."
        }
    }

    fun removeReminder(reminderId: String) {
        viewModelScope.launch {
            repository.removeReminder(reminderId)
            _userMessage.value = "Reminder removed."
        }
    }

    fun confirmRedemption(offer: Offer, savingsAmount: Double) {
        viewModelScope.launch {
            repository.addPurchaseRecord(offer, savingsAmount)
            _redeemTargetOffer.value = null
            val formatted = SupportedLocations.formatPrice(savingsAmount, offer.country)
            _userMessage.value = "Awesome! Saved $formatted recorded to your pattern."
        }
    }

    fun testPushNotification() {
        val offer = displayedOffers.value.firstOrNull() ?: allOffers.value.firstOrNull()
        if (offer != null) {
            val sent = repository.notificationHelper.showDealAlertNotification(
                notificationId = (1000..9999).random(),
                title = "🚨 Urgent Offer Alert: ${offer.merchant}",
                body = "Don't miss ${offer.discountHighlight}! Expires on ${offer.expiryDate}. Tap to view terms.",
                discountHighlight = offer.discountHighlight
            )
            _userMessage.value = if (sent) "Push notification dispatched to device bar!" else "Please grant notification permission."
        }
    }

    fun discoverAiDeals(query: String) {
        viewModelScope.launch {
            val country = _filterState.value.selectedCountry
            val city = _filterState.value.selectedCity
            val cat = _filterState.value.selectedCategory
            val result = repository.discoverLiveAiOffers(country, city, cat, query)
            if (result.isSuccess) {
                val count = result.getOrNull()?.size ?: 0
                _userMessage.value = "AI Scout retrieved $count verified offers for $city!"
            } else {
                _userMessage.value = "AI updated offers with latest local catalog."
            }
        }
    }

    fun refreshInsights() {
        viewModelScope.launch {
            repository.refreshAiInsights()
            _userMessage.value = "AI Insights refreshed based on your search history."
        }
    }

    fun undoDismiss() {
        _userMessage.value = "Action restored."
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun updateUserProfile(profile: UserProfile) {
        repository.updateUserProfile(profile)
        viewModelScope.launch {
            firebaseSyncRepo.saveUserProfile(profile)
        }
        _userMessage.value = "Profile preferences saved & synced."
    }

    fun signInWithGoogleIdToken(idToken: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    val updated = userProfile.value.copy(
                        name = user.displayName ?: "Google User",
                        email = user.email ?: "",
                        isLoggedIn = true
                    )
                    repository.updateUserProfile(updated)
                    firebaseSyncRepo.saveUserProfile(updated)
                    _userMessage.value = "Welcome, ${user.displayName ?: user.email}! Cloud Sync Active."
                    onResult(true, null)
                } else {
                    onResult(false, "No user returned from Google credential")
                }
            } catch (e: Exception) {
                Log.e("Auth", "Google sign in error: ${e.message}", e)
                onResult(false, e.localizedMessage ?: "Sign-in failed")
            }
        }
    }

    fun signOutGoogle() {
        firebaseSyncRepo.signOut()
        repository.updateUserProfile(userProfile.value.copy(isLoggedIn = false))
        _userMessage.value = "Signed out of Google account."
    }
}
