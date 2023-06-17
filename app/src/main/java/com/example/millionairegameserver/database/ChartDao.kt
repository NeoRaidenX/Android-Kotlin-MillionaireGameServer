package com.example.millionairegameserver.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.millionairegameserver.datamodel.ChartModel
import com.example.millionairegameserver.datamodel.QuestionModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChartDao {
    @Query("SELECT * FROM chartmodel")
    fun getAll(): List<ChartModel>

    @Query("SELECT * FROM chartmodel WHERE uid = :id")
    fun loadQuestionById(id: Int): ChartModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg chartModel: ChartModel)

    @Update
    fun updateQuestionAsAnswered(vararg chartModel: ChartModel)

}