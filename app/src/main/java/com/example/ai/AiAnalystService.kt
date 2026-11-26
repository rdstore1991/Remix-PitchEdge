package com.example.ai

import android.util.Log
import com.example.data.LiveSportsDataService
import com.example.model.PitchEdgeAnalysisResponse
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import org.json.JSONObject
import java.util.Locale

private const val TAG = "AiAnalystService"

val PITCH_EDGE_SYSTEM_PROMPT = """
# ROLE & OBJECTIVE
You are **PitchEdge AI**, a quantitative cricket and sports trading analyst covering International and Domestic Cricket (Tests, ODIs, T20Is, IPL, BBL, WBBL, PSL, CPL, SA20, The Hundred, and domestic List-A/T20 leagues) alongside global sports.

Your objective is to synthesize live match telemetry, toss/pitch/dew dynamics, player phase matchups, and multi-bookmaker/exchange odds into vig-free win probabilities, innings total projections, run-line edges, and disciplined unit stake recommendations.

# TOOL ORCHESTRATION PROTOCOL
Never guess live scores, match statuses, or betting odds. Always follow this execution sequence:
1. **Verify Live Telemetry & Context:** Call `get_live_cricket_matches` to locate the exact match ID, status (`LIVE`, `UPCOMING`, `COMPLETED`), venue, teams, and current score. If live or recently tossed, call `get_cricket_match_scorecard` using `matchId` to inspect toss winner/choice, innings progression, and CRR.
2. **Discover Active Odds Markets:** Call `get_cricket_sport_keys` to identify the active tournament sport key.
3. **Harvest & Strip Vig Across Three Core Markets:** Call `get_live_match_odds` with `sportKey` and target team names. It harvests `h2h`, `totals` (grouped by `linePoint`), and `spreads` (grouped by absolute margin) with pre-calculated `bestBackOdds`, `exchangeLayOdds`, `marketOverroundPercent`, and `trueVigFreeMarketPercent`.
4. **Compute Expected Value (+EV) & Fair Value Odds:**
   - `edgePercent = pitchEdgeModelPercent - trueMarketImpliedPercent`
   - `fairValueOdds = 100 / pitchEdgeModelPercent`
5. **Cricket Micro-Factor Adjustments:**
   - Adjust win probabilities and 2nd-innings totals for evening dew in day/night white-ball matches, powerplay/middle/death overs run-rate differentials, and key player matchups.
   - Flag **Market Lag** during live chases when exchange/bookmaker lines overreact to a single top-order wicket despite deep batting resources remaining.
6. **Bankroll & Unit Stake Discipline:**
   - Recommend `"BET"` ONLY when the highest cross-market `edgePercent >= 3.0` and live odds are listed. Scale `unitStake` from `1` to `5` units (`0` and `"NO_BET_PASS"` if no market clears `+3.0%`).
   - If a domestic fixture has no active bookmaker odds, set odds fields to `null`, `bestBookmaker` to `"Unlisted"`, and still output `pitchEdgeModelPercent` and `fairValueOdds`.
""".trimIndent()

data class GroundedSource(
    val title: String,
    val url: String
)

data class GroundedIntelResult(
    val summary: String,
    val searchQueries: List<String> = emptyList(),
    val sources: List<GroundedSource> = emptyList()
)

sealed interface AiStreamEvent {
    data class ToolProgress(val toolNames: List<String>) : AiStreamEvent
    data class Chunk(
        val accumulatedText: String,
        val searchQueries: List<String> = emptyList(),
        val sources: List<GroundedSource> = emptyList()
    ) : AiStreamEvent

    data class Completed(
        val fullText: String,
        val searchQueries: List<String> = emptyList(),
        val sources: List<GroundedSource> = emptyList(),
        val structuredAnalysis: PitchEdgeAnalysisResponse? = null
    ) : AiStreamEvent
}

class AiAnalystService(
    private val sportsDataService: LiveSportsDataService = LiveSportsDataService()
) {

    private val searchGroundedModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.googleSearch()),
            systemInstruction = content {
                text(
                    """
                    You are PitchEdge AI, a quantitative sports trading analyst.
                    Provide crisp, data-backed answers using tabular numbers, decimal odds, implied probabilities,
                    Expected Value (+EV %), Fractional Kelly stake sizing (1–5 units), and tactical phase analysis.
                    Use Google Search Grounding whenever the user asks for live scores, latest team sheets,
                    toss updates, injuries, weather, or current bookmaker odds.
                    """.trimIndent()
                )
            }
        )
    }

    private val pitchEdgeToolDeclarations: List<FunctionDeclaration> by lazy {
        listOf(
            FunctionDeclaration(
                name = "get_live_cricket_matches",
                description = "Fetches currently live and upcoming international and domestic cricket matches with match IDs, teams, venues, and summary scores.",
                parameters = mapOf(
                    "teamFilter" to Schema.string(
                        description = "Optional team or tournament name to filter results (e.g., \"India\", \"Australia\", \"Dubai Capitals\", \"Desert Vipers\").",
                        nullable = true
                    )
                ),
                optionalParameters = listOf("teamFilter")
            ),
            FunctionDeclaration(
                name = "get_cricket_match_scorecard",
                description = "Fetches detailed live telemetry for a specific cricket match ID, including toss winner, toss decision, venue, CRR, and innings-by-innings runs, wickets, and overs.",
                parameters = mapOf(
                    "matchId" to Schema.string(
                        description = "The unique match ID returned by get_live_cricket_matches."
                    )
                )
            ),
            FunctionDeclaration(
                name = "get_cricket_sport_keys",
                description = "Retrieves the list of currently active cricket tournament sport keys from The Odds API (e.g., cricket_international_t20, cricket_odi, cricket_test_match, cricket_big_bash).",
                parameters = emptyMap()
            ),
            FunctionDeclaration(
                name = "get_live_match_odds",
                description = "Harvests live decimal odds across h2h, totals, and spreads from UK, EU, and AU sportsbooks and exchanges for a given sportKey, and calculates vig-free implied probabilities.",
                parameters = mapOf(
                    "sportKey" to Schema.string(
                        description = "Exact active sport key from get_cricket_sport_keys (e.g., \"cricket_international_t20\")."
                    ),
                    "teamQuery" to Schema.string(
                        description = "Optional team name to filter odds to a specific fixture.",
                        nullable = true
                    )
                ),
                optionalParameters = listOf("teamQuery")
            )
        )
    }

    private val pitchEdgeOrchestratorModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.2f
            },
            tools = listOf(Tool.functionDeclarations(pitchEdgeToolDeclarations)),
            systemInstruction = content {
                text(PITCH_EDGE_SYSTEM_PROMPT)
            }
        )
    }

    private val pitchEdgeAnalysisSchema: Schema by lazy {
        val marketQuoteSchema = Schema.obj(
            properties = mapOf(
                "marketType" to Schema.enumeration(
                    values = listOf("h2h", "totals", "spreads"),
                    description = "Market type: h2h, totals, or spreads."
                ),
                "selection" to Schema.string(description = "Selection label, e.g. team name, Over 165.5, or Team -12.5."),
                "linePoint" to Schema.double(description = "Run threshold or spread handicap margin if totals/spreads.", nullable = true),
                "bestBackOdds" to Schema.double(description = "Best decimal back odds or null if unlisted.", nullable = true),
                "exchangeLayOdds" to Schema.double(description = "Exchange lay odds if available, otherwise null.", nullable = true),
                "bestBookmaker" to Schema.string(description = "Best bookmaker name or \"Unlisted\"."),
                "marketOverroundPercent" to Schema.double(description = "Bookmaker overround % or null if unlisted.", nullable = true),
                "trueMarketImpliedPercent" to Schema.double(description = "Vig-free market implied win probability % or null if unlisted.", nullable = true),
                "pitchEdgeModelPercent" to Schema.double(description = "PitchEdge model win probability %."),
                "fairValueOdds" to Schema.double(description = "100 / pitchEdgeModelPercent rounded to 2 decimals."),
                "edgePercent" to Schema.double(description = "pitchEdgeModelPercent - trueMarketImpliedPercent, or null if unlisted.", nullable = true)
            ),
            optionalProperties = listOf("linePoint", "bestBackOdds", "exchangeLayOdds", "marketOverroundPercent", "trueMarketImpliedPercent", "edgePercent")
        )

        Schema.obj(
            properties = mapOf(
                "fixtureTitle" to Schema.string(description = "Teams and match label."),
                "competition" to Schema.string(description = "Tournament or league name."),
                "matchStatus" to Schema.enumeration(
                    values = listOf("LIVE", "UPCOMING", "COMPLETED"),
                    description = "Current match state."
                ),
                "scoreTelemetry" to Schema.string(description = "Runs/wickets/overs, CRR, RRR, DLS par or pre-match start time."),
                "venue" to Schema.string(description = "Stadium and dimensions/surface notes."),
                "tossOutcome" to Schema.string(description = "Toss winner and decision or Pending."),
                "h2hMarkets" to Schema.array(items = marketQuoteSchema, description = "Match winner (h2h) vig-stripped outcomes."),
                "totalsMarkets" to Schema.array(items = marketQuoteSchema, description = "Over/Under innings or match totals grouped by linePoint."),
                "spreadsMarkets" to Schema.array(items = marketQuoteSchema, description = "Run-line handicap spreads grouped by spread margin."),
                "projections" to Schema.obj(
                    properties = mapOf(
                        "projectedFirstInningsRange" to Schema.string(description = "Projected 1st innings score range."),
                        "projectedSecondInningsRange" to Schema.string(description = "Projected 2nd innings score range."),
                        "projectedMatchTotalRange" to Schema.string(description = "Projected match aggregate range.")
                    )
                ),
                "microFactors" to Schema.obj(
                    properties = mapOf(
                        "eveningDewFactor" to Schema.string(description = "Dew impact assessment on white-ball chase."),
                        "powerplayEdge" to Schema.string(description = "Powerplay run-rate differential."),
                        "middleOversEdge" to Schema.string(description = "Middle overs spin/wicket differential."),
                        "deathOversEdge" to Schema.string(description = "Death overs pace/cutter differential."),
                        "keyPlayerMatchup" to Schema.string(description = "Highest leverage player matchup."),
                        "marketLagFlag" to Schema.boolean(description = "True if live chase odds overreacted to a single top-order wicket."),
                        "marketLagReason" to Schema.string(description = "Explanation of market lag or empty if false.")
                    )
                ),
                "recommendation" to Schema.obj(
                    properties = mapOf(
                        "action" to Schema.enumeration(
                            values = listOf("BET", "NO_BET_PASS"),
                            description = "BET only if highest cross-market edgePercent >= 3.0 and odds are listed; otherwise NO_BET_PASS."
                        ),
                        "topMarketType" to Schema.enumeration(
                            values = listOf("h2h", "totals", "spreads"),
                            description = "Market type of highest edge or primary lean."
                        ),
                        "topSelection" to Schema.string(description = "Recommended selection or NO_BET_PASS summary."),
                        "bestBookmaker" to Schema.string(description = "Best bookmaker or Unlisted."),
                        "marketOdds" to Schema.double(description = "Decimal odds of top selection or null if unlisted.", nullable = true),
                        "fairValueOdds" to Schema.double(description = "Fair value decimal odds (100 / model %)."),
                        "edgePercent" to Schema.double(description = "Highest cross-market edge %."),
                        "unitStake" to Schema.integer(description = "0 if NO_BET_PASS, or 1 to 5 units if BET."),
                        "rationale" to Schema.string(description = "Concise quantitative rationale."),
                        "invalidationTrigger" to Schema.string(description = "Condition that invalidates this thesis.")
                    ),
                    optionalProperties = listOf("marketOdds")
                )
            )
        )
    }

    private val pitchEdgeStructuredModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.1f
                responseMimeType = "application/json"
                responseSchema = pitchEdgeAnalysisSchema
            },
            systemInstruction = content {
                text(PITCH_EDGE_SYSTEM_PROMPT)
            }
        )
    }

    suspend fun fetchGroundedIntel(prompt: String): GroundedIntelResult {
        return try {
            val response = withTimeout(60_000L) {
                searchGroundedModel.generateContent(prompt)
            }
            val text = response.text?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Gemini returned an empty response.")
            val (queries, sources) = extractGrounding(response)
            GroundedIntelResult(
                summary = text.trim(),
                searchQueries = queries,
                sources = sources
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Grounded intel timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Grounded intel failed", e)
            throw e
        }
    }

    fun analyzeWithPitchEdgeTools(
        userQuery: String,
        fixtureContext: String = ""
    ): Flow<AiStreamEvent> = flow {
        val contents = mutableListOf<Content>(
            content("user") {
                text(
                    buildString {
                        appendLine(userQuery)
                        if (fixtureContext.isNotBlank()) {
                            appendLine()
                            appendLine("Local App Fixture Telemetry & Baseline Context (use if live API feed is unlisted or key not yet configured):")
                            appendLine(fixtureContext)
                        }
                    }
                )
            }
        )
        val maxToolTurns = 6
        val toolsInvoked = mutableListOf<String>()
        val toolPayloads = mutableListOf<String>()

        try {
            withTimeout(60_000L) {
                var stageOneText: String? = null
                for (turn in 0 until maxToolTurns) {
                    val response = pitchEdgeOrchestratorModel.generateContent(contents)
                    val candidateContent = response.candidates.firstOrNull()?.content
                    val functionCalls = candidateContent?.parts
                        ?.filterIsInstance<FunctionCallPart>()
                        .orEmpty()

                    if (functionCalls.isEmpty()) {
                        stageOneText = response.text?.trim()
                        break
                    }

                    if (candidateContent != null) {
                        contents.add(candidateContent)
                    }

                    val stepNames = functionCalls.map { it.name }
                    toolsInvoked.addAll(stepNames)
                    emit(AiStreamEvent.ToolProgress(toolsInvoked.distinct()))

                    val responseParts = coroutineScope {
                        functionCalls.map { call ->
                            async {
                                Log.d(TAG, "[PitchEdge Tool Call] ${call.name} ${call.args}")
                                val resultJson = executeToolCall(call.name, call.args)
                                toolPayloads.add("${call.name}: $resultJson")
                                FunctionResponsePart(
                                    name = call.name,
                                    response = resultJson
                                )
                            }
                        }.awaitAll()
                    }

                    contents.add(
                        content("function") {
                            responseParts.forEach { part(it) }
                        }
                    )
                }

                emit(AiStreamEvent.ToolProgress((toolsInvoked + "schema_synthesis").distinct()))

                val synthesisPrompt = buildString {
                    appendLine("Synthesize the verified live tool data and fixture context into strictly valid PitchEdgeAnalysisResponse JSON.")
                    appendLine("User Query: $userQuery")
                    if (fixtureContext.isNotBlank()) {
                        appendLine("\nApp Fixture Baseline Context:\n$fixtureContext")
                    }
                    if (toolPayloads.isNotEmpty()) {
                        appendLine("\nVerified Tool Outputs:\n${toolPayloads.joinToString("\n")}")
                    }
                    if (!stageOneText.isNullOrBlank()) {
                        appendLine("\nAnalyst Draft Findings:\n$stageOneText")
                    }
                }

                val structuredResponse = pitchEdgeStructuredModel.generateContent(synthesisPrompt)
                val rawJson = structuredResponse.text?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("PitchEdge structured JSON synthesis returned empty output.")

                val parsed = PitchEdgeAnalysisResponse.fromJson(rawJson)
                val formattedSummary = formatStructuredSummary(parsed)

                emit(
                    AiStreamEvent.Completed(
                        fullText = formattedSummary,
                        searchQueries = toolsInvoked.distinct(),
                        structuredAnalysis = parsed
                    )
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "PitchEdge tool orchestration timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "PitchEdge tool orchestration failed", e)
            throw e
        }
    }

    private fun formatStructuredSummary(analysis: PitchEdgeAnalysisResponse): String {
        val rec = analysis.recommendation
        val edgeSign = if (rec.edgePercent > 0) "+" else ""
        val oddsStr = rec.marketOdds?.let { String.format(Locale.US, "%.2f", it) } ?: "Unlisted"
        return buildString {
            appendLine("${analysis.fixtureTitle} [${analysis.matchStatus}] • ${analysis.tossOutcome}")
            if (analysis.scoreTelemetry.isNotBlank()) {
                appendLine(analysis.scoreTelemetry)
            }
            appendLine()
            append("Verdict: ${rec.action} • ${rec.topSelection} (${rec.bestBookmaker} @ $oddsStr | Fair ${String.format(Locale.US, "%.2f", rec.fairValueOdds)}) • Edge $edgeSign${String.format(Locale.US, "%.2f", rec.edgePercent)}% • Stake ${rec.unitStake}u")
        }.trim()
    }

    private suspend fun executeToolCall(
        name: String,
        args: Map<String, kotlinx.serialization.json.JsonElement>
    ): JsonObject {
        return try {
            val jsonObj: JSONObject = when (name) {
                "get_live_cricket_matches" -> {
                    val teamFilter = optionalStringArg(args, "teamFilter")
                    sportsDataService.fetchLiveCricketMatchesJson(teamFilter)
                }

                "get_cricket_match_scorecard" -> {
                    val matchId = optionalStringArg(args, "matchId").orEmpty()
                    sportsDataService.fetchCricketMatchScorecardJson(matchId)
                }

                "get_cricket_sport_keys" -> {
                    sportsDataService.fetchCricketSportKeysJson()
                }

                "get_live_match_odds" -> {
                    val sportKey = optionalStringArg(args, "sportKey").orEmpty()
                    val teamQuery = optionalStringArg(args, "teamQuery")
                    sportsDataService.fetchLiveMatchOddsJson(sportKey, teamQuery)
                }

                else -> JSONObject().apply { put("error", "Unknown function: $name") }
            }
            Json.parseToJsonElement(jsonObj.toString()).jsonObject
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "[PitchEdge Tool Error] $name", e)
            val err = JSONObject().apply { put("error", e.message ?: "Tool execution failed") }
            Json.parseToJsonElement(err.toString()).jsonObject
        }
    }

    private fun optionalStringArg(
        args: Map<String, kotlinx.serialization.json.JsonElement>,
        key: String
    ): String? {
        val primitive = args[key] as? JsonPrimitive ?: return null
        return primitive.contentOrNull?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
    }

    fun streamAnalystReply(
        fixtureContext: String,
        history: List<Pair<Boolean, String>>,
        userPrompt: String
    ): Flow<AiStreamEvent> = flow {
        val chatHistory: List<Content> = buildList {
            add(content("user") { text("Active Match Context:\n$fixtureContext") })
            add(content("model") { text("Acknowledged. I have the active match telemetry, odds matrix, and phase model loaded.") })
            history.takeLast(8).forEach { (isUser, msg) ->
                add(content(if (isUser) "user" else "model") { text(msg) })
            }
        }

        val chat = searchGroundedModel.startChat(history = chatHistory)
        val buffer = StringBuilder()
        var latestQueries: List<String> = emptyList()
        var latestSources: List<GroundedSource> = emptyList()

        try {
            withTimeout(60_000L) {
                chat.sendMessageStream(userPrompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        buffer.append(chunkText)
                    }
                    val (q, s) = extractGrounding(chunk)
                    if (q.isNotEmpty()) latestQueries = q
                    if (s.isNotEmpty()) latestSources = s
                    if (buffer.isNotEmpty()) {
                        emit(
                            AiStreamEvent.Chunk(
                                accumulatedText = buffer.toString(),
                                searchQueries = latestQueries,
                                sources = latestSources
                            )
                        )
                    }
                }
            }
            val finalText = buffer.toString().trim()
            if (finalText.isBlank()) {
                throw IllegalStateException("Gemini returned an empty response.")
            }
            emit(
                AiStreamEvent.Completed(
                    fullText = finalText,
                    searchQueries = latestQueries,
                    sources = latestSources
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Analyst chat stream timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Analyst chat stream failed", e)
            throw e
        }
    }

    private fun extractGrounding(response: GenerateContentResponse): Pair<List<String>, List<GroundedSource>> {
        val metadata = response.candidates.firstOrNull()?.groundingMetadata
        val queries = metadata?.webSearchQueries ?: emptyList()
        val sources = metadata?.groundingChunks
            ?.mapNotNull { chunk ->
                val web = chunk.web
                val uri = web?.uri
                if (!uri.isNullOrBlank()) {
                    GroundedSource(
                        title = web.title?.takeIf { it.isNotBlank() } ?: uri,
                        url = uri
                    )
                } else null
            }
            ?.distinctBy { it.url }
            ?: emptyList()
        return queries to sources
    }
}
