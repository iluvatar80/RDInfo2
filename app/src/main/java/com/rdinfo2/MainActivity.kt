// app/src/main/java/com/rdinfo2/MainActivity.kt
package com.rdinfo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rdinfo2.logic.DosingCalculator
import com.rdinfo2.logic.PatientCalculator
import com.rdinfo2.ui.theme.RDInfo2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RDInfo2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Bar
        TabRow(selectedTabIndex = selectedTab) {
            val tabs = listOf("Medikamente", "xABCDE", "Normalwerte", "Nachschlagen")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Content based on selected tab
        when (selectedTab) {
            0 -> MedicationScreen()
            1 -> AlgorithmScreen()
            2 -> ReferenceScreen()
            3 -> SpecialScreen()
        }
    }
}

@Composable
fun MedicationScreen() {
    var ageYears by remember { mutableStateOf("") }
    var ageMonths by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var useEstimatedWeight by remember { mutableStateOf(true) }
    var selectedGender by remember { mutableStateOf(0) }
    var selectedMedication by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }

    val estimatedWeight = if (ageYears.isNotBlank()) {
        PatientCalculator.estimateWeight(
            ageYears.toIntOrNull() ?: 0,
            ageMonths.toIntOrNull() ?: 0
        )
    } else 0.0

    val effectiveWeight = if (useEstimatedWeight) estimatedWeight else (weight.toDoubleOrNull() ?: 0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Patientendaten",
                    style = MaterialTheme.typography.titleLarge
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageYears,
                        onValueChange = {
                            ageYears = it
                            showResult = false
                        },
                        label = { Text("Jahre") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ageMonths,
                        onValueChange = {
                            ageMonths = it
                            showResult = false
                        },
                        label = { Text("Monate") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (estimatedWeight > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Geschätztes Gewicht: ${String.format("%.1f", estimatedWeight)} kg",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { useEstimatedWeight = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (useEstimatedWeight)
                                            MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Text("Schätzung verwenden")
                                }

                                OutlinedButton(
                                    onClick = { useEstimatedWeight = false },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (!useEstimatedWeight)
                                            MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Text("Manuell eingeben")
                                }
                            }
                        }
                    }
                }

                if (!useEstimatedWeight) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = {
                            weight = it
                            showResult = false
                        },
                        label = { Text("Gewicht (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Geschlecht:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val genders = listOf("Männlich", "Weiblich", "Unbekannt")
                    genders.forEachIndexed { index, gender ->
                        OutlinedButton(
                            onClick = { selectedGender = index },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedGender == index)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(gender)
                        }
                    }
                }
            }
        }

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Medikament auswählen",
                    style = MaterialTheme.typography.titleLarge
                )

                val medications = listOf("Paracetamol", "Ibuprofen", "Midazolam", "Adrenalin")
                medications.forEachIndexed { index, medication ->
                    OutlinedButton(
                        onClick = { selectedMedication = index },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedMedication == index)
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(medication)
                    }
                }
            }
        }

        if (ageYears.isNotBlank() && effectiveWeight > 0) {
            Button(
                onClick = { showResult = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dosierung berechnen")
            }

            if (showResult) {
                val medications = DosingCalculator.getSampleMedications()
                val result = DosingCalculator.calculateDosage(
                    medication = medications[selectedMedication],
                    weightKg = effectiveWeight,
                    ageYears = ageYears.toIntOrNull() ?: 0
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Dosierungsempfehlung",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = result.getDisplayText(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "⚠️ Achtung: Dies sind nur Richtwerte! Immer individuelle Dosierung und Kontraindikationen beachten.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Bitte Patientendaten eingeben",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun AlgorithmScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "xABCDE Algorithmen",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Hier werden die Flowcharts implementiert",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ReferenceScreen() {
    var ageYears by remember { mutableStateOf("") }
    var ageMonths by remember { mutableStateOf("") }
    var showCalculation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Patientenalter für Normalwerte",
                    style = MaterialTheme.typography.titleLarge
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageYears,
                        onValueChange = { ageYears = it },
                        label = { Text("Jahre") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ageMonths,
                        onValueChange = { ageMonths = it },
                        label = { Text("Monate") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = { showCalculation = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = ageYears.isNotBlank()
                ) {
                    Text("Normalwerte berechnen")
                }
            }
        }

        if (showCalculation && ageYears.isNotBlank()) {
            val years = ageYears.toIntOrNull() ?: 0
            val months = ageMonths.toIntOrNull() ?: 0
            val estimatedWeight = PatientCalculator.estimateWeight(years, months)
            val vitalParams = PatientCalculator.calculateVitalParameters(years, months, estimatedWeight)
            val ageCategory = PatientCalculator.getAgeCategory(years, months)

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
                        text = "Normalwerte für $ageCategory",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Geschätztes Gewicht: ${String.format("%.1f", estimatedWeight)} kg",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Vitalparameter
            VitalParametersCard(vitalParams, estimatedWeight)

            // Weitere physiologische Parameter
            PhysiologicalParametersCard(vitalParams, estimatedWeight)
        }

        // Glasgow Coma Scale (immer anzeigen)
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Glasgow Coma Scale (GCS)",
                    style = MaterialTheme.typography.titleLarge
                )
                GlasgowComaScale()
            }
        }
    }
}

@Composable
fun VitalParametersCard(vitalParams: VitalParameters, weightKg: Double) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Vitalparameter",
                style = MaterialTheme.typography.titleLarge
            )

            VitalParameterRow("Herzfrequenz", "${vitalParams.heartRateMin}-${vitalParams.heartRateMax}", "bpm")
            VitalParameterRow("RR systolisch", "${vitalParams.systolicBPMin}-${vitalParams.systolicBPMax}", "mmHg")
            VitalParameterRow("Atemfrequenz", "${vitalParams.respiratoryRateMin}-${vitalParams.respiratoryRateMax}", "/min")
            VitalParameterRow("SpO2", "≥ 95", "%")
            VitalParameterRow("Hämoglobin", "${String.format("%.1f", vitalParams.hemoglobinMin)}-${String.format("%.1f", vitalParams.hemoglobinMax)}", "g/dl")
        }
    }
}

@Composable
fun PhysiologicalParametersCard(vitalParams: VitalParameters, weightKg: Double) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Physiologische Parameter",
                style = MaterialTheme.typography.titleLarge
            )

            val tidalVolumeTotal = (vitalParams.tidalVolume * weightKg).toInt()
            val bloodVolumeTotal = (vitalParams.bloodVolume * weightKg).toInt()
            val fluidTotal = (vitalParams.fluidRequirement * weightKg).toInt()
            val calorieTotal = (vitalParams.calorieRequirement * weightKg).toInt()

            VitalParameterRow("Atemzugvolumen", "$tidalVolumeTotal", "ml")
            VitalParameterRow("", "${String.format("%.0f", vitalParams.tidalVolume)} ml/kg", "")
            VitalParameterRow("Blutvolumen", "$bloodVolumeTotal", "ml")
            VitalParameterRow("", "${String.format("%.0f", vitalParams.bloodVolume)} ml/kg", "")
            VitalParameterRow("Flüssigkeitsbedarf", "$fluidTotal", "ml/Tag")
            VitalParameterRow("", "${String.format("%.0f", vitalParams.fluidRequirement)} ml/kg/Tag", "")
            VitalParameterRow("Kalorienbedarf", "$calorieTotal", "kcal/Tag")
            VitalParameterRow("", "${String.format("%.0f", vitalParams.calorieRequirement)} kcal/kg/Tag", "")
        }
    }
}

@Composable
fun VitalParameterRow(parameter: String, value: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = parameter,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$value $unit".trim(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun VitalParameterTable() {
    val vitalData = listOf(
        VitalRange("Säugling (0-1J)", "120-160", "30-60", "65-95", "≥95"),
        VitalRange("Kleinkind (1-3J)", "90-130", "20-40", "80-110", "≥95"),
        VitalRange("Schulkind (4-12J)", "70-120", "15-30", "90-120", "≥95"),
        VitalRange("Jugendlich (13-17J)", "60-100", "12-25", "100-130", "≥95"),
        VitalRange("Erwachsene", "60-100", "12-20", "100-140", "≥95"),
        VitalRange("Geriatrisch (>65J)", "60-90", "12-20", "110-160", "≥95")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Alter", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(2f))
            Text("Puls", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            Text("AF", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            Text("RR sys", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            Text("SpO2", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        }

        vitalData.forEach { vital ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(vital.ageGroup, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                    Text(vital.heartRate, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(vital.respRate, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(vital.bloodPressure, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(vital.oxygen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun GlasgowComaScale() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GCSSection("Augenöffnung (E)", listOf(
            "Spontan" to "4",
            "Auf Ansprache" to "3",
            "Auf Schmerzreiz" to "2",
            "Nicht" to "1"
        ))

        GCSSection("Verbale Reaktion (V)", listOf(
            "Orientiert" to "5",
            "Verwirrt" to "4",
            "Inadäquat" to "3",
            "Unverständlich" to "2",
            "Keine" to "1"
        ))

        GCSSection("Motorische Reaktion (M)", listOf(
            "Befolgt Aufforderungen" to "6",
            "Gezielte Abwehr" to "5",
            "Ungezielte Abwehr" to "4",
            "Beugesynergismen" to "3",
            "Strecksynergismen" to "2",
            "Keine Reaktion" to "1"
        ))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Bewertung:", style = MaterialTheme.typography.titleSmall)
                Text("15: Normal", style = MaterialTheme.typography.bodySmall)
                Text("13-14: Leichte Bewusstseinsstörung", style = MaterialTheme.typography.bodySmall)
                Text("9-12: Mittelschwere Bewusstseinsstörung", style = MaterialTheme.typography.bodySmall)
                Text("≤8: Schwere Bewusstseinsstörung", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun GCSSection(title: String, items: List<Pair<String, String>>) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        items.forEach { (description, score) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(description, style = MaterialTheme.typography.bodySmall)
                Text(score, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun LabValues() {
    val labData = listOf(
        "Blutzucker" to "70-110 mg/dl",
        "Hämoglobin ♂" to "14-18 g/dl",
        "Hämoglobin ♀" to "12-16 g/dl",
        "Leukozyten" to "4.0-10.0 Tsd/µl",
        "Thrombozyten" to "150-400 Tsd/µl",
        "CRP" to "<5 mg/l",
        "Troponin T" to "<14 ng/l"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        labData.forEach { (parameter, range) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(parameter, style = MaterialTheme.typography.bodyMedium)
                Text(range, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

data class VitalRange(
    val ageGroup: String,
    val heartRate: String,
    val respRate: String,
    val bloodPressure: String,
    val oxygen: String
)

@Composable
fun SpecialScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Nachschlagewerke",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "SEPSIS, ISOBAR, Toxidrome etc.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}