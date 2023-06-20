package com.example.millionairegameserver.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QuestionModel(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "question") val question: String,
    @ColumnInfo(name = "option_a") val optionA: String,
    @ColumnInfo(name = "option_b") val optionB: String,
    @ColumnInfo(name = "option_c") val optionC: String,
    @ColumnInfo(name = "option_d") val optionD: String,
    @ColumnInfo(name = "correct_ans") val correct: Int,
    @ColumnInfo(name = "marked_ans") val markedAnswer: Int,
    @ColumnInfo(name = "is_answered") val isAnswered: Boolean,
    @ColumnInfo(name = "is_opt_a") val isOptA: Boolean,
    @ColumnInfo(name = "is_opt_b") val isOptB: Boolean,
    @ColumnInfo(name = "is_opt_c") val isOptC: Boolean,
    @ColumnInfo(name = "is_opt_d") val isOptD: Boolean,
)
