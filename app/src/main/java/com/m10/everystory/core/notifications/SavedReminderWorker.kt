package com.m10.everystory.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.m10.everystory.domain.repository.SavedArticlesRepository
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Runs a while after a story is saved. Only reminds about stories saved since the reader last
 * looked at Saved, so anyone who already went back to them hears nothing.
 * Built by EveryStoryWorkerFactory, which hands it its dependencies.
 */
class SavedReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val savedArticlesRepository: SavedArticlesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notifier: SavedReminderNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val lastViewed = userPreferencesRepository.savedLastViewedAt.first()
        val waiting = savedArticlesRepository.countSavedSince(lastViewed)
        if (waiting > 0) notifier.show(waiting)
        return Result.success()
    }
}
