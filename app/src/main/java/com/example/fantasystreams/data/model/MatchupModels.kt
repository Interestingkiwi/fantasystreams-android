package com.example.fantasystreams.data.model

import com.google.gson.annotations.SerializedName

// --- /api/matchup_page_data Response ---

data class MatchupPageData(
    @SerializedName("db_exists")
    val dbExists: Boolean,
    val weeks: List<Week>,
    val teams: List<Team>,
    val matchups: List<Matchup>,
    @SerializedName("scoring_categories")
    val scoringCategories: List<ScoringCategory>,
    @SerializedName("current_week")
    val currentWeek: Int
)

data class Week(
    @SerializedName("week_num")
    val weekNum: Int,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String
)

data class Team(
    @SerializedName("team_id")
    val teamId: String, // Keep as String to be safe
    val name: String
)

data class Matchup(
    val week: Int,
    val team1: String,
    val team2: String
)

data class ScoringCategory(
    val category: String,
    @SerializedName("stat_id")
    val statId: Int,
    @SerializedName("scoring_group")
    val scoringGroup: String
)


// --- /api/matchup_team_stats Request Body ---

data class MatchupStatsRequest(
    val week: String,
    @SerializedName("team1_name")
    val team1Name: String,
    @SerializedName("team2_name")
    val team2Name: String,
    val categories: List<String>? = null,
    @SerializedName("simulated_moves")
    val simulatedMoves: List<Any> = emptyList(),
    val sourcing: String? = null // Added sourcing field
)


// --- /api/matchup_team_stats Response ---

data class MatchupStatsResponse(
    @SerializedName("team1")
    val team1Stats: TeamStats,
    @SerializedName("team2")
    val team2Stats: TeamStats,
    @SerializedName("game_counts")
    val gameCounts: GameCounts,
    @SerializedName("team1_unused_spots")
    val team1UnusedSpots: Map<String, Map<String, String>>?
)

data class TeamStats(
    // Map<CategoryName, StatValue>
    val live: Map<String, Double>,
    val row: Map<String, Double>
)

data class GameCounts(
    @SerializedName("team1_total")
    val team1Total: Int,
    @SerializedName("team2_total")
    val team2Total: Int,
    @SerializedName("team1_remaining")
    val team1Remaining: Int,
    @SerializedName("team2_remaining")
    val team2Remaining: Int
)