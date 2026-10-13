package com.example.data.repository

import com.example.data.model.DealReminder
import com.example.data.model.Offer
import com.example.data.model.OfferCategory
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testUserProfileSaveAndRetrieve() = runBlocking {
        val uid = signInTestUser("alice@test.com")
        val repo = FirebaseSyncRepository(firestore, auth)

        val profile = UserProfile(
            name = "Alice Test",
            email = "alice@test.com",
            isLoggedIn = true,
            homeCountry = "Saudi Arabia",
            homeCity = "Riyadh",
            myCards = listOf("AlRajhi Visa Infinite"),
            notificationsEnabled = true
        )

        repo.saveUserProfile(profile)

        val retrieved = repo.observeUserProfile(uid).first()
        assertNotNull(retrieved)
        assertEquals("Alice Test", retrieved?.name)
        assertEquals("Riyadh", retrieved?.homeCity)
    }

    @Test
    fun testSaveDealAndObserveCloudDeals() = runBlocking {
        val uid = signInTestUser("deal_user@test.com")
        val repo = FirebaseSyncRepository(firestore, auth)

        val offer = Offer(
            id = "test_deal_1",
            title = "Weekly Circular 30% OFF",
            merchant = "Panda Supermarket",
            source = "ClickFlyers",
            category = OfferCategory.SUPERMARKET,
            country = "Saudi Arabia",
            city = "Jeddah",
            discountPercentage = 30.0,
            originalPrice = 100.0,
            discountHighlight = "30% OFF",
            description = "Save on fresh produce and electronics",
            linkUrl = "https://panda.com.sa",
            expiryDate = "2026-12-31",
            daysUntilExpiry = 15,
            terms = "Terms apply"
        )

        repo.saveDealToCloud(uid, offer)

        val deals = repo.observeCloudSavedDeals(uid).first()
        assertTrue(deals.any { it.id == "test_deal_1" && it.merchant == "Panda Supermarket" })

        // Remove deal
        repo.removeDealFromCloud(uid, "test_deal_1")
        val afterDelete = repo.observeCloudSavedDeals(uid).first()
        assertTrue(afterDelete.none { it.id == "test_deal_1" })
    }

    @Test
    fun testSaveReminderAndObserveCloudReminders() = runBlocking {
        val uid = signInTestUser("reminder_user@test.com")
        val repo = FirebaseSyncRepository(firestore, auth)

        val reminder = DealReminder(
            id = "rem_test_1",
            offerId = "deal_123",
            offerTitle = "Emirates 15% OFF",
            merchant = "Emirates Airlines",
            expiryDate = "2026-11-20",
            daysUntilExpiry = 3,
            scheduledTimeDesc = "Notify 3 days before",
            isTriggered = false
        )

        repo.saveReminderToCloud(uid, reminder)

        val reminders = repo.observeCloudReminders(uid).first()
        assertTrue(reminders.any { it.id == "rem_test_1" && it.merchant == "Emirates Airlines" })
    }
}
