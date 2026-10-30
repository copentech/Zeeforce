package com.example

import android.app.Application
import com.example.util.NotificationHelper
import com.example.worker.AutoSyncManager
import com.example.worker.SupabaseSyncWorker

class SalesForceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        AutoSyncManager.initialize(this)
        SupabaseSyncWorker.schedulePeriodic(this)
    }
}
