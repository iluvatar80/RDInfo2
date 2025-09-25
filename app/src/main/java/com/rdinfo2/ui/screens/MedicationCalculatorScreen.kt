// app/src/main/java/com/rdinfo2/ui/screens/MedicationCalculatorScreen.kt
package com.rdinfo2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rdinfo2.data.model.Medication
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.logic.DosingCalculator
import com.rdinfo2.logic.DosingResult

@Composable
fun MedicationCalculatorScreen() {
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var selectedIndication by remember { mutableStateOf("") }
    var selectedRoute by remember { mutableStateOf<String?>(null) }
    var dosingResult by remember { mutableStateOf<DosingResult?>(null) }
    var showMedicationSelector by remember { mutableStateOf(false) }

    val patientDataManager = PatientDataManager.getInstance()
    val currentPatient by patientDataManager.currentPatient.collectAsStateWithLifecycle()
    val calculatedValues by patientDataManager.calculatedValues.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Patient Summary Card
        PatientSummaryCard(
            currentPatient = currentPatient,
            calculatedValues = calculatedValues
        )

        // Medication Selection
        MedicationSelectionCard(
            selectedMedication = selectedMedication,
            onMedicationSelected = {
                selectedMedication = it
                dosingResult = null // Clear previous results
            },
            onShowSelector = { showMedicationSelector = true }
        )

        // Indication and Route Selection
        if (selectedMedication != null) {
            IndicationRouteCard(
                medication = selectedMedication!!,
                selectedIndication = selectedIndication,
                selectedRoute = selectedRoute,
                onIndicationChanged = {
                    selectedIndication = it
                    dosingResult = null
                },
                onRouteChanged = {
                    selectedRoute = it
                    dosingResult = null
                }
            )

            // Calculate Button
            CalculateButton(
                enabled = selectedMedication != null && selectedIndication.isNotBlank(),
                onCalculate = {
                    val calculator = DosingCalculator.getInstance()
                    dosingResult = calculator.calculateDosage(
                        medication = selectedMedication!!,
                        indication = selectedIndication,
                        route = selectedRoute,
                        patientData = currentPatient
                    )
                }
            )

            // Results Display
            dosingResult?.let { result ->
                DosingResultCard(result = result)
            }
        }

        // Gesture hint for patient data
        if (currentPatient.ageYears == 0 && currentPatient.ageMonths == 0) {
            GestureHintCard()
        }
    }

    // Medication Selector Dialog
    if (showMedicationSelector) {
        MedicationSelectorDialog(
            onMedicationSelected = { medication ->
                selectedMedication = medication
                showMedicationSelector = false
                dosingResult = null
            },
            onDismiss = { showMedicationSelector = false }
        )
    }
}

@Composable
private fun PatientSummaryCard(
    currentPatient: com.rdinfo2.data.patient.PatientData,
    calculatedValues: com.rdinfo2.data.patient.CalculatedValues
) {
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
                text = "Aktuelle Patientendaten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val ageText = when {
                currentPatient.ageYears == 0 && currentPatient.ageMonths == 0 -> "Alter nicht eingegeben"
                currentPatient.ageYears == 0 -> "${calculatedValues.totalAgeMonths} Monate"
                currentPatient.ageMonths == 0 -> "${currentPatient.ageYears} Jahre"
                else -> "${currentPatient.ageYears} Jahre, ${currentPatient.ageMonths} Monate"
            }

            val weightText = if (calculatedValues.effectiveWeight > 0) {
                "${String.format("%.1f", calculatedValues.effectiveWeight)} kg ${if (currentPatient.isManualWeight) "(manuell)" else "(geschätzt)"}"
            } else {
                "Gewicht wird geschätzt"
            }

            Text(text = ageText, style = MaterialTheme.typography.bodyMedium)
            Text(text = weightText, style = MaterialTheme.typography.bodyMedium)

            if (currentPatient.isPregnant) {
                Text(
                    text = "Schwanger (${currentPatient.gestationalWeek ?: "?") SSW",
                            style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                        )
                    }
            }
        }
    }

    @Composable
    private fun MedicationSelectionCard(
        selectedMedication: Medication?,
        onMedicationSelected: (Medication) -> Unit,
        onShowSelector: () -> Unit
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Medikament auswählen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = onShowSelector
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedMedication != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = selectedMedication.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (selectedMedication.brandNames.isNotEmpty()) {
                                        Text(
                                            text = selectedMedication.brandNames.joinToString(", "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Medikament wählen",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (selectedMedication != null) {
                        FilledTonalIconButton(
                            onClick = { onMedicationSelected(selectedMedication) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Auswahl löschen"
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun IndicationRouteCard(
        medication: Medication,
        selectedIndication: String,
        selectedRoute: String?,
        onIndicationChanged: (String) -> Unit,
        onRouteChanged: (String?) -> Unit
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Indikation & Applikationsweg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Available indications from dosing rules
                val availableIndications = medication.dosingRules.map { it.indication }.distinct()

                // Indication Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Indikation:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    availableIndications.forEach { indication ->
                        FilterChip(
                            selected = selectedIndication == indication,
                            onClick = { onIndicationChanged(indication) },
                            label = { Text(indication) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (availableIndications.isEmpty()) {
                        Text(
                            text = "Keine Indikationen verfügbar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Route Selection (if indication is selected)
                if (selectedIndication.isNotBlank()) {
                    val availableRoutes = medication.dosingRules
                        .filter { it.indication == selectedIndication }
                        .map { it.route.route }
                        .distinct()

                    if (availableRoutes.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Applikationsweg:",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                availableRoutes.forEach { route ->
                                    FilterChip(
                                        selected = selectedRoute == route,
                                        onClick = { onRouteChanged(route) },
                                        label = { Text(route) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CalculateButton(
        enabled: Boolean,
        onCalculate: () -> Unit
    ) {
        Button(
            onClick = onCalculate,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dosierung berechnen")
        }
    }

    @Composable
    private fun DosingResultCard(result: DosingResult) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (result.success) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (result.success) "Dosierungsberechnung" else "Fehler bei Berechnung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (result.success) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )

                if (result.success) {
                    // Successful calculation result
                    result.calculatedDose?.let { dose ->
                        Text(
                            text = "Dosierung: ${dose.displayText}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    result.volumeToAdminister?.let { volume ->
                        Text(
                            text = "Volumen: ${String.format("%.2f", volume)} ml",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    result.preparationInstructions?.let { instructions ->
                        Text(
                            text = "Zubereitung: $instructions",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    result.notes?.let { notes ->
                        Text(
                            text = "Hinweise: $notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    // Warnings
                    if (result.warnings.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            result.warnings.forEach { warning ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = warning,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Error case
                    result.errorMessage?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun GestureHintCard() {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Tipp: Patientendaten eingeben",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wischen Sie von oben nach unten, um Patientendaten zu erfassen. Dies ermöglicht genauere Dosierungsberechnungen.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    @Composable
    private fun MedicationSelectorDialog(
        onMedicationSelected: (Medication) -> Unit,
        onDismiss: () -> Unit
    ) {
        // Placeholder for now - could be populated with sample medications
        val sampleMedications = remember {
            listOf<Medication>() // Empty for now, will be populated from database
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Medikament auswählen") },
            text = {
                if (sampleMedications.isEmpty()) {
                    Text("Keine Medikamente verfügbar. Diese werden später aus der Datenbank geladen.")
                } else {
                    LazyColumn {
                        items(sampleMedications) { medication ->
                            TextButton(
                                onClick = { onMedicationSelected(medication) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = medication.name,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Schließen")
                }
            }
        )
    }