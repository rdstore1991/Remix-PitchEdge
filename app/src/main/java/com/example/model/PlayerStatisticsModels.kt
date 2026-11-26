package com.example.model

import kotlinx.serialization.Serializable

/**
 * Individual stat entry returned in CricAPI `/players_info` (`data.stats[]`).
 * Each entry maps `(fn, matchtype, stat)` -> `value` (e.g., `fn="batting", matchtype="t20", stat="sr", value="142.8"`).
 */
@Serializable
data class PlayerStatEntryDto(
    val fn: String = "",
    val matchtype: String = "",
    val stat: String = "",
    val value: String = ""
)

/**
 * Full player profile & career statistics payload from CricAPI `/players_info`.
 */
@Serializable
data class PlayerProfileDto(
    val id: String = "",
    val name: String = "",
    val dateOfBirth: String? = null,
    val role: String = "",
    val battingStyle: String? = null,
    val bowlingStyle: String? = null,
    val placeOfBirth: String? = null,
    val country: String = "",
    val playerImg: String? = null,
    val stats: List<PlayerStatEntryDto> = emptyList()
) {
    /**
     * Groups flat CricAPI stat rows by cricket format (`t20`, `t20i`, `odi`, `test`, `ipl`, etc.)
     * into strongly typed batting and bowling statistics.
     */
    fun groupStatsByFormat(): Map<String, PlayerFormatStatistics> {
        if (stats.isEmpty()) return emptyMap()
        return stats
            .groupBy { it.matchtype.trim().lowercase() }
            .filterKeys { it.isNotBlank() }
            .mapValues { (format, entries) ->
                val battingMap = entries
                    .filter { it.fn.equals("batting", ignoreCase = true) }
                    .associate { it.stat.trim().lowercase() to it.value.trim() }
                val bowlingMap = entries
                    .filter { it.fn.equals("bowling", ignoreCase = true) }
                    .associate { it.stat.trim().lowercase() to it.value.trim() }

                PlayerFormatStatistics(
                    format = format,
                    batting = battingMap.takeIf { it.isNotEmpty() }?.let { PlayerBattingMetrics.fromMap(it) },
                    bowling = bowlingMap.takeIf { it.isNotEmpty() }?.let { PlayerBowlingMetrics.fromMap(it) }
                )
            }
    }
}

/**
 * Player item inside CricAPI `/match_squad` or `/series_squad`.
 */
@Serializable
data class SquadPlayerDto(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val battingStyle: String? = null,
    val bowlingStyle: String? = null,
    val country: String = "",
    val playerImg: String? = null
)

/**
 * Team roster entry inside CricAPI `/match_squad` or `/series_squad`.
 */
@Serializable
data class MatchSquadTeamDto(
    val teamName: String = "",
    val shortname: String = "",
    val img: String? = null,
    val players: List<SquadPlayerDto> = emptyList()
)

@Serializable
data class PlayerBattingMetrics(
    val matches: Int = 0,
    val innings: Int = 0,
    val notOuts: Int = 0,
    val runs: Int = 0,
    val highScore: String = "0",
    val average: Double = 0.0,
    val ballsFaced: Int = 0,
    val strikeRate: Double = 0.0,
    val hundreds: Int = 0,
    val twoHundreds: Int = 0,
    val fifties: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0
) {
    companion object {
        fun fromMap(values: Map<String, String>): PlayerBattingMetrics = PlayerBattingMetrics(
            matches = values["m"]?.toCleanInt() ?: 0,
            innings = values["inn"]?.toCleanInt() ?: 0,
            notOuts = values["no"]?.toCleanInt() ?: 0,
            runs = values["runs"]?.toCleanInt() ?: 0,
            highScore = values["hs"]?.takeIf { it.isNotBlank() } ?: "0",
            average = values["avg"]?.toCleanDouble() ?: 0.0,
            ballsFaced = values["bf"]?.toCleanInt() ?: 0,
            strikeRate = values["sr"]?.toCleanDouble() ?: 0.0,
            hundreds = values["100s"]?.toCleanInt() ?: values["100"]?.toCleanInt() ?: 0,
            twoHundreds = values["200s"]?.toCleanInt() ?: values["200"]?.toCleanInt() ?: 0,
            fifties = values["50s"]?.toCleanInt() ?: values["50"]?.toCleanInt() ?: 0,
            fours = values["4s"]?.toCleanInt() ?: 0,
            sixes = values["6s"]?.toCleanInt() ?: 0
        )
    }
}

@Serializable
data class PlayerBowlingMetrics(
    val matches: Int = 0,
    val innings: Int = 0,
    val balls: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val bestInnings: String = "0/0",
    val bestMatch: String = "0/0",
    val average: Double = 0.0,
    val economy: Double = 0.0,
    val strikeRate: Double = 0.0,
    val fiveWickets: Int = 0,
    val tenWickets: Int = 0
) {
    companion object {
        fun fromMap(values: Map<String, String>): PlayerBowlingMetrics = PlayerBowlingMetrics(
            matches = values["m"]?.toCleanInt() ?: 0,
            innings = values["inn"]?.toCleanInt() ?: 0,
            balls = values["b"]?.toCleanInt() ?: 0,
            runsConceded = values["runs"]?.toCleanInt() ?: 0,
            wickets = values["wkts"]?.toCleanInt() ?: 0,
            bestInnings = values["bbi"]?.takeIf { it.isNotBlank() } ?: "0/0",
            bestMatch = values["bbm"]?.takeIf { it.isNotBlank() } ?: "0/0",
            average = values["avg"]?.toCleanDouble() ?: 0.0,
            economy = values["econ"]?.toCleanDouble() ?: 0.0,
            strikeRate = values["sr"]?.toCleanDouble() ?: 0.0,
            fiveWickets = values["5w"]?.toCleanInt() ?: 0,
            tenWickets = values["10w"]?.toCleanInt() ?: 0
        )
    }
}

@Serializable
data class PlayerFormatStatistics(
    val format: String,
    val batting: PlayerBattingMetrics? = null,
    val bowling: PlayerBowlingMetrics? = null
)

private fun String.toCleanInt(): Int =
    trim().replace(",", "").toDoubleOrNull()?.toInt() ?: 0

private fun String.toCleanDouble(): Double =
    trim().replace(",", "").toDoubleOrNull() ?: 0.0
