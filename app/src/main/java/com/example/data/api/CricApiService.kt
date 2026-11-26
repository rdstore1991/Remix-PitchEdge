package com.example.data.api

import com.example.model.CricApiEnvelope
import com.example.model.CricApiSeriesDto
import com.example.model.CricApiSeriesPointsRowDto
import com.example.model.CricketMatchScorecardDto
import com.example.model.CricketMatchSummaryDto
import com.example.model.MatchSquadTeamDto
import com.example.model.PlayerProfileDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service definition for CricAPI v1 endpoints (`https://api.cricapi.com/v1/`).
 */
interface CricApiService {

    @GET("currentMatches")
    suspend fun getCurrentMatches(
        @Query("apikey") apiKey: String,
        @Query("offset") offset: Int = 0
    ): CricApiEnvelope<List<CricketMatchSummaryDto>>

    @GET("match_info")
    suspend fun getMatchInfo(
        @Query("apikey") apiKey: String,
        @Query("id") matchId: String
    ): CricApiEnvelope<CricketMatchScorecardDto>

    @GET("match_scorecard")
    suspend fun getMatchScorecard(
        @Query("apikey") apiKey: String,
        @Query("id") matchId: String
    ): CricApiEnvelope<CricketMatchScorecardDto>

    @GET("players_info")
    suspend fun getPlayerInfo(
        @Query("apikey") apiKey: String,
        @Query("id") playerId: String
    ): CricApiEnvelope<PlayerProfileDto>

    @GET("match_squad")
    suspend fun getMatchSquad(
        @Query("apikey") apiKey: String,
        @Query("id") matchId: String
    ): CricApiEnvelope<List<MatchSquadTeamDto>>

    @GET("series")
    suspend fun getSeries(
        @Query("apikey") apiKey: String,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String = ""
    ): CricApiEnvelope<List<CricApiSeriesDto>>

    @GET("series_points")
    suspend fun getSeriesPoints(
        @Query("apikey") apiKey: String,
        @Query("id") seriesId: String
    ): CricApiEnvelope<List<CricApiSeriesPointsRowDto>>
}
