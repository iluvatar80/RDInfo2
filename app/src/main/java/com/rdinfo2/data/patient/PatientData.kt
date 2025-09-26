// app/src/main/java/com/rdinfo2/data/patient/PatientData.kt
package com.rdinfo2.data.patient

/**
 * Basis-Datenmodell für Patienteninformationen
 * Hält temporäre Daten für die aktuelle Behandlung
 * Wird NICHT persistent gespeichert (nur im RAM)
 */
data class PatientData(
    // Grunddaten
    val ageYears: Int = 0,
    val ageMonths: Int = 0,
    val weightKg: Double = 0.0,
    val isManualWeight: Boolean = false, // true = manuell eingegeben, false = geschätzt
    val gender: PatientGender = PatientGender.UNKNOWN,

    // Spezielle Situationen
    val isPregnant: Boolean = false,
    val gestationalWeek: Int? = null, // Schwangerschaftswoche

    // Medizinische Historie (für Warnungen)
    val allergies: List<String> = emptyList(),
    val medications: List<String> = emptyList(),

    // Berechnete/abgeleitete Werte (werden automatisch aktualisiert)
    val estimatedWeightKg: Double = 0.0, // Geschätztes Gewicht nach Alter
    val ageCategory: AgeCategory = AgeCategory.ADULT,
    val totalAgeMonths: Int = ageYears * 12 + ageMonths,

    // Letzte Aktualisierung (für Datenvalidität)
    val lastUpdated: Long = System.currentTimeMillis()
) {

    /**
     * Gibt das effektiv zu verwendende Gewicht zurück
     * (manuell eingegeben oder geschätzt)
     */
    fun getEffectiveWeight(): Double {
        return if (isManualWeight && weightKg > 0) {
            weightKg
        } else {
            estimatedWeightKg
        }
    }

    /**
     * Prüft ob die Patientendaten vollständig/gültig sind
     */
    fun isValid(): Boolean {
        return ageYears >= 0 &&
                ageMonths in 0..11 &&
                getEffectiveWeight() > 0
    }

    /**
     * Formatierte Altersangabe für UI
     */
    fun getFormattedAge(): String {
        return when {
            ageYears == 0 -> "$ageMonths Monate"
            ageMonths == 0 -> "$ageYears Jahre"
            else -> "$ageYears J., $ageMonths M."
        }
    }

    /**
     * Formatierte Gewichtsangabe für UI
     */
    fun getFormattedWeight(): String {
        val weight = getEffectiveWeight()
        val suffix = if (isManualWeight) "" else " (geschätzt)"
        return "${String.format("%.1f", weight)} kg$suffix"
    }
}

enum class PatientGender {
    MALE,
    FEMALE,
    UNKNOWN
}

enum class AgeCategory {
    NEWBORN,        // 0-28 Tage
    INFANT,         // 1-12 Monate
    TODDLER,        // 1-2 Jahre
    CHILD,          // 3-11 Jahre
    ADOLESCENT,     // 12-17 Jahre
    ADULT,          // 18-64 Jahre
    GERIATRIC;      // 65+ Jahre

    companion object {
        fun fromAge(years: Int, months: Int): AgeCategory {
            val totalMonths = years * 12 + months

            return when {
                totalMonths == 0 -> NEWBORN
                totalMonths <= 12 -> INFANT
                years <= 2 -> TODDLER
                years <= 11 -> CHILD
                years <= 17 -> ADOLESCENT
                years <= 64 -> ADULT
                else -> GERIATRIC
            }
        }
    }
}

/**
 * Factory-Funktionen für typische Patientengruppen
 */
object PatientDataFactory {

    /**
     * Erstellt Patientendaten mit automatischer Gewichtsschätzung
     */
    fun create(
        ageYears: Int,
        ageMonths: Int = 0,
        gender: PatientGender = PatientGender.UNKNOWN
    ): PatientData {
        val estimatedWeight = estimateWeight(ageYears, ageMonths)
        val category = AgeCategory.fromAge(ageYears, ageMonths)

        return PatientData(
            ageYears = ageYears,
            ageMonths = ageMonths,
            gender = gender,
            estimatedWeightKg = estimatedWeight,
            ageCategory = category,
            totalAgeMonths = ageYears * 12 + ageMonths,
            isManualWeight = false
        )
    }

    /**
     * Erstellt Patientendaten mit manuellem Gewicht
     */
    fun createWithWeight(
        ageYears: Int,
        ageMonths: Int = 0,
        weightKg: Double,
        gender: PatientGender = PatientGender.UNKNOWN
    ): PatientData {
        val estimatedWeight = estimateWeight(ageYears, ageMonths)
        val category = AgeCategory.fromAge(ageYears, ageMonths)

        return PatientData(
            ageYears = ageYears,
            ageMonths = ageMonths,
            weightKg = weightKg,
            isManualWeight = true,
            gender = gender,
            estimatedWeightKg = estimatedWeight,
            ageCategory = category,
            totalAgeMonths = ageYears * 12 + ageMonths
        )
    }

    /**
     * Standard-Notfallpatient (unbekanntes Alter/Gewicht)
     */
    fun createUnknown(): PatientData {
        return create(ageYears = 40) // Standardalter für Berechnungen
    }

    /**
     * Gewichtsschätzung nach medizinischen Formeln
     */
    private fun estimateWeight(years: Int, months: Int): Double {
        val totalMonths = years * 12 + months

        return when {
            // Neugeborene/Säuglinge: 3.5kg + 0.5kg/Monat
            totalMonths == 0 -> 3.5
            totalMonths <= 12 -> 3.5 + (totalMonths * 0.5)

            // Kleinkinder: 2 * Jahre + 8kg
            years in 1..5 -> 2.0 * years + 8.0

            // Schulkinder: 3 * Jahre + 7kg
            years in 6..12 -> 3.0 * years + 7.0

            // Jugendliche: 50kg + 5kg pro Jahr über 13
            years in 13..17 -> 50.0 + (years - 13) * 5.0

            // Erwachsene: Standardgewicht 70kg
            else -> 70.0
        }
    }
}