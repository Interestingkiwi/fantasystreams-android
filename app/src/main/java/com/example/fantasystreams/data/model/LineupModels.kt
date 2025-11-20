package com.example.fantasystreams.data.model

import com.google.gson.annotations.SerializedName

// --- /api/lineup_page_data ---
data class LineupPageData(
    @SerializedName("db_exists") val dbExists: Boolean,
    val weeks: List<Week>,
    val teams: List<Team>,
    @SerializedName("current_week") val currentWeek: Int
)

// --- /api/roster_data Request ---
data class RosterDataRequest(
    val week: String,
    @SerializedName("team_name") val teamName: String,
    val sourcing: String, // "projected", "todate", "combined"
    val categories: List<String>? = null,
    @SerializedName("simulated_moves") val simulatedMoves: List<Any> = emptyList()
)

// --- /api/roster_data Response ---
data class RosterDataResponse(
    val players: List<LineupPlayer>,
    @SerializedName("daily_optimal_lineups")
    val dailyOptimalLineups: Map<String, Map<String, List<LineupPlayer>>>, // Date -> Position -> Players
    @SerializedName("scoring_categories") val scoringCategories: List<String>,
    @SerializedName("skater_categories") val skaterCategories: List<String>,
    @SerializedName("goalie_categories") val goalieCategories: List<String>,
    @SerializedName("lineup_settings") val lineupSettings: Map<String, Int>,
    @SerializedName("unused_roster_spots")
    val unusedRosterSpots: Map<String, Map<String, String>>?
)

data class LineupPlayer(
    @SerializedName("player_id") val playerId: Int,
    @SerializedName("player_name") val playerName: String,
    @SerializedName("player_name_normalized") val playerNameNormalized: String?,
    val team: String?,
    @SerializedName("eligible_positions") val eligiblePositions: String?, // "C,LW"
    val status: String?,

    // Stats & Ranks (Dynamic based on league)
    @SerializedName("total_rank") val totalRank: Double?,
    @SerializedName("total_cat_rank") val totalCatRank: Double?, // Sometimes used in other endpoints

    // Schedule
    @SerializedName("games_this_week") val gamesThisWeek: List<String>?, // ["Mon", "Wed"]
    @SerializedName("games_next_week") val gamesNextWeek: List<String>?,
    @SerializedName("opponent_stats_this_week") val opponentStats: List<OpponentStat>?,

    // PP Stats
    @SerializedName("avg_ppTimeOnIcePctPerGame") val ppTimePct: Double?,
    @SerializedName("total_ppGoals") val ppGoals: Double?,
    @SerializedName("total_ppAssists") val ppAssists: Double?,
    @SerializedName("lg_ppGoals") val lgPpGoals: Double?,
    @SerializedName("lg_ppAssists") val lgPpAssists: Double?,

    // Dynamic Stat Maps (Handled via generic map for unknown columns)
    // We will use a custom deserializer or simple map access in UI if Gson allows,
    // but for simplicity in Retrofit/Gson, we often list common ones or use a Map<String, Any> adapter.
    // However, since the API returns a flat JSON object for the player,
    // we can capture specific stats we know exist or access them via a Map if we change the API structure.
    // Since we can't change the API structure easily to a nested 'stats' object without breaking web,
    // we will rely on the UI looking up values from a Map<String, Any> or define common ones here.

    // Common Stats (Add yours here)
    val G: Double?, val A: Double?, val P: Double?, val PPP: Double?,
    val SOG: Double?, val HIT: Double?, val BLK: Double?,
    val W: Double?, val L: Double?, val GA: Double?, val GAA: Double?,
    val SV: Double?, val SA: Double?, val SVpct: Double?, val SHO: Double?,

    // Rank Columns (e.g. G_cat_rank)
    @SerializedName("G_cat_rank") val rankG: Double?,
    @SerializedName("A_cat_rank") val rankA: Double?,
    @SerializedName("P_cat_rank") val rankP: Double?,
    @SerializedName("PPP_cat_rank") val rankPPP: Double?,
    @SerializedName("SOG_cat_rank") val rankSOG: Double?,
    @SerializedName("HIT_cat_rank") val rankHIT: Double?,
    @SerializedName("BLK_cat_rank") val rankBLK: Double?,
    @SerializedName("W_cat_rank") val rankW: Double?,
    @SerializedName("GAA_cat_rank") val rankGAA: Double?,
    @SerializedName("SV_cat_rank") val rankSV: Double?,
    @SerializedName("SVpct_cat_rank") val rankSVpct: Double?,
    @SerializedName("SHO_cat_rank") val rankSHO: Double?
) {
    // Helper to get dynamic stat/rank by string key
    fun getStatValue(key: String): Double? {
        return when(key) {
            "G" -> G; "A" -> A; "P" -> P; "PPP" -> PPP; "SOG" -> SOG; "HIT" -> HIT; "BLK" -> BLK
            "W" -> W; "L" -> L; "GA" -> GA; "GAA" -> GAA; "SV" -> SV; "SA" -> SA; "SVpct" -> SVpct; "SHO" -> SHO
            else -> 0.0
        }
    }

    fun getRankValue(key: String): Double? {
        return when(key) {
            "G" -> rankG; "A" -> rankA; "P" -> rankP; "PPP" -> rankPPP; "SOG" -> rankSOG; "HIT" -> rankHIT; "BLK" -> rankBLK
            "W" -> rankW; "GAA" -> rankGAA; "SV" -> rankSV; "SVpct" -> rankSVpct; "SHO" -> rankSHO
            else -> 0.0
        }
    }
}

data class OpponentStat(
    @SerializedName("game_date") val gameDate: String, // "Mon, Oct 12"
    @SerializedName("opponent_tricode") val opponent: String,
    @SerializedName("ga_gm") val gaGm: Double?,
    @SerializedName("pk_pct") val pkPct: Double?
)