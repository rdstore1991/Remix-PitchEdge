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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LeagueTelemetryNormalizer
import com.example.model.CricketMatchSummaryDto
import com.example.model.FixtureOverview
import com.example.model.StatusFilter
import com.example.ui.theme.LocalEdgeColors

/**
 * Single cricket match row displaying both team names, current match status,
 * venue/score summary, and a placeholder slot for the probability edge indicator.
 */
@Composable
fun CricketMatchRow(
    fixture: FixtureOverview,
    onClick: () -> Unit = {},
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val statusText = when (fixture.status) {
        StatusFilter.LIVE_NOW -> stringResource(R.string.status_live_label)
        StatusFilter.UPCOMING_TODAY -> stringResource(R.string.status_upcoming_label)
        StatusFilter.VALUE_PICKS -> stringResource(R.string.status_value_pick_label)
    }

    CricketMatchRowContent(
        matchId = fixture.id,
        leagueTitle = fixture.leagueTitle,
        homeName = fixture.homeName,
        homeCode = fixture.homeCode,
        awayName = fixture.awayName,
        awayCode = fixture.awayCode,
        statusLabel = statusText,
        isLive = fixture.status == StatusFilter.LIVE_NOW,
        statusDetail = "${fixture.liveScoreOrTimer} • ${fixture.oversOrPeriod}",
        venue = fixture.venue,
        selected = selected,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * Convenience overload for rendering a raw CricAPI [CricketMatchSummaryDto] match listing
 * directly with team names, live/scheduled status, and the probability edge indicator placeholder.
 */
@Composable
fun CricketMatchRow(
    match: CricketMatchSummaryDto,
    onClick: () -> Unit = {},
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val homeName = match.teams.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "Team A"
    val awayName = match.teams.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "Team B"
    val homeCode = LeagueTelemetryNormalizer.canonicalTeamCode(homeName)
    val awayCode = LeagueTelemetryNormalizer.canonicalTeamCode(awayName)

    val isLive = match.matchStarted && !match.matchEnded
    val statusLabel = when {
        isLive -> stringResource(R.string.status_live_label)
        match.matchEnded -> stringResource(R.string.status_completed_label)
        else -> stringResource(R.string.status_upcoming_label)
    }

    val scoreText = match.score.lastOrNull()?.let { latest ->
        "${latest.runs}/${latest.wickets} (${latest.overs} ov)"
    }
    val statusDetail = listOfNotNull(
        scoreText,
        match.status.takeIf { it.isNotBlank() } ?: match.dateTimeGMT.takeIf { it.isNotBlank() }
    ).joinToString(" • ").ifBlank { statusLabel }

    CricketMatchRowContent(
        matchId = match.id.ifBlank { "${homeCode}_${awayCode}" },
        leagueTitle = match.name.ifBlank { match.matchType.uppercase() },
        homeName = homeName,
        homeCode = homeCode,
        awayName = awayName,
        awayCode = awayCode,
        statusLabel = statusLabel,
        isLive = isLive,
        statusDetail = statusDetail,
        venue = match.venue.ifBlank { "Venue TBA" },
        selected = selected,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun CricketMatchRowContent(
    matchId: String,
    leagueTitle: String,
    homeName: String,
    homeCode: String,
    awayName: String,
    awayCode: String,
    statusLabel: String,
    isLive: Boolean,
    statusDetail: String,
    venue: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val statusContainer = if (isLive) {
        edgeColors.positiveEdgeContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    }
    val statusContent = if (isLive) {
        edgeColors.positiveEdge
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("cricket_match_row_$matchId"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else edgeColors.cardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top bar: League title & current match status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = leagueTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MatchStatusBadge(
                    label = statusLabel,
                    isLive = isLive,
                    containerColor = statusContainer,
                    contentColor = statusContent,
                    modifier = Modifier.testTag("match_status_badge_$matchId")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Team Names Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TeamCodePill(
                    code = homeCode,
                    accentColor = edgeColors.capitalsAccent,
                    containerColor = edgeColors.capitalsContainer
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$homeName vs $awayName",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TeamCodePill(
                    code = awayCode,
                    accentColor = edgeColors.vipersAccent,
                    containerColor = edgeColors.vipersContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = venue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Placeholder for the Probability Edge Indicator
            ProbabilityEdgeIndicatorPlaceholder(
                matchId = matchId,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MatchStatusBadge(
    label: String,
    isLive: Boolean,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isLive) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(contentColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TeamCodePill(
    code: String,
    accentColor: Color,
    containerColor: Color
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.25.dp, accentColor),
        modifier = Modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = code.take(4),
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/**
 * Placeholder slot reserved for the computed probability edge indicator (+EV / vig-stripped split).
 */
@Composable
fun ProbabilityEdgeIndicatorPlaceholder(
    matchId: String,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val dashColor = edgeColors.cardBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(edgeColors.elevatedSurface.copy(alpha = 0.7f))
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(
                        width = 1.25.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                )
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("probability_edge_placeholder_$matchId")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.QueryStats,
                    contentDescription = stringResource(R.string.edge_indicator_placeholder_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.edge_indicator_placeholder_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.edge_indicator_placeholder_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, edgeColors.cardBorder)
            ) {
                Text(
                    text = stringResource(R.string.edge_indicator_placeholder_value),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
