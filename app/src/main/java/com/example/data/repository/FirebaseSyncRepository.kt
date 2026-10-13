package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.DealReminder
import com.example.data.model.Offer
import com.example.data.model.OfferCategory
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = auth.currentUser?.uid ?: return
        val data = mapOf(
            "userId" to uid,
            "email" to (auth.currentUser?.email ?: profile.email),
            "displayName" to (auth.currentUser?.displayName ?: profile.name),
            "currency" to "USD",
            "country" to profile.homeCountry,
            "city" to profile.homeCity,
            "notificationsEnabled" to profile.notificationsEnabled,
            "savedCards" to profile.myCards
        )
        db.collection("users").document(uid).set(data).await()
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        return db.collection("users").document(userId)
            .snapshots()
            .map { doc ->
                if (!doc.exists()) null
                else {
                    UserProfile(
                        name = doc.getString("displayName") ?: "Shopper",
                        email = doc.getString("email") ?: "",
                        isLoggedIn = true,
                        homeCountry = doc.getString("country") ?: "United States",
                        homeCity = doc.getString("city") ?: "New York",
                        myCards = (doc.get("savedCards") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true
                    )
                }
            }
    }

    suspend fun saveDealToCloud(userId: String, offer: Offer) {
        val data = mapOf(
            "userId" to userId,
            "dealId" to offer.id,
            "merchant" to offer.merchant,
            "title" to offer.title,
            "discountHighlight" to offer.discountHighlight,
            "category" to offer.category.name,
            "city" to offer.city,
            "country" to offer.country,
            "linkUrl" to offer.linkUrl,
            "promoCode" to (offer.promoCode ?: ""),
            "expiryDate" to offer.expiryDate
        )
        db.collection("users").document(userId)
            .collection("saved_deals").document(offer.id)
            .set(data).await()
    }

    suspend fun removeDealFromCloud(userId: String, dealId: String) {
        db.collection("users").document(userId)
            .collection("saved_deals").document(dealId)
            .delete().await()
    }

    fun observeCloudSavedDeals(userId: String): Flow<List<Offer>> {
        return db.collection("users").document(userId)
            .collection("saved_deals")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("dealId") ?: return@mapNotNull null
                    val merchant = doc.getString("merchant") ?: return@mapNotNull null
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val discountHighlight = doc.getString("discountHighlight") ?: ""
                    val categoryStr = doc.getString("category") ?: OfferCategory.SUPERMARKET.name
                    val category = try {
                        OfferCategory.valueOf(categoryStr)
                    } catch (e: Exception) {
                        OfferCategory.SUPERMARKET
                    }
                    val city = doc.getString("city") ?: "Global"
                    val country = doc.getString("country") ?: "Global"
                    val linkUrl = doc.getString("linkUrl") ?: "https://example.com"
                    val promoCode = doc.getString("promoCode")?.takeIf { it.isNotBlank() }
                    val expiryDate = doc.getString("expiryDate") ?: "2026-12-31"

                    Offer(
                        id = id,
                        title = title,
                        merchant = merchant,
                        source = "Cloud Sync",
                        category = category,
                        country = country,
                        city = city,
                        discountPercentage = 20.0,
                        originalPrice = 100.0,
                        discountHighlight = discountHighlight,
                        description = "Saved deal synced from Firebase Cloud",
                        promoCode = promoCode,
                        linkUrl = linkUrl,
                        expiryDate = expiryDate,
                        daysUntilExpiry = 10,
                        terms = "Terms apply as per merchant."
                    )
                }
            }
    }

    suspend fun saveReminderToCloud(userId: String, reminder: DealReminder) {
        val data = mapOf(
            "userId" to userId,
            "reminderId" to reminder.id,
            "dealId" to reminder.offerId,
            "dealTitle" to reminder.offerTitle,
            "merchant" to reminder.merchant,
            "notifyBeforeDays" to reminder.daysUntilExpiry,
            "expiryDate" to reminder.expiryDate,
            "isDismissed" to reminder.isTriggered
        )
        db.collection("users").document(userId)
            .collection("reminders").document(reminder.id)
            .set(data).await()
    }

    suspend fun removeReminderFromCloud(userId: String, reminderId: String) {
        db.collection("users").document(userId)
            .collection("reminders").document(reminderId)
            .delete().await()
    }

    fun observeCloudReminders(userId: String): Flow<List<DealReminder>> {
        return db.collection("users").document(userId)
            .collection("reminders")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val reminderId = doc.getString("reminderId") ?: return@mapNotNull null
                    val dealId = doc.getString("dealId") ?: return@mapNotNull null
                    val dealTitle = doc.getString("dealTitle") ?: ""
                    val merchant = doc.getString("merchant") ?: ""
                    val notifyBeforeDays = (doc.getLong("notifyBeforeDays") ?: 2L).toInt()
                    val expiryDate = doc.getString("expiryDate") ?: ""
                    val isDismissed = doc.getBoolean("isDismissed") ?: false

                    DealReminder(
                        id = reminderId,
                        offerId = dealId,
                        offerTitle = dealTitle,
                        merchant = merchant,
                        expiryDate = expiryDate,
                        daysUntilExpiry = notifyBeforeDays,
                        scheduledTimeDesc = "Notify $notifyBeforeDays days before expiry",
                        isTriggered = isDismissed
                    )
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }
}
