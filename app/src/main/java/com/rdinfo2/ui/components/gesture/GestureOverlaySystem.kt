// app/src/main/java/com/rdinfo2/ui/components/gesture/GestureOverlaySystem.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.rdinfo2.ui.components.gesture

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.ui.components.overlays.PatientDataOverlay

/**
 * FIXED: Gesture-basiertes Overlay-System
 * Verwaltet Overlays durch Wischgesten - funktioniert jetzt korrekt
 * Verwendet geteiltes OverlayState enum
 */

@Composable
fun GestureOverlaySystem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var overlayState by remember { mutableStateOf(OverlayState.HIDDEN) }
    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val triggerThreshold = with(density) { 120.dp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        // Hauptinhalt
        content()

        // Gesture Detection Layer (nur wenn kein Overlay offen ist)
        if (overlayState == OverlayState.HIDDEN) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                // Nur Drags vom oberen Bildschirmbereich (erste 100px) erlauben
                                if (offset.y < 100) {
                                    isDragging = true
                                    dragOffset = 0f
                                }
                            },
                            onDragEnd = {
                                if (isDragging) {
                                    // Prüfen ob genug nach unten gezogen wurde
                                    if (dragOffset > triggerThreshold) {
                                        overlayState = OverlayState.PATIENT_DATA
                                    }
                                    isDragging = false
                                    dragOffset = 0f
                                }
                            }
                        ) { change, _ ->
                            if (isDragging) {
                                // Nur nach unten ziehen erlauben
                                if (change.position.y > 0) {
                                    dragOffset = change.position.y.coerceAtMost(triggerThreshold * 2)
                                }
                            }
                        }
                    }
            )
        }

        // Drag Indicator (zeigt Fortschritt beim Ziehen)
        if (isDragging && dragOffset > 0) {
            val progress = (dragOffset / triggerThreshold).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .zIndex(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f + progress * 0.7f),
                            RoundedCornerShape(2.dp)
                        )
                )

                // Progress Text
                if (progress > 0.3f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (progress > 0.8f) "Loslassen!" else "Weiter ziehen...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = progress)
                    )
                }
            }
        }

        // Overlay Content mit Animation
        AnimatedVisibility(
            visible = overlayState != OverlayState.HIDDEN,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300, easing = EaseInOut)
            ) + fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
        ) {
            when (overlayState) {
                OverlayState.PATIENT_DATA -> {
                    // Hintergrund-Overlay (Semi-transparent)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .pointerInput(Unit) {
                                // Verhindert Gesten durch das Overlay hindurch
                                detectDragGestures { _, _ -> }
                            }
                    ) {
                        PatientDataOverlay(
                            onDismiss = {
                                overlayState = OverlayState.HIDDEN
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 60.dp)
                        )
                    }
                }
                OverlayState.SAMPLER -> {
                    TemporaryOverlay("Sampler Overlay") {
                        overlayState = OverlayState.HIDDEN
                    }
                }
                OverlayState.XABCDE -> {
                    TemporaryOverlay("xABCDE Overlay") {
                        overlayState = OverlayState.HIDDEN
                    }
                }
                OverlayState.HIDDEN -> {
                    // Sollte nicht erreicht werden
                }
            }
        }
    }
}

/**
 * Temporärer Overlay-Placeholder für zukünftige Features
 */
@Composable
private fun TemporaryOverlay(
    title: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                // Verhindert Gesten durch das Overlay hindurch
                detectDragGestures { _, _ -> }
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Wird in der nächsten Version implementiert",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = onDismiss) {
                    Text("Schließen")
                }
            }
        }
    }
}