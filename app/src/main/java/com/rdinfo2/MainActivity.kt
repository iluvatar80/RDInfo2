// app/src/main/java/com/rdinfo2/MainActivity.kt
package com.rdinfo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rdinfo2.data.preferences.SettingsManager
import com.rdinfo2.data.preferences.ThemeMode
import com.rdinfo2.ui.theme.RDInfo2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize managers
        val settingsManager = SettingsManager.getInstance(this)

        setContent {
            MainScreen(settingsManager)
        }
    }
}

@Composable
fun MainScreen(settingsManager: SettingsManager) {
    val themeMode by settingsManager.themeMode.collectAsStateWithLifecycle()

    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    RDInfo2Theme(darkTheme = darkTheme) {
        MainContent(settingsManager)
    }
}

@Composable
fun MainContent(settingsManager: SettingsManager) {
    var currentTab by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    val tabs = listOf("Medikamente", "xABCDE", "Normalwerte", "Nachschlagewerke")

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content with tabs
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Top bar with tabs and menu
            TopAppBarWithTabs(
                currentTab = currentTab,
                tabs = tabs,
                onTabChange = { currentTab = it },
                onMenuClick = { showMenu = true }
            )

            // Tab content
            when (currentTab) {
                0 -> MedicationCalculatorScreenPlaceholder()
                1 -> AlgorithmsScreen()
                2 -> ReferenceValuesScreen()
                3 -> SpecialReferencesScreen()
            }
        }

        // Main menu dropdown
        if (showMenu) {
            MainMenuDropdown(
                onDismiss = { showMenu = false },
                onNavigateToSettings = {
                    showMenu = false
                    // TODO: Navigate to settings
                },
                onNavigateToEditor = {
                    showMenu = false
                    // TODO: Navigate to editor
                },
                onNavigateToInfo = {
                    showMenu = false
                    // TODO: Navigate to info
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithTabs(
    currentTab: Int,
    tabs: List<String>,
    onTabChange: (Int) -> Unit,
    onMenuClick: () -> Unit
) {
    Column {
        // Top app bar
        TopAppBar(
            title = { Text("RD-Info2") },
            actions = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menü"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // Tab row - vereinfacht ohne problematische Indikatoren
        TabRow(
            selectedTabIndex = currentTab,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = currentTab == index,
                    onClick = { onTabChange(index) },
                    text = {
                        Text(
                            text = tab,
                            color = if (currentTab == index) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun MainMenuDropdown(
    onDismiss: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToInfo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = onDismiss,
            offset = DpOffset(x = (-8).dp, y = 8.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Editor") },
                onClick = onNavigateToEditor
            )
            DropdownMenuItem(
                text = { Text("Einstellungen") },
                onClick = onNavigateToSettings
            )
            DropdownMenuItem(
                text = { Text("Info") },
                onClick = onNavigateToInfo
            )
        }
    }
}

// =====================================================
// PLACEHOLDER SCREENS - Diese werden schrittweise ersetzt
// =====================================================

// PLACEHOLDER - wird durch echten MedicationCalculatorScreen ersetzt
// Diese Version ist FUNKTIONAL - nicht nur Text!
@Composable
fun MedicationCalculatorScreenPlaceholder() {
    val patientManager = remember { PatientDataManager.getInstance() }
    val currentPatient by patientManager.currentPatient.collectAsStateWithLifecycle()

    var selectedMedication by remember { mutableStateOf("Adrenalin") }
    var showPatientEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🚀 RD-Info2 - Funktionaler Medikamentenrechner",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Diese Version ist FUNKTIONAL - du kannst echte Berechnungen durchführen!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Patientendaten Card - INTERAKTIV
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = { showPatientEdit = true }
                    ) {
                        Text("Bearbeiten")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Alter: ${currentPatient.getFormattedAge()}")
                Text("Gewicht: ${currentPatient.getFormattedWeight()}")
                Text("Kategorie: ${currentPatient.ageCategory}")

                Spacer(modifier = Modifier.height(8.dp))

                // Quick-Patient Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { patientManager.setQuickPatient("säugling") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Säugling")
                    }
                    Button(
                        onClick = { patientManager.setQuickPatient("kind") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Kind")
                    }
                    Button(
                        onClick = { patientManager.setQuickPatient("erwachsener") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Erwachsener")
                    }
                }
            }
        }

        // Medikamenten-Rechner Card - FUNKTIONAL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Adrenalin-Rechner (Reanimation)",
                    style = MaterialTheme.typography.titleMedium
                )

                val weight = currentPatient.getEffectiveWeight()
                val dose = when {
                    currentPatient.ageYears < 1 -> 0.01 * weight
                    currentPatient.ageYears < 12 -> 0.01 * weight
                    else -> 1.0
                }

                Text(
                    text = "Berechnete Dosis: ${String.format("%.2f", dose)} mg",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text("Volumen: ${String.format("%.1f", dose)} ml (1:1000)")
                Text("Verabreichung: i.v./i.o. unverdünnt")

                if (dose > 5.0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            "⚠️ Hohe Dosis - Kontrolle empfohlen",
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Andere Medikamente
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val atropinDose = when {
                                currentPatient.ageYears < 12 -> (0.02 * weight).coerceAtLeast(0.1).coerceAtMost(1.0)
                                else -> 0.5
                            }
                            selectedMedication = "Atropin: ${String.format("%.2f", atropinDose)} mg"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Atropin", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            val glucoseDose = when {
                                currentPatient.ageYears < 1 -> 2.0 * weight
                                currentPatient.ageYears < 12 -> 1.0 * weight
                                else -> 50.0
                            }
                            selectedMedication = "Glucose 40%: ${String.format("%.0f", glucoseDose)} ml"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Glucose", fontSize = 12.sp)
                    }
                }

                if (selectedMedication != "Adrenalin") {
                    Text(
                        selectedMedication,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Patient Edit Dialog
    if (showPatientEdit) {
        var ageYears by remember { mutableStateOf(currentPatient.ageYears.toString()) }
        var ageMonths by remember { mutableStateOf(currentPatient.ageMonths.toString()) }
        var weight by remember { mutableStateOf(if (currentPatient.isManualWeight) currentPatient.weightKg.toString() else "") }

        AlertDialog(
            onDismissRequest = { showPatientEdit = false },
            title = { Text("Patientendaten") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageYears,
                        onValueChange = { ageYears = it },
                        label = { Text("Jahre") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = ageMonths,
                        onValueChange = { ageMonths = it },
                        label = { Text("Monate (0-11)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Gewicht (kg, optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Leer = automatische Schätzung") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val years = ageYears.toIntOrNull() ?: 0
                            val months = ageMonths.toIntOrNull() ?: 0
                            val weightValue = weight.toDoubleOrNull()

                            if (weightValue != null && weightValue > 0) {
                                patientManager.updatePatient(
                                    com.rdinfo2.data.patient.PatientDataFactory.createWithWeight(years, months, weightValue)
                                )
                            } else {
                                patientManager.updatePatient(
                                    com.rdinfo2.data.patient.PatientDataFactory.create(years, months)
                                )
                            }
                            showPatientEdit = false
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPatientEdit = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun AlgorithmsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "xABCDE Algorithmen",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "35 Algorithmen aus dem Hamburger Rettungsdienst-Handbuch wurden analysiert. " +
                            "Implementierung beginnt mit x1.1 (HLW Erwachsene).",
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider()

                Column {
                    Text("Geplante erste Algorithmen:", style = MaterialTheme.typography.titleSmall)
                    Text("• x1.1: Reanimation Erwachsene", style = MaterialTheme.typography.bodySmall)
                    Text("• A1: Freimachen der Atemwege", style = MaterialTheme.typography.bodySmall)
                    Text("• B1: Spannungspneumothorax", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ReferenceValuesScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Normalwerte",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Altersabhängige Vitalparameter und Referenzwerte. " +
                            "Benötigt PatientData für dynamische Berechnung.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider()

                Column {
                    Text("Geplante Inhalte:", style = MaterialTheme.typography.titleSmall)
                    Text("• Herzfrequenz (HF)", style = MaterialTheme.typography.bodySmall)
                    Text("• Atemfrequenz (AF)", style = MaterialTheme.typography.bodySmall)
                    Text("• Blutdruck (RR)", style = MaterialTheme.typography.bodySmall)
                    Text("• Gewichtsschätzung", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun SpecialReferencesScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Spezielle Nachschlagewerke",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Medizinische Scores und Bewertungsschemata für den Rettungsdienst.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider()

                Column {
                    Text("Geplante Referenzen:", style = MaterialTheme.typography.titleSmall)
                    Text("• Glasgow Coma Scale (GCS)", style = MaterialTheme.typography.bodySmall)
                    Text("• APGAR-Score", style = MaterialTheme.typography.bodySmall)
                    Text("• FAST-Test", style = MaterialTheme.typography.bodySmall)
                    Text("• Verbrennungsregel der 9er", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}