// app/src/main/java/com/rdinfo2/data/model/AlgorithmModels.kt
package com.rdinfo2.data.model

import kotlinx.serialization.Serializable

/**
 * Datenmodelle für Algorithmen und Flowcharts (xABCDE Schema)
 * Unterstützt interaktive Entscheidungsbäume und statische Referenzen
 */

@Serializable
data class Algorithm(
    val id: String,                           // "x", "a", "b", "c", "d", "e"
    val name: String,                         // "extreme bleeding", "Airway"
    val displayName: String,                  // "Extreme Blutung/Kritisch"
    val color: String = "#FF0000",            // Hex-Farbe für UI
    val icon: String? = null,                 // Icon-Name oder Path
    val priority: Int = 0,                    // Anzeigereihenfolge
    val subcategories: List<Subcategory> = emptyList(),
    val description: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class Subcategory(
    val id: String,                           // "a1", "a2", "b1", "x1.1"
    val name: String,                         // "Freimachen der Atemwege"
    val shortName: String? = null,            // Kurzer Name für UI
    val symptoms: List<String> = emptyList(), // Leitsymptome
    val flowchart: Flowchart? = null,         // Interaktiver Ablauf
    val staticContent: StaticContent? = null, // Alternative: statischer Inhalt
    val contraindications: List<String> = emptyList(),
    val timeLimit: Int? = null,               // Maximale Behandlungszeit (Minuten)
    val priority: Priority = Priority.NORMAL,
    val requiredEquipment: List<String> = emptyList(),
    val notes: String? = null
)

@Serializable
data class Flowchart(
    val id: String,
    val name: String,
    val startNodeId: String,                  // ID des ersten Knotens
    val nodes: List<FlowchartNode> = emptyList(),
    val version: Int = 1
)

@Serializable
data class FlowchartNode(
    val id: String,                           // Eindeutige Node-ID
    val type: NodeType,
    val title: String,                        // Kurztitel
    val content: String,                      // Hauptinhalt/Frage
    val position: NodePosition? = null,       // Position für Editor
    val connections: NodeConnections = NodeConnections(),
    val actions: List<NodeAction> = emptyList(),
    val conditions: List<NodeCondition> = emptyList(),
    val timeLimit: Int? = null,               // Zeitlimit in Sekunden
    val priority: Priority = Priority.NORMAL,
    val metadata: NodeMetadata? = null
)

@Serializable
enum class NodeType {
    START,           // Startknoten
    QUESTION,        // Ja/Nein Frage
    DECISION,        // Mehrfachauswahl
    ACTION,          // Maßnahme durchführen
    MEDICATION,      // Medikament verabreichen
    PROCEDURE,       // Invasive Maßnahme
    TIMER,           // Zeitbasierte Aktion
    REFERENCE,       // Verweis auf andere Bereiche
    END,             // Endpunkt
    PARALLEL         // Parallele Maßnahmen
}

@Serializable
data class NodePosition(
    val x: Float,
    val y: Float,
    val width: Float = 200f,
    val height: Float = 100f
)

@Serializable
data class NodeConnections(
    val yesPath: String? = null,              // Nächster Node bei "Ja"
    val noPath: String? = null,               // Nächster Node bei "Nein"
    val defaultPath: String? = null,          // Standard-Nächster Node
    val conditionalPaths: List<ConditionalPath> = emptyList() // Mehrfachauswahl
)

@Serializable
data class ConditionalPath(
    val condition: String,                    // Bedingung/Auswahltext
    val targetNodeId: String,                 // Ziel-Node
    val color: String? = null                 // Pfad-Farbe
)

@Serializable
data class NodeAction(
    val id: String,
    val type: ActionType,
    val description: String,
    val medicationId: String? = null,         // Referenz zu Medication
    val dosageRuleId: String? = null,         // Spezifische Dosierung
    val equipment: List<String> = emptyList(),
    val duration: Int? = null,                // Dauer in Sekunden
    val isRequired: Boolean = true,
    val alternatives: List<String> = emptyList()
)

@Serializable
enum class ActionType {
    MEDICATION,      // Medikament geben
    PROCEDURE,       // Prozedur durchführen
    MONITOR,         // Überwachung
    POSITION,        // Lagerung
    TRANSPORT,       // Transport
    CALL_BACKUP,     // Nachforderung
    DOCUMENTATION,   // Dokumentation
    OTHER            // Sonstige Maßnahme
}

@Serializable
data class NodeCondition(
    val type: ConditionType,
    val parameter: String,                    // Z.B. "age", "weight", "bp_systolic"
    val operator: String,                     // "<", ">", "==", "!=", "between"
    val value: String,                        // Vergleichswert oder Bereich
    val description: String                   // Menschenlesbare Beschreibung
)

@Serializable
enum class ConditionType {
    AGE,             // Altersbereich
    WEIGHT,          // Gewichtsbereich
    VITAL_SIGNS,     // Vitalparameter
    SYMPTOM,         // Symptom vorhanden/nicht vorhanden
    MEDICATION,      // Medikament bereits gegeben
    TIME,            // Zeitbasierte Bedingung
    USER_ROLE,       // NotSan vs Notarzt
    CUSTOM           // Benutzerdefiniert
}

@Serializable
data class NodeMetadata(
    val evidence: String? = null,             // Evidenzgrad
    val sources: List<String> = emptyList(),  // Quellen/Leitlinien
    val lastReview: Long? = null,             // Letzte Überprüfung
    val reviewBy: String? = null,             // Überprüft von
    val changeNotes: String? = null           // Änderungsnotizen
)

@Serializable
enum class Priority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL,
    IMMEDIATE        // Sofortige Maßnahme erforderlich
}

// Alternative zu Flowcharts: Statischer Content
@Serializable
data class StaticContent(
    val sections: List<ContentSection> = emptyList(),
    val images: List<String> = emptyList(),   // Bildpfade
    val tables: List<ReferenceTable> = emptyList(),
    val checkboxes: List<ChecklistItem> = emptyList()
)

@Serializable
data class ContentSection(
    val title: String,
    val content: String,
    val type: ContentType = ContentType.TEXT,
    val priority: Priority = Priority.NORMAL
)

@Serializable
enum class ContentType {
    TEXT,            // Fließtext
    LIST,            // Aufzählung
    WARNING,         // Warnung/Hinweis
    PROCEDURE,       // Verfahrensanweisung
    FORMULA,         // Berechnungsformel
    REFERENCE        // Referenz/Verweis
}

@Serializable
data class ChecklistItem(
    val id: String,
    val text: String,
    val isRequired: Boolean = false,
    val category: String? = null
)

@Serializable
data class Reference(
    val id: String,
    val title: String,
    val category: ReferenceCategory,
    val content: String,
    val tables: List<ReferenceTable> = emptyList(),
    val searchKeywords: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class ReferenceTable(
    val id: String,
    val name: String,
    val description: String? = null,
    val headers: List<String>,
    val rows: List<List<String>>,
    val searchable: Boolean = true,
    val sortable: Boolean = false,
    val category: String? = null
)

@Serializable
enum class ReferenceCategory {
    VITAL_SIGNS,     // Normalwerte
    PEDIATRIC,       // Pädiatrische Werte
    TOXICOLOGY,      // Vergiftungen
    EKG,             // EKG-Interpretation
    SCORING,         // Scores (GCS, APGAR, etc.)
    PROTOCOLS,       // Protokolle
    CALCULATIONS,    // Berechnungsformeln
    GUIDELINES,      // Leitlinien
    OTHER            // Sonstiges
}

// Spezielle Referenz-Typen
@Serializable
data class VitalSignsReference(
    val ageGroup: String,                     // "0-1 Jahre", "Erwachsene"
    val heartRate: IntRange?,                 // Herzfrequenz Bereich
    val bloodPressure: BPRange?,              // Blutdruck
    val respiratoryRate: IntRange?,           // Atemfrequenz
    val temperature: DoubleRange?,            // Körpertemperatur
    val oxygenSaturation: IntRange? = IntRange(95, 100)
)

@Serializable
data class BPRange(
    val systolic: IntRange,
    val diastolic: IntRange
)

@Serializable
data class DoubleRange(
    val min: Double,
    val max: Double
)

// Erweiterte Strukturen für spezielle Bereiche
@Serializable
data class SepsisScore(
    val qSOFA: QSOFAScore,
    val newsScore: NEWSScore? = null
)

@Serializable
data class QSOFAScore(
    val alteredMentation: Boolean,            // GCS < 15
    val systolicBP: Boolean,                  // ≤ 100 mmHg
    val respiratoryRate: Boolean              // ≥ 22/min
) {
    val score: Int get() = listOf(alteredMentation, systolicBP, respiratoryRate).count { it }
    val isPositive: Boolean get() = score >= 2
}

@Serializable
data class NEWSScore(
    val respiratoryRate: Int,
    val oxygenSaturation: Int,
    val temperature: Double,
    val systolicBP: Int,
    val heartRate: Int,
    val levelOfConsciousness: String
) {
    // NEWS Scoring Logik würde hier implementiert
}