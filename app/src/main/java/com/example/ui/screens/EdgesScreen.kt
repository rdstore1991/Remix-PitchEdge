package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.EdgeFilter
import com.example.model.MarketEdgeItem
import com.example.ui.MatchdayUiState
import com.example.ui.components.ProbabilityEdgeComparisonChart
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusChip
import com.example.ui.theme.LocalEdgeColors
import java.util.Locale
import kotlin.math.max

@Composable
fun EdgesScreen(
    uiState: MatchdayUiState,
    onSelectFilter: (EdgeFilter) -> Unit,
    onSelectCalculatorMarket: (String) -> Unit,
    onAdjustCustomOdds: (Double) -> Unit,
    onUpdateUnitSize: (Double) -> Unit,
    onUpdateKellyMultiplier: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Vig-Free Market Consensus Card
        item {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, edgeColors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pre-Match Consensus Baseline",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StatusChip(
                            text = "VIG-FREE",
                            containerColor = edgeColors.capitalsContainer,
                            contentColor = edgeColors.capitalsAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1xBet: DC 2.13 / DV 1.72 (~5.1% overround)\nParimatch: DC 2.06 / DV 1.77 (~5.0% overround)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = edgeColors.vipersContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "DV CONSENSUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = edgeColors.vipersAccent
                                )
                                Text(
                                    text = "54.6%",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = edgeColors.capitalsContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "DC CONSENSUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = edgeColors.capitalsAccent
                                )
                                Text(
                                    text = "45.4%",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter Chips + Matrix Header
        item {
            SectionHeader(
                title = "Probability & Odds Matrix",
                subtitle = "Tap any market row to load into the Kelly EV calculator below"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EdgeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = uiState.edgeFilter == filter,
                        onClick = { onSelectFilter(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("edge_filter_${filter.name.lowercase()}")
                    )
                }
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, edgeColors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DC vs. DV — AI Model vs. Market Implied Edges",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.tossScenario.title} • Tap any bar column to inspect EV delta & load into Kelly simulator",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ProbabilityEdgeComparisonChart(
                        markets = uiState.filteredEdges,
                        selectedMarketId = uiState.selectedCalculatorMarketId,
                        onSelectMarket = onSelectCalculatorMarket
                    )
                }
            }
        }

        items(uiState.filteredEdges, key = { it.id }) { item ->
            MarketEdgeCard(
                item = item,
                isSelectedForCalc = item.id == uiState.selectedCalculatorMarketId,
                onClick = { onSelectCalculatorMarket(item.id) }
            )
        }

        // Interactive Fractional Kelly & Live EV Calculator
        item {
            uiState.selectedCalculatorMarket?.let { selectedMarket ->
                KellyCalculatorCard(
                    market = selectedMarket,
                    customOdds = uiState.customOdds,
                    unitSizeDollars = uiState.unitSizeDollars,
                    kellyMultiplier = uiState.kellyMultiplier,
                    onAdjustOdds = onAdjustCustomOdds,
                    onSelectUnitSize = onUpdateUnitSize,
                    onSelectKellyMult = onUpdateKellyMultiplier
                )
            }
        }

        // Tournament Outright Note
        item {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, edgeColors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Tournament Outright",
                        tint = edgeColors.warningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Tournament Outright Note",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Betfair lists Gulf Giants at 4.00 after their 2–0 start; prior to Match 1, models flagged defending champions Desert Vipers at 8.50 as undervalued relative to their bowling depth.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun MarketEdgeCard(
    item: MarketEdgeItem,
    isSelectedForCalc: Boolean,
    onClick: () -> Unit
) {
    val edgeColors = LocalEdgeColors.current
    val isPositive = item.edgePct > 0.0
    val accent = when {
        item.hasRiskFlag -> edgeColors.warningAmber
        isPositive -> edgeColors.positiveEdge
        else -> edgeColors.negativeEdge
    }
    val container = when {
        item.hasRiskFlag -> edgeColors.warningAmberContainer
        isPositive -> edgeColors.positiveEdgeContainer
        else -> edgeColors.negativeEdgeContainer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .testTag("market_card_${item.id}"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelectedForCalc) 1.5.dp else 1.dp,
            color = if (isSelectedForCalc) MaterialTheme.colorScheme.primary else edgeColors.cardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.marketName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.bookmakerNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(
                    text = item.statusBadge,
                    containerColor = container,
                    contentColor = accent
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricCol(
                    label = "BEST ODDS",
                    value = String.format(Locale.US, "%.2f", item.bestOdds),
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                MetricCol(
                    label = "MKT IMPLIED",
                    value = String.format(Locale.US, "%.1f%%", item.marketImpliedPct),
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MetricCol(
                    label = "AI MODEL",
                    value = String.format(Locale.US, "%.1f%%", item.modelPct),
                    valueColor = MaterialTheme.colorScheme.secondary
                )
                MetricCol(
                    label = "EDGE",
                    value = (if (item.edgePct > 0) "+" else "") + String.format(Locale.US, "%.1f%%", item.edgePct),
                    valueColor = accent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = edgeColors.elevatedSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.evNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.rawImpliedNote,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCol(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun KellyCalculatorCard(
    market: MarketEdgeItem,
    customOdds: Double,
    unitSizeDollars: Double,
    kellyMultiplier: Double,
    onAdjustOdds: (Double) -> Unit,
    onSelectUnitSize: (Double) -> Unit,
    onSelectKellyMult: (Double) -> Unit
) {
    val edgeColors = LocalEdgeColors.current
    val p = market.modelPct / 100.0
    val impliedPct = (1.0 / customOdds) * 100.0
    val liveEdgePct = market.modelPct - impliedPct
    val evPct = (p * customOdds - 1.0) * 100.0
    val b = customOdds - 1.0
    val rawKellyFraction = if (b > 0) max(0.0, ((b * p) - (1.0 - p)) / b) else 0.0
    val scaledKellyPct = rawKellyFraction * kellyMultiplier * 100.0
    // Map to units assuming 1 unit = 1% of bankroll (standard sports analytics convention)
    val recommendedUnits = (scaledKellyPct).coerceAtMost(5.0)
    val recommendedDollars = recommendedUnits * unitSizeDollars

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = "Kelly Calculator",
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fractional Kelly & Live EV Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = market.marketName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Odds Stepper
            Surface(
                shape = MaterialTheme.shapes.small,
                color = edgeColors.elevatedSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LIVE DECIMAL ODDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", customOdds),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onAdjustOdds(-0.05) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("calc_odds_minus")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Decrease odds by 0.05",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { onAdjustOdds(0.05) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("calc_odds_plus")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Increase odds by 0.05",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Unit Size & Kelly Fraction chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Unit ($)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(25.0, 50.0, 100.0).forEach { unitVal ->
                        val active = unitSizeDollars == unitVal
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else edgeColors.elevatedSurface,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clickable { onSelectUnitSize(unitVal) }
                                .testTag("unit_size_${unitVal.toInt()}")
                        ) {
                            Text(
                                text = "$${unitVal.toInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kelly Tier",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.25 to "0.25x", 0.50 to "0.50x", 1.0 to "Full").forEach { (mult, label) ->
                        val active = kellyMultiplier == mult
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else edgeColors.elevatedSurface,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clickable { onSelectKellyMult(mult) }
                                .testTag("kelly_mult_$label")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Computed Outputs
            val isPositiveEv = evPct > 0.0
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isPositiveEv) edgeColors.positiveEdgeContainer else edgeColors.negativeEdgeContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (isPositiveEv) edgeColors.positiveEdge else edgeColors.negativeEdge,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EV ${(if (evPct > 0) "+" else "")}${String.format(Locale.US, "%.1f", evPct)}% • Edge ${(if (liveEdgePct > 0) "+" else "")}${String.format(Locale.US, "%.1f", liveEdgePct)}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isPositiveEv) edgeColors.positiveEdge else edgeColors.negativeEdge
                            )
                        }
                        Text(
                            text = "Implied ${String.format(Locale.US, "%.1f", impliedPct)}% vs Model ${String.format(Locale.US, "%.1f", market.modelPct)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format(Locale.US, "%.2f", recommendedUnits)}u",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.0f", recommendedDollars)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
