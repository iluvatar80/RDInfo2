// app/src/main/java/com/rdinfo2/ui/theme/Shape.kt
package com.rdinfo2.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Medical/Emergency optimized shapes - balance between modern design and accessibility
val RDInfoShapes = Shapes(
    // Extra small - for small buttons, badges, indicators
    extraSmall = RoundedCornerShape(4.dp),

    // Small - for chips, small cards, input fields
    small = RoundedCornerShape(8.dp),

    // Medium - for cards, panels, dialog boxes
    medium = RoundedCornerShape(12.dp),

    // Large - for bottom sheets, large cards
    large = RoundedCornerShape(16.dp),

    // Extra large - for modal dialogs, full-screen overlays
    extraLarge = RoundedCornerShape(20.dp)
)

// Emergency/Medical specific shapes
object EmergencyShapes {
    // Critical elements - slightly more rounded for visibility
    val CriticalButton = RoundedCornerShape(16.dp)
    val CriticalCard = RoundedCornerShape(16.dp)

    // Timer elements - fully rounded for prominence
    val TimerContainer = RoundedCornerShape(24.dp)
    val TimerButton = RoundedCornerShape(20.dp)

    // Input fields - subtle rounding for better touch targets
    val InputField = RoundedCornerShape(8.dp)
    val InputFieldFocused = RoundedCornerShape(12.dp)

    // Medication cards - distinctive but not distracting
    val MedicationCard = RoundedCornerShape(12.dp)
    val MedicationChip = RoundedCornerShape(16.dp)

    // Algorithm cards - clear boundaries for information hierarchy
    val AlgorithmCard = RoundedCornerShape(10.dp)
    val AlgorithmStep = RoundedCornerShape(8.dp)

    // Overlay panels - smooth transitions
    val OverlayPanel = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Side panels - for gesture-based navigation
    val SidePanelLeft = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 20.dp
    )

    val SidePanelRight = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 0.dp,
        bottomStart = 20.dp,
        bottomEnd = 0.dp
    )

    // Status indicators - various shapes for different states
    val StatusIndicator = RoundedCornerShape(50) // Circular
    val StatusBadge = RoundedCornerShape(12.dp)

    // Dosing calculation containers - clear, technical appearance
    val DosingContainer = RoundedCornerShape(8.dp)
    val DosingResult = RoundedCornerShape(12.dp)

    // Warning/Alert shapes - attention-grabbing but not alarming
    val WarningCard = RoundedCornerShape(14.dp)
    val AlertDialog = RoundedCornerShape(20.dp)

    // Navigation elements
    val TabShape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    val BottomSheetShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Flowchart elements - geometric but approachable
    val FlowchartNode = RoundedCornerShape(10.dp)
    val FlowchartDecision = RoundedCornerShape(20.dp) // More rounded for decision points
    val FlowchartAction = RoundedCornerShape(8.dp)   // Less rounded for actions
    val FlowchartEnd = RoundedCornerShape(50)        // Circular for endpoints

    // Quick access elements
    val QuickAccessButton = RoundedCornerShape(20.dp)
    val QuickAccessPanel = RoundedCornerShape(16.dp)

    // Search and filter elements
    val SearchBar = RoundedCornerShape(24.dp)
    val FilterChip = RoundedCornerShape(16.dp)

    // Progress indicators
    val ProgressContainer = RoundedCornerShape(12.dp)
    val ProgressBar = RoundedCornerShape(6.dp)
}

// Utility shapes for specific use cases
object UtilityShapes {
    // No rounding - for technical displays, tables
    val None = RoundedCornerShape(0.dp)

    // Full rounding - for circular elements
    val Circle = RoundedCornerShape(50)

    // Asymmetric shapes for directional elements
    val LeftArrow = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 4.dp,
        bottomStart = 16.dp,
        bottomEnd = 4.dp
    )

    val RightArrow = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 16.dp,
        bottomStart = 4.dp,
        bottomEnd = 16.dp
    )

    // Top-only rounding for headers
    val TopRounded = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Bottom-only rounding for footers
    val BottomRounded = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )
}