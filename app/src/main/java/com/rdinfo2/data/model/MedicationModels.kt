// app/src/main/java/com/rdinfo2/data/model/MedicationModels.kt
package com.rdinfo2.data.model

/**
 * BEREINIGTE VERSION - Vereinfachte Medikamenten-Datenmodelle ohne Duplikate
 * Kompatibel mit bestehendem System, entfernt Konflikte
 */

// Basis-Medikament (vereinfacht)
data class SimpleMedication(
    val id: String,
    val name: String,
    val category: SimpleMedicationCategory,
    val dosePerKg: Double? = null,          // mg/kg
    val fixedDose: Double? = null,          // mg (fixe Dosis)
    val maxDose: Double? = null,            // mg (Maximaldosis)
    val concentration: Double,              // mg/ml
    val unit: String = "mg",                // Einheit
    val indications: List<String> = emptyList(),
    val contraindications: List<String> = emptyList(),
    val ageRestrictions: SimpleAgeRestrictions? = null,
    val notes: String? = null
)

// Vereinfachte Kategorien (ohne Konflikte)
enum class SimpleMedicationCategory {
    ANALGESIC,           // Schmerzmittel
    CARDIOVASCULAR,      // Herz-Kreislauf
    RESPIRATORY,         // Atemwege
    NEUROLOGIC,          // Neurologisch
    EMERGENCY,           // Notfallmedikamente
    ANTIHISTAMINE,       // Antihistaminika
    OTHER               // Sonstige
}

// Vereinfachte Altersrestriktionen
data class SimpleAgeRestrictions(
    val minAgeMonths: Int? = null,
    val maxAgeMonths: Int? = null,
    val description: String? = null
)

// Vereinfachte Dosierungsberechnung
data class SimpleDoseCalculation(
    val dose: Double,
    val volume: Double,
    val unit: String,
    val calculation: String,
    val warnings: List<String> = emptyList(),
    val isValid: Boolean = true,
    val errorMessage: String? = null
) {
    companion object {
        fun invalid(message: String) = SimpleDoseCalculation(
            dose = 0.0,
            volume = 0.0,
            unit = "",
            calculation = "",
            warnings = emptyList(),
            isValid = false,
            errorMessage = message
        )
    }
}

// Standard-Medikamentenliste (vereinfacht)
object StandardMedications {
    val medications = listOf(
        SimpleMedication(
            id = "morphin",
            name = "Morphin",
            category = SimpleMedicationCategory.ANALGESIC,
            dosePerKg = 0.1,
            maxDose = 10.0,
            concentration = 10.0,
            unit = "mg",
            indications = listOf("Starke Schmerzen", "Herzinfarkt"),
            ageRestrictions = SimpleAgeRestrictions(minAgeMonths = 12)
        ),

        SimpleMedication(
            id = "adrenalin",
            name = "Adrenalin",
            category = SimpleMedicationCategory.CARDIOVASCULAR,
            dosePerKg = 0.01,
            maxDose = 1.0,
            concentration = 1.0,
            unit = "mg",
            indications = listOf("Reanimation", "Anaphylaxie")
        ),

        SimpleMedication(
            id = "diazepam",
            name = "Diazepam",
            category = SimpleMedicationCategory.NEUROLOGIC,
            dosePerKg = 0.3,
            maxDose = 10.0,
            concentration = 5.0,
            unit = "mg",
            indications = listOf("Krampfanfall", "Sedierung")
        ),

        SimpleMedication(
            id = "salbutamol",
            name = "Salbutamol",
            category = SimpleMedicationCategory.RESPIRATORY,
            dosePerKg = 2.5,
            maxDose = 5000.0,
            concentration = 5000.0,
            unit = "µg",
            indications = listOf("Bronchospasmus", "Asthma")
        ),

        SimpleMedication(
            id = "paracetamol",
            name = "Paracetamol",
            category = SimpleMedicationCategory.ANALGESIC,
            dosePerKg = 15.0,
            maxDose = 1000.0,
            concentration = 10.0,
            unit = "mg",
            indications = listOf("Schmerzen", "Fieber")
        )
    )
}