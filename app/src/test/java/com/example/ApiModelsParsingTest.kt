package com.example

import com.example.model.ApiJson
import com.example.model.CricApiEnvelope
import com.example.model.CricApiSeriesPointsRowDto
import com.example.model.CricketMatchScorecardDto
import com.example.model.MatchSquadTeamDto
import com.example.model.OddsEventDto
import com.example.model.OddsSportKeyDto
import com.example.model.PitchEdgeAnalysisResponse
import com.example.model.PlayerProfileDto
import com.example.model.ProbabilityMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiModelsParsingTest {

    @Test
    fun parsesCricApiMatchScorecardAndComputesTrueRunRate() {
        val rawJson = """
            {
              "status": "success",
              "data": {
                "id": "m-ilt20-6",
                "name": "Dubai Capitals vs Desert Vipers, 6th Match",
                "matchType": "t20",
                "status": "Desert Vipers opt to bat",
                "venue": "Dubai International Cricket Stadium",
                "date": "2026-11-25",
                "dateTimeGMT": "2026-11-25T14:30:00",
                "teams": ["Desert Vipers", "Dubai Capitals"],
                "teamInfo": [
                  {"name": "Desert Vipers", "shortname": "DV"},
                  {"name": "Dubai Capitals", "shortname": "DC"}
                ],
                "tossWinner": "Desert Vipers",
                "tossChoice": "bat",
                "matchWinner": null,
                "score": [
                  {"r": 124, "w": 3, "o": 14.3, "inning": "Desert Vipers Inning 1"}
                ],
                "scorecard": [
                  {
                    "inning": "Desert Vipers Inning 1",
                    "batting": [
                      {
                        "batsman": {"id": "p1", "name": "Fakhar Zaman"},
                        "dismissal": "caught",
                        "bowler": {"id": "b1", "name": "Mustafizur Rahman"},
                        "catcher": {"id": "c1", "name": "Sam Billings"},
                        "dismissal-text": "c Billings b Mustafizur",
                        "r": 54,
                        "b": 31,
                        "4s": 6,
                        "6s": 2,
                        "sr": 174.19
                      }
                    ],
                    "bowling": [
                      {
                        "bowler": {"id": "b1", "name": "Mustafizur Rahman"},
                        "o": 3.0,
                        "m": 0,
                        "r": 21,
                        "w": 2,
                        "nb": 0,
                        "wd": 1,
                        "eco": 7.0
                      }
                    ],
                    "catching": [
                      {
                        "catcher": {"id": "c1", "name": "Sam Billings"},
                        "stumped": 0,
                        "runout": 0,
                        "catch": 1,
                        "cb": 0,
                        "lbw": 0,
                        "bowled": 0
                      }
                    ],
                    "extras": {"r": 5, "b": 0, "lb": 2, "w": 3, "nb": 0, "p": 0},
                    "totals": {"R": 124, "O": 14.3, "W": 3, "RR": 8.55}
                  }
                ],
                "matchStarted": true,
                "matchEnded": false
              }
            }
        """.trimIndent()

        val envelope = ApiJson.lenient.decodeFromString<CricApiEnvelope<CricketMatchScorecardDto>>(rawJson)
        assertTrue(envelope.isSuccess)
        val match = envelope.data!!
        assertEquals("Desert Vipers", match.tossWinner)
        assertEquals(14.5, match.score.first().trueOvers, 0.001)
        assertEquals(8.55, match.score.first().computedRunRate, 0.01)

        val inn = match.scorecard.first()
        assertEquals("Fakhar Zaman", inn.batting.first().batsman.name)
        assertEquals(36, inn.batting.first().boundaryRuns)
        assertEquals("c Billings b Mustafizur", inn.batting.first().dismissalText)
        assertEquals(2, inn.bowling.first().wickets)
        assertEquals(1, inn.catching.first().catchCount)
        assertEquals(5, inn.extras?.total)
        assertEquals(124, inn.totals?.runs)
    }

    @Test
    fun parsesCricApiPlayerStatsAndSquadModels() {
        val playerJson = """
            {
              "status": "success",
              "data": {
                "id": "p-shadab",
                "name": "Shadab Khan",
                "role": "Batting Allrounder",
                "battingStyle": "Right Handed Bat",
                "bowlingStyle": "Right-arm legbreak",
                "country": "Pakistan",
                "stats": [
                  {"fn": "batting", "matchtype": "t20", "stat": "m", "value": "295"},
                  {"fn": "batting", "matchtype": "t20", "stat": "runs", "value": "3420"},
                  {"fn": "batting", "matchtype": "t20", "stat": "sr", "value": "139.5"},
                  {"fn": "bowling", "matchtype": "t20", "stat": "wkts", "value": "331"},
                  {"fn": "bowling", "matchtype": "t20", "stat": "econ", "value": "7.38"}
                ]
              }
            }
        """.trimIndent()

        val playerEnvelope = ApiJson.lenient.decodeFromString<CricApiEnvelope<PlayerProfileDto>>(playerJson)
        val player = playerEnvelope.data!!
        val grouped = player.groupStatsByFormat()
        val t20 = grouped["t20"]
        assertNotNull(t20)
        assertEquals(3420, t20?.batting?.runs)
        assertEquals(139.5, t20?.batting?.strikeRate ?: 0.0, 0.01)
        assertEquals(331, t20?.bowling?.wickets)
        assertEquals(7.38, t20?.bowling?.economy ?: 0.0, 0.01)

        val squadJson = """
            {
              "status": "success",
              "data": [
                {
                  "teamName": "Dubai Capitals",
                  "shortname": "DC",
                  "players": [
                    {"id": "p1", "name": "Sam Billings", "role": "WK-Batsman", "country": "England"}
                  ]
                }
              ]
            }
        """.trimIndent()
        val squadEnv = ApiJson.lenient.decodeFromString<CricApiEnvelope<List<MatchSquadTeamDto>>>(squadJson)
        assertEquals("DC", squadEnv.data?.first()?.shortname)
        assertEquals("Sam Billings", squadEnv.data?.first()?.players?.first()?.name)

        val pointsJson = """
            {
              "status": "success",
              "data": [
                {"teamname": "Gulf Giants", "shortname": "GG", "matches": 2, "wins": 2, "loss": 0, "ties": 0, "nr": 0}
              ]
            }
        """.trimIndent()
        val pointsEnv = ApiJson.lenient.decodeFromString<CricApiEnvelope<List<CricApiSeriesPointsRowDto>>>(pointsJson)
        assertEquals(4, pointsEnv.data?.first()?.points)
        assertEquals(100.0, pointsEnv.data?.first()?.winRatePercent ?: 0.0, 0.01)
    }

    @Test
    fun parsesOddsApiEventsAndStripsVigAcrossMarkets() {
        val sportsJson = """
            [
              {"key": "cricket_ilt20", "group": "Cricket", "title": "ILT20", "description": "DP World ILT20", "active": true, "has_outrights": false}
            ]
        """.trimIndent()
        val sports = ApiJson.lenient.decodeFromString<List<OddsSportKeyDto>>(sportsJson)
        assertEquals("cricket_ilt20", sports.first().key)

        val eventsJson = """
            [
              {
                "id": "evt-1",
                "sport_key": "cricket_ilt20",
                "sport_title": "ILT20",
                "commence_time": "2026-11-25T14:30:00Z",
                "home_team": "Dubai Capitals",
                "away_team": "Desert Vipers",
                "bookmakers": [
                  {
                    "key": "pinnacle",
                    "title": "Pinnacle",
                    "markets": [
                      {
                        "key": "h2h",
                        "outcomes": [
                          {"name": "Dubai Capitals", "price": 2.20},
                          {"name": "Desert Vipers", "price": 1.72}
                        ]
                      },
                      {
                        "key": "totals",
                        "outcomes": [
                          {"name": "Over", "price": 1.91, "point": 162.5},
                          {"name": "Under", "price": 1.91, "point": 162.5}
                        ]
                      },
                      {
                        "key": "spreads",
                        "outcomes": [
                          {"name": "Desert Vipers", "price": 1.95, "point": -10.5},
                          {"name": "Dubai Capitals", "price": 1.88, "point": 10.5}
                        ]
                      }
                    ]
                  },
                  {
                    "key": "betfair_ex_uk",
                    "title": "Betfair Exchange",
                    "markets": [
                      {
                        "key": "h2h_lay",
                        "outcomes": [
                          {"name": "Dubai Capitals", "price": 2.24},
                          {"name": "Desert Vipers", "price": 1.75}
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
        """.trimIndent()

        val events = ApiJson.lenient.decodeFromString<List<OddsEventDto>>(eventsJson)
        val analyzed = events.first().toAnalyzedEventOdds()

        assertEquals("evt-1", analyzed.eventId)
        assertEquals(1, analyzed.bookmakerCount)
        assertTrue(analyzed.marketOverroundPercent > 0.0)

        val dcQuote = analyzed.marketProbabilities.first { it.selection == "Dubai Capitals" }
        val dvQuote = analyzed.marketProbabilities.first { it.selection == "Desert Vipers" }
        assertEquals(2.24, dcQuote.exchangeLayOdds ?: 0.0, 0.001)
        assertEquals(100.0, dcQuote.trueVigFreeMarketPercent + dvQuote.trueVigFreeMarketPercent, 0.1)

        // With 48.0% model probability at 2.20 odds: EV = (0.48 * 2.20 - 1) * 100 = +5.6%
        assertEquals(5.6, dcQuote.expectedValuePercent(48.0), 0.1)
        assertTrue(ProbabilityMath.fractionalKellyFraction(48.0, 2.20, 0.25) > 0.0)

        assertEquals(162.5, analyzed.totalsMarkets.first().linePoint, 0.01)
        assertEquals(10.5, analyzed.spreadsMarkets.first().absSpread, 0.01)
    }

    @Test
    fun parsesPitchEdgeAnalysisJsonWithStakeGuardrail() {
        val json = """
            {
              "fixtureTitle": "DC vs DV",
              "competition": "ILT20 2026",
              "matchStatus": "UPCOMING",
              "recommendation": {
                "action": "BET",
                "topMarketType": "h2h",
                "topSelection": "Dubai Capitals",
                "bestBookmaker": "Pinnacle",
                "marketOdds": 2.20,
                "fairValueOdds": 2.13,
                "edgePercent": 1.5,
                "unitStake": 3
              }
            }
        """.trimIndent()

        val parsed = PitchEdgeAnalysisResponse.fromJson(json)
        assertEquals("NO_BET_PASS", parsed.recommendation.action)
        assertEquals(0, parsed.recommendation.unitStake)
    }
}
