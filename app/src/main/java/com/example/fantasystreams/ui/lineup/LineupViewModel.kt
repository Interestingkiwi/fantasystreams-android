package com.example.fantasystreams.ui.lineup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasystreams.data.AppPreferences
import com.example.fantasystreams.data.model.*
import com.example.fantasystreams.data.network.FantasyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class LineupUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val pageData: LineupPageData? = null,
    val rosterData: RosterDataResponse? = null,

    val selectedWeek: String? = null,
    val selectedTeam: String? = null,
    val selectedDate: String? = null, // e.g., "Fri, Nov 21" (Format matching API keys)

    val enabledCategories: Set<String> = emptySet(),
    val isCategoryDrawerOpen: Boolean = false,

    val showRawData: Boolean = false,
    val dayOptions: List<String> = emptyList() // List of dates for the dropdown
)

@HiltViewModel
class LineupViewModel @Inject constructor(
    private val apiService: FantasyApiService,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LineupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val initialWeek = prefs.getSelectedWeek()
        val initialTeam = prefs.getSelectedTeam()
        val showRaw = prefs.getShowRawData()

        _uiState.update {
            it.copy(
                selectedWeek = initialWeek,
                selectedTeam = initialTeam,
                showRawData = showRaw
            )
        }

        fetchPageData()
    }

    // Called from LaunchedEffect to ensure data is fresh if settings change
    fun refreshData() {
        val currentRaw = prefs.getShowRawData()
        if (_uiState.value.showRawData != currentRaw) {
            _uiState.update { it.copy(showRawData = currentRaw) }
        }
        // Re-fetch roster if we have the basics
        if (_uiState.value.selectedWeek != null && _uiState.value.selectedTeam != null) {
            fetchRosterData()
        }
    }

    private fun fetchPageData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getLineupPageData()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val week = _uiState.value.selectedWeek ?: data.currentWeek.toString()
                    val team = _uiState.value.selectedTeam ?: data.teams.firstOrNull()?.name

                    if (_uiState.value.selectedWeek == null) prefs.saveSelectedWeek(week)
                    if (_uiState.value.selectedTeam == null && team != null) prefs.saveSelectedTeam(team)

                    _uiState.update {
                        it.copy(
                            pageData = data,
                            selectedWeek = week,
                            selectedTeam = team,
                            isLoading = false
                        )
                    }
                    fetchRosterData()
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load page data.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun fetchRosterData() {
        val week = _uiState.value.selectedWeek ?: return
        val team = _uiState.value.selectedTeam ?: return
        val sourcing = prefs.getDataSource()

        // If categories are empty (first load), send null to get defaults from backend
        val catsToSend = if (_uiState.value.enabledCategories.isEmpty()) null else _uiState.value.enabledCategories.toList()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = RosterDataRequest(week, team, sourcing, catsToSend)
                val response = apiService.getRosterData(request)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // Calculate day options for the selected week
                    val dayOptions = calculateDayOptions(week)
                    // Default date: Today if in range, else first day
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val todayFmt = convertToDisplayDate(todayStr) // needs to match keys in dailyOptimalLineups?
                    // Actually dailyOptimalLineups keys are formatted like "Fri, Oct 10" from python backend

                    // We need to parse week start/end to find today
                    // But for now, let's look at the keys returned in dailyOptimalLineups
                    val availableDates = data.dailyOptimalLineups.keys.sorted() // Sort might need work if formats differ

                    // Better logic: find a key that contains today's date parts or just default to first
                    val defaultDate = availableDates.firstOrNull()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rosterData = data,
                            enabledCategories = if (it.enabledCategories.isEmpty()) data.scoringCategories.toSet() else it.enabledCategories,
                            dayOptions = availableDates,
                            selectedDate = it.selectedDate ?: defaultDate
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun calculateDayOptions(weekNum: String): List<String> {
        // This is a fallback if API doesn't return map keys we want.
        // But we use the keys from dailyOptimalLineups in the API response usually.
        return emptyList()
    }

    // Placeholder helper - Python sends "Fri, Oct 10"
    private fun convertToDisplayDate(isoDate: String): String {
        return isoDate // Implementation depends on exact format matching
    }

    fun onWeekSelected(week: String) {
        prefs.saveSelectedWeek(week)
        _uiState.update { it.copy(selectedWeek = week, selectedDate = null) }
        fetchRosterData()
    }

    fun onTeamSelected(team: String) {
        prefs.saveSelectedTeam(team)
        _uiState.update { it.copy(selectedTeam = team) }
        fetchRosterData()
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun toggleCategoryDrawer() {
        _uiState.update { it.copy(isCategoryDrawerOpen = !it.isCategoryDrawerOpen) }
    }

    fun toggleCategory(cat: String) {
        val current = _uiState.value.enabledCategories.toMutableSet()
        if (current.contains(cat)) current.remove(cat) else current.add(cat)

        _uiState.update { it.copy(enabledCategories = current) }
        // Trigger fetch to update 'total_rank' based on new selection
        fetchRosterData()
    }
}