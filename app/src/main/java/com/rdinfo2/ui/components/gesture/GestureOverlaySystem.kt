// app/src/main/java/com/rdinfo2/ui/components/overlay/GestureOverlaySystem.kt
package com.rdinfo2.ui.components.overlay

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.data.patient.PatientGender
import com.rdinfo2.data.repository.MedicationRepository
import kotlin.math.abs

/**
 * Haupt-Overlay-System mit Gesture-Erkennung
 * Ersetzt die alte GestureOverlayContainer
 */
@Composable
fun GestureOverlaySystem(
    modifier: Modifier = Modifier
) {
    var overlayState by remember { mutableStateOf(OverlayState.NONE) }
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Overlay öffnen wenn weit genug gezogen
                        if (abs(dragOffset) > 200) {
                            overlayState = when {
                                dragOffset > 0 -> OverlayState.PATIENT_DATA
                                // Später: dragOffset < -200 -> andere Overlays
                                else -> OverlayState.NONE
                            }
                        }
                        dragOffset = 0f
                    }
                ) { _, dragAmount ->
                    // Nur vertikale Gesten für jetzt (von oben nach unten)
                    if (overlayState == OverlayState.NONE) {
                        dragOffset += dragAmount.y
                        // Nur positive Werte (nach unten) zulassen
                        dragOffset = dragOffset.coerceAtLeast(0f)
                    }
                }
            }
    ) {
        // Drag-Indikator (zeigt Fortschritt beim Ziehen)
        if (dragOffset > 0 && overlayState == OverlayState.NONE) {
            DragIndicator(
                progress = (dragOffset / 200f).coerceAtMost(1f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }

        // Overlay anzeigen
        AnimatedOverlay(
            overlayState = overlayState,
            onDismiss = { overlayState = OverlayState.NONE }
        )
    }
}

@Composable
private fun DragIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .alpha(progress)
            .size(width = 60.dp, height = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = progress)
        ),
        shape = RoundedCornerShape(2.dp)
    ) {}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedOverlay(
    overlayState: OverlayState,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = overlayState != OverlayState.NONE,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { -it }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it }
        ) + fadeOut()
    ) {
        when (overlayState) {
            OverlayState.PATIENT_DATA -> PatientDataOverlay(onDismiss = onDismiss)
            OverlayState.SAMPLER -> { /* Später implementiert */ }
            OverlayState.XABCDE -> { /* Später implementiert */ }
            OverlayState.NONE -> { /* Nichts anzeigen */ }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDataOverlay(
    onDismiss: () -> Unit
) {
    val patientManager = remember { PatientDataManager.getInstance() }
    val currentPatient by patientManager.currentPatient.collectAsStateWithLifecycle()

    // Drag-Offset für Overlay
    var dragOffset by remember { mutableStateOf(0f) }

    // Lokaler Edit-State
    var tempAgeYears by remember { mutableStateOf(currentPatient.ageYears.toString()) }
    var tempAgeMonths by remember { mutableStateOf(currentPatient.ageMonths.toString()) }
    var tempWeight by remember {
        mutableStateOf(
            if (currentPatient.isManualWeight) currentPatient.weightKg.toString() else ""
        )
    }
    var tempGender by remember { mutableStateOf(currentPatient.gender) }

    // Overlay-Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .zIndex(10f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Overlay schließen bei Swipe nach oben
                        if (dragOffset < -200) {
                            onDismiss()
                        }
                        dragOffset = 0f
                    }
                ) { _, dragAmount ->
                    dragOffset += dragAmount.y
                }
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.TopCenter)
                .systemBarsPadding(), // Notch-Problem behoben
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header mit Close-Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Patientendaten",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Schließen")
                    }
                }

                // Aktueller Patient Status
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Aktueller Patient:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text("Alter: ${currentPatient.getFormattedAge()}")
                        Text("Gewicht: ${currentPatient.getFormattedWeight()}")
                        Text("Kategorie: ${currentPatient.ageCategory}")
                    }
                }

                Divider()

                // Eingabefelder
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tempAgeYears,
                        onValueChange = { tempAgeYears = it },
                        label = { Text("Jahre") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tempAgeMonths,
                        onValueChange = { tempAgeMonths = it },
                        label = { Text("Monate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = tempWeight,
                    onValueChange = { tempWeight = it },
                    label = { Text("Gewicht (kg, optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("Leer lassen für automatische Schätzung") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Geschlecht-Auswahl
                Text(
                    "Geschlecht:",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf(PatientGender.MALE, PatientGender.FEMALE).forEach { gender ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tempGender == gender,
                                onClick = { tempGender = gender }
                            )
                            Text(
                                text = when (gender) {
                                    PatientGender.MALE -> "Männlich"
                                    PatientGender.FEMALE -> "Weiblich"
                                    PatientGender.UNKNOWN -> "Unbekannt" // Wird nicht erreicht
                                },
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Quick-Patient Buttons
                Text(
                    "Schnellauswahl:",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            tempAgeYears = "0"
                            tempAgeMonths = "6"
                            tempWeight = ""
                            tempGender = PatientGender.UNKNOWN
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Säugling")
                    }
                    Button(
                        onClick = {
                            tempAgeYears = "8"
                            tempAgeMonths = "0"
                            tempWeight = ""
                            tempGender = PatientGender.UNKNOWN
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Kind")
                    }
                    Button(
                        onClick = {
                            tempAgeYears = "35"
                            tempAgeMonths = "0"
                            tempWeight = ""
                            tempGender = PatientGender.UNKNOWN
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Erwachsener")
                    }
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Abbrechen")
                    }

                    Button(
                        onClick = {
                            try {
                                val years = tempAgeYears.toIntOrNull() ?: 0
                                val months = tempAgeMonths.toIntOrNull() ?: 0
                                val weightValue = tempWeight.toDoubleOrNull()

                                val updatedPatient = if (weightValue != null && weightValue > 0) {
                                    com.rdinfo2.data.patient.PatientDataFactory.createWithWeight(
                                        years, months, weightValue, tempGender
                                    )
                                } else {
                                    com.rdinfo2.data.patient.PatientDataFactory.create(
                                        years, months, tempGender
                                    )
                                }

                                patientManager.updatePatient(updatedPatient)
                                onDismiss()
                            } catch (e: Exception) {
                                // Handle error - in real app, show error message
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}

enum class OverlayState {
    NONE,
    PATIENT_DATA,
    SAMPLER,        // Für später
    XABCDE          // Für später
}