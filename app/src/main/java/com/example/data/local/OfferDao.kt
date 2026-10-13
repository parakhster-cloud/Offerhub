package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for user-specific activity, bookmarks,
 * search patterns, and reminders.
 */
@Dao
interface OfferDao {

    // Saved Offers / Bookmarks
    @Query("SELECT offerId FROM saved_offers")
    fun getSavedOfferIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOffer(entity: SavedOfferEntity)

    @Query("DELETE FROM saved_offers WHERE offerId = :offerId")
    suspend fun removeSavedOffer(offerId: String)

    @Query("SELECT d.* FROM deals d INNER JOIN saved_offers s ON d.id = s.offerId ORDER BY s.savedAtMillis DESC")
    fun getSavedDeals(): Flow<List<Deal>>

    @Query("DELETE FROM saved_offers")
    suspend fun clearAllSavedOffers()

    // Dismissed Offers
    @Query("SELECT offerId FROM dismissed_offers")
    fun getDismissedOfferIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dismissOffer(entity: DismissedOfferEntity)

    @Query("DELETE FROM dismissed_offers WHERE offerId = :offerId")
    suspend fun undoDismissOffer(offerId: String)

    @Query("DELETE FROM dismissed_offers")
    suspend fun clearAllDismissedOffers()

    // Search History / Patterns
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getSearchHistory(): Flow<List<SearchLogEntity>>

    @Insert
    suspend fun recordSearch(entity: SearchLogEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    // Purchase / Redeemed Patterns
    @Query("SELECT * FROM purchase_history ORDER BY timestamp DESC")
    fun getPurchaseHistory(): Flow<List<PurchaseRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordPurchase(entity: PurchaseRecordEntity)

    // Reminders
    @Query("SELECT * FROM deal_reminders ORDER BY createdAtMillis DESC")
    fun getReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReminder(entity: ReminderEntity)

    @Query("DELETE FROM deal_reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)

    @Query("DELETE FROM deal_reminders WHERE offerId = :offerId")
    suspend fun deleteReminderByOfferId(offerId: String)
}
