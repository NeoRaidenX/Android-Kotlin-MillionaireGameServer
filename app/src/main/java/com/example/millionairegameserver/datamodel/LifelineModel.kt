package com.example.millionairegameserver.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LifelineModel(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "lifeline_phone")val lifelinePhone: Boolean,
    @ColumnInfo(name = "lifeline_fifty")val lifelineFifty: Boolean,
    @ColumnInfo(name = "lifeline_group")val lifelineGroup: Boolean,
    @ColumnInfo(name = "lifeline_chart")val lifelineChart: Boolean,

)
