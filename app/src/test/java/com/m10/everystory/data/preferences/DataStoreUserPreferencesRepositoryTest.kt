package com.m10.everystory.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.m10.everystory.domain.model.NewsLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreUserPreferencesRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scope = TestScope(UnconfinedTestDispatcher())

    // A fresh file per repository, so each test gets its own DataStore.
    private fun repository(englishOnly: Boolean) = DataStoreUserPreferencesRepository(
        dataStore = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            folder.newFile("prefs.preferences_pb").also { it.delete() }
        },
        englishOnly = englishOnly,
    )

    @Test
    fun `news language is saved`() = scope.runTest {
        val repository = repository(englishOnly = false)

        repository.setNewsLanguage(NewsLanguage.German)

        assertEquals(NewsLanguage.German, repository.newsLanguage.first())
    }

    @Test
    fun `english only ignores a language change`() = scope.runTest {
        val repository = repository(englishOnly = true)

        repository.setNewsLanguage(NewsLanguage.Arabic)

        assertEquals(NewsLanguage.English, repository.newsLanguage.first())
    }
}
