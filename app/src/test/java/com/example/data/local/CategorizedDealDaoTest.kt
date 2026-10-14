package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CategorizedDealDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CategorizedDealDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.categorizedDealDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryDealsByType_Travel_CreditCard_Local() = runBlocking {
        val travelDeal = CategorizedDealEntity(
            id = "t1",
            title = "FlyNas 25% Off Domestic Flights",
            merchant = "FlyNas",
            dealType = DealType.TRAVEL,
            category = "TRAVEL",
            discountPercentage = 25.0,
            discountHighlight = "25% OFF",
            description = "Book early for domestic flights",
            expiryDate = "2026-12-31"
        )

        val cardDeal = CategorizedDealEntity(
            id = "c1",
            title = "AlRajhi 20% Cashback at Supermarkets",
            merchant = "AlRajhi Bank",
            dealType = DealType.CREDIT_CARD,
            category = "CREDIT_CARD",
            discountPercentage = 20.0,
            discountHighlight = "20% Cashback",
            description = "Applicable on Visa Infinite cards",
            expiryDate = "2026-11-30"
        )

        val localDeal = CategorizedDealEntity(
            id = "l1",
            title = "Lulu Hypermarket Fresh Meat 30% OFF",
            merchant = "Lulu Hypermarket",
            dealType = DealType.LOCAL,
            category = "SUPERMARKET",
            discountPercentage = 30.0,
            discountHighlight = "30% OFF",
            description = "Weekend circular special",
            city = "Riyadh",
            expiryDate = "2026-10-25"
        )

        dao.insertDeals(listOf(travelDeal, cardDeal, localDeal))

        // Verify Travel deals query
        val travelDeals = dao.getTravelDeals().first()
        assertEquals(1, travelDeals.size)
        assertEquals("FlyNas", travelDeals[0].merchant)
        assertEquals(DealType.TRAVEL, travelDeals[0].dealType)

        // Verify Credit Card deals query
        val cardDeals = dao.getCreditCardDeals().first()
        assertEquals(1, cardDeals.size)
        assertEquals("AlRajhi Bank", cardDeals[0].merchant)
        assertEquals(DealType.CREDIT_CARD, cardDeals[0].dealType)

        // Verify Local deals query
        val localDeals = dao.getLocalDeals().first()
        assertEquals(1, localDeals.size)
        assertEquals("Lulu Hypermarket", localDeals[0].merchant)
        assertEquals(DealType.LOCAL, localDeals[0].dealType)

        // Verify getDealsByType dynamic query
        val byType = dao.getDealsByType(DealType.TRAVEL).first()
        assertEquals(1, byType.size)
        assertEquals("t1", byType[0].id)

        // Verify count query
        val count = dao.countDealsByType(DealType.LOCAL).first()
        assertEquals(1, count)

        // Verify distinct deal types
        val distinctTypes = dao.getDistinctDealTypes().first()
        assertTrue(distinctTypes.containsAll(listOf("Credit Card", "Local", "Travel")))

        // Test delete
        dao.deleteDealById("t1")
        val afterDeleteTravel = dao.getTravelDeals().first()
        assertEquals(0, afterDeleteTravel.size)
    }
}
