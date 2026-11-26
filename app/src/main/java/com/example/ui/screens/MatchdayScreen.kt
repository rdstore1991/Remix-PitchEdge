package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.FormatFilter
import com.example.model.PlayerSlot
import com.example.model.SportFilter
import com.example.model.StatusFilter
import com.example.model.TacticalPhase
import com.example.model.TeamSide
import com.example.model.TossScenario
import com.example.ui.MatchdayUiState
import com.example.ui.components.AiBestOpinionCard
import com.example.ui.components.CompactMatchSelectorStrip
import com.example.ui.components.CricketMatchRow
import com.example.ui.components.MatchSelectorSidebar
import com.example.ui.components.ScoreProjectionTacticalCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusChip
import com.example.ui.components.TopFilterBar
import com.example.ui.components.WinProbabilityOddsCard
import com.example.ui.components.WorkspaceHeaderBanner
import com.example.ui.theme.LocalEdgeColors
import com.example.R
import androidx.compose.ui.res.stringResource

@Composable
fun MatchdayScreen(
    uiState: MatchdayUiState,
    onSelectSport: (SportFilter) -> Unit,
    onSelectFormat: (FormatFilter) -> Unit,
    onSelectStatus: (StatusFilter) -> Unit,
    onSelectFixture: (String) -> Unit,
    onSyncLiveFeeds: () -> Unit,
    onRefreshGroundedIntel: () -> Unit,
    onFirstInningsChange: (Int) -> Unit,
    onSecondInningsChange: (Int) -> Unit,
    onSelectTossScenario: (TossScenario) -> Unit,
    onSelectXiTeam: (TeamSide) -> Unit,
    onSelectPhase: (TacticalPhase) -> Unit,
    onOpenEdgesTab: () -> Unit,
    onOpenTacticsTab: () -> Unit,
    onOpenCardTab: () -> Unit,
    onOpenAnalystChat: () -> Unit,
    onRunToolAgent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val activeFixture = uiState.activeFixture

    Column(modifier = modifier.fillMaxSize()) {
        TopFilterBar(
            selectedSport = uiState.selectedSport,
            selectedFormat = uiState.selectedFormat,
            selectedStatus = uiState.selectedStatus,
            onSelectSport = onSelectSport,
            onSelectFormat = onSelectFormat,
            onSelectStatus = onSelectStatus
        )

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val showLeftSidebar = maxWidth >= 720.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (showLeftSidebar) {
                    MatchSelectorSidebar(
                        fixtures = uiState.filteredFixtures,
                        selectedFixtureId = activeFixture.id,
                        isSyncingLiveFeeds = uiState.isSyncingLiveFeeds,
                        liveFeedStatusMessage = uiState.liveFeedStatusMessage,
                        onSelectFixture = onSelectFixture,
                        onSyncLiveFeeds = onSyncLiveFeeds
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!showLeftSidebar) {
                        item {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.liveFeedStatusMessage ?: "Match Slate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    androidx.compose.material3.TextButton(
                                        onClick = onSyncLiveFeeds,
                                        enabled = !uiState.isSyncingLiveFeeds,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(
                                            text = if (uiState.isSyncingLiveFeeds) "Syncing..." else "Sync CricAPI",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                CompactMatchSelectorStrip(
                                    fixtures = uiState.filteredFixtures,
                                    selectedFixtureId = activeFixture.id,
                                    onSelectFixture = onSelectFixture
                                )
                            }
                        }
                    }

                    // 1. Header Banner (Team badges, countdown, venue, weather, toss, Search Grounding)
                    item {
                        WorkspaceHeaderBanner(
                            fixture = activeFixture,
                            tossScenario = uiState.tossScenario,
                            groundedIntelState = uiState.groundedIntelState,
                            onRefreshGroundedIntel = onRefreshGroundedIntel
                        )
                    }

                    // 2. Win Probability & Odds Comparison Card
                    item {
                        WinProbabilityOddsCard(
                            fixture = activeFixture,
                            tossScenario = uiState.tossScenario,
                            isFeaturedDcDv = uiState.isFeaturedDcDvMatch,
                            markets = uiState.scenarioAdjustedEdges,
                            onOpenEdgesTab = onOpenEdgesTab
                        )
                    }

                    // 3. Score Projection & Tactical Matrix
                    item {
                        ScoreProjectionTacticalCard(
                            fixture = activeFixture,
                            projectedFirstInnings = uiState.projectedFirstInnings,
                            projectedSecondInnings = uiState.projectedSecondInnings,
                            onFirstInningsChange = onFirstInningsChange,
                            onSecondInningsChange = onSecondInningsChange,
                            tossScenario = uiState.tossScenario,
                            onSelectTossScenario = onSelectTossScenario,
                            venueScores = uiState.venueScores,
                            selectedPhase = uiState.selectedPhase,
                            phaseInfo = uiState.selectedPhaseInfo,
                            playerLeverages = uiState.playerLeverages,
                            onSelectPhase = onSelectPhase,
                            onOpenTacticsTab = onOpenTacticsTab
                        )
                    }

                    // 4. AI Best Opinion Card
                    item {
                        AiBestOpinionCard(
                            fixture = activeFixture,
                            onAskAnalystClick = onOpenAnalystChat,
                            onRunToolAgentClick = onRunToolAgent,
                            onOpenCardTab = onOpenCardTab
                        )
                    }

                    // 5. Projected 12-Man XIs (for DC vs DV Match 6)
                    if (uiState.isFeaturedDcDvMatch) {
                        item {
                            val isDc = uiState.selectedXiTeam == TeamSide.DC
                            val roster = if (isDc) uiState.dcXi else uiState.dvXi

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dashboard_projected_xi_card"),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, edgeColors.cardBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Projected XIs & Team News",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Pre-Toss 12-man rotations • Monitor 14:00 GMT toss sheet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        TeamToggleButton(
                                            label = "Dubai Capitals (12)",
                                            selected = isDc,
                                            accentColor = edgeColors.capitalsAccent,
                                            containerColor = edgeColors.capitalsContainer,
                                            onClick = { onSelectXiTeam(TeamSide.DC) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("xi_toggle_dc")
                                        )
                                        TeamToggleButton(
                                            label = "Desert Vipers (12)",
                                            selected = !isDc,
                                            accentColor = edgeColors.vipersAccent,
                                            containerColor = edgeColors.vipersContainer,
                                            onClick = { onSelectXiTeam(TeamSide.DV) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("xi_toggle_dv")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = if (isDc) edgeColors.negativeEdgeContainer else edgeColors.positiveEdgeContainer,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isDc) edgeColors.negativeEdge.copy(alpha = 0.4f) else edgeColors.positiveEdge.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = if (isDc) Icons.Filled.Warning else Icons.Filled.Groups,
                                                contentDescription = null,
                                                tint = if (isDc) edgeColors.negativeEdge else edgeColors.positiveEdge,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isDc) {
                                                    "Key Absence: Phil Salt remains on England duty in Australia. Rovman Powell & Dasun Shanaka missed M3 — monitor 14:00 GMT toss sheet."
                                                } else {
                                                    "Key Boost: Babar Azam arrived for Match 4 and immediately struck 86 (51), anchoring the top order alongside Fakhar Zaman."
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    roster.forEachIndexed { index, slot ->
                                        PlayerSlotRow(index = index + 1, slot = slot, isDc = isDc)
                                        if (index < roster.lastIndex) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. API Match Listings (Team names, current status & probability edge placeholder)
                    item(key = "api_match_listings_header") {
                        SectionHeader(
                            title = stringResource(R.string.match_listings_section_title),
                            subtitle = stringResource(R.string.match_listings_section_subtitle)
                        )
                    }

                    items(
                        items = uiState.filteredFixtures,
                        key = { fixture -> "listing_${fixture.id}" }
                    ) { fixture ->
                        CricketMatchRow(
                            fixture = fixture,
                            selected = fixture.id == activeFixture.id,
                            onClick = { onSelectFixture(fixture.id) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TeamToggleButton(
    label: String,
    selected: Boolean,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) containerColor else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) accentColor else edgeColors.cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PlayerSlotRow(index: Int, slot: PlayerSlot, isDc: Boolean) {
    val edgeColors = LocalEdgeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(26.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = slot.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusChip(
                    text = slot.roleTag,
                    containerColor = when {
                        slot.isImpactOrRotation -> edgeColors.warningAmberContainer
                        isDc -> edgeColors.capitalsContainer
                        else -> edgeColors.vipersContainer
                    },
                    contentColor = when {
                        slot.isImpactOrRotation -> edgeColors.warningAmber
                        isDc -> edgeColors.capitalsAccent
                        else -> edgeColors.vipersAccent
                    }
                )
            }
            if (slot.note != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = slot.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
