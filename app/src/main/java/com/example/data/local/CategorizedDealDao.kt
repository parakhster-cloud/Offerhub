package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for categorizing, querying, and managing deals by organizational types
 * like 'Travel', 'Credit Card', and 'Local'.
 */
@Dao
interface CategorizedDealDao {

    // Reactive queries by DealType
    @Query("SELECT * FROM categorized_deals ORDER BY daysUntilExpiry ASC")
    fun getAllCategorizedDeals(): Flow<List<CategorizedDealEntity>>

    @Query("SELECT * FROM categorized_deals WHERE dealType = :dealType ORDER BY daysUntilExpiry ASC")
    fun getDealsByType(dealType: String): Flow<List<CategorizedDealEntity>>

    @Query("SELECT * FROM categorized_deals WHERE dealType = 'Travel' ORDER BY daysUntilExpiry ASC")
    fun getTravelDeals(): Flow<List<CategorizedDealEntity>>

    @Query("SELECT * FROM categorized_deals WHERE dealType = 'Credit Card' ORDER BY daysUntilExpiry ASC")
    fun getCreditCardDeals(): Flow<List<CategorizedDealEntity>>

    @Query("SELECT * FROM categorized_deals WHERE dealType = 'Local' ORDER BY daysUntilExpiry ASC")
    fun getLocalDeals(): Flow<List<CategorizedDealEntity>>

    // Filtered by Country and City
    @Query("SELECT * FROM categorized_deals WHERE dealType = :dealType AND country = :country ORDER BY daysUntilExpiry ASC")
    fun getDealsByTypeAndCountry(dealType: String, country: String): Flow<List<CategorizedDealEntity>>

    @Query("SELECT * FROM categorized_deals WHERE dealType = :dealType AND (city = :city OR city = 'All Cities / Nationwide') ORDER BY daysUntilExpiry ASC")
    fun getDealsByTypeAndCity(dealType: String, city: String): Flow<List<CategorizedDealEntity>>

    // Aggregates & Distinct Types
    @Query("SELECT COUNT(*) FROM categorized_deals WHERE dealType = :dealType")
    fun countDealsByType(dealType: String): Flow<Int>

    @Query("SELECT DISTINCT dealType FROM categorized_deals ORDER BY dealType ASC")
    fun getDistinctDealTypes(): Flow<List<String>>

    // Single item query
    @Query("SELECT * FROM categorized_deals WHERE id = :id")
    suspend fun getDealById(id: String): CategorizedDealEntity?

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: CategorizedDealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeals(deals: List<CategorizedDealEntity>)

    // Update & Delete operations
    @Update
    suspend fun updateDeal(deal: CategorizedDealEntity)

    @Delete
    suspend fun deleteDeal(deal: CategorizedDealEntity)

    @Query("DELETE FROM categorized_deals WHERE id = :id")
    suspend fun deleteDealById(id: String)

    @Query("DELETE FROM categorized_deals WHERE dealType = :dealType")
    suspend fun clearDealsByType(dealType: String)

    @Query("DELETE FROM categorized_deals")
    suspend fun clearAll()
}
