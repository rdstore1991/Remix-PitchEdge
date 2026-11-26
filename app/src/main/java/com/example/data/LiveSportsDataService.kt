package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.CricApiService
import com.example.data.api.NetworkClient
import com.example.data.api.OddsApiService
import com.example.model.AnalyzedEventOdds
import com.example.model.AnalyzedOddsResponse
import com.example.model.ApiJson
import com.example.model.BookmakerOddsRow
import com.example.model.CricApiSeriesDto
import com.example.model.CricApiSeriesPointsRowDto
import com.example.model.CricketMatchScorecardDto
import com.example.model.CricketMatchSummaryDto
import com.example.model.CricketSportKeysResponse
import com.example.model.FixtureOverview
import com.example.model.FormatFilter
import com.example.model.LiveCricketMatchesResponse
import com.example.model.MatchSquadTeamDto
import com.example.model.OddsEventDto
import com.example.model.OddsSportKeyDto
import com.example.model.PlayerProfileDto
import com.example.model.SportFilter
import com.example.model.StatusFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.json.JSONObject
import retrofit2.HttpException
import java.util.Locale

private const val TAG = "LiveSportsDataService"

class LiveSportsDataService(
    private val cache: MemoryTtlCache = MemoryTtlCache(),
    private val cricApiService: CricApiService = NetworkClient.cricApiService,
    private val oddsApiService: OddsApiService = NetworkClient.oddsApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun isCricApiConfigured(): Boolean {
        val key = BuildConfig.CRIC_API_KEY
        return key.isNotBlank() && key != "MY_CRIC_API_KEY"
    }

    fun isOddsApiConfigured(): Boolean {
        val key = BuildConfig.ODDS_API_KEY
        return key.isNotBlank() && key != "MY_ODDS_API_KEY"
    }

    suspend fun fetchLiveCricketMatches(teamFilter: String? = null): LiveCricketMatchesResponse =
        withContext(ioDispatcher) {
            if (!isCricApiConfigured()) {
                throw IllegalStateException("CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
            }

            val cacheKey = "pitchedge:cricapi:currentMatches:v2"
            var matches = cache.get<List<CricketMatchSummaryDto>>(cacheKey)

            if (matches == null) {
                val envelope = cricApiService.getCurrentMatches(
                    apiKey = BuildConfig.CRIC_API_KEY,
                    offset = 0
                )
                if (!envelope.isSuccess) {
                    throw IllegalStateException("CricAPI Error: ${envelope.reason ?: "Failed to fetch matches"}")
                }
                matches = envelope.data.orEmpty()
                cache.set(cacheKey, matches, 30L)
            }

            val q = teamFilter?.trim()?.lowercase(Locale.US).orEmpty()
            val filtered = if (q.isEmpty()) {
                matches.take(20)
            } else {
                matches.filter { m ->
                    m.name.lowercase(Locale.US).contains(q) ||
                        m.teams.any { it.lowercase(Locale.US).contains(q) }
                }.take(20)
            }

            LiveCricketMatchesResponse(
                count = filtered.size,
                matches = filtered
            )
        }

    suspend fun fetchLiveCricketMatchesJson(teamFilter: String? = null): JSONObject {
        if (!isCricApiConfigured()) {
            Log.w(TAG, "CRIC_API_KEY is not configured in Secrets.")
            return JSONObject().apply {
                put("error", "CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
            }
        }
        return try {
            val response = fetchLiveCricketMatches(teamFilter)
            JSONObject(ApiJson.lenient.encodeToString(response))
        } catch (e: Exception) {
            JSONObject().apply { put("error", e.message ?: "CricAPI fetch failed") }
        }
    }

    suspend fun fetchCricketMatchScorecard(
        matchId: String,
        includeFullScorecard: Boolean = false
    ): CricketMatchScorecardDto = withContext(ioDispatcher) {
        if (!isCricApiConfigured()) {
            throw IllegalStateException("CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
        }
        val endpoint = if (includeFullScorecard) "match_scorecard" else "match_info"
        val cacheKey = "pitchedge:cricapi:$endpoint:$matchId"
        cache.get<CricketMatchScorecardDto>(cacheKey)?.let { return@withContext it }

        val envelope = if (includeFullScorecard) {
            cricApiService.getMatchScorecard(apiKey = BuildConfig.CRIC_API_KEY, matchId = matchId)
        } else {
            cricApiService.getMatchInfo(apiKey = BuildConfig.CRIC_API_KEY, matchId = matchId)
        }
        val rawDto = envelope.data
        if (!envelope.isSuccess || rawDto == null) {
            throw IllegalStateException("CricAPI Match Info Error: ${envelope.reason ?: "Match not found"}")
        }

        val enriched = rawDto.copy(
            tossWinner = rawDto.tossWinner.ifBlank { "Pending" },
            tossChoice = rawDto.tossChoice.ifBlank { "Pending" },
            score = rawDto.score.map { s -> s.copy(crr = s.computedRunRate) }
        )
        cache.set(cacheKey, enriched, 25L)
        enriched
    }

    suspend fun fetchCricketMatchScorecardJson(matchId: String): JSONObject {
        if (!isCricApiConfigured()) {
            return JSONObject().apply {
                put("error", "CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
            }
        }
        return try {
            val dto = fetchCricketMatchScorecard(matchId, includeFullScorecard = false)
            JSONObject(ApiJson.lenient.encodeToString(dto))
        } catch (e: Exception) {
            JSONObject().apply { put("error", e.message ?: "CricAPI scorecard error") }
        }
    }

    suspend fun fetchPlayerStatistics(playerId: String): PlayerProfileDto =
        withContext(ioDispatcher) {
            if (!isCricApiConfigured()) {
                throw IllegalStateException("CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
            }
            val cacheKey = "pitchedge:cricapi:players_info:$playerId"
            cache.get<PlayerProfileDto>(cacheKey)?.let { return@withContext it }

            val envelope = cricApiService.getPlayerInfo(
                apiKey = BuildConfig.CRIC_API_KEY,
                playerId = playerId
            )
            val player = envelope.data
            if (!envelope.isSuccess || player == null) {
                throw IllegalStateException("CricAPI Player Info Error: ${envelope.reason ?: "Player not found"}")
            }
            cache.set(cacheKey, player, 300L)
            player
        }

    suspend fun fetchMatchSquads(matchId: String): List<MatchSquadTeamDto> =
        withContext(ioDispatcher) {
            if (!isCricApiConfigured()) {
                throw IllegalStateException("CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
            }
            val cacheKey = "pitchedge:cricapi:match_squad:$matchId"
            cache.get<List<MatchSquadTeamDto>>(cacheKey)?.let { return@withContext it }

            val envelope = cricApiService.getMatchSquad(
                apiKey = BuildConfig.CRIC_API_KEY,
                matchId = matchId
            )
            if (!envelope.isSuccess) {
                throw IllegalStateException("CricAPI Squad Error: ${envelope.reason ?: "Squad not found"}")
            }
            val squads = envelope.data.orEmpty()
            cache.set(cacheKey, squads, 120L)
            squads
        }

    suspend fun fetchCricketSportKeys(): CricketSportKeysResponse =
        withContext(ioDispatcher) {
            if (!isOddsApiConfigured()) {
                throw IllegalStateException("ODDS_API_KEY is not set. Add ODDS_API_KEY in the AI Studio Secrets panel.")
            }
            val cacheKey = "pitchedge:oddsapi:cricket_sports:v2"
            cache.get<List<OddsSportKeyDto>>(cacheKey)?.let { cached ->
                return@withContext CricketSportKeysResponse(sports = cached)
            }

            val allSports = oddsApiService.getSports(apiKey = BuildConfig.ODDS_API_KEY)
            val cricketSports = allSports.filter {
                it.group.equals("cricket", ignoreCase = true) && it.active
            }
            cache.set(cacheKey, cricketSports, 3600L)
            CricketSportKeysResponse(sports = cricketSports)
        }

    suspend fun fetchCricketSportKeysJson(): JSONObject {
        if (!isOddsApiConfigured()) {
            return JSONObject().apply {
                put("error", "ODDS_API_KEY is not set. Add ODDS_API_KEY in the AI Studio Secrets panel.")
            }
        }
        return try {
            val response = fetchCricketSportKeys()
            JSONObject(ApiJson.lenient.encodeToString(response))
        } catch (e: Exception) {
            JSONObject().apply { put("error", e.message ?: "Odds API sports fetch failed") }
        }
    }

    suspend fun fetchLiveMatchOdds(
        sportKey: String,
        teamQuery: String? = null
    ): AnalyzedOddsResponse = withContext(ioDispatcher) {
        if (!isOddsApiConfigured()) {
            throw IllegalStateException("ODDS_API_KEY is not set. Add ODDS_API_KEY in the AI Studio Secrets panel.")
        }

        val cacheKey = "pitchedge:oddsapi:odds_v3:$sportKey"
        var events = cache.get<List<OddsEventDto>>(cacheKey)

        if (events == null) {
            events = try {
                oddsApiService.getSportOdds(
                    sportKey = sportKey,
                    apiKey = BuildConfig.ODDS_API_KEY,
                    regions = "uk,eu,au",
                    markets = "h2h,totals,spreads",
                    oddsFormat = "decimal"
                )
            } catch (e: HttpException) {
                if (e.code() == 422) {
                    oddsApiService.getSportOdds(
                        sportKey = sportKey,
                        apiKey = BuildConfig.ODDS_API_KEY,
                        regions = "uk,eu,au",
                        markets = "h2h",
                        oddsFormat = "decimal"
                    )
                } else {
                    throw e
                }
            }
            cache.set(cacheKey, events, 30L)
        }

        val q = teamQuery?.trim()?.lowercase(Locale.US).orEmpty()
        val analyzedEvents: List<AnalyzedEventOdds> = events
            .filter { event ->
                q.isEmpty() ||
                    event.homeTeam.lowercase(Locale.US).contains(q) ||
                    event.awayTeam.lowercase(Locale.US).contains(q)
            }
            .map { it.toAnalyzedEventOdds() }

        AnalyzedOddsResponse(
            sportKey = sportKey,
            eventsFound = analyzedEvents.size,
            events = analyzedEvents
        )
    }

    suspend fun fetchLiveMatchOddsJson(sportKey: String, teamQuery: String? = null): JSONObject {
        if (!isOddsApiConfigured()) {
            return JSONObject().apply {
                put("error", "ODDS_API_KEY is not set. Add ODDS_API_KEY in the AI Studio Secrets panel.")
            }
        }
        return try {
            val analyzed = fetchLiveMatchOdds(sportKey, teamQuery)
            JSONObject(ApiJson.lenient.encodeToString(analyzed))
        } catch (e: Exception) {
            JSONObject().apply { put("error", e.message ?: "Odds API fetch failed") }
        }
    }

    suspend fun fetchCricApiSeriesList(searchQuery: String): List<CricApiSeriesDto> =
        withContext(ioDispatcher) {
            if (!isCricApiConfigured()) {
                throw IllegalStateException("CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
            }
            val cacheKey = "pitchedge:cricapi:series:v2:${searchQuery.lowercase(Locale.US)}"
            cache.get<List<CricApiSeriesDto>>(cacheKey)?.let { return@withContext it }

            val envelope = cricApiService.getSeries(
                apiKey = BuildConfig.CRIC_API_KEY,
                offset = 0,
                search = searchQuery
            )
            if (!envelope.isSuccess) {
                throw IllegalStateException("CricAPI Series Error: ${envelope.reason ?: "Failed to fetch series"}")
            }
            val list = envelope.data.orEmpty()
            cache.set(cacheKey, list, 300L)
            list
        }

    suspend fun fetchCricApiSeriesListJson(searchQuery: String): JSONObject =
        withContext(ioDispatcher) {
            if (!isCricApiConfigured()) {
                return@withContext JSONObject().apply {
                    put("error", "CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
                }
            }
            try {
                val list = fetchCricApiSeriesList(searchQuery)
                JSONObject().apply {
                    put("status", "success")
                    put("data", org.json.JSONArray(ApiJson.lenient.encodeToString(list)))
                }
            } catch (e: Exception) {
                JSONObject().apply { put("error", e.message ?: "CricAPI series fetch failed") }
            }
        }

    suspend fun fetchCricApiSeriesPoints(seriesId: String): List<CricApiSeriesPointsRowDto> =
        withContext(ioDispatcher) {
            if (!isCricApiConfigured()) {
                throw IllegalStateException("CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
            }
            val cacheKey = "pitchedge:cricapi:series_points:v2:$seriesId"
            cache.get<List<CricApiSeriesPointsRowDto>>(cacheKey)?.let { return@withContext it }

            val envelope = cricApiService.getSeriesPoints(
                apiKey = BuildConfig.CRIC_API_KEY,
                seriesId = seriesId
            )
            if (!envelope.isSuccess) {
                throw IllegalStateException("CricAPI Series Points Error: ${envelope.reason ?: "Failed to fetch points"}")
            }
            val rows = envelope.data.orEmpty()
            cache.set(cacheKey, rows, 120L)
            rows
        }

    suspend fun fetchCricApiSeriesPointsJson(seriesId: String): JSONObject =
        withContext(ioDispatcher) {
            if (!isCricApiConfigured()) {
                return@withContext JSONObject().apply {
                    put("error", "CRIC_API_KEY is not set. Add CRIC_API_KEY in the AI Studio Secrets panel.")
                }
            }
            try {
                val rows = fetchCricApiSeriesPoints(seriesId)
                JSONObject().apply {
                    put("status", "success")
                    put("data", org.json.JSONArray(ApiJson.lenient.encodeToString(rows)))
                }
            } catch (e: Exception) {
                JSONObject().apply { put("error", e.message ?: "CricAPI series points fetch failed") }
            }
        }

    suspend fun fetchLiveCricketSlateForUi(): List<FixtureOverview> {
        val response = fetchLiveCricketMatches(null)
        return response.matches.take(8).map { m ->
            val home = m.teams.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "Team A"
            val away = m.teams.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "Team B"
            val firstScore = m.score.firstOrNull()
            val scoreText = if (firstScore != null) {
                "${firstScore.runs}/${firstScore.wickets} (${firstScore.overs} ov)"
            } else {
                m.status.ifBlank { "Scheduled" }
            }
            val started = m.matchStarted
            val ended = m.matchEnded
            val matchType = m.matchType.ifBlank { "T20" }.uppercase(Locale.US)
            val format = when {
                matchType in setOf("TEST", "ODI", "T20I") -> FormatFilter.INTERNATIONAL
                m.name.contains("League", ignoreCase = true) ||
                    m.name.contains("ILT20", ignoreCase = true) ||
                    m.name.contains("IPL", ignoreCase = true) ||
                    m.name.contains("BBL", ignoreCase = true) -> FormatFilter.T20_LEAGUES
                else -> FormatFilter.DOMESTIC
            }

            FixtureOverview(
                id = "cricapi_${m.id}",
                sport = SportFilter.CRICKET,
                format = format,
                status = if (started && !ended) StatusFilter.LIVE_NOW else StatusFilter.UPCOMING_TODAY,
                hasPositiveEdge = false,
                leagueTitle = "$matchType • CricAPI Live",
                matchLabel = "${home.take(3).uppercase()} vs ${away.take(3).uppercase()}",
                homeCode = home.take(3).uppercase(),
                homeName = home,
                homeRecord = m.status,
                awayCode = away.take(3).uppercase(),
                awayName = away,
                awayRecord = m.dateTimeGMT.take(16),
                liveScoreOrTimer = scoreText,
                oversOrPeriod = m.status,
                venue = m.venue.ifBlank { "Venue TBA" },
                weatherLine = "Run PitchEdge Agent for live conditions & odds",
                tossStatus = if (started) "Live In Progress" else "Pre-Toss",
                homeModelPct = 50.0f,
                awayModelPct = 50.0f,
                marketHomePct = 50.0f,
                marketAwayPct = 50.0f,
                topEdgeBadge = if (started && !ended) "LIVE" else "CRICAPI",
                bestPickTitle = "Tap 'Run PitchEdge Agent' to fetch Odds API vig-free lines & scorecard edge",
                bestPickFairOdds = 2.00,
                bestPickMarketOdds = 2.00,
                bestPickConfidence = "Pending Agent",
                bestPickUnits = 1.0f,
                bestPickRiskWarning = "Live CricAPI fixture loaded. Trigger PitchEdge Agent to check The Odds API markets and calculate +EV.",
                defaultFirstInnScore = 165,
                defaultSecondInnScore = 158,
                scoreUnitLabel = "Runs",
                bookmakerRows = listOf(
                    BookmakerOddsRow("Odds API", 2.00, 2.00, 0.0)
                )
            )
        }
    }
}
