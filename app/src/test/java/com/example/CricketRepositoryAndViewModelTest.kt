package com.example

import android.app.Application
import android.content.Context
import com.example.data.CricketMatchRepository
import com.example.data.LeagueTelemetryNormalizer
import com.example.data.LiveSportsDataService
import com.example.data.MatchRepository
import com.example.data.MemoryTtlCache
import com.example.data.SeriesRepository
import com.example.data.api.CricApiService
import com.example.data.api.OddsApiService
import com.example.model.CricApiEnvelope
import com.example.model.CricApiSeriesDto
import com.example.model.CricApiSeriesPointsRowDto
import com.example.model.CricketMatchScorecardDto
import com.example.model.CricketMatchSummaryDto
import com.example.model.InningsScoreDto
import com.example.model.MatchSquadTeamDto
import com.example.model.OddsBookmakerDto
import com.example.model.OddsEventDto
import com.example.model.OddsMarketDto
import com.example.model.OddsOutcomeDto
import com.example.model.OddsSportKeyDto
import com.example.model.PlayerProfileDto
import com.example.model.SeriesLeague
import com.example.ui.AsyncUiState
import com.example.ui.BaseCricketMatchViewModel
import com.example.ui.BaseDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CricketRepositoryAndViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class StubApplication : Application() {
        override fun getApplicationContext(): Context = this
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun normalizerIdentifiesIlt20AndWbblTeamsConsistently() {
        assertEquals("DC", LeagueTelemetryNormalizer.canonicalTeamCode("Dubai Capitals"))
        assertEquals("DV", LeagueTelemetryNormalizer.canonicalTeamCode("Desert Vipers"))
        assertEquals("MR", LeagueTelemetryNormalizer.canonicalTeamCode("Melbourne Renegades Women"))
        assertEquals("SS", LeagueTelemetryNormalizer.canonicalTeamCode("Sydney Sixers Women"))

        assertEquals(
            SeriesLeague.ILT20_2026,
            LeagueTelemetryNormalizer.detectSeriesLeague(
                "DP World ILT20 Match 6",
                listOf("Dubai Capitals", "Desert Vipers")
            )
        )
        assertEquals(
            SeriesLeague.WBBL_2026,
            LeagueTelemetryNormalizer.detectSeriesLeague(
                "WBBL 2026",
                listOf("Melbourne Renegades Women", "Sydney Sixers Women")
            )
        )
    }

    @Test
    fun baseDataViewModelTransitionsThroughLoadingSuccessAndError() = runTest(testDispatcher) {
        val vm = object : BaseDataViewModel<AsyncUiState<String>>(AsyncUiState.Idle) {
            fun fetchOk() = launchAsync(
                key = "test",
                loadingMessage = "Loading...",
                onStateChange = { newState -> updateState { newState } }
            ) {
                "ILT20_OK"
            }

            fun fetchFail() = launchAsync(
                key = "test",
                loadingMessage = "Loading...",
                onStateChange = { newState -> updateState { newState } }
            ) {
                throw IllegalStateException("Network unavailable")
            }
        }

        assertTrue(vm.state.value is AsyncUiState.Idle)

        vm.fetchOk()
        advanceUntilIdle()
        val success = vm.state.value
        assertTrue(success is AsyncUiState.Success)
        assertEquals("ILT20_OK", success.dataOrNull)

        vm.fetchFail()
        advanceUntilIdle()
        val error = vm.state.value
        assertTrue(error is AsyncUiState.Error)
        assertEquals("Network unavailable", error.errorMessageOrNull)
    }

    @Test
    fun repositoryAndBaseCricketViewModelOrchestrateConsistentTelemetry() = runTest(testDispatcher) {
        val app = StubApplication()
        val fakeCricApi = FakeCricApiService()
        val fakeOddsApi = FakeOddsApiService()
        val cache = MemoryTtlCache()
        val repo = CricketMatchRepository(
            matchRepository = MatchRepository(app),
            seriesRepository = SeriesRepository(),
            sportsDataService = LiveSportsDataService(cache, fakeCricApi, fakeOddsApi, testDispatcher),
            cricApiService = fakeCricApi,
            oddsApiService = fakeOddsApi,
            cache = cache,
            ioDispatcher = testDispatcher
        )

        val vm = BaseCricketMatchViewModel(app, repo)
        val featured = repo.baselineFixtures.first { it.id == "ilt20_m6_dc_dv" }

        vm.loadMatchTelemetry(featured)
        advanceUntilIdle()

        val telemetryState = vm.telemetryAsyncState.value
        assertTrue(telemetryState is AsyncUiState.Success)
        val bundle = telemetryState.dataOrNull!!
        assertEquals("ilt20_m6_dc_dv", bundle.fixture.id)
        assertTrue(bundle.synchronizedEdges.isNotEmpty())

        // Verify baseline ILT20 & WBBL series bundles are consistent
        val bundles = repo.baselineSeriesBundles()
        assertTrue(bundles.containsKey(SeriesLeague.ILT20_2026))
        assertTrue(bundles.containsKey(SeriesLeague.WBBL_2026))
        assertTrue(bundles[SeriesLeague.ILT20_2026]!!.pointsTable.isNotEmpty())
        assertTrue(bundles[SeriesLeague.WBBL_2026]!!.pointsTable.isNotEmpty())
    }

    private class FakeCricApiService : CricApiService {
        override suspend fun getCurrentMatches(apiKey: String, offset: Int) = CricApiEnvelope(
            status = "success",
            data = listOf(
                CricketMatchSummaryDto(
                    id = "cric-m6",
                    name = "Dubai Capitals vs Desert Vipers, 6th Match",
                    matchType = "t20",
                    status = "Dubai Capitals opt to bowl",
                    venue = "Dubai International Cricket Stadium",
                    teams = listOf("Dubai Capitals", "Desert Vipers"),
                    score = listOf(InningsScoreDto(runs = 72, wickets = 1, overs = 8.3, inning = "Desert Vipers Inning 1")),
                    matchStarted = true,
                    matchEnded = false
                )
            )
        )

        override suspend fun getMatchInfo(apiKey: String, matchId: String) =
            getMatchScorecard(apiKey, matchId)

        override suspend fun getMatchScorecard(apiKey: String, matchId: String) = CricApiEnvelope(
            status = "success",
            data = CricketMatchScorecardDto(
                id = matchId,
                name = "Dubai Capitals vs Desert Vipers",
                matchType = "t20",
                status = "Desert Vipers 72/1 (8.3 ov)",
                venue = "Dubai International Cricket Stadium",
                teams = listOf("Dubai Capitals", "Desert Vipers"),
                tossWinner = "Dubai Capitals",
                tossChoice = "bowl",
                score = listOf(InningsScoreDto(runs = 72, wickets = 1, overs = 8.3, inning = "Desert Vipers Inning 1")),
                matchStarted = true,
                matchEnded = false
            )
        )

        override suspend fun getPlayerInfo(apiKey: String, playerId: String) =
            CricApiEnvelope<PlayerProfileDto>(status = "success", data = PlayerProfileDto(id = playerId))

        override suspend fun getMatchSquad(apiKey: String, matchId: String) =
            CricApiEnvelope<List<MatchSquadTeamDto>>(status = "success", data = emptyList())

        override suspend fun getSeries(apiKey: String, offset: Int, search: String) =
            CricApiEnvelope(
                status = "success",
                data = listOf(CricApiSeriesDto(id = "s-ilt20", name = "DP World ILT20 2026"))
            )

        override suspend fun getSeriesPoints(apiKey: String, seriesId: String) =
            CricApiEnvelope(
                status = "success",
                data = listOf(
                    CricApiSeriesPointsRowDto(teamname = "Gulf Giants", shortname = "GG", matches = 2, wins = 2, loss = 0)
                )
            )
    }

    private class FakeOddsApiService : OddsApiService {
        override suspend fun getSports(apiKey: String) = listOf(
            OddsSportKeyDto(key = "cricket_ilt20", group = "Cricket", title = "ILT20", active = true)
        )

        override suspend fun getSportOdds(
            sportKey: String,
            apiKey: String,
            regions: String,
            markets: String,
            oddsFormat: String
        ) = listOf(
            OddsEventDto(
                id = "evt-dc-dv",
                sportKey = sportKey,
                sportTitle = "ILT20",
                homeTeam = "Dubai Capitals",
                awayTeam = "Desert Vipers",
                bookmakers = listOf(
                    OddsBookmakerDto(
                        key = "pinnacle",
                        title = "Pinnacle",
                        markets = listOf(
                            OddsMarketDto(
                                key = "h2h",
                                outcomes = listOf(
                                    OddsOutcomeDto(name = "Dubai Capitals", price = 2.20),
                                    OddsOutcomeDto(name = "Desert Vipers", price = 1.72)
                                )
                            )
                        )
                    )
                )
            )
        )

        override suspend fun getEventOdds(
            sportKey: String,
            eventId: String,
            apiKey: String,
            regions: String,
            markets: String,
            oddsFormat: String
        ) = getSportOdds(sportKey, apiKey, regions, markets, oddsFormat).first()
    }
}
