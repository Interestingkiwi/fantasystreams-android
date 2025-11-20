package com.example.fantasystreams.data.network

import com.example.fantasystreams.data.model.MatchupPageData
import com.example.fantasystreams.data.model.MatchupStatsRequest
import com.example.fantasystreams.data.model.MatchupStatsResponse
import com.example.fantasystreams.data.model.*
import retrofit2.Response // Use Response<T> for error handling
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FantasyApiService {

    @GET("/api/matchup_page_data")
    suspend fun getMatchupPageData(): Response<MatchupPageData>

    @POST("/api/matchup_team_stats")
    suspend fun getMatchupStats(@Body request: MatchupStatsRequest): Response<MatchupStatsResponse>

    @GET("/api/lineup_page_data")
    suspend fun getLineupPageData(): Response<LineupPageData>

    @POST("/api/roster_data")
    suspend fun getRosterData(@Body request: RosterDataRequest): Response<RosterDataResponse>
}