// app/src/main/java/com/rdinfo2/ui/screens/MedicationCalculatorScreen.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.rdinfo2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.data.patient.PatientData
import com.rdinfo2.logic.PatientCalculator
import com.rdinfo2.logic.VitalParameters

/**
 * SIMPLIFIED: MedicationCalculatorScreen die nur existierende Datenmodelle verwendet
 * Funktioniert mit PatientDataManager, PatientData und PatientCalculator
 */
@Composable
fun MedicationCalculatorScreen(
    modifier: Modifier = Modifier
) {
    // Aktuelle Patientendaten
    val currentPatient = PatientData(
        ageYears = PatientDataManager.ageYears,
        ageMonths = PatientDataManager.ageMonths,
        weightKg = PatientDataManager.estimatedWeightKg,
        gender = when (PatientDataManager.gender) {
            com.rdinfo2.data.patient.PatientGender.MALE -> com.rdinfo2.data.patient.PatientGender.MALE
            com.rdinfo2.data.patient.PatientGender.FEMALE -> com.rdinfo2.data.patient.PatientGender.FEMALE
            com.rdinfo2.data.patient.PatientGender.UNKNOWN -> com.rdinfo2.data.patient.PatientGender.UNKNOWN
        }
    )

    // Berechnete Vitalparameter
    val vitalParams = remember(currentPatient.ageYears, currentPatient.ageMonths, currentPatient.weightKg) {
        PatientCalculator.calculateVitalParameters(
            ageYears = currentPatient.ageYears,
            ageMonths = currentPatient.ageMonths,
            weightKg = currentPatient.weightKg
        )
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
                PatientInfoCard(currentPatient)
            }

            // Vitalparameter-Card
            item {
                VitalParametersCard(vitalParams)
            }

            // Gewichtsschätzung-Card
            item {
                WeightEstimationCard(currentPatient)
            }

            // Medikamenten-Platzhalter
            item {
                MedicationPlaceholderCard()
            }
        }
    }
}

@Composable
private fun PatientInfoCard(patient: PatientData) {
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

            val ageText = if (patient.ageMonths > 0) {
                "${patient.ageYears} Jahre, ${patient.ageMonths} Monate"
            } else {
                "${patient.ageYears} Jahre"
            }

            Text(
                text = "$ageText, ${String.format("%.1f", patient.weightKg)} kg",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = PatientCalculator.getAgeCategory(patient.ageYears, patient.ageMonths),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun VitalParametersCard(vitalParams: VitalParameters) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Vitalparameter (Normalwerte)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            VitalParameterRow(
                label = "Herzfrequenz:",
                value = "${vitalParams.heartRateMin}-${vitalParams.heartRateMax}",
                unit = "bpm"
            )

            VitalParameterRow(
                label = "Atemfrequenz:",
                value = "${vitalParams.respiratoryRateMin}-${vitalParams.respiratoryRateMax}",
                unit = "/min"
            )

            VitalParameterRow(
                label = "Systol. RR:",
                value = "${vitalParams.systolicBPMin}-${vitalParams.systolicBPMax}",
                unit = "mmHg"
            )

            vitalParams.tidalVolume?.let { tidalVolume ->
                VitalParameterRow(
                    label = "Atemzugvolumen:",
                    value = String.format("%.1f", tidalVolume),
                    unit = "ml/kg"
                )
            }

            VitalParameterRow(
                label = "Flüssigkeitsbedarf:",
                value = String.format("%.0f", vitalParams.fluidRequirement),
                unit = "ml/kg/Tag"
            )
        }
    }
}

@Composable
private fun VitalParameterRow(
    label: String,
    value: String,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "$value $unit",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun WeightEstimationCard(patient: PatientData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gewichtsschätzung",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            val estimatedWeight = PatientCalculator.estimateWeight(patient.ageYears, patient.ageMonths)
            val formula = when {
                patient.ageYears == 0 -> "3 kg + (Monate × 0.7 kg)"
                patient.ageYears in 1..5 -> "(Alter × 2) + 8 kg"
                patient.ageYears in 6..12 -> "(Alter × 3) + 7 kg"
                patient.ageYears in 13..17 -> "50 + (Alter - 13) × 5 kg"
                else -> "Standard Erwachsenengewicht"
            }

            Text(
                text = "Geschätzt: ${String.format("%.1f", estimatedWeight)} kg",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Text(
                text = "Formel: $formula",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MedicationPlaceholderCard() {
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
            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Medikamentenberechnung",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hier wird der erweiterte Medikamentenrechner implementiert. Basierend auf der RDInfo-App Logik, aber mit Unterstützung für alle Einheiten.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "💡 Gesten-Navigation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• Von oben wischen: Patientendaten eingeben\n• Von links wischen: SAMPLER-Schema\n• Von rechts wischen: xABCDE-Schema",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}