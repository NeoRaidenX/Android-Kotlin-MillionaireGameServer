package com.example.millionairegameserver.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.example.millionairegameserver.Actions
import com.example.millionairegameserver.App
import com.example.millionairegameserver.LifelinesEnum
import com.example.millionairegameserver.RewardTableEnum
import com.example.millionairegameserver.bluetooth.BluetoothChatService
import com.example.millionairegameserver.bluetooth.BluetoothUiState
import com.example.millionairegameserver.bluetooth.Constants
import com.example.millionairegameserver.database.AppDatabase
import com.example.millionairegameserver.datamodel.ChartModel
import com.example.millionairegameserver.datamodel.LifelineModel
import com.example.millionairegameserver.datamodel.QuestionModel
import com.example.millionairegameserver.ui.viewmodel.ChartUiState
import com.example.millionairegameserver.ui.viewmodel.CurrentQuestionUiState
import com.example.millionairegameserver.ui.viewmodel.CurrentRewardUiState
import com.example.millionairegameserver.ui.viewmodel.LifelinesUiState
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

    private val _btUiState = MutableStateFlow<BluetoothUiState>(BluetoothUiState.Connecting(""))
    val btUiState: StateFlow<BluetoothUiState> = _btUiState

    /*private val _mainUiState = MutableStateFlow<CurrentQuestionUiState>(
        CurrentQuestionUiState.Success(
        QuestionModel(0, "", "", "", "", "", 0, 0, false, true, true, true, true)
    ))*/
    private val _mainUiState = MutableStateFlow<CurrentQuestionUiState>(CurrentQuestionUiState.ResetQuestionUi(-1))
    val mainUiState: StateFlow<CurrentQuestionUiState> = _mainUiState

    private val _rewardUiState = MutableStateFlow<RewardUiState>(RewardUiState.Success("0", false))
    val rewardUiState: StateFlow<RewardUiState> = _rewardUiState

    private val _tableUiState = MutableStateFlow<CurrentRewardUiState>(CurrentRewardUiState.Loading(0))
    val tableUiState: StateFlow<CurrentRewardUiState> = _tableUiState

    private val _chartUiState = MutableStateFlow<ChartUiState>(
        ChartUiState.Success(
            ChartModel(0, 50,50,50,50)
        )
    )
    val chartUiState: StateFlow<ChartUiState> = _chartUiState

    private val _lifelinesUiState = MutableStateFlow<LifelinesUiState>(LifelinesUiState.Success(
        LifelineModel(
            uid = 0,
            lifelinePhone = false,
            lifelineFifty = false,
            lifelineGroup = false,
            lifelineChart = false
        )
    ))

    val lifelinesUiState: StateFlow<LifelinesUiState> = _lifelinesUiState




    private var connected: Boolean = false
    private lateinit var  mConnectedDeviceName: String
    private var mChatService: BluetoothChatService? = null

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {

            when (msg.what) {

                Constants.MESSAGE_STATE_CHANGE -> {
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: ")
                    when (msg.arg1) {

                        BluetoothChatService.STATE_CONNECTED -> {
                            Log.d(TAG, "STATE_CONNECTED: ")
                            connected = true
                        }

                        BluetoothChatService.STATE_CONNECTING -> {
                            Log.d(TAG, "STATE_CONNECTING: ")
                            connected = false
                        }

                        BluetoothChatService.STATE_LISTEN, BluetoothChatService.STATE_NONE -> {
                            Log.d(TAG, "STATE_LISTEN: ")
                            connected = false
                        }
                    }
                }

                Constants.MESSAGE_WRITE -> {
                    Log.d(TAG, "MESSAGE_WRITE: ")
                    val writeBuf = msg.obj as ByteArray

                }
                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                    Log.d(TAG, "MESSAGE_READ: $readMessage")
                    executeAction(readMessage)
                }
                Constants.MESSAGE_DEVICE_NAME -> {
                    Log.d(TAG, "MESSAGE_DEVICE_NAME: ")
                    // save the connected device's name
                    mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)!!
                    connected = true
                    _btUiState.value = BluetoothUiState.Success(0)
                }
                Constants.MESSAGE_TOAST -> {
                    Log.d(TAG, "MESSAGE_TOAST: ")
                    connected = false
                }
            }
        }
    }

    private fun executeAction(action: String) {
        when(action) {
            Actions.MAIN_LOAD_QUESTION -> loadQuestion()
            Actions.MAIN_SHOW_QUESTION -> showQuestion()
            Actions.MAIN_SHOW_OPTION_A -> showOption(0)
            Actions.MAIN_SHOW_OPTION_B -> showOption(1)
            Actions.MAIN_SHOW_OPTION_C -> showOption(2)
            Actions.MAIN_SHOW_OPTION_D -> showOption(3)
            Actions.MAIN_MARK_OPTION_A -> markAnswer(0)
            Actions.MAIN_MARK_OPTION_B -> markAnswer(1)
            Actions.MAIN_MARK_OPTION_C -> markAnswer(2)
            Actions.MAIN_MARK_OPTION_D -> markAnswer(3)
            Actions.MAIN_SHOW_ANSWER -> showCorrectAnswer(-1)
            Actions.MAIN_CHANGE_NEXT_Q -> nextQuestion()
            Actions.MAIN_SHOW_ALL_OPTIONS -> showAllOptions()

            Actions.NAVIGATE_UP -> navigateUp()
            Actions.NAVIGATE_REWARD -> navigateReward()
            Actions.NAVIGATE_CHART -> navigateChart()
            Actions.NAVIGATE_CLOCK -> navigateClock()
            Actions.NAVIGATE_TABLE -> navigateTable()
            Actions.NAVIGATE_NEXT -> navigateNext()

            Actions.LIFE_SHOW_50 -> showFifty()

            Actions.LIFE_TOGGLE_PHONE -> togglePhone()
            Actions.LIFE_TOGGLE_50 -> toggleFifty()
            Actions.LIFE_TOGGLE_GROUP -> toggleGroup()
            Actions.LIFE_TOGGLE_CHART -> toggleChart()

            Actions.CONFIG_SHOW_OPENING -> navigateOpening()

            Actions.CONFIG_NAV_QUEST -> navigateQuestion()
            Actions.CONFIG_RESET_UI -> resetQuestionUi()

            Actions.LIFE_TABLE_SHOW_REWARD -> showTableNextReward()

            else -> {
                val position = action.substringAfter("|").toInt()
                selectQuestion(position)
            }

        }
    }

    override fun showTableNextReward() {
        broadcastUpdate(Actions.LIFE_TABLE_SHOW_REWARD)
    }

    private fun togglePhone() {
        val dao = AppDatabase.getDatabase().lifelinesDao()
        scope.launch {
            val lifelines = dao.loadLifelines(0)
            val updated = lifelines.copy(lifelinePhone = !lifelines.lifelinePhone)
            dao.updateLifelines(updated)
            _lifelinesUiState.value = LifelinesUiState.Success(updated)
        }
    }

    private fun toggleFifty() {
        val dao = AppDatabase.getDatabase().lifelinesDao()
        scope.launch {
            val lifelines = dao.loadLifelines(0)
            val updated = lifelines.copy(lifelineFifty = !lifelines.lifelineFifty)
            dao.updateLifelines(updated)
            _lifelinesUiState.value = LifelinesUiState.Success(updated)
        }
    }

    private fun toggleGroup() {
        val dao = AppDatabase.getDatabase().lifelinesDao()
        scope.launch {
            val lifelines = dao.loadLifelines(0)
            val updated = lifelines.copy(lifelineGroup = !lifelines.lifelineGroup)
            dao.updateLifelines(updated)
            _lifelinesUiState.value = LifelinesUiState.Success(updated)
        }
    }

    private fun toggleChart() {
        val dao = AppDatabase.getDatabase().lifelinesDao()
        scope.launch {
            val lifelines = dao.loadLifelines(0)
            val updated = lifelines.copy(lifelineChart = !lifelines.lifelineChart)
            dao.updateLifelines(updated)
            _lifelinesUiState.value = LifelinesUiState.Success(updated)
        }
    }

    override fun loadQuestionsToDatabase() {
        saveQuestionsFromFile()
    }

    override fun loadQuestion() {
        val dao = AppDatabase.getDatabase().questionDao()
        scope.launch {
            val current = dao.getCurrentQuestion()
            _mainUiState.value = CurrentQuestionUiState.Success(current)
        }
    }

    override fun showQuestion() {
        _mainUiState.value = CurrentQuestionUiState.ShowQuestion(-1)
    }

    override fun showAllOptions() {
        scope.launch {
            showOption(0)
            delay(1000)
            showOption(1)
            delay(1000)
            showOption(2)
            delay(1000)
            showOption(3)
        }
    }

    override fun showOption(position: Int) {
        _mainUiState.value = CurrentQuestionUiState.ShowOption(position)
    }

    override fun markAnswer(position: Int) {
        _mainUiState.value = CurrentQuestionUiState.MarkAnswer(position)
    }

    override fun showCorrectAnswer(position: Int) {
        _mainUiState.value = CurrentQuestionUiState.CorrectAnswer(position)
    }

    override fun nextQuestion() {
        val dao = AppDatabase.getDatabase().questionDao()
        scope.launch {
            val answeredCount = dao.getAnsweredCount()
            updateLastAnswered(answeredCount)
            //showQuestion()
        }
    }

    fun resetQuestionUi() {
        _mainUiState.value = CurrentQuestionUiState.ResetQuestionUi(-1)
    }

    override fun updateLastAnswered(questionNumber: Int) {
        val dao = AppDatabase.getDatabase().questionDao()
        scope.launch {
            val questionModel = dao.loadQuestionById(questionNumber)
            val updated = questionModel.copy(isAnswered = true)
            dao.updateQuestionAsAnswered(updated)
        }
    }

    override fun toggleLifeline(lifeline: LifelinesEnum) {
    }

    override suspend fun getCurrentQuestion()  {
        Log.d(TAG, "getCurrentQuestion: ")
        val dao = AppDatabase.getDatabase().questionDao()
        scope.launch {

        }
    }

    override fun selectQuestion(questionNumber: Int) {
        val dao = AppDatabase.getDatabase().questionDao()
        scope.launch {
            dao.getAll().take(questionNumber).forEach {
                val updated = it.copy(isAnswered = true)
                dao.updateQuestionAsAnswered(updated)
            }
            val count = dao.getAnsweredCount()
            _mainUiState.value = CurrentQuestionUiState.UpdateSuccess(count)
        }
    }

    override fun showFifty() {
        _mainUiState.value = CurrentQuestionUiState.ShowFifty(-1)
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

    override fun navigateOpening() {
        Log.d(TAG, "navigateOpening: ")
        broadcastUpdate(Actions.NAVIGATE_OPEN)
    }

    override fun navigateQuestion() {
        Log.d(TAG, "navigateQuestion: ")
        broadcastUpdate(Actions.NAVIGATE_QUEST)
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

    private fun navigateNext() {
        Log.d(TAG, "navigateNext: ")
        broadcastUpdate(Actions.NAVIGATE_NEXT)
    }

    override fun getCurrentReward() {
        val dao = AppDatabase.getDatabase().questionDao()
        scope.launch {
            var answeredCount = dao.getAnsweredCount() - 1
            answeredCount = if (answeredCount == -1) 0 else answeredCount
            _rewardUiState.value = RewardUiState.Success(RewardTableEnum.values()[answeredCount].title, answeredCount == 19)
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

    private fun saveLifelines() {
        val dao = AppDatabase.getDatabase().lifelinesDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertAll(
                LifelineModel(
                uid = 0,
                lifelinePhone = false,
                lifelineFifty = false,
                lifelineGroup = false,
                lifelineChart = false
            )
            )
        }
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
        saveLifelines()
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

    fun startBluetoothServer() {
        mChatService = BluetoothChatService(context, mHandler)
        mChatService!!.start()
    }


}