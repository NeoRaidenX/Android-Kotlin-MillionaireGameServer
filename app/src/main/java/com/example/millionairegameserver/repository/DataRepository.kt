package com.example.millionairegameserver.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.millionairegameserver.Actions
import com.example.millionairegameserver.App
import com.example.millionairegameserver.LifelinesEnum
import com.example.millionairegameserver.RewardTableEnum
import com.example.millionairegameserver.database.AppDatabase
import com.example.millionairegameserver.datamodel.ChartModel
import com.example.millionairegameserver.datamodel.QuestionModel
import com.example.millionairegameserver.ui.viewmodel.ChartUiState
import com.example.millionairegameserver.ui.viewmodel.CurrentQuestionUiState
import com.example.millionairegameserver.ui.viewmodel.CurrentRewardUiState
import com.example.millionairegameserver.ui.viewmodel.RewardUiState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class DataRepository(private val context: Context): Repository {

    companion object {
        private const val TAG = "DataRepository"
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val _mainUiState = MutableStateFlow<CurrentQuestionUiState>(
        CurrentQuestionUiState.Success(
        QuestionModel(0, "", "", "", "", "", 0, 0, false)
    ))

    val mainUiState: StateFlow<CurrentQuestionUiState> = _mainUiState

    private val _rewardUiState = MutableStateFlow<RewardUiState>(RewardUiState.Success("0"))
    val rewardUiState: StateFlow<RewardUiState> = _rewardUiState

    private val _tableUiState = MutableStateFlow<CurrentRewardUiState>(CurrentRewardUiState.Success(0))
    val tableUiState: StateFlow<CurrentRewardUiState> = _tableUiState

    private val _chartUiState = MutableStateFlow<ChartUiState>(
        ChartUiState.Success(
            ChartModel(0, 50,50,50,50)
        )
    )
    val chartUiState: StateFlow<ChartUiState> = _chartUiState


    override fun loadQuestionsToDatabase() {
        saveQuestionsFromFile()
    }

    override fun showQuestion() {


    }

    override fun showAnswer(position: Int) {
        _mainUiState.value = CurrentQuestionUiState.ShowAnswer(position)
    }

    override fun markAnswer(position: Int) {
        _mainUiState.value = CurrentQuestionUiState.MarkAnswer(position)
    }

    override fun showCorrectAnswer(position: Int) {
        _mainUiState.value = CurrentQuestionUiState.CorrectAnswer(position)
    }

    override fun nextQuestion() {
    }

    override fun updateLastAnswered(questionNumber: Int) {
        val dao = AppDatabase.getDatabase().questionDao()
        val questionModel = dao.loadQuestionById(questionNumber)
        val updated = questionModel.copy(isAnswered = true)
        dao.updateQuestionAsAnswered(updated)
    }

    override fun toggleLifeline(lifeline: LifelinesEnum) {
    }

    override suspend fun getCurrentQuestion()  {
        Log.d(TAG, "getCurrentQuestion: ")
        val dao = AppDatabase.getDatabase().questionDao()
        scope.launch {
            //_mainUiState.value = CurrentQuestionUiState.Success(dao.getCurrentQuestion())
            /*delay(2000)
            showAnswer(0)
            delay(1000)
            showAnswer(1)
            delay(1000)
            showAnswer(2)
            delay(1000)
            showAnswer(3)
            delay(2000)
            markAnswer(0)
            delay(2000)
            showCorrectAnswer(0)*/
            /*delay(2000)
            updateLastAnswered(0)
            updateLastAnswered(1)
            updateLastAnswered(2)
            updateLastAnswered(3)
            updateLastAnswered(4)
            delay(2000)
            _mainUiState.value = CurrentQuestionUiState.Success(dao.getCurrentQuestion())
            delay(2000)
            navigateReward()
            delay(2000)
            navigateUp()
            delay(2000)*/
            //navigateTable()
            //navigateChart()
            //navigateClock()
        }
    }

    override fun selectQuestion(questionNumber: Int) {
    }

    override fun showFifty() {
    }

    override fun loadChartResult(chartModel: ChartModel) {
        val dao = AppDatabase.getDatabase().chartDao()
        scope.launch {
            dao.insertAll(chartModel)
        }
        Toast.makeText(context, "Resultados cargados", Toast.LENGTH_SHORT).show()
    }

    override fun showChartResult() {
        val dao = AppDatabase.getDatabase().chartDao()
        scope.launch {
            val chartResult = dao.loadQuestionById(0)
            _chartUiState.value = ChartUiState.Success(chartResult)
        }
    }

    override fun navigateChart() {
        Log.d(TAG, "navigateChart: ")
        broadcastUpdate(Actions.NAVIGATE_CHART)
    }

    override fun navigateClock() {
        Log.d(TAG, "navigateClock: ")
        broadcastUpdate(Actions.NAVIGATE_CLOCK)
    }

    override fun navigateUp() {
        Log.d(TAG, "navigateUp: ")
        broadcastUpdate(Actions.NAVIGATE_UP)
    }

    override fun navigateTable() {
        Log.d(TAG, "navigateTable: ")
        broadcastUpdate(Actions.NAVIGATE_TABLE)
    }

    override fun navigateReward() {
        Log.d(TAG, "navigateReward: ")
        broadcastUpdate(Actions.NAVIGATE_REWARD)
    }

    override fun startClock() {
        Log.d(TAG, "startClock: ")
        broadcastUpdate(Actions.PLAY_CLOCK)
    }

    override fun getCurrentReward() {
        val dao = AppDatabase.getDatabase().questionDao()
        scope.launch {
            var answeredCount = dao.getAnsweredCount() - 1
            answeredCount = if (answeredCount == -1) 0 else answeredCount
            _rewardUiState.value = RewardUiState.Success(RewardTableEnum.values()[answeredCount].title)
        }
    }

    private fun getJsonDataFromAsset(): String? {
        val jsonString: String
        try {
            jsonString = App.applicationContext().assets.open("questions.json").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun saveQuestionsFromFile() {
        val jsonString = getJsonDataFromAsset()
        val gson = Gson()
        val questionType = object : TypeToken<List<QuestionModel>>() {}.type

        val questions: List<QuestionModel> = gson.fromJson(jsonString, questionType)
        val dao = AppDatabase.getDatabase().questionDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertAll(questions)
        }
        Toast.makeText(context, "Se han guardado todas las preguntas", Toast.LENGTH_SHORT).show()


    }

    suspend fun getCurrentTableReward() {
        val dao = AppDatabase.getDatabase().questionDao()
        scope.launch {
            val answeredCount = dao.getAnsweredCount()
            _tableUiState.value = CurrentRewardUiState.Success(answeredCount)
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        context.sendBroadcast(intent)
    }


}