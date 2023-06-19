package com.example.millionairegameserver.bluetooth

import androidx.lifecycle.ViewModel
import com.example.millionairegameserver.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val dataRepository: DataRepository
): ViewModel() {

    val uiState: StateFlow<BluetoothUiState> = dataRepository.btUiState

    fun startListening() {
        dataRepository.startBluetoothServer()
    }
}

sealed class BluetoothUiState {
    data class Success(val position: Int): BluetoothUiState()
    data class Connecting(val status: String): BluetoothUiState()
    data class Error(val e: Throwable): BluetoothUiState()
}