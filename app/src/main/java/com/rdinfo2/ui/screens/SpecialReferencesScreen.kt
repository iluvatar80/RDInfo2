// app/src/main/java/com/rdinfo2/ui/screens/SpecialReferencesScreen.kt
package com.rdinfo2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rdinfo2.data.patient.PatientDataManager

@Composable
fun SpecialReferencesScreen() {
    var selectedReference by remember { mutableStateOf<SpecialReference?>(null) }

    selectedReference?.let { reference ->
        SpecialReferenceDetailView(
            reference = reference,
            onBack = { selectedReference = null }
        )
    } ?: SpecialReferenceOverview(
        onReferenceSelected = { selectedReference = it }
    )
}

@Composable
private fun SpecialReferenceOverview(
    onReferenceSelected: (SpecialReference) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Spezielle Nachschlagewerke",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "Strukturierte Hilfsmittel für die Notfallmedizin",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(getSpecialReferences()) { reference ->
            SpecialReferenceCard(
                reference = reference,
                onClick = { onReferenceSelected(reference) }
            )
        }
    }
}

@Composable
private fun SpecialReferenceCard(
    reference: SpecialReference,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = reference.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = reference.icon,
                    contentDescription = null,
                    tint = reference.color,
                    modifier = Modifier.size(32.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reference.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = reference.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Öffnen",
                    tint = reference.color
                )
            }

            if (reference.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    reference.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = reference.color.copy(alpha = 0.2f),
                                labelColor = reference.color
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpecialReferenceDetailView(
    reference: SpecialReference,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(reference.title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Zurück")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = reference.color,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (reference.id) {
                "sepsis" -> items(getSepsisContent()) { content -> ReferenceContentCard(content) }
                "isobar" -> items(getIsobarContent()) { content -> ReferenceContentCard(content) }
                "gpstart" -> items(getGpStartContent()) { content -> ReferenceContentCard(content) }
                "toxidrome" -> items(getToxidromeContent()) { content -> ReferenceContentCard(content) }
                "ekg" -> items(getEkgContent()) { content -> ReferenceContentCard(content) }
                "trauma" -> items(getTraumaContent()) { content -> ReferenceContentCard(content) }
                "pediatric" -> items(getPediatricContent()) { content -> ReferenceContentCard(content) }
                "obstetric" -> items(getObstetricContent()) { content -> ReferenceContentCard(content) }
                else -> item {
                    Card {
                        Text(
                            text = "Inhalt wird noch implementiert",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceContentCard(content: ReferenceContent) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (content.type) {
                "warning" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                "info" -> MaterialTheme.colorScheme.primaryContainer
                "checklist" -> MaterialTheme.colorScheme.secondaryContainer
                "table" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (content.title.isNotBlank()) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (content.type) {
                        "warning" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            when (content.type) {
                "checklist" -> {
                    content.items.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("☐", style = MaterialTheme.typography.bodyMedium)
                            Text(item, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                "table" -> {
                    content.items.forEach { item ->
                        val parts = item.split("|")
                        if (parts.size >= 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = parts[0].trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = parts[1].trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text(item, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                else -> {
                    if (content.text.isNotBlank()) {
                        Text(content.text, style = MaterialTheme.typography.bodyMedium)
                    }
                    content.items.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// Data classes for special references
data class SpecialReference(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val tags: List<String> = emptyList()
)

data class ReferenceContent(
    val title: String = "",
    val text: String = "",
    val items: List<String> = emptyList(),
    val type: String = "default" // warning, info, checklist, table
)

private fun getSpecialReferences(): List<SpecialReference> {
    return listOf(
        SpecialReference(
            id = "sepsis",
            title = "SEPSIS-Score",
            description = "Sepsis erkennen und bewerten",
            icon = Icons.Default.LocalHospital,
            color = Color(0xFFE53E3E),
            tags = listOf("qSOFA", "SIRS", "Infekt")
        ),
        SpecialReference(
            id = "isobar",
            title = "ISOBAR-Schema",
            description = "Strukturierte Patientenübergabe",
            icon = Icons.Default.RecordVoiceOver,
            color = Color(0xFF3182CE),
            tags = listOf("Übergabe", "Kommunikation", "SAMPLER")
        ),
        SpecialReference(
            id = "gpstart",
            title = "GP-START",
            description = "Sichtung und Triage",
            icon = Icons.Default.Sort,
            color = Color(0xFFD69E2E),
            tags = listOf("Triage", "Massenanfall", "Sichtung")
        ),
        SpecialReference(
            id = "toxidrome",
            title = "Toxidrome",
            description = "Vergiftungsmerkmale erkennen",
            icon = Icons.Default.Warning,
            color = Color(0xFF805AD5),
            tags = listOf("Vergiftung", "Anticholinerg", "Sympathomimetisch")
        ),
        SpecialReference(
            id = "ekg",
            title = "EKG-Interpretation",
            description = "Hochrisiko-EKG erkennen",
            icon = Icons.Default.MonitorHeart,
            color = Color(0xFFE53E3E),
            tags = listOf("STEMI", "Rhythmus", "Herzinfarkt")
        ),
        SpecialReference(
            id = "trauma",
            title = "Trauma-Assessment",
            description = "ATLS und Traumaversorgung",
            icon = Icons.Default.LocalHospital,
            color = Color(0xFF38A169),
            tags = listOf("ATLS", "Polytrauma", "Schockraum")
        ),
        SpecialReference(
            id = "pediatric",
            title = "Pädiatrisches Dreieck",
            description = "Kindernotfälle beurteilen",
            icon = Icons.Default.ChildCare,
            color = Color(0xFFDD6B20),
            tags = listOf("Kinder", "Beurteilung", "Aussehen")
        ),
        SpecialReference(
            id = "obstetric",
            title = "Geburtshilfe",
            description = "Notfallgeburt und Komplikationen",
            icon = Icons.Default.PregnantWoman,
            color = Color(0xFFD53F8C),
            tags = listOf("Geburt", "Schwangerschaft", "Notfall")
        )
    )
}

private fun getSepsisContent(): List<ReferenceContent> {
    return listOf(
        ReferenceContent(
            title = "qSOFA-Score (Quick Sequential Organ Failure Assessment)",
            text = "Schnelle Bewertung des Sepsisrisikos. Bei ≥2 Punkten: hohes Sepsisrisiko.",
            type = "info"
        ),
        ReferenceContent(
            title = "qSOFA-Kriterien (je 1 Punkt)",
            items = listOf(
                "Atemfrequenz ≥ 22/min",
                "Bewusstsein verändert (GCS < 15)",
                "Systolischer RR ≤ 100 mmHg"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "SIRS-Kriterien (Systemic Inflammatory Response Syndrome)",
            text = "≥2 Kriterien = SIRS. SIRS + Infektion = Sepsis.",
            type = "info"
        ),
        ReferenceContent(
            title = "SIRS-Kriterien",
            items = listOf(
                "Temperatur >38°C oder <36°C | Fieber/Hypothermie",
                "Herzfrequenz >90/min | Tachykardie",
                "Atemfrequenz >20/min | Tachypnoe",
                "Leukozyten >12.000/µl oder <4.000/µl | Leukozytose/-penie"
            ),
            type = "table"
        ),
        ReferenceContent(
            title = "⚠️ Red Flags bei Sepsis",
            items = listOf(
                "Laktat > 2 mmol/l",
                "Hypotension trotz Volumengabe",
                "Oligurie < 0,5 ml/kg/h",
                "Thrombozytopenie",
                "Bewusstseinstrübung"
            ),
            type = "warning"
        ),
        ReferenceContent(
            title = "Sofortmaßnahmen",
            items = listOf(
                "Blutkulturen vor Antibiotikatherapie",
                "Breitspektrum-Antibiotikum binnen 1h",
                "Volumengabe bei Hypotension",
                "Vasopressoren bei refraktärer Hypotension",
                "Frühzeitige Intensivtherapie"
            ),
            type = "checklist"
        )
    )
}

private fun getIsobarContent(): List<ReferenceContent> {
    return listOf(
        ReferenceContent(
            title = "ISOBAR-Schema für Patientenübergabe",
            text = "Strukturierte Kommunikation zur Vermeidung von Informationsverlusten.",
            type = "info"
        ),
        ReferenceContent(
            title = "I - Identifikation",
            items = listOf(
                "Patient: Name, Alter, Geschlecht",
                "Übergeber: Name, Funktion",
                "Zeit und Ort der Übernahme"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "S - Situation",
            items = listOf(
                "Aktueller Zustand des Patienten",
                "Warum ist der Patient hier?",
                "Was ist das Problem?"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "O - Observation",
            items = listOf(
                "Vitalzeichen (RR, Puls, SpO2, Temp, BZ)",
                "Bewusstseinslage (GCS)",
                "Schmerzen (NRS 0-10)",
                "Weitere relevante Befunde"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "B - Background",
            items = listOf(
                "Vorgeschichte/Anamnese",
                "Vorerkrankungen",
                "Aktuelle Medikation",
                "Allergien/Unverträglichkeiten"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "A - Assessment",
            items = listOf(
                "Arbeitshypothese/Verdachtsdiagnose",
                "Durchgeführte Maßnahmen",
                "Wirksamkeit der Therapie",
                "Aktuelle Risiken"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "R - Recommendation",
            items = listOf(
                "Empfohlenes weiteres Vorgehen",
                "Prioritäten setzen",
                "Besondere Überwachung",
                "Rückfragen beantworten"
            ),
            type = "checklist"
        )
    )
}

private fun getGpStartContent(): List<ReferenceContent> {
    return listOf(
        ReferenceContent(
            title = "GP-START Triage-System",
            text = "Strukturierte Sichtung bei Massenanfall von Verletzten (MANV).",
            type = "info"
        ),
        ReferenceContent(
            title = "Erste Sichtung (30 Sekunden pro Patient)",
            items = listOf(
                "Kann der Patient gehen? → T4 (Grün)",
                "Atmet der Patient? → Nein: Tot oder T1",
                "Atemfrequenz ≤30/min? → Ja: weiter",
                "Radialispuls tastbar? → Nein: T1 (Rot)",
                "Befolgt einfache Befehle? → Nein: T1 (Rot)"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "Triage-Kategorien",
            items = listOf(
                "T1 (Rot) | Höchste Priorität, sofortige Behandlung",
                "T2 (Gelb) | Dringende Behandlung, kann warten",
                "T3 (Grün) | Spätere Behandlung möglich",
                "T4 (Blau) | Gehfähige Verletzte",
                "T5 (Schwarz) | Verstorben oder hoffnungslos"
            ),
            type = "table"
        ),
        ReferenceContent(
            title = "⚠️ Besondere Beachtung",
            items = listOf(
                "Kinder haben oft bessere Kompensation",
                "Schwangere ab 20. SSW → automatisch T1",
                "Verbrennungen >20% KOF → T1",
                "Re-Triage nach Erstversorgung",
                "Dokumentation auf Sichtungsanhänger"
            ),
            type = "warning"
        )
    )
}

private fun getToxidromeContent(): List<ReferenceContent> {
    return listOf(
        ReferenceContent(
            title = "Toxidrome - Vergiftungsbilder",
            text = "Charakteristische Symptomkomplexe bei Vergiftungen zur schnellen Orientierung.",
            type = "info"
        ),
        ReferenceContent(
            title = "Sympathomimetisches Toxidrom",
            items = listOf(
                "Substanzen: Amphetamine, Kokain, Ecstasy",
                "Mydriasis (weite Pupillen)",
                "Tachykardie, Hypertonie",
                "Hyperthermie, Diaphorese",
                "Agitiertheit, Halluzinationen",
                "Tremor, Hyperreflexie"
            ),
            type = "info"
        ),
        ReferenceContent(
            title = "Anticholinerges Toxidrom",
            text = "'Hot as a hare, blind as a bat, dry as a bone, red as a beet, mad as a hatter'",
            type = "info"
        ),
        ReferenceContent(
            title = "Anticholinerge Symptome",
            items = listOf(
                "Substanzen: Atropin, Scopolamin, Tollkirsche",
                "Mydriasis, verschwommenes Sehen",
                "Tachykardie, Hypertonie",
                "Hyperthermie, trockene Haut",
                "Verwirrtheit, Halluzinationen",
                "Harnverhalt, Darmatonie"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "Cholinerges Toxidrom",
            text = "MUDDLES: Miosis, Urination, Defecation, Diaphoresis, Lacrimation, Emesis, Salivation",
            type = "info"
        ),
        ReferenceContent(
            title = "Opiat-Toxidrom",
            items = listOf(
                "Substanzen: Morphin, Heroin, Fentanyl",
                "Miosis (Stecknadelkopf-Pupillen) | Engste Pupillen",
                "Atemdepression | <12/min, flach",
                "Bewusstseinstrübung | Somnolenz bis Koma",
                "Bradykardie, Hypotonie | Kreislaufdepression",
                "Verminderte Darmgeräusche | Obstipation"
            ),
            type = "table"
        ),
        ReferenceContent(
            title = "⚠️ Antidote - Gegenmittel",
            items = listOf(
                "Opiate → Naloxon (Narcanti) 0,4-2mg i.v.",
                "Benzodiazepine → Flumazenil 0,2mg i.v.",
                "Anticholinergika → Physostigmin 1-2mg i.v.",
                "CO-Vergiftung → 100% O2, HBO-Therapie",
                "Paracetamol → N-Acetylcystein"
            ),
            type = "warning"
        )
    )
}

private fun getEkgContent(): List<ReferenceContent> {
    return listOf(
        ReferenceContent(
            title = "EKG-Interpretation Notfall",
            text = "Systematische Beurteilung lebensbedrohlicher EKG-Veränderungen.",
            type = "info"
        ),
        ReferenceContent(
            title = "STEMI-Kriterien (ST-Hebungsinfarkt)",
            items = listOf(
                "ST-Hebung ≥1mm in ≥2 zusammenhängenden Ableitungen",
                "V1-V6: ≥2mm ST-Hebung (anterior)",
                "II, III, aVF: ≥1mm ST-Hebung (inferior)",
                "I, aVL: ≥1mm ST-Hebung (lateral)",
                "Neu aufgetretener Linksschenkelblock = STEMI-Äquivalent"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "Gefährliche Rhythmusstörungen",
            items = listOf(
                "Ventrikuläre Tachykardie | >100/min, breite QRS",
                "Kammerflimmern | Chaotische Erregung",
                "AV-Block III° | P und QRS unabhängig",
                "Bradykardie <50/min | Mit Hypotension",
                "Sick-Sinus-Syndrom | Wechselnde Rhythmen"
            ),
            type = "table"
        ),
        ReferenceContent(
            title = "⚠️ Sofortmaßnahmen bei Rhythmusstörungen",
            items = listOf(
                "VT/VF → Sofortige Defibrillation",
                "AV-Block III° → Atropin, Schrittmacher",
                "Bradykardie symptomatisch → Atropin 0,5mg",
                "Hyperkaliämie-Zeichen → Calcium, Insulin/Glucose",
                "Digitalis-Intox → Digitalis-Antidot erwägen"
            ),
            type = "warning"
        )
    )
}

private fun getTraumaContent(): List<ReferenceContent> {
    return listOf(
        ReferenceContent(
            title = "ATLS-Algorithmus",
            text = "Advanced Trauma Life Support - systematische Traumaversorgung.",
            type = "info"
        ),
        ReferenceContent(
            title = "Primary Survey (ABCDE)",
            items = listOf(
                "A - Airway (Atemweg sichern, HWS-Schutz)",
                "B - Breathing (Beatmung, Thorax)",
                "C - Circulation (Kreislauf, Blutung)",
                "D - Disability (Neurologie, Wirbelsäule)",
                "E - Exposure (Entkleiden, Umgebung)"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "Schockarten beim Trauma",
            items = listOf(
                "Hämorrhagischer Schock | Blutverlust >20%",
                "Neurogener Schock | Rückenmarkverletzung",
                "Kardiogener Schock | Herztrauma",
                "Spannungspneumothorax | Mediastinalshift",
                "Herzbeuteltamponade | Perikarderguss"
            ),
            type = "table"
        ),
        ReferenceContent(
            title = "⚠️ Damage Control",
            items = listOf(
                "Permissive Hypotonie (RR sys 80-90 mmHg)",
                "Massive Transfusion 1:1:1 (EK:FFP:TK)",
                "Hypothermie vermeiden (>35°C)",
                "Azidose korrigieren (pH >7,2)",
                "Koagulopathie behandeln (Tranexamsäure)"
            ),
            type = "warning"
        )
    )
}

private fun getPediatricContent(): List<ReferenceContent> {
    return listOf(
        ReferenceContent(
            title = "Pädiatrisches Beurteilungsdreieck",
            text = "Schnelle Einschätzung des Schweregrades bei Kindern binnen 30 Sekunden.",
            type = "info"
        ),
        ReferenceContent(
            title = "1. Aussehen (Appearance)",
            items = listOf(
                "Interaktion mit Umgebung",
                "Blickkontakt zu Bezugsperson",
                "Trostbarkeit",
                "Sprache/Schrei",
                "Spontanmotorik"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "2. Atemarbeit (Work of Breathing)",
            items = listOf(
                "Einziehungen (interkostal, jugulär)",
                "Nasenflügeln",
                "Kopfbewegungen bei Atmung",
                "Stridor/Giemen hörbar",
                "Atemfrequenz altersentsprechend"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "3. Durchblutung (Circulation)",
            items = listOf(
                "Hautfarbe | Blass, zyanotisch, marmoriert",
                "Rekapillarisierungszeit | >2s pathologisch",
                "Puls | Tastbar, Frequenz, Qualität"
            ),
            type = "table"
        ),
        ReferenceContent(
            title = "⚠️ Pädiatrische Red Flags",
            items = listOf(
                "Trinkverweigerung bei Säuglingen",
                "Teilnahmslosigkeit, schwer tröstbar",
                "Atemfrequenz >60/min oder <20/min",
                "Fieber >38°C bei <3 Monaten",
                "Petechiale Blutungen (Meningitis?)"
            ),
            type = "warning"
        )
    )
}

private fun getObstetricContent(): List<ReferenceContent> {
    return listOf(
        ReferenceContent(
            title = "Notfallgeburt - Vorbereitung",
            text = "Wenn Transport nicht mehr möglich ist oder keine Zeit für Klinikaufnahme.",
            type = "info"
        ),
        ReferenceContent(
            title = "Geburtsphasen",
            items = listOf(
                "Eröffnungsperiode | Muttermund 0-10cm",
                "Austreibungsperiode | Geburt des Kindes",
                "Nachgeburtsperiode | Plazenta-Geburt"
            ),
            type = "table"
        ),
        ReferenceContent(
            title = "Ausrüstung für Notfallgeburt",
            items = listOf(
                "Sterile Handschuhe, Tücher",
                "Nabelklemmen oder sterile Schnur",
                "Sterile Schere",
                "Absaugung bereithalten",
                "Wärmeerhaltung für Neugeborenes"
            ),
            type = "checklist"
        ),
        ReferenceContent(
            title = "Komplikationen bei der Geburt",
            items = listOf(
                "Nabelschnurvorfall | Notfall! → Steißhochlagerung",
                "Schulterdystokie | McRoberts-Manöver",
                "Postpartale Blutung | Fundus-Massage, Oxytocin",
                "Präeklampsie | RR >140/90, Proteinurie, Ödeme",
                "Eklampsie | Krampfanfälle → Magnesium i.v."
            ),
            type = "warning"
        ),
        ReferenceContent(
            title = "Neugeborenenversorgung",
            items = listOf(
                "APGAR-Score nach 1, 5, 10 min",
                "Absaugen nur bei Bedarf",
                "Wärmeerhaltung (Hautkontakt)",
                "Nabelschnur erst nach Pulsation durchtrennen",
                "Atmung stimulieren durch Reiben"
            ),
            type = "checklist"
        )
    )
}