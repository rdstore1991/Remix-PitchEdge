package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AnalyticalPick
import com.example.ui.MatchdayUiState
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusChip
import com.example.ui.theme.LocalEdgeColors
import java.util.Locale

@Composable
fun SelectionsScreen(
    uiState: MatchdayUiState,
    onToggleFakharBabar: () -> Unit,
    onTogglePowellShanaka: () -> Unit,
    onToggleDcBowls: () -> Unit,
    onToggleHoldenXi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Ticket Exposure Summary
        item {
            val activeExposure = uiState.totalQualifiedUnits * uiState.unitSizeDollars
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "QUALIFIED CARD EXPOSURE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.2f", uiState.totalQualifiedUnits)} Units ($${String.format(Locale.US, "%.0f", activeExposure)})",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Unit = $${uiState.unitSizeDollars.toInt()} • Updates dynamically from 14:00 GMT toss sheet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.FactCheck,
                        contentDescription = "Qualified Card",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Primary Selection Hero Card
        uiState.primarySelection?.let { primary ->
            item {
                val active = uiState.isFakharQualified
                PrimarySelectionCard(
                    pick = primary,
                    isActive = active
                )
            }
        }

        // 14:00 GMT Toss & XI Verification Checklist
        item {
            SectionHeader(
                title = "14:00 GMT Toss Verification Gate",
                subtitle = "Toggle confirmed team-sheet conditions to qualify or void plays"
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, edgeColors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    ChecklistToggleRow(
                        title = "Fakhar & Babar Confirmed Opening Pair",
                        subtitle = "Required for Primary 2.0u play (invalidated if Gous opens with Fakhar)",
                        checked = uiState.checklist.fakharBabarOpeningConfirmed,
                        onCheckedChange = { onToggleFakharBabar() },
                        testTag = "chk_fakhar_babar"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChecklistToggleRow(
                        title = "Rovman Powell or Dasun Shanaka in DC XI",
                        subtitle = "Qualifies Dubai Capitals ML (2.20 • 1.0u)",
                        checked = uiState.checklist.powellOrShanakaInDcXi,
                        onCheckedChange = { onTogglePowellShanaka() },
                        testTag = "chk_powell_shanaka"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChecklistToggleRow(
                        title = "DC Wins Toss & Elects to Bowl",
                        subtitle = "Mustafizur/Noor/Hassan exploit fresh surface moisture (qualifies DC ML)",
                        checked = uiState.checklist.dcWonTossAndBowls,
                        onCheckedChange = { onToggleDcBowls() },
                        testTag = "chk_dc_bowl"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChecklistToggleRow(
                        title = "Max Holden in DC Starting XI (Not Sub)",
                        subtitle = "Avoids Impact-Sub sportsbook void rules on 4.33 Top Batter",
                        checked = uiState.checklist.holdenInStartingXi,
                        onCheckedChange = { onToggleHoldenXi() },
                        testTag = "chk_holden_xi"
                    )
                }
            }
        }

        // Secondary & Conditional Leans
        item {
            SectionHeader(
                title = "Secondary & Conditional Leans",
                subtitle = "Match-market discipline & Impact-Sub rule protection"
            )
        }

        items(uiState.secondaryLeans, key = { it.id }) { lean ->
            val qualifiedState = when (lean.id) {
                "pick_dc_ml" -> if (uiState.isDcMlQualified) "QUALIFIED • 1.0u" else "GATED (WAIT FOR TOSS)"
                "pick_holden" -> if (uiState.isHoldenQualified) "QUALIFIED • 0.75u" else "BLOCKED (SUB RISK)"
                else -> "PASS / NO PLAY"
            }
            val isPlayableNow = when (lean.id) {
                "pick_dc_ml" -> uiState.isDcMlQualified
                "pick_holden" -> uiState.isHoldenQualified
                else -> false
            }
            SecondaryLeanCard(
                pick = lean,
                statusText = qualifiedState,
                isPlayableNow = isPlayableNow
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun PrimarySelectionCard(
    pick: AnalyticalPick,
    isActive: Boolean
) {
    val edgeColors = LocalEdgeColors.current
    val accent = if (isActive) edgeColors.positiveEdge else edgeColors.negativeEdge
    val container = if (isActive) edgeColors.positiveEdgeContainer else edgeColors.negativeEdgeContainer

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, accent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isActive) Icons.Filled.Star else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = pick.tierLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = accent
                    )
                }
                StatusChip(
                    text = if (isActive) "ACTIVE • 2.0 UNITS" else "INVALIDATED • 0.0u",
                    containerColor = container,
                    contentColor = accent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = pick.selectionTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectionStatPill("TARGET ODDS", pick.targetOdds, Modifier.weight(1f))
                SelectionStatPill("EDGE / TIER", "${pick.edgeLabel} • ${pick.confidenceTier}", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectionStatPill("PRICING", pick.modelVsMarket, Modifier.weight(1f))
                SelectionStatPill("LIMIT", pick.minPlayableOdds, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = pick.rationale,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isActive) edgeColors.warningAmberContainer else edgeColors.negativeEdgeContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (isActive) edgeColors.warningAmber else edgeColors.negativeEdge,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pick.triggerOrInvalidation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = edgeColors.elevatedSurface
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChecklistToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val edgeColors = LocalEdgeColors.current
    Surface(
        shape = MaterialTheme.shapes.small,
        color = edgeColors.elevatedSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag(testTag)
            )
        }
    }
}

@Composable
private fun SecondaryLeanCard(
    pick: AnalyticalPick,
    statusText: String,
    isPlayableNow: Boolean
) {
    val edgeColors = LocalEdgeColors.current
    val isSkip = pick.id == "pick_dv_skip"
    val accent = when {
        isPlayableNow -> edgeColors.positiveEdge
        isSkip -> edgeColors.negativeEdge
        else -> edgeColors.warningAmber
    }
    val container = when {
        isPlayableNow -> edgeColors.positiveEdgeContainer
        isSkip -> edgeColors.negativeEdgeContainer
        else -> edgeColors.warningAmberContainer
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isPlayableNow) accent else edgeColors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pick.tierLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusChip(
                    text = statusText,
                    containerColor = container,
                    contentColor = accent
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = pick.selectionTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${pick.targetOdds} • ${pick.edgeLabel} • ${pick.modelVsMarket}",
                style = MaterialTheme.typography.labelMedium,
                color = accent
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = pick.rationale,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = if (isPlayableNow) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = pick.triggerOrInvalidation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
