// app/src/main/java/com/rdinfo2/ui/theme/Color.kt
package com.rdinfo2.ui.theme

import androidx.compose.ui.graphics.Color

object RDInfoColors {
    // Light Theme Colors
    val PrimaryLight = Color(0xFF1976D2)              // Medical Blue
    val OnPrimaryLight = Color(0xFFFFFFFF)
    val PrimaryContainerLight = Color(0xFFBBDEFB)
    val OnPrimaryContainerLight = Color(0xFF0D47A1)

    val SecondaryLight = Color(0xFF388E3C)            // Medical Green
    val OnSecondaryLight = Color(0xFFFFFFFF)
    val SecondaryContainerLight = Color(0xFFC8E6C9)
    val OnSecondaryContainerLight = Color(0xFF1B5E20)

    val TertiaryLight = Color(0xFFD32F2F)             // Emergency Red
    val OnTertiaryLight = Color(0xFFFFFFFF)
    val TertiaryContainerLight = Color(0xFFFFCDD2)
    val OnTertiaryContainerLight = Color(0xFFB71C1C)

    val ErrorLight = Color(0xFFD32F2F)
    val OnErrorLight = Color(0xFFFFFFFF)
    val ErrorContainerLight = Color(0xFFFFCDD2)
    val OnErrorContainerLight = Color(0xFFB71C1C)

    val BackgroundLight = Color(0xFFFAFAFA)
    val OnBackgroundLight = Color(0xFF1C1B1F)
    val SurfaceLight = Color(0xFFFFFFFF)
    val OnSurfaceLight = Color(0xFF1C1B1F)
    val SurfaceVariantLight = Color(0xFFE7E0EC)
    val OnSurfaceVariantLight = Color(0xFF49454F)
    val OutlineLight = Color(0xFF79747E)
    val OutlineVariantLight = Color(0xFFCAC4D0)
    val ScrimLight = Color(0xFF000000)

    // Dark Theme Colors - Optimized for Medical/Emergency Use
    val PrimaryDark = Color(0xFF64B5F6)               // Softer Medical Blue
    val OnPrimaryDark = Color(0xFF0D47A1)
    val PrimaryContainerDark = Color(0xFF1565C0)
    val OnPrimaryContainerDark = Color(0xFFE3F2FD)

    val SecondaryDark = Color(0xFF81C784)             // Softer Medical Green
    val OnSecondaryDark = Color(0xFF1B5E20)
    val SecondaryContainerDark = Color(0xFF2E7D32)
    val OnSecondaryContainerDark = Color(0xFFE8F5E8)

    val TertiaryDark = Color(0xFFEF5350)              // Softer Emergency Red
    val OnTertiaryDark = Color(0xFFB71C1C)
    val TertiaryContainerDark = Color(0xFFC62828)
    val OnTertiaryContainerDark = Color(0xFFFFEBEE)

    val ErrorDark = Color(0xFFEF5350)
    val OnErrorDark = Color(0xFFB71C1C)
    val ErrorContainerDark = Color(0xFFC62828)
    val OnErrorContainerDark = Color(0xFFFFEBEE)

    val BackgroundDark = Color(0xFF121212)            // Pure Dark Background
    val OnBackgroundDark = Color(0xFFE6E1E5)
    val SurfaceDark = Color(0xFF1E1E1E)               // Card/Panel Background
    val OnSurfaceDark = Color(0xFFE6E1E5)
    val SurfaceVariantDark = Color(0xFF2B2B2B)        // Input Fields
    val OnSurfaceVariantDark = Color(0xFFCAC4D0)
    val OutlineDark = Color(0xFF938F99)               // Borders
    val OutlineVariantDark = Color(0xFF49454F)
    val ScrimDark = Color(0xFF000000)

    // Additional Medical/Emergency Colors
    val WarningLight = Color(0xFFFF9800)              // Orange Warning
    val WarningDark = Color(0xFFFFB74D)

    val InfoLight = Color(0xFF2196F3)                 // Info Blue
    val InfoDark = Color(0xFF64B5F6)

    val SuccessLight = Color(0xFF4CAF50)              // Success Green
    val SuccessDark = Color(0xFF81C784)

    // Specialized Medical Colors
    val CardiacRed = Color(0xFFE53935)                // Heart/Cardiac
    val RespiratoryBlue = Color(0xFF1E88E5)           // Breathing/Lungs
    val NeurologicalPurple = Color(0xFF8E24AA)        // Neurological
    val TraumaOrange = Color(0xFFFF6F00)              // Trauma/Emergency
    val PediatricPink = Color(0xFFE91E63)             // Pediatric
    val GeriatricGray = Color(0xFF757575)             // Geriatric

    // Status Indication Colors
    val StatusNormal = Color(0xFF4CAF50)              // Normal/OK
    val StatusElevated = Color(0xFFFF9800)            // Elevated/Warning
    val StatusCritical = Color(0xFFD32F2F)            // Critical/Danger
    val StatusUnknown = Color(0xFF9E9E9E)             // Unknown/Not Available

    // Dosing Safety Colors
    val DosingSafe = Color(0xFF4CAF50)                // Safe dosing range
    val DosingCaution = Color(0xFFFF9800)             // Caution range
    val DosingDanger = Color(0xFFD32F2F)              // Dangerous range
    val DosingMaximum = Color(0xFF8BC34A)             // Maximum allowed dose

    // UI Feedback Colors
    val SelectedItem = Color(0xFF2196F3)              // Selected item highlight
    val HoverItem = Color(0xFFE0E0E0)                 // Hover state (light)
    val HoverItemDark = Color(0xFF424242)             // Hover state (dark)
    val DisabledItem = Color(0xFFBDBDBD)              // Disabled state (light)
    val DisabledItemDark = Color(0xFF616161)          // Disabled state (dark)

    // Timer/Progress Colors
    val TimerNormal = Color(0xFF4CAF50)               // Normal timing
    val TimerWarning = Color(0xFFFF9800)              // Time running out
    val TimerCritical = Color(0xFFD32F2F)             // Time expired

    // Medication Category Colors (subtle variations for organization)
    val MedCardiovascular = Color(0xFFFFEBEE)         // Light red tint
    val MedRespiratory = Color(0xFFE3F2FD)            // Light blue tint
    val MedNeurological = Color(0xFFF3E5F5)          // Light purple tint
    val MedAnalgesic = Color(0xFFE8F5E8)             // Light green tint
    val MedSedative = Color(0xFFFFF3E0)              // Light orange tint
    val MedAntidote = Color(0xFFE1F5FE)              // Light cyan tint
    val MedAntiallergic = Color(0xFFFCE4EC)          // Light pink tint
    val MedOther = Color(0xFFF5F5F5)                 // Light gray tint
}