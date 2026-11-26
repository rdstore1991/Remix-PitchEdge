package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.CricApiService
import com.example.data.api.NetworkClient
import com.example.data.api.OddsApiService
import com.example.model.AnalyzedEventOdds
import com.example.model.BookmakerOddsRow
import com.example.model.CricketMatchScorecardDto
import com.example.model.CricketMatchSummaryDto
import com.example.model.FixtureMatchStatus
import com.example.model.FixtureOverview
import com.example.model.FormatFilter
import com.example.model.MarketEdgeItem
import com.example.model.OddsEventDto
import com.example.model.PointsTableRow
import com.example.model.ProbabilityMath
import com.example.model.SeriesBundle
import com.example.model.SeriesLeague
import com.example.model.SportFilter
import com.example.model.StatusFilter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Locale

private const val TAG = "CricketMatchRepository"

/**
 * Combined telemetry snapshot for a cricket fixture, uniting CricAPI live scorecards,
 * Odds API vig-stripped market probabilities, and PitchEdge baseline/model edge items.
 */
data class MatchTelemetryBundle(
    val fixture: FixtureOverview,
    val scorecard: CricketMatchScorecardDto? = null,
    val analyzedOdds: AnalyzedEventOdds? = null,
    val synchronizedEdges: List<MarketEdgeItem> = emptyList(),
    val isLiveTelemetry: Boolean = false,
    val telemetrySourceNote: String = "Baseline Model"
)

/**
 * Normalizes team codes, franchise names, and tournament keys across CricAPI, The Odds API,
 * and local PitchEdge ILT20 / WBBL baseline datasets so telemetry stays consistent.
 */
object LeagueTelemetryNormalizer {
    private val ilt20TeamCodes = mapOf(
        "dubai capitals" to "DC",
        "desert vipers" to "DV",
        "gulf giants" to "GG",
        "mi emirates" to "MIE",
        "abu dhabi knight riders" to "ADKR",
        "sharjah warriorz" to "SW",
        "sharjah warriors" to "SW"
    )

    private val wbblTeamCodes = mapOf(
        "melbourne renegades" to "MR",
        "melbourne renegades women" to "MR",
        "sydney sixers" to "SS",
        "sydney sixers women" to "SS",
        "brisbane heat" to "BH",
        "brisbane heat women" to "BH",
        "perth scorchers" to "PS",
        "perth scorchers women" to "PS",
        "adelaide strikers" to "AS",
        "adelaide strikers women" to "AS",
        "hobart hurricanes" to "HH",
        "hobart hurricanes women" to "HH",
        "melbourne stars" to "MS",
        "melbourne stars women" to "MS",
        "sydney thunder" to "ST",
        "sydney thunder women" to "ST"
    )

    fun canonicalTeamCode(teamName: String): String {
        val normalized = teamName.trim().lowercase(Locale.US)
        ilt20TeamCodes[normalized]?.let { return it }
        wbblTeamCodes[normalized]?.let { return it }
        ilt20TeamCodes.entries.firstOrNull { normalized.contains(it.key) }?.let { return it.value }
        wbblTeamCodes.entries.firstOrNull { normalized.contains(it.key) }?.let { return it.value }
        val words = teamName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return if (words.size >= 2) {
            words.take(3).joinToString("") { it.first().uppercaseChar().toString() }
        } else {
            teamName.trim().take(3).uppercase(Locale.US).ifBlank { "TBA" }
        }
    }

    fun detectSeriesLeague(nameOrTitle: String, teams: List<String> = emptyList()): SeriesLeague? {
        val haystack = (listOf(nameOrTitle) + teams).joinToString(" ").lowercase(Locale.US)
        if (haystack.contains("ilt20") || ilt20TeamCodes.keys.any { haystack.contains(it) }) {
            return SeriesLeague.ILT20_2026
        }
        if (haystack.contains("wbbl") ||
            haystack.contains("women's big bash") ||
            haystack.contains("womens big bash") ||
            wbblTeamCodes.keys.any { haystack.contains(it) }
        ) {
            return SeriesLeague.WBBL_2026
        }
        return null
    }

    fun teamsMatch(fixture: FixtureOverview, candidateTeams: List<String>): Boolean {
        if (candidateTeams.isEmpty()) return false
        val codes = candidateTeams.map(::canonicalTeamCode).toSet()
        if (fixture.homeCode.uppercase(Locale.US) in codes &&
            fixture.awayCode.uppercase(Locale.US) in codes
        ) {
            return true
        }
        val namesLower = candidateTeams.map { it.lowercase(Locale.US) }
        val homeHit = namesLower.any {
            it.contains(fixture.homeName.lowercase(Locale.US)) ||
                fixture.homeName.lowercase(Locale.US).contains(it)
        }
        val awayHit = namesLower.any {
            it.contains(fixture.awayName.lowercase(Locale.US)) ||
                fixture.awayName.lowercase(Locale.US).contains(it)
        }
        return homeHit && awayHit
    }
}

/**
 * Repository orchestrating Retrofit API services (CricAPI & The Odds API v4),
 * TTL caching, and curated ILT20 / WBBL match telemetry.
 */
class CricketMatchRepository(
    private val matchRepository: MatchRepository,
    private val seriesRepository: SeriesRepository = SeriesRepository(),
    val sportsDataService: LiveSportsDataService = LiveSportsDataService(),
    private val cricApiService: CricApiService = NetworkClient.cricApiService,
    private val oddsApiService: OddsApiService = NetworkClient.oddsApiService,
    private val cache: MemoryTtlCache = MemoryTtlCache(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(context: Context) : this(
        matchRepository = MatchRepository(context.applicationContext)
    )

    val baselineFixtures: List<FixtureOverview>
        get() = matchRepository.fixturesSlate

    val userPrefsFlow get() = matchRepository.userPrefsFlow

    suspend fun setDarkTheme(enabled: Boolean) = matchRepository.setDarkTheme(enabled)
    suspend fun setTossScenario(scenario: com.example.model.TossScenario) = matchRepository.setTossScenario(scenario)
    suspend fun setUnitSize(unitDollars: Double) = matchRepository.setUnitSize(unitDollars)
    suspend fun setKellyMultiplier(mult: Double) = matchRepository.setKellyMultiplier(mult)
    suspend fun updateChecklist(checklist: com.example.model.PreTossChecklist) = matchRepository.updateChecklist(checklist)

    fun baselineVenueScores() = matchRepository.season5VenueScores
    fun baselineDcXi() = matchRepository.dcProjectedXi
    fun baselineDvXi() = matchRepository.dvProjectedXi
    fun baselineMarketEdges(fixture: FixtureOverview) = matchRepository.marketEdgesForFixture(fixture)
    fun baselinePhaseMatchups() = matchRepository.phaseMatchups
    fun baselinePlayerLeverages() = matchRepository.playerLeverages
    fun baselinePrimarySelection() = matchRepository.primarySelection
    fun baselineSecondaryLeans() = matchRepository.secondaryLeans
    fun baselineSeriesBundles(): Map<SeriesLeague, SeriesBundle> = mapOf(
        SeriesLeague.ILT20_2026 to seriesRepository.getSeriesBundle(SeriesLeague.ILT20_2026),
        SeriesLeague.WBBL_2026 to seriesRepository.getSeriesBundle(SeriesLeague.WBBL_2026)
    )

    /**
     * Fetches live CricAPI cricket fixtures and merges them consistently with the curated
     * ILT20 and WBBL baseline slate without dropping model projections or duplicate fixtures.
     */
    suspend fun fetchSynchronizedCricketSlate(
        forceRefresh: Boolean = false
    ): List<FixtureOverview> = withContext(ioDispatcher) {
        val baseline = matchRepository.fixturesSlate
        if (!sportsDataService.isCricApiConfigured()) {
            return@withContext baseline
        }

        val cacheKey = "pitchedge:repo:live_matches"
        val liveMatches: List<CricketMatchSummaryDto> = if (!forceRefresh) {
            cache.get<List<CricketMatchSummaryDto>>(cacheKey) ?: fetchCurrentMatchesFromApi(cacheKey)
        } else {
            fetchCurrentMatchesFromApi(cacheKey)
        }

        if (liveMatches.isEmpty()) {
            return@withContext baseline
        }

        val matchedLiveIds = mutableSetOf<String>()
        val mergedBaseline = baseline.map { baseFixture ->
            if (baseFixture.sport != SportFilter.CRICKET) {
                baseFixture
            } else {
                val match = liveMatches.firstOrNull { m ->
                    LeagueTelemetryNormalizer.teamsMatch(baseFixture, m.teams)
                }
                if (match != null) {
                    matchedLiveIds.add(match.id)
                    mergeLiveSummaryIntoFixture(baseFixture, match)
                } else {
                    baseFixture
                }
            }
        }

        val extraLiveFixtures = liveMatches
            .filterNot { it.id in matchedLiveIds }
            .take(8)
            .map(::mapSummaryToFixtureOverview)

        mergedBaseline + extraLiveFixtures
    }

    /**
     * Orchestrates parallel fetching of CricAPI live scorecard and The Odds API v4 markets
     * for a given fixture, ensuring consistent probabilities and edge metrics.
     */
    suspend fun fetchMatchTelemetry(
        fixture: FixtureOverview,
        sportKeyHint: String? = null
    ): MatchTelemetryBundle = withContext(ioDispatcher) {
        val baseEdges = matchRepository.marketEdgesForFixture(fixture)
        val rawMatchId = fixture.id.removePrefix("cricapi_").takeIf {
            fixture.id.startsWith("cricapi_")
        }

        coroutineScope {
            val scorecardDeferred = async {
                if (!sportsDataService.isCricApiConfigured()) return@async null
                runCatching {
                    val targetId = rawMatchId ?: findMatchingCricApiId(fixture)
                    if (targetId != null) {
                        sportsDataService.fetchCricketMatchScorecard(
                            matchId = targetId,
                            includeFullScorecard = true
                        )
                    } else null
                }.onFailure { if (it is CancellationException) throw it else Log.w(TAG, "Scorecard fetch skipped", it) }
                    .getOrNull()
            }

            val oddsDeferred = async {
                if (!sportsDataService.isOddsApiConfigured()) return@async null
                runCatching {
                    val sportKey = sportKeyHint ?: resolveOddsSportKey(fixture) ?: return@runCatching null
                    val oddsResp = fetchOddsFromApi(sportKey)
                    oddsResp.firstOrNull { event ->
                        LeagueTelemetryNormalizer.teamsMatch(
                            fixture,
                            listOf(event.homeTeam, event.awayTeam)
                        )
                    }?.toAnalyzedEventOdds()
                }.onFailure { if (it is CancellationException) throw it else Log.w(TAG, "Odds fetch skipped", it) }
                    .getOrNull()
            }

            val scorecard = scorecardDeferred.await()
            val analyzedOdds = oddsDeferred.await()

            var updatedFixture = fixture
            if (scorecard != null) {
                updatedFixture = mergeScorecardIntoFixture(updatedFixture, scorecard)
            }
            val synchronizedEdges = if (analyzedOdds != null) {
                updatedFixture = mergeOddsIntoFixture(updatedFixture, analyzedOdds)
                recomputeEdgesWithLiveOdds(baseEdges, updatedFixture, analyzedOdds)
            } else {
                baseEdges
            }

            val sourceNote = buildList {
                if (scorecard != null) add("CricAPI Live Scorecard")
                if (analyzedOdds != null) add("Odds API v4 (${analyzedOdds.bookmakerCount} books)")
            }.joinToString(" + ").ifBlank { "PitchEdge Baseline Model" }

            MatchTelemetryBundle(
                fixture = updatedFixture,
                scorecard = scorecard,
                analyzedOdds = analyzedOdds,
                synchronizedEdges = synchronizedEdges,
                isLiveTelemetry = scorecard != null || analyzedOdds != null,
                telemetrySourceNote = sourceNote
            )
        }
    }

    /**
     * Synchronizes ILT20 2026 or WBBL 2026 series standings and live fixture scores from CricAPI
     * with the curated SeriesBundle so points tables and match previews stay consistent.
     */
    suspend fun fetchConsistentSeriesBundle(league: SeriesLeague): SeriesBundle =
        withContext(ioDispatcher) {
            val baseBundle = seriesRepository.getSeriesBundle(league)
            if (!sportsDataService.isCricApiConfigured()) {
                return@withContext baseBundle
            }

            val query = when (league) {
                SeriesLeague.ILT20_2026 -> "ILT20"
                SeriesLeague.WBBL_2026 -> "Women's Big Bash"
            }

            val matchedSeries = runCatching {
                cricApiService.getSeries(apiKey = BuildConfig.CRIC_API_KEY, offset = 0, search = query)
                    .data
                    ?.firstOrNull()
            }.onFailure { if (it is CancellationException) throw it }
                .getOrNull()

            val apiPoints = if (matchedSeries != null && matchedSeries.id.isNotBlank()) {
                runCatching {
                    cricApiService.getSeriesPoints(
                        apiKey = BuildConfig.CRIC_API_KEY,
                        seriesId = matchedSeries.id
                    ).data.orEmpty()
                }.onFailure { if (it is CancellationException) throw it }
                    .getOrNull()
                    .orEmpty()
            } else {
                emptyList()
            }

            val liveMatches = runCatching {
                fetchCurrentMatchesFromApi("pitchedge:repo:live_matches")
                    .filter { LeagueTelemetryNormalizer.detectSeriesLeague(it.name, it.teams) == league }
            }.onFailure { if (it is CancellationException) throw it }
                .getOrNull()
                .orEmpty()

            val updatedPoints: List<PointsTableRow> = if (apiPoints.isNotEmpty()) {
                val nrrLookup = baseBundle.pointsTable.associateBy { it.teamCode.uppercase(Locale.US) }
                apiPoints
                    .sortedWith(compareByDescending<com.example.model.CricApiSeriesPointsRowDto> { it.points }.thenByDescending { it.wins })
                    .mapIndexed { idx, row ->
                        val code = row.shortname.ifBlank {
                            LeagueTelemetryNormalizer.canonicalTeamCode(row.teamname)
                        }.uppercase(Locale.US)
                        val baselineRow = nrrLookup[code]
                        PointsTableRow(
                            rank = idx + 1,
                            teamCode = code,
                            teamName = row.teamname.ifBlank { baselineRow?.teamName ?: code },
                            played = row.matches,
                            won = row.wins,
                            lost = row.loss,
                            noResult = row.nr + row.ties,
                            points = row.points,
                            nrr = baselineRow?.nrr ?: "—",
                            recentForm = baselineRow?.recentForm ?: "—",
                            qualifierZone = idx < 4
                        )
                    }
            } else {
                baseBundle.pointsTable
            }

            val updatedFixtures = if (liveMatches.isNotEmpty()) {
                baseBundle.fixtures.map { item ->
                    val match = liveMatches.firstOrNull { m ->
                        val codes = m.teams.map(LeagueTelemetryNormalizer::canonicalTeamCode).toSet()
                        item.teamACode.uppercase(Locale.US) in codes &&
                            item.teamBCode.uppercase(Locale.US) in codes
                    }
                    if (match == null) {
                        item
                    } else {
                        val liveStatus = when {
                            match.matchEnded -> FixtureMatchStatus.COMPLETED
                            match.matchStarted -> FixtureMatchStatus.LIVE
                            else -> item.status
                        }
                        val scoreSummary = match.score.joinToString(" • ") { s ->
                            "${s.inning.substringBefore(" Inning")}: ${s.runs}/${s.wickets} (${s.overs} ov)"
                        }.ifBlank { match.status.ifBlank { item.scoreOrPreview } }

                        item.copy(
                            status = liveStatus,
                            scoreOrPreview = scoreSummary
                        )
                    }
                }
            } else {
                baseBundle.fixtures
            }

            val bannerNote = if (matchedSeries != null) {
                "CricAPI Synced: ${matchedSeries.name} • ${baseBundle.statusBanner}"
            } else {
                baseBundle.statusBanner
            }

            baseBundle.copy(
                statusBanner = bannerNote,
                fixtures = updatedFixtures,
                pointsTable = updatedPoints
            )
        }

    private suspend fun fetchCurrentMatchesFromApi(cacheKey: String): List<CricketMatchSummaryDto> {
        val envelope = cricApiService.getCurrentMatches(apiKey = BuildConfig.CRIC_API_KEY, offset = 0)
        if (!envelope.isSuccess) {
            throw IllegalStateException("CricAPI Error: ${envelope.reason ?: "Failed to fetch matches"}")
        }
        val data = envelope.data.orEmpty()
        cache.set(cacheKey, data, 30L)
        return data
    }

    private suspend fun findMatchingCricApiId(fixture: FixtureOverview): String? {
        val matches = cache.get<List<CricketMatchSummaryDto>>("pitchedge:repo:live_matches")
            ?: fetchCurrentMatchesFromApi("pitchedge:repo:live_matches")
        return matches.firstOrNull { LeagueTelemetryNormalizer.teamsMatch(fixture, it.teams) }?.id
    }

    private suspend fun resolveOddsSportKey(fixture: FixtureOverview): String? {
        val sports = oddsApiService.getSports(apiKey = BuildConfig.ODDS_API_KEY)
            .filter { it.group.equals("cricket", ignoreCase = true) && it.active }
        if (sports.isEmpty()) return null

        val league = LeagueTelemetryNormalizer.detectSeriesLeague(
            fixture.leagueTitle,
            listOf(fixture.homeName, fixture.awayName)
        )
        return when (league) {
            SeriesLeague.WBBL_2026 -> sports.firstOrNull {
                it.key.contains("big_bash", ignoreCase = true) ||
                    it.title.contains("WBBL", ignoreCase = true)
            }?.key
            SeriesLeague.ILT20_2026 -> sports.firstOrNull {
                it.key.contains("ilt20", ignoreCase = true) ||
                    it.title.contains("ILT20", ignoreCase = true)
            }?.key
            null -> null
        } ?: sports.firstOrNull {
            it.key.contains("t20", ignoreCase = true) || it.key.contains("international", ignoreCase = true)
        }?.key ?: sports.firstOrNull()?.key
    }

    private suspend fun fetchOddsFromApi(sportKey: String): List<OddsEventDto> {
        val cacheKey = "pitchedge:repo:odds:$sportKey"
        cache.get<List<OddsEventDto>>(cacheKey)?.let { return it }
        val events = try {
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
        return events
    }

    private fun mergeLiveSummaryIntoFixture(
        base: FixtureOverview,
        live: CricketMatchSummaryDto
    ): FixtureOverview {
        val firstScore = live.score.firstOrNull()
        val liveScore = if (firstScore != null) {
            "${firstScore.runs}/${firstScore.wickets} (${firstScore.overs} ov • CRR ${firstScore.computedRunRate})"
        } else {
            live.status.ifBlank { base.liveScoreOrTimer }
        }
        val status = when {
            live.matchStarted && !live.matchEnded -> StatusFilter.LIVE_NOW
            else -> base.status
        }
        return base.copy(
            status = status,
            liveScoreOrTimer = liveScore,
            oversOrPeriod = live.status.ifBlank { base.oversOrPeriod },
            venue = live.venue.ifBlank { base.venue },
            topEdgeBadge = if (live.matchStarted && !live.matchEnded) "LIVE • ${base.topEdgeBadge}" else base.topEdgeBadge
        )
    }

    private fun mergeScorecardIntoFixture(
        base: FixtureOverview,
        scorecard: CricketMatchScorecardDto
    ): FixtureOverview {
        val latest = scorecard.score.lastOrNull()
        val liveScore = if (latest != null) {
            "${latest.runs}/${latest.wickets} (${latest.overs} ov • CRR ${latest.computedRunRate})"
        } else {
            scorecard.status.ifBlank { base.liveScoreOrTimer }
        }
        val tossText = if (!scorecard.tossWinner.equals("Pending", ignoreCase = true)) {
            "${scorecard.tossWinner} won toss (${scorecard.tossChoice})"
        } else {
            base.tossStatus
        }
        return base.copy(
            status = if (scorecard.matchStarted && !scorecard.matchEnded) StatusFilter.LIVE_NOW else base.status,
            liveScoreOrTimer = liveScore,
            oversOrPeriod = scorecard.status.ifBlank { base.oversOrPeriod },
            tossStatus = tossText,
            venue = scorecard.venue.ifBlank { base.venue }
        )
    }

    private fun mergeOddsIntoFixture(
        base: FixtureOverview,
        odds: AnalyzedEventOdds
    ): FixtureOverview {
        val homeQuote = odds.marketProbabilities.firstOrNull {
            LeagueTelemetryNormalizer.canonicalTeamCode(it.selection) == base.homeCode ||
                it.selection.contains(base.homeName, ignoreCase = true)
        }
        val awayQuote = odds.marketProbabilities.firstOrNull {
            LeagueTelemetryNormalizer.canonicalTeamCode(it.selection) == base.awayCode ||
                it.selection.contains(base.awayName, ignoreCase = true)
        }
        if (homeQuote == null || awayQuote == null) return base

        val liveRow = BookmakerOddsRow(
            bookmaker = "${homeQuote.bestBookmaker}/${awayQuote.bestBookmaker}",
            homeOdds = homeQuote.bestBackOdds,
            awayOdds = awayQuote.bestBackOdds,
            overroundPct = odds.marketOverroundPercent,
            isBestHome = true,
            isBestAway = true
        )

        return base.copy(
            marketHomePct = homeQuote.trueVigFreeMarketPercent.toFloat(),
            marketAwayPct = awayQuote.trueVigFreeMarketPercent.toFloat(),
            bookmakerRows = listOf(liveRow) + base.bookmakerRows.filterNot { it.bookmaker == liveRow.bookmaker }
        )
    }

    private fun recomputeEdgesWithLiveOdds(
        baseEdges: List<MarketEdgeItem>,
        fixture: FixtureOverview,
        odds: AnalyzedEventOdds
    ): List<MarketEdgeItem> {
        return baseEdges.map { item ->
            val matchedQuote = odds.marketProbabilities.firstOrNull { q ->
                item.marketName.contains(q.selection, ignoreCase = true) ||
                    (item.id.contains("dc_", ignoreCase = true) && q.selection.contains("Dubai", ignoreCase = true)) ||
                    (item.id.contains("dv_", ignoreCase = true) && q.selection.contains("Desert", ignoreCase = true))
            }
            if (matchedQuote == null) {
                item
            } else {
                val edge = ProbabilityMath.round2(item.modelPct - matchedQuote.trueVigFreeMarketPercent)
                val evPct = matchedQuote.expectedValuePercent(item.modelPct)
                val evSign = if (evPct > 0.0) "+" else ""
                item.copy(
                    bestOdds = matchedQuote.bestBackOdds,
                    bookmakerNote = "${matchedQuote.bestBookmaker} (Live)",
                    marketImpliedPct = matchedQuote.trueVigFreeMarketPercent,
                    rawImpliedNote = "${matchedQuote.rawImpliedPercent}% raw • ${odds.marketOverroundPercent}% vig",
                    edgePct = edge,
                    evNote = "$evSign$evPct% EV at ${matchedQuote.bestBackOdds}",
                    statusBadge = when {
                        edge >= 3.0 -> "PLAYABLE +EV"
                        edge > 0.0 -> "CONDITIONAL +EV"
                        else -> "SKIP (-EV)"
                    }
                )
            }
        }
    }

    private fun mapSummaryToFixtureOverview(m: CricketMatchSummaryDto): FixtureOverview {
        val home = m.teams.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "Team A"
        val away = m.teams.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "Team B"
        val homeCode = LeagueTelemetryNormalizer.canonicalTeamCode(home)
        val awayCode = LeagueTelemetryNormalizer.canonicalTeamCode(away)
        val firstScore = m.score.firstOrNull()
        val scoreText = if (firstScore != null) {
            "${firstScore.runs}/${firstScore.wickets} (${firstScore.overs} ov)"
        } else {
            m.status.ifBlank { "Scheduled" }
        }
        val matchType = m.matchType.ifBlank { "T20" }.uppercase(Locale.US)
        val league = LeagueTelemetryNormalizer.detectSeriesLeague(m.name, m.teams)
        val format = when {
            league != null -> FormatFilter.T20_LEAGUES
            matchType in setOf("TEST", "ODI", "T20I") -> FormatFilter.INTERNATIONAL
            m.name.contains("League", ignoreCase = true) ||
                m.name.contains("IPL", ignoreCase = true) ||
                m.name.contains("BBL", ignoreCase = true) -> FormatFilter.T20_LEAGUES
            else -> FormatFilter.DOMESTIC
        }
        val leagueTitle = when (league) {
            SeriesLeague.ILT20_2026 -> "DP World ILT20 2026 • Live"
            SeriesLeague.WBBL_2026 -> "WBBL 2026 • Live"
            null -> "$matchType • CricAPI Live"
        }

        return FixtureOverview(
            id = "cricapi_${m.id}",
            sport = SportFilter.CRICKET,
            format = format,
            status = if (m.matchStarted && !m.matchEnded) StatusFilter.LIVE_NOW else StatusFilter.UPCOMING_TODAY,
            hasPositiveEdge = false,
            leagueTitle = leagueTitle,
            matchLabel = "$homeCode vs $awayCode",
            homeCode = homeCode,
            homeName = home,
            homeRecord = m.status,
            awayCode = awayCode,
            awayName = away,
            awayRecord = m.dateTimeGMT.take(16),
            liveScoreOrTimer = scoreText,
            oversOrPeriod = m.status,
            venue = m.venue.ifBlank { "Venue TBA" },
            weatherLine = "Run PitchEdge Agent for live conditions & odds",
            tossStatus = if (m.matchStarted) "Live In Progress" else "Pre-Toss",
            homeModelPct = 50.0f,
            awayModelPct = 50.0f,
            marketHomePct = 50.0f,
            marketAwayPct = 50.0f,
            topEdgeBadge = if (m.matchStarted && !m.matchEnded) "LIVE" else "CRICAPI",
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
