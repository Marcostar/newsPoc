package com.m10.everystory.feature.datausage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.domain.model.DataUsage
import com.m10.everystory.domain.model.NetworkKind
import com.m10.everystory.domain.repository.DataUsageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DataUsageUiState(
    // Mobile first, as in the prototype: that's the data people pay for.
    val network: NetworkKind = NetworkKind.Mobile,
    val usage: DataUsage? = null, // null while loading
)

class DataUsageViewModel(
    private val dataUsageRepository: DataUsageRepository,
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataUsageUiState())
    val uiState: StateFlow<DataUsageUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onNetworkSelected(network: NetworkKind) {
        if (network == _uiState.value.network) return
        _uiState.update { it.copy(network = network, usage = null) }
        load()
    }

    private fun load() {
        val network = _uiState.value.network
        // Switching tabs quickly: only the last tab's numbers should land.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val usage = dataUsageRepository.usage(network, today())
            _uiState.update { it.copy(usage = usage) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                DataUsageViewModel(app.container.dataUsageRepository)
            }
        }
    }
}
