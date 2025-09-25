// app/src/main/java/com/rdinfo2/ui/theme/EmergencyComponents.kt
package com.rdinfo2.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Emergency-spezifische Shapes für bessere Usability
 */
object EmergencyShapes {
    val OverlayPanel = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    val ActionButton = RoundedCornerShape(12.dp)
    val InfoCard = RoundedCornerShape(8.dp)
    val AlertCard = RoundedCornerShape(16.dp)
}

/**
 * Emergency-spezifische Typography für kritische Situationen
 */
object EmergencyTypography {
    val VitalSigns = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp
    )

    val DosageAmount = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    )

    val DataEntry = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.25.sp
    )

    val EmergencyAlert = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    )
}