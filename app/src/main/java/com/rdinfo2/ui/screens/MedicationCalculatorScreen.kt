// app/src/main/java/com/rdinfo2/ui/screens/MedicationCalculatorScreen.kt
package com.rdinfo2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdinfo2.data.json.JsonMedicationLoader
import com.rdinfo2.data.model.*
import com.rdinfo2.data.patient.PatientDataManager
import com.rdinfo2.logic.DosingCalculator

@Composable
fun MedicationCalculatorScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // State
    var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var selectedUseCase by remember { mutableStateOf<UseCase?>(null) }
    var calculationResult by remember { mutableStateOf<DoseCalculationResult?>(null) }

    // Konzentrations-Override State
    var manualConcentrationEnabled by remember { mutableStateOf(false) }
    var manualConcentration by remember { mutableStateOf("") }
    var manualConcentrationUnit by remember { mutableStateOf("mg") }
    var manualVolume by remember { mutableStateOf("") }
    var manualVolumeUnit by remember { mutableStateOf("ml") }

    // Lade Medikamente beim Start
    LaunchedEffect(Unit) {
        val loader = JsonMedicationLoader(context)
        medications = loader.loadMedications()
    }

    // Automatische Neuberechnung
    LaunchedEffect(
        selectedMedication,
        selectedUseCase,
        PatientDataManager.ageYears,
        PatientDataManager.estimatedWeightKg,
        manualConcentrationEnabled,
        manualConcentration,
        manualConcentrationUnit,
        manualVolume,
        manualVolumeUnit
    ) {
        if (selectedMedication != null && selectedUseCase != null) {
            val override = if (manualConcentrationEnabled &&
                manualConcentration.isNotEmpty() &&
                manualVolume.isNotEmpty()) {
                ConcentrationOverride(
                    medicationId = selectedMedication!!.id,
                    concentration = manualConcentration.toDoubleOrNull() ?: 0.0,
                    concentrationUnit = manualConcentrationUnit,
                    volume = manualVolume.toDoubleOrNull() ?: 0.0,
                    volumeUnit = manualVolumeUnit
                )
            } else null

            calculationResult = DosingCalculator.calculateDose(
                medication = selectedMedication!!,
                useCase = selectedUseCase!!,
                weightKg = PatientDataManager.estimatedWeightKg,
                ageYears = PatientDataManager.ageYears,
                concentrationOverride = override
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Medikamentenrechner",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PatientInfoCard()
            }

            item {
                MedicationSelectionCard(
                    medications = medications,
                    selectedMedication = selectedMedication,
                    onMedicationSelected = {
                        selectedMedication = it
                        selectedUseCase = it.useCases.firstOrNull()
                        manualConcentrationEnabled = false
                    }
                )
            }

            selectedMedication?.let { medication ->
                if (medication.useCases.size > 1) {
                    item {
                        UseCaseSelectionCard(
                            useCases = medication.useCases,
                            selectedUseCase = selectedUseCase,
                            onUseCaseSelected = { selectedUseCase = it }
                        )
                    }
                }
            }

            selectedMedication?.let { medication ->
                selectedUseCase?.let { useCase ->
                    val preparation = medication.preparations.find {
                        it.id == useCase.preparation.preparationId
                    }
                    preparation?.let { prep ->
                        item {
                            ConcentrationCard(
                                preparation = prep,
                                manualEnabled = manualConcentrationEnabled,
                                onManualEnabledChange = {
                                    manualConcentrationEnabled = it
                                    if (!it) {
                                        manualConcentration = ""
                                        manualVolume = ""
                                    }
                                },
                                manualConcentration = manualConcentration,
                                onManualConcentrationChange = { manualConcentration = it },
                                manualConcentrationUnit = manualConcentrationUnit,
                                onManualConcentrationUnitChange = { manualConcentrationUnit = it },
                                manualVolume = manualVolume,
                                onManualVolumeChange = { manualVolume = it },
                                manualVolumeUnit = manualVolumeUnit,
                                onManualVolumeUnitChange = { manualVolumeUnit = it }
                            )
                        }
                    }
                }
            }

            calculationResult?.let { result ->
                item {
                    CalculationResultCard(result = result)
                }
            }
        }
    }
}

// Alle Composables auf Top-Level (nicht innerhalb der Hauptfunktion)

@Composable
fun PatientInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Patient",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${PatientDataManager.ageYears} Jahre | ${PatientDataManager.estimatedWeightKg} kg",
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationSelectionCard(
    medications: List<Medication>,
    selectedMedication: Medication?,
    onMedicationSelected: (Medication) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalPharmacy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Medikament",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedMedication?.name ?: "Auswählen...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    medications.forEach { medication ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(medication.name, fontWeight = FontWeight.Medium)
                                    medication.genericName?.let {
                                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
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
fun UseCaseSelectionCard(
    useCases: List<UseCase>,
    selectedUseCase: UseCase?,
    onUseCaseSelected: (UseCase) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Einsatzfall",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedUseCase?.name ?: "Auswählen...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    useCases.forEach { useCase ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(useCase.name)
                                    Text(
                                        "Route: ${useCase.route}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onUseCaseSelected(useCase)
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
fun ConcentrationCard(
    preparation: Preparation,
    manualEnabled: Boolean,
    onManualEnabledChange: (Boolean) -> Unit,
    manualConcentration: String,
    onManualConcentrationChange: (String) -> Unit,
    manualConcentrationUnit: String,
    onManualConcentrationUnitChange: (String) -> Unit,
    manualVolume: String,
    onManualVolumeChange: (String) -> Unit,
    manualVolumeUnit: String,
    onManualVolumeUnitChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Medikamentenkonzentration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!manualEnabled) {
                Text(
                    text = preparation.getConcentrationDisplay(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (preparation.isDrySubstance()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠ Trockensubstanz",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    preparation.getConcentrationPerMlDisplay()?.let { perMl ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = perMl,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manuell", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = manualEnabled,
                    onCheckedChange = onManualEnabledChange
                )
            }

            if (manualEnabled) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualConcentration,
                        onValueChange = onManualConcentrationChange,
                        label = { Text("Konzentration") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(2f)
                    )

                    ConcentrationUnitDropdown(
                        selectedUnit = manualConcentrationUnit,
                        onUnitSelected = onManualConcentrationUnitChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualVolume,
                        onValueChange = onManualVolumeChange,
                        label = { Text("Volumen") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(2f)
                    )

                    VolumeUnitDropdown(
                        selectedUnit = manualVolumeUnit,
                        onUnitSelected = onManualVolumeUnitChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcentrationUnitDropdown(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val units = listOf("mg", "µg", "g", "I.E.")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeUnitDropdown(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val units = listOf("ml", "Hub", "l")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CalculationResultCard(
    result: DoseCalculationResult
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isValid) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (result.isValid) "Dosierung" else "Fehler",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (result.isValid) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )

            if (result.isValid) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dosis:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = "${String.format("%.2f", result.dose)} ${result.doseUnit}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Volumen:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = "${String.format("%.2f", result.volume)} ${result.volumeUnit}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (result.warnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    result.warnings.forEach { warning ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warning,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = result.calculation,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.errorMessage ?: "Unbekannter Fehler",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
var selectedMedication by remember { mutableStateOf<Medication?>(null) }
var selectedUseCase by remember { mutableStateOf<UseCase?>(null) }
var calculationResult by remember { mutableStateOf<DoseCalculationResult?>(null) }

// Konzentrations-Override State
var manualConcentrationEnabled by remember { mutableStateOf(false) }
var manualConcentration by remember { mutableStateOf("") }
var manualConcentrationUnit by remember { mutableStateOf("mg") }
var manualVolume by remember { mutableStateOf("") }
var manualVolumeUnit by remember { mutableStateOf("ml") }

// Lade Medikamente beim Start
LaunchedEffect(Unit) {
    val loader = JsonMedicationLoader(context)
    medications = loader.loadMedications()
}

// Automatische Neuberechnung
LaunchedEffect(
selectedMedication,
selectedUseCase,
PatientDataManager.ageYears,
PatientDataManager.estimatedWeightKg,
manualConcentrationEnabled,
manualConcentration,
manualConcentrationUnit,
manualVolume,
manualVolumeUnit
) {
    if (selectedMedication != null && selectedUseCase != null) {
        val override = if (manualConcentrationEnabled &&
            manualConcentration.isNotEmpty() &&
            manualVolume.isNotEmpty()) {
            ConcentrationOverride(
                medicationId = selectedMedication!!.id,
                concentration = manualConcentration.toDoubleOrNull() ?: 0.0,
                concentrationUnit = manualConcentrationUnit,
                volume = manualVolume.toDoubleOrNull() ?: 0.0,
                volumeUnit = manualVolumeUnit
            )
        } else null

        calculationResult = DosingCalculator.calculateDose(
            medication = selectedMedication!!,
            useCase = selectedUseCase!!,
            weightKg = PatientDataManager.estimatedWeightKg,
            ageYears = PatientDataManager.ageYears,
            concentrationOverride = override
        )
    }
}

Column(
modifier = modifier
.fillMaxSize()
.padding(16.dp)
) {
    Text(
        text = "Medikamentenrechner",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Patienteninfo
        item {
            PatientInfoCard()
        }

        // Medikamentenauswahl
        item {
            MedicationSelectionCard(
                medications = medications,
                selectedMedication = selectedMedication,
                onMedicationSelected = {
                    selectedMedication = it
                    selectedUseCase = it.useCases.firstOrNull()
                    manualConcentrationEnabled = false
                }
            )
        }

        // UseCase-Auswahl
        selectedMedication?.let { medication ->
            if (medication.useCases.size > 1) {
                item {
                    UseCaseSelectionCard(
                        useCases = medication.useCases,
                        selectedUseCase = selectedUseCase,
                        onUseCaseSelected = { selectedUseCase = it }
                    )
                }
            }
        }

        // Konzentrations-Anzeige und Override
        selectedMedication?.let { medication ->
            selectedUseCase?.let { useCase ->
                val preparation = medication.preparations.find {
                    it.id == useCase.preparation.preparationId
                }
                preparation?.let { prep ->
                    item {
                        ConcentrationCard(
                            preparation = prep,
                            manualEnabled = manualConcentrationEnabled,
                            onManualEnabledChange = {
                                manualConcentrationEnabled = it
                                if (!it) {
                                    manualConcentration = manualConcentration,
                                    onManualConcentrationChange = { manualConcentration = it },
                                    manualConcentrationUnit = manualConcentrationUnit,
                                    onManualConcentrationUnitChange = { manualConcentrationUnit = it },
                                    manualVolume = manualVolume,
                                    onManualVolumeChange = { manualVolume = it },
                                    manualVolumeUnit = manualVolumeUnit,
                                    onManualVolumeUnitChange = { manualVolumeUnit = it }
                                    )
                                }
                            }
                    }
                }

                // Berechnungsergebnis
                calculationResult?.let { result ->
                    item {
                        CalculationResultCard(result = result)
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
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Patient",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${PatientDataManager.ageYears} Jahre | ${PatientDataManager.estimatedWeightKg} kg",
                    fontSize = 16.sp
                )
            }
        }
    }

    @Composable
    private fun MedicationSelectionCard(
        medications: List<Medication>,
        selectedMedication: Medication?,
        onMedicationSelected: (Medication) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Medikament",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMedication?.name ?: "Auswählen...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        medications.forEach { medication ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(medication.name, fontWeight = FontWeight.Medium)
                                        medication.genericName?.let {
                                            Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                },
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

    @Composable
    private fun UseCaseSelectionCard(
        useCases: List<UseCase>,
        selectedUseCase: UseCase?,
        onUseCaseSelected: (UseCase) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Einsatzfall",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedUseCase?.name ?: "Auswählen...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        useCases.forEach { useCase ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(useCase.name)
                                        Text(
                                            "Route: ${useCase.route}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onUseCaseSelected(useCase)
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
        preparation: Preparation,
        manualEnabled: Boolean,
        onManualEnabledChange: (Boolean) -> Unit,
        manualConcentration: String,
        onManualConcentrationChange: (String) -> Unit,
        manualConcentrationUnit: String,
        onManualConcentrationUnitChange: (String) -> Unit,
        manualVolume: String,
        onManualVolumeChange: (String) -> Unit,
        manualVolumeUnit: String,
        onManualVolumeUnitChange: (String) -> Unit
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Medikamentenkonzentration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!manualEnabled) {
                    // Standard-Konzentration anzeigen
                    Text(
                        text = preparation.getConcentrationDisplay(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (preparation.isDrySubstance()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠ Trockensubstanz",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        preparation.getConcentrationPerMlDisplay()?.let { perMl ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = perMl,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manuell-Schalter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manuell", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = manualEnabled,
                        onCheckedChange = onManualEnabledChange
                    )
                }

                // Manuelle Eingabefelder
                if (manualEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Konzentration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualConcentration,
                            onValueChange = onManualConcentrationChange,
                            label = { Text("Konzentration") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(2f)
                        )

                        ConcentrationUnitDropdown(
                            selectedUnit = manualConcentrationUnit,
                            onUnitSelected = onManualConcentrationUnitChange,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Volumen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualVolume,
                            onValueChange = onManualVolumeChange,
                            label = { Text("Volumen") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(2f)
                        )

                        VolumeUnitDropdown(
                            selectedUnit = manualVolumeUnit,
                            onUnitSelected = onManualVolumeUnitChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ConcentrationUnitDropdown(
        selectedUnit: String,
        onUnitSelected: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val units = listOf("mg", "µg", "g", "I.E.")
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = modifier
        ) {
            OutlinedTextField(
                value = selectedUnit,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = {
                            onUnitSelected(unit)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun VolumeUnitDropdown(
        selectedUnit: String,
        onUnitSelected: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val units = listOf("ml", "Hub", "l")
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = modifier
        ) {
            OutlinedTextField(
                value = selectedUnit,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = {
                            onUnitSelected(unit)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun CalculationResultCard(
        result: DoseCalculationResult
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (result.isValid) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (result.isValid) "Dosierung" else "Fehler",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.isValid) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )

                if (result.isValid) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dosis
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dosis:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = "${String.format("%.2f", result.dose)} ${result.doseUnit}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Volumen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Volumen:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = "${String.format("%.2f", result.volume)} ${result.volumeUnit}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Warnungen
                    if (result.warnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        result.warnings.forEach { warning ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = warning,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Berechnung
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = result.calculation,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.errorMessage ?: "Unbekannter Fehler",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    } ""
    manualVolume = ""
}
},
manualConcentration =