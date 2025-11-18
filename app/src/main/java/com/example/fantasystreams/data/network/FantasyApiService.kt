package com.example.fantasystreams.data.network

import com.example.fantasystreams.data.model.MatchupPageData
import com.example.fantasystreams.data.model.MatchupStatsRequest
import com.example.fantasystreams.data.model.MatchupStatsResponse
import retrofit2.Response // Use Response<T> for error handling
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FantasyApiService {

    @GET("/api/matchup_page_data")
    suspend fun getMatchupPageData(): Response<MatchupPageData>

    @POST("/api/matchup_team_stats")
    suspend fun getMatchupStats(
        @Body request: MatchupStatsRequest
    ): Response<MatchupStatsResponse>
}