package com.example.millionairegameserver.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.millionairegameserver.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataRepository: DataRepository
): ViewModel() {
    fun loadQuestionsToDatabase() {
        dataRepository.loadQuestionsToDatabase()
        viewModelScope.launch {
        }
    }
}