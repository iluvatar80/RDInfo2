// app/src/main/java/com/rdinfo2/ui/screens/MedicationCalculatorScreen.kt
package com.rdinfo2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.data.model.SimpleMedication
import com.rdinfo2.data.model.SimpleDoseCalculation
import com.rdinfo2.logic.DosingCalculator

/**
 * WORKING: MedicationCalculatorScreen ohne fehlende Referenzen
 * Verwendet nur existierende Komponenten
 */
@Composable
fun MedicationCalculatorScreen(
    modifier: Modifier = Modifier
) {
    var selectedMedication by remember { mutableStateOf<SimpleMedication?>(null) }
    var calculation by remember { mutableStateOf<SimpleDoseCalculation?>(null) }

    // Alle verfügbaren Medikamente
    val medications = remember { DosingCalculator.getAllMedications() }

    // Automatische Neuberechnung wenn sich Patientendaten ändern
    LaunchedEffect(
        PatientDataManager.ageYears,
        PatientDataManager.ageMonths,
        PatientDataManager.estimatedWeightKg,
        selectedMedication
    ) {
        selectedMedication?.let { medication ->
            calculation = DosingCalculator.calculateDose(
                medication = medication,
                weightKg = PatientDataManager.estimatedWeightKg,
                ageMonths = PatientDataManager.totalAgeInMonths
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Medikamentenrechner",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Patienteninfo-Card
            item {
                PatientInfoCard()
            }

            // Medikamentenauswahl
            item {
                MedicationSelectionCard(
                    medications = medications,
                    selectedMedication = selectedMedication,
                    onMedicationSelected = { medication ->
                        selectedMedication = medication
                    }
                )
            }

            // Berechnungsergebnis
            calculation?.let { calc ->
                item {
                    CalculationResultCard(calculation = calc)
                }
            }

            // Hinweis wenn kein Medikament ausgewählt
            if (selectedMedication == null) {
                item {
                    HintCard()
                }
            }
        }
    }
}

@Composable
private fun PatientInfoCard() {
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Patient",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = PatientDataManager.getPatientSummary(),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = PatientDataManager.getAgeClassification(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MedicationSelectionCard(
    medications: List<SimpleMedication>,
    selectedMedication: SimpleMedication?,
    onMedicationSelected: (SimpleMedication) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalPharmacy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Medikament auswählen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            medications.forEach { medication ->
                MedicationItem(
                    medication = medication,
                    isSelected = selectedMedication?.id == medication.id,
                    onClick = { onMedicationSelected(medication) }
                )
            }
        }
    }
}

@Composable
private fun MedicationItem(
    medication: SimpleMedication,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = medication.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            if (medication.indications.isNotEmpty()) {
                Text(
                    text = medication.indications.joinToString(", "),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CalculationResultCard(
    calculation: SimpleDoseCalculation
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (calculation.isValid) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (calculation.isValid) "Dosierung" else "Fehler",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (calculation.isValid) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (calculation.isValid) {
                // Dosierung
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Dosis:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%.2f", calculation.dose)} ${calculation.unit}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Volumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Volumen:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%.1f", calculation.volume)} ml",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Warnungen
                if (calculation.warnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    calculation.warnings.forEach { warning ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warning,
                                fontSize = 14.sp,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }

                // Berechnung anzeigen
                if (calculation.calculation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = calculation.calculation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            } else {
                // Fehlermeldung
                Text(
                    text = calculation.errorMessage ?: "Unbekannter Fehler",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun HintCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💡 Hinweis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Wählen Sie ein Medikament aus der Liste aus, um die Dosierung zu berechnen.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Die Berechnung erfolgt automatisch basierend auf den aktuellen Patientendaten.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}