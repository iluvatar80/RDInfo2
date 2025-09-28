// app/src/main/java/com/rdinfo2/ui/screens/MedicationCalculatorScreen.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.rdinfo2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdinfo2.data.PatientDataManager
import com.rdinfo2.data.Gender

/**
 * FINAL: MedicationCalculatorScreen - Funktioniert mit altem PatientDataManager
 * Verwendet nur existierende Klassen ohne Import-Probleme
 */
@Composable
fun MedicationCalculatorScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Medikamentenrechner",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Patienteninfo-Card
            item {
                PatientInfoCard()
            }

            // Gewichtsschätzung-Card
            item {
                WeightEstimationCard()
            }

            // Altersklassifikation-Card
            item {
                AgeClassificationCard()
            }

            // Medikamenten-Platzhalter
            item {
                MedicationPlaceholderCard()
            }

            // Gesten-Hinweis
            item {
                GestureHintCard()
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Patient",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = PatientDataManager.getPatientSummary(),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            val ageClassification = when {
                PatientDataManager.isInfant -> "👶 Säugling (0-12 Monate)"
                PatientDataManager.isChild -> "🧒 Kind (1-17 Jahre)"
                PatientDataManager.isAdult -> "👨 Erwachsener (18+ Jahre)"
                else -> "👤 Patient"
            }

            Text(
                text = ageClassification,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun WeightEstimationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gewichtsschätzung",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Text(
                text = "Aktuell: ${String.format("%.1f", PatientDataManager.estimatedWeightKg)} kg",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            val formula = when {
                PatientDataManager.ageYears == 0 -> "3 kg + (Monate × 0.7 kg)"
                PatientDataManager.ageYears in 1..5 -> "(Alter × 2) + 8 kg"
                PatientDataManager.ageYears in 6..12 -> "(Alter × 3) + 7 kg"
                PatientDataManager.ageYears in 13..17 -> "50 + (Alter - 13) × 5 kg"
                else -> "Standard Erwachsenengewicht"
            }

            Text(
                text = "Formel: $formula",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AgeClassificationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Altersklassifikation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val ageText = if (PatientDataManager.ageMonths > 0) {
                "${PatientDataManager.ageYears} Jahre, ${PatientDataManager.ageMonths} Monate"
            } else {
                "${PatientDataManager.ageYears} Jahre"
            }

            Text(
                text = "Alter: $ageText",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Gesamtmonate: ${PatientDataManager.totalAgeInMonths}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            val characteristics = when {
                PatientDataManager.isInfant -> listOf(
                    "• Obligate Nasenatmung bis 6 Monate",
                    "• Großer Kopf, kurzer Hals",
                    "• Atemwege sehr eng (4mm = massive Verlegung)",
                    "• Hypothermierisiko hoch"
                )
                PatientDataManager.isChild -> listOf(
                    "• Atemwege noch entwicklungsbedingt enger",
                    "• Gewichtsschätzung nach Formeln möglich",
                    "• Medikamentendosierung altersabhängig"
                )
                PatientDataManager.isAdult -> listOf(
                    "• Standard-Medikamentendosierung",
                    "• Berücksichtigung von Begleiterkrankungen",
                    "• Gewicht meist bekannt oder geschätzt"
                )
                else -> listOf("• Standardpatient")
            }

            Spacer(modifier = Modifier.height(8.dp))

            characteristics.forEach { characteristic ->
                Text(
                    text = characteristic,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun MedicationPlaceholderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Medikamentenberechnung",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hier wird der erweiterte Medikamentenrechner implementiert. Basierend auf der RDInfo-App Logik, aber mit Unterstützung für alle Einheiten.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "📋 Geplante Features:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val features = listOf(
                "• Dosierungsberechnung nach Alter/Gewicht",
                "• Unterstützung für alle Darreichungsformen",
                "• Automatische Maximaldosis-Kontrolle",
                "• Warnungen bei kritischen Dosierungen",
                "• Integration mit Patientendaten"
            )

            features.forEach { feature ->
                Text(
                    text = feature,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun GestureHintCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💡 Gesten-Navigation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "• Von oben wischen: Patientendaten eingeben\n• Von links wischen: SAMPLER-Schema\n• Von rechts wischen: xABCDE-Schema",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hinweis: Gesten-System wird in der nächsten Version aktiviert",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}