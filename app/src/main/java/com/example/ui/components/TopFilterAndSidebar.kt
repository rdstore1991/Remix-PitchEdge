package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.FixtureOverview
import com.example.model.FormatFilter
import com.example.model.SportFilter
import com.example.model.StatusFilter
import com.example.ui.theme.LocalEdgeColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopFilterBar(
    selectedSport: SportFilter,
    selectedFormat: FormatFilter,
    selectedStatus: StatusFilter,
    onSelectSport: (SportFilter) -> Unit,
    onSelectFormat: (FormatFilter) -> Unit,
    onSelectStatus: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, edgeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Row 1: Sport Selector + Format Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SportFilter.entries.forEach { sport ->
                    val selected = sport == selectedSport
                    val icon = when (sport) {
                        SportFilter.CRICKET -> Icons.Filled.SportsCricket
                        SportFilter.FOOTBALL -> Icons.Filled.SportsSoccer
                        SportFilter.TENNIS -> Icons.Filled.SportsTennis
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectSport(sport) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = sport.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sport_filter_${sport.name.lowercase()}")
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                FormatFilter.entries.forEach { format ->
                    val selected = format == selectedFormat
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectFormat(format) },
                        label = {
                            Text(
                                text = format.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = edgeColors.capitalsContainer,
                            selectedLabelColor = edgeColors.capitalsAccent
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("format_filter_${format.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Status Tabs (Live Now, Upcoming Today, Value Picks)
            val selectedTabIndex = StatusFilter.entries.indexOf(selectedStatus).coerceAtLeast(0)
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                StatusFilter.entries.forEach { status ->
                    val isSelected = status == selectedStatus
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectStatus(status) },
                        text = {
                            Text(
                                text = status.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("status_tab_${status.name.lowercase()}")
                    )
                }
            }
        }
    }
}

@Composable
fun MatchSelectorSidebar(
    fixtures: List<FixtureOverview>,
    selectedFixtureId: String,
    isSyncingLiveFeeds: Boolean,
    liveFeedStatusMessage: String?,
    onSelectFixture: (String) -> Unit,
    onSyncLiveFeeds: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current

    Surface(
        modifier = modifier
            .width(268.dp)
            .fillMaxHeight()
            .testTag("match_selector_sidebar"),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, edgeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FIXTURE SLATE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.TextButton(
                    onClick = onSyncLiveFeeds,
                    enabled = !isSyncingLiveFeeds,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("sync_cricapi_button")
                ) {
                    Text(
                        text = if (isSyncingLiveFeeds) "Syncing..." else "Sync CricAPI",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            if (!liveFeedStatusMessage.isNullOrBlank()) {
                Text(
                    text = liveFeedStatusMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(fixtures, key = { it.id }) { fixture ->
                    FixtureSidebarItem(
                        fixture = fixture,
                        selected = fixture.id == selectedFixtureId,
                        onClick = { onSelectFixture(fixture.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CompactMatchSelectorStrip(
    fixtures: List<FixtureOverview>,
    selectedFixtureId: String,
    onSelectFixture: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compact_match_selector_strip"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(fixtures, key = { it.id }) { fixture ->
            FixtureSidebarItem(
                fixture = fixture,
                selected = fixture.id == selectedFixtureId,
                onClick = { onSelectFixture(fixture.id) },
                modifier = Modifier.width(248.dp)
            )
        }
    }
}

@Composable
private fun FixtureSidebarItem(
    fixture: FixtureOverview,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val edgeColor = if (fixture.hasPositiveEdge) edgeColors.positiveEdge else edgeColors.warningAmber
    val edgeContainer = if (fixture.hasPositiveEdge) edgeColors.positiveEdgeContainer else edgeColors.warningAmberContainer

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .testTag("fixture_item_${fixture.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else edgeColors.elevatedSurface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else edgeColors.cardBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fixture.leagueTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip(
                    text = fixture.topEdgeBadge,
                    containerColor = edgeContainer,
                    contentColor = edgeColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${fixture.homeName} vs ${fixture.awayName}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${fixture.liveScoreOrTimer} • ${fixture.oversOrPeriod}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = edgeColors.capitalsContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${fixture.homeCode} ${String.format(Locale.US, "%.1f", fixture.homeModelPct)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = edgeColors.capitalsAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = edgeColors.vipersContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${fixture.awayCode} ${String.format(Locale.US, "%.1f", fixture.awayModelPct)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = edgeColors.vipersAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
