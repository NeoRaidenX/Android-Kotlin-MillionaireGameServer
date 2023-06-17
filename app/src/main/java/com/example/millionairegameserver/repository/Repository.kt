package com.example.millionairegameserver.repository

import com.example.millionairegameserver.LifelinesEnum

interface Repository {

    fun loadQuestionsToDatabase()

    //MainScreen
    fun showQuestion()
    fun showAnswer(position: Int)
    fun markAnswer(position: Int)
    fun showCorrectAnswer(position: Int)
    fun navigateReward()
    fun navigateUp()
    fun navigateTable()
    fun nextQuestion()
    fun updateLastAnswered(questionNumber: Int)
    fun toggleLifeline(lifeline: LifelinesEnum)
    suspend fun getCurrentQuestion()
    fun selectQuestion(questionNumber: Int)

    fun showFifty()
    //Lifelines
    fun getChartQuantity()
    fun navigateChart()

    fun navigateClock()
    fun startClock()

    fun getCurrentReward()

}