package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class PitchEdgeMarketQuote(
    val marketType: String = "h2h", // "h2h", "totals", "spreads"
    val selection: String = "",
    val linePoint: Double? = null,
    val bestBackOdds: Double? = null,
    val exchangeLayOdds: Double? = null,
    val bestBookmaker: String = "Unlisted",
    val marketOverroundPercent: Double? = null,
    val trueMarketImpliedPercent: Double? = null,
    val pitchEdgeModelPercent: Double = 50.0,
    val fairValueOdds: Double = 2.0,
    val edgePercent: Double? = null
)

@Serializable
data class PitchEdgeMicroFactors(
    val eveningDewFactor: String = "",
    val powerplayEdge: String = "",
    val middleOversEdge: String = "",
    val deathOversEdge: String = "",
    val keyPlayerMatchup: String = "",
    val marketLagFlag: Boolean = false,
    val marketLagReason: String = ""
)

@Serializable
data class PitchEdgeProjections(
    val projectedFirstInningsRange: String = "",
    val projectedSecondInningsRange: String = "",
    val projectedMatchTotalRange: String = ""
)

@Serializable
data class PitchEdgeRecommendation(
    val action: String = "NO_BET_PASS", // "BET" or "NO_BET_PASS"
    val topMarketType: String = "h2h",
    val topSelection: String = "",
    val bestBookmaker: String = "Unlisted",
    val marketOdds: Double? = null,
    val fairValueOdds: Double = 2.0,
    val edgePercent: Double = 0.0,
    val unitStake: Int = 0, // 1..5 for BET, 0 for NO_BET_PASS
    val rationale: String = "",
    val invalidationTrigger: String = ""
)

@Serializable
data class PitchEdgeAnalysisResponse(
    val fixtureTitle: String = "",
    val competition: String = "",
    val matchStatus: String = "UPCOMING", // LIVE, UPCOMING, COMPLETED
    val scoreTelemetry: String = "",
    val venue: String = "",
    val tossOutcome: String = "Pending",
    val h2hMarkets: List<PitchEdgeMarketQuote> = emptyList(),
    val totalsMarkets: List<PitchEdgeMarketQuote> = emptyList(),
    val spreadsMarkets: List<PitchEdgeMarketQuote> = emptyList(),
    val projections: PitchEdgeProjections = PitchEdgeProjections(),
    val microFactors: PitchEdgeMicroFactors = PitchEdgeMicroFactors(),
    val recommendation: PitchEdgeRecommendation = PitchEdgeRecommendation()
) {
    val allMarkets: List<PitchEdgeMarketQuote>
        get() = h2hMarkets + totalsMarkets + spreadsMarkets

    fun normalized(): PitchEdgeAnalysisResponse {
        val rec = recommendation
        val enforcedAction = if (rec.action == "BET" && rec.edgePercent >= 3.0 && rec.marketOdds != null) {
            "BET"
        } else {
            "NO_BET_PASS"
        }
        val enforcedUnits = if (enforcedAction == "BET") {
            rec.unitStake.coerceIn(1, 5)
        } else {
            0
        }
        return copy(
            tossOutcome = tossOutcome.ifBlank { "Pending" },
            h2hMarkets = h2hMarkets.map {
                it.copy(
                    marketType = it.marketType.ifBlank { "h2h" },
                    bestBookmaker = it.bestBookmaker.ifBlank { "Unlisted" }
                )
            },
            totalsMarkets = totalsMarkets.map {
                it.copy(
                    marketType = it.marketType.ifBlank { "totals" },
                    bestBookmaker = it.bestBookmaker.ifBlank { "Unlisted" }
                )
            },
            spreadsMarkets = spreadsMarkets.map {
                it.copy(
                    marketType = it.marketType.ifBlank { "spreads" },
                    bestBookmaker = it.bestBookmaker.ifBlank { "Unlisted" }
                )
            },
            recommendation = rec.copy(
                action = enforcedAction,
                bestBookmaker = rec.bestBookmaker.ifBlank { "Unlisted" },
                unitStake = enforcedUnits
            )
        )
    }

    companion object {
        fun fromJson(jsonString: String): PitchEdgeAnalysisResponse =
            ApiJson.lenient.decodeFromString<PitchEdgeAnalysisResponse>(jsonString).normalized()
    }
}
