// app/src/main/java/com/rdinfo2/ui/components/overlays/XABCDEOverlay.kt
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rdinfo2.ui.theme.EmergencyColors
import com.rdinfo2.ui.theme.EmergencyShapes
import com.rdinfo2.ui.theme.EmergencyTypography

/**
 * xABCDE-Schema Overlay für Algorithmus-Navigation
 * Wird durch Wischen von rechts nach links eingeblendet
 */
@Composable
fun XABCDEOverlay(
    onDismiss: () -> Unit,
    onNavigateToAlgorithm: (algorithmId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(380.dp)
            .fillMaxHeight()
            .clip(EmergencyShapes.SidePanelRight),
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
                    text = "xABCDE",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Schließen"
                    )
                }
            }

            Text(
                text = "Systematische Patientenversorgung",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // xABCDE content
            XABCDEContent(onNavigateToAlgorithm = onNavigateToAlgorithm)
        }
    }
}

@Composable
fun XABCDEContent(onNavigateToAlgorithm: (algorithmId: String) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val xabcdeItems = getXABCDEItems()

        xabcdeItems.forEach { item ->
            XABCDEItem(
                item = item,
                onNavigateToAlgorithm = onNavigateToAlgorithm
            )
        }

        // Bottom spacer for gesture area
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun XABCDEItem(
    item: XABCDEItemData,
    onNavigateToAlgorithm: (algorithmId: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = item.color.copy(alpha = 0.1f)
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
                            .size(40.dp)
                            .background(item.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.letter,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Einklappen" else "Ausklappen",
                    tint = item.color
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(
                        color = item.color.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    // Quick assessment points
                    Text(
                        text = "Schnellbeurteilung:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = item.color
                    )

                    item.assessmentPoints.forEach { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = EmergencyTypography.InstructionText,
                                color = item.color
                            )
                            Text(
                                text = point,
                                style = EmergencyTypography.InstructionText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Subcategories/Algorithms
                    if (item.subcategories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Algorithmen:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = item.color
                        )

                        item.subcategories.forEach { subcat ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onNavigateToAlgorithm(subcat.id)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = item.color.copy(alpha = 0.05f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subcat.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (subcat.symptoms.isNotEmpty()) {
                                            Text(
                                                text = subcat.symptoms.joinToString(", "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Algorithmus öffnen",
                                        tint = item.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getXABCDEItems(): List<XABCDEItemData> {
    return listOf(
        XABCDEItemData(
            letter = "x",
            title = "Extreme bleeding/kritisch",
            description = "Lebensbedrohliche Zustände (ca. 10s)",
            color = EmergencyColors.CriticalRed,
            assessmentPoints = listOf(
                "Blick über den Patienten, Lebenszeichen",
                "Reaktion auf Ansprache",
                "Sichtbare lebensbedrohliche Verletzung",
                "Dyspnoe, Hautkolorit (Zyanose/kalter Schweiß)",
                "Rekapillarisierungszeit > 2s?",
                "Puls schwach tastbar/langsam/schnell?"
            ),
            subcategories = listOf(
                SubcategoryData("x1.1", "Kreislaufstillstand Erwachsene", listOf("Bewusstlosigkeit", "keine Atmung")),
                SubcategoryData("x1.2", "Traumatisch bedingter Kreislaufstillstand", listOf("Trauma", "Kreislaufstillstand")),
                SubcategoryData("x2.1", "Kreislaufstillstand Kinder", listOf("Kind", "Bewusstlosigkeit")),
                SubcategoryData("x2.2", "Neugeborenen-Versorgung", listOf("Geburt", "Anpassungsstörung")),
                SubcategoryData("x3", "Rückkehr des Spontankreislaufs (ROSC)", listOf("nach Reanimation")),
                SubcategoryData("x4", "Lebensbedrohliche Blutungen", listOf("massive Blutung"))
            )
        ),
        XABCDEItemData(
            letter = "A",
            title = "Airway",
            description = "Atemweg - obere Atemwege frei?",
            color = EmergencyColors.AirwayBlue,
            assessmentPoints = listOf(
                "Sprechen möglich?",
                "Inspiratorischer Stridor?",
                "Inspektion des Mund-Rachenraumes",
                "Fremdkörper sichtbar?",
                "Schwellung/Ödem erkennbar?"
            ),
            subcategories = listOf(
                SubcategoryData("a1", "Freimachen der Atemwege", listOf("Verlegung", "Fremdkörper", "Sekret")),
                SubcategoryData("a2", "Schwellung der oberen Atemwege", listOf("Stridor", "Anaphylaxie", "Angioödem"))
            )
        ),
        XABCDEItemData(
            letter = "B",
            title = "Breathing",
            description = "Belüftung/Atmung - ausreichende Oxygenierung?",
            color = EmergencyColors.BreathingGreen,
            assessmentPoints = listOf(
                "SpO₂-Messung",
                "Inspektion, Atemfrequenz",
                "Palpation des Thorax",
                "Auskultation beidseitig",
                "Atemhilfsmuskulatur?",
                "Einziehungen?"
            ),
            subcategories = listOf(
                SubcategoryData("b1", "Spannungspneumothorax", listOf("einseitig fehlendes Atemgeräusch", "Schock")),
                SubcategoryData("b2", "Akute Linksherzinsuffizienz", listOf("Rasselgeräusche", "Dyspnoe")),
                SubcategoryData("b3", "Obstruktion der unteren Atemwege", listOf("Giemen", "Asthma", "COPD")),
                SubcategoryData("b4", "Atemdepression durch Opiate", listOf("Bewusstseinsstörung", "flache Atmung")),
                SubcategoryData("b5", "Patient mit Heimbeatmung", listOf("Tracheostoma", "Beatmungsgerät"))
            )
        ),
        XABCDEItemData(
            letter = "C",
            title = "Circulation",
            description = "Kreislauf - Schock? Blutungen?",
            color = EmergencyColors.CirculationOrange,
            assessmentPoints = listOf(
                "Bedrohliche Blutung nach außen?",
                "Rekapillarisierungszeit, Puls, Hautkolorit",
                "Bedrohliche Blutung nach innen?",
                "Palpation: Abdomen, Oberschenkel",
                "Blutdruck (ggf. bds.), EKG"
            ),
            subcategories = listOf(
                SubcategoryData("c1", "Relativer Volumenmangel", listOf("Schock", "warme Haut")),
                SubcategoryData("c2", "Absoluter Volumenmangel", listOf("Schock", "kalte Haut", "Blutverlust")),
                SubcategoryData("c3", "Beckentrauma", listOf("Instabilität", "Schmerzen")),
                SubcategoryData("c4", "Symptomatische Bradykardie", listOf("HF < 50", "Bewusstseinsstörung")),
                SubcategoryData("c5", "Symptomatische Tachykardie", listOf("HF > 150", "Bewusstseinsstörung")),
                SubcategoryData("c6", "Hypertensiver Notfall", listOf("Bluthochdruck", "Organschäden")),
                SubcategoryData("c7", "ACS", listOf("Thoraxschmerz", "EKG-Veränderungen")),
                SubcategoryData("c9", "Akutes Aortensyndrom", listOf("Thoraxschmerz", "Blutdruckdifferenz"))
            )
        ),
        XABCDEItemData(
            letter = "D",
            title = "Disability",
            description = "Bewusstsein/neurologisches Defizit",
            color = EmergencyColors.DisabilityPurple,
            assessmentPoints = listOf(
                "BZ-Kontrolle",
                "Glasgow-Coma-Scale (GCS)",
                "Pupillenkontrolle",
                "MANKO (Mund-Augen-Nase-Kopf-Ohren)",
                "FAST-Test (Face-Arms-Speech-Time)",
                "Motorik, Sensibilität"
            ),
            subcategories = listOf(
                SubcategoryData("d1", "Cerebraler Krampfanfall", listOf("Krämpfe", "Bewusstseinsstörung")),
                SubcategoryData("d2", "Symptomatische Hypoglykämie", listOf("niedriger BZ", "Verwirrtheit")),
                SubcategoryData("d3", "Schlaganfall", listOf("FAST positiv", "neurologische Ausfälle"))
            )
        ),
        XABCDEItemData(
            letter = "E",
            title = "Exposure/Examination",
            description = "Erweiterte Versorgung - weitere Verletzungen?",
            color = EmergencyColors.ExposureYellow,
            assessmentPoints = listOf(
                "Gezielte Entkleidung & Untersuchung",
                "DMS an verletzter/erkrankter Extremität",
                "Körpertemperatur",
                "SAMPLER-Anamnese vervollständigen",
                "Weitere Verletzungen/Erkrankungen?"
            ),
            subcategories = listOf(
                SubcategoryData("e1", "Geburt", listOf("Wehentätigkeit", "Pressdrang")),
                SubcategoryData("e3", "Kontakt mit Flusssäure", listOf("Verätzung", "spezielle Dekontamination")),
                SubcategoryData("e4", "Starke Schmerzen (NRS ≥ 4)", listOf("Analgesie erforderlich")),
                SubcategoryData("e5", "Extremitätenfraktur mit grober Fehlstellung", listOf("Dislokation", "DMS-Defizit")),
                SubcategoryData("e6", "Kontakt mit infektiösem Material", listOf("Nadelstichverletzung", "Exposition")),
                SubcategoryData("e8", "Hyperthermie", listOf("erhöhte Körpertemperatur")),
                SubcategoryData("e9", "Hypothermie", listOf("erniedrigte Körpertemperatur")),
                SubcategoryData("e10", "Verbrennung/Verbrühung", listOf("thermische Verletzung"))
            )
        )
    )
}

private data class XABCDEItemData(
    val letter: String,
    val title: String,
    val description: String,
    val color: androidx.compose.ui.graphics.Color,
    val assessmentPoints: List<String>,
    val subcategories: List<SubcategoryData>
)

private data class SubcategoryData(
    val id: String,
    val name: String,
    val symptoms: List<String>
)