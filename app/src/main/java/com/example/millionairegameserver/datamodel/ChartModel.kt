package com.example.millionairegameserver.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChartModel(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "ans_a") val ansAPercentage: Int,
    @ColumnInfo(name = "ans_b") val ansBPercentage: Int,
    @ColumnInfo(name = "ans_c") val ansCPercentage: Int,
    @ColumnInfo(name = "ans_d") val ansDPercentage: Int
)
