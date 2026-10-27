package com.m10.everystory.domain.repository

import com.m10.everystory.domain.model.DataUsage
import com.m10.everystory.domain.model.NetworkKind
import java.time.LocalDate

interface DataUsageRepository {
    // This calendar month so far, plus the seven days ending today.
    suspend fun usage(network: NetworkKind, today: LocalDate): DataUsage
}
