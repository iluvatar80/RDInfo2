// app/src/main/java/com/rdinfo2/MainActivity.kt
package com.rdinfo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                0 -> MedicationCalculatorScreen()
                1 -> AlgorithmsScreen()
                2 -> ReferenceValuesScreen()
                3 -> SpecialReferencesScreen()
            }
        }

        // Overlay-System für Wischgesten
        com.rdinfo2.ui.components.overlay.GestureOverlaySystem(
            modifier = Modifier.fillMaxSize()
        )

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
// FUNKTIONALER MEDIKAMENTENRECHNER - Keine externen Abhängigkeiten
// =====================================================

@Composable
fun MedicationCalculatorScreen() {
    // Einfacher lokaler State - keine komplexen Manager
    var patientAgeYears by remember { mutableIntStateOf(35) }
    var patientAgeMonths by remember { mutableIntStateOf(0) }
    var patientWeight by remember { mutableDoubleStateOf(0.0) } // 0 = automatisch schätzen
    var showPatientEdit by remember { mutableStateOf(false) }
    var selectedMedication by remember { mutableStateOf("Adrenalin") }

    // Gewichtsschätzung
    val estimatedWeight = when {
        patientWeight > 0 -> patientWeight // Manuelles Gewicht
        else -> estimateWeight(patientAgeYears, patientAgeMonths) // Automatisch
    }

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
                    text = "🚀 Funktionaler Medikamentenrechner",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Klicke auf Buttons für echte Dosierungsberechnungen!",
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
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Button(onClick = { showPatientEdit = true }) {
                        Text("Bearbeiten")
                    }
                }

                // Aktuelle Patientendaten
                val formattedAge = if (patientAgeYears == 0 && patientAgeMonths > 0) {
                    "$patientAgeMonths Monate"
                } else if (patientAgeMonths == 0) {
                    "$patientAgeYears Jahre"
                } else {
                    "$patientAgeYears J., $patientAgeMonths M."
                }

                val formattedWeight = if (patientWeight > 0) {
                    "${String.format("%.1f", patientWeight)} kg"
                } else {
                    "${String.format("%.1f", estimatedWeight)} kg (geschätzt)"
                }

                Text("Alter: $formattedAge")
                Text("Gewicht: $formattedWeight")

                // Quick-Patient Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            patientAgeYears = 0
                            patientAgeMonths = 6
                            patientWeight = 0.0
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Säugling", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            patientAgeYears = 8
                            patientAgeMonths = 0
                            patientWeight = 0.0
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Kind", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            patientAgeYears = 35
                            patientAgeMonths = 0
                            patientWeight = 0.0
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Erwachsener", fontSize = 12.sp)
                    }
                }
            }
        }

        // Medikamenten-Rechner - FUNKTIONAL
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

                // Adrenalin Berechnung
                val adrenalinDose = when {
                    patientAgeYears < 1 -> 0.01 * estimatedWeight
                    patientAgeYears < 12 -> 0.01 * estimatedWeight
                    else -> 1.0
                }

                Text(
                    text = "Dosis: ${String.format("%.2f", adrenalinDose)} mg",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text("Volumen: ${String.format("%.1f", adrenalinDose)} ml (1:1000)")
                Text("Verabreichung: i.v./i.o. unverdünnt")

                // Warnung bei hoher Dosis
                if (adrenalinDose > 5.0) {
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
                                patientAgeYears < 12 -> (0.02 * estimatedWeight).coerceAtLeast(0.1).coerceAtMost(1.0)
                                else -> 0.5
                            }
                            selectedMedication = "Atropin: ${String.format("%.2f", atropinDose)} mg i.v."
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Atropin", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            val glucoseDose = when {
                                patientAgeYears < 1 -> 2.0 * estimatedWeight
                                patientAgeYears < 12 -> 1.0 * estimatedWeight
                                else -> 50.0
                            }
                            selectedMedication = "Glucose 40%: ${String.format("%.0f", glucoseDose)} ml i.v."
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Glucose", fontSize = 12.sp)
                    }
                }

                // Anzeige anderer Medikamente
                if (selectedMedication != "Adrenalin") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Weitere Berechnung:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                selectedMedication,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    // Patient Edit Dialog
    if (showPatientEdit) {
        var tempAgeYears by remember { mutableStateOf(patientAgeYears.toString()) }
        var tempAgeMonths by remember { mutableStateOf(patientAgeMonths.toString()) }
        var tempWeight by remember { mutableStateOf(if (patientWeight > 0) patientWeight.toString() else "") }

        AlertDialog(
            onDismissRequest = { showPatientEdit = false },
            title = { Text("Patientendaten bearbeiten") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tempAgeYears,
                        onValueChange = { tempAgeYears = it },
                        label = { Text("Jahre") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = tempAgeMonths,
                        onValueChange = { tempAgeMonths = it },
                        label = { Text("Monate (0-11)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = tempWeight,
                        onValueChange = { tempWeight = it },
                        label = { Text("Gewicht (kg, optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Leer lassen für automatische Schätzung") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            patientAgeYears = tempAgeYears.toIntOrNull() ?: 0
                            patientAgeMonths = tempAgeMonths.toIntOrNull() ?: 0
                            patientWeight = tempWeight.toDoubleOrNull() ?: 0.0
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

// Gewichtsschätzung nach medizinischen Formeln
fun estimateWeight(years: Int, months: Int): Double {
    val totalMonths = years * 12 + months

    return when {
        totalMonths == 0 -> 3.5
        totalMonths <= 12 -> 3.5 + (totalMonths * 0.5)
        years in 1..5 -> 2.0 * years + 8.0
        years in 6..12 -> 3.0 * years + 7.0
        years in 13..17 -> 50.0 + (years - 13) * 5.0
        else -> 70.0
    }
}

// =====================================================
// PLACEHOLDER SCREENS - Zeigen Roadmap
// =====================================================

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
                            "Integration mit dem Medikamentenrechner geplant.",
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