package com.example.millionairegameserver.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.millionairegameserver.datamodel.ChartModel
import com.example.millionairegameserver.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val dataRepository: DataRepository
): ViewModel() {
    val uiState: StateFlow<ChartUiState> = dataRepository.chartUiState

    suspend fun loadChartResult() {
        dataRepository.loadChartResult(ChartModel(0, 50, 20, 10, 10))
    }
    suspend fun showChartResult() {
        dataRepository.showChartResult()
    }
}

sealed class ChartUiState {
    data class Success(val chartModel: ChartModel): ChartUiState()
    data class Error(val e: Throwable): ChartUiState()
}