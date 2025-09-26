// app/src/main/java/com/rdinfo2/ui/screens/MedicationCalculatorScreen.kt
package com.rdinfo2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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