// app/src/main/java/com/rdinfo2/data/model/MedicationModels.kt
package com.rdinfo2.data.model

/**
 * Vollständige Datenmodelle für Medikamentenverwaltung
 * FIXED: Alle fehlenden Klassen und Enums hinzugefügt
 */

data class Medication(
    val id: String,
    val name: String,
    val category: MedicationCategory,
    val dosingRule: DosingRule,
    val preparation: Preparation,
    val indications: List<String> = emptyList(),
    val contraindications: List<String> = emptyList(),
    val ageRestrictions: AgeRestrictions? = null,
    val weightRestrictions: WeightRestrictions? = null,
    val notes: String? = null
)

data class DosingRule(
    val type: DosingType,
    val dosePerKg: Double? = null,      // mg/kg
    val dosePerM2: Double? = null,      // mg/m² (für Körperoberfläche)
    val fixedDose: Double? = null,      // mg (fixe Dosis)
    val maxDose: Double? = null,        // mg (Maximaldosis)
    val minDose: Double? = null,        // mg (Minimaldosis)
    val intervalHours: Int = 24,        // Dosierungsintervall in Stunden
    val description: String? = null
)

enum class DosingType {
    WEIGHT_BASED,           // Gewichtsbasiert (mg/kg)
    FIXED_DOSE,             // Fixe Dosis unabhängig vom Gewicht
    AGE_WEIGHT_COMBINED,    // Kombination aus Alter und Gewicht
    SURFACE_AREA            // Körperoberflächenbasiert (mg/m²)
}

data class Preparation(
    val type: PreparationType,
    val strength: Double,           // mg pro Einheit (Tablette/Ampulle)
    val concentration: Double,      // mg/ml (für Lösungen)
    val volume: Double? = null,     // ml (Ampullengröße)
    val unit: String,              // "mg", "µg", "IE", etc.
    val step: Double = 0.1         // Kleinste verfügbare Dosierungseinheit
)

enum class PreparationType {
    SOLUTION,    // Lösung (ml)
    TABLET,      // Tablette (Stück)
    AMPULE,      // Ampulle (ml)
    INJECTION    // Fertigspritze (ml)
}

enum class MedicationCategory {
    ANALGESIC,           // Schmerzmittel
    CARDIOVASCULAR,      // Herz-Kreislauf
    RESPIRATORY,         // Atemwege
    NEUROLOGIC,          // Neurologisch
    ANTIBIOTIC,          // Antibiotika
    EMERGENCY,           // Notfallmedikamente
    ANESTHETIC,          // Anästhetika
    ANTIHISTAMINE,       // Antihistaminika
    CORTICOSTEROID,      // Kortikosteroide
    ELECTROLYTE,         // Elektrolyte
    OTHER               // Sonstige
}

data class AgeRestrictions(
    val minAgeMonths: Int? = null,   // Mindestalter in Monaten
    val maxAgeMonths: Int? = null,   // Höchstalter in Monaten
    val description: String? = null
)

data class WeightRestrictions(
    val minWeightKg: Double? = null,  // Mindestgewicht in kg
    val maxWeightKg: Double? = null,  // Höchstgewicht in kg
    val description: String? = null
)

// FIXED: Gender Enum (falls nicht bereits in PatientDataManager definiert)
enum class Gender {
    MALE, FEMALE, UNKNOWN
}

/**
 * Standard-Medikamentenliste für Rettungsdienst
 */
object StandardMedications {
    val medications = listOf(
        // Analgetika
        Medication(
            id = "morphin",
            name = "Morphin",
            category = MedicationCategory.ANALGESIC,
            dosingRule = DosingRule(
                type = DosingType.WEIGHT_BASED,
                dosePerKg = 0.1,
                maxDose = 10.0,
                description = "0,1 mg/kg, max. 10 mg"
            ),
            preparation = Preparation(
                type = PreparationType.AMPULE,
                strength = 10.0,
                concentration = 10.0,
                volume = 1.0,
                unit = "mg"
            ),
            indications = listOf("Starke Schmerzen", "Herzinfarkt", "Polytrauma"),
            ageRestrictions = AgeRestrictions(minAgeMonths = 12) // Ab 1 Jahr
        ),

        Medication(
            id = "fentanyl",
            name = "Fentanyl",
            category = MedicationCategory.ANALGESIC,
            dosingRule = DosingRule(
                type = DosingType.WEIGHT_BASED,
                dosePerKg = 1.0,
                maxDose = 100.0,
                description = "1 µg/kg, max. 100 µg"
            ),
            preparation = Preparation(
                type = PreparationType.AMPULE,
                strength = 100.0,
                concentration = 50.0,
                volume = 2.0,
                unit = "µg"
            ),
            indications = listOf("Starke Schmerzen", "Narkoseeinleitung"),
            ageRestrictions = AgeRestrictions(minAgeMonths = 24) // Ab 2 Jahre
        ),

        // Herz-Kreislauf
        Medication(
            id = "adrenalin",
            name = "Adrenalin",
            category = MedicationCategory.CARDIOVASCULAR,
            dosingRule = DosingRule(
                type = DosingType.WEIGHT_BASED,
                dosePerKg = 10.0,
                maxDose = 1000.0,
                description = "10 µg/kg, max. 1 mg"
            ),
            preparation = Preparation(
                type = PreparationType.AMPULE,
                strength = 1000.0,
                concentration = 1000.0,
                volume = 1.0,
                unit = "µg"
            ),
            indications = listOf("Reanimation", "Anaphylaxie", "Bronchospasmus")
        ),

        Medication(
            id = "atropin",
            name = "Atropin",
            category = MedicationCategory.CARDIOVASCULAR,
            dosingRule = DosingRule(
                type = DosingType.WEIGHT_BASED,
                dosePerKg = 20.0,
                maxDose = 3000.0,
                description = "20 µg/kg, max. 3 mg"
            ),
            preparation = Preparation(
                type = PreparationType.AMPULE,
                strength = 500.0,
                concentration = 500.0,
                volume = 1.0,
                unit = "µg"
            ),
            indications = listOf("Bradykardie", "Vergiftung")
        ),

        // Atemwege
        Medication(
            id = "salbutamol",
            name = "Salbutamol",
            category = MedicationCategory.RESPIRATORY,
            dosingRule = DosingRule(
                type = DosingType.AGE_WEIGHT_COMBINED,
                dosePerKg = 2.5,
                maxDose = 5000.0,
                description = "2,5 µg/kg, max. 5 mg"
            ),
            preparation = Preparation(
                type = PreparationType.SOLUTION,
                strength = 5000.0,
                concentration = 5000.0,
                volume = 2.5,
                unit = "µg"
            ),
            indications = listOf("Bronchospasmus", "Asthma", "COPD")
        ),

        // Notfallmedikamente
        Medication(
            id = "diazepam",
            name = "Diazepam",
            category = MedicationCategory.NEUROLOGIC,
            dosingRule = DosingRule(
                type = DosingType.WEIGHT_BASED,
                dosePerKg = 0.3,
                maxDose = 10.0,
                description = "0,3 mg/kg, max. 10 mg"
            ),
            preparation = Preparation(
                type = PreparationType.AMPULE,
                strength = 10.0,
                concentration = 5.0,
                volume = 2.0,
                unit = "mg"
            ),
            indications = listOf("Krampfanfall", "Angst", "Sedierung")
        )
    )
}