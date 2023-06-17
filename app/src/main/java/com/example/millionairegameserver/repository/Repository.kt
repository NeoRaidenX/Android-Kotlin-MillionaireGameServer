package com.example.millionairegameserver.repository

import com.example.millionairegameserver.LifelinesEnum
import com.example.millionairegameserver.datamodel.QuestionModel
import kotlinx.coroutines.flow.Flow

interface Repository {

    fun loadQuestionsToDatabase()

    //MainScreen
    fun showQuestion()
    fun showAnswer(position: Int)
    fun markAnswer(position: Int)
    fun showCorrectAnswer(position: Int)
    fun showReward()
    fun nextQuestion()
    fun updateLastAnswered(questionNumber: Int)
    fun toggleLifeline(lifeline: LifelinesEnum)
    suspend fun getCurrentQuestion()
    fun selectQuestion(questionNumber: Int)
    fun showFifty()

    //Lifelines
    fun getChartQuantity()
    fun showChart()
    fun showClock()
    fun startClock()

    fun getCurrentReward()

}