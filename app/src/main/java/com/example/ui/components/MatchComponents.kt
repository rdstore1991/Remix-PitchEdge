package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.TossScenario
import com.example.model.VenueScorePoint
import com.example.ui.theme.LocalEdgeColors
import java.util.Locale

@Composable
fun StadiumHeroCard(
    scenario: TossScenario,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val surfaceColor = MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, edgeColors.cardBorder),
        color = surfaceColor,
        tonalElevation = 4.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
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
                                    surfaceColor.copy(alpha = 0.65f),
                                    surfaceColor.copy(alpha = 0.96f)
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
                            text = "MATCH 6 • PRE-MATCH",
                            containerColor = edgeColors.positiveEdgeContainer,
                            contentColor = edgeColors.positiveEdge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(surfaceColor.copy(alpha = 0.85f))
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
                                text = "14:30 GMT • TOSS 14:00",
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
                        TeamCrestBlock(
                            code = "DC",
                            name = "Dubai Capitals",
                            record = "0W–1L • NRR -2.750",
                            accent = edgeColors.capitalsAccent,
                            container = edgeColors.capitalsContainer,
                            alignment = Alignment.Start
                        )
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                        TeamCrestBlock(
                            code = "DV",
                            name = "Desert Vipers",
                            record = "1W–1L • NRR -0.793",
                            accent = edgeColors.vipersAccent,
                            container = edgeColors.vipersContainer,
                            alignment = Alignment.End
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                WinProbabilitySplitBar(
                    dcPct = scenario.dcWinModelPct,
                    dvPct = scenario.dvWinModelPct,
                    scenarioLabel = scenario.title
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = edgeColors.elevatedSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Season 5 Table Leader",
                            tint = edgeColors.warningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "S5 Leader: Gulf Giants 2–0 (4 pts, NRR +1.414) after 6-wkt chase of 92 vs MIE",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamCrestBlock(
    code: String,
    name: String,
    record: String,
    accent: Color,
    container: Color,
    alignment: Alignment.Horizontal
) {
    Column(horizontalAlignment = alignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (alignment == Alignment.Start) {
                TeamBadgeCircle(code = code, accent = accent, container = container)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            if (alignment == Alignment.End) {
                Spacer(modifier = Modifier.width(8.dp))
                TeamBadgeCircle(code = code, accent = accent, container = container)
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
private fun TeamBadgeCircle(code: String, accent: Color, container: Color) {
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

@Composable
fun WinProbabilitySplitBar(
    dcPct: Float,
    dvPct: Float,
    scenarioLabel: String,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val animatedDcRatio by animateFloatAsState(
        targetValue = (dcPct / 100f).coerceIn(0.1f, 0.9f),
        animationSpec = spring(stiffness = 380f),
        label = "dcProbAnim"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DC ${String.format(Locale.US, "%.1f", dcPct)}%",
                style = MaterialTheme.typography.labelLarge,
                color = edgeColors.capitalsAccent
            )
            Text(
                text = "MODEL ($scenarioLabel)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "DV ${String.format(Locale.US, "%.1f", dvPct)}%",
                style = MaterialTheme.typography.labelLarge,
                color = edgeColors.vipersAccent
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val splitX = size.width * animatedDcRatio
            val radius = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(
                color = edgeColors.vipersAccent.copy(alpha = 0.85f),
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = radius
            )
            drawRoundRect(
                color = edgeColors.capitalsAccent,
                topLeft = Offset.Zero,
                size = Size(width = splitX, height = size.height),
                cornerRadius = radius
            )
            drawLine(
                color = Color.White,
                start = Offset(splitX, 0f),
                end = Offset(splitX, size.height),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

@Composable
fun Season5VenueScoreChart(
    scores: List<VenueScorePoint>,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = edgeColors.cardBorder

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Season 5 Dubai 1st-Innings vs Historical",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            StatusChip(
                text = "S5 AVG 144.0",
                containerColor = edgeColors.warningAmberContainer,
                contentColor = edgeColors.warningAmber
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        val maxScore = 200f
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
        ) {
            val stepY = size.height / 4f
            for (i in 0..4) {
                val y = i * stepY
                drawLine(
                    color = gridColor.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (scores.isNotEmpty()) {
                val slotWidth = size.width / scores.size
                val barWidth = (slotWidth * 0.52f).coerceAtMost(42.dp.toPx())
                scores.forEachIndexed { index, point ->
                    val barHeight = ((point.score / maxScore) * size.height).coerceAtLeast(8.dp.toPx())
                    val left = index * slotWidth + (slotWidth - barWidth) / 2f
                    val top = size.height - barHeight
                    val barColor = when {
                        point.label == "S5 Avg" -> tertiaryColor
                        point.label == "Hist 1st" -> primaryColor
                        point.score < 120 -> edgeColors.negativeEdge
                        else -> secondaryColor
                    }
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            scores.forEach { point ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = point.wickets,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun PhaseTensionBar(
    dcAdvantage: Float,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val animatedRatio by animateFloatAsState(
        targetValue = dcAdvantage.coerceIn(0.15f, 0.85f),
        animationSpec = spring(stiffness = 340f),
        label = "phaseTensionAnim"
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DC Edge ${(animatedRatio * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = edgeColors.capitalsAccent
            )
            Text(
                text = "DV Edge ${((1f - animatedRatio) * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = edgeColors.vipersAccent
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val split = size.width * animatedRatio
            val r = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            drawRoundRect(
                color = edgeColors.vipersAccent,
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = r
            )
            drawRoundRect(
                color = edgeColors.capitalsAccent,
                topLeft = Offset.Zero,
                size = Size(split, size.height),
                cornerRadius = r
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(split, size.height / 2f)
            )
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
