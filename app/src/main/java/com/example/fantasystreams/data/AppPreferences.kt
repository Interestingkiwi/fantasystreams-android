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
        private const val DATA_SOURCE = "data_source"
        private const val SHOW_RAW_DATA = "show_raw_data"
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

    fun saveDataSource(source: String) {
        prefs.edit().putString(DATA_SOURCE, source).apply()
    }

    fun getDataSource(): String {
        // Default to "projected" (Projected ROS)
        return prefs.getString(DATA_SOURCE, "projected") ?: "projected"
    }

    fun saveShowRawData(show: Boolean) {
        prefs.edit().putBoolean(SHOW_RAW_DATA, show).apply()
    }

    fun getShowRawData(): Boolean {
        return prefs.getBoolean(SHOW_RAW_DATA, false)
    }
}