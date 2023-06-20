package com.example.millionairegameserver.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.millionairegameserver.App
import com.example.millionairegameserver.datamodel.ChartModel
import com.example.millionairegameserver.datamodel.LifelineModel
import com.example.millionairegameserver.datamodel.QuestionModel

@Database(entities = [QuestionModel::class, ChartModel::class, LifelineModel::class], version = 4)
abstract class AppDatabase: RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun chartDao(): ChartDao

    abstract fun lifelinesDao(): LifelinesDao

    companion object {
        private const val DB_NAME = "questions_db"
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        App.applicationContext(),
                        AppDatabase::class.java, DB_NAME
                    ).fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE!!
        }
    }
}