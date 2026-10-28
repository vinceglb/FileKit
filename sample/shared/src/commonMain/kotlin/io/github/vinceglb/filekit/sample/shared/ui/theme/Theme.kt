package io.github.vinceglb.filekit.sample.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Brand schemes: green primary, sage secondary, gold tertiary, ink text and dark surfaces.
// Every text and button pair reaches WCAG AA (4.5:1), including `outline`, which the sample uses for secondary text.
// Dynamic color stays off so the brand shows on every platform.

internal val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB3F0C9),
    onPrimaryContainer = Color(0xFF00391F),
    inversePrimary = Color(0xFF86D9A4),
    secondary = Color(0xFF4D6356),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD0E8D7),
    onSecondaryContainer = Color(0xFF0A1F14),
    tertiary = Color(0xFF6E5D00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFCE7A8),
    onTertiaryContainer = Color(0xFF221B00),
    background = Color(0xFFF8FAF5),
    onBackground = BrandInk,
    surface = Color(0xFFF8FAF5),
    onSurface = BrandInk,
    surfaceVariant = Color(0xFFDCE5DC),
    onSurfaceVariant = Color(0xFF404942),
    surfaceTint = BrandGreen,
    inverseSurface = Color(0xFF1C2A22),
    inverseOnSurface = Color(0xFFEEF2EC),
    outline = Color(0xFF5E6962),
    outlineVariant = Color(0xFFC0C9C0),
    surfaceBright = Color(0xFFF8FAF5),
    surfaceDim = Color(0xFFD8DBD5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F5EF),
    surfaceContainer = Color(0xFFECF0E9),
    surfaceContainerHigh = Color(0xFFE6EAE3),
    surfaceContainerHighest = Color(0xFFE0E4DD),
)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF86D9A4),
    onPrimary = Color(0xFF00391F),
    primaryContainer = BrandGreen,
    onPrimaryContainer = Color(0xFFFFFFFF),
    inversePrimary = BrandGreen,
    secondary = Color(0xFFB4CCBB),
    onSecondary = Color(0xFF1F352A),
    secondaryContainer = Color(0xFF354B3F),
    onSecondaryContainer = Color(0xFFD0E8D7),
    tertiary = BrandGold,
    onTertiary = Color(0xFF3B2F00),
    tertiaryContainer = Color(0xFF554600),
    onTertiaryContainer = Color(0xFFFFE6A6),
    background = BrandInk,
    onBackground = Color(0xFFDEE8E0),
    surface = BrandInk,
    onSurface = Color(0xFFDEE8E0),
    surfaceVariant = Color(0xFF2A3A31),
    onSurfaceVariant = Color(0xFFC0C9C0),
    surfaceTint = Color(0xFF86D9A4),
    inverseSurface = Color(0xFFDEE8E0),
    inverseOnSurface = Color(0xFF1C2A22),
    outline = Color(0xFF8C978F),
    outlineVariant = Color(0xFF404942),
    surfaceBright = Color(0xFF2D3D33),
    surfaceDim = BrandInk,
    surfaceContainerLowest = Color(0xFF030D08),
    surfaceContainerLow = Color(0xFF0D1D14),
    surfaceContainer = Color(0xFF112219),
    surfaceContainerHigh = Color(0xFF1B2C23),
    surfaceContainerHighest = Color(0xFF26372D),
)

internal val AppMaxWidth = 500.dp

/** Whether [AppTheme] uses the dark scheme, for assets that come in a light and a dark version. */
internal val LocalDarkTheme = staticCompositionLocalOf { false }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialExpressiveTheme(
            motionScheme = MotionScheme.expressive(),
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = geistTypography(),
            content = content,
        )
    }
}
