package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketEdgeItem
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalEdgeColors
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

private enum class EdgeChartViewMode(val label: String) {
    DUAL("Dual View"),
    PROBABILITY("Prob %"),
    EV_DELTA("EV Bars")
}

@Composable
fun ProbabilityEdgeComparisonChart(
    markets: List<MarketEdgeItem>,
    selectedMarketId: String? = null,
    onSelectMarket: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val textMeasurer = rememberTextMeasurer()

    var viewMode by remember { mutableStateOf(EdgeChartViewMode.DUAL) }
    var internalSelectedId by remember(markets) {
        mutableStateOf(selectedMarketId ?: markets.firstOrNull()?.id)
    }
    val activeMarketId = selectedMarketId ?: internalSelectedId
    val activeMarket = markets.find { it.id == activeMarketId } ?: markets.firstOrNull()

    val marketBarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val gridColor = edgeColors.cardBorder
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val highlightSlotColor = edgeColors.elevatedSurface

    val axisStyle = remember(axisTextColor) {
        TextStyle(
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = axisTextColor
        )
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (markets.isNotEmpty()) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 260f),
        label = "probEdgeChartReveal"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("probability_edge_canvas_chart")
    ) {
        // Legend & View Mode Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendDot(color = marketBarColor, label = "Market %")
                ChartLegendDot(color = edgeColors.positiveEdge, label = "+EV")
                ChartLegendDot(color = edgeColors.warningAmber, label = "Pre-XI")
                ChartLegendDot(color = edgeColors.negativeEdge, label = "-EV")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Compact Segmented Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EdgeChartViewMode.entries.forEach { mode ->
                val selected = viewMode == mode
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
                        .clickable { viewMode = mode }
                        .testTag("edge_chart_mode_${mode.name.lowercase()}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (markets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No markets match the selected filter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        // 1. Grouped Probability Canvas (Market Implied % vs AI Model %)
        AnimatedVisibility(visible = viewMode == EdgeChartViewMode.DUAL || viewMode == EdgeChartViewMode.PROBABILITY) {
            Column {
                Text(
                    text = "AI MODEL VS. MARKET IMPLIED (%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                val maxPct = max(66f, ceil((markets.maxOfOrNull { max(it.modelPct, it.marketImpliedPct) } ?: 60.0) / 10.0).toFloat() * 10f + 6f)
                val gridTicks = listOf(0, 20, 40, 60)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(158.dp)
                        .semantics {
                            contentDescription = "Grouped probability bar chart comparing Market Implied percentage against AI Model percentage for Dubai Capitals vs Desert Vipers."
                        }
                        .testTag("grouped_probability_canvas")
                        .pointerInput(markets) {
                            detectTapGestures { offset ->
                                val leftPad = 36.dp.toPx()
                                val chartWidth = (size.width - leftPad).coerceAtLeast(1f)
                                if (offset.x >= leftPad && markets.isNotEmpty()) {
                                    val slotWidth = chartWidth / markets.size
                                    val index = ((offset.x - leftPad) / slotWidth)
                                        .toInt()
                                        .coerceIn(0, markets.lastIndex)
                                    val tappedId = markets[index].id
                                    internalSelectedId = tappedId
                                    onSelectMarket?.invoke(tappedId)
                                }
                            }
                        }
                ) {
                    val leftPad = 36.dp.toPx()
                    val topPad = 22.dp.toPx()
                    val bottomPad = 22.dp.toPx()
                    val chartWidth = (size.width - leftPad).coerceAtLeast(1f)
                    val chartHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
                    val baselineY = topPad + chartHeight
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()), 0f)

                    gridTicks.forEach { tick ->
                        val y = topPad + chartHeight * (1f - (tick / maxPct))
                        drawLine(
                            color = gridColor.copy(alpha = if (tick == 0) 0.9f else 0.5f),
                            start = Offset(leftPad, y),
                            end = Offset(size.width, y),
                            strokeWidth = if (tick == 0) 1.2.dp.toPx() else 0.8.dp.toPx(),
                            pathEffect = if (tick == 0) null else dashEffect
                        )
                        val labelLayout = textMeasurer.measure("$tick%", axisStyle)
                        drawText(
                            textLayoutResult = labelLayout,
                            topLeft = Offset(
                                x = (leftPad - labelLayout.size.width - 6.dp.toPx()).coerceAtLeast(0f),
                                y = y - labelLayout.size.height / 2f
                            )
                        )
                    }

                    val slotWidth = chartWidth / markets.size
                    val barWidth = (slotWidth * 0.24f).coerceIn(11.dp.toPx(), 24.dp.toPx())
                    val barGap = 4.dp.toPx()
                    val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                    markets.forEachIndexed { index, item ->
                        val slotLeft = leftPad + index * slotWidth
                        val slotCenterX = slotLeft + slotWidth / 2f
                        val isActive = item.id == activeMarket?.id

                        if (isActive) {
                            drawRoundRect(
                                color = highlightSlotColor.copy(alpha = 0.72f),
                                topLeft = Offset(slotLeft + 4.dp.toPx(), topPad - 4.dp.toPx()),
                                size = Size(slotWidth - 8.dp.toPx(), chartHeight + 4.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }

                        val marketHeight = ((item.marketImpliedPct.toFloat() / maxPct) * chartHeight * animatedProgress)
                            .coerceAtLeast(4.dp.toPx())
                        val modelHeight = ((item.modelPct.toFloat() / maxPct) * chartHeight * animatedProgress)
                            .coerceAtLeast(4.dp.toPx())

                        val mktBarLeft = slotCenterX - barGap / 2f - barWidth
                        val modelBarLeft = slotCenterX + barGap / 2f
                        val modelColor = edgeColorFor(item, edgeColors)

                        // Market Implied % bar
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(marketBarColor, marketBarColor.copy(alpha = 0.6f)),
                                startY = baselineY - marketHeight,
                                endY = baselineY
                            ),
                            topLeft = Offset(mktBarLeft, baselineY - marketHeight),
                            size = Size(barWidth, marketHeight),
                            cornerRadius = corner
                        )

                        // AI Model % bar (color-coded by EV status)
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(modelColor, modelColor.copy(alpha = 0.72f)),
                                startY = baselineY - modelHeight,
                                endY = baselineY
                            ),
                            topLeft = Offset(modelBarLeft, baselineY - modelHeight),
                            size = Size(barWidth, modelHeight),
                            cornerRadius = corner
                        )

                        // Edge delta badge above tallest bar
                        val sign = if (item.edgePct > 0.0) "+" else ""
                        val deltaLabel = "$sign${String.format(Locale.US, "%.1f", item.edgePct)}%"
                        val deltaLayout = textMeasurer.measure(
                            text = deltaLabel,
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = modelColor
                            )
                        )
                        val tallerTopY = baselineY - max(marketHeight, modelHeight)
                        val pillW = deltaLayout.size.width + 8.dp.toPx()
                        val pillH = deltaLayout.size.height + 4.dp.toPx()
                        val pillLeft = (slotCenterX - pillW / 2f).coerceIn(leftPad, size.width - pillW)
                        val pillTop = (tallerTopY - pillH - 4.dp.toPx()).coerceAtLeast(0f)

                        drawRoundRect(
                            color = modelColor.copy(alpha = 0.16f),
                            topLeft = Offset(pillLeft, pillTop),
                            size = Size(pillW, pillH),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        drawText(
                            textLayoutResult = deltaLayout,
                            topLeft = Offset(pillLeft + 4.dp.toPx(), pillTop + 2.dp.toPx())
                        )

                        // Short X-axis label
                        val xLayout = textMeasurer.measure(
                            text = shortMarketLabel(item),
                            style = axisStyle.copy(
                                color = if (isActive) modelColor else axisTextColor,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                        drawText(
                            textLayoutResult = xLayout,
                            topLeft = Offset(
                                x = (slotCenterX - xLayout.size.width / 2f)
                                    .coerceIn(leftPad, size.width - xLayout.size.width),
                                y = baselineY + 5.dp.toPx()
                            )
                        )
                    }
                }
            }
        }

        if (viewMode == EdgeChartViewMode.DUAL) {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. Diverging +EV / -EV Edge Canvas (Zero-baseline color-coded bars)
        AnimatedVisibility(visible = viewMode == EdgeChartViewMode.DUAL || viewMode == EdgeChartViewMode.EV_DELTA) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROBABILITY EDGE (+EV / -EV DELTA)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "0% Fair • +3% Stake Line",
                        style = MaterialTheme.typography.labelSmall,
                        color = edgeColors.positiveEdge
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                val maxAbsEdge = max(
                    10f,
                    (ceil((markets.maxOfOrNull { abs(it.edgePct) } ?: 8.0) / 2.0) * 2.0).toFloat() + 2f
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .semantics {
                            contentDescription = "Diverging expected value bar chart showing positive EV bars above zero and negative EV bars below zero."
                        }
                        .testTag("diverging_ev_canvas")
                        .pointerInput(markets) {
                            detectTapGestures { offset ->
                                val leftPad = 36.dp.toPx()
                                val chartWidth = (size.width - leftPad).coerceAtLeast(1f)
                                if (offset.x >= leftPad && markets.isNotEmpty()) {
                                    val slotWidth = chartWidth / markets.size
                                    val index = ((offset.x - leftPad) / slotWidth)
                                        .toInt()
                                        .coerceIn(0, markets.lastIndex)
                                    val tappedId = markets[index].id
                                    internalSelectedId = tappedId
                                    onSelectMarket?.invoke(tappedId)
                                }
                            }
                        }
                ) {
                    val leftPad = 36.dp.toPx()
                    val topPad = 14.dp.toPx()
                    val bottomPad = 24.dp.toPx()
                    val chartWidth = (size.width - leftPad).coerceAtLeast(1f)
                    val chartHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
                    val zeroY = topPad + chartHeight / 2f
                    val halfHeight = chartHeight / 2f
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()), 0f)

                    // Positive, Zero, Negative horizontal reference lines
                    val topY = topPad
                    val bottomY = topPad + chartHeight
                    val plusMaxLabel = "+${maxAbsEdge.toInt()}%"
                    val minusMaxLabel = "-${maxAbsEdge.toInt()}%"

                    listOf(
                        Triple(topY, plusMaxLabel, true),
                        Triple(zeroY, "0%", false),
                        Triple(bottomY, minusMaxLabel, true)
                    ).forEach { (y, label, dashed) ->
                        drawLine(
                            color = if (dashed) gridColor.copy(alpha = 0.5f) else axisTextColor.copy(alpha = 0.75f),
                            start = Offset(leftPad, y),
                            end = Offset(size.width, y),
                            strokeWidth = if (dashed) 0.8.dp.toPx() else 1.4.dp.toPx(),
                            pathEffect = if (dashed) dashEffect else null
                        )
                        val lbl = textMeasurer.measure(label, axisStyle)
                        drawText(
                            textLayoutResult = lbl,
                            topLeft = Offset(
                                x = (leftPad - lbl.size.width - 6.dp.toPx()).coerceAtLeast(0f),
                                y = y - lbl.size.height / 2f
                            )
                        )
                    }

                    // +3.0% Minimum Stake Discipline Reference Line
                    val thresholdY = zeroY - (3.0f / maxAbsEdge) * halfHeight
                    drawLine(
                        color = edgeColors.positiveEdge.copy(alpha = 0.45f),
                        start = Offset(leftPad, thresholdY),
                        end = Offset(size.width, thresholdY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    val slotWidth = chartWidth / markets.size
                    val barWidth = (slotWidth * 0.36f).coerceIn(18.dp.toPx(), 38.dp.toPx())
                    val corner = CornerRadius(5.dp.toPx(), 5.dp.toPx())

                    markets.forEachIndexed { index, item ->
                        val slotLeft = leftPad + index * slotWidth
                        val slotCenterX = slotLeft + slotWidth / 2f
                        val isActive = item.id == activeMarket?.id

                        if (isActive) {
                            drawRoundRect(
                                color = highlightSlotColor.copy(alpha = 0.72f),
                                topLeft = Offset(slotLeft + 4.dp.toPx(), topPad - 2.dp.toPx()),
                                size = Size(slotWidth - 8.dp.toPx(), chartHeight + 4.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }

                        val barColor = edgeColorFor(item, edgeColors)
                        val barLeft = slotCenterX - barWidth / 2f
                        val rawMag = ((abs(item.edgePct).toFloat() / maxAbsEdge) * halfHeight * animatedProgress)
                            .coerceIn(4.dp.toPx(), halfHeight - 2.dp.toPx())

                        val barTop = if (item.edgePct >= 0.0) zeroY - rawMag else zeroY

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = if (item.edgePct >= 0.0) {
                                    listOf(barColor, barColor.copy(alpha = 0.65f))
                                } else {
                                    listOf(barColor.copy(alpha = 0.65f), barColor)
                                },
                                startY = barTop,
                                endY = barTop + rawMag
                            ),
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth, rawMag),
                            cornerRadius = corner
                        )

                        if (item.hasRiskFlag || isActive) {
                            drawRoundRect(
                                color = if (item.hasRiskFlag) edgeColors.warningAmber else barColor,
                                topLeft = Offset(barLeft, barTop),
                                size = Size(barWidth, rawMag),
                                cornerRadius = corner,
                                style = Stroke(width = if (isActive) 1.8.dp.toPx() else 1.2.dp.toPx())
                            )
                        }

                        // Value label positioned outside the diverging bar tip
                        val sign = if (item.edgePct > 0.0) "+" else ""
                        val edgeText = "$sign${String.format(Locale.US, "%.1f", item.edgePct)}%"
                        val edgeLayout = textMeasurer.measure(
                            text = edgeText,
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = barColor
                            )
                        )
                        val labelY = if (item.edgePct >= 0.0) {
                            (barTop - edgeLayout.size.height - 2.dp.toPx()).coerceAtLeast(0f)
                        } else {
                            (barTop + rawMag + 2.dp.toPx()).coerceAtMost(bottomY - edgeLayout.size.height)
                        }
                        drawText(
                            textLayoutResult = edgeLayout,
                            topLeft = Offset(
                                x = (slotCenterX - edgeLayout.size.width / 2f)
                                    .coerceIn(leftPad, size.width - edgeLayout.size.width),
                                y = labelY
                            )
                        )

                        // X-axis label
                        val xLayout = textMeasurer.measure(
                            text = shortMarketLabel(item),
                            style = axisStyle.copy(
                                color = if (isActive) barColor else axisTextColor,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                        drawText(
                            textLayoutResult = xLayout,
                            topLeft = Offset(
                                x = (slotCenterX - xLayout.size.width / 2f)
                                    .coerceIn(leftPad, size.width - xLayout.size.width),
                                y = bottomY + 5.dp.toPx()
                            )
                        )
                    }
                }
            }
        }

        // 3. Synchronized Interactive Inspector Card
        if (activeMarket != null) {
            Spacer(modifier = Modifier.height(8.dp))
            val accent = edgeColorFor(activeMarket, edgeColors)
            val fairOdds = if (activeMarket.modelPct > 0.0) 100.0 / activeMarket.modelPct else 0.0
            val roiEvPct = ((activeMarket.modelPct / 100.0) * activeMarket.bestOdds - 1.0) * 100.0
            val edgeSign = if (activeMarket.edgePct > 0.0) "+" else ""
            val evSign = if (roiEvPct > 0.0) "+" else ""

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = edgeColors.elevatedSurface,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("probability_chart_inspector")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeMarket.marketName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mkt ${String.format(Locale.US, "%.1f", activeMarket.marketImpliedPct)}% vs AI ${String.format(Locale.US, "%.1f", activeMarket.modelPct)}% • Odds ${String.format(Locale.US, "%.2f", activeMarket.bestOdds)} (Fair ${String.format(Locale.US, "%.2f", fairOdds)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$edgeSign${String.format(Locale.US, "%.1f", activeMarket.edgePct)}% Edge",
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "EV $evSign${String.format(Locale.US, "%.1f", roiEvPct)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                }
            }
        }
    }
}

private fun edgeColorFor(
    item: MarketEdgeItem,
    edgeColors: com.example.ui.theme.EdgeSemanticColors
): Color = when {
    item.hasRiskFlag -> edgeColors.warningAmber
    item.edgePct > 0.0 -> edgeColors.positiveEdge
    else -> edgeColors.negativeEdge
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shortMarketLabel(item: MarketEdgeItem): String = when (item.id) {
    "fakhar_10_quicker" -> "Fakhar 10R"
    "holden_top_dc" -> "Holden Bat"
    "dc_match_winner" -> "DC Win"
    "dv_match_winner" -> "DV Win"
    else -> {
        val clean = item.marketName.substringBefore(" (").trim()
        when {
            clean.startsWith("1st Inn Over") -> clean.replace("1st Inn Over", "Ov").substringBefore(" Runs").trim()
            clean.contains(" ") -> clean.split(" ").filter { it.isNotBlank() }.take(2).joinToString(" ") { it.take(5) }
            else -> clean.take(10)
        }
    }
}
