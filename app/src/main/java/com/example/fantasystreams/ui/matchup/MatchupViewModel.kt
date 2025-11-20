package com.example.fantasystreams.ui.matchup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystreams.data.AppPreferences
import com.example.fantasystreams.data.model.*
import com.example.fantasystreams.data.network.FantasyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Represents the entire state of the Matchup screen
data class MatchupScreenState(
    val isLoadingPage: Boolean = true,
    val isLoadingStats: Boolean = false,
    val pageData: MatchupPageData? = null,
    val matchupStats: MatchupStatsResponse? = null,
    val selectedWeek: String? = null,
    val selectedTeam: String? = null,
    val selectedOpponent: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MatchupViewModel @Inject constructor(
    private val apiService: FantasyApiService,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchupScreenState())
    val uiState: StateFlow<MatchupScreenState> = _uiState.asStateFlow()

    init {
        // Load initial state from preferences and then fetch data
        val initialWeek = prefs.getSelectedWeek()
        val initialTeam = prefs.getSelectedTeam()

        _uiState.update {
            it.copy(selectedWeek = initialWeek, selectedTeam = initialTeam)
        }

        fetchPageData()
    }

    private fun fetchPageData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPage = true, errorMessage = null) }
            try {
                val response = apiService.getMatchupPageData()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    _uiState.update { state ->
                        // Use saved week/team, or default to API's current week/first team
                        val week = state.selectedWeek ?: data.currentWeek.toString()
                        val team = state.selectedTeam ?: data.teams.firstOrNull()?.name

                        // Save the defaults if they weren't set
                        if (state.selectedWeek == null) prefs.saveSelectedWeek(week)
                        if (state.selectedTeam == null) team?.let { prefs.saveSelectedTeam(it) }

                        state.copy(
                            isLoadingPage = false,
                            pageData = data,
                            selectedWeek = week,
                            selectedTeam = team
                        )
                    }
                    // After getting page data, auto-select opponent and fetch stats
                    updateOpponentAndFetchStats()
                } else {
                    _uiState.update { it.copy(isLoadingPage = false, errorMessage = response.message()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingPage = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

    // Called when user selects a new week from the dropdown
    fun onWeekSelected(weekNum: String) {
        prefs.saveSelectedWeek(weekNum) // Save to SharedPreferences
        _uiState.update { it.copy(selectedWeek = weekNum, matchupStats = null) } // Clear old stats
        updateOpponentAndFetchStats()
    }

    // Called when user selects a new team from the dropdown
    fun onTeamSelected(teamName: String) {
        prefs.saveSelectedTeam(teamName) // Save to SharedPreferences
        _uiState.update { it.copy(selectedTeam = teamName, matchupStats = null) } // Clear old stats
        updateOpponentAndFetchStats()
    }

    // Called when user manually selects an opponent
    fun onOpponentSelected(teamName: String) {
        _uiState.update { it.copy(selectedOpponent = teamName, matchupStats = null) }
        fetchMatchupStats() // Just fetch, don't update auto-opponent logic
    }

    // Mimics the logic from matchup.js 'updateOpponentDropdown'
    private fun updateOpponentAndFetchStats() {
        val state = _uiState.value
        val week = state.selectedWeek
        val team = state.selectedTeam
        val matchups = state.pageData?.matchups

        if (week == null || team == null) return // Not ready yet

        val opponent = matchups?.find { m ->
            m.week.toString() == week && (m.team1 == team || m.team2 == team)
        }?.let {
            if (it.team1 == team) it.team2 else it.team1
        } ?: state.pageData?.teams?.firstOrNull { it.name != team }?.name // Default to first other team

        _uiState.update { it.copy(selectedOpponent = opponent) }
        fetchMatchupStats()
    }

    fun fetchMatchupStats() {
        val state = _uiState.value
        val week = state.selectedWeek
        val team1 = state.selectedTeam
        val team2 = state.selectedOpponent

        if (week == null || team1 == null || team2 == null) {
            _uiState.update { it.copy(errorMessage = "Please make all selections.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true, errorMessage = null) }
            try {
                // Fetch the current data source setting
                val sourcing = prefs.getDataSource()

                val request = MatchupStatsRequest(
                    week = week,
                    team1Name = team1,
                    team2Name = team2,
                    sourcing = sourcing // Pass to API
                )
                val response = apiService.getMatchupStats(request)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update {
                        it.copy(isLoadingStats = false, matchupStats = response.body())
                    }
                } else {
                    _uiState.update { it.copy(isLoadingStats = false, errorMessage = response.message()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingStats = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }
}