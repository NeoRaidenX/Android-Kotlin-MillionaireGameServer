package com.example.millionairegameserver.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.millionairegameserver.repository.DataRepository
import com.example.millionairegameserver.datamodel.QuestionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val dataRepository: DataRepository
): ViewModel() {

    val uiState: StateFlow<CurrentQuestionUiState> = dataRepository.mainUiState

    suspend fun getCurrentQuestion() {
        dataRepository.getCurrentQuestion()
    }
}

sealed class CurrentQuestionUiState {
    data class Success(val currentQuestion: QuestionModel): CurrentQuestionUiState()
    data class Error(val e: Throwable): CurrentQuestionUiState()
    data class ShowAnswer(val position: Int): CurrentQuestionUiState()
    data class MarkAnswer(val position: Int): CurrentQuestionUiState()
    data class CorrectAnswer(val position: Int): CurrentQuestionUiState()
}