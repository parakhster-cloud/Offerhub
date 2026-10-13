package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = BrandPrimaryContainerDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = BrandSecondaryDark,
    onSecondary = Color(0xFF431407),
    secondaryContainer = BrandSecondaryContainerDark,
    onSecondaryContainer = Color(0xFFFFD8CC),
    tertiary = BrandTertiaryDark,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = BrandTertiaryContainerDark,
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = DarkNavyBase,
    onBackground = TextPrimaryDark,
    surface = DarkNavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF475569),
    error = UrgencyRedDark,
    errorContainer = UrgencyRedContainerDark
)

// Vibrant, Catchy, Appealing Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryContainerLight,
    onPrimaryContainer = BrandPrimaryDeep,
    secondary = BrandSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = BrandSecondaryContainerLight,
    onSecondaryContainer = BrandSecondaryDeep,
    tertiary = BrandTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = BrandTertiaryContainerLight,
    onTertiaryContainer = BrandTertiaryDeep,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightCardVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    error = UrgencyRed,
    errorContainer = UrgencyRedContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to vibrant, appealing light theme as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

