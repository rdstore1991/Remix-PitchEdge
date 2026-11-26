package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.FixtureOverview
import com.example.model.GroundedIntelUiState
import com.example.model.MarketEdgeItem
import com.example.model.PhaseMatchupInfo
import com.example.model.PlayerLeverageNote
import com.example.model.SportFilter
import com.example.model.TacticalPhase
import com.example.model.TossScenario
import com.example.model.VenueScorePoint
import com.example.ui.theme.LocalEdgeColors
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WorkspaceHeaderBanner(
    fixture: FixtureOverview,
    tossScenario: TossScenario,
    groundedIntelState: GroundedIntelUiState,
    onRefreshGroundedIntel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val uriHandler = LocalUriHandler.current

    // Live countdown ticker for pre-match fixtures (14:30 GMT target)
    var remainingSeconds by remember(fixture.id) { mutableLongStateOf(3780L) }
    LaunchedEffect(fixture.id) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
    }
    val hrs = remainingSeconds / 3600
    val mins = (remainingSeconds % 3600) / 60
    val secs = remainingSeconds % 60
    val countdownLabel = String.format(Locale.US, "T-%02d:%02d:%02d", hrs, mins, secs)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("workspace_header_banner"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(1.dp, edgeColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(178.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_dubai_stadium_hero),
                    contentDescription = stringResource(R.string.hero_banner_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    surfaceColor.copy(alpha = 0.25f),
                                    surfaceColor.copy(alpha = 0.68f),
                                    surfaceColor.copy(alpha = 0.98f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(
                            text = fixture.leagueTitle.uppercase(Locale.US),
                            containerColor = edgeColors.positiveEdgeContainer,
                            contentColor = edgeColors.positiveEdge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(surfaceColor.copy(alpha = 0.88f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (fixture.id == "ilt20_m6_dc_dv") countdownLabel else fixture.liveScoreOrTimer,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        BannerTeamBlock(
                            code = fixture.homeCode,
                            name = fixture.homeName,
                            record = fixture.homeRecord,
                            accent = edgeColors.capitalsAccent,
                            container = edgeColors.capitalsContainer,
                            isLeft = true
                        )
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                        BannerTeamBlock(
                            code = fixture.awayCode,
                            name = fixture.awayName,
                            record = fixture.awayRecord,
                            accent = edgeColors.vipersAccent,
                            container = edgeColors.vipersContainer,
                            isLeft = false
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                // Venue, Weather & Toss Status Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BannerTelemetryPill(
                        icon = Icons.Filled.Stadium,
                        label = fixture.venue,
                        modifier = Modifier.weight(1f)
                    )
                    BannerTelemetryPill(
                        icon = Icons.Filled.WaterDrop,
                        label = fixture.weatherLine,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = edgeColors.warningAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (fixture.id == "ilt20_m6_dc_dv") {
                                "${fixture.oversOrPeriod} • ${tossScenario.title}"
                            } else {
                                "${fixture.tossStatus} • ${fixture.oversOrPeriod}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onRefreshGroundedIntel,
                        enabled = groundedIntelState !is GroundedIntelUiState.Loading,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("google_search_grounding_button")
                    ) {
                        if (groundedIntelState is GroundedIntelUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.TravelExplore,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Search",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Live Google Search Grounding Result Panel
                when (groundedIntelState) {
                    GroundedIntelUiState.Idle -> Unit
                    GroundedIntelUiState.Loading -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = edgeColors.elevatedSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Querying Gemini 3.8 Flash + Google Search Grounding...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is GroundedIntelUiState.Error -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = edgeColors.negativeEdgeContainer,
                            border = BorderStroke(1.dp, edgeColors.negativeEdge),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = groundedIntelState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = edgeColors.negativeEdge,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onRefreshGroundedIntel) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Retry",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    is GroundedIntelUiState.Success -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = edgeColors.positiveEdgeContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, edgeColors.positiveEdge.copy(alpha = 0.55f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusChip(
                                        text = "GOOGLE SEARCH GROUNDED",
                                        containerColor = edgeColors.positiveEdgeContainer,
                                        contentColor = edgeColors.positiveEdge
                                    )
                                    Text(
                                        text = groundedIntelState.queryLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = groundedIntelState.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (groundedIntelState.sources.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    groundedIntelState.sources.take(3).forEach { src ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable { runCatching { uriHandler.openUri(src.url) } }
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.OpenInNew,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = src.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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

@Composable
private fun BannerTeamBlock(
    code: String,
    name: String,
    record: String,
    accent: Color,
    container: Color,
    isLeft: Boolean
) {
    Column(horizontalAlignment = if (isLeft) Alignment.Start else Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLeft) {
                Surface(
                    shape = CircleShape,
                    color = container,
                    border = BorderStroke(1.5.dp, accent),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            if (!isLeft) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = container,
                    border = BorderStroke(1.5.dp, accent),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = record,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BannerTelemetryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = edgeColors.elevatedSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WinProbabilityOddsCard(
    fixture: FixtureOverview,
    tossScenario: TossScenario,
    isFeaturedDcDv: Boolean,
    markets: List<MarketEdgeItem>,
    onOpenEdgesTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val homeModel = if (isFeaturedDcDv) tossScenario.dcWinModelPct else fixture.homeModelPct
    val awayModel = if (isFeaturedDcDv) tossScenario.dvWinModelPct else fixture.awayModelPct

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("win_probability_odds_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, edgeColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Win Probability & Odds Comparison",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "AI Model vs. Bookmaker Consensus (${fixture.homeCode} ${String.format(Locale.US, "%.1f", fixture.marketHomePct)}% / ${fixture.awayCode} ${String.format(Locale.US, "%.1f", fixture.marketAwayPct)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = onOpenEdgesTab,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("open_kelly_calc_button")
                ) {
                    Text(text = "Kelly Calc", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Kelly Calculator",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Model Split Bar
            WinProbabilitySplitBar(
                dcPct = homeModel,
                dvPct = awayModel,
                scenarioLabel = if (isFeaturedDcDv) tossScenario.title else "AI Model"
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Bookmaker Market Consensus Bar
            WinProbabilitySplitBar(
                dcPct = fixture.marketHomePct,
                dvPct = fixture.marketAwayPct,
                scenarioLabel = "Bookmaker Market"
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = edgeColors.cardBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            // Multi-Bookmaker Decimal Odds Comparison Table
            Text(
                text = "Multi-Bookmaker Decimal Odds",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "BOOKMAKER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = fixture.homeCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.8f)
                )
                Text(
                    text = fixture.awayCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.8f)
                )
                Text(
                    text = "VIG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "BEST +EV",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.0f)
                )
            }

            HorizontalDivider(color = edgeColors.cardBorder)

            fixture.bookmakerRows.forEachIndexed { idx, row ->
                val homeEv = ((homeModel / 100.0) * row.homeOdds - 1.0) * 100.0
                val awayEv = ((awayModel / 100.0) * row.awayOdds - 1.0) * 100.0
                val bestEv = kotlin.math.max(homeEv, awayEv)
                val evColor = when {
                    bestEv >= 1.5 -> edgeColors.positiveEdge
                    bestEv > -0.5 -> edgeColors.warningAmber
                    else -> edgeColors.negativeEdge
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.bookmaker,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1.3f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", row.homeOdds),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (row.isBestHome) edgeColors.positiveEdge else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (row.isBestHome) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", row.awayOdds),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (row.isBestAway) edgeColors.positiveEdge else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (row.isBestAway) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", row.overroundPct),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(
                        text = (if (bestEv > 0) "+" else "") + String.format(Locale.US, "%.1f%%", bestEv),
                        style = MaterialTheme.typography.labelMedium,
                        color = evColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.0f)
                    )
                }
                if (idx < fixture.bookmakerRows.lastIndex) {
                    HorizontalDivider(color = edgeColors.cardBorder.copy(alpha = 0.45f))
                }
            }

            if (markets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                ProbabilityEdgeComparisonChart(markets = markets)
            }
        }
    }
}

@Composable
fun ScoreProjectionTacticalCard(
    fixture: FixtureOverview,
    projectedFirstInnings: Int,
    projectedSecondInnings: Int,
    onFirstInningsChange: (Int) -> Unit,
    onSecondInningsChange: (Int) -> Unit,
    tossScenario: TossScenario,
    onSelectTossScenario: (TossScenario) -> Unit,
    venueScores: List<VenueScorePoint>,
    selectedPhase: TacticalPhase,
    phaseInfo: PhaseMatchupInfo?,
    playerLeverages: List<PlayerLeverageNote>,
    onSelectPhase: (TacticalPhase) -> Unit,
    onOpenTacticsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val isCricket = fixture.sport == SportFilter.CRICKET
    val minScore = when (fixture.sport) {
        SportFilter.CRICKET -> 80f
        SportFilter.FOOTBALL -> 0f
        SportFilter.TENNIS -> 12f
    }
    val maxScore = when (fixture.sport) {
        SportFilter.CRICKET -> 220f
        SportFilter.FOOTBALL -> 6f
        SportFilter.TENNIS -> 36f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("score_projection_tactical_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, edgeColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Score Projection & Tactical Matrix",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(
                    onClick = onOpenTacticsTab,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("open_full_tactics_button")
                ) {
                    Text("Full Prep", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Tactics",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Projected 1st / 2nd Innings Score Sliders
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = edgeColors.elevatedSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isCricket) "Projected 1st Innings Score" else "Projected ${fixture.homeCode} Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$projectedFirstInnings ${fixture.scoreUnitLabel}",
                            style = MaterialTheme.typography.labelLarge,
                            color = edgeColors.capitalsAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = projectedFirstInnings.toFloat().coerceIn(minScore, maxScore),
                        onValueChange = { onFirstInningsChange(it.roundToInt()) },
                        valueRange = minScore..maxScore,
                        colors = SliderDefaults.colors(
                            thumbColor = edgeColors.capitalsAccent,
                            activeTrackColor = edgeColors.capitalsAccent
                        ),
                        modifier = Modifier.testTag("first_innings_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isCricket) "Projected 2nd Innings / Chase" else "Projected ${fixture.awayCode} Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$projectedSecondInnings ${fixture.scoreUnitLabel}",
                            style = MaterialTheme.typography.labelLarge,
                            color = edgeColors.vipersAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = projectedSecondInnings.toFloat().coerceIn(minScore, maxScore),
                        onValueChange = { onSecondInningsChange(it.roundToInt()) },
                        valueRange = minScore..maxScore,
                        colors = SliderDefaults.colors(
                            thumbColor = edgeColors.vipersAccent,
                            activeTrackColor = edgeColors.vipersAccent
                        ),
                        modifier = Modifier.testTag("second_innings_slider")
                    )

                    if (isCricket) {
                        val deltaVsS5 = projectedFirstInnings - 144
                        val sign = if (deltaVsS5 >= 0) "+" else ""
                        Text(
                            text = "Delta vs S5 Dubai Avg (144.0): $sign$deltaVsS5 runs • Par target ${if (projectedFirstInnings >= 165) "favors defending" else "keeps chase live"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (projectedFirstInnings >= 165) edgeColors.warningAmber else edgeColors.positiveEdge
                        )
                    }
                }
            }

            if (fixture.id == "ilt20_m6_dc_dv") {
                Spacer(modifier = Modifier.height(12.dp))
                Season5VenueScoreChart(scores = venueScores)

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TossScenario.entries.forEach { option ->
                        FilterChip(
                            selected = option == tossScenario,
                            onClick = { onSelectTossScenario(option) },
                            label = {
                                Text(text = option.shortLabel, style = MaterialTheme.typography.labelMedium)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toss_chip_${option.name.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = edgeColors.cardBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(12.dp))

            // Tabbed Tactical & Score Projection Breakdown
            TacticalScoreProjectionTabsCard(
                fixture = fixture,
                projectedFirstInnings = projectedFirstInnings,
                projectedSecondInnings = projectedSecondInnings,
                tossScenario = tossScenario,
                selectedPhase = selectedPhase,
                phaseInfo = phaseInfo,
                onSelectPhase = onSelectPhase
            )

            // Key Player Impact Cards
            if (fixture.id == "ilt20_m6_dc_dv" && playerLeverages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Key Player Impact Cards",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    playerLeverages.forEach { player ->
                        Surface(
                            modifier = Modifier.width(260.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = edgeColors.elevatedSurface,
                            border = BorderStroke(1.dp, edgeColors.cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = player.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StatusChip(
                                    text = player.statHighlight,
                                    containerColor = edgeColors.positiveEdgeContainer,
                                    contentColor = edgeColors.positiveEdge
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = player.analysis,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
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
fun AiBestOpinionCard(
    fixture: FixtureOverview,
    onAskAnalystClick: () -> Unit,
    onRunToolAgentClick: () -> Unit,
    onOpenCardTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val filledSegments = fixture.bestPickUnits.roundToInt().coerceIn(0, 5)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_best_opinion_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, edgeColors.positiveEdge),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = edgeColors.positiveEdge,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI BEST OPINION",
                        style = MaterialTheme.typography.labelLarge,
                        color = edgeColors.positiveEdge
                    )
                }
                StatusChip(
                    text = fixture.bestPickConfidence.uppercase(Locale.US),
                    containerColor = edgeColors.positiveEdgeContainer,
                    contentColor = edgeColors.positiveEdge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = fixture.bestPickTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = edgeColors.elevatedSurface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "FAIR ODDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", fixture.bestPickFairOdds),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = edgeColors.positiveEdgeContainer.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "MARKET ODDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = edgeColors.positiveEdge
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", fixture.bestPickMarketOdds),
                            style = MaterialTheme.typography.titleLarge,
                            color = edgeColors.positiveEdge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1–5 Unit Stake Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECOMMENDED STAKE METER (1–5 UNITS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", fixture.bestPickUnits)} / 5.0u",
                    style = MaterialTheme.typography.labelLarge,
                    color = edgeColors.positiveEdge
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (unitIndex in 1..5) {
                    val active = unitIndex <= filledSegments
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = if (active) edgeColors.positiveEdge else edgeColors.elevatedSurface
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toss / Injury Risk Warning
            Surface(
                shape = MaterialTheme.shapes.small,
                color = edgeColors.warningAmberContainer.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, edgeColors.warningAmber.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Toss and Injury Risk Warning",
                        tint = edgeColors.warningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fixture.bestPickRiskWarning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAskAnalystClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("ask_ai_analyst_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Ask Analyst", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onRunToolAgentClick,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("run_pitchedge_tool_agent_button")
                ) {
                    Text("Live Tools", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onOpenCardTab,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("open_card_checklist_button")
                ) {
                    Text("14:00 Gate", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
