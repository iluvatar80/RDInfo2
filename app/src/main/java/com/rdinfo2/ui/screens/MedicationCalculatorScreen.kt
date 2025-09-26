// app/src/main/java/com/rdinfo2/ui/screens/MedicationCalculatorScreen.kt
package com.rdinfo2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.data.patient.PatientGender
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationCalculatorScreen() {
    val patientManager = remember { PatientDataManager.getInstance() }
    val currentPatient by patientManager.currentPatient.collectAsStateWithLifecycle()

    // UI State für Eingaben
    var selectedMedication by remember { mutableStateOf("Adrenalin") }
    var selectedIndication by remember { mutableStateOf("Reanimation") }
    var showPatientEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Patientendaten-Card
        PatientDataCard(
            patient = currentPatient,
            onEditClick = { showPatientEdit = true }
        )

        // Medikamenten-Auswahl
        MedicationSelectionCard(
            selectedMedication = selectedMedication,
            selectedIndication = selectedIndication,
            onMedicationChange = { selectedMedication = it },
            onIndicationChange = { selectedIndication = it }
        )

        // Berechnungsergebnis
        CalculationResultCard(
            medication = selectedMedication,
            indication = selectedIndication,
            patient = currentPatient
        )
    }

    // Patient-Edit Dialog
    if (showPatientEdit) {
        PatientEditDialog(
            patient = currentPatient,
            onDismiss = { showPatientEdit = false },
            onSave = { updatedPatient ->
                patientManager.updatePatient(updatedPatient)
                showPatientEdit = false
            }
        )
    }
}

@Composable
fun PatientDataCard(
    patient: com.rdinfo2.data.patient.PatientData,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Patientendaten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Alter: ${patient.getFormattedAge()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Gewicht: ${patient.getFormattedWeight()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text(
                        text = "Kategorie: ${patient.ageCategory}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Geschlecht: ${patient.gender}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationSelectionCard(
    selectedMedication: String,
    selectedIndication: String,
    onMedicationChange: (String) -> Unit,
    onIndicationChange: (String) -> Unit
) {
    // Vereinfachte Medikamenten-Liste für Prototyp
    val medications = listOf(
        "Adrenalin",
        "Atropin",
        "Amiodaron",
        "Glucose 40%",
        "Furosemid"
    )

    val indications = mapOf(
        "Adrenalin" to listOf("Reanimation", "Anaphylaxie", "Asthma"),
        "Atropin" to listOf("Bradykardie", "Vergiftung"),
        "Amiodaron" to listOf("VT", "VF", "Vorhofflimmern"),
        "Glucose 40%" to listOf("Hypoglykämie"),
        "Furosemid" to listOf("Lungenödem", "Herzinsuffizienz")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Medikament & Indikation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Medikamenten-Dropdown
            var medicationExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = medicationExpanded,
                onExpandedChange = { medicationExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedMedication,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Medikament") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = medicationExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = medicationExpanded,
                    onDismissRequest = { medicationExpanded = false }
                ) {
                    medications.forEach { medication ->
                        DropdownMenuItem(
                            text = { Text(medication) },
                            onClick = {
                                onMedicationChange(medication)
                                // Erste verfügbare Indikation auswählen
                                indications[medication]?.firstOrNull()?.let { firstIndication ->
                                    onIndicationChange(firstIndication)
                                }
                                medicationExpanded = false
                            }
                        )
                    }
                }
            }

            // Indikations-Dropdown
            var indicationExpanded by remember { mutableStateOf(false) }
            val availableIndications = indications[selectedMedication] ?: emptyList()

            ExposedDropdownMenuBox(
                expanded = indicationExpanded,
                onExpandedChange = { indicationExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedIndication,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Indikation") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = indicationExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = indicationExpanded,
                    onDismissRequest = { indicationExpanded = false }
                ) {
                    availableIndications.forEach { indication ->
                        DropdownMenuItem(
                            text = { Text(indication) },
                            onClick = {
                                onIndicationChange(indication)
                                indicationExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculationResultCard(
    medication: String,
    indication: String,
    patient: com.rdinfo2.data.patient.PatientData
) {
    // Vereinfachte Dosierungsberechnung für Prototyp
    val result = calculateSimpleDosage(medication, indication, patient)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isSuccess) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (result.isSuccess) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Text(
                    text = "Dosierungsberechnung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (result.isSuccess) {
                Text(
                    text = "Dosis: ${result.dosage}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                result.volume?.let { volume ->
                    Text(
                        text = "Volumen: $volume",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                result.preparation?.let { prep ->
                    Text(
                        text = "Zubereitung: $prep",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                result.warnings.forEach { warning ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = result.errorMessage ?: "Berechnung fehlgeschlagen",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientEditDialog(
    patient: com.rdinfo2.data.patient.PatientData,
    onDismiss: () -> Unit,
    onSave: (com.rdinfo2.data.patient.PatientData) -> Unit
) {
    var ageYears by remember { mutableStateOf(patient.ageYears.toString()) }
    var ageMonths by remember { mutableStateOf(patient.ageMonths.toString()) }
    var weight by remember { mutableStateOf(if (patient.isManualWeight) patient.weightKg.toString() else "") }
    var selectedGender by remember { mutableStateOf(patient.gender) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Patientendaten bearbeiten") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageYears,
                        onValueChange = { ageYears = it },
                        label = { Text("Jahre") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ageMonths,
                        onValueChange = { ageMonths = it },
                        label = { Text("Monate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Gewicht (kg, optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Leer lassen für automatische Schätzung") }
                )

                // Gender Selection
                Text("Geschlecht:", style = MaterialTheme.typography.bodyMedium)
                Row {
                    PatientGender.values().forEach { gender ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedGender == gender,
                                onClick = { selectedGender = gender }
                            )
                            Text(gender.name)
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val years = ageYears.toIntOrNull() ?: 0
                        val months = ageMonths.toIntOrNull() ?: 0
                        val weightValue = weight.toDoubleOrNull()

                        val updatedPatient = if (weightValue != null && weightValue > 0) {
                            com.rdinfo2.data.patient.PatientDataFactory.createWithWeight(
                                years, months, weightValue, selectedGender
                            )
                        } else {
                            com.rdinfo2.data.patient.PatientDataFactory.create(
                                years, months, selectedGender
                            )
                        }

                        onSave(updatedPatient)
                    } catch (e: Exception) {
                        // Handle error - in real app, show error message
                    }
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

// Vereinfachte Dosierungsberechnung für Prototyp
data class SimpleDosageResult(
    val isSuccess: Boolean,
    val dosage: String? = null,
    val volume: String? = null,
    val preparation: String? = null,
    val warnings: List<String> = emptyList(),
    val errorMessage: String? = null
)

fun calculateSimpleDosage(
    medication: String,
    indication: String,
    patient: com.rdinfo2.data.patient.PatientData
): SimpleDosageResult {
    val weight = patient.getEffectiveWeight()
    val ageYears = patient.ageYears

    return when (medication) {
        "Adrenalin" -> when (indication) {
            "Reanimation" -> {
                val dose = when {
                    ageYears < 1 -> 0.01 * weight
                    ageYears < 12 -> 0.01 * weight
                    else -> 1.0
                }
                SimpleDosageResult(
                    isSuccess = true,
                    dosage = "${String.format("%.2f", dose)} mg",
                    volume = "${String.format("%.1f", dose)} ml (1:1000)",
                    preparation = "Unverdünnt i.v./i.o.",
                    warnings = if (dose > 5.0) listOf("Hohe Dosis - Kontrolle empfohlen") else emptyList()
                )
            }
            "Anaphylaxie" -> {
                val dose = when {
                    ageYears < 6 -> 0.15
                    ageYears < 12 -> 0.3
                    else -> 0.5
                }
                SimpleDosageResult(
                    isSuccess = true,
                    dosage = "${String.format("%.2f", dose)} mg",
                    volume = "${String.format("%.2f", dose)} ml (1:1000)",
                    preparation = "i.m. (Oberschenkel)"
                )
            }
            else -> SimpleDosageResult(false, errorMessage = "Indikation nicht unterstützt")
        }

        "Atropin" -> when (indication) {
            "Bradykardie" -> {
                val dose = when {
                    ageYears < 12 -> 0.02 * weight
                    else -> 0.5
                }.coerceAtLeast(0.1).coerceAtMost(1.0)

                SimpleDosageResult(
                    isSuccess = true,
                    dosage = "${String.format("%.2f", dose)} mg",
                    volume = "${String.format("%.1f", dose)} ml",
                    preparation = "i.v. langsam"
                )
            }
            else -> SimpleDosageResult(false, errorMessage = "Indikation nicht unterstützt")
        }

        "Glucose 40%" -> when (indication) {
            "Hypoglykämie" -> {
                val dose = when {
                    ageYears < 1 -> 2.0 * weight
                    ageYears < 12 -> 1.0 * weight
                    else -> 50.0
                }
                SimpleDosageResult(
                    isSuccess = true,
                    dosage = "${String.format("%.0f", dose)} ml",
                    volume = "${String.format("%.0f", dose)} ml Glucose 40%",
                    preparation = "i.v. über großlumigen Zugang",
                    warnings = listOf("Paravasation vermeiden!")
                )
            }
            else -> SimpleDosageResult(false, errorMessage = "Indikation nicht unterstützt")
        }

        else -> SimpleDosageResult(
            false,
            errorMessage = "Medikament noch nicht implementiert"
        )
    }
}