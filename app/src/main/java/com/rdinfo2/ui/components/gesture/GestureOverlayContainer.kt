// app/src/main/java/com/rdinfo2/ui/components/gesture/GestureOverlayContainer.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.rdinfo2.ui.components.gesture

import android.util.Log
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
import com.rdinfo2.ui.components.overlays.PatientDataOverlay

/**
 * DEBUG VERSION - Gesture-basierter Overlay Container
 * Mit Logging und vereinfachter Gesture-Erkennung
 */

@Composable
fun GestureOverlayContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var overlayState by remember { mutableStateOf(OverlayState.HIDDEN) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStartY by remember { mutableStateOf(0f) }
    var currentDragY by remember { mutableStateOf(0f) }

    val density = LocalDensity.current
    val triggerThreshold = with(density) { 100.dp.toPx() } // Reduzierte Schwelle

    Log.d("GestureDebug", "GestureOverlayContainer rendered, overlayState: $overlayState")

    Box(modifier = modifier.fillMaxSize()) {

        // Hauptinhalt zuerst rendern
        content()

        // DEBUG: Immer sichtbarer Indikator oben
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .zIndex(5f),
            color = if (overlayState == OverlayState.HIDDEN) {
                Color.Red.copy(alpha = 0.3f)
            } else {
                Color.Green.copy(alpha = 0.8f)
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (overlayState == OverlayState.HIDDEN) "WISCHE HIER" else "OVERLAY OFFEN",
                modifier = Modifier.padding(8.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Vereinfachte Gesture Detection über den gesamten oberen Bereich
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Größerer Trigger-Bereich
                .zIndex(1f)
                .background(Color.Transparent) // DEBUG: Transparenter Bereich sichtbar machen
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            Log.d("GestureDebug", "Drag started at: ${offset.x}, ${offset.y}")
                            isDragging = true
                            dragStartY = offset.y
                            currentDragY = offset.y
                        },
                        onDragEnd = {
                            Log.d("GestureDebug", "Drag ended. Distance: ${currentDragY - dragStartY}")
                            if (isDragging) {
                                val dragDistance = currentDragY - dragStartY
                                if (dragDistance > triggerThreshold) {
                                    Log.d("GestureDebug", "Threshold reached! Opening overlay")
                                    overlayState = OverlayState.PATIENT_DATA
                                } else {
                                    Log.d("GestureDebug", "Threshold not reached: $dragDistance < $triggerThreshold")
                                }
                                isDragging = false
                                currentDragY = 0f
                                dragStartY = 0f
                            }
                        }
                    ) { change, _ ->
                        if (isDragging) {
                            currentDragY = change.position.y
                            val dragDistance = currentDragY - dragStartY
                            Log.d("GestureDebug", "Dragging: distance = $dragDistance")
                        }
                    }
                }
        )

        // DEBUG: Drag Indicator während des Ziehens
        if (isDragging) {
            val progress = ((currentDragY - dragStartY) / triggerThreshold).coerceIn(0f, 1.2f)

            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp)
                    .zIndex(2f),
                color = Color.Blue.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Progress: ${(progress * 100).toInt()}%",
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }
        }

        // Overlay Content
        if (overlayState == OverlayState.PATIENT_DATA) {
            Log.d("GestureDebug", "Rendering PatientDataOverlay")

            // Semi-transparenter Hintergrund
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(10f)
                    .pointerInput(Unit) {
                        // Verhindert Gesten durch das Overlay hindurch
                        detectDragGestures { _, _ -> }
                    }
            ) {
                PatientDataOverlay(
                    onDismiss = {
                        Log.d("GestureDebug", "Overlay dismissed")
                        overlayState = OverlayState.HIDDEN
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                )
            }
        }

        // DEBUG: State Indicator unten rechts
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(5f),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "State: $overlayState",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Dragging: $isDragging",
                    style = MaterialTheme.typography.bodySmall
                )
                if (isDragging) {
                    Text(
                        text = "Distance: ${(currentDragY - dragStartY).toInt()}px",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}