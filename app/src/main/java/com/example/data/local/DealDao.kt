package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for the 'Deal' entity.
 * Manages database operations including inserting, querying, updating, and deleting offers.
 */
@Dao
interface DealDao {

    // Query operations
    @Query("SELECT * FROM deals ORDER BY daysUntilExpiry ASC")
    fun getAllDeals(): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE id = :id")
    suspend fun getDealById(id: String): Deal?

    @Query("SELECT * FROM deals WHERE country = :country ORDER BY daysUntilExpiry ASC")
    fun getDealsByCountry(country: String): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE country = :country AND (city = :city OR city = 'All Cities / Nationwide') ORDER BY daysUntilExpiry ASC")
    fun getDealsByCountryAndCity(country: String, city: String): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE category = :category ORDER BY daysUntilExpiry ASC")
    fun getDealsByCategory(category: String): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE title LIKE '%' || :query || '%' OR merchant LIKE '%' || :query || '%'")
    fun searchDeals(query: String): Flow<List<Deal>>

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: Deal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeals(deals: List<Deal>)

    // Update operations
    @Update
    suspend fun updateDeal(deal: Deal)

    // Delete operations
    @Delete
    suspend fun deleteDeal(deal: Deal)

    @Query("DELETE FROM deals WHERE id = :id")
    suspend fun deleteDealById(id: String)

    @Query("DELETE FROM deals")
    suspend fun clearAllDeals()
}
