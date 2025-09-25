// app/src/main/java/com/rdinfo2/MainActivity.kt
package com.rdinfo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rdinfo2.data.preferences.SettingsManager
import com.rdinfo2.data.preferences.ThemeMode
import com.rdinfo2.ui.components.gesture.GestureOverlayContainer
import com.rdinfo2.ui.screens.*
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
    val context = LocalContext.current
    val themeMode by settingsManager.themeMode.collectAsStateWithLifecycle()

    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    RDInfo2Theme(darkTheme = darkTheme) {
        MainContent(settingsManager)
    }
}

@Composable
fun MainContent(settingsManager: SettingsManager) {
    var currentTab by remember { mutableStateOf(MainTab.MEDICATION_CALCULATOR) }
    var showMenu by remember { mutableStateOf(false) }

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
                onTabChange = { currentTab = it },
                onMenuClick = { showMenu = true }
            )

            // Tab content
            when (currentTab) {
                MainTab.MEDICATION_CALCULATOR -> {
                    MedicationCalculatorScreen()
                }
                MainTab.ALGORITHMS -> {
                    AlgorithmsScreen()
                }
                MainTab.REFERENCE_VALUES -> {
                    ReferenceValuesScreen()
                }
                MainTab.SPECIAL_REFERENCES -> {
                    SpecialReferencesScreen()
                }
            }
        }

        // Gesture overlays container
        GestureOverlayContainer(
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
    currentTab: MainTab,
    onTabChange: (MainTab) -> Unit,
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
            selectedTabIndex = currentTab.ordinal,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            indicator = { tabPositions ->
                if (tabPositions.isNotEmpty() && currentTab.ordinal < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) {
            MainTab.values().forEach { tab ->
                Tab(
                    selected = currentTab == tab,
                    onClick = { onTabChange(tab) },
                    text = {
                        Text(
                            text = tab.displayName,
                            color = if (currentTab == tab) {
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

// Placeholder screens - will be implemented in separate files
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
                        text = "Gesten-Navigation",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "• Von oben wischen: Patientendaten eingeben\n" +
                                "• Von links wischen: SAMPLER-Schema\n" +
                                "• Von rechts wischen: xABCDE-Schema",
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
                    text = "Hier werden die interaktiven Flowcharts aus dem Hamburger Rettungsdienst-Handbuch implementiert.",
                    style = MaterialTheme.typography.bodyMedium
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
                    text = "Alters- und gewichtsspezifische Normalwerte für Vitalparameter, " +
                            "basierend auf den eingegebenen Patientendaten.",
                    style = MaterialTheme.typography.bodyMedium
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
                    text = "SEPSIS-Score, ISOBAR-Schema, GP-START, Toxidrome, " +
                            "EKG-Interpretation und weitere Hilfsmittel.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

enum class MainTab(val displayName: String) {
    MEDICATION_CALCULATOR("Medikamente"),
    ALGORITHMS("xABCDE"),
    REFERENCE_VALUES("Normalwerte"),
    SPECIAL_REFERENCES("Nachschlagewerke")
}