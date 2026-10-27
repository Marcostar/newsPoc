package com.m10.everystory.domain.usecase

import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.domain.repository.ApiKeyRepository
import com.m10.everystory.domain.repository.NewsRepository

// NewsAPI keys are 32 hex characters. Checking the shape first saves a request on a typo.
private val NEWS_API_KEY_PATTERN = Regex("^[a-fA-F0-9]{32}$")

// Only a key NewsAPI has just accepted gets saved, so the app never switches to a broken one.
class SaveOwnApiKeyUseCase(
    private val newsRepository: NewsRepository,
    private val apiKeyRepository: ApiKeyRepository,
) {
    sealed interface Result {
        data object Saved : Result
        data object Empty : Result
        data object WrongFormat : Result
        data object Rejected : Result
        data class CouldNotCheck(val error: AppError) : Result
    }

    suspend operator fun invoke(input: String): Result {
        // Pasted keys often come with a stray space or line break.
        val key = input.trim()
        if (key.isEmpty()) return Result.Empty
        if (!NEWS_API_KEY_PATTERN.matches(key)) return Result.WrongFormat
        return when (val check = newsRepository.checkApiKey(key)) {
            is AppResult.Success -> {
                apiKeyRepository.saveOwnKey(key)
                Result.Saved
            }
            is AppResult.Failure ->
                if (check.error == AppError.Unauthorized) Result.Rejected else Result.CouldNotCheck(check.error)
        }
    }
}
