// app/src/main/java/com/rdinfo2/MainActivity.kt
package com.rdinfo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
                0 -> MedicationCalculatorScreen()
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

        // Tab row
        ScrollableTabRow(
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

// Inline Screen Implementations to avoid import issues
@Composable
fun MedicationCalculatorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = "Medikamentenrechner",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Hier wird der erweiterte Medikamentenrechner implementiert. " +
                                "Basierend auf der RDInfo-App Logik, aber mit Unterstützung für alle Einheiten.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Gesture hint
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
                        text = "Status: App läuft! 🎉",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Die Grundstruktur ist implementiert. Als nächstes:\n" +
                                "• Medikamenten-Datenbank hinzufügen\n" +
                                "• Interaktive Flowcharts implementieren\n" +
                                "• Timer-Funktionen für HLW",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
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
                    text = "Hier werden die 35 interaktiven Flowcharts aus dem Hamburger " +
                            "Rettungsdienst-Handbuch implementiert. Startend mit x1.1 (HLW Erwachsene).",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Status info
                Divider()
                Text(
                    text = "PDF-Analyse: 100% abgeschlossen\n" +
                            "Flowchart-Struktur: Definiert\n" +
                            "Implementation: Bereit zum Start",
                    style = MaterialTheme.typography.bodySmall
                )
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
                    text = "Alters- und gewichtsspezifische Normalwerte für Vitalparameter. " +
                            "Automatische Berechnung basierend auf Patientendaten (Alter, Geschlecht, Gewicht).",
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider()
                Text(
                    text = "• Neugeborene bis Geriatrisch\n" +
                            "• HF, RR, AF, Temperatur\n" +
                            "• Atemvolumen & Blutvolumen\n" +
                            "• Hämoglobin-Werte",
                    style = MaterialTheme.typography.bodySmall
                )
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
                    text = "16 verschiedene Nachschlagewerke für den Rettungsdienst:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider()
                Text(
                    text = "• Glasgow Coma Scale (GCS)\n" +
                            "• APGAR-Score\n" +
                            "• FAST-Test\n" +
                            "• Verbrennungsregel der 9er\n" +
                            "• EKG-Normwerte\n" +
                            "• Intubationsgrößen\n" +
                            "• SEPSIS-Score, ISOBAR, GP-START...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}