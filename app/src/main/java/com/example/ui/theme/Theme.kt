package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Immutable
data class EdgeSemanticColors(
    val positiveEdge: Color,
    val positiveEdgeContainer: Color,
    val negativeEdge: Color,
    val negativeEdgeContainer: Color,
    val warningAmber: Color,
    val warningAmberContainer: Color,
    val capitalsAccent: Color,
    val capitalsContainer: Color,
    val vipersAccent: Color,
    val vipersContainer: Color,
    val cardBorder: Color,
    val elevatedSurface: Color
)

private val DarkEdgeColors = EdgeSemanticColors(
    positiveEdge = EmeraldEdge,
    positiveEdgeContainer = EmeraldContainerDark,
    negativeEdge = CoralAlert,
    negativeEdgeContainer = CoralContainerDark,
    warningAmber = AmberGold,
    warningAmberContainer = AmberContainerDark,
    capitalsAccent = CapitalsBlue,
    capitalsContainer = CapitalsContainerDark,
    vipersAccent = VipersGold,
    vipersContainer = VipersContainerDark,
    cardBorder = SlateBorder,
    elevatedSurface = ElevatedSlate
)

private val LightEdgeColors = EdgeSemanticColors(
    positiveEdge = EmeraldEdgeDark,
    positiveEdgeContainer = EmeraldContainerLight,
    negativeEdge = CoralAlertDark,
    negativeEdgeContainer = CoralContainerLight,
    warningAmber = AmberGoldDark,
    warningAmberContainer = AmberContainerLight,
    capitalsAccent = CapitalsBlue,
    capitalsContainer = CapitalsContainerLight,
    vipersAccent = AmberGoldDark,
    vipersContainer = VipersContainerLight,
    cardBorder = DaylightBorder,
    elevatedSurface = SoftCoolSurface
)

val LocalEdgeColors = staticCompositionLocalOf { DarkEdgeColors }

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldEdge,
    onPrimary = ObsidianNight,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldEdge,
    secondary = CapitalsBlue,
    onSecondary = ChalkWhite,
    secondaryContainer = CapitalsContainerDark,
    onSecondaryContainer = ChalkWhite,
    tertiary = AmberGold,
    onTertiary = ObsidianNight,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = AmberGold,
    background = ObsidianNight,
    onBackground = ChalkWhite,
    surface = MidnightSlate,
    onSurface = ChalkWhite,
    surfaceVariant = ElevatedSlate,
    onSurfaceVariant = MutedSteel,
    outline = SlateBorder,
    outlineVariant = SlateBorder,
    error = CoralAlert,
    onError = ObsidianNight,
    errorContainer = CoralContainerDark,
    onErrorContainer = CoralAlert
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldEdgeDark,
    onPrimary = PureSurface,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = EmeraldEdgeDark,
    secondary = CapitalsBlue,
    onSecondary = PureSurface,
    secondaryContainer = CapitalsContainerLight,
    onSecondaryContainer = InkNavy,
    tertiary = AmberGoldDark,
    onTertiary = PureSurface,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = InkNavy,
    background = IvoryPitch,
    onBackground = InkNavy,
    surface = PureSurface,
    onSurface = InkNavy,
    surfaceVariant = SoftCoolSurface,
    onSurfaceVariant = SlateInk,
    outline = DaylightBorder,
    outlineVariant = DaylightBorder,
    error = CoralAlertDark,
    onError = PureSurface,
    errorContainer = CoralContainerLight,
    onErrorContainer = CoralAlertDark
)

val PitchEdgeShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PitchEdgeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val edgeColors = if (darkTheme) DarkEdgeColors else LightEdgeColors

    CompositionLocalProvider(LocalEdgeColors provides edgeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = PitchEdgeShapes,
            content = content
        )
    }
}
