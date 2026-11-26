package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.FinalTeamSquad
import com.example.model.FixtureMatchStatus
import com.example.model.PointsTableRow
import com.example.model.SeriesBundle
import com.example.model.SeriesFixtureItem
import com.example.model.SeriesLeague
import com.example.model.SeriesLiveSyncState
import com.example.model.SeriesNewsItem
import com.example.model.SeriesPlayerStat
import com.example.model.SeriesSectionTab
import com.example.model.SeriesSummaryMetrics
import com.example.model.SeriesVenueDetail
import com.example.ui.MatchdayUiState
import com.example.ui.theme.LocalEdgeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    uiState: MatchdayUiState,
    onSelectSeries: (SeriesLeague) -> Unit,
    onSelectSeriesTab: (SeriesSectionTab) -> Unit,
    onSyncSeriesLiveData: (SeriesLeague, SeriesSectionTab) -> Unit,
    onOpenFixtureQuantBreakdown: (String) -> Unit,
    onRunQuantAgentForFixture: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val bundle = uiState.activeSeriesBundle ?: return
    val syncState = uiState.activeSeriesSyncState
    val expandedTeams = remember(bundle.league) {
        mutableStateMapOf<String, Boolean>().apply {
            bundle.teams.take(2).forEach { put(it.teamCode, true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("series_hub_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SeriesHeaderCard(
                selectedLeague = uiState.selectedSeries,
                selectedTab = uiState.selectedSeriesTab,
                bundle = bundle,
                syncState = syncState,
                onSelectSeries = onSelectSeries,
                onSyncSeriesLiveData = onSyncSeriesLiveData
            )
        }

        item {
            SeriesSummaryStrip(metrics = bundle.summaryMetrics)
        }

        item {
            val selectedIdx = SeriesSectionTab.entries.indexOf(uiState.selectedSeriesTab).coerceAtLeast(0)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, edgeColors.cardBorder)
            ) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedIdx,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    SeriesSectionTab.entries.forEach { tab ->
                        val selected = uiState.selectedSeriesTab == tab
                        Tab(
                            selected = selected,
                            onClick = { onSelectSeriesTab(tab) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("series_tab_${tab.name.lowercase()}"),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = sectionTabIcon(tab),
                                        contentDescription = tab.label,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        when (uiState.selectedSeriesTab) {
            SeriesSectionTab.FIXTURES -> {
                val upcoming = bundle.fixtures.filter { it.status != FixtureMatchStatus.COMPLETED }
                val completed = bundle.fixtures.filter { it.status == FixtureMatchStatus.COMPLETED }

                if (upcoming.isNotEmpty()) {
                    item {
                        SectionSubHeader(
                            title = "Upcoming Fixtures & Quant Breakdown",
                            subtitle = "Tap Quant Breakdown to load full EV chart, Kelly sizing & odds table"
                        )
                    }
                    items(upcoming, key = { it.id }) { fixture ->
                        SeriesFixtureCard(
                            fixture = fixture,
                            onOpenQuantBreakdown = onOpenFixtureQuantBreakdown,
                            onRunQuantAgent = onRunQuantAgentForFixture
                        )
                    }
                }

                if (completed.isNotEmpty()) {
                    item {
                        SectionSubHeader(
                            title = "Recent Results & Season Telemetry",
                            subtitle = "Completed matches feeding current NRR and form baselines"
                        )
                    }
                    items(completed, key = { it.id }) { fixture ->
                        SeriesFixtureCard(
                            fixture = fixture,
                            onOpenQuantBreakdown = onOpenFixtureQuantBreakdown,
                            onRunQuantAgent = onRunQuantAgentForFixture
                        )
                    }
                }
            }

            SeriesSectionTab.POINTS -> {
                item {
                    PointsTableCard(
                        league = bundle.league,
                        rows = bundle.pointsTable
                    )
                }
            }

            SeriesSectionTab.TEAMS -> {
                item {
                    SectionSubHeader(
                        title = "Final Teams List & Availability",
                        subtitle = "Tap any franchise to inspect full squad roles and duty alerts"
                    )
                }
                items(bundle.teams, key = { it.teamCode }) { team ->
                    val isExpanded = expandedTeams[team.teamCode] == true
                    FinalTeamSquadCard(
                        squad = team,
                        expanded = isExpanded,
                        onToggleExpand = { expandedTeams[team.teamCode] = !isExpanded }
                    )
                }
            }

            SeriesSectionTab.NEWS -> {
                item {
                    SectionSubHeader(
                        title = "Tournament Bulletins & Trading Signals",
                        subtitle = "Roster shifts, surface updates, and market pricing catalysts"
                    )
                }
                items(bundle.news, key = { it.id }) { item ->
                    SeriesNewsCard(newsItem = item)
                }
            }

            SeriesSectionTab.VENUES -> {
                item {
                    SectionSubHeader(
                        title = "Venue Telemetry & Dew Profiles",
                        subtitle = "Boundary dimensions, innings averages, and chase bias"
                    )
                }
                items(bundle.venues, key = { it.name }) { venue ->
                    SeriesVenueCard(venue = venue)
                }
            }

            SeriesSectionTab.STATS -> {
                item {
                    PlayerStatsLeaderboardCard(
                        title = "Batting Leaders",
                        subtitle = "Top run-scorers & boundary efficiency",
                        stats = bundle.battingLeaders,
                        isBatting = true
                    )
                }
                item {
                    PlayerStatsLeaderboardCard(
                        title = "Bowling Leaders",
                        subtitle = "Wicket volume, economy & phase lethality",
                        stats = bundle.bowlingLeaders,
                        isBatting = false
                    )
                }
            }
        }
    }
}

@Composable
private fun SeriesHeaderCard(
    selectedLeague: SeriesLeague,
    selectedTab: SeriesSectionTab,
    bundle: SeriesBundle,
    syncState: SeriesLiveSyncState,
    onSelectSeries: (SeriesLeague) -> Unit,
    onSyncSeriesLiveData: (SeriesLeague, SeriesSectionTab) -> Unit
) {
    val edgeColors = LocalEdgeColors.current
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, edgeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Tournament Series Hub",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = bundle.league.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = bundle.league.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onSyncSeriesLiveData(selectedLeague, selectedTab) },
                    enabled = syncState !is SeriesLiveSyncState.Loading,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("series_live_sync_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (syncState is SeriesLiveSyncState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.TravelExplore,
                            contentDescription = "Live Sync Series Intel",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (syncState is SeriesLiveSyncState.Loading) "Syncing" else "Live Sync",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeriesLeague.entries.forEach { league ->
                    val selected = league == selectedLeague
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectSeries(league) },
                        label = {
                            Text(
                                text = league.shortLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("series_league_${league.name.lowercase()}")
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = edgeColors.elevatedSurface
            ) {
                Text(
                    text = bundle.statusBanner,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            when (syncState) {
                is SeriesLiveSyncState.Idle -> Unit
                is SeriesLiveSyncState.Loading -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text(
                                text = "Querying Google Search Grounding & CricAPI for ${selectedLeague.shortLabel} (${selectedTab.label})...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                is SeriesLiveSyncState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = edgeColors.negativeEdgeContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = syncState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = edgeColors.negativeEdge,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { onSyncSeriesLiveData(selectedLeague, selectedTab) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Retry Live Sync",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                is SeriesLiveSyncState.Success -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = edgeColors.positiveEdgeContainer,
                        border = BorderStroke(1.dp, edgeColors.positiveEdge.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "GROUNDED LIVE SYNC • ${selectedLeague.shortLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = edgeColors.positiveEdge
                            )
                            Text(
                                text = syncState.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (syncState.sources.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    syncState.sources.take(4).forEach { source ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .clickable {
                                                    runCatching { uriHandler.openUri(source.url) }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = source.title.take(28),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Icon(
                                                    imageVector = Icons.Filled.OpenInNew,
                                                    contentDescription = "Open source ${source.title}",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesSummaryStrip(metrics: SeriesSummaryMetrics) {
    val edgeColors = LocalEdgeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryMetricPill(label = "Matches", value = metrics.totalMatches, borderColor = edgeColors.cardBorder)
        SummaryMetricPill(label = "1st Inn Avg", value = metrics.avgFirstInningsScore, borderColor = edgeColors.cardBorder)
        SummaryMetricPill(label = "Chase Win %", value = metrics.chasingWinPct, borderColor = edgeColors.positiveEdge.copy(alpha = 0.45f))
        SummaryMetricPill(label = "Surface Profile", value = metrics.paceVsSpinSplit, borderColor = edgeColors.warningAmber.copy(alpha = 0.45f))
    }
}

@Composable
private fun SummaryMetricPill(label: String, value: String, borderColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SectionSubHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SeriesFixtureCard(
    fixture: SeriesFixtureItem,
    onOpenQuantBreakdown: (String) -> Unit,
    onRunQuantAgent: (String) -> Unit
) {
    val edgeColors = LocalEdgeColors.current
    val statusColor = when (fixture.status) {
        FixtureMatchStatus.LIVE -> edgeColors.negativeEdge
        FixtureMatchStatus.UPCOMING -> edgeColors.positiveEdge
        FixtureMatchStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusBg = when (fixture.status) {
        FixtureMatchStatus.LIVE -> edgeColors.negativeEdgeContainer
        FixtureMatchStatus.UPCOMING -> edgeColors.positiveEdgeContainer
        FixtureMatchStatus.COMPLETED -> edgeColors.elevatedSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("series_fixture_${fixture.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (fixture.linkedFixtureId != null) {
                edgeColors.positiveEdge.copy(alpha = 0.45f)
            } else {
                edgeColors.cardBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${fixture.matchNumber} • ${fixture.dateTimeLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = RoundedCornerShape(8.dp), color = statusBg) {
                    Text(
                        text = fixture.status.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${fixture.teamAName} (${fixture.teamACode}) vs ${fixture.teamBName} (${fixture.teamBCode})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = fixture.venue,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = fixture.scoreOrPreview,
                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!fixture.quantSummary.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = edgeColors.positiveEdgeContainer
                ) {
                    Text(
                        text = fixture.quantSummary,
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.SemiBold,
                        color = edgeColors.positiveEdge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            if (fixture.linkedFixtureId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onOpenQuantBreakdown(fixture.linkedFixtureId) },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("open_quant_${fixture.linkedFixtureId}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Quant Breakdown",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Quant Breakdown",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { onRunQuantAgent(fixture.linkedFixtureId) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("run_agent_${fixture.linkedFixtureId}"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Run AI Agent",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Run AI Agent",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PointsTableCard(
    league: SeriesLeague,
    rows: List<PointsTableRow>
) {
    val edgeColors = LocalEdgeColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("series_points_table_${league.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, edgeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "${league.shortLabel} Standings & Playoff Cutline",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Top 4 advance to Playoffs • Emerald bar indicates qualifier zone",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = edgeColors.cardBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Team", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("P", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(26.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("W", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(26.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("L", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(26.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Pts", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("NRR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(58.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            rows.forEachIndexed { index, row ->
                val isCutline = index == 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(26.dp)
                            .background(
                                color = if (row.qualifierZone) edgeColors.positiveEdge else edgeColors.cardBorder,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = row.rank.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        modifier = Modifier.width(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(modifier = Modifier.weight(1.4f)) {
                        Text(
                            text = "${row.teamCode} • ${row.teamName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Form: ${row.recentForm}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(row.played.toString(), style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"), modifier = Modifier.width(26.dp), textAlign = TextAlign.End)
                    Text(row.won.toString(), style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"), modifier = Modifier.width(26.dp), textAlign = TextAlign.End)
                    Text(row.lost.toString(), style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"), modifier = Modifier.width(26.dp), textAlign = TextAlign.End)
                    Text(
                        text = row.points.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(34.dp),
                        textAlign = TextAlign.End,
                        color = if (row.qualifierZone) edgeColors.positiveEdge else MaterialTheme.colorScheme.onSurface
                    )
                    val nrrColor = if (row.nrr.startsWith("+")) edgeColors.positiveEdge else edgeColors.negativeEdge
                    Text(
                        text = row.nrr,
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(58.dp),
                        textAlign = TextAlign.End,
                        color = nrrColor
                    )
                }
                if (isCutline && index < rows.lastIndex) {
                    HorizontalDivider(
                        color = edgeColors.positiveEdge.copy(alpha = 0.55f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                } else if (index < rows.lastIndex) {
                    HorizontalDivider(color = edgeColors.cardBorder.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinalTeamSquadCard(
    squad: FinalTeamSquad,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val edgeColors = LocalEdgeColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("series_team_${squad.teamCode.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, edgeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${squad.teamName} (${squad.teamCode})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Captain: ${squad.captain} • ${squad.coachOrTheme}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse ${squad.teamName} squad" else "Expand ${squad.teamName} squad",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = squad.rosterSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!squad.injuryOrDutyAlert.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = edgeColors.warningAmberContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Availability Alert",
                            tint = edgeColors.warningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = squad.injuryOrDutyAlert,
                            style = MaterialTheme.typography.labelSmall,
                            color = edgeColors.warningAmber
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    squad.players.forEach { player ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = edgeColors.elevatedSurface,
                            border = BorderStroke(
                                1.dp,
                                if (player.isImpactOrRotation) edgeColors.warningAmber.copy(alpha = 0.45f)
                                else edgeColors.cardBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = listOfNotNull(player.roleTag, player.note?.takeIf { it.isNotBlank() }).joinToString(" • "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesNewsCard(newsItem: SeriesNewsItem) {
    val edgeColors = LocalEdgeColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("series_news_${newsItem.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, edgeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = newsItem.tag.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = newsItem.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = newsItem.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = newsItem.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = edgeColors.elevatedSurface
            ) {
                Text(
                    text = "Trading Impact: ${newsItem.tradingImpact}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = edgeColors.positiveEdge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun SeriesVenueCard(venue: SeriesVenueDetail) {
    val edgeColors = LocalEdgeColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, edgeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = venue.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${venue.city} • ${venue.dimensionsNote}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VenueStatChip(label = "1st Inn", value = venue.firstInningsAvg, modifier = Modifier.weight(1f))
                VenueStatChip(label = "2nd Inn", value = venue.secondInningsAvg, modifier = Modifier.weight(1f))
                VenueStatChip(label = "Chase %", value = venue.chaseWinRate, modifier = Modifier.weight(1f))
            }

            Text(
                text = venue.pitchAndDewProfile,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun VenueStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    val edgeColors = LocalEdgeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = edgeColors.elevatedSurface
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlayerStatsLeaderboardCard(
    title: String,
    subtitle: String,
    stats: List<SeriesPlayerStat>,
    isBatting: Boolean
) {
    val edgeColors = LocalEdgeColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, edgeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            stats.forEachIndexed { index, stat ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = edgeColors.elevatedSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (index == 0) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "#${stat.rank}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 0) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "${stat.playerName} (${stat.teamCode})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = stat.primaryStat,
                                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.Bold,
                                color = if (isBatting) edgeColors.positiveEdge else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = stat.secondaryStat,
                            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stat.quantNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun sectionTabIcon(tab: SeriesSectionTab): ImageVector {
    return when (tab) {
        SeriesSectionTab.FIXTURES -> Icons.Filled.Schedule
        SeriesSectionTab.POINTS -> Icons.Filled.Leaderboard
        SeriesSectionTab.TEAMS -> Icons.Filled.Groups
        SeriesSectionTab.NEWS -> Icons.Filled.Newspaper
        SeriesSectionTab.VENUES -> Icons.Filled.Stadium
        SeriesSectionTab.STATS -> Icons.Filled.Analytics
    }
}
