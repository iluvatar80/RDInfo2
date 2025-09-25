// app/src/main/java/com/rdinfo2/ui/theme/Theme.kt
package com.rdinfo2.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RDInfoColors.PrimaryDark,
    onPrimary = RDInfoColors.OnPrimaryDark,
    primaryContainer = RDInfoColors.PrimaryContainerDark,
    onPrimaryContainer = RDInfoColors.OnPrimaryContainerDark,
    secondary = RDInfoColors.SecondaryDark,
    onSecondary = RDInfoColors.OnSecondaryDark,
    secondaryContainer = RDInfoColors.SecondaryContainerDark,
    onSecondaryContainer = RDInfoColors.OnSecondaryContainerDark,
    tertiary = RDInfoColors.TertiaryDark,
    onTertiary = RDInfoColors.OnTertiaryDark,
    tertiaryContainer = RDInfoColors.TertiaryContainerDark,
    onTertiaryContainer = RDInfoColors.OnTertiaryContainerDark,
    error = RDInfoColors.ErrorDark,
    onError = RDInfoColors.OnErrorDark,
    errorContainer = RDInfoColors.ErrorContainerDark,
    onErrorContainer = RDInfoColors.OnErrorContainerDark,
    background = RDInfoColors.BackgroundDark,
    onBackground = RDInfoColors.OnBackgroundDark,
    surface = RDInfoColors.SurfaceDark,
    onSurface = RDInfoColors.OnSurfaceDark,
    surfaceVariant = RDInfoColors.SurfaceVariantDark,
    onSurfaceVariant = RDInfoColors.OnSurfaceVariantDark,
    outline = RDInfoColors.OutlineDark,
    outlineVariant = RDInfoColors.OutlineVariantDark,
    scrim = RDInfoColors.ScrimDark
)

private val LightColorScheme = lightColorScheme(
    primary = RDInfoColors.PrimaryLight,
    onPrimary = RDInfoColors.OnPrimaryLight,
    primaryContainer = RDInfoColors.PrimaryContainerLight,
    onPrimaryContainer = RDInfoColors.OnPrimaryContainerLight,
    secondary = RDInfoColors.SecondaryLight,
    onSecondary = RDInfoColors.OnSecondaryLight,
    secondaryContainer = RDInfoColors.SecondaryContainerLight,
    onSecondaryContainer = RDInfoColors.OnSecondaryContainerLight,
    tertiary = RDInfoColors.TertiaryLight,
    onTertiary = RDInfoColors.OnTertiaryLight,
    tertiaryContainer = RDInfoColors.TertiaryContainerLight,
    onTertiaryContainer = RDInfoColors.OnTertiaryContainerLight,
    error = RDInfoColors.ErrorLight,
    onError = RDInfoColors.OnErrorLight,
    errorContainer = RDInfoColors.ErrorContainerLight,
    onErrorContainer = RDInfoColors.OnErrorContainerLight,
    background = RDInfoColors.BackgroundLight,
    onBackground = RDInfoColors.OnBackgroundLight,
    surface = RDInfoColors.SurfaceLight,
    onSurface = RDInfoColors.OnSurfaceLight,
    surfaceVariant = RDInfoColors.SurfaceVariantLight,
    onSurfaceVariant = RDInfoColors.OnSurfaceVariantLight,
    outline = RDInfoColors.OutlineLight,
    outlineVariant = RDInfoColors.OutlineVariantLight,
    scrim = RDInfoColors.ScrimLight
)

@Composable
fun RDInfo2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RDInfoTypography,
        shapes = RDInfoShapes,
        content = content
    )
}

// Emergency/Medical specific color extensions
object EmergencyColors {
    // xABCDE Schema Colors
    val CriticalRed = RDInfoColors.ErrorLight
    val AirwayBlue = androidx.compose.ui.graphics.Color(0xFF2196F3)
    val BreathingGreen = androidx.compose.ui.graphics.Color(0xFF4CAF50)
    val CirculationOrange = androidx.compose.ui.graphics.Color(0xFFFF9800)
    val DisabilityPurple = androidx.compose.ui.graphics.Color(0xFF9C27B0)
    val ExposureYellow = androidx.compose.ui.graphics.Color(0xFFFFC107)

    // Priority Colors
    val PriorityImmediate = androidx.compose.ui.graphics.Color(0xFFD32F2F) // Rot
    val PriorityCritical = androidx.compose.ui.graphics.Color(0xFFFF5722)  // Orange-Rot
    val PriorityHigh = androidx.compose.ui.graphics.Color(0xFFFF9800)      // Orange
    val PriorityNormal = androidx.compose.ui.graphics.Color(0xFF4CAF50)    // Grün
    val PriorityLow = androidx.compose.ui.graphics.Color(0xFF9E9E9E)       // Grau

    // Medical Status Colors
    val VitalStable = androidx.compose.ui.graphics.Color(0xFF4CAF50)       // Grün
    val VitalCritical = androidx.compose.ui.graphics.Color(0xFFD32F2F)     // Rot
    val VitalUnknown = androidx.compose.ui.graphics.Color(0xFF757575)      // Grau

    // Medication Colors
    val MedicationGiven = androidx.compose.ui.graphics.Color(0xFF4CAF50)   // Grün
    val MedicationPending = androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
    val MedicationContraindicated = androidx.compose.ui.graphics.Color(0xFFD32F2F) // Rot

    @Composable
    fun getAlgorithmColor(algorithmId: String): androidx.compose.ui.graphics.Color {
        return when (algorithmId.lowercase()) {
            "x" -> CriticalRed
            "a" -> AirwayBlue
            "b" -> BreathingGreen
            "c" -> CirculationOrange
            "d" -> DisabilityPurple
            "e" -> ExposureYellow
            else -> MaterialTheme.colorScheme.primary
        }
    }

    @Composable
    fun getPriorityColor(priority: Priority): androidx.compose.ui.graphics.Color {
        return when (priority) {
            Priority.IMMEDIATE -> PriorityImmediate
            Priority.CRITICAL -> PriorityCritical
            Priority.HIGH -> PriorityHigh
            Priority.NORMAL -> PriorityNormal
            Priority.LOW -> PriorityLow
        }
    }
}

// Import Priority enum (wird später aus AlgorithmModels importiert)
enum class Priority {
    LOW, NORMAL, HIGH, CRITICAL, IMMEDIATE
}