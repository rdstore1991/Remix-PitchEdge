package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.PitchEdgeAnalysisResponse
import com.example.model.PitchEdgeMarketQuote
import com.example.ui.theme.LocalEdgeColors
import java.util.Locale

@Composable
fun PitchEdgeStructuredAnalysisCard(
    analysis: PitchEdgeAnalysisResponse,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    var selectedMarketTab by remember(analysis) {
        mutableStateOf(
            when {
                analysis.h2hMarkets.isNotEmpty() -> "h2h"
                analysis.totalsMarkets.isNotEmpty() -> "totals"
                else -> "spreads"
            }
        )
    }

    val activeQuotes: List<PitchEdgeMarketQuote> = when (selectedMarketTab) {
        "totals" -> analysis.totalsMarkets
        "spreads" -> analysis.spreadsMarkets
        else -> analysis.h2hMarkets
    }

    val isBet = analysis.recommendation.action == "BET"
    val verdictAccent = if (isBet) edgeColors.positiveEdge else edgeColors.warningAmber
    val verdictContainer = if (isBet) edgeColors.positiveEdgeContainer else edgeColors.warningAmberContainer

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, verdictAccent.copy(alpha = 0.55f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pitchedge_structured_analysis_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Telemetry Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = analysis.fixtureTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${analysis.competition} • ${analysis.venue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip(
                    text = analysis.matchStatus,
                    containerColor = if (analysis.matchStatus == "LIVE") edgeColors.positiveEdgeContainer else edgeColors.capitalsContainer,
                    contentColor = if (analysis.matchStatus == "LIVE") edgeColors.positiveEdge else edgeColors.capitalsAccent
                )
            }

            if (analysis.scoreTelemetry.isNotBlank() || analysis.tossOutcome.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${analysis.scoreTelemetry} • Toss: ${analysis.tossOutcome}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Market Lag Alert Banner
            if (analysis.microFactors.marketLagFlag) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = edgeColors.warningAmberContainer,
                    border = BorderStroke(1.dp, edgeColors.warningAmber.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Market Lag Detected",
                            tint = edgeColors.warningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MARKET LAG: ${analysis.microFactors.marketLagReason}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Market Switcher (H2H / Totals / Spreads)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "h2h" to "H2H (${analysis.h2hMarkets.size})",
                    "totals" to "Totals (${analysis.totalsMarkets.size})",
                    "spreads" to "Spreads (${analysis.spreadsMarkets.size})"
                ).forEach { (key, label) ->
                    val selected = selectedMarketTab == key
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else edgeColors.elevatedSurface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else edgeColors.cardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clickable { selectedMarketTab = key }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeQuotes.isEmpty()) {
                Text(
                    text = "No active lines quoted for ${selectedMarketTab.uppercase(Locale.US)}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            } else {
                activeQuotes.forEachIndexed { idx, quote ->
                    MarketQuoteRow(quote = quote)
                    if (idx < activeQuotes.lastIndex) {
                        HorizontalDivider(color = edgeColors.cardBorder.copy(alpha = 0.5f))
                    }
                }
            }

            // Projections & Micro-Factors
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = edgeColors.elevatedSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Projections & Micro-Factors",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1st Inn: ${analysis.projections.projectedFirstInningsRange} • 2nd Inn: ${analysis.projections.projectedSecondInningsRange}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (analysis.microFactors.eveningDewFactor.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Dew: ${analysis.microFactors.eveningDewFactor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (analysis.microFactors.keyPlayerMatchup.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Matchup: ${analysis.microFactors.keyPlayerMatchup}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Recommendation Verdict Box
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = verdictContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, verdictAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBet) Icons.Filled.CheckCircle else Icons.Filled.DoNotDisturbOn,
                                contentDescription = null,
                                tint = verdictAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBet) "ACTION: BET (${analysis.recommendation.unitStake}u)" else "NO BET / PASS (0u)",
                                style = MaterialTheme.typography.labelLarge,
                                color = verdictAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        val edgeSign = if (analysis.recommendation.edgePercent > 0) "+" else ""
                        Text(
                            text = "$edgeSign${String.format(Locale.US, "%.2f", analysis.recommendation.edgePercent)}% EV",
                            style = MaterialTheme.typography.labelMedium,
                            color = verdictAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    val recOdds = analysis.recommendation.marketOdds?.let { String.format(Locale.US, "%.2f", it) } ?: "Unlisted"
                    Text(
                        text = "${analysis.recommendation.topSelection} • ${analysis.recommendation.bestBookmaker} @ $recOdds (Fair ${String.format(Locale.US, "%.2f", analysis.recommendation.fairValueOdds)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                    // 1..5 Unit Stake Meter
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (u in 1..5) {
                            val filled = u <= analysis.recommendation.unitStake
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (filled) verdictAccent else edgeColors.cardBorder)
                            )
                        }
                    }

                    if (analysis.recommendation.rationale.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = analysis.recommendation.rationale,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (analysis.recommendation.invalidationTrigger.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = edgeColors.warningAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = analysis.recommendation.invalidationTrigger,
                                style = MaterialTheme.typography.labelSmall,
                                color = edgeColors.warningAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketQuoteRow(quote: PitchEdgeMarketQuote) {
    val edgeColors = LocalEdgeColors.current
    val edgeVal = quote.edgePercent
    val edgeColor = when {
        edgeVal == null -> MaterialTheme.colorScheme.onSurfaceVariant
        edgeVal >= 3.0 -> edgeColors.positiveEdge
        edgeVal > 0.0 -> edgeColors.warningAmber
        else -> edgeColors.negativeEdge
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.3f)) {
            Text(
                text = quote.selection,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            val layNote = quote.exchangeLayOdds?.let { " • Lay ${String.format(Locale.US, "%.2f", it)}" }.orEmpty()
            Text(
                text = "${quote.bestBookmaker}$layNote",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f)
        ) {
            val oddsStr = quote.bestBackOdds?.let { String.format(Locale.US, "%.2f", it) } ?: "Unlisted"
            Text(
                text = "$oddsStr (Fair ${String.format(Locale.US, "%.2f", quote.fairValueOdds)})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            val mktStr = quote.trueMarketImpliedPercent?.let { String.format(Locale.US, "%.1f%%", it) } ?: "—"
            val modStr = String.format(Locale.US, "%.1f%%", quote.pitchEdgeModelPercent)
            val edgeStr = edgeVal?.let {
                (if (it > 0) "+" else "") + String.format(Locale.US, "%.2f%%", it)
            } ?: "Unlisted"
            Text(
                text = "Mkt $mktStr • AI $modStr • $edgeStr",
                style = MaterialTheme.typography.labelSmall,
                color = edgeColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
