package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.FixtureOverview
import com.example.ui.AsyncUiState
import com.example.ui.BaseCricketMatchViewModel
import com.example.ui.components.CricketMatchRow
import com.example.ui.theme.LocalEdgeColors

/**
 * Main cricket match listings screen powered by [BaseCricketMatchViewModel].
 * Automatically triggers an API slate sync on initial composition and renders
 * the matches inside a [Scaffold] with a [TopAppBar] and central [LazyColumn].
 */
@Composable
fun MainMatchesScreen(
    viewModel: BaseCricketMatchViewModel,
    selectedFixtureId: String? = null,
    onMatchClick: (FixtureOverview) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val fixturesState by viewModel.fixturesAsyncState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.loadCricketSlate(forceRefresh = false)
    }

    MainMatchesScreen(
        fixturesState = fixturesState,
        selectedFixtureId = selectedFixtureId,
        onMatchClick = onMatchClick,
        onRefresh = { viewModel.loadCricketSlate(forceRefresh = true) },
        modifier = modifier
    )
}

/**
 * Stateless main screen composable implementing a Material 3 [Scaffold] with a [TopAppBar]
 * showing the app name and a central [LazyColumn] displaying cricket match listings fetched from the API.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMatchesScreen(
    fixturesState: AsyncUiState<List<FixtureOverview>>,
    selectedFixtureId: String? = null,
    onMatchClick: (FixtureOverview) -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_matches_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SportsCricket,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("main_matches_topbar_title")
                            )
                            Text(
                                text = stringResource(R.string.matches_screen_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !fixturesState.isLoading,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("main_matches_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.sync_matches_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .testTag("main_matches_lazy_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (fixturesState) {
                    is AsyncUiState.Loading -> {
                        item(key = "loading_card") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("main_matches_loading_card"),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, edgeColors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = fixturesState.message,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.loading_matches_message),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is AsyncUiState.Error -> {
                        item(key = "error_card") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("main_matches_error_card"),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = edgeColors.negativeEdgeContainer.copy(alpha = 0.55f)
                                ),
                                border = BorderStroke(1.dp, edgeColors.negativeEdge.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Warning,
                                            contentDescription = null,
                                            tint = edgeColors.negativeEdge,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = fixturesState.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    FilledTonalButton(
                                        onClick = onRefresh,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(text = stringResource(R.string.retry_label))
                                    }
                                }
                            }
                        }
                    }

                    is AsyncUiState.Idle -> {
                        item(key = "idle_empty_card") {
                            EmptyMatchesStateCard(onRefresh = onRefresh)
                        }
                    }

                    is AsyncUiState.Success -> {
                        val matches = fixturesState.data
                        if (matches.isEmpty()) {
                            item(key = "empty_matches_card") {
                                EmptyMatchesStateCard(onRefresh = onRefresh)
                            }
                        } else {
                            item(key = "matches_header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.match_listings_section_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${matches.size} fixtures",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            items(items = matches, key = { it.id }) { fixture ->
                                CricketMatchRow(
                                    fixture = fixture,
                                    selected = fixture.id == selectedFixtureId,
                                    onClick = { onMatchClick(fixture) }
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
private fun EmptyMatchesStateCard(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edgeColors = LocalEdgeColors.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_matches_empty_card"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, edgeColors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.SportsCricket,
                contentDescription = stringResource(R.string.empty_matches_title),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.empty_matches_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.empty_matches_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            FilledTonalButton(
                onClick = onRefresh,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.sync_matches_label))
            }
        }
    }
}
