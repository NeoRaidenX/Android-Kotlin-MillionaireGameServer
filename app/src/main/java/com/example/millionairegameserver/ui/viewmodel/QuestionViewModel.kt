package com.example.millionairegameserver.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.millionairegameserver.datamodel.LifelineModel
import com.example.millionairegameserver.repository.DataRepository
import com.example.millionairegameserver.datamodel.QuestionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val dataRepository: DataRepository
): ViewModel() {

    val uiState: StateFlow<CurrentQuestionUiState> = dataRepository.mainUiState

    val lifelinesState: StateFlow<LifelinesUiState> = dataRepository.lifelinesUiState

    fun sendResetUi() {
        dataRepository.resetQuestionUi()
    }

    suspend fun getCurrentQuestion() {
        dataRepository.getCurrentQuestion()
    }

    fun changeNextQuestion() {
        dataRepository.nextQuestion()
    }

    fun sendLoadCurrentQuestion() {
        dataRepository.loadQuestion()
    }

}

sealed class LifelinesUiState {
    data class Success(val lifeline: LifelineModel): LifelinesUiState()
    data class Error(val e: Throwable): LifelinesUiState()
}

sealed class CurrentQuestionUiState {
    data class Success(val currentQuestion: QuestionModel): CurrentQuestionUiState()
    data class Error(val e: Throwable): CurrentQuestionUiState()
    data class ShowQuestion(val position: Int): CurrentQuestionUiState()
    data class ShowOption(val position: Int): CurrentQuestionUiState()
    data class MarkAnswer(val position: Int): CurrentQuestionUiState()

    data class ShowFifty(val position: Int): CurrentQuestionUiState()

    data class ResetQuestionUi(val position: Int): CurrentQuestionUiState()
    data class CorrectAnswer(val position: Int): CurrentQuestionUiState()
}