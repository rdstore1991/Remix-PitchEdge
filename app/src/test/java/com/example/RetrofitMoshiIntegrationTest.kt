package com.example

import com.example.data.api.NetworkClient
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetrofitMoshiIntegrationTest {

    private fun fakeClient(handler: (String) -> Pair<Int, String>): OkHttpClient {
        val interceptor = Interceptor { chain ->
            val req = chain.request()
            val (code, body) = handler(req.url.toString())
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(if (code in 200..299) "OK" else "Error")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }
        return OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

    @Test
    fun retrofitMoshiDeserializesCricApiScorecardAndPlayerStats() = runTest {
        val cricPayload = """
            {
              "status": "success",
              "data": {
                "id": "m-101",
                "name": "Dubai Capitals vs Desert Vipers",
                "matchType": "t20",
                "status": "Live",
                "venue": "Dubai International Cricket Stadium",
                "date": "2026-11-25",
                "dateTimeGMT": "2026-11-25T14:30:00",
                "teams": ["Dubai Capitals", "Desert Vipers"],
                "tossWinner": "Dubai Capitals",
                "tossChoice": "bowl",
                "series_id": "s-ilt20-5",
                "score": [
                  {"r": 88, "w": 2, "o": 10.2, "inning": "Desert Vipers Inning 1"}
                ],
                "scorecard": [
                  {
                    "inning": "Desert Vipers Inning 1",
                    "batting": [
                      {
                        "batsman": {"id": "b1", "name": "Babar Azam"},
                        "dismissal": "not out",
                        "dismissal-text": "not out",
                        "r": 49,
                        "b": 34,
                        "4s": 5,
                        "6s": 1,
                        "sr": 144.12
                      }
                    ],
                    "bowling": [
                      {
                        "bowler": {"id": "bw1", "name": "Noor Ahmad"},
                        "o": 2.0,
                        "m": 0,
                        "r": 14,
                        "w": 1,
                        "nb": 0,
                        "wd": 0,
                        "eco": 7.0
                      }
                    ],
                    "catching": [
                      {
                        "catcher": {"id": "c1", "name": "Rovman Powell"},
                        "stumped": 0,
                        "runout": 0,
                        "catch": 1,
                        "cb": 0,
                        "lbw": 0,
                        "bowled": 0
                      }
                    ],
                    "extras": {"r": 4, "b": 0, "lb": 1, "w": 3, "nb": 0, "p": 0},
                    "totals": {"R": 88, "O": 10.2, "W": 2, "RR": 8.52}
                  }
                ],
                "matchStarted": true,
                "matchEnded": false
              }
            }
        """.trimIndent()

        val playerPayload = """
            {
              "status": "success",
              "data": {
                "id": "p-babar",
                "name": "Babar Azam",
                "role": "Batsman",
                "country": "Pakistan",
                "stats": [
                  {"fn": "batting", "matchtype": "t20", "stat": "runs", "value": "10980"},
                  {"fn": "batting", "matchtype": "t20", "stat": "avg", "value": "43.6"}
                ]
              }
            }
        """.trimIndent()

        val client = fakeClient { url ->
            when {
                url.contains("match_scorecard") -> 200 to cricPayload
                url.contains("players_info") -> 200 to playerPayload
                else -> 404 to "{}"
            }
        }

        val cricService = NetworkClient.createCricApiService(client = client)
        val scorecardEnv = cricService.getMatchScorecard(apiKey = "test-key", matchId = "m-101")
        assertTrue(scorecardEnv.isSuccess)
        val match = scorecardEnv.data!!
        assertEquals("s-ilt20-5", match.seriesId)
        assertEquals(88, match.score.first().runs)
        val batting = match.scorecard.first().batting.first()
        assertEquals("Babar Azam", batting.batsman.name)
        assertEquals("not out", batting.dismissalText)
        assertEquals(5, batting.fours)
        assertEquals(1, match.scorecard.first().catching.first().catchCount)
        assertEquals(88, match.scorecard.first().totals?.runs)

        val playerEnv = cricService.getPlayerInfo(apiKey = "test-key", playerId = "p-babar")
        val t20Stats = playerEnv.data?.groupStatsByFormat()?.get("t20")
        assertEquals(10980, t20Stats?.batting?.runs)
        assertEquals(43.6, t20Stats?.batting?.average ?: 0.0, 0.01)
    }

    @Test
    fun retrofitMoshiDeserializesOddsApiSportsAndEvents() = runTest {
        val sportsPayload = """
            [
              {
                "key": "cricket_ilt20",
                "group": "Cricket",
                "title": "ILT20",
                "description": "DP World ILT20",
                "active": true,
                "has_outrights": false
              }
            ]
        """.trimIndent()

        val eventsPayload = """
            [
              {
                "id": "odds-evt-1",
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
                      }
                    ]
                  }
                ]
              }
            ]
        """.trimIndent()

        val client = fakeClient { url ->
            when {
                url.contains("/sports/?") -> 200 to sportsPayload
                url.contains("/odds/") -> 200 to eventsPayload
                else -> 404 to "[]"
            }
        }

        val oddsService = NetworkClient.createOddsApiService(client = client)
        val sports = oddsService.getSports(apiKey = "test-key")
        assertEquals(1, sports.size)
        assertEquals("cricket_ilt20", sports.first().key)
        assertEquals(false, sports.first().hasOutrights)

        val events = oddsService.getSportOdds(sportKey = "cricket_ilt20", apiKey = "test-key")
        assertEquals("Dubai Capitals", events.first().homeTeam)
        val analyzed = events.first().toAnalyzedEventOdds()
        assertEquals(2, analyzed.marketProbabilities.size)
        assertEquals(2.20, analyzed.marketProbabilities.first { it.selection == "Dubai Capitals" }.bestBackOdds, 0.001)
    }
}
