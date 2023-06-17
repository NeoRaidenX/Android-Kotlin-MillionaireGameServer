package com.example.millionairegameserver.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.millionairegameserver.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    private val dataRepository: DataRepository
): ViewModel() {
    suspend fun startClock() {
        dataRepository.startClock()
    }

}