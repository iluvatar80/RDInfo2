// app/src/main/java/com/rdinfo2/ui/components/overlays/SamplerOverlay.kt
package com.rdinfo2.ui.components.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rdinfo2.ui.theme.EmergencyShapes
import com.rdinfo2.ui.theme.EmergencyTypography

/**
 * SAMPLER-Schema Overlay für Anamnese-Gedankenstütze
 * Wird durch Wischen von links nach rechts eingeblendet
 */
@Composable
fun SamplerOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(350.dp)
            .fillMaxHeight()
            .clip(EmergencyShapes.SidePanelLeft),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAMPLER",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Schließen"
                    )
                }
            }

            Text(
                text = "Anamnese-Schema",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SAMPLER content
            SamplerContent()
        }
    }
}

@Composable
fun SamplerContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val samplerItems = getSamplerItems()

        samplerItems.forEach { item ->
            SamplerItem(
                letter = item.letter,
                title = item.title,
                description = item.description,
                details = item.details,
                color = item.color
            )
        }

        // Bottom spacer for gesture area
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SamplerItem(
    letter: String,
    title: String,
    description: String,
    details: List<String>,
    color: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Letter circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Einklappen" else "Ausklappen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable details
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    details.forEach { detail ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = EmergencyTypography.InstructionText,
                                color = color
                            )
                            Text(
                                text = detail,
                                style = EmergencyTypography.InstructionText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getSamplerItems(): List<SamplerItemData> {
    return listOf(
        SamplerItemData(
            letter = "S",
            title = "Symptome/Schmerzen",
            description = "Art und Verlauf der Beschwerden",
            details = listOf(
                "Onset: Beginn der Beschwerden (wann und wie)",
                "Palliation/Provocation: Einflüsse zur Linderung/Verschlimmerung",
                "Quality: Art/Qualität der Beschwerden",
                "Radiation: Lokalisation/Ausstrahlung",
                "Severity: Stärke (NRS 0-10)",
                "Time: Verlauf/Dauer der Beschwerden"
            ),
            color = androidx.compose.ui.graphics.Color(0xFFE53935) // Red
        ),
        SamplerItemData(
            letter = "A",
            title = "Allergien",
            description = "Bekannte Allergien und Unverträglichkeiten",
            details = listOf(
                "Allergenkontakt?",
                "Allergiepass vorhanden?",
                "Medikamentenallergien?",
                "Unverträglichkeiten?",
                "Art der allergischen Reaktion",
                "Zeitpunkt der letzten Reaktion"
            ),
            color = androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
        ),
        SamplerItemData(
            letter = "M",
            title = "Medikamente",
            description = "Aktuelle und regelmäßige Medikation",
            details = listOf(
                "Dauermedikation (Medikamentenplan)?",
                "Eingenommen wie verordnet?",
                "Neuverordnungen?",
                "Eigenmedikation?",
                "BTM-pflichtige Medikamente?",
                "Letzte Einnahme von Medikamenten"
            ),
            color = androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        ),
        SamplerItemData(
            letter = "P",
            title = "Patientenvorgeschichte",
            description = "Vorerkrankungen und medizinische Historie",
            details = listOf(
                "Vorerkrankungen, Folgeschäden",
                "Krankenhausaufenthalte/Operationen",
                "Patientenausweis (Schrittmacher-, Marcumar-, Mutterpass)",
                "Arztbriefe vorhanden?",
                "Infektionen/Kolonisation? (HIV, HCV, MRSA, MRGN)",
                "Familienanamnese bei relevanten Erkrankungen"
            ),
            color = androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
        ),
        SamplerItemData(
            letter = "L",
            title = "Letzte Nahrungsaufnahme",
            description = "Zeitpunkt und Art der letzten Nahrung",
            details = listOf(
                "Letzte Nahrungsaufnahme (Zeitpunkt und Bestandteile)",
                "Flüssigkeitsaufnahme",
                "Stuhlgang/Urinausscheidung",
                "Regelblutung (bei Frauen)",
                "Nüchternheit vor geplanten Eingriffen",
                "Besondere Diäten oder Ernährungsformen"
            ),
            color = androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
        ),
        SamplerItemData(
            letter = "E",
            title = "Ereignisse",
            description = "Umstände vor den Beschwerden",
            details = listOf(
                "Unfallmechanismus oder Tätigkeit kurz vor Beschwerdebeginn",
                "Begleitumstände",
                "Stress-Situationen",
                "In Ruhe aufgetreten?",
                "Auslösende Faktoren",
                "Zeugen des Ereignisses"
            ),
            color = androidx.compose.ui.graphics.Color(0xFF607D8B) // Blue Grey
        ),
        SamplerItemData(
            letter = "R",
            title = "Risikofaktoren",
            description = "Individuelle und soziale Risikofaktoren",
            details = listOf(
                "Rauchen, Alkohol-/Drogenkonsum",
                "Berufliche Situation",
                "Familiäre Belastung",
                "Gewichtsabnahme/-zunahme",
                "Schwangerschaft",
                "Diabetes mellitus",
                "Weitere kardiovaskuläre Risikofaktoren"
            ),
            color = androidx.compose.ui.graphics.Color(0xFFFF5722) // Deep Orange
        )
    )
}

private data class SamplerItemData(
    val letter: String,
    val title: String,
    val description: String,
    val details: List<String>,
    val color: androidx.compose.ui.graphics.Color
)