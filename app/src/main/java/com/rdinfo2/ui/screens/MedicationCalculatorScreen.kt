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
import com.rdinfo2.data.model.*

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
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var selectedIndication by remember { mutableStateOf<Indication?>(null) }
    var selectedRoute by remember { mutableStateOf<String?>(null) }
    var customConcentration by remember { mutableStateOf("") }
    var selectedInfoTab by remember { mutableStateOf<InfoTab?>(null) }

    // Patient data
    val currentPatient = PatientDataManager.currentPatient
    val calculatedValues = PatientDataManager.calculatedValues

    // Standardkonzentration extrahieren
    val standardConcentration = selectedMedication?.let { med ->
        extractConcentration(med.indications.firstOrNull()?.preparation ?: "")
    } ?: 1.0

    val effectiveConcentration = customConcentration.toDoubleOrNull()
        ?: standardConcentration

    // Dosisberechnung
    val calculatedDose = selectedIndication?.let { indication ->
        val ageGroup = determineAgeGroup(currentPatient.ageYears, currentPatient.ageMonths)
        val dosageRule = indication.dosageRules.find {
            it.ageGroup == ageGroup || it.ageGroup == AgeGroup.ALL_AGES
        } ?: indication.dosageRules.lastOrNull()

        dosageRule?.let { rule ->
            val dose = when (rule.calculation.type) {
                CalculationType.PER_KG -> rule.calculation.value * calculatedValues.effectiveWeight
                CalculationType.FIXED -> rule.calculation.value
                else -> rule.calculation.value
            }

            // Max/Min Dose prüfen
            when {
                rule.calculation.maxDose != null && dose > rule.calculation.maxDose!! ->
                    rule.calculation.maxDose!!
                rule.calculation.minDose != null && dose < rule.calculation.minDose!! ->
                    rule.calculation.minDose!!
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
        // Header mit Debug-Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Medikamentenrechner",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Debug Badge
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "${medications.size} Meds",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Patienteninfo
        PatientInfoCard(currentPatient, calculatedValues)

        // Medikament auswählen
        MedicationDropdown(
            medications = medications,
            selectedMedication = selectedMedication,
            onMedicationSelected = { medication ->
                selectedMedication = medication
                selectedIndication = medication.indications.firstOrNull()
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

        // Applikationsart auswählen (Dropdown)
        selectedMedication?.let { medication ->
            val availableRoutes = medication.indications
                .map { it.route }
                .distinct()

            if (availableRoutes.size > 1) {
                RouteDropdown(
                    routes = availableRoutes,
                    selectedRoute = selectedRoute,
                    onRouteSelected = { route ->
                        selectedRoute = route
                        // Wähle erste Indikation mit dieser Route
                        selectedIndication = medication.indications.find { it.route == route }
                    }
                )
            } else {
                // Wenn nur eine Route, als Card anzeigen
                selectedIndication?.let { indication ->
                    ApplicationCard(
                        route = indication.route,
                        preparation = indication.preparation ?: "Keine Angabe"
                    )
                }
            }
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

        // Info Buttons - optimiert
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
    medications: List<Medication>,
    selectedMedication: Medication?,
    onMedicationSelected: (Medication) -> Unit
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
                text = "Medikament (${medications.size} verfügbar)",
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
    indications: List<Indication>,
    selectedIndication: Indication?,
    onIndicationSelected: (Indication) -> Unit
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
                            text = { Text("${indication.name} (${indication.route})") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteDropdown(
    routes: List<String>,
    selectedRoute: String?,
    onRouteSelected: (String) -> Unit
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
                text = "Applikationsart",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedRoute ?: "Bitte wählen...",
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
                    routes.forEach { route ->
                        DropdownMenuItem(
                            text = { Text(route) },
                            onClick = {
                                onRouteSelected(route)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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
private fun CompactInfoButtonRow(
    selectedTab: InfoTab?,
    onTabSelected: (InfoTab) -> Unit
) {
    // Alle 4 Buttons in einer Reihe, jeder nur so breit wie nötig
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CompactInfoButton(
            text = "Indikation",
            isSelected = selectedTab == InfoTab.INDICATION,
            onClick = { onTabSelected(InfoTab.INDICATION) }
        )

        CompactInfoButton(
            text = "Kontraindikation",
            isSelected = selectedTab == InfoTab.CONTRAINDICATION,
            onClick = { onTabSelected(InfoTab.CONTRAINDICATION) }
        )

        CompactInfoButton(
            text = "Wirkung",
            isSelected = selectedTab == InfoTab.EFFECT,
            onClick = { onTabSelected(InfoTab.EFFECT) }
        )

        CompactInfoButton(
            text = "Nebenwirkung",
            isSelected = selectedTab == InfoTab.SIDE_EFFECT,
            onClick = { onTabSelected(InfoTab.SIDE_EFFECT) }
        )
    }
}

@Composable
private fun CompactInfoButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
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
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun InfoContentCard(
    tab: InfoTab,
    medication: Medication
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
private fun loadMedicationsFromJson(context: Context): List<Medication> {
    return try {
        val medications = JsonMedicationLoader(context).loadMedications()
        android.util.Log.d("MedCalc", "Erfolgreich ${medications.size} Medikamente geladen")
        medications.forEach { med ->
            android.util.Log.d("MedCalc", "  - ${med.name} (${med.id})")
        }
        medications
    } catch (e: Exception) {
        android.util.Log.e("MedCalc", "Fehler beim Laden der Medikamente: ${e.message}", e)
        val fallback = EmergencyMedications.getStandardSet()
        android.util.Log.d("MedCalc", "Verwende Fallback mit ${fallback.size} Medikamenten")
        fallback
    }
}

private fun extractConcentration(preparation: String): Double {
    val regex = """(\d+(?:\.\d+)?)\s*mg""".toRegex()
    val match = regex.find(preparation)
    return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
}

private fun determineAgeGroup(years: Int, months: Int): AgeGroup {
    val totalMonths = years * 12 + months
    return when {
        totalMonths < 1 -> AgeGroup.NEONATE
        totalMonths < 12 -> AgeGroup.INFANT
        years < 3 -> AgeGroup.TODDLER
        years < 12 -> AgeGroup.CHILD
        years < 18 -> AgeGroup.ADOLESCENT
        years < 65 -> AgeGroup.ADULT
        else -> AgeGroup.GERIATRIC
    }
}