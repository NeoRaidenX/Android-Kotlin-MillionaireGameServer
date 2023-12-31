package com.example.millionairegameserver

object Actions {
    private const val prefix = "millgame."
    const val NAVIGATE_UP = prefix + "nav_up"
    const val NAVIGATE_OPEN = prefix + "nav_op"
    const val NAVIGATE_QUEST = prefix + "nav_quest"
    const val NAVIGATE_TABLE = prefix + "nav_table"
    const val NAVIGATE_REWARD = prefix + "nav_reward"
    const val NAVIGATE_CHART = prefix + "nav_chart"
    const val NAVIGATE_CLOCK = prefix + "nav_clock"
    const val NAVIGATE_NEXT = prefix + "nav_next"
    const val PLAY_CLOCK = prefix + "play_clock"

    const val MAIN_LOAD_QUESTION = prefix + "loadquest"
    const val MAIN_SHOW_QUESTION = prefix + "showquestion"
    const val MAIN_SHOW_OPTION_A = prefix + "showopt_a"
    const val MAIN_SHOW_OPTION_B = prefix + "showopt_b"
    const val MAIN_SHOW_OPTION_C = prefix + "showopt_c"
    const val MAIN_SHOW_OPTION_D = prefix + "showopt_d"
    const val MAIN_MARK_OPTION_A = prefix + "markopt_a"
    const val MAIN_MARK_OPTION_B = prefix + "markopt_b"
    const val MAIN_MARK_OPTION_C = prefix + "markopt_c"
    const val MAIN_MARK_OPTION_D = prefix + "markopt_d"
    const val MAIN_SHOW_ANSWER = prefix + "showans"
    const val MAIN_SHOW_REWARD = prefix + "showrew"
    const val MAIN_CHANGE_NEXT_Q = prefix + "changenext"
    const val MAIN_SHOW_ALL_OPTIONS = prefix + "showallopt"

    const val LIFE_SHOW_PPL_FORM = prefix + "showpplform"
    const val LIFE_SHOW_PPL_CHOICE = prefix + "showpplchoice"
    const val LIFE_SHOW_CLOCK = prefix + "showclock"
    const val LIFE_SHOW_50 = prefix + "show50"
    const val LIFE_TABLE_SHOW_REWARD = prefix + "showtablerew"

    const val LIFE_TOGGLE_GROUP = prefix + "togglelifegroup"
    const val LIFE_TOGGLE_CHART = prefix + "togglelifechart"
    const val LIFE_TOGGLE_PHONE = prefix + "togglelifephone"
    const val LIFE_TOGGLE_50 = prefix + "togglelife50"

    const val CONFIG_SHOW_OPENING = prefix + "showop"
    const val CONFIG_SHOW_TABLE = prefix + "showtable"
    const val CONFIG_SELECT_QUEST = prefix + "selectquest"
    const val CONFIG_NAV_QUEST = prefix + "nav_quest"
    const val CONFIG_RESET_UI = prefix + "resetui"

    const val MSG_DATA = prefix + "msgdata"
    const val START_FOREGROUND = prefix + "startforeground"
}