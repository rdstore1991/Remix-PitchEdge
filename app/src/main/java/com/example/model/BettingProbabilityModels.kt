package com.example.model

import com.squareup.moshi.Json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.round

/**
 * Active sport descriptor returned by The Odds API v4 `/v4/sports/`.
 */
@Serializable
data class OddsSportKeyDto(
    val key: String = "",
    val group: String = "",
    val title: String = "",
    val description: String = "",
    val active: Boolean = false,
    @SerialName("has_outrights")
    @Json(name = "has_outrights")
    val hasOutrights: Boolean = false
)

@Serializable
data class CricketSportKeysResponse(
    val sports: List<OddsSportKeyDto> = emptyList()
)

/**
 * Outcome quote returned inside a bookmaker market from `/v4/sports/{sport}/odds/`.
 */
@Serializable
data class OddsOutcomeDto(
    val name: String = "",
    val price: Double = 0.0,
    val point: Double? = null,
    val description: String? = null
)

@Serializable
data class OddsMarketDto(
    val key: String = "",
    @SerialName("last_update")
    @Json(name = "last_update")
    val lastUpdate: String? = null,
    val outcomes: List<OddsOutcomeDto> = emptyList()
)

@Serializable
data class OddsBookmakerDto(
    val key: String = "",
    val title: String = "",
    @SerialName("last_update")
    @Json(name = "last_update")
    val lastUpdate: String? = null,
    val markets: List<OddsMarketDto> = emptyList()
)

/**
 * Raw fixture odds payload returned by The Odds API v4 `/v4/sports/{sport}/odds/`.
 */
@Serializable
data class OddsEventDto(
    val id: String = "",
    @SerialName("sport_key")
    @Json(name = "sport_key")
    val sportKey: String = "",
    @SerialName("sport_title")
    @Json(name = "sport_title")
    val sportTitle: String = "",
    @SerialName("commence_time")
    @Json(name = "commence_time")
    val commenceTime: String = "",
    @SerialName("home_team")
    @Json(name = "home_team")
    val homeTeam: String = "",
    @SerialName("away_team")
    @Json(name = "away_team")
    val awayTeam: String = "",
    val bookmakers: List<OddsBookmakerDto> = emptyList()
) {
    /**
     * Strips bookmaker vigorish (overround) across `h2h`, `totals`, and `spreads` markets
     * and computes true market-implied probabilities.
     */
    fun toAnalyzedEventOdds(): AnalyzedEventOdds {
        val bestBackH2h = mutableMapOf<String, Pair<Double, String>>()
        val exchangeLayH2h = mutableMapOf<String, Pair<Double, String>>()
        val totalsByPoint = mutableMapOf<Double, MutableMap<String, Pair<Double, String>>>()
        val spreadsByAbs = mutableMapOf<Double, MutableMap<String, SpreadQuoteKey>>()

        var bookmakerCount = 0

        for (bookie in bookmakers) {
            val isExchange = bookie.key.contains("betfair", ignoreCase = true) ||
                bookie.key.contains("smarkets", ignoreCase = true) ||
                bookie.key.contains("matchbook", ignoreCase = true)

            for (market in bookie.markets) {
                if (market.outcomes.isEmpty()) continue
                when (market.key) {
                    "h2h" -> {
                        bookmakerCount++
                        for (outcome in market.outcomes) {
                            if (outcome.price <= 0.0) continue
                            val prev = bestBackH2h[outcome.name]
                            if (prev == null || outcome.price > prev.first) {
                                bestBackH2h[outcome.name] = outcome.price to bookie.title
                            }
                            if (isExchange) {
                                val layEst = ProbabilityMath.round2(outcome.price + 0.02)
                                val prevLay = exchangeLayH2h[outcome.name]
                                if (prevLay == null || layEst < prevLay.first) {
                                    exchangeLayH2h[outcome.name] = layEst to bookie.title
                                }
                            }
                        }
                    }

                    "h2h_lay" -> {
                        for (outcome in market.outcomes) {
                            if (outcome.price <= 0.0) continue
                            val prevLay = exchangeLayH2h[outcome.name]
                            if (prevLay == null || outcome.price < prevLay.first) {
                                exchangeLayH2h[outcome.name] = outcome.price to bookie.title
                            }
                        }
                    }

                    "totals" -> {
                        for (outcome in market.outcomes) {
                            val point = outcome.point ?: continue
                            if (outcome.price <= 0.0) continue
                            val sideMap = totalsByPoint.getOrPut(point) { mutableMapOf() }
                            val prev = sideMap[outcome.name]
                            if (prev == null || outcome.price > prev.first) {
                                sideMap[outcome.name] = outcome.price to bookie.title
                            }
                        }
                    }

                    "spreads" -> {
                        for (outcome in market.outcomes) {
                            val point = outcome.point ?: continue
                            if (outcome.price <= 0.0) continue
                            val absLine = abs(point)
                            val lineMap = spreadsByAbs.getOrPut(absLine) { mutableMapOf() }
                            val compositeKey = "${outcome.name}|$point"
                            val prev = lineMap[compositeKey]
                            if (prev == null || outcome.price > prev.bestOdds) {
                                lineMap[compositeKey] = SpreadQuoteKey(
                                    teamName = outcome.name,
                                    signedPoint = point,
                                    bestOdds = outcome.price,
                                    bookmaker = bookie.title
                                )
                            }
                        }
                    }
                }
            }
        }

        val h2hRaw = bestBackH2h.entries.map { (name, pair) ->
            Triple(name, pair, 1.0 / pair.first)
        }
        val h2hTotalImplied = h2hRaw.sumOf { it.third }
        val h2hOverround = ProbabilityMath.overroundPercent(h2hTotalImplied)
        val marketProbs = h2hRaw.map { (name, pair, rawImplied) ->
            MarketProbabilityQuote(
                selection = name,
                bestBackOdds = pair.first,
                bestBookmaker = pair.second,
                exchangeLayOdds = exchangeLayH2h[name]?.first,
                rawImpliedPercent = ProbabilityMath.round2(rawImplied * 100.0),
                trueVigFreeMarketPercent = if (h2hTotalImplied > 0.0) {
                    ProbabilityMath.round2((rawImplied / h2hTotalImplied) * 100.0)
                } else {
                    0.0
                }
            )
        }

        val totalsGroups = totalsByPoint.toSortedMap().map { (linePoint, sideMap) ->
            val entries = sideMap.entries.map { (side, pair) -> Triple(side, pair, 1.0 / pair.first) }
            val sumRaw = entries.sumOf { it.third }
            TotalsMarketGroup(
                linePoint = linePoint,
                marketOverroundPercent = ProbabilityMath.overroundPercent(sumRaw),
                outcomes = entries.map { (side, pair, rawImp) ->
                    TotalsOutcomeQuote(
                        selection = "$side $linePoint",
                        side = side,
                        linePoint = linePoint,
                        bestBackOdds = pair.first,
                        bestBookmaker = pair.second,
                        rawImpliedPercent = ProbabilityMath.round2(rawImp * 100.0),
                        trueVigFreeMarketPercent = if (sumRaw > 0.0) {
                            ProbabilityMath.round2((rawImp / sumRaw) * 100.0)
                        } else {
                            0.0
                        }
                    )
                }
            )
        }

        val spreadsGroups = spreadsByAbs.toSortedMap().map { (absSpread, lineMap) ->
            val entries = lineMap.values.map { cand -> cand to (1.0 / cand.bestOdds) }
            val sumRaw = entries.sumOf { it.second }
            SpreadsMarketGroup(
                absSpread = absSpread,
                marketOverroundPercent = ProbabilityMath.overroundPercent(sumRaw),
                outcomes = entries.map { (cand, rawImp) ->
                    val signedStr = if (cand.signedPoint > 0) "+${cand.signedPoint}" else "${cand.signedPoint}"
                    SpreadsOutcomeQuote(
                        selection = "${cand.teamName} $signedStr",
                        linePoint = cand.signedPoint,
                        bestBackOdds = cand.bestOdds,
                        bestBookmaker = cand.bookmaker,
                        rawImpliedPercent = ProbabilityMath.round2(rawImp * 100.0),
                        trueVigFreeMarketPercent = if (sumRaw > 0.0) {
                            ProbabilityMath.round2((rawImp / sumRaw) * 100.0)
                        } else {
                            0.0
                        }
                    )
                }
            )
        }

        return AnalyzedEventOdds(
            eventId = id,
            sportKey = sportKey,
            sportTitle = sportTitle,
            commenceTime = commenceTime,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            marketOverroundPercent = h2hOverround,
            marketProbabilities = marketProbs,
            totalsMarkets = totalsGroups,
            spreadsMarkets = spreadsGroups,
            bookmakerCount = bookmakerCount
        )
    }
}

private data class SpreadQuoteKey(
    val teamName: String,
    val signedPoint: Double,
    val bestOdds: Double,
    val bookmaker: String
)

/**
 * Vig-stripped H2H outcome probability quote.
 */
@Serializable
data class MarketProbabilityQuote(
    val selection: String = "",
    val bestBackOdds: Double = 0.0,
    val bestBookmaker: String = "",
    val exchangeLayOdds: Double? = null,
    val rawImpliedPercent: Double = 0.0,
    val trueVigFreeMarketPercent: Double = 0.0
) {
    fun edgeAgainst(modelWinPercent: Double): Double =
        ProbabilityMath.round2(modelWinPercent - trueVigFreeMarketPercent)

    fun expectedValuePercent(modelWinPercent: Double): Double =
        ProbabilityMath.expectedValuePercent(modelWinPercent, bestBackOdds)
}

@Serializable
data class TotalsOutcomeQuote(
    val selection: String = "",
    val side: String = "",
    val linePoint: Double = 0.0,
    val bestBackOdds: Double = 0.0,
    val bestBookmaker: String = "",
    val rawImpliedPercent: Double = 0.0,
    val trueVigFreeMarketPercent: Double = 0.0
)

@Serializable
data class TotalsMarketGroup(
    val linePoint: Double = 0.0,
    val marketOverroundPercent: Double = 0.0,
    val outcomes: List<TotalsOutcomeQuote> = emptyList()
)

@Serializable
data class SpreadsOutcomeQuote(
    val selection: String = "",
    val linePoint: Double = 0.0,
    val bestBackOdds: Double = 0.0,
    val bestBookmaker: String = "",
    val rawImpliedPercent: Double = 0.0,
    val trueVigFreeMarketPercent: Double = 0.0
)

@Serializable
data class SpreadsMarketGroup(
    val absSpread: Double = 0.0,
    val marketOverroundPercent: Double = 0.0,
    val outcomes: List<SpreadsOutcomeQuote> = emptyList()
)

@Serializable
data class AnalyzedEventOdds(
    val eventId: String = "",
    val sportKey: String = "",
    val sportTitle: String = "",
    val commenceTime: String = "",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val marketOverroundPercent: Double = 0.0,
    val marketProbabilities: List<MarketProbabilityQuote> = emptyList(),
    val totalsMarkets: List<TotalsMarketGroup> = emptyList(),
    val spreadsMarkets: List<SpreadsMarketGroup> = emptyList(),
    val bookmakerCount: Int = 0
)

@Serializable
data class AnalyzedOddsResponse(
    val sportKey: String = "",
    val eventsFound: Int = 0,
    val events: List<AnalyzedEventOdds> = emptyList()
)

/**
 * Quantitative probability, vig-removal, and Kelly Criterion sizing utilities.
 */
object ProbabilityMath {
    fun round2(value: Double): Double = round(value * 100.0) / 100.0

    fun overroundPercent(totalImpliedProbSum: Double): Double =
        if (totalImpliedProbSum > 0.0) round2((totalImpliedProbSum - 1.0) * 100.0) else 0.0

    fun fairOddsFromPercent(winPercent: Double): Double =
        if (winPercent > 0.0) round2(100.0 / winPercent) else 0.0

    fun expectedValuePercent(modelWinPercent: Double, decimalOdds: Double): Double {
        if (decimalOdds <= 1.0 || modelWinPercent <= 0.0) return 0.0
        val p = modelWinPercent / 100.0
        return round2(((p * decimalOdds) - 1.0) * 100.0)
    }

    fun fractionalKellyFraction(
        modelWinPercent: Double,
        decimalOdds: Double,
        kellyMultiplier: Double = 0.25
    ): Double {
        if (decimalOdds <= 1.0 || modelWinPercent <= 0.0) return 0.0
        val p = (modelWinPercent / 100.0).coerceIn(0.0, 1.0)
        val q = 1.0 - p
        val b = decimalOdds - 1.0
        val fullKelly = ((b * p) - q) / b
        return if (fullKelly > 0.0) fullKelly * kellyMultiplier else 0.0
    }
}
