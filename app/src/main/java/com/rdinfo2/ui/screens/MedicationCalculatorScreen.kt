// app/src/main/java/com/rdinfo2/ui/screens/MedicationCalculatorScreen.kt
package com.rdinfo2.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.data.patient.PatientGender
import com.rdinfo2.data.json.JsonMedicationLoader
import com.rdinfo2.data.model.Medication as JsonMedication
import com.rdinfo2.data.model.Indication as JsonIndication

/**
 * Medikamentenrechner mit JSON-Datenquelle
 */

enum class InfoTab {
    INDICATION, CONTRAINDICATION, EFFECT, SIDE_EFFECT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationCalculatorScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Lade Medikamente aus JSON
    val medications = remember {
        loadMedicationsFromJson(context)
    }

    // State
    var selectedMedication by remember { mutableStateOf<JsonMedication?>(null) }
    var selectedIndication by remember { mutableStateOf<JsonIndication?>(null) }
    var selectedRoute by remember { mutableStateOf<String?>(null) }
    var customConcentration by remember { mutableStateOf("") }
    var selectedInfoTab by remember { mutableStateOf<InfoTab?>(null) }

    // Patient data
    val currentPatient = PatientDataManager.currentPatient
    val calculatedValues = PatientDataManager.calculatedValues

    // Berechnung der Standardkonzentration aus der JSON
    val standardConcentration = selectedMedication?.let { med ->
        // Extrahiere Konzentration aus dem preparation String
        extractConcentration(med.indications.firstOrNull()?.preparation ?: "")
    } ?: 1.0

    val effectiveConcentration = customConcentration.toDoubleOrNull()
        ?: standardConcentration

    // Dosisberechnung
    val calculatedDose = selectedIndication?.let { indication ->
        val ageGroup = determineAgeGroup(currentPatient.ageYears, currentPatient.ageMonths)
        val dosageRule = indication.dosageRules.find {
            it.ageGroup == ageGroup || it.ageGroup == "ALL_AGES"
        } ?: indication.dosageRules.firstOrNull()

        dosageRule?.let { rule ->
            val dose = when (rule.calculation.type) {
                "PER_KG" -> rule.calculation.value * calculatedValues.effectiveWeight
                "FIXED" -> rule.calculation.value
                else -> rule.calculation.value
            }

            // Max/Min Dose prüfen
            when {
                rule.calculation.maxDose != null && dose > rule.calculation.maxDose ->
                    rule.calculation.maxDose
                rule.calculation.minDose != null && dose < rule.calculation.minDose ->
                    rule.calculation.minDose
                else -> dose
            }
        }
    }

    val calculatedVolume = if (effectiveConcentration > 0 && calculatedDose != null) {
        calculatedDose / effectiveConcentration
    } else null

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Medikamentenrechner",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Patienteninfo
        PatientInfoCard(currentPatient, calculatedValues)

        // Medikament auswählen
        MedicationDropdown(
            medications = medications,
            selectedMedication = selectedMedication,
            onMedicationSelected = { medication ->
                selectedMedication = medication
                selectedIndication = medication.indications.firstOrNull()
                selectedRoute = medication.indications.firstOrNull()?.route
                customConcentration = ""
                selectedInfoTab = null
            }
        )

        // Einsatzfall auswählen
        selectedMedication?.let { medication ->
            IndicationDropdown(
                indications = medication.indications,
                selectedIndication = selectedIndication,
                onIndicationSelected = { indication ->
                    selectedIndication = indication
                    selectedRoute = indication.route
                }
            )
        }

        // Applikationsart anzeigen
        selectedIndication?.let { indication ->
            ApplicationCard(
                route = indication.route,
                preparation = indication.preparation
            )
        }

        // Konzentration
        selectedMedication?.let {
            ConcentrationCard(
                standardConcentration = standardConcentration,
                customConcentration = customConcentration,
                onConcentrationChanged = { customConcentration = it }
            )
        }

        // Berechnung
        if (calculatedDose != null && calculatedVolume != null) {
            val hasWarning = selectedIndication?.dosageRules
                ?.firstOrNull()?.calculation?.maxDose?.let {
                    calculatedDose >= it
                } == true

            CalculationResultCard(
                dose = calculatedDose,
                volume = calculatedVolume,
                diluentInfo = selectedIndication?.preparation,
                hasWarning = hasWarning
            )
        }

        // Info Buttons - optimiert für Platz
        selectedMedication?.let { medication ->
            CompactInfoButtonRow(
                selectedTab = selectedInfoTab,
                onTabSelected = { selectedInfoTab = it }
            )

            // Info Content
            selectedInfoTab?.let { tab ->
                InfoContentCard(
                    tab = tab,
                    medication = medication
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PatientInfoCard(
    patient: com.rdinfo2.data.patient.PatientData,
    calculatedValues: com.rdinfo2.data.patient.CalculatedPatientValues
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Patient",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                val ageText = if (patient.ageMonths > 0) {
                    "${patient.ageYears}J ${patient.ageMonths}M"
                } else {
                    "${patient.ageYears} Jahre"
                }

                val genderText = when (patient.gender) {
                    PatientGender.MALE -> "♂"
                    PatientGender.FEMALE -> "♀"
                    PatientGender.UNKNOWN -> "?"
                }

                Text(
                    text = "$ageText • $genderText",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "${String.format("%.1f", calculatedValues.effectiveWeight)} kg",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationDropdown(
    medications: List<JsonMedication>,
    selectedMedication: JsonMedication?,
    onMedicationSelected: (JsonMedication) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Medikament",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedMedication?.name ?: "Bitte wählen...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    medications.forEach { medication ->
                        DropdownMenuItem(
                            text = { Text(medication.name) },
                            onClick = {
                                onMedicationSelected(medication)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IndicationDropdown(
    indications: List<JsonIndication>,
    selectedIndication: JsonIndication?,
    onIndicationSelected: (JsonIndication) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Einsatzfall",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedIndication?.name ?: "Bitte wählen...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    indications.forEach { indication ->
                        DropdownMenuItem(
                            text = { Text(indication.name) },
                            onClick = {
                                onIndicationSelected(indication)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationCard(
    route: String,
    preparation: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Applikationsart",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = route,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = preparation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ConcentrationCard(
    standardConcentration: Double,
    customConcentration: String,
    onConcentrationChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Konzentration",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Standard: $standardConcentration mg/ml",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = customConcentration,
                onValueChange = onConcentrationChanged,
                label = { Text("Andere Konzentration (mg/ml)") },
                placeholder = { Text("$standardConcentration") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun CalculationResultCard(
    dose: Double,
    volume: Double,
    diluentInfo: String?,
    hasWarning: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasWarning) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                if (hasWarning) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Berechnung",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dosis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dosis:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.2f", dose)} mg",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Volumen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Volumen:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.1f", volume)} ml",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Verdünnungsinfo
            diluentInfo?.let { info ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Zubereitung: $info",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (hasWarning) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⚠️ Maximaldosis erreicht!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompactInfoButtonRow(
    selectedTab: InfoTab?,
    onTabSelected: (InfoTab) -> Unit
) {
    // Erste Reihe
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CompactInfoButton(
            text = "Indikation",
            isSelected = selectedTab == InfoTab.INDICATION,
            onClick = { onTabSelected(InfoTab.INDICATION) },
            modifier = Modifier.weight(1f)
        )

        CompactInfoButton(
            text = "Kontra-\nindikation",
            isSelected = selectedTab == InfoTab.CONTRAINDICATION,
            onClick = { onTabSelected(InfoTab.CONTRAINDICATION) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Zweite Reihe
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CompactInfoButton(
            text = "Wirkung",
            isSelected = selectedTab == InfoTab.EFFECT,
            onClick = { onTabSelected(InfoTab.EFFECT) },
            modifier = Modifier.weight(1f)
        )

        CompactInfoButton(
            text = "Neben-\nwirkung",
            isSelected = selectedTab == InfoTab.SIDE_EFFECT,
            onClick = { onTabSelected(InfoTab.SIDE_EFFECT) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CompactInfoButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InfoContentCard(
    tab: InfoTab,
    medication: JsonMedication
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (tab) {
                    InfoTab.INDICATION -> "Indikationen"
                    InfoTab.CONTRAINDICATION -> "Kontraindikationen"
                    InfoTab.EFFECT -> "Wirkung"
                    InfoTab.SIDE_EFFECT -> "Nebenwirkungen"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val content = when (tab) {
                InfoTab.INDICATION -> medication.indications.joinToString("\n\n") {
                    "• ${it.name} (${it.route})"
                }
                InfoTab.CONTRAINDICATION -> medication.contraindications.joinToString("\n") { "• $it" }
                InfoTab.EFFECT -> medication.notes ?: "Keine Informationen verfügbar"
                InfoTab.SIDE_EFFECT -> medication.warnings.joinToString("\n") { "• $it" }
            }

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// Hilfsfunktionen
private fun loadMedicationsFromJson(context: Context): List<JsonMedication> {
    return try {
        JsonMedicationLoader.loadMedications(context)
    } catch (e: Exception) {
        emptyList()
    }
}

private fun extractConcentration(preparation: String): Double {
    // Versuche mg/ml aus dem String zu extrahieren
    val regex = """(\d+(?:\.\d+)?)\s*mg/ml""".toRegex()
    return regex.find(preparation)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
}

private fun determineAgeGroup(years: Int, months: Int): String {
    val totalMonths = years * 12 + months
    return when {
        totalMonths < 12 -> "INFANT"
        years < 12 -> "CHILD"
        years < 18 -> "ADOLESCENT"
        years < 65 -> "ADULT"
        else -> "GERIATRIC"
    }
}