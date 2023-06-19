package com.example.millionairegameserver.repository

import com.example.millionairegameserver.LifelinesEnum
import com.example.millionairegameserver.datamodel.ChartModel

interface Repository {

    fun loadQuestionsToDatabase()

    //MainScreen

    fun loadQuestion()
    fun showQuestion()
    fun showOption(position: Int)
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
    fun loadChartResult(chartModel: ChartModel)
    fun showChartResult()
    fun navigateChart()

    fun navigateClock()
    fun startClock()

    fun getCurrentReward()

    fun navigateQuestion()
    fun navigateOpening()
    fun showAllAnswers()
}