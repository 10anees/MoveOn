package com.example.moveon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =============================================================================
// MoveOnTheme — wires the global dark mode preference into Compose
// =============================================================================
//
// HOW DARK MODE IS DELIVERED
//   1. `LocalAppDarkTheme` is a CompositionLocal published by `MoveOnTheme`
//      that exposes the current dark/light flag to every composition descendant.
//   2. The theme-adaptive color properties in `Color.kt` (LightBackground,
//      LightTextPrimary, LightBorder, ...) read this CompositionLocal and
//      automatically resolve to the matching palette token.
//   3. The Material 3 `colorScheme` is also swapped so any code that uses
//      `MaterialTheme.colorScheme.*` (the idiomatic Material API) is themed
//      consistently.
//
// EXTERNAL CONTROL
//   The hosting Activity (MainActivity) reads the user's persisted preference
//   from `UserPreferences.darkModeFlow` and passes it as `darkTheme`. Toggling
//   the preference from `AppSettingsScreen` immediately flips the entire UI.
// =============================================================================

/**
 * CompositionLocal exposing the active dark mode flag for the MoveOn app.
 *
 * UI code does not normally read this directly — instead use the adaptive
 * color properties (e.g. `LightBackground`, `LightTextPrimary`) which already
 * delegate to it. Reading this is only useful for one-off branches that need
 * to vary asset selection (e.g. a logo) by theme.
 */
val LocalAppDarkTheme = staticCompositionLocalOf { false }

// ============ LIGHT COLOR SCHEME ============
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBEDEFF),
    onPrimaryContainer = Color(0xFF001D3B),

    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA5F0D6),
    onSecondaryContainer = Color(0xFF002619),

    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEADDFF),
    onTertiaryContainer = Color(0xFF21005E),

    background = LightBackgroundValue,
    onBackground = LightTextPrimaryValue,

    surface = LightSurfaceValue,
    onSurface = LightTextPrimaryValue,
    surfaceVariant = LightSurfaceVariantValue,
    onSurfaceVariant = LightTextSecondaryValue,

    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFDDADA),
    onErrorContainer = Color(0xFF410E0B),

    outline = LightBorderValue,
    outlineVariant = LightBorderLightValue
)

// ============ DARK COLOR SCHEME ============
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFBEDEFF),

    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF084C3E),
    onSecondaryContainer = Color(0xFFA5F0D6),

    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF5D3FA0),
    onTertiaryContainer = Color(0xFFEADDFF),

    background = DarkBackgroundValue,
    onBackground = DarkTextPrimaryValue,

    surface = DarkSurfaceValue,
    onSurface = DarkTextPrimaryValue,
    surfaceVariant = DarkSurfaceVariantValue,
    onSurfaceVariant = DarkTextSecondaryValue,

    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDADA),

    outline = DarkBorderValue,
    outlineVariant = DarkBorderSubtleValue
)

// ============ MOVE ON THEME COMPOSABLE ============
@Composable
fun MoveOnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
