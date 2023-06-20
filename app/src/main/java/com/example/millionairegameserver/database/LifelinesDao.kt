package com.example.millionairegameserver.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.millionairegameserver.datamodel.ChartModel
import com.example.millionairegameserver.datamodel.LifelineModel
import com.example.millionairegameserver.datamodel.QuestionModel
import kotlinx.coroutines.flow.Flow

@Dao
interface LifelinesDao {

    @Query("SELECT * FROM lifelinemodel WHERE uid = :id")
    fun loadLifelines(id: Int): LifelineModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg lifelineModel: LifelineModel)

    @Update
    fun updateLifelines(vararg lifelineModel: LifelineModel)

}