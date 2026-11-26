package com.example.model

import com.squareup.moshi.Json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json as KotlinxJson
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Shared lenient JSON serializer configured for third-party sports & odds API payloads.
 */
object ApiJson {
    val lenient: KotlinxJson = KotlinxJson {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }
}

/**
 * Generic CricAPI v1 envelope wrapping all CricAPI endpoints
 * (`/currentMatches`, `/match_info`, `/match_scorecard`, `/players_info`, `/series`, `/series_points`).
 */
@Serializable
data class CricApiEnvelope<T>(
    val apikey: String? = null,
    val data: T? = null,
    val status: String = "",
    val reason: String? = null,
    val info: CricApiUsageInfoDto? = null
) {
    val isSuccess: Boolean
        get() = status.equals("success", ignoreCase = true)
}

@Serializable
data class CricApiUsageInfoDto(
    val hitsToday: Int = 0,
    val hitsUsed: Int = 0,
    val hitsLimit: Int = 0,
    val credits: Int = 0,
    val server: Int = 0,
    val offsetRows: Int = 0,
    val totalRows: Int = 0,
    val queryTime: Double = 0.0,
    val s: Int = 0
)

@Serializable
data class TeamInfoDto(
    val name: String = "",
    val shortname: String = "",
    val img: String? = null
)

/**
 * Innings score summary returned in `/currentMatches`, `/match_info`, and `/match_scorecard`.
 * Cricket overs use `.1`..`.5` notation for balls in the incomplete over.
 */
@Serializable
data class InningsScoreDto(
    @SerialName("r")
    @Json(name = "r")
    val runs: Int = 0,
    @SerialName("w")
    @Json(name = "w")
    val wickets: Int = 0,
    @SerialName("o")
    @Json(name = "o")
    val overs: Double = 0.0,
    val inning: String = "",
    val crr: Double? = null
) {
    /**
     * Converts cricket overs notation (e.g., `14.3` = 14 overs, 3 balls) into true decimal overs (`14.5`).
     */
    val trueOvers: Double
        get() {
            val wholeOvers = floor(overs)
            val balls = round((overs - wholeOvers) * 10.0).coerceIn(0.0, 5.0)
            return wholeOvers + (balls / 6.0)
        }

    val computedRunRate: Double
        get() = crr ?: if (trueOvers > 0.0) {
            round((runs / trueOvers) * 100.0) / 100.0
        } else {
            0.0
        }
}

/**
 * Match summary returned by CricAPI `/currentMatches` and the normalized `get_live_cricket_matches` tool.
 */
@Serializable
data class CricketMatchSummaryDto(
    val id: String = "",
    val name: String = "",
    val matchType: String = "",
    val status: String = "",
    val venue: String = "",
    val date: String = "",
    val dateTimeGMT: String = "",
    val teams: List<String> = emptyList(),
    val teamInfo: List<TeamInfoDto> = emptyList(),
    val score: List<InningsScoreDto> = emptyList(),
    @SerialName("series_id")
    @Json(name = "series_id")
    val seriesId: String? = null,
    val fantasyEnabled: Boolean = false,
    val bbbEnabled: Boolean = false,
    val hasSquad: Boolean = false,
    val matchStarted: Boolean = false,
    val matchEnded: Boolean = false
)

@Serializable
data class LiveCricketMatchesResponse(
    val count: Int = 0,
    val matches: List<CricketMatchSummaryDto> = emptyList()
)

@Serializable
data class PlayerIdentityDto(
    val id: String = "",
    val name: String = ""
)

/**
 * Individual batter line in a CricAPI `/match_scorecard` innings scorecard.
 */
@Serializable
data class BattingScorecardEntryDto(
    val batsman: PlayerIdentityDto = PlayerIdentityDto(),
    val dismissal: String? = null,
    val bowler: PlayerIdentityDto? = null,
    val catcher: PlayerIdentityDto? = null,
    @SerialName("dismissal-text")
    @Json(name = "dismissal-text")
    val dismissalText: String? = null,
    @SerialName("r")
    @Json(name = "r")
    val runs: Int = 0,
    @SerialName("b")
    @Json(name = "b")
    val balls: Int = 0,
    @SerialName("4s")
    @Json(name = "4s")
    val fours: Int = 0,
    @SerialName("6s")
    @Json(name = "6s")
    val sixes: Int = 0,
    @SerialName("sr")
    @Json(name = "sr")
    val strikeRate: Double = 0.0
) {
    val isNotOut: Boolean
        get() = dismissal.isNullOrBlank() || dismissal.equals("not out", ignoreCase = true)

    val boundaryRuns: Int
        get() = (fours * 4) + (sixes * 6)
}

/**
 * Individual bowler line in a CricAPI `/match_scorecard` innings scorecard.
 */
@Serializable
data class BowlingScorecardEntryDto(
    val bowler: PlayerIdentityDto = PlayerIdentityDto(),
    @SerialName("o")
    @Json(name = "o")
    val overs: Double = 0.0,
    @SerialName("m")
    @Json(name = "m")
    val maidens: Int = 0,
    @SerialName("r")
    @Json(name = "r")
    val runsConceded: Int = 0,
    @SerialName("w")
    @Json(name = "w")
    val wickets: Int = 0,
    @SerialName("nb")
    @Json(name = "nb")
    val noBalls: Int = 0,
    @SerialName("wd")
    @Json(name = "wd")
    val wides: Int = 0,
    @SerialName("eco")
    @Json(name = "eco")
    val economy: Double = 0.0
)

/**
 * Fielding / catching contribution row inside a CricAPI `/match_scorecard` innings scorecard.
 */
@Serializable
data class CatchingScorecardEntryDto(
    val catcher: PlayerIdentityDto = PlayerIdentityDto(),
    val stumped: Int = 0,
    val runout: Int = 0,
    @SerialName("catch")
    @Json(name = "catch")
    val catchCount: Int = 0,
    @SerialName("cb")
    @Json(name = "cb")
    val caughtAndBowled: Int = 0,
    val lbw: Int = 0,
    val bowled: Int = 0
)

@Serializable
data class InningsExtrasDto(
    @SerialName("r")
    @Json(name = "r")
    val total: Int = 0,
    @SerialName("b")
    @Json(name = "b")
    val byes: Int = 0,
    @SerialName("lb")
    @Json(name = "lb")
    val legByes: Int = 0,
    @SerialName("w")
    @Json(name = "w")
    val wides: Int = 0,
    @SerialName("nb")
    @Json(name = "nb")
    val noBalls: Int = 0,
    @SerialName("p")
    @Json(name = "p")
    val penalty: Int = 0
)

@Serializable
data class InningsTotalsDto(
    @SerialName("R")
    @Json(name = "R")
    val runs: Int = 0,
    @SerialName("O")
    @Json(name = "O")
    val overs: Double = 0.0,
    @SerialName("W")
    @Json(name = "W")
    val wickets: Int = 0,
    @SerialName("RR")
    @Json(name = "RR")
    val runRate: Double = 0.0
)

/**
 * Complete innings breakdown from CricAPI `/match_scorecard`.
 */
@Serializable
data class InningsScorecardDto(
    val inning: String = "",
    val batting: List<BattingScorecardEntryDto> = emptyList(),
    val bowling: List<BowlingScorecardEntryDto> = emptyList(),
    val catching: List<CatchingScorecardEntryDto> = emptyList(),
    val extras: InningsExtrasDto? = null,
    val totals: InningsTotalsDto? = null
)

/**
 * Detailed match info & full scorecard payload returned by CricAPI `/match_info` and `/match_scorecard`.
 */
@Serializable
data class CricketMatchScorecardDto(
    val id: String = "",
    val name: String = "",
    val matchType: String = "",
    val status: String = "",
    val venue: String = "",
    val date: String = "",
    val dateTimeGMT: String = "",
    val teams: List<String> = emptyList(),
    val teamInfo: List<TeamInfoDto> = emptyList(),
    val tossWinner: String = "Pending",
    val tossChoice: String = "Pending",
    val matchWinner: String? = null,
    @SerialName("series_id")
    @Json(name = "series_id")
    val seriesId: String? = null,
    val score: List<InningsScoreDto> = emptyList(),
    val scorecard: List<InningsScorecardDto> = emptyList(),
    val matchStarted: Boolean = false,
    val matchEnded: Boolean = false
)

/**
 * Series metadata from CricAPI `/series`.
 */
@Serializable
data class CricApiSeriesDto(
    val id: String = "",
    val name: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val odi: Int = 0,
    val t20: Int = 0,
    val test: Int = 0,
    val squads: Int = 0,
    val matches: Int = 0
)

/**
 * Standings / points row from CricAPI `/series_points`.
 */
@Serializable
data class CricApiSeriesPointsRowDto(
    val teamname: String = "",
    val shortname: String = "",
    val img: String? = null,
    val matches: Int = 0,
    val wins: Int = 0,
    val loss: Int = 0,
    val ties: Int = 0,
    val nr: Int = 0
) {
    val points: Int
        get() = (wins * 2) + ties + nr

    val winRatePercent: Double
        get() = if (matches > 0) ((wins.toDouble() / matches) * 1000.0).roundToInt() / 10.0 else 0.0
}
