// app/src/main/java/com/rdinfo2/data/model/MedicationModels.kt
package com.rdinfo2.data.model

import kotlinx.serialization.Serializable

/**
 * Basis-Datenmodelle für RDInfo2 App
 * Unterstützt alle Darreichungsformen: Ampullen, Trockensubstanzen, Sprays, Infusionen, etc.
 */

@Serializable
data class RDInfoDatabase(
    val version: String = "1.0.0",
    val lastUpdate: Long = System.currentTimeMillis(),
    val medications: List<Medication> = emptyList(),
    val algorithms: List<Algorithm> = emptyList(),
    val references: List<Reference> = emptyList()
)

@Serializable
data class Medication(
    val id: String,                           // "adrenalin", "acetylsalicylsaeure"
    val name: String,                         // "Adrenalin", "Acetylsalicylsäure"
    val brandNames: List<String> = emptyList(), // "Suprarenin", "Aspirin"
    val unit: MedicationUnit,                 // Flexible Einheitendefinition
    val forms: List<MedicationForm> = emptyList(), // Verschiedene Darreichungsformen
    val dosingRules: List<DosingRule> = emptyList(),
    val info: MedicationInfo? = null,
    val category: MedicationCategory = MedicationCategory.OTHER,
    val btmStatus: BTMStatus? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class MedicationUnit(
    val baseUnit: String,                     // "mg", "g", "µg", "I.E.", "Hub", "%"
    val displayName: String,                  // "Milligramm", "Internationale Einheiten"
    val canConvert: Boolean = true,           // Kann in andere Gewichtseinheiten umgerechnet werden?
    val conversionFactor: Double = 1.0,       // Faktor zu mg (g=1000, µg=0.001)
    val isCountUnit: Boolean = false          // Zähleinheit wie "Hub", "Tropfen"
)

@Serializable
data class MedicationForm(
    val id: String,                           // "standard", "dry_substance", "spray"
    val name: String,                         // "Standard-Ampulle", "Trockensubstanz"
    val type: FormType,
    val concentration: Concentration,
    val isDefault: Boolean = false,
    val preparation: PreparationRule? = null,
    val notes: String? = null
)

@Serializable
enum class FormType {
    AMPOULE,           // 1 mg / 1 ml
    DRY_SUBSTANCE,     // 500 mg + 5 ml Träger = 500 mg / 5 ml
    SPRAY,             // 0.4 mg / Hub
    INFUSION,          // 1000 mg / 100 ml
    TABLET,            // 500 mg / Tablette
    DROPS,             // 2 mg / Tropfen
    SUPPOSITORY        // 125 mg / Zäpfchen
}

@Serializable
data class Concentration(
    val activeAmount: Double,                 // Wirkstoffmenge
    val activeUnit: String,                   // Einheit des Wirkstoffs
    val vehicleVolume: Double,                // Trägermenge (0 bei Zähleinheiten)
    val vehicleUnit: String = "ml",           // "ml", "Hub", "Tbl", "Tr"
    val display: String,                      // "500 mg / 0 ml", "0.4 mg / Hub"
    val concentrationPerMl: Double? = null    // Finale Konzentration nach Verdünnung
)

@Serializable
data class PreparationRule(
    val carrierFluid: String,                 // "Aqua", "NaCl 0,9%", "G5%"
    val carrierVolume: Double,                // 5.0 ml
    val finalVolume: Double,                  // Endvolumen nach Mischung
    val finalConcentrationPerMl: Double,      // mg/ml nach Verdünnung
    val instructions: String? = null,         // Mischungsanleitung
    val stability: String? = null             // "Sofort verwenden"
)

@Serializable
data class DosingRule(
    val id: String,                           // Eindeutige Regel-ID
    val indication: String,                   // "Reanimation", "Anaphylaxie"
    val route: ApplicationRoute,
    val calculation: DosageCalculation,
    val ageRange: AgeRange? = null,
    val weightRange: WeightRange? = null,
    val maxDose: MaxDose? = null,
    val repetition: RepetitionRule? = null,
    val warnings: List<String> = emptyList(),
    val notes: String? = null,
    val priority: Int = 0                     // Höhere Zahl = höhere Priorität bei Überlappung
)

@Serializable
data class ApplicationRoute(
    val route: String,                        // "i.v.", "i.m.", "p.i.", "s.l."
    val displayName: String,                  // "intravenös", "intramuskulär"
    val requiresIvAccess: Boolean = false,
    val preparationTime: Int? = null,         // Sekunden
    val specialEquipment: List<String> = emptyList()
)

@Serializable
data class DosageCalculation(
    val type: CalculationType,
    val fixedAmount: Double? = null,          // Feste Dosis
    val perKgAmount: Double? = null,          // Pro kg Körpergewicht
    val minAmount: Double? = null,            // Mindestdosis
    val maxAmount: Double? = null,            // Höchstdosis
    val roundingRules: RoundingRule? = null
)

@Serializable
enum class CalculationType {
    FIXED,             // Feste Dosierung
    PER_KG,            // Pro Kilogramm Körpergewicht
    AGE_BASED,         // Altersbasiert (wie bei Kindern)
    CUSTOM             // Spezielle Formel
}

@Serializable
data class AgeRange(
    val minYears: Int? = null,                // Minimum Alter in Jahren (inklusiv)
    val maxYears: Int? = null                 // Maximum Alter in Jahren (exklusiv)
)

@Serializable
data class WeightRange(
    val minKg: Double? = null,                // Minimum Gewicht (inklusiv)
    val maxKg: Double? = null                 // Maximum Gewicht (exklusiv)
)

@Serializable
data class MaxDose(
    val amount: Double,
    val unit: String,
    val timeframe: String,                    // "pro Einsatz", "pro Tag"
    val warning: String? = null
)

@Serializable
data class RepetitionRule(
    val allowed: Boolean,
    val minInterval: Int,                     // Minuten zwischen Wiederholungen
    val maxRepeats: Int,
    val conditions: String? = null
)

@Serializable
data class RoundingRule(
    val precision: Int = 2,                   // Nachkommastellen
    val roundToNearest: Double? = null,       // z.B. 0.5 mg Schritte
    val displayTrailingZeros: Boolean = false
)

@Serializable
data class MedicationInfo(
    val indication: String? = null,
    val contraindication: String? = null,
    val effect: String? = null,
    val sideEffects: String? = null,
    val interactions: List<String> = emptyList(),
    val storage: String? = null
)

@Serializable
data class BTMStatus(
    val isBTM: Boolean,
    val schedule: String? = null,             // BtMG Anlage I, II, III
    val documentationRequired: Boolean = true,
    val specialStorageRequired: Boolean = true,
    val notes: String? = null
)

@Serializable
enum class MedicationCategory {
    CARDIOVASCULAR,    // Herz-Kreislauf
    RESPIRATORY,       // Atemwege
    NEUROLOGICAL,      // Neurologie
    ANALGESIC,         // Schmerzmittel
    SEDATIVE,          // Beruhigungsmittel
    ANTIDOTE,          // Gegenmittel
    ANTIALLERGIC,      // Antiallergikum
    ANTIBIOTIC,        // Antibiotikum
    OTHER              // Sonstiges
}