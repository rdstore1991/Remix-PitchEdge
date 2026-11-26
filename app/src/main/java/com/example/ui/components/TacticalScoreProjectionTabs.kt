package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.FixtureOverview
import com.example.model.PhaseMatchupInfo
import com.example.model.TacticalPhase
import com.example.model.TossScenario
import com.example.ui.theme.LocalEdgeColors
import java.util.Locale
import kotlin.math.roundToInt

private data class PhaseTabSpec(
    val phase: TacticalPhase,
    val title: String,
    val oversLabel: String,
    val icon: ImageVector,
    val testTag: String
)

private val TacticalTabSpecs = listOf(
    PhaseTabSpec(
        phase = TacticalPhase.POWERPLAY,
        title = "Powerplay Efficiency",
        oversLabel = "Overs 1–6",
        icon = Icons.Filled.Bolt,
        testTag = "tactical_tab_powerplay"
    ),
    PhaseTabSpec(
        phase = TacticalPhase.MIDDLE,
        title = "Middle-Overs Spin Trap",
        oversLabel = "Overs 7–15",
        icon = Icons.Filled.Cyclone,
        testTag = "tactical_tab_middle"
    ),
    PhaseTabSpec(
        phase = TacticalPhase.DEATH,
        title = "Death-Over Batting Strike Rates",
        oversLabel = "Overs 16–20",
        icon = Icons.Filled.Speed,
        testTag = "tactical_tab_death"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalScoreProjectionTabsCard(
    fixture: FixtureOverview,
    projectedFirstInnings: Int,
    projectedSecondInnings: Int,
    tossScenario: TossScenario,
    selectedPhase: TacticalPhase,
    phaseInfo: PhaseMatchupInfo?,
    onSelectPhase: (TacticalPhase) -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    val selectedIndex = TacticalTabSpecs.indexOfFirst { it.phase == selectedPhase }.coerceAtLeast(0)

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, edgeColors.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("tactical_score_projection_tabs_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        imageVector = Icons.Filled.SportsCricket,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Tactical & Score Projection",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${fixture.homeCode} vs ${fixture.awayCode} • ${tossScenario.shortLabel} (${projectedFirstInnings}/${projectedSecondInnings} ${fixture.scoreUnitLabel})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(
                    text = TacticalTabSpecs[selectedIndex].oversLabel,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryTabRow(
                selectedTabIndex = selectedIndex,
                containerColor = edgeColors.elevatedSurface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedIndex),
                        height = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                TacticalTabSpecs.forEachIndexed { index, spec ->
                    val isSelected = index == selectedIndex
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectPhase(spec.phase) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag(spec.testTag),
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = spec.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = spec.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = selectedPhase,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(160))
                },
                label = "tacticalScoreTabTransition"
            ) { phase ->
                when (phase) {
                    TacticalPhase.POWERPLAY -> PowerplayEfficiencyTabContent(
                        fixture = fixture,
                        projectedFirstInnings = projectedFirstInnings,
                        projectedSecondInnings = projectedSecondInnings,
                        phaseInfo = phaseInfo
                    )

                    TacticalPhase.MIDDLE -> MiddleOversSpinTrapTabContent(
                        fixture = fixture,
                        projectedFirstInnings = projectedFirstInnings,
                        projectedSecondInnings = projectedSecondInnings,
                        phaseInfo = phaseInfo
                    )

                    TacticalPhase.DEATH -> DeathOverStrikeRatesTabContent(
                        fixture = fixture,
                        projectedFirstInnings = projectedFirstInnings,
                        projectedSecondInnings = projectedSecondInnings,
                        phaseInfo = phaseInfo
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerplayEfficiencyTabContent(
    fixture: FixtureOverview,
    projectedFirstInnings: Int,
    projectedSecondInnings: Int,
    phaseInfo: PhaseMatchupInfo?
) {
    val edgeColors = LocalEdgeColors.current
    val pp1stPar = (projectedFirstInnings * 0.29f).roundToInt()
    val pp2ndPar = (projectedSecondInnings * 0.29f).roundToInt()
    val ppRpo = String.format(Locale.US, "%.2f", pp1stPar / 6.0)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PhaseProjectionSummaryBanner(
            headline = phaseInfo?.headline ?: "Powerplay Efficiency (Overs 1–6)",
            scoreTag = "1st Inn PP ~$pp1stPar (${ppRpo} RPO) • Chase ~$pp2ndPar",
            dcAdvantage = phaseInfo?.dcAdvantageScore ?: 0.41f,
            dcRating = phaseInfo?.dcRatingOutOf10 ?: 6.4,
            dvRating = phaseInfo?.dvRatingOutOf10 ?: 7.8
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhaseMetricTile(
                title = "${fixture.homeCode} PP PROFILE",
                primaryStat = "6/3 & 34/5",
                secondaryStat = "M3 collapse • Chameera 4-0-25-2",
                accent = edgeColors.capitalsAccent,
                container = edgeColors.capitalsContainer,
                modifier = Modifier.weight(1f)
            )
            PhaseMetricTile(
                title = "${fixture.awayCode} PP PROFILE",
                primaryStat = "SR 135 vs 129",
                secondaryStat = "Fakhar attacks ring • Khuzaima 3/31",
                accent = edgeColors.vipersAccent,
                container = edgeColors.vipersContainer,
                modifier = Modifier.weight(1f)
            )
        }

        PhaseBulletList(
            items = phaseInfo?.keyBattles ?: listOf(
                "Naseem Shah & Khuzaima Bin Tanveer test DC's reshaped top order (Phil Salt away on England duty).",
                "Fakhar Zaman (T20 SR ~135) outpaces Babar Azam (SR ~129) inside Powerplay restrictions (+6.5% edge).",
                "Live Contraction Trigger: If DC reaches 45/1 or better at Over 6, live odds compress from 2.20 toward 2.00."
            )
        )
    }
}

@Composable
private fun MiddleOversSpinTrapTabContent(
    fixture: FixtureOverview,
    projectedFirstInnings: Int,
    projectedSecondInnings: Int,
    phaseInfo: PhaseMatchupInfo?
) {
    val edgeColors = LocalEdgeColors.current
    val mid1stPar = (projectedFirstInnings * 0.41f).roundToInt()
    val mid2ndPar = (projectedSecondInnings * 0.41f).roundToInt()
    val midRpo = String.format(Locale.US, "%.2f", mid1stPar / 9.0)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PhaseProjectionSummaryBanner(
            headline = phaseInfo?.headline ?: "Middle-Overs Spin Trap (Overs 7–15)",
            scoreTag = "1st Inn Mid ~$mid1stPar (${midRpo} RPO) • Chase ~$mid2ndPar",
            dcAdvantage = phaseInfo?.dcAdvantageScore ?: 0.50f,
            dcRating = phaseInfo?.dcRatingOutOf10 ?: 8.4,
            dvRating = phaseInfo?.dvRatingOutOf10 ?: 8.4
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhaseMetricTile(
                title = "${fixture.homeCode} SPIN CHOKE",
                primaryStat = "3.25 Econ",
                secondaryStat = "Hassan Khan (69% wkts 7–15) + Noor 2/40",
                accent = edgeColors.capitalsAccent,
                container = edgeColors.capitalsContainer,
                modifier = Modifier.weight(1f)
            )
            PhaseMetricTile(
                title = "${fixture.awayCode} SPIN CHOKE",
                primaryStat = "Shadab 3/18",
                secondaryStat = "Shadab + Mujeeb mystery squeeze",
                accent = edgeColors.vipersAccent,
                container = edgeColors.vipersContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = edgeColors.positiveEdgeContainer.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, edgeColors.positiveEdge.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "DEW & GRIP FACTOR: Low November humidity (29–44%) minimizes evening dew in Dubai, keeping wrist-spin and cutters biting across both innings (S5 venue 1st-inn avg: 144.0).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(10.dp)
            )
        }

        PhaseBulletList(
            items = phaseInfo?.keyBattles ?: listOf(
                "Hassan Khan (4-0-13-0 vs ADKR) + Noor Ahmad create a 9-over wrist/finger spin squeeze.",
                "Babar Azam's strike rotation mitigates DV dot-ball clusters, while DC choked ADKR to 48/6 late.",
                "Cluster Wicket Sensitivity: Losing 3+ wickets in Overs 7–15 drops expected total by ~18–22 runs."
            )
        )
    }
}

@Composable
private fun DeathOverStrikeRatesTabContent(
    fixture: FixtureOverview,
    projectedFirstInnings: Int,
    projectedSecondInnings: Int,
    phaseInfo: PhaseMatchupInfo?
) {
    val edgeColors = LocalEdgeColors.current
    val death1stPar = (projectedFirstInnings - (projectedFirstInnings * 0.29f).roundToInt() - (projectedFirstInnings * 0.41f).roundToInt()).coerceAtLeast(1)
    val death2ndPar = (projectedSecondInnings - (projectedSecondInnings * 0.29f).roundToInt() - (projectedSecondInnings * 0.41f).roundToInt()).coerceAtLeast(1)
    val deathRpo = String.format(Locale.US, "%.2f", death1stPar / 5.0)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PhaseProjectionSummaryBanner(
            headline = phaseInfo?.headline ?: "Death-Over Batting Strike Rates (Overs 16–20)",
            scoreTag = "1st Inn Death ~$death1stPar (${deathRpo} RPO) • Chase ~$death2ndPar",
            dcAdvantage = phaseInfo?.dcAdvantageScore ?: 0.44f,
            dcRating = phaseInfo?.dcRatingOutOf10 ?: 7.1,
            dvRating = phaseInfo?.dvRatingOutOf10 ?: 7.9
        )

        // Strike-rate & slower-ball comparison rows
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = edgeColors.elevatedSurface,
            border = BorderStroke(1.dp, edgeColors.cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StrikeRateComparisonBar(
                    label = "DV Finishers (Hetmyer / Shadab)",
                    srLabel = "SR 158.4 • vs Pace/Cutters",
                    fraction = 0.79f,
                    color = edgeColors.vipersAccent
                )
                StrikeRateComparisonBar(
                    label = "DC Finishers (Stoinis / Billings)",
                    srLabel = "SR 141.2 • Thin w/o Salt & Powell",
                    fraction = 0.68f,
                    color = edgeColors.capitalsAccent
                )
                StrikeRateComparisonBar(
                    label = "Death Bowling Control (Curran / Mustafizur)",
                    srLabel = "Curran 4-0-21-2 • Fiz Cutters",
                    fraction = 0.82f,
                    color = edgeColors.positiveEdge
                )
            }
        }

        PhaseBulletList(
            items = phaseInfo?.keyBattles ?: listOf(
                "Mustafizur Rahman's off-cutters into the two-paced surface counter Shimron Hetmyer's boundary arc.",
                "Tom Curran (4-0-21-2 vs Warriorz) deploys back-of-the-hand slower balls on Dubai's large square boundaries.",
                "Toss-Sheet Catalyst: Rovman Powell or Dasun Shanaka starting at 14:00 GMT upgrades DC death SR projection by +14 pts."
            )
        )
    }
}

@Composable
private fun PhaseProjectionSummaryBanner(
    headline: String,
    scoreTag: String,
    dcAdvantage: Float,
    dcRating: Double,
    dvRating: Double
) {
    val edgeColors = LocalEdgeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            StatusChip(
                text = "DC $dcRating / DV $dvRating",
                containerColor = edgeColors.elevatedSurface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = scoreTag,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        PhaseTensionBar(dcAdvantage = dcAdvantage)
    }
}

@Composable
private fun PhaseMetricTile(
    title: String,
    primaryStat: String,
    secondaryStat: String,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = primaryStat,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = secondaryStat,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StrikeRateComparisonBar(
    label: String,
    srLabel: String,
    fraction: Float,
    color: Color
) {
    val edgeColors = LocalEdgeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = srLabel,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(edgeColors.cardBorder.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.05f, 1f))
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun PhaseBulletList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.FiberManualRecord,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(7.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
