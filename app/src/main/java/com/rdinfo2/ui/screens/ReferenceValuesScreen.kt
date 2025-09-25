// app/src/main/java/com/rdinfo2/ui/screens/ReferenceValuesScreen.kt
package com.rdinfo2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.data.patient.PatientGender
import kotlin.math.*

@Composable
fun ReferenceValuesScreen() {
    val patientDataManager = PatientDataManager.getInstance()
    val currentPatient by patientDataManager.currentPatient.collectAsStateWithLifecycle()
    val calculatedValues by patientDataManager.calculatedValues.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Normalwerte",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            PatientInfoCard(
                currentPatient = currentPatient,
                calculatedValues = calculatedValues
            )
        }

        item {
            VitalParametersCard(
                patient = currentPatient,
                calculatedValues = calculatedValues
            )
        }

        item {
            GlasgowComaScaleCard()
        }

        item {
            PediatricSpecificCard(
                patient = currentPatient,
                calculatedValues = calculatedValues
            )
        }

        item {
            LaboratoryValuesCard(
                patient = currentPatient
            )
        }

        item {
            EmergencyDosesCard(
                patient = currentPatient,
                effectiveWeight = calculatedValues.effectiveWeight
            )
        }

        if (currentPatient.ageYears == 0 && currentPatient.ageMonths == 0) {
            item {
                PatientDataHintCard()
            }
        }
    }
}

@Composable
private fun PatientInfoCard(
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Patienteninformation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            val ageText = when {
                currentPatient.ageYears == 0 && currentPatient.ageMonths == 0 -> "Alter nicht angegeben"
                currentPatient.ageYears == 0 -> "${calculatedValues.totalAgeMonths} Monate alt"
                currentPatient.ageMonths == 0 -> "${currentPatient.ageYears} Jahre alt"
                else -> "${currentPatient.ageYears} Jahre, ${currentPatient.ageMonths} Monate alt"
            }

            val categoryText = when {
                calculatedValues.isInfant -> "Säugling (0-12 Monate)"
                calculatedValues.isChild -> "Kind (1-12 Jahre)"
                calculatedValues.isAdolescent -> "Jugendlicher (13-17 Jahre)"
                calculatedValues.isGeriatric -> "Geriatrischer Patient (>65 Jahre)"
                else -> "Erwachsener (18-65 Jahre)"
            }

            Text(text = ageText, style = MaterialTheme.typography.bodyLarge)
            Text(text = categoryText, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Gewicht: ${String.format("%.1f", calculatedValues.effectiveWeight)} kg ${if (currentPatient.isManualWeight) "(manuell)" else "(geschätzt)"}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (currentPatient.isPregnant) {
                Text(
                    text = "Schwanger (${currentPatient.gestationalWeek ?: "?"} SSW)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun VitalParametersCard(
    patient: com.rdinfo2.data.patient.PatientData,
    calculatedValues: com.rdinfo2.data.patient.CalculatedValues
) {
    val vitalRanges = calculateVitalParameterRanges(
        ageYears = patient.ageYears,
        ageMonths = patient.ageMonths,
        weight = calculatedValues.effectiveWeight,
        isInfant = calculatedValues.isInfant,
        isChild = calculatedValues.isChild,
        isGeriatric = calculatedValues.isGeriatric
    )

    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Vitalparameter-Normalwerte",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VitalParameterRow(
                    parameter = "Herzfrequenz",
                    range = "${vitalRanges.heartRateMin}-${vitalRanges.heartRateMax}",
                    unit = "bpm",
                    color = Color(0xFFE53E3E)
                )

                VitalParameterRow(
                    parameter = "Atemfrequenz",
                    range = "${vitalRanges.respiratoryRateMin}-${vitalRanges.respiratoryRateMax}",
                    unit = "/min",
                    color = Color(0xFF3182CE)
                )

                VitalParameterRow(
                    parameter = "Systolischer RR",
                    range = "${vitalRanges.systolicBPMin}-${vitalRanges.systolicBPMax}",
                    unit = "mmHg",
                    color = Color(0xFFD69E2E)
                )

                VitalParameterRow(
                    parameter = "Diastolischer RR",
                    range = "${vitalRanges.diastolicBPMin}-${vitalRanges.diastolicBPMax}",
                    unit = "mmHg",
                    color = Color(0xFF38A169)
                )

                VitalParameterRow(
                    parameter = "Sauerstoffsättigung",
                    range = "≥ 95",
                    unit = "%",
                    color = Color(0xFF805AD5)
                )

                VitalParameterRow(
                    parameter = "Körpertemperatur",
                    range = "36.0-37.5",
                    unit = "°C",
                    color = Color(0xFFE53E3E)
                )
            }
        }
    }
}

@Composable
private fun VitalParameterRow(
    parameter: String,
    range: String,
    unit: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = parameter,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$range $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun GlasgowComaScaleCard() {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Glasgow Coma Scale (GCS)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GCSSection(
                    title = "Augen öffnen (E)",
                    items = listOf(
                        "Spontan" to "4",
                        "Auf Ansprache" to "3",
                        "Auf Schmerzreiz" to "2",
                        "Nicht" to "1"
                    )
                )

                GCSSection(
                    title = "Verbale Reaktion (V)",
                    items = listOf(
                        "Orientiert" to "5",
                        "Verwirrt" to "4",
                        "Inadäquat" to "3",
                        "Unverständlich" to "2",
                        "Keine" to "1"
                    )
                )

                GCSSection(
                    title = "Motorische Reaktion (M)",
                    items = listOf(
                        "Befolgt Aufforderungen" to "6",
                        "Gezielte Abwehr" to "5",
                        "Ungezielte Abwehr" to "4",
                        "Beugesynergismen" to "3",
                        "Strecksynergismen" to "2",
                        "Keine Reaktion" to "1"
                    )
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Bewertung:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text("15: Normal", style = MaterialTheme.typography.bodySmall)
                    Text("13-14: Leichte Bewusstseinsstörung", style = MaterialTheme.typography.bodySmall)
                    Text("9-12: Mittelschwere Bewusstseinsstörung", style = MaterialTheme.typography.bodySmall)
                    Text("≤8: Schwere Bewusstseinsstörung", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun GCSSection(title: String, items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        items.forEach { (description, score) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = score,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PediatricSpecificCard(
    patient: com.rdinfo2.data.patient.PatientData,
    calculatedValues: com.rdinfo2.data.patient.CalculatedValues
) {
    if (!calculatedValues.isInfant && !calculatedValues.isChild) return

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pädiatrische Besonderheiten",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (calculatedValues.isInfant) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Säugling (0-12 Monate):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("• Großer Kopf, kurzer Hals", style = MaterialTheme.typography.bodySmall)
                    Text("• Atemwege sehr eng (4mm = massive Verlegung)", style = MaterialTheme.typography.bodySmall)
                    Text("• Obligate Nasenatmung bis 6 Monate", style = MaterialTheme.typography.bodySmall)
                    Text("• Hypothermierisiko hoch", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (calculatedValues.isChild) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Gewichtsschätzung nach Alter:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (patient.ageYears in 1..5) {
                        Text("Gewicht = (2 × Alter) + 8 kg", style = MaterialTheme.typography.bodySmall)
                    }
                    if (patient.ageYears in 6..12) {
                        Text("Gewicht = (3 × Alter) + 7 kg", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // APGAR Score for newborns
            if (calculatedValues.totalAgeMonths < 1) {
                Text(
                    text = "APGAR-Score (Neugeborene):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("A - Atmung, P - Puls, G - Grundtonus", style = MaterialTheme.typography.bodySmall)
                    Text("A - Aussehen, R - Reflexe", style = MaterialTheme.typography.bodySmall)
                    Text("Je 0-2 Punkte, max. 10 Punkte", style = MaterialTheme.typography.bodySmall)
                    Text("≥7: Normal, 4-6: Überwachung, <4: Reanimation", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun LaboratoryValuesCard(patient: com.rdinfo2.data.patient.PatientData) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Laborwerte (Normalbereich)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabValueRow("Blutzucker", "70-110", "mg/dl")
                LabValueRow("Hämoglobin ♂", "14-18", "g/dl")
                LabValueRow("Hämoglobin ♀", "12-16", "g/dl")
                LabValueRow("Hämatokrit ♂", "40-54", "%")
                LabValueRow("Hämatokrit ♀", "37-47", "%")
                LabValueRow("Leukozyten", "4.0-10.0", "Tsd/µl")
                LabValueRow("Thrombozyten", "150-400", "Tsd/µl")
                LabValueRow("CRP", "<5", "mg/l")
                LabValueRow("Troponin T", "<14", "ng/l")
                LabValueRow("D-Dimer", "<500", "µg/l")
            }

            if (patient.isPregnant) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Schwangerschaftsspezifische Werte:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Hb: 11-13 g/dl (Verdünnung)", style = MaterialTheme.typography.bodySmall)
                        Text("HCG: variiert je SSW", style = MaterialTheme.typography.bodySmall)
                        Text("RR tendenziell niedriger", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun LabValueRow(parameter: String, range: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = parameter,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$range $unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmergencyDosesCard(
    patient: com.rdinfo2.data.patient.PatientData,
    effectiveWeight: Double
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Notfall-Dosierungen (Schnellreferenz)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmergencyDoseRow("Adrenalin (Reanimation)", "${String.format("%.2f", effectiveWeight * 0.01)}", "mg")
                EmergencyDoseRow("Adrenalin (Anaphylaxie)", "${String.format("%.2f", effectiveWeight * 0.01)}", "mg i.m.")
                EmergencyDoseRow("Atropin", "${String.format("%.2f", maxOf(0.5, effectiveWeight * 0.02))}", "mg")
                EmergencyDoseRow("Midazolam", "${String.format("%.1f", effectiveWeight * 0.1)}", "mg")
                EmergencyDoseRow("Morphin", "${String.format("%.1f", effectiveWeight * 0.1)}", "mg")
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "⚠️ Achtung: Diese Werte sind nur Richtwerte! Immer individuelle Dosierung und Kontraindikationen beachten.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmergencyDoseRow(medication: String, dose: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = medication,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$dose $unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun PatientDataHintCard() {
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
                text = "💡 Genauere Normalwerte",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Geben Sie Patientendaten ein (Wischen von oben), um alters- und gewichtsspezifische Normalwerte zu erhalten.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Data class for vital parameter ranges
data class VitalParameterRanges(
    val heartRateMin: Int,
    val heartRateMax: Int,
    val respiratoryRateMin: Int,
    val respiratoryRateMax: Int,
    val systolicBPMin: Int,
    val systolicBPMax: Int,
    val diastolicBPMin: Int,
    val diastolicBPMax: Int
)

private fun calculateVitalParameterRanges(
    ageYears: Int,
    ageMonths: Int,
    weight: Double,
    isInfant: Boolean,
    isChild: Boolean,
    isGeriatric: Boolean
): VitalParameterRanges {
    val totalMonths = ageYears * 12 + ageMonths

    return when {
        // Neugeborene (0-1 Monat)
        totalMonths <= 1 -> VitalParameterRanges(
            heartRateMin = 120, heartRateMax = 160,
            respiratoryRateMin = 30, respiratoryRateMax = 60,
            systolicBPMin = 65, systolicBPMax = 95,
            diastolicBPMin = 30, diastolicBPMax = 60
        )
        // Säuglinge (1-12 Monate)
        isInfant -> VitalParameterRanges(
            heartRateMin = 100, heartRateMax = 150,
            respiratoryRateMin = 25, respiratoryRateMax = 50,
            systolicBPMin = 70, systolicBPMax = 100,
            diastolicBPMin = 50, diastolicBPMax = 70
        )
        // Kleinkinder (1-3 Jahre)
        ageYears in 1..3 -> VitalParameterRanges(
            heartRateMin = 90, heartRateMax = 130,
            respiratoryRateMin = 20, respiratoryRateMax = 40,
            systolicBPMin = 80, systolicBPMax = 110,
            diastolicBPMin = 50, diastolicBPMax = 80
        )
        // Schulkinder (4-12 Jahre)
        isChild -> VitalParameterRanges(
            heartRateMin = 70, heartRateMax = 120,
            respiratoryRateMin = 15, respiratoryRateMax = 30,
            systolicBPMin = 90, systolicBPMax = 120,
            diastolicBPMin = 60, diastolicBPMax = 80
        )
        // Jugendliche (13-17 Jahre)
        ageYears in 13..17 -> VitalParameterRanges(
            heartRateMin = 60, heartRateMax = 100,
            respiratoryRateMin = 12, respiratoryRateMax = 25,
            systolicBPMin = 100, systolicBPMax = 130,
            diastolicBPMin = 65, diastolicBPMax = 85
        )
        // Geriatrische Patienten (>65 Jahre)
        isGeriatric -> VitalParameterRanges(
            heartRateMin = 60, heartRateMax = 90,
            respiratoryRateMin = 12, respiratoryRateMax = 20,
            systolicBPMin = 110, systolicBPMax = 160,
            diastolicBPMin = 70, diastolicBPMax = 90
        )
        // Erwachsene (18-65 Jahre)
        else -> VitalParameterRanges(
            heartRateMin = 60, heartRateMax = 100,
            respiratoryRateMin = 12, respiratoryRateMax = 20,
            systolicBPMin = 100, systolicBPMax = 140,
            diastolicBPMin = 60, diastolicBPMax = 90
        )
    }
}