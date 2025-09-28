// app/src/main/java/com/rdinfo2/ui/components/overlays/PatientDataOverlay.kt
package com.rdinfo2.ui.components.overlays

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.data.patient.PatientGender
import com.rdinfo2.data.patient.PatientData
import com.rdinfo2.data.patient.CalculatedPatientValues

/**
 * FIXED: Overlay für Patientendaten-Eingabe
 * Verwendet das neue PatientDataManager System ohne Enum-Konflikte
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDataOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Verwende das neue PatientDataManager System
    val currentPatient = PatientDataManager.currentPatient
    val calculatedValues = PatientDataManager.calculatedValues

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Patientendaten",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Schließen"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Patient data input form
            PatientDataForm(
                currentPatient = currentPatient,
                calculatedValues = calculatedValues,
                onUpdateAge = { years, months ->
                    PatientDataManager.updateAge(years, months)
                },
                onUpdateWeight = { weight, isManual ->
                    PatientDataManager.updateWeight(weight, isManual)
                },
                onUpdateGender = { gender ->
                    PatientDataManager.updateGender(gender)
                },
                onUpdatePregnancy = { isPregnant, week ->
                    PatientDataManager.updatePregnancy(isPregnant, week)
                }
            )
        }
    }
}

@Composable
fun PatientDataForm(
    currentPatient: PatientData,
    calculatedValues: CalculatedPatientValues,
    onUpdateAge: (years: Int, months: Int) -> Unit,
    onUpdateWeight: (weight: Double?, isManual: Boolean) -> Unit,
    onUpdateGender: (gender: PatientGender) -> Unit,
    onUpdatePregnancy: (isPregnant: Boolean, week: Int?) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Age Section
        AgeSection(
            currentYears = currentPatient.ageYears,
            currentMonths = currentPatient.ageMonths,
            onAgeChanged = onUpdateAge
        )

        // Weight Section
        WeightSection(
            currentWeight = currentPatient.weightKg,
            estimatedWeight = calculatedValues.estimatedWeight,
            isManualWeight = currentPatient.isManualWeight,
            onWeightChanged = onUpdateWeight
        )

        // Gender Section
        GenderSection(
            currentGender = currentPatient.gender,
            onGenderChanged = onUpdateGender
        )

        // Pregnancy Section (only for females)
        if (currentPatient.gender == PatientGender.FEMALE) {
            PregnancySection(
                isPregnant = currentPatient.isPregnant ?: false,
                gestationalWeek = currentPatient.weekOfPregnancy,
                onPregnancyChanged = onUpdatePregnancy
            )
        }

        // Summary Card
        PatientSummaryCard(
            patient = currentPatient,
            calculatedValues = calculatedValues
        )
    }
}

@Composable
fun AgeSection(
    currentYears: Int,
    currentMonths: Int,
    onAgeChanged: (years: Int, months: Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Alter",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Years slider
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Jahre: $currentYears",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = currentYears.toFloat(),
                        onValueChange = { value ->
                            onAgeChanged(value.toInt(), currentMonths)
                        },
                        valueRange = 0f..120f,
                        steps = 119
                    )
                }

                // Months slider
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Monate: $currentMonths",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = currentMonths.toFloat(),
                        onValueChange = { value ->
                            onAgeChanged(currentYears, value.toInt())
                        },
                        valueRange = 0f..11f,
                        steps = 10
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightSection(
    currentWeight: Double,
    estimatedWeight: Double,
    isManualWeight: Boolean,
    onWeightChanged: (weight: Double?, isManual: Boolean) -> Unit
) {
    var weightText by remember(currentWeight) {
        mutableStateOf(if (isManualWeight && currentWeight > 0) currentWeight.toString() else "")
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Gewicht",
                style = MaterialTheme.typography.titleMedium
            )

            // Estimated weight display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Geschätzt: ${String.format("%.1f", estimatedWeight)} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isManualWeight) {
                    TextButton(
                        onClick = {
                            onWeightChanged(null, false)
                            weightText = ""
                        }
                    ) {
                        Text("Schätzung verwenden")
                    }
                }
            }

            // Manual weight input
            OutlinedTextField(
                value = weightText,
                onValueChange = { newValue ->
                    weightText = newValue
                    val weight = newValue.toDoubleOrNull()
                    if (weight != null && weight > 0) {
                        onWeightChanged(weight, true)
                    } else if (newValue.isEmpty()) {
                        onWeightChanged(null, false)
                    }
                },
                label = { Text("Manuelles Gewicht (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("${String.format("%.1f", estimatedWeight)}") }
            )

            // Effective weight display
            val effectiveWeight = if (isManualWeight && currentWeight > 0) currentWeight else estimatedWeight
            Text(
                text = "Verwendet: ${String.format("%.1f", effectiveWeight)} kg",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSection(
    currentGender: PatientGender,
    onGenderChanged: (PatientGender) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Geschlecht",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PatientGender.values().forEach { gender ->
                    FilterChip(
                        selected = currentGender == gender,
                        onClick = { onGenderChanged(gender) },
                        label = {
                            Text(when (gender) {
                                PatientGender.MALE -> "Männlich"
                                PatientGender.FEMALE -> "Weiblich"
                                PatientGender.UNKNOWN -> "Unbekannt"
                            })
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancySection(
    isPregnant: Boolean,
    gestationalWeek: Int?,
    onPregnancyChanged: (isPregnant: Boolean, week: Int?) -> Unit
) {
    var weekText by remember(gestationalWeek) {
        mutableStateOf(gestationalWeek?.toString() ?: "")
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schwangerschaft",
                    style = MaterialTheme.typography.titleMedium
                )

                Switch(
                    checked = isPregnant,
                    onCheckedChange = { checked ->
                        onPregnancyChanged(checked, if (checked) gestationalWeek else null)
                    }
                )
            }

            if (isPregnant) {
                OutlinedTextField(
                    value = weekText,
                    onValueChange = { newValue ->
                        weekText = newValue
                        val week = newValue.toIntOrNull()
                        if (week != null && week in 1..42) {
                            onPregnancyChanged(true, week)
                        }
                    },
                    label = { Text("Schwangerschaftswoche") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("z.B. 20") }
                )
            }
        }
    }
}

@Composable
fun PatientSummaryCard(
    patient: PatientData,
    calculatedValues: CalculatedPatientValues
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
                text = "Zusammenfassung",
                style = MaterialTheme.typography.titleMedium
            )

            val ageText = when {
                patient.ageYears == 0 -> "${calculatedValues.totalAgeMonths} Monate"
                patient.ageMonths == 0 -> "${patient.ageYears} Jahre"
                else -> "${patient.ageYears} Jahre, ${patient.ageMonths} Monate"
            }

            val genderText = when (patient.gender) {
                PatientGender.MALE -> "männlich"
                PatientGender.FEMALE -> if (patient.isPregnant == true) {
                    "weiblich, schwanger (${patient.weekOfPregnancy ?: "?"} SSW)"
                } else "weiblich"
                PatientGender.UNKNOWN -> "unbekannt"
            }

            val categoryText = when {
                calculatedValues.isInfant -> "Säugling"
                calculatedValues.isChild -> "Kind"
                calculatedValues.isAdolescent -> "Jugendlicher"
                calculatedValues.isGeriatric -> "Geriatrisch"
                else -> "Erwachsener"
            }

            Text(
                text = "$categoryText, $ageText, $genderText",
                style = MaterialTheme.typography.bodyLarge
            )

            val weightSuffix = if (patient.isManualWeight) " (manuell)" else " (geschätzt)"
            Text(
                text = "Gewicht: ${String.format("%.1f", calculatedValues.effectiveWeight)} kg$weightSuffix",
                style = MaterialTheme.typography.bodyMedium
            )

            if (calculatedValues.riskFactors.isNotEmpty()) {
                Text(
                    text = "Risikofaktoren: ${calculatedValues.riskFactors.joinToString(", ") { it.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}