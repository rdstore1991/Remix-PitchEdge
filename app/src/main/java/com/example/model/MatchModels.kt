package com.example.model

import com.example.ai.GroundedSource

enum class AppDestination(val label: String) {
    MATCHDAY("Dashboard"),
    SERIES("Series"),
    EDGES("Edges"),
    TACTICS("Tactics"),
    SELECTIONS("Card")
}

enum class SportFilter(val label: String) {
    CRICKET("Cricket"),
    FOOTBALL("Football"),
    TENNIS("Tennis")
}

enum class FormatFilter(val label: String) {
    ALL("All"),
    INTERNATIONAL("International"),
    T20_LEAGUES("T20 Leagues"),
    DOMESTIC("Domestic")
}

enum class StatusFilter(val label: String) {
    LIVE_NOW("Live Now"),
    UPCOMING_TODAY("Upcoming Today"),
    VALUE_PICKS("Value Picks")
}

enum class TeamSide { DC, DV }

enum class TossScenario(
    val title: String,
    val shortLabel: String,
    val parRange: String,
    val dvWinModelPct: Float,
    val dcWinModelPct: Float,
    val marketLagSummary: String
) {
    PRE_TOSS(
        title = "Pre-Toss Baseline",
        shortLabel = "Pre-Toss",
        parRange = "DV 162–174 • DC 152–166",
        dvWinModelPct = 53.5f,
        dcWinModelPct = 46.5f,
        marketLagSummary = "DC undervalued after 112 all-out; ADKR held to 48/6 in final 7.2 overs. Expect live contraction from 2.20 → 2.00 if DC reaches 45/1 in Powerplay."
    ),
    DV_BAT_FIRST(
        title = "DV Bat First",
        shortLabel = "DV Bat 1st",
        parRange = "162 – 174 Projected Par",
        dvWinModelPct = 55.0f,
        dcWinModelPct = 45.0f,
        marketLagSummary = "Anchored top order (Fakhar/Babar) + Hetmyer/Shadab finishing faces DC spin/cutter choke. Low dew keeps Mujeeb & Shadab dangerous in chase."
    ),
    DC_BAT_FIRST(
        title = "DC Bat First",
        shortLabel = "DC Bat 1st",
        parRange = "152 – 166 Projected Par",
        dvWinModelPct = 51.5f,
        dcWinModelPct = 48.5f,
        marketLagSummary = "Higher variance batting first; if Faf, Holden & Stoinis survive Naseem/Khuzaima to 45/1 (6 ov), live DC odds compress sharply toward 2.00."
    ),
    DC_BOWL_FIRST(
        title = "DC Win Toss & Bowl",
        shortLabel = "DC Bowl 1st",
        parRange = "Target ≤ 165 Chase",
        dvWinModelPct = 50.5f,
        dcWinModelPct = 49.5f,
        marketLagSummary = "Mustafizur, Noor & Hassan exploit fresh surface moisture early. Unlocks conditional 1.0u play on Dubai Capitals ML at 2.20 (+EV)."
    )
}

enum class EdgeFilter(val label: String) {
    ALL("All Markets"),
    POSITIVE_EV("+EV Only"),
    PROPS("Player Props")
}

enum class TacticalPhase(val title: String, val oversLabel: String) {
    POWERPLAY("Powerplay", "Overs 1–6"),
    MIDDLE("Middle Overs", "Overs 7–15"),
    DEATH("Death Overs", "Overs 16–20")
}

data class BookmakerOddsRow(
    val bookmaker: String,
    val homeOdds: Double,
    val awayOdds: Double,
    val overroundPct: Double,
    val isBestHome: Boolean = false,
    val isBestAway: Boolean = false
)

data class FixtureOverview(
    val id: String,
    val sport: SportFilter,
    val format: FormatFilter,
    val status: StatusFilter,
    val hasPositiveEdge: Boolean,
    val leagueTitle: String,
    val matchLabel: String,
    val homeCode: String,
    val homeName: String,
    val homeRecord: String,
    val awayCode: String,
    val awayName: String,
    val awayRecord: String,
    val liveScoreOrTimer: String,
    val oversOrPeriod: String,
    val venue: String,
    val weatherLine: String,
    val tossStatus: String,
    val homeModelPct: Float,
    val awayModelPct: Float,
    val marketHomePct: Float,
    val marketAwayPct: Float,
    val topEdgeBadge: String,
    val bestPickTitle: String,
    val bestPickFairOdds: Double,
    val bestPickMarketOdds: Double,
    val bestPickConfidence: String,
    val bestPickUnits: Float,
    val bestPickRiskWarning: String,
    val defaultFirstInnScore: Int,
    val defaultSecondInnScore: Int,
    val scoreUnitLabel: String,
    val bookmakerRows: List<BookmakerOddsRow>
)

data class VenueScorePoint(
    val label: String,
    val score: Int,
    val wickets: String,
    val detail: String,
    val isAverage: Boolean = false
)

data class PlayerSlot(
    val name: String,
    val roleTag: String,
    val note: String? = null,
    val isImpactOrRotation: Boolean = false
)

data class MarketEdgeItem(
    val id: String,
    val marketName: String,
    val bookmakerNote: String,
    val bestOdds: Double,
    val marketImpliedPct: Double,
    val rawImpliedNote: String,
    val modelPct: Double,
    val edgePct: Double,
    val evNote: String,
    val isProp: Boolean,
    val statusBadge: String,
    val hasRiskFlag: Boolean = false
)

data class PhaseMatchupInfo(
    val phase: TacticalPhase,
    val headline: String,
    val dcAdvantageScore: Float,
    val dcRatingOutOf10: Double,
    val dvRatingOutOf10: Double,
    val keyBattles: List<String>,
    val dcMetrics: String,
    val dvMetrics: String,
    val tacticalVerdict: String
)

data class PlayerLeverageNote(
    val title: String,
    val subtitle: String,
    val statHighlight: String,
    val analysis: String
)

data class AnalyticalPick(
    val id: String,
    val tierLabel: String,
    val selectionTitle: String,
    val targetOdds: String,
    val minPlayableOdds: String,
    val edgeLabel: String,
    val modelVsMarket: String,
    val stakeUnits: Double,
    val confidenceTier: String,
    val rationale: String,
    val triggerOrInvalidation: String
)

data class PreTossChecklist(
    val fakharBabarOpeningConfirmed: Boolean = true,
    val powellOrShanakaInDcXi: Boolean = false,
    val dcWonTossAndBowls: Boolean = false,
    val holdenInStartingXi: Boolean = false
)

sealed interface GroundedIntelUiState {
    data object Idle : GroundedIntelUiState
    data object Loading : GroundedIntelUiState
    data class Success(
        val queryLabel: String,
        val summary: String,
        val searchQueries: List<String>,
        val sources: List<GroundedSource>
    ) : GroundedIntelUiState
    data class Error(val message: String) : GroundedIntelUiState
}

data class AnalystChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val searchQueries: List<String> = emptyList(),
    val sources: List<GroundedSource> = emptyList(),
    val structuredAnalysis: PitchEdgeAnalysisResponse? = null
)
