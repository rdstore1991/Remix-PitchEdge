package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AnalyticalPick
import com.example.model.BookmakerOddsRow
import com.example.model.FixtureOverview
import com.example.model.FormatFilter
import com.example.model.MarketEdgeItem
import com.example.model.PhaseMatchupInfo
import com.example.model.PlayerLeverageNote
import com.example.model.PlayerSlot
import com.example.model.PreTossChecklist
import com.example.model.SportFilter
import com.example.model.StatusFilter
import com.example.model.TacticalPhase
import com.example.model.TossScenario
import com.example.model.VenueScorePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pitchEdgeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pitchedge_prefs"
)

data class PersistedMatchPrefs(
    val darkTheme: Boolean = true,
    val tossScenario: TossScenario = TossScenario.PRE_TOSS,
    val unitSizeDollars: Double = 50.0,
    val kellyMultiplier: Double = 0.25,
    val checklist: PreTossChecklist = PreTossChecklist()
)

class MatchRepository(private val context: Context) {

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val TOSS_SCENARIO = stringPreferencesKey("toss_scenario")
        val UNIT_SIZE = doublePreferencesKey("unit_size")
        val KELLY_MULT = doublePreferencesKey("kelly_mult")
        val CHK_FAKHAR_BABAR = booleanPreferencesKey("chk_fakhar_babar")
        val CHK_POWELL_SHANAKA = booleanPreferencesKey("chk_powell_shanaka")
        val CHK_DC_BOWL = booleanPreferencesKey("chk_dc_bowl")
        val CHK_HOLDEN_XI = booleanPreferencesKey("chk_holden_xi")
    }

    val userPrefsFlow: Flow<PersistedMatchPrefs> = context.pitchEdgeDataStore.data.map { prefs ->
        val scenarioName = prefs[Keys.TOSS_SCENARIO] ?: TossScenario.PRE_TOSS.name
        val scenario = runCatching { TossScenario.valueOf(scenarioName) }
            .getOrDefault(TossScenario.PRE_TOSS)

        PersistedMatchPrefs(
            darkTheme = prefs[Keys.DARK_THEME] ?: true,
            tossScenario = scenario,
            unitSizeDollars = prefs[Keys.UNIT_SIZE] ?: 50.0,
            kellyMultiplier = prefs[Keys.KELLY_MULT] ?: 0.25,
            checklist = PreTossChecklist(
                fakharBabarOpeningConfirmed = prefs[Keys.CHK_FAKHAR_BABAR] ?: true,
                powellOrShanakaInDcXi = prefs[Keys.CHK_POWELL_SHANAKA] ?: false,
                dcWonTossAndBowls = prefs[Keys.CHK_DC_BOWL] ?: false,
                holdenInStartingXi = prefs[Keys.CHK_HOLDEN_XI] ?: false
            )
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.pitchEdgeDataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setTossScenario(scenario: TossScenario) {
        context.pitchEdgeDataStore.edit { it[Keys.TOSS_SCENARIO] = scenario.name }
    }

    suspend fun setUnitSize(unitDollars: Double) {
        context.pitchEdgeDataStore.edit { it[Keys.UNIT_SIZE] = unitDollars }
    }

    suspend fun setKellyMultiplier(mult: Double) {
        context.pitchEdgeDataStore.edit { it[Keys.KELLY_MULT] = mult }
    }

    suspend fun updateChecklist(checklist: PreTossChecklist) {
        context.pitchEdgeDataStore.edit { prefs ->
            prefs[Keys.CHK_FAKHAR_BABAR] = checklist.fakharBabarOpeningConfirmed
            prefs[Keys.CHK_POWELL_SHANAKA] = checklist.powellOrShanakaInDcXi
            prefs[Keys.CHK_DC_BOWL] = checklist.dcWonTossAndBowls
            prefs[Keys.CHK_HOLDEN_XI] = checklist.holdenInStartingXi
        }
    }

    val fixturesSlate: List<FixtureOverview> = listOf(
        FixtureOverview(
            id = "ilt20_m6_dc_dv",
            sport = SportFilter.CRICKET,
            format = FormatFilter.T20_LEAGUES,
            status = StatusFilter.UPCOMING_TODAY,
            hasPositiveEdge = true,
            leagueTitle = "DP World ILT20 2026 • Match 6",
            matchLabel = "DC vs DV",
            homeCode = "DC",
            homeName = "Dubai Capitals",
            homeRecord = "0W–1L • NRR -2.750",
            awayCode = "DV",
            awayName = "Desert Vipers",
            awayRecord = "1W–1L • NRR -0.793",
            liveScoreOrTimer = "PRE-MATCH • 14:30 GMT",
            oversOrPeriod = "Toss 14:00 GMT (6:00 PM GST)",
            venue = "Dubai International Cricket Stadium",
            weatherLine = "32°C → 25°C • Clear • 29–44% Humidity (Low Dew)",
            tossStatus = "Pre-Toss • Hist Chasing 29/46 (63%)",
            homeModelPct = 46.5f,
            awayModelPct = 53.5f,
            marketHomePct = 45.4f,
            marketAwayPct = 54.6f,
            topEdgeBadge = "+6.5% EV",
            bestPickTitle = "Fakhar Zaman 10 Runs Quicker than Babar Azam",
            bestPickFairOdds = 1.92,
            bestPickMarketOdds = 2.20,
            bestPickConfidence = "Medium • +6.5% Edge",
            bestPickUnits = 2.0f,
            bestPickRiskWarning = "INVALIDATION: Do not bet if DV splits Fakhar/Babar opening pair. Hold Max Holden Top Bat (4.33) until 14:00 GMT Starting XI confirms non-sub role; Phil Salt out on England duty.",
            defaultFirstInnScore = 166,
            defaultSecondInnScore = 154,
            scoreUnitLabel = "Runs",
            bookmakerRows = listOf(
                BookmakerOddsRow("Betfair", 2.20, 1.75, 2.6, isBestHome = true, isBestAway = false),
                BookmakerOddsRow("Puntit", 2.10, 1.78, 3.8, isBestHome = false, isBestAway = true),
                BookmakerOddsRow("1xBet", 2.13, 1.72, 5.1, isBestHome = false, isBestAway = false),
                BookmakerOddsRow("Parimatch", 2.06, 1.77, 5.0, isBestHome = false, isBestAway = false)
            )
        ),
        FixtureOverview(
            id = "ilt20_m7_gg_sw",
            sport = SportFilter.CRICKET,
            format = FormatFilter.T20_LEAGUES,
            status = StatusFilter.UPCOMING_TODAY,
            hasPositiveEdge = true,
            leagueTitle = "DP World ILT20 2026 • Match 7",
            matchLabel = "GG vs SW",
            homeCode = "GG",
            homeName = "Gulf Giants",
            homeRecord = "2W–0L • NRR +1.414",
            awayCode = "SW",
            awayName = "Sharjah Warriorz",
            awayRecord = "0W–1L • Lost M4",
            liveScoreOrTimer = "THU NOV 26 • 14:30 GMT",
            oversOrPeriod = "Toss 14:00 GMT • Sharjah",
            venue = "Sharjah Cricket Stadium",
            weatherLine = "29°C → 23°C • Clear • Compact 58–64m Boundaries",
            tossStatus = "Pre-Toss • Hist 1st Inn Avg 171.2",
            homeModelPct = 59.5f,
            awayModelPct = 40.5f,
            marketHomePct = 55.2f,
            marketAwayPct = 44.8f,
            topEdgeBadge = "+4.3% EV",
            bestPickTitle = "Gulf Giants Match Winner & Over 168.5 Match Total",
            bestPickFairOdds = 1.68,
            bestPickMarketOdds = 1.81,
            bestPickConfidence = "High • +4.3% Edge",
            bestPickUnits = 2.0f,
            bestPickRiskWarning = "Compact Sharjah boundaries amplify Charles/Kohler-Cadmore Powerplay variance; verify Adil Rashid & Mark Adair availability at toss.",
            defaultFirstInnScore = 172,
            defaultSecondInnScore = 166,
            scoreUnitLabel = "Runs",
            bookmakerRows = listOf(
                BookmakerOddsRow("Betfair", 1.81, 2.24, 2.9, isBestHome = true, isBestAway = true),
                BookmakerOddsRow("1xBet", 1.76, 2.16, 4.9, isBestHome = false, isBestAway = false),
                BookmakerOddsRow("Parimatch", 1.78, 2.12, 4.8, isBestHome = false, isBestAway = false)
            )
        ),
        FixtureOverview(
            id = "ilt20_m8_adkr_mie",
            sport = SportFilter.CRICKET,
            format = FormatFilter.T20_LEAGUES,
            status = StatusFilter.UPCOMING_TODAY,
            hasPositiveEdge = true,
            leagueTitle = "DP World ILT20 2026 • Match 8",
            matchLabel = "ADKR vs MIE",
            homeCode = "ADKR",
            homeName = "Abu Dhabi Knight Riders",
            homeRecord = "1W–1L • Beat DC 167/6",
            awayCode = "MIE",
            awayName = "MI Emirates",
            awayRecord = "0W–2L • Bowled 91 vs GG",
            liveScoreOrTimer = "FRI NOV 27 • 14:30 GMT",
            oversOrPeriod = "Toss 14:00 GMT • Abu Dhabi",
            venue = "Sheikh Zayed Cricket Stadium",
            weatherLine = "30°C → 24°C • Light Breeze • True Bounce",
            tossStatus = "Pre-Toss • Hist 1st Inn Avg 163.5",
            homeModelPct = 47.0f,
            awayModelPct = 53.0f,
            marketHomePct = 51.2f,
            marketAwayPct = 48.8f,
            topEdgeBadge = "+4.2% EV",
            bestPickTitle = "MI Emirates Match Winner (0–2 Rebound Value)",
            bestPickFairOdds = 1.89,
            bestPickMarketOdds = 2.05,
            bestPickConfidence = "Medium • +4.2% Edge",
            bestPickUnits = 1.5f,
            bestPickRiskWarning = "MIE batting collapsed for 91 on two-paced Dubai pitch; Abu Dhabi's truer carry suits Pooran & Waseem unless Boult/Farooqi misfire early.",
            defaultFirstInnScore = 164,
            defaultSecondInnScore = 158,
            scoreUnitLabel = "Runs",
            bookmakerRows = listOf(
                BookmakerOddsRow("Betfair", 1.96, 2.05, 2.8, isBestHome = true, isBestAway = true),
                BookmakerOddsRow("Puntit", 1.92, 2.00, 4.2, isBestHome = false, isBestAway = false)
            )
        ),
        FixtureOverview(
            id = "wbbl_s12_ss_bh",
            sport = SportFilter.CRICKET,
            format = FormatFilter.T20_LEAGUES,
            status = StatusFilter.UPCOMING_TODAY,
            hasPositiveEdge = true,
            leagueTitle = "Women's Big Bash League 2026",
            matchLabel = "SS vs BH",
            homeCode = "SS",
            homeName = "Sydney Sixers",
            homeRecord = "3W–1L • +0.620 NRR",
            awayCode = "BH",
            awayName = "Brisbane Heat",
            awayRecord = "2W–2L • +0.290 NRR",
            liveScoreOrTimer = "WBBL • 08:10 GMT",
            oversOrPeriod = "North Sydney Oval • Pre-Toss",
            venue = "North Sydney Oval, Sydney",
            weatherLine = "22°C → 18°C • Coastal Breeze • Short 54–58m Straight",
            tossStatus = "Pre-Toss • Hist 1st Inn Avg 158.4",
            homeModelPct = 54.5f,
            awayModelPct = 45.5f,
            marketHomePct = 50.8f,
            marketAwayPct = 49.2f,
            topEdgeBadge = "+3.7% EV",
            bestPickTitle = "Sydney Sixers ML & Over 154.5 1st Innings Total",
            bestPickFairOdds = 1.83,
            bestPickMarketOdds = 1.97,
            bestPickConfidence = "Medium • +3.7% Edge",
            bestPickUnits = 1.5f,
            bestPickRiskWarning = "Short straight North Sydney dimensions amplify Grace Harris & Alyssa Healy PP variance; confirm Sophie Ecclestone fitness pre-toss.",
            defaultFirstInnScore = 160,
            defaultSecondInnScore = 153,
            scoreUnitLabel = "Runs",
            bookmakerRows = listOf(
                BookmakerOddsRow("Betfair", 1.97, 2.04, 2.5, isBestHome = true, isBestAway = true),
                BookmakerOddsRow("1xBet", 1.91, 1.98, 4.7, isBestHome = false, isBestAway = false)
            )
        ),
        FixtureOverview(
            id = "wbbl_s12_ms_ps",
            sport = SportFilter.CRICKET,
            format = FormatFilter.T20_LEAGUES,
            status = StatusFilter.UPCOMING_TODAY,
            hasPositiveEdge = true,
            leagueTitle = "Women's Big Bash League 2026",
            matchLabel = "MS vs PS",
            homeCode = "MS",
            homeName = "Melbourne Stars",
            homeRecord = "1W–3L • -0.610 NRR",
            awayCode = "PS",
            awayName = "Perth Scorchers",
            awayRecord = "3W–1L • Table Leaders (+0.840)",
            liveScoreOrTimer = "WBBL • 04:00 GMT",
            oversOrPeriod = "Junction Oval • Day Match",
            venue = "CitiPower Centre (Junction Oval)",
            weatherLine = "21°C • Sunny • Afternoon Crosswind",
            tossStatus = "Pre-Toss • Hist 1st Inn Avg 151.0",
            homeModelPct = 42.5f,
            awayModelPct = 57.5f,
            marketHomePct = 46.5f,
            marketAwayPct = 53.5f,
            topEdgeBadge = "+4.0% EV",
            bestPickTitle = "Perth Scorchers Match Winner (-1.5 Runs Spread)",
            bestPickFairOdds = 1.74,
            bestPickMarketOdds = 1.87,
            bestPickConfidence = "Medium • +4.0% Edge",
            bestPickUnits = 1.5f,
            bestPickRiskWarning = "Stars new-ball duo Kapp & Garth pose Powerplay wicket risk; monitor Beth Mooney / Sophie Devine opening matchup.",
            defaultFirstInnScore = 152,
            defaultSecondInnScore = 148,
            scoreUnitLabel = "Runs",
            bookmakerRows = listOf(
                BookmakerOddsRow("Betfair", 2.16, 1.87, 2.6, isBestHome = true, isBestAway = true),
                BookmakerOddsRow("Parimatch", 2.10, 1.82, 4.9, isBestHome = false, isBestAway = false)
            )
        ),
        FixtureOverview(
            id = "wbbl_s12_as_hh",
            sport = SportFilter.CRICKET,
            format = FormatFilter.T20_LEAGUES,
            status = StatusFilter.UPCOMING_TODAY,
            hasPositiveEdge = true,
            leagueTitle = "Women's Big Bash League 2026",
            matchLabel = "AS vs HH",
            homeCode = "AS",
            homeName = "Adelaide Strikers",
            homeRecord = "2W–2L • +0.110 NRR",
            awayCode = "HH",
            awayName = "Hobart Hurricanes",
            awayRecord = "2W–2L • -0.240 NRR",
            liveScoreOrTimer = "WBBL • 07:40 GMT",
            oversOrPeriod = "Adelaide • Day/Night",
            venue = "Karen Rolton Oval / Adelaide Oval",
            weatherLine = "23°C → 17°C • Light Evening Swing",
            tossStatus = "Pre-Toss • Hist Chase 57.0%",
            homeModelPct = 53.5f,
            awayModelPct = 46.5f,
            marketHomePct = 49.5f,
            marketAwayPct = 50.5f,
            topEdgeBadge = "+4.0% EV",
            bestPickTitle = "Adelaide Strikers ML (Spin Choke Overs 7–15)",
            bestPickFairOdds = 1.87,
            bestPickMarketOdds = 2.02,
            bestPickConfidence = "Medium • +4.0% Edge",
            bestPickUnits = 1.5f,
            bestPickRiskWarning = "Lizelle Lee & Danielle Wyatt-Hodge Powerplay surge can offset Wellington's middle-overs grip if Strikers bowl short.",
            defaultFirstInnScore = 147,
            defaultSecondInnScore = 142,
            scoreUnitLabel = "Runs",
            bookmakerRows = listOf(
                BookmakerOddsRow("Betfair", 2.02, 1.98, 2.5, isBestHome = true, isBestAway = true),
                BookmakerOddsRow("1xBet", 1.96, 1.93, 4.7, isBestHome = false, isBestAway = false)
            )
        ),
        FixtureOverview(
            id = "ilt20_m5_gg_mie",
            sport = SportFilter.CRICKET,
            format = FormatFilter.T20_LEAGUES,
            status = StatusFilter.LIVE_NOW,
            hasPositiveEdge = true,
            leagueTitle = "DP World ILT20 2026 • Table Context",
            matchLabel = "GG vs MIE",
            homeCode = "GG",
            homeName = "Gulf Giants",
            homeRecord = "2W–0L • 4 pts • NRR +1.414",
            awayCode = "MIE",
            awayName = "MI Emirates",
            awayRecord = "0W–2L • Bowled out 91",
            liveScoreOrTimer = "GG CHASED 92 (6 WKTS)",
            oversOrPeriod = "Season 5 Table Leaders",
            venue = "Dubai International Cricket Stadium",
            weatherLine = "Two-paced surface • Seam & cutters dominant",
            tossStatus = "Result Logged • Outright GG 4.00",
            homeModelPct = 58.0f,
            awayModelPct = 42.0f,
            marketHomePct = 54.0f,
            marketAwayPct = 46.0f,
            topEdgeBadge = "Outright 4.00",
            bestPickTitle = "Monitor Gulf Giants 4.00 & Desert Vipers 8.50 Outright Value",
            bestPickFairOdds = 3.60,
            bestPickMarketOdds = 4.00,
            bestPickConfidence = "Outright Watch",
            bestPickUnits = 1.0f,
            bestPickRiskWarning = "Use Google Search Grounding for updated live Season 5 standings and post-Match 6 outright price shifts.",
            defaultFirstInnScore = 91,
            defaultSecondInnScore = 92,
            scoreUnitLabel = "Runs",
            bookmakerRows = listOf(
                BookmakerOddsRow("Betfair", 4.00, 8.50, 4.2, isBestHome = true, isBestAway = true)
            )
        ),
        FixtureOverview(
            id = "cricket_intl_live_search",
            sport = SportFilter.CRICKET,
            format = FormatFilter.INTERNATIONAL,
            status = StatusFilter.LIVE_NOW,
            hasPositiveEdge = true,
            leagueTitle = "International Cricket • Search Grounded",
            matchLabel = "Intl Slate",
            homeCode = "ENG",
            homeName = "International Watch",
            homeRecord = "Phil Salt on England duty",
            awayCode = "AUS",
            awayName = "Live Test / ODI / T20I",
            awayRecord = "Tap Google Search Refresh",
            liveScoreOrTimer = "GROUNDING READY",
            oversOrPeriod = "Live Web Search Feed",
            venue = "Global International Venues",
            weatherLine = "Live pitch & weather via Google Search",
            tossStatus = "Tap 'Live Search' for real-time XIs",
            homeModelPct = 51.0f,
            awayModelPct = 49.0f,
            marketHomePct = 50.0f,
            marketAwayPct = 50.0f,
            topEdgeBadge = "Search Live",
            bestPickTitle = "Run Gemini Google Search Grounding for Today's International Slate",
            bestPickFairOdds = 1.95,
            bestPickMarketOdds = 2.02,
            bestPickConfidence = "Live Grounding",
            bestPickUnits = 1.0f,
            bestPickRiskWarning = "Verify live toss and playing XI via Google Search Grounding before placing any international fixture positions.",
            defaultFirstInnScore = 172,
            defaultSecondInnScore = 165,
            scoreUnitLabel = "Runs",
            bookmakerRows = listOf(
                BookmakerOddsRow("Consensus", 2.02, 1.88, 3.9, isBestHome = true, isBestAway = true)
            )
        ),
        FixtureOverview(
            id = "cricket_domestic_slate",
            sport = SportFilter.CRICKET,
            format = FormatFilter.DOMESTIC,
            status = StatusFilter.UPCOMING_TODAY,
            hasPositiveEdge = false,
            leagueTitle = "Domestic T20 • Search Grounded",
            matchLabel = "Domestic",
            homeCode = "DOM",
            homeName = "Domestic T20 Slate",
            homeRecord = "Regional 20-Over Fixtures",
            awayCode = "XI",
            awayName = "Today's Card",
            awayRecord = "Tap Google Search Refresh",
            liveScoreOrTimer = "TODAY • GROUNDED",
            oversOrPeriod = "Tap Live Search for scores",
            venue = "Domestic Venues",
            weatherLine = "Check surface & dew via Search",
            tossStatus = "Pre-Toss • Search Grounding",
            homeModelPct = 50.0f,
            awayModelPct = 50.0f,
            marketHomePct = 50.0f,
            marketAwayPct = 50.0f,
            topEdgeBadge = "Neutral",
            bestPickTitle = "Query Gemini Search for Today's Domestic T20 Lineups & Pitch Reports",
            bestPickFairOdds = 2.00,
            bestPickMarketOdds = 1.95,
            bestPickConfidence = "Neutral",
            bestPickUnits = 0.5f,
            bestPickRiskWarning = "Domestic markets carry tighter limits and higher late-scratch variance; verify team sheets via Google Search.",
            defaultFirstInnScore = 160,
            defaultSecondInnScore = 155,
            scoreUnitLabel = "Runs",
            bookmakerRows = listOf(
                BookmakerOddsRow("Consensus", 1.95, 1.95, 2.5, isBestHome = true, isBestAway = true)
            )
        ),
        FixtureOverview(
            id = "football_live_slate",
            sport = SportFilter.FOOTBALL,
            format = FormatFilter.INTERNATIONAL,
            status = StatusFilter.LIVE_NOW,
            hasPositiveEdge = true,
            leagueTitle = "Football Analytics • Search Grounded",
            matchLabel = "Football",
            homeCode = "HOM",
            homeName = "Today's Top Fixture",
            homeRecord = "Tap Google Search for live xG",
            awayCode = "AWY",
            awayName = "Live / Upcoming Match",
            awayRecord = "Real-time odds & lineups",
            liveScoreOrTimer = "SEARCH GROUNDED",
            oversOrPeriod = "90' + Stoppage • Live Feed",
            venue = "Tap Live Search for stadium & weather",
            weatherLine = "Live pitch conditions via Google Search",
            tossStatus = "Starting XI Check via Search",
            homeModelPct = 54.0f,
            awayModelPct = 46.0f,
            marketHomePct = 51.0f,
            marketAwayPct = 49.0f,
            topEdgeBadge = "+3.0% Search",
            bestPickTitle = "Pull Today's Live Football Fixtures & Asian Handicap Edges via Gemini Search",
            bestPickFairOdds = 1.85,
            bestPickMarketOdds = 1.96,
            bestPickConfidence = "Search Grounded",
            bestPickUnits = 1.5f,
            bestPickRiskWarning = "Confirm confirmed starting lineups 60 minutes pre-kickoff via Google Search Grounding.",
            defaultFirstInnScore = 2,
            defaultSecondInnScore = 1,
            scoreUnitLabel = "Goals",
            bookmakerRows = listOf(
                BookmakerOddsRow("Consensus", 1.96, 3.80, 4.1, isBestHome = true, isBestAway = true)
            )
        ),
        FixtureOverview(
            id = "tennis_tour_slate",
            sport = SportFilter.TENNIS,
            format = FormatFilter.INTERNATIONAL,
            status = StatusFilter.UPCOMING_TODAY,
            hasPositiveEdge = true,
            leagueTitle = "ATP / WTA Tour • Search Grounded",
            matchLabel = "Tennis",
            homeCode = "P1",
            homeName = "Tour Matchup",
            homeRecord = "Tap Google Search for draw & H2H",
            awayCode = "P2",
            awayName = "Surface Hold/Break",
            awayRecord = "Real-time serve/return edge",
            liveScoreOrTimer = "SEARCH GROUNDED",
            oversOrPeriod = "Best of 3 Sets • Live Draw",
            venue = "Hard / Indoor Court",
            weatherLine = "Surface speed & indoor/wind via Search",
            tossStatus = "Order of Play via Search",
            homeModelPct = 56.0f,
            awayModelPct = 44.0f,
            marketHomePct = 52.5f,
            marketAwayPct = 47.5f,
            topEdgeBadge = "+3.5% Search",
            bestPickTitle = "Query Gemini Search for Today's ATP/WTA Hold-Break Edges & Retirements",
            bestPickFairOdds = 1.79,
            bestPickMarketOdds = 1.91,
            bestPickConfidence = "Search Grounded",
            bestPickUnits = 1.5f,
            bestPickRiskWarning = "Check sportsbook retirement void rules (1-set vs full-match) before backing pre-match moneyline.",
            defaultFirstInnScore = 22,
            defaultSecondInnScore = 21,
            scoreUnitLabel = "Games",
            bookmakerRows = listOf(
                BookmakerOddsRow("Consensus", 1.91, 1.98, 2.8, isBestHome = true, isBestAway = true)
            )
        )
    )

    val season5VenueScores: List<VenueScorePoint> = listOf(
        VenueScorePoint("MIE v DV", 174, "174/8", "S5 Match • 1st Inn", isAverage = false),
        VenueScorePoint("ADKR v DC", 167, "167/6", "S5 Match • 1st Inn", isAverage = false),
        VenueScorePoint("MIE v GG", 91, "91/10", "S5 Match • 1st Inn", isAverage = false),
        VenueScorePoint("S5 Avg", 144, "144.0", "Season 5 Mean", isAverage = true),
        VenueScorePoint("Hist 1st", 158, "157–159", "2024–26 Avg", isAverage = true)
    )

    val dcProjectedXi: List<PlayerSlot> = listOf(
        PlayerSlot("Faf du Plessis", "Top Order", "Powerplay anchor vs Naseem/Khuzaima"),
        PlayerSlot("Masood Gurbaz", "UAE", "Local opener pairing"),
        PlayerSlot("Max Holden", "Impact Sub", "Opened via Impact Sub in M3 • 43.7% top-bat rate", isImpactOrRotation = true),
        PlayerSlot("Sam Billings", "c / wk", "Middle-order stabilizer"),
        PlayerSlot("Marcus Stoinis", "All-Rounder", "47 in M3; key power hinge"),
        PlayerSlot("Tom Moores", "Middle Order", "Left-hand finisher option"),
        PlayerSlot("Hassan Khan", "Spin AR", "4-0-13-0 (3.25 econ) vs ADKR • 69% wickets in middle overs"),
        PlayerSlot("Karim Janat", "Seam AR", "Death/middle overs flexibility"),
        PlayerSlot("Dushmantha Chameera", "Pace", "4-0-25-2 vs ADKR • new-ball & back-end speed"),
        PlayerSlot("Noor Ahmad", "Wrist Spin", "2/40 vs ADKR • thrives with low dew"),
        PlayerSlot("Haider Ali-I", "UAE", "Local spin/depth slot"),
        PlayerSlot("Mustafizur Rahman", "12-Man Rot.", "Cutters tailored to two-paced Dubai track", isImpactOrRotation = true)
    )

    val dvProjectedXi: List<PlayerSlot> = listOf(
        PlayerSlot("Fakhar Zaman", "Opener", "Career T20 SR ~135 • attacks inside Powerplay ring"),
        PlayerSlot("Babar Azam", "Opener", "Struck 86 (51) in M4 • career SR ~129, early rotation"),
        PlayerSlot("Andries Gous", "wk", "Aggressive top/middle wicketkeeper-batter"),
        PlayerSlot("Joe Clarke", "Top/Middle", "Spin-hitting option in middle overs"),
        PlayerSlot("Shimron Hetmyer", "Finisher", "Death-overs boundary threat"),
        PlayerSlot("Shadab Khan", "c / Spin AR", "3/18 vs Warriorz • dual-threat middle-overs leader"),
        PlayerSlot("Tom Curran", "Pace AR", "4-0-21-2 vs Warriorz • back-of-the-hand slower balls"),
        PlayerSlot("Vriitya Aravind", "UAE", "Local batting insurance"),
        PlayerSlot("Khuzaima Bin Tanveer", "UAE Pace", "3/31 vs Warriorz • swings new ball"),
        PlayerSlot("Naseem Shah", "Pace", "Attacks DC's fragile Powerplay top order"),
        PlayerSlot("Mujeeb Ur Rahman", "Spin", "Powerplay & middle-overs mystery control"),
        PlayerSlot("Mohammad Saleem", "12-Man Rot.", "Seam rotation option", isImpactOrRotation = true)
    )

    fun marketEdgesForFixture(fixture: FixtureOverview): List<MarketEdgeItem> {
        if (fixture.id == "ilt20_m6_dc_dv") return marketEdgeMatrix

        val homeBook = fixture.bookmakerRows.firstOrNull()
        val homeOdds = homeBook?.homeOdds ?: 2.00
        val awayOdds = homeBook?.awayOdds ?: 2.00
        val homeEdge = Math.round((fixture.homeModelPct - fixture.marketHomePct) * 10.0) / 10.0
        val awayEdge = Math.round((fixture.awayModelPct - fixture.marketAwayPct) * 10.0) / 10.0
        val totalLine = fixture.defaultFirstInnScore - 3.5

        return listOf(
            MarketEdgeItem(
                id = "${fixture.id}_home",
                marketName = "${fixture.homeName} (H2H)",
                bookmakerNote = homeBook?.bookmaker ?: "Betfair",
                bestOdds = homeOdds,
                marketImpliedPct = fixture.marketHomePct.toDouble(),
                rawImpliedNote = "Raw ${String.format(java.util.Locale.US, "%.1f", 100.0 / homeOdds)}%",
                modelPct = fixture.homeModelPct.toDouble(),
                edgePct = homeEdge,
                evNote = if (homeEdge >= 3.0) "Playable +EV • ${fixture.homeCode}" else "Marginal / Pass",
                isProp = false,
                statusBadge = if (homeEdge >= 3.0) "PRIMARY +EV" else if (homeEdge > 0.0) "LEAN" else "SKIP (-EV)"
            ),
            MarketEdgeItem(
                id = "${fixture.id}_away",
                marketName = "${fixture.awayName} (H2H)",
                bookmakerNote = homeBook?.bookmaker ?: "Betfair",
                bestOdds = awayOdds,
                marketImpliedPct = fixture.marketAwayPct.toDouble(),
                rawImpliedNote = "Raw ${String.format(java.util.Locale.US, "%.1f", 100.0 / awayOdds)}%",
                modelPct = fixture.awayModelPct.toDouble(),
                edgePct = awayEdge,
                evNote = if (awayEdge >= 3.0) "Playable +EV • ${fixture.awayCode}" else "Negative / Pass",
                isProp = false,
                statusBadge = if (awayEdge >= 3.0) "PRIMARY +EV" else if (awayEdge > 0.0) "LEAN" else "SKIP (-EV)"
            ),
            MarketEdgeItem(
                id = "${fixture.id}_total_over",
                marketName = "1st Inn Over ${String.format(java.util.Locale.US, "%.1f", totalLine)} ${fixture.scoreUnitLabel}",
                bookmakerNote = "Market Line (Vig-Free)",
                bestOdds = 1.94,
                marketImpliedPct = 50.0,
                rawImpliedNote = "Raw 51.5%",
                modelPct = 54.0,
                edgePct = 4.0,
                evNote = "Venue par ${fixture.defaultFirstInnScore} supports Over lean",
                isProp = true,
                statusBadge = "+4.0% TOTALS"
            )
        )
    }

    val marketEdgeMatrix: List<MarketEdgeItem> = listOf(
        MarketEdgeItem(
            id = "fakhar_10_quicker",
            marketName = "Fakhar Zaman 10 Runs Quicker vs Babar",
            bookmakerNote = "Betfair (6/5)",
            bestOdds = 2.20,
            marketImpliedPct = 45.5,
            rawImpliedNote = "Raw 45.5%",
            modelPct = 52.0,
            edgePct = 6.5,
            evNote = "Playable down to 2.05",
            isProp = true,
            statusBadge = "PRIMARY +EV"
        ),
        MarketEdgeItem(
            id = "holden_top_dc",
            marketName = "Max Holden — DC Top Batter",
            bookmakerNote = "Betfair (10/3)",
            bestOdds = 4.33,
            marketImpliedPct = 23.1,
            rawImpliedNote = "Raw 23.1%",
            modelPct = 31.0,
            edgePct = 7.9,
            evNote = "Gated on Starting XI (not Impact Sub)",
            isProp = true,
            statusBadge = "PRE-XI RISK",
            hasRiskFlag = true
        ),
        MarketEdgeItem(
            id = "dc_match_winner",
            marketName = "Dubai Capitals (Match Winner)",
            bookmakerNote = "Betfair Sportsbook",
            bestOdds = 2.20,
            marketImpliedPct = 45.4,
            rawImpliedNote = "Raw 45.5%",
            modelPct = 46.5,
            edgePct = 1.1,
            evNote = "+2.3% EV at 2.20 • Conditional 1.0u",
            isProp = false,
            statusBadge = "CONDITIONAL +EV"
        ),
        MarketEdgeItem(
            id = "dv_match_winner",
            marketName = "Desert Vipers (Match Winner)",
            bookmakerNote = "Puntit (1xBet 1.72 / PM 1.77)",
            bestOdds = 1.78,
            marketImpliedPct = 54.6,
            rawImpliedNote = "Raw 56.2%",
            modelPct = 53.5,
            edgePct = -1.1,
            evNote = "Negative EV against 53.5% model",
            isProp = false,
            statusBadge = "SKIP (-EV)"
        )
    )

    val phaseMatchups: List<PhaseMatchupInfo> = listOf(
        PhaseMatchupInfo(
            phase = TacticalPhase.POWERPLAY,
            headline = "DC Top-Order Fragility vs. DV New-Ball Seam",
            dcAdvantageScore = 0.41f,
            dcRatingOutOf10 = 6.4,
            dvRatingOutOf10 = 7.8,
            keyBattles = listOf(
                "Naseem Shah & Khuzaima Bin Tanveer (3/31 vs Warriorz) attack Faf & Holden early",
                "DC imploded to 6/3 and 34/5 in Match 3 against seam/swing",
                "Fakhar Zaman (SR ~135) & Babar Azam (SR ~129) vs Chameera (4-0-25-2) & Mustafizur"
            ),
            dcMetrics = "M3 collapse: 6/3 & 34/5 • Chameera 4-0-25-2",
            dvMetrics = "Khuzaima 3/31 • Fakhar SR ~135 / Babar SR ~129",
            tacticalVerdict = "Slight DV edge early. If DC navigates Overs 1–6 at 45/1 or better, live DC price contracts from 2.20 toward 2.00."
        ),
        PhaseMatchupInfo(
            phase = TacticalPhase.MIDDLE,
            headline = "The Decisive Spin Choke (Overs 7–15)",
            dcAdvantageScore = 0.50f,
            dcRatingOutOf10 = 8.4,
            dvRatingOutOf10 = 8.4,
            keyBattles = listOf(
                "Hassan Khan: 4-0-13-0 (3.25 econ) vs ADKR; 69% of career wickets in middle overs",
                "Noor Ahmad (2/40 vs ADKR) + Hassan vs Shadab Khan (3/18 vs Warriorz) & Mujeeb",
                "Low Nov humidity (29–44%) limits dew, keeping grip & cutters potent"
            ),
            dcMetrics = "Hassan 3.25 econ (69% mid-ov wkts) • Noor 2/40",
            dvMetrics = "Shadab 3/18 vs Warriorz • Mujeeb control",
            tacticalVerdict = "Dead even elite spin battleground. Whichever batting side avoids a 3-for-20 middle-overs cluster takes full control."
        ),
        PhaseMatchupInfo(
            phase = TacticalPhase.DEATH,
            headline = "Cutters & Slower Balls on Large Square Boundaries",
            dcAdvantageScore = 0.44f,
            dcRatingOutOf10 = 7.1,
            dvRatingOutOf10 = 7.9,
            keyBattles = listOf(
                "Mustafizur Rahman's cutters vs Shimron Hetmyer & Shadab Khan",
                "Tom Curran (4-0-21-2 vs Warriorz) back-of-the-hand slower balls",
                "DC lower-middle looks thin after Stoinis (47) without Phil Salt unless Powell/Shanaka return"
            ),
            dcMetrics = "Mustafizur cutters • Thin finish without Salt/Powell",
            dvMetrics = "Tom Curran 4-0-21-2 • Hetmyer & Shadab power",
            tacticalVerdict = "Slight DV death-hitting edge unless DC recalls Rovman Powell or Dasun Shanaka on the 14:00 GMT toss sheet."
        )
    )

    val playerLeverages: List<PlayerLeverageNote> = listOf(
        PlayerLeverageNote(
            title = "Fakhar Zaman vs. Babar Azam",
            subtitle = "Race to 10 Runs • Market Overreaction to Sharjah 86 (51)",
            statHighlight = "52.0% Model vs 45.5% Implied (+6.5% Edge)",
            analysis = "Market pricing shaded heavily toward Babar after his 86 (51) in Sharjah. On a slower, two-paced Dubai surface against Chameera and Mustafizur, Babar favors risk-averse strike rotation early while Fakhar (T20 SR ~135) attacks inside the Powerplay ring."
        ),
        PlayerLeverageNote(
            title = "Max Holden (DC Top Batter)",
            subtitle = "Impact-Sub Void Risk • Wait for 14:00 GMT Sheet",
            statHighlight = "43.7% Two-Year Top-Bat Win Rate (+7.9% Edge)",
            analysis = "Holden holds a 43.7% top-batter win rate over his two-year T20 sample and opened immediately via Impact Sub in Match 3. Only play 4.33 (10/3) if confirmed in DC's Starting XI, as sportsbook Impact-Sub rules void/exclude substitute batters."
        )
    )

    val primarySelection = AnalyticalPick(
        id = "pick_fakhar",
        tierLabel = "PRIMARY SELECTION",
        selectionTitle = "Fakhar Zaman to Reach 10 Runs Quicker than Babar Azam",
        targetOdds = "2.20 (6/5 Betfair)",
        minPlayableOdds = "Playable down to 2.05",
        edgeLabel = "+6.5% Edge",
        modelVsMarket = "Model 52.0% vs Implied 45.5%",
        stakeUnits = 2.0,
        confidenceTier = "Medium",
        rationale = "Babar's props over-indexed on his Sharjah 86 (51). On a slower Dubai deck, Babar rotates strike early against Chameera/Mustafizur while Fakhar hunts early Powerplay boundaries.",
        triggerOrInvalidation = "INVALIDATION TRIGGER: Do NOT bet if Desert Vipers alter their opening pair at the toss (e.g., dropping Babar to No. 3 and promoting Andries Gous to open with Fakhar)."
    )

    val secondaryLeans: List<AnalyticalPick> = listOf(
        AnalyticalPick(
            id = "pick_dc_ml",
            tierLabel = "CONDITIONAL LEAN",
            selectionTitle = "Dubai Capitals — Match Winner",
            targetOdds = "2.20 (Betfair)",
            minPlayableOdds = "Conditional Pre-Toss / Live",
            edgeLabel = "+1.1% (+2.3% EV)",
            modelVsMarket = "Model 46.5% vs Vig-Free 45.4%",
            stakeUnits = 1.0,
            confidenceTier = "Conditional",
            rationale = "Bookmakers over-penalized DC's 112 all-out in M3 despite DC choking ADKR to 48/6 over their final 7.2 overs. Watch for live contraction toward 2.00 if DC reaches 45/1.",
            triggerOrInvalidation = "QUALIFICATION GATE: Play 1.0u ONLY if Rovman Powell or Dasun Shanaka starts in the 14:00 GMT XI, OR if DC wins the toss and bowls first."
        ),
        AnalyticalPick(
            id = "pick_holden",
            tierLabel = "PRE-XI GATED PROP",
            selectionTitle = "Max Holden — DC Top Batter",
            targetOdds = "4.33 (10/3 Betfair)",
            minPlayableOdds = "Starting XI Only",
            edgeLabel = "+7.9% Edge",
            modelVsMarket = "Model 31.0% vs Implied 23.1%",
            stakeUnits = 0.75,
            confidenceTier = "Conditional",
            rationale = "43.7% two-year T20 top-batter win rate and opened in Match 3, with Phil Salt away on England duty.",
            triggerOrInvalidation = "RULE RISK: Back ONLY after 14:00 GMT team sheets confirm Max Holden in the Starting XI (not listed as an Impact Sub)."
        ),
        AnalyticalPick(
            id = "pick_dv_skip",
            tierLabel = "PASS / FADE",
            selectionTitle = "Skip Desert Vipers Match Winner",
            targetOdds = "1.72 – 1.78",
            minPlayableOdds = "No Play",
            edgeLabel = "-1.1% Edge",
            modelVsMarket = "Model 53.5% vs Vig-Adj 54.6% (Raw 56.2%)",
            stakeUnits = 0.0,
            confidenceTier = "Pass",
            rationale = "Pre-match consensus (1xBet 1.72, Parimatch 1.77, Puntit 1.78) bakes in too much recency bias from DC's M3 batting collapse.",
            triggerOrInvalidation = "NEGATIVE EV: Pass pre-toss; monitor DV only if drifting above 1.90 live."
        )
    )
}
