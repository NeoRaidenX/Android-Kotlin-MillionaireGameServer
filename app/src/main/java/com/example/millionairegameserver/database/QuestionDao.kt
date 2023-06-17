package com.example.millionairegameserver.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.millionairegameserver.datamodel.QuestionModel
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questionmodel")
    fun getAll(): List<QuestionModel>

    @Query("SELECT * FROM questionmodel WHERE uid = :questionId")
    fun loadQuestionById(questionId: Int): QuestionModel

    @Query("SELECT * FROM questionmodel WHERE is_answered == 0 LIMIT 1")
    suspend fun getCurrentQuestion(): QuestionModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(questions: List<QuestionModel>)

    @Update
    fun updateQuestionAsAnswered(vararg questionModel: QuestionModel)

    @Update
    fun updateQuestionAsNotAnswered(vararg questionModel: QuestionModel)

    @Query("SELECT COUNT(is_answered) FROM questionmodel WHERE is_answered == 1")
    fun getAnsweredCount(): Int
}