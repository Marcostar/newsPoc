package com.m10.everystory.core.notifications

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.m10.everystory.BuildConfig
import java.time.Duration

private const val WORK_NAME = "saved-reminder"

// Long enough to be "later"; debug builds use a minute so the reminder can be shown in a demo.
private val REMINDER_DELAY: Duration = if (BuildConfig.DEBUG) Duration.ofMinutes(1) else Duration.ofHours(4)

class WorkManagerSavedReminderScheduler(
    private val workManager: WorkManager,
) : SavedReminderScheduler {

    // One reminder at a time: saving again moves it later instead of stacking a second one.
    override fun schedule() {
        val request = OneTimeWorkRequestBuilder<SavedReminderWorker>()
            .setInitialDelay(REMINDER_DELAY)
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
