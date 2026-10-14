package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.repository.OfferRepository
import com.example.notification.DealNotificationHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Background CoroutineWorker that runs daily to scrape and synchronize latest promotions,
 * circular flyers, and bank perks across the user's selected region, then delivers a push digest.
 */
class DailyDealSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val repository = OfferRepository(context)
        val notificationHelper = DealNotificationHelper(context)

        return try {
            val userProfile = repository.userProfile.value
            val country = userProfile.homeCountry.ifBlank { "United States" }
            val city = userProfile.homeCity.ifBlank { "All Cities / Nationwide" }

            Log.d(TAG, "Starting automated deal sync and web scraping for $city, $country...")

            // Run aggregated scraping & sync across Amazon, ClickFlyers, and Bank discount portals
            val syncResult = repository.syncAggregatedDealsFromSources(country, city)
            val syncedCount = syncResult.getOrNull() ?: 0

            // Query top deals from local cache to generate digest
            val allOffers = repository.allOffers.first()
            val topOffers = allOffers
                .filter { it.discountPercentage >= 20.0 }
                .sortedByDescending { it.discountPercentage }

            // Trigger daily digest push notification if new deals were found
            if (topOffers.isNotEmpty()) {
                notificationHelper.showDailyDigestNotification(
                    city = city,
                    country = country,
                    newDealsCount = syncedCount.coerceAtLeast(topOffers.size),
                    topOffers = topOffers
                )
            }

            // Check for urgent deals expiring in <= 48 hours to notify
            val urgentExpiring = allOffers.filter { it.daysUntilExpiry in 1..2 }
            urgentExpiring.firstOrNull()?.let { urgentDeal ->
                notificationHelper.showDealAlertNotification(
                    notificationId = (2000..8999).random(),
                    title = "⏳ Urgent: Deal Expiring Soon at ${urgentDeal.merchant}",
                    body = "${urgentDeal.discountHighlight} on ${urgentDeal.title} ends in ${urgentDeal.daysUntilExpiry} days!",
                    discountHighlight = urgentDeal.discountHighlight
                )
            }

            Log.d(TAG, "Automated deal sync finished successfully. Synced $syncedCount deals.")
            Result.success(workDataOf("synced_count" to syncedCount))
        } catch (e: Exception) {
            Log.e(TAG, "Error running DailyDealSyncWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DailyDealSyncWorker"
        const val WORK_NAME_PERIODIC = "OfferScout_DailyDealSync"
        const val WORK_NAME_ONE_TIME = "OfferScout_ImmediateDealSync"

        /**
         * Schedules periodic daily background scraping with network connectivity constraints.
         */
        fun scheduleDaily(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<DailyDealSyncWorker>(
                24, TimeUnit.HOURS,
                6, TimeUnit.HOURS // flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        /**
         * Triggers an immediate one-time deal scan and push notification dispatch.
         */
        fun triggerImmediate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<DailyDealSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        }
    }
}
