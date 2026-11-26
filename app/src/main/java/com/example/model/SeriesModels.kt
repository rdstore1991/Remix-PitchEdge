package com.example.model

import com.example.ai.GroundedSource

enum class SeriesLeague(
    val title: String,
    val shortLabel: String,
    val subtitle: String,
    val searchKeywords: String
) {
    ILT20_2026(
        title = "DP World ILT20 2026 (Season 5)",
        shortLabel = "ILT20 2026 S5",
        subtitle = "Season 5 • Nov–Dec 2026 • UAE (6 Franchises)",
        searchKeywords = "DP World ILT20 2026 Season 5 schedule points table squads stats news"
    ),
    WBBL_2026(
        title = "Women's Big Bash League 2026",
        shortLabel = "WBBL 2026",
        subtitle = "WBBL 2026 • Nov–Dec 2026 • Australia (8 Franchises)",
        searchKeywords = "Womens Big Bash League WBBL 2026 schedule points table squads stats news"
    )
}

enum class SeriesSectionTab(val label: String) {
    FIXTURES("Fixtures"),
    POINTS("Points"),
    TEAMS("Teams"),
    NEWS("News"),
    VENUES("Venues"),
    STATS("Stats")
}

enum class FixtureMatchStatus(val label: String) {
    COMPLETED("Result"),
    LIVE("Live"),
    UPCOMING("Upcoming")
}

data class SeriesFixtureItem(
    val id: String,
    val matchNumber: String,
    val dateTimeLabel: String,
    val teamACode: String,
    val teamAName: String,
    val teamBCode: String,
    val teamBName: String,
    val venue: String,
    val status: FixtureMatchStatus,
    val scoreOrPreview: String,
    val quantSummary: String? = null,
    val linkedFixtureId: String? = null
)

data class PointsTableRow(
    val rank: Int,
    val teamCode: String,
    val teamName: String,
    val played: Int,
    val won: Int,
    val lost: Int,
    val noResult: Int,
    val points: Int,
    val nrr: String,
    val recentForm: String,
    val qualifierZone: Boolean = false
)

data class FinalTeamSquad(
    val teamCode: String,
    val teamName: String,
    val captain: String,
    val coachOrTheme: String,
    val rosterSummary: String,
    val players: List<PlayerSlot>,
    val injuryOrDutyAlert: String? = null
)

data class SeriesNewsItem(
    val id: String,
    val title: String,
    val tag: String,
    val timestamp: String,
    val summary: String,
    val tradingImpact: String
)

data class SeriesVenueDetail(
    val name: String,
    val city: String,
    val dimensionsNote: String,
    val firstInningsAvg: String,
    val secondInningsAvg: String,
    val chaseWinRate: String,
    val pitchAndDewProfile: String
)

data class SeriesPlayerStat(
    val rank: Int,
    val playerName: String,
    val teamCode: String,
    val category: String, // "Batting" or "Bowling"
    val primaryStat: String,
    val secondaryStat: String,
    val quantNote: String
)

data class SeriesSummaryMetrics(
    val totalMatches: String,
    val avgFirstInningsScore: String,
    val chasingWinPct: String,
    val paceVsSpinSplit: String
)

data class SeriesBundle(
    val league: SeriesLeague,
    val statusBanner: String,
    val summaryMetrics: SeriesSummaryMetrics,
    val fixtures: List<SeriesFixtureItem>,
    val pointsTable: List<PointsTableRow>,
    val teams: List<FinalTeamSquad>,
    val news: List<SeriesNewsItem>,
    val venues: List<SeriesVenueDetail>,
    val battingLeaders: List<SeriesPlayerStat>,
    val bowlingLeaders: List<SeriesPlayerStat>
)

sealed interface SeriesLiveSyncState {
    data object Idle : SeriesLiveSyncState
    data object Loading : SeriesLiveSyncState
    data class Success(
        val summary: String,
        val sources: List<GroundedSource> = emptyList()
    ) : SeriesLiveSyncState
    data class Error(val message: String) : SeriesLiveSyncState
}
