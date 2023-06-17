package com.example.millionairegameserver.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.millionairegameserver.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.FieldPosition
import javax.inject.Inject

@HiltViewModel
class TableViewModel @Inject constructor(
    private val dataRepository: DataRepository
): ViewModel() {

}

sealed class CurrentRewardUiState {
    data class Success(val position: Int): CurrentRewardUiState()
    data class Error(val e: Throwable): CurrentRewardUiState()
    data class Navigate(val action: Int): CurrentRewardUiState()
}