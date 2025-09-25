// app/src/main/java/com/rdinfo2/ui/components/gesture/GestureOverlayContainer.kt
package com.rdinfo2.ui.components.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.data.preferences.SettingsManager
import com.rdinfo2.ui.components.overlays.PatientDataOverlay
import com.rdinfo2.ui.components.overlays.SamplerOverlay
import com.rdinfo2.ui.components.overlays.XABCDEOverlay
import com.rdinfo2.ui.theme.EmergencyShapes
import kotlin.math.abs

/**
 * Container für Gesten-basierte Overlays
 * Verwaltet das Einblenden von Patient-Daten, SAMPLER und xABCDE durch Wisch-Gesten
 */
@Composable
fun GestureOverlayContainer(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var gestureState by remember { mutableStateOf(GestureState()) }

    // Gesture detection thresholds
    val swipeThreshold = with(density) { 100.dp.toPx() }
    val minimumVelocity = with(density) { 50.dp.toPx() }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        gestureState = gestureState.copy(
                            isDragging = true,
                            startPosition = offset,
                            currentPosition = offset
                        )
                    },
                    onDragEnd = {
                        val dragDistance = gestureState.currentPosition - gestureState.startPosition
                        val gestureType = determineGestureType(
                            startPos = gestureState.startPosition,
                            dragDistance = dragDistance,
                            screenWidth = size.width.toFloat(),
                            screenHeight = size.height.toFloat(),
                            threshold = swipeThreshold
                        )

                        when (gestureType) {
                            GestureType.SWIPE_DOWN_FROM_TOP -> {
                                gestureState = gestureState.copy(
                                    patientDataVisible = !gestureState.patientDataVisible,
                                    isDragging = false
                                )
                            }
                            GestureType.SWIPE_RIGHT_FROM_LEFT -> {
                                gestureState = gestureState.copy(
                                    samplerVisible = !gestureState.samplerVisible,
                                    isDragging = false
                                )
                            }
                            GestureType.SWIPE_LEFT_FROM_RIGHT -> {
                                gestureState = gestureState.copy(
                                    xabcdeVisible = !gestureState.xabcdeVisible,
                                    isDragging = false
                                )
                            }
                            GestureType.SWIPE_UP_FROM_BOTTOM -> {
                                // Hide patient data if visible
                                if (gestureState.patientDataVisible) {
                                    gestureState = gestureState.copy(
                                        patientDataVisible = false,
                                        isDragging = false
                                    )
                                }
                            }
                            GestureType.SWIPE_LEFT_FROM_CENTER -> {
                                // Hide SAMPLER if visible
                                if (gestureState.samplerVisible) {
                                    gestureState = gestureState.copy(
                                        samplerVisible = false,
                                        isDragging = false
                                    )
                                }
                            }
                            GestureType.SWIPE_RIGHT_FROM_CENTER -> {
                                // Hide xABCDE if visible
                                if (gestureState.xabcdeVisible) {
                                    gestureState = gestureState.copy(
                                        xabcdeVisible = false,
                                        isDragging = false
                                    )
                                }
                            }
                            else -> {
                                gestureState = gestureState.copy(isDragging = false)
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        gestureState = gestureState.copy(
                            currentPosition = gestureState.currentPosition + change
                        )
                    }
                )
            }
    ) {
        // Patient Data Overlay (top)
        AnimatedVisibility(
            visible = gestureState.patientDataVisible,
            enter = slideInVertically(
                initialOffsetY = { -it }
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it }
            ),
            modifier = Modifier
                .zIndex(3f)
                .align(Alignment.TopCenter)
        ) {
            PatientDataOverlay(
                onDismiss = {
                    gestureState = gestureState.copy(patientDataVisible = false)
                }
            )
        }

        // SAMPLER Overlay (left)
        AnimatedVisibility(
            visible = gestureState.samplerVisible,
            enter = slideInHorizontally(
                initialOffsetX = { -it }
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it }
            ),
            modifier = Modifier
                .zIndex(2f)
                .align(Alignment.CenterStart)
        ) {
            SamplerOverlay(
                onDismiss = {
                    gestureState = gestureState.copy(samplerVisible = false)
                }
            )
        }

        // xABCDE Overlay (right)
        AnimatedVisibility(
            visible = gestureState.xabcdeVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it }
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it }
            ),
            modifier = Modifier
                .zIndex(2f)
                .align(Alignment.CenterEnd)
        ) {
            XABCDEOverlay(
                onDismiss = {
                    gestureState = gestureState.copy(xabcdeVisible = false)
                },
                onNavigateToAlgorithm = { algorithmId ->
                    // TODO: Navigate to specific algorithm in xABCDE tab
                    gestureState = gestureState.copy(xabcdeVisible = false)
                }
            )
        }

        // Optional: Gesture hints for first-time users
        if (!gestureState.patientDataVisible &&
            !gestureState.samplerVisible &&
            !gestureState.xabcdeVisible) {
            GestureHints()
        }
    }
}

@Composable
fun GestureHints() {
    // Subtle hints at screen edges
    Box(modifier = Modifier.fillMaxSize()) {
        // Top hint
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(width = 60.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )

        // Left hint
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(width = 4.dp, height = 60.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )

        // Right hint
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .size(width = 4.dp, height = 60.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )
    }
}

/**
 * Bestimmt den Gestentyp basierend auf Start-Position und Drag-Distanz
 */
private fun determineGestureType(
    startPos: Offset,
    dragDistance: Offset,
    screenWidth: Float,
    screenHeight: Float,
    threshold: Float
): GestureType {
    val isVerticalGesture = abs(dragDistance.y) > abs(dragDistance.x)
    val isHorizontalGesture = abs(dragDistance.x) > abs(dragDistance.y)

    // Mindest-Distanz prüfen
    if (abs(dragDistance.x) < threshold && abs(dragDistance.y) < threshold) {
        return GestureType.NONE
    }

    val edgeThreshold = 50f // Pixels from edge
    val topThreshold = 150f // Pixels from top for patient data

    return when {
        // Vertical gestures
        isVerticalGesture && startPos.y < topThreshold && dragDistance.y > threshold -> {
            GestureType.SWIPE_DOWN_FROM_TOP
        }
        isVerticalGesture && startPos.y > screenHeight - edgeThreshold && dragDistance.y < -threshold -> {
            GestureType.SWIPE_UP_FROM_BOTTOM
        }

        // Horizontal gestures from edges
        isHorizontalGesture && startPos.x < edgeThreshold && dragDistance.x > threshold -> {
            GestureType.SWIPE_RIGHT_FROM_LEFT
        }
        isHorizontalGesture && startPos.x > screenWidth - edgeThreshold && dragDistance.x < -threshold -> {
            GestureType.SWIPE_LEFT_FROM_RIGHT
        }

        // Horizontal gestures from center (for closing)
        isHorizontalGesture && startPos.x > edgeThreshold && startPos.x < screenWidth - edgeThreshold -> {
            when {
                dragDistance.x < -threshold -> GestureType.SWIPE_LEFT_FROM_CENTER
                dragDistance.x > threshold -> GestureType.SWIPE_RIGHT_FROM_CENTER
                else -> GestureType.NONE
            }
        }

        else -> GestureType.NONE
    }
}

/**
 * Zustand der Gesten-Navigation
 */
data class GestureState(
    val isDragging: Boolean = false,
    val startPosition: Offset = Offset.Zero,
    val currentPosition: Offset = Offset.Zero,
    val patientDataVisible: Boolean = false,
    val samplerVisible: Boolean = false,
    val xabcdeVisible: Boolean = false
)

/**
 * Erkannte Gestentypen
 */
enum class GestureType {
    NONE,
    SWIPE_DOWN_FROM_TOP,      // Patientendaten einblenden
    SWIPE_UP_FROM_BOTTOM,     // Patientendaten ausblenden
    SWIPE_RIGHT_FROM_LEFT,    // SAMPLER einblenden
    SWIPE_LEFT_FROM_CENTER,   // SAMPLER ausblenden
    SWIPE_LEFT_FROM_RIGHT,    // xABCDE einblenden
    SWIPE_RIGHT_FROM_CENTER   // xABCDE ausblenden
}