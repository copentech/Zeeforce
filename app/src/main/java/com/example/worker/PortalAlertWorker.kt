package com.example.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.util.NotificationHelper
import java.util.concurrent.TimeUnit

class PortalAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        NotificationHelper.showNotification(
            applicationContext,
            "Order Portal Update",
            "The Zayed SalesForce order portal remains active. Next system check in 1 hour."
        )
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "portal_periodic_alert_work"

        fun schedule(context: Context) {
            val alertRequest = PeriodicWorkRequestBuilder<PortalAlertWorker>(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                alertRequest
            )
        }
    }
}
