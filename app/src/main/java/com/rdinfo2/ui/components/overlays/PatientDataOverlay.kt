// app/src/main/java/com/rdinfo2/ui/components/overlays/PatientDataOverlay.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.rdinfo2.ui.components.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.data.patient.PatientGender

/**
 * WORKING: Patientendaten-Overlay ohne Compile-Fehler
 * Funktioniert mit korrigiertem PatientDataManager
 */
@Composable
fun PatientDataOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state mit korrekten Typen
    var localAgeYears by remember { mutableStateOf(PatientDataManager.ageYears.toString()) }
    var localAgeMonths by remember { mutableStateOf(PatientDataManager.ageMonths.toString()) }
    var localWeight by remember { mutableStateOf(PatientDataManager.weightKg?.toString() ?: "") }
    var localGender by remember { mutableStateOf(PatientDataManager.gender) }

    // Live-Updates anwenden
    LaunchedEffect(localAgeYears, localAgeMonths) {
        val years = localAgeYears.toIntOrNull() ?: 0
        val months = localAgeMonths.toIntOrNull() ?: 0
        if (years >= 0 && months >= 0) {
            PatientDataManager.updateAge(years, months)
        }
    }

    LaunchedEffect(localWeight) {
        val weight = if (localWeight.isBlank()) null else localWeight.toDoubleOrNull()
        PatientDataManager.updateWeight(weight)
    }

    LaunchedEffect(localGender) {
        PatientDataManager.updateGender(localGender)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(16.dp),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Patientendaten",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Schließen"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quick-Patient Buttons
                    Text(
                        text = "Schnellauswahl",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Säugling
                        OutlinedButton(
                            onClick = {
                                PatientDataManager.setInfant()
                                localAgeYears = PatientDataManager.ageYears.toString()
                                localAgeMonths = PatientDataManager.ageMonths.toString()
                                localWeight = PatientDataManager.weightKg?.toString() ?: ""
                                localGender = PatientDataManager.gender
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Säugling")
                        }

                        // Kind
                        OutlinedButton(
                            onClick = {
                                PatientDataManager.setChild()
                                localAgeYears = PatientDataManager.ageYears.toString()
                                localAgeMonths = PatientDataManager.ageMonths.toString()
                                localWeight = PatientDataManager.weightKg?.toString() ?: ""
                                localGender = PatientDataManager.gender
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kind")
                        }

                        // Erwachsener
                        OutlinedButton(
                            onClick = {
                                PatientDataManager.setAdult()
                                localAgeYears = PatientDataManager.ageYears.toString()
                                localAgeMonths = PatientDataManager.ageMonths.toString()
                                localWeight = PatientDataManager.weightKg?.toString() ?: ""
                                localGender = PatientDataManager.gender
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Erwachsener")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Alter Eingabe
                    Text(
                        text = "Alter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Jahre
                        OutlinedTextField(
                            value = localAgeYears,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.all { char -> char.isDigit() }) {
                                    localAgeYears = value
                                }
                            },
                            label = { Text("Jahre") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Monate
                        OutlinedTextField(
                            value = localAgeMonths,
                            onValueChange = { value ->
                                if (value.isEmpty() || (value.all { char -> char.isDigit() } && (value.toIntOrNull() ?: 0) <= 11)) {
                                    localAgeMonths = value
                                }
                            },
                            label = { Text("Monate (0-11)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Gewicht Eingabe
                    Text(
                        text = "Gewicht",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = localWeight,
                        onValueChange = { value ->
                            if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                                localWeight = value
                            }
                        },
                        label = { Text("Gewicht in kg (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = {
                            if (localWeight.isEmpty()) {
                                Text(
                                    text = "Automatische Schätzung: ${String.format("%.1f", PatientDataManager.estimatedWeightKg)} kg",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Geschlecht Auswahl
                    Text(
                        text = "Geschlecht",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { localGender = PatientGender.MALE },
                            label = { Text("Männlich") },
                            selected = localGender == PatientGender.MALE,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            onClick = { localGender = PatientGender.FEMALE },
                            label = { Text("Weiblich") },
                            selected = localGender == PatientGender.FEMALE,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            onClick = { localGender = PatientGender.UNKNOWN },
                            label = { Text("Unbekannt") },
                            selected = localGender == PatientGender.UNKNOWN,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Zusammenfassung
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Aktuelle Einstellungen",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = PatientDataManager.getPatientSummary(),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Klassifikation: ${PatientDataManager.getAgeClassification()}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                PatientDataManager.resetToDefaults()
                                localAgeYears = PatientDataManager.ageYears.toString()
                                localAgeMonths = PatientDataManager.ageMonths.toString()
                                localWeight = PatientDataManager.weightKg?.toString() ?: ""
                                localGender = PatientDataManager.gender
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Zurücksetzen")
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Fertig")
                        }
                    }
                }
            }
        }
    }
}