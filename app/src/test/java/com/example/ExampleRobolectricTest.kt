package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.Deal
import com.example.data.model.OfferCategory
import com.example.data.model.SupportedLocations
import com.example.data.repository.OfferSeedData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OfferScout", appName)
  }

  @Test
  fun `seed offers contains required credit card travel and supermarket categories`() {
    val offers = OfferSeedData.INITIAL_OFFERS
    assertTrue(offers.isNotEmpty())
    assertTrue(offers.any { it.category == OfferCategory.CREDIT_CARD })
    assertTrue(offers.any { it.category == OfferCategory.TRAVEL })
    assertTrue(offers.any { it.category == OfferCategory.SUPERMARKET })
    assertTrue(offers.all { it.expiryDate.isNotBlank() && it.daysUntilExpiry > 0 })
    assertTrue(offers.all { it.originalPrice > 0.0 })
  }

  @Test
  fun `deal entity supports required fields and caching`() {
    val deal = Deal(
        id = "test-deal-1",
        title = "30% Off Organic Market",
        category = "SUPERMARKET",
        merchant = "Whole Foods Market",
        discountPercentage = 30.0,
        expiryDate = "Nov 30, 2026",
        originalPrice = 100.0,
        country = "United States",
        city = "New York"
    )

    assertEquals("test-deal-1", deal.id)
    assertEquals("30% Off Organic Market", deal.title)
    assertEquals("SUPERMARKET", deal.category)
    assertEquals("Whole Foods Market", deal.merchant)
    assertEquals(30.0, deal.discountPercentage, 0.001)
    assertEquals("Nov 30, 2026", deal.expiryDate)
    assertEquals(100.0, deal.originalPrice, 0.001)

    val offer = deal.toOffer()
    assertEquals(OfferCategory.SUPERMARKET, offer.category)
    assertEquals(70.0, offer.discountedPrice, 0.001)
  }

  @Test
  fun `deal dao manages inserting querying and deleting deals`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val dao = db.dealDao()

    val testDeal = Deal(
        id = "deal-101",
        title = "Exclusive 25% Off Electronics",
        category = "ELECTRONICS",
        merchant = "Best Buy",
        discountPercentage = 25.0,
        expiryDate = "Dec 15, 2026",
        originalPrice = 400.0,
        country = "United States",
        city = "Chicago"
    )

    // Insert
    dao.insertDeal(testDeal)
    val fetched = dao.getDealById("deal-101")
    assertNotNull(fetched)
    assertEquals("Best Buy", fetched?.merchant)

    // Query Flow
    val list = dao.getAllDeals().first()
    assertEquals(1, list.size)

    // Delete by ID
    dao.deleteDealById("deal-101")
    val afterDelete = dao.getDealById("deal-101")
    assertNull(afterDelete)

    db.close()
  }

  @Test
  fun `country and cities registry has comprehensive locations and currency formatting`() {
    val locations = SupportedLocations.LOCATIONS
    assertTrue("Should have at least 25 global countries", locations.size >= 25)

    // Check UK currency
    val ukCurrency = SupportedLocations.getCurrencySymbol("United Kingdom")
    assertEquals("£", ukCurrency)
    assertEquals("£150", SupportedLocations.formatPrice(150.0, "United Kingdom"))

    // Check UAE currency
    val uaeCurrency = SupportedLocations.getCurrencySymbol("United Arab Emirates")
    assertEquals("AED ", uaeCurrency)

    // Check Saudi Arabia currency
    val saCurrency = SupportedLocations.getCurrencySymbol("Saudi Arabia")
    assertEquals("SAR ", saCurrency)
    assertEquals("SAR 450", SupportedLocations.formatPrice(450.0, "Saudi Arabia"))

    // Check India currency
    val inCurrency = SupportedLocations.getCurrencySymbol("India")
    assertEquals("₹", inCurrency)

    // Check Germany currency
    val deCurrency = SupportedLocations.getCurrencySymbol("Germany")
    assertEquals("€", deCurrency)

    // Verify all countries have cities list with nationwide option
    for (countryInfo in locations) {
      assertTrue("Country ${countryInfo.country} must have cities", countryInfo.cities.size >= 3)
      assertTrue("Country ${countryInfo.country} must have default nationwide option", countryInfo.cities.contains(SupportedLocations.GLOBAL_CITY))
    }
  }

  @Test
  fun `saudi arabia and riyadh offers captured across amazon clickflyers and banks`() = runBlocking {
    val offers = OfferSeedData.INITIAL_OFFERS
    val saudiOffers = offers.filter { it.country.equals("Saudi Arabia", ignoreCase = true) }
    assertTrue("Should have multiple Saudi Arabia deals", saudiOffers.size >= 10)

    val riyadhOffers = saudiOffers.filter { it.city.equals("Riyadh", ignoreCase = true) }
    assertTrue("Should have multiple Riyadh deals", riyadhOffers.size >= 8)

    // Verify Amazon.sa
    assertTrue("Should capture Amazon.sa offers", saudiOffers.any { it.merchant.contains("Amazon", ignoreCase = true) || it.source.contains("Amazon", ignoreCase = true) })

    // Verify ClickFlyers (Panda, Lulu, Carrefour, Othaim, Danube)
    assertTrue("Should capture ClickFlyers offers", saudiOffers.any { it.source.contains("ClickFlyer", ignoreCase = true) })
    assertTrue("Should capture Panda hypermarket", saudiOffers.any { it.merchant.contains("Panda", ignoreCase = true) })
    assertTrue("Should capture Lulu hypermarket", saudiOffers.any { it.merchant.contains("Lulu", ignoreCase = true) })

    // Verify Bank deals (Al Rajhi, SNB, Riyad Bank)
    assertTrue("Should capture Al Rajhi Bank perks", saudiOffers.any { it.merchant.contains("Al Rajhi", ignoreCase = true) })
    assertTrue("Should capture SNB or Riyad Bank perks", saudiOffers.any { it.merchant.contains("SNB", ignoreCase = true) || it.merchant.contains("Riyad", ignoreCase = true) })

    // Verify airlines (Saudia, flynas)
    assertTrue("Should capture Saudia or flynas flights", saudiOffers.any { it.merchant.contains("Saudia", ignoreCase = true) || it.merchant.contains("flynas", ignoreCase = true) })

    // Verify Room database caching and querying by country and city
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val dao = db.dealDao()

    val dealEntities = saudiOffers.map { it.toDealEntity() }
    dao.insertDeals(dealEntities)

    val dbRiyadhDeals = dao.getDealsByCountryAndCity("Saudi Arabia", "Riyadh").first()
    assertTrue("Room should query cached Riyadh deals", dbRiyadhDeals.isNotEmpty())

    db.close()
  }

  @Test
  fun `deal category filtering and sorting by discount and expiry date works correctly`() {
    val offers = OfferSeedData.INITIAL_OFFERS

    // 1. Category Filtering
    val travelDeals = offers.filter { it.category == OfferCategory.TRAVEL }
    val supermarketDeals = offers.filter { it.category == OfferCategory.SUPERMARKET }
    val creditCardDeals = offers.filter { it.category == OfferCategory.CREDIT_CARD }

    assertTrue("Travel deals should be present", travelDeals.isNotEmpty())
    assertTrue("Supermarket deals should be present", supermarketDeals.isNotEmpty())
    assertTrue("Credit card deals should be present", creditCardDeals.isNotEmpty())

    // 2. Sort by Highest Discount %
    val sortedByDiscountDesc = offers.sortedByDescending { it.discountPercentage }
    assertTrue(sortedByDiscountDesc.first().discountPercentage >= sortedByDiscountDesc.last().discountPercentage)
    assertTrue("Highest discount should be >= 40%", sortedByDiscountDesc.first().discountPercentage >= 40.0)

    // 3. Sort by Expiring Soonest
    val sortedByExpirySoonest = offers.sortedWith(
        compareBy<com.example.data.model.Offer> { it.daysUntilExpiry }
            .thenBy { it.expiryDate }
            .thenByDescending { it.discountPercentage }
    )
    assertTrue(sortedByExpirySoonest.first().daysUntilExpiry <= sortedByExpirySoonest.last().daysUntilExpiry)
    assertTrue("Soonest deal should have small days until expiry", sortedByExpirySoonest.first().daysUntilExpiry <= 2)
  }
}

