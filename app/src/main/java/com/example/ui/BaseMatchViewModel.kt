package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CricketMatchRepository
import com.example.data.MatchTelemetryBundle
import com.example.model.FixtureOverview
import com.example.model.SeriesBundle
import com.example.model.SeriesLeague
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException

private const val TAG = "BaseMatchViewModel"

private fun safeLogW(message: String, tr: Throwable) {
    runCatching { Log.w(TAG, message, tr) }
}

private fun safeLogE(message: String, tr: Throwable) {
    runCatching { Log.e(TAG, message, tr) }
}

/**
 * Standard asynchronous UI state wrapper for cricket telemetry and market odds fetches.
 */
sealed interface AsyncUiState<out T> {
    data object Idle : AsyncUiState<Nothing>
    data class Loading(val message: String = "Fetching live telemetry...") : AsyncUiState<Nothing>
    data class Success<T>(
        val data: T,
        val isFromCache: Boolean = false,
        val updatedAtMillis: Long = System.currentTimeMillis()
    ) : AsyncUiState<T>

    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val isTimeout: Boolean = false
    ) : AsyncUiState<Nothing>

    val isLoading: Boolean
        get() = this is Loading

    val dataOrNull: T?
        get() = (this as? Success)?.data

    val errorMessageOrNull: String?
        get() = (this as? Error)?.message
}

/**
 * Lightweight pure [ViewModel] base class for screen-level state management without an [Application] dependency.
 */
abstract class BaseDataViewModel<S>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val activeJobs = mutableMapOf<String, Job>()

    protected val currentState: S
        get() = _state.value

    protected fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    protected fun <T> launchAsync(
        key: String? = null,
        loadingMessage: String = "Loading...",
        timeoutMillis: Long = 30_000L,
        onStateChange: (AsyncUiState<T>) -> Unit,
        block: suspend () -> T
    ): Job {
        if (key != null) {
            activeJobs[key]?.cancel()
        }
        val job = viewModelScope.launch {
            onStateChange(AsyncUiState.Loading(loadingMessage))
            try {
                val result = withTimeout(timeoutMillis) { block() }
                onStateChange(AsyncUiState.Success(result))
            } catch (e: TimeoutCancellationException) {
                safeLogW("Async operation timed out (${timeoutMillis}ms)", e)
                onStateChange(
                    AsyncUiState.Error(
                        message = "Request timed out after ${timeoutMillis / 1000}s. Please retry.",
                        cause = e,
                        isTimeout = true
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                safeLogE("HTTP ${e.code()} error during async fetch", e)
                onStateChange(
                    AsyncUiState.Error(
                        message = "API error (HTTP ${e.code()}): ${e.message()}",
                        cause = e
                    )
                )
            } catch (e: Exception) {
                safeLogE("Async fetch failed", e)
                onStateChange(
                    AsyncUiState.Error(
                        message = e.localizedMessage ?: "Unexpected error loading cricket data.",
                        cause = e
                    )
                )
            }
        }
        if (key != null) {
            activeJobs[key] = job
        }
        return job
    }

    override fun onCleared() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        super.onCleared()
    }
}

/**
 * Base [AndroidViewModel] providing cooperative coroutine cancellation, timeouts,
 * and standardized [AsyncUiState] transitions (Loading / Success / Error).
 */
abstract class BaseAsyncViewModel(application: Application) : AndroidViewModel(application) {

    private val activeJobs = mutableMapOf<String, Job>()

    protected fun <T> launchAsync(
        key: String? = null,
        loadingMessage: String = "Fetching cricket telemetry...",
        timeoutMillis: Long = 30_000L,
        onStateChange: (AsyncUiState<T>) -> Unit,
        block: suspend () -> T
    ): Job {
        if (key != null) {
            activeJobs[key]?.cancel()
        }
        val job = viewModelScope.launch {
            onStateChange(AsyncUiState.Loading(loadingMessage))
            try {
                val data = withTimeout(timeoutMillis) { block() }
                onStateChange(AsyncUiState.Success(data))
            } catch (e: TimeoutCancellationException) {
                safeLogW("Request timed out (${timeoutMillis}ms)", e)
                onStateChange(
                    AsyncUiState.Error(
                        message = "Request timed out after ${timeoutMillis / 1000}s. Tap to retry.",
                        cause = e,
                        isTimeout = true
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                safeLogE("HTTP ${e.code()} during cricket data fetch", e)
                onStateChange(
                    AsyncUiState.Error(
                        message = "API request failed (HTTP ${e.code()}): ${e.message()}",
                        cause = e
                    )
                )
            } catch (e: Exception) {
                safeLogE("Cricket data fetch failed", e)
                onStateChange(
                    AsyncUiState.Error(
                        message = e.localizedMessage ?: "Unable to fetch cricket data.",
                        cause = e
                    )
                )
            }
        }
        if (key != null) {
            activeJobs[key] = job
        }
        return job
    }

    override fun onCleared() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        super.onCleared()
    }
}

/**
 * Base ViewModel for cricket matchday, scorecard, and series telemetry orchestration.
 * Manages [AsyncUiState] flows for the fixture slate, active fixture scorecard/odds bundle,
 * and ILT20 / WBBL series telemetry via [CricketMatchRepository].
 */
open class BaseCricketMatchViewModel(
    application: Application,
    protected val cricketRepository: CricketMatchRepository = CricketMatchRepository(application)
) : BaseAsyncViewModel(application) {

    private val _fixturesAsyncState =
        MutableStateFlow<AsyncUiState<List<FixtureOverview>>>(
            AsyncUiState.Success(cricketRepository.baselineFixtures, isFromCache = true)
        )
    val fixturesAsyncState: StateFlow<AsyncUiState<List<FixtureOverview>>> =
        _fixturesAsyncState.asStateFlow()

    private val _telemetryAsyncState =
        MutableStateFlow<AsyncUiState<MatchTelemetryBundle>>(AsyncUiState.Idle)
    val telemetryAsyncState: StateFlow<AsyncUiState<MatchTelemetryBundle>> =
        _telemetryAsyncState.asStateFlow()

    private val _seriesAsyncState =
        MutableStateFlow<AsyncUiState<SeriesBundle>>(AsyncUiState.Idle)
    val seriesAsyncState: StateFlow<AsyncUiState<SeriesBundle>> =
        _seriesAsyncState.asStateFlow()

    fun loadCricketSlate(forceRefresh: Boolean = false): Job =
        launchAsync(
            key = JOB_SLATE,
            loadingMessage = "Syncing live CricAPI cricket fixtures...",
            onStateChange = { _fixturesAsyncState.value = it }
        ) {
            cricketRepository.fetchSynchronizedCricketSlate(forceRefresh = forceRefresh)
        }

    fun loadMatchTelemetry(
        fixture: FixtureOverview,
        sportKeyHint: String? = null
    ): Job =
        launchAsync(
            key = JOB_TELEMETRY,
            loadingMessage = "Fetching scorecard & vig-free odds for ${fixture.matchLabel}...",
            onStateChange = { _telemetryAsyncState.value = it }
        ) {
            cricketRepository.fetchMatchTelemetry(fixture = fixture, sportKeyHint = sportKeyHint)
        }

    fun loadConsistentSeriesBundle(league: SeriesLeague): Job =
        launchAsync(
            key = JOB_SERIES,
            loadingMessage = "Synchronizing ${league.shortLabel} points & fixtures...",
            onStateChange = { _seriesAsyncState.value = it }
        ) {
            cricketRepository.fetchConsistentSeriesBundle(league)
        }

    private companion object {
        const val JOB_SLATE = "cricket_slate"
        const val JOB_TELEMETRY = "match_telemetry"
        const val JOB_SERIES = "series_bundle"
    }
}
