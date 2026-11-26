package com.example.data.api

import com.example.model.OddsEventDto
import com.example.model.OddsSportKeyDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service definition for The Odds API v4 endpoints (`https://api.the-odds-api.com/v4/`).
 */
interface OddsApiService {

    @GET("sports/")
    suspend fun getSports(
        @Query("apiKey") apiKey: String
    ): List<OddsSportKeyDto>

    @GET("sports/{sportKey}/odds/")
    suspend fun getSportOdds(
        @Path("sportKey") sportKey: String,
        @Query("apiKey") apiKey: String,
        @Query("regions") regions: String = "uk,eu,au",
        @Query("markets") markets: String = "h2h,totals,spreads",
        @Query("oddsFormat") oddsFormat: String = "decimal"
    ): List<OddsEventDto>

    @GET("sports/{sportKey}/events/{eventId}/odds/")
    suspend fun getEventOdds(
        @Path("sportKey") sportKey: String,
        @Path("eventId") eventId: String,
        @Query("apiKey") apiKey: String,
        @Query("regions") regions: String = "uk,eu,au",
        @Query("markets") markets: String = "h2h,totals,spreads",
        @Query("oddsFormat") oddsFormat: String = "decimal"
    ): OddsEventDto
}
