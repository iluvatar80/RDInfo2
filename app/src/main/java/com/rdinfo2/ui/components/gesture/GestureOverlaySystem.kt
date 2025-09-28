// app/src/main/java/com/rdinfo2/ui/components/overlay/GestureOverlaySystem.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.rdinfo2.ui.components.overlay

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.rdinfo2.data.patient.PatientDataManager  // FIXED: Korrekte Import-Pfade
import com.rdinfo2.ui.components.overlays.PatientDataOverlay

/**
 * FIXED: Gesture-basiertes Overlay-System
 * Verwaltet Overlays durch Wischgesten ohne Compile-Fehler
 */

enum class OverlayState {
    HIDDEN,
    PATIENT_DATA,  // Oben nach unten wischen
    SAMPLER,       // Links nach rechts wischen (später)
    XABCDE         // Rechts nach links wischen (später)
}

@Composable
fun GestureOverlaySystem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var overlayState by remember { mutableStateOf(OverlayState.HIDDEN) }
    var dragProgress by remember { mutableStateOf(0f) }

    val density = LocalDensity.current

    Box(modifier = modifier.fillMaxSize()) {
        // Hauptinhalt
        content()

        // Gesture Detection Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { _ ->
                            // Reset progress
                            dragProgress = 0f
                        },
                        onDragEnd = {
                            // Entscheiden ob Overlay öffnen oder schließen
                            if (dragProgress > 0.3f) {
                                // Overlay öffnen
                                overlayState = OverlayState.PATIENT_DATA
                            } else {
                                // Overlay schließen
                                overlayState = OverlayState.HIDDEN
                            }
                            dragProgress = 0f
                        }
                    ) { change, _ ->
                        // Nur vertikale Drags von oben nach unten für Patient Data
                        if (change.position.y > 100) { // Mindestabstand vom oberen Rand
                            val newProgress = (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                            dragProgress = newProgress
                        }
                    }
                }
        )

        // Drag Indicator (zeigt Fortschritt)
        if (dragProgress > 0f && overlayState == OverlayState.HIDDEN) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .width(60.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = dragProgress),
                        RoundedCornerShape(2.dp)
                    )
            )
        }

        // Overlay Content mit Animation
        AnimatedVisibility(
            visible = overlayState == OverlayState.PATIENT_DATA,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            when (overlayState) {
                OverlayState.PATIENT_DATA -> {
                    PatientDataOverlay(
                        onDismiss = {
                            overlayState = OverlayState.HIDDEN
                        }
                    )
                }
                OverlayState.SAMPLER -> {
                    // TODO: Sampler Overlay implementieren
                    TemporaryOverlay("Sampler Overlay") {
                        overlayState = OverlayState.HIDDEN
                    }
                }
                OverlayState.XABCDE -> {
                    // TODO: xABCDE Overlay implementieren
                    TemporaryOverlay("xABCDE Overlay") {
                        overlayState = OverlayState.HIDDEN
                    }
                }
                OverlayState.HIDDEN -> {
                    // Nichts anzeigen
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
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
                    style = MaterialTheme.typography.headlineMedium
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