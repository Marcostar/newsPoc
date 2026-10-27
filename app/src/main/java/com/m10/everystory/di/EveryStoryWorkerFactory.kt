package com.m10.everystory.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.m10.everystory.core.notifications.SavedReminderNotifier
import com.m10.everystory.core.notifications.SavedReminderWorker
import com.m10.everystory.domain.repository.SavedArticlesRepository
import com.m10.everystory.domain.repository.UserPreferencesRepository

// WorkManager's way of constructor injection: it asks this factory for each worker by class name.
class EveryStoryWorkerFactory(
    private val savedArticlesRepository: SavedArticlesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedReminderNotifier: SavedReminderNotifier,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        SavedReminderWorker::class.java.name -> SavedReminderWorker(
            context = appContext,
            params = workerParameters,
            savedArticlesRepository = savedArticlesRepository,
            userPreferencesRepository = userPreferencesRepository,
            notifier = savedReminderNotifier,
        )
        // Null lets WorkManager fall back to building the worker itself.
        else -> null
    }
}
