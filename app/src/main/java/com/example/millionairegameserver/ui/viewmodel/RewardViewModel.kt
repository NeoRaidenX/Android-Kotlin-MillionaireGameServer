package com.example.millionairegameserver.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.millionairegameserver.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RewardViewModel @Inject constructor(
    private val dataRepository: DataRepository
): ViewModel() {
    val uiState: StateFlow<RewardUiState> = dataRepository.rewardUiState
    suspend fun getCurrentReward() {
        dataRepository.getCurrentReward()
    }
}

sealed class RewardUiState {
    data class Success(val reward: String, val isLastReward: Boolean): RewardUiState()
    data class Loading(val position: Int): RewardUiState()
    data class Error(val e: Throwable): RewardUiState()

}