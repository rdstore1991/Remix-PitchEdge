package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.model.AppDestination
import com.example.ui.MatchdayUiState
import com.example.ui.MatchdayViewModel
import com.example.ui.components.AnalystChatPanel
import com.example.ui.screens.EdgesScreen
import com.example.ui.screens.MatchdayScreen
import com.example.ui.screens.SelectionsScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.screens.TacticsScreen
import com.example.ui.theme.PitchEdgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            val viewModel: MatchdayViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            PitchEdgeTheme(darkTheme = uiState.darkTheme) {
                PitchEdgeAppShell(
                    uiState = uiState,
                    onSelectTab = viewModel::selectTab,
                    onToggleTheme = viewModel::toggleTheme,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitchEdgeAppShell(
    uiState: MatchdayUiState,
    onSelectTab: (AppDestination) -> Unit,
    onToggleTheme: () -> Unit,
    viewModel: MatchdayViewModel
) {
    if (uiState.isChatOpen) {
        BackHandler {
            viewModel.setChatOpen(false)
        }
    } else if (uiState.currentTab != AppDestination.MATCHDAY) {
        BackHandler {
            onSelectTab(AppDestination.MATCHDAY)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp
        val useSideChatDrawer = maxWidth >= 840.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "GEMINI",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = uiState.activeFixture.matchLabel + " • Search Grounded",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = { viewModel.setChatOpen(!uiState.isChatOpen) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("top_bar_analyst_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Open AI Analyst Chat",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Analyst",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = stringResource(R.string.toggle_theme_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        AppDestination.entries.forEach { destination ->
                            val selected = uiState.currentTab == destination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { onSelectTab(destination) },
                                icon = {
                                    Icon(
                                        imageVector = destinationIcon(destination, selected),
                                        contentDescription = destination.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        AppDestination.entries.forEach { destination ->
                            val selected = uiState.currentTab == destination
                            NavigationRailItem(
                                selected = selected,
                                onClick = { onSelectTab(destination) },
                                icon = {
                                    Icon(
                                        imageVector = destinationIcon(destination, selected),
                                        contentDescription = destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_rail_${destination.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = if (uiState.currentTab == AppDestination.MATCHDAY) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = 840.dp)
                    }

                    Box(modifier = contentModifier) {
                        when (uiState.currentTab) {
                            AppDestination.MATCHDAY -> MatchdayScreen(
                                uiState = uiState,
                                onSelectSport = viewModel::selectSport,
                                onSelectFormat = viewModel::selectFormat,
                                onSelectStatus = viewModel::selectStatus,
                                onSelectFixture = viewModel::selectFixture,
                                onSyncLiveFeeds = viewModel::syncLiveCricApiSlate,
                                onRefreshGroundedIntel = viewModel::refreshLiveGroundedIntel,
                                onFirstInningsChange = viewModel::updateProjectedFirstInnings,
                                onSecondInningsChange = viewModel::updateProjectedSecondInnings,
                                onSelectTossScenario = viewModel::selectTossScenario,
                                onSelectXiTeam = viewModel::selectXiTeam,
                                onSelectPhase = viewModel::selectPhase,
                                onOpenEdgesTab = { onSelectTab(AppDestination.EDGES) },
                                onOpenTacticsTab = { onSelectTab(AppDestination.TACTICS) },
                                onOpenCardTab = { onSelectTab(AppDestination.SELECTIONS) },
                                onOpenAnalystChat = { viewModel.setChatOpen(true) },
                                onRunToolAgent = viewModel::runPitchEdgeToolAgent
                            )

                            AppDestination.SERIES -> SeriesScreen(
                                uiState = uiState,
                                onSelectSeries = viewModel::selectSeries,
                                onSelectSeriesTab = viewModel::selectSeriesTab,
                                onSyncSeriesLiveData = viewModel::syncSeriesLiveData,
                                onOpenFixtureQuantBreakdown = viewModel::openFixtureQuantBreakdown,
                                onRunQuantAgentForFixture = viewModel::runQuantAgentForFixture
                            )

                            AppDestination.EDGES -> EdgesScreen(
                                uiState = uiState,
                                onSelectFilter = viewModel::selectEdgeFilter,
                                onSelectCalculatorMarket = viewModel::selectCalculatorMarket,
                                onAdjustCustomOdds = viewModel::adjustCustomOdds,
                                onUpdateUnitSize = viewModel::updateUnitSize,
                                onUpdateKellyMultiplier = viewModel::updateKellyMultiplier
                            )

                            AppDestination.TACTICS -> TacticsScreen(
                                uiState = uiState,
                                onSelectPhase = viewModel::selectPhase
                            )

                            AppDestination.SELECTIONS -> SelectionsScreen(
                                uiState = uiState,
                                onToggleFakharBabar = viewModel::toggleFakharBabarOpening,
                                onTogglePowellShanaka = viewModel::togglePowellOrShanaka,
                                onToggleDcBowls = viewModel::toggleDcWonTossBowls,
                                onToggleHoldenXi = viewModel::toggleHoldenStartingXi
                            )
                        }
                    }
                }

                // Side Chat Drawer on wide/tablet screens (>= 840dp)
                if (useSideChatDrawer) {
                    AnimatedVisibility(
                        visible = uiState.isChatOpen,
                        enter = expandHorizontally(expandFrom = Alignment.End),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.End)
                    ) {
                        AnalystChatPanel(
                            activeFixtureTitle = uiState.activeFixture.matchLabel,
                            messages = uiState.chatMessages,
                            chatInput = uiState.chatInput,
                            isStreaming = uiState.isChatStreaming,
                            onInputChange = viewModel::updateChatInput,
                            onSendMessage = viewModel::sendAnalystQuestion,
                            onRunToolAgent = viewModel::runPitchEdgeToolAgent,
                            onClose = { viewModel.setChatOpen(false) },
                            modifier = Modifier.width(360.dp)
                        )
                    }
                }
            }
        }

        // Modal Sheet Chat Drawer on compact/phone screens (< 840dp)
        if (!useSideChatDrawer && uiState.isChatOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { viewModel.setChatOpen(false) },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AnalystChatPanel(
                    activeFixtureTitle = uiState.activeFixture.matchLabel,
                    messages = uiState.chatMessages,
                    chatInput = uiState.chatInput,
                    isStreaming = uiState.isChatStreaming,
                    onInputChange = viewModel::updateChatInput,
                    onSendMessage = viewModel::sendAnalystQuestion,
                    onRunToolAgent = viewModel::runPitchEdgeToolAgent,
                    onClose = { viewModel.setChatOpen(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.86f)
                )
            }
        }
    }
}

private fun destinationIcon(destination: AppDestination, selected: Boolean): ImageVector {
    return when (destination) {
        AppDestination.MATCHDAY -> if (selected) Icons.Filled.Stadium else Icons.Outlined.Stadium
        AppDestination.SERIES -> if (selected) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents
        AppDestination.EDGES -> if (selected) Icons.Filled.QueryStats else Icons.Outlined.QueryStats
        AppDestination.TACTICS -> if (selected) Icons.Filled.Analytics else Icons.Outlined.Analytics
        AppDestination.SELECTIONS -> if (selected) Icons.Filled.FactCheck else Icons.Outlined.FactCheck
    }
}
