package com.example.fantasystreams.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("FantasyStreamsPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val SELECTED_TEAM_NAME = "selected_team_name"
        private const val SELECTED_WEEK = "selected_week"
    }

    fun saveSelectedTeam(teamName: String) {
        prefs.edit().putString(SELECTED_TEAM_NAME, teamName).apply()
    }

    fun getSelectedTeam(): String? {
        return prefs.getString(SELECTED_TEAM_NAME, null)
    }

    fun saveSelectedWeek(weekNum: String) {
        prefs.edit().putString(SELECTED_WEEK, weekNum).apply()
    }

    fun getSelectedWeek(): String? {
        return prefs.getString(SELECTED_WEEK, null)
    }
}