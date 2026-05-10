package com.example.moveon.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// =============================================================================
// MoveOn Color System — adaptive light/dark
// =============================================================================
//
// PUBLIC API for screens & components:
//   - Brand colors (theme-agnostic):   Primary, Secondary, Tertiary, Accent,
//                                      Success, Warning, Error, ErrorDeep
//   - Theme-adaptive surfaces & text:  LightBackground, LightSurface,
//                                      LightSurfaceVariant, LightTextPrimary,
//                                      LightTextSecondary, LightTextTertiary,
//                                      LightBorder, LightBorderLight
//
// DESIGN NOTE
// The "Light*" names are kept for source compatibility with the existing UI
// codebase. Despite their names, each one is a *theme-adaptive* @Composable
// property: when the app is running in dark mode, they automatically resolve to
// the matching dark-palette token. This means every screen that already uses
// `LightBackground`, `LightTextPrimary`, etc. immediately gets dark mode for
// free — no per-screen rewrites are required to support new themes.
//
// HOW TO ADD A NEW SCREEN (replicable recipe)
//   1. Use the existing `LightX` color names exactly as before. They will
//      respond to the global dark-mode preference automatically.
//   2. For Material defaults you can also use `MaterialTheme.colorScheme.*`
//      directly; both APIs read from the same underlying palette tokens.
//   3. Avoid hardcoded `Color(0xFF...)` for backgrounds, text, or borders.
//
// =============================================================================

// ============ SEMANTIC BRAND COLORS (theme-agnostic) ============
val Primary = Color(0xFF1565C0)           // Deep Blue - used throughout screens
val Secondary = Color(0xFF10B981)         // Cyber Mint - verified states, success
val Tertiary = Color(0xFF7C4DFF)          // Purple - AI/automation features
val Accent = Color(0xFFFF6F00)            // Orange - urgent actions, QR scanning
val Success = Color(0xFF2E7D32)           // Deep Green - success highlights
val Warning = Color(0xFFF59E0B)           // Amber - cautionary elements
val Error = Color(0xFFEF4444)             // Error Red - error states
val ErrorDeep = Color(0xFFD32F2F)         // Deep Red - destructive actions

// ============ LIGHT PALETTE TOKENS (raw hex; theme-agnostic) ============
// These are the literal light-theme colors. UI code should NOT reference these
// directly — use the theme-adaptive properties below (LightBackground, etc.).
val LightBackgroundValue = Color(0xFFFAFAFA)
val LightSurfaceValue = Color(0xFFFFFFFF)
val LightSurfaceVariantValue = Color(0xFFF5F5F5)
val LightTextPrimaryValue = Color(0xFF1C1B1F)
val LightTextSecondaryValue = Color(0xFF757575)
val LightTextTertiaryValue = Color(0xFFA0A0A0)
val LightBorderValue = Color(0xFFE0E0E0)
val LightBorderLightValue = Color(0xFFEDEDED)

// ============ DARK PALETTE TOKENS (raw hex; theme-agnostic) ============
val DarkBackgroundValue = Color(0xFF0F172A)     // Deep Slate
val DarkSurfaceValue = Color(0xFF1E293B)        // Slightly raised slate (cards)
val DarkSurfaceVariantValue = Color(0xFF263449) // Alt surface (search bars, inputs)
val DarkTextPrimaryValue = Color(0xFFF8FAFC)    // Near-white primary text
val DarkTextSecondaryValue = Color(0xFFB4C0D3)  // Slate secondary text
val DarkTextTertiaryValue = Color(0xFF7C8BA1)   // Disabled / tertiary text
val DarkBorderValue = Color(0xFF334155)         // Visible border on dark
val DarkBorderSubtleValue = Color(0xFF1E293B)   // Subtle divider on dark

// ============ THEME-ADAPTIVE PROPERTIES (PUBLIC UI API) ============
// These read `LocalAppDarkTheme` and return the correct palette token for the
// active theme. Because they are top-level @Composable @ReadOnlyComposable
// properties, every existing call site like `color = LightBackground` becomes
// dark-mode aware automatically.

val LightBackground: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkBackgroundValue else LightBackgroundValue

val LightSurface: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkSurfaceValue else LightSurfaceValue

val LightSurfaceVariant: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkSurfaceVariantValue else LightSurfaceVariantValue

val LightTextPrimary: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkTextPrimaryValue else LightTextPrimaryValue

val LightTextSecondary: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkTextSecondaryValue else LightTextSecondaryValue

val LightTextTertiary: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkTextTertiaryValue else LightTextTertiaryValue

val LightBorder: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkBorderValue else LightBorderValue

val LightBorderLight: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkBorderSubtleValue else LightBorderLightValue

// Legacy aliases — preserved for any callers still importing the old "Dark*"
// names. New code should use the LightX adaptive properties above.
val DarkBackground: Color
    @Composable @ReadOnlyComposable
    get() = LightBackground

val DarkSurface: Color
    @Composable @ReadOnlyComposable
    get() = LightSurface

val DarkSurfaceVariant: Color
    @Composable @ReadOnlyComposable
    get() = LightSurfaceVariant

val DarkTextPrimary: Color
    @Composable @ReadOnlyComposable
    get() = LightTextPrimary

val DarkTextSecondary: Color
    @Composable @ReadOnlyComposable
    get() = LightTextSecondary

val DarkTextTertiary: Color
    @Composable @ReadOnlyComposable
    get() = LightTextTertiary

val DarkBorder: Color
    @Composable @ReadOnlyComposable
    get() = LightBorder

val DarkBorderSubtle: Color
    @Composable @ReadOnlyComposable
    get() = LightBorderLight

// ============ ACCENT / SELECTION SURFACES (theme-adaptive) ============
// Blue-tinted surfaces for selected states, price summaries, etc.
val LightAccentSurfaceValue = Color(0xFFDDECF9)       // Light blue tint (selected cards, price box)
val LightAccentBorderValue = Color(0xFF9FC4E9)        // Light blue border
val LightIconBackgroundValue = Color(0xFFE8EDF3)      // Light grey for icon backgrounds
val LightStepInactiveValue = Color(0xFF666666)        // Inactive step text

val DarkAccentSurfaceValue = Color(0xFF1A3A5C)        // Dark blue tint
val DarkAccentBorderValue = Color(0xFF3A6890)         // Dark blue border
val DarkIconBackgroundValue = Color(0xFF2D3E50)       // Dark grey for icon backgrounds
val DarkStepInactiveValue = Color(0xFF9CA3AF)         // Light grey for inactive steps in dark mode

val AccentSurface: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkAccentSurfaceValue else LightAccentSurfaceValue

val AccentBorder: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkAccentBorderValue else LightAccentBorderValue

val IconBackground: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkIconBackgroundValue else LightIconBackgroundValue

val StepInactive: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalAppDarkTheme.current) DarkStepInactiveValue else LightStepInactiveValue

// ============ GLASSMORPHISM & EFFECTS ============
val GlassWhiteLight = Color(0x1AFFFFFF)   // 10% white - light glass overlay
val GlassWhiteMedium = Color(0x33FFFFFF)  // 20% white - medium glass overlay
val GlassWhiteStrong = Color(0x66FFFFFF)  // 40% white - strong glass overlay

val GlassDarkLight = Color(0x1A1F2937)    // 10% dark - dark glass overlay
val GlassDarkMedium = Color(0x331F2937)   // 20% dark - medium dark glass overlay

// ============ CATEGORY TINT COLORS ============
val BlueTint = Color(0xFF141565C0)        // 8% blue tint
val OrangeTint = Color(0xFF14FF6F00)      // 8% orange tint
val GreenTint = Color(0xFF142E7D32)       // 8% green tint
val PurpleTint = Color(0xFF147C4DFF)      // 8% purple tint

// ============ LEGACY COMPATIBILITY ============
val ElectricIndigo = Color(0xFF6366F1)
val CyberMint = Color(0xFF10B981)
val DeepSlate = Color(0xFF0F172A)
