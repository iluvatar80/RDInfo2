// app/src/main/java/com/rdinfo2/data/PatientDataManager.kt
package com.rdinfo2.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Verwaltet Patientendaten zentral und persistent
 * FIXED: Eingaben werden jetzt korrekt gespeichert und bleiben beim Overlay-Schließen erhalten
 */
object PatientDataManager {

    // State für UI - wird NICHT zurückgesetzt beim Overlay schließen
    var ageYears by mutableStateOf(5)      // FIXED: Startwert 5 statt 40
        private set

    var ageMonths by mutableStateOf(5)     // FIXED: Startwert 5 statt 0
        private set

    var weightKg by mutableStateOf<Double?>(null)
        private set

    var gender by mutableStateOf(Gender.UNKNOWN)
        private set

    // FIXED: Persistent storage für Eingaben
    private var _persistentAgeYears = 5
    private var _persistentAgeMonths = 5
    private var _persistentWeight: Double? = null
    private var _persistentGender = Gender.UNKNOWN

    // Computed properties
    val totalAgeInMonths: Int
        get() = ageYears * 12 + ageMonths

    val estimatedWeightKg: Double
        get() = weightKg ?: calculateEstimatedWeight()

    val isInfant: Boolean
        get() = totalAgeInMonths < 12

    val isChild: Boolean
        get() = totalAgeInMonths >= 12 && totalAgeInMonths < 18 * 12

    val isAdult: Boolean
        get() = totalAgeInMonths >= 18 * 12

    // FIXED: Update-Methoden speichern persistent
    fun updateAge(years: Int, months: Int) {
        val validYears = years.coerceIn(0, 120)
        val validMonths = months.coerceIn(0, 11)

        ageYears = validYears
        ageMonths = validMonths

        // Persistent speichern
        _persistentAgeYears = validYears
        _persistentAgeMonths = validMonths

        // Auto-update Gewicht wenn nicht manuell gesetzt
        if (weightKg == null) {
            weightKg = calculateEstimatedWeight()
        }
    }

    fun updateWeight(weight: Double?) {
        weightKg = weight?.coerceIn(0.5, 300.0)
        _persistentWeight = weightKg
    }

    fun updateGender(newGender: Gender) {
        gender = newGender
        _persistentGender = newGender
    }

    // FIXED: Reset lädt persistent gespeicherte Werte
    fun resetToDefaults() {
        // Lade gespeicherte Werte statt Defaults
        ageYears = _persistentAgeYears
        ageMonths = _persistentAgeMonths
        weightKg = _persistentWeight
        gender = _persistentGender
    }

    // Quick-Set Methoden für häufige Patiententypen
    fun setInfant() {
        updateAge(0, 6)  // 6 Monate alter Säugling
        updateWeight(8.0)
        updateGender(Gender.UNKNOWN)
    }

    fun setChild() {
        updateAge(5, 0)  // 5 Jahre altes Kind
        updateWeight(null) // Auto-Schätzung
        updateGender(Gender.UNKNOWN)
    }

    fun setAdult() {
        updateAge(35, 0)  // 35 Jahre alter Erwachsener
        updateWeight(70.0)
        updateGender(Gender.UNKNOWN)
    }

    // FIXED: Verbesserte Gewichtsschätzung mit korrekten Formeln
    private fun calculateEstimatedWeight(): Double {
        return when {
            // Säuglinge (0-12 Monate): Empirische Formel
            totalAgeInMonths <= 12 -> {
                when (totalAgeInMonths) {
                    0 -> 3.5  // Neugeborenes
                    1 -> 4.5
                    2 -> 5.5
                    3 -> 6.5
                    4 -> 7.0
                    5 -> 7.5
                    6 -> 8.0
                    7 -> 8.5
                    8 -> 9.0
                    9 -> 9.5
                    10 -> 10.0
                    11 -> 10.5
                    12 -> 11.0
                    else -> 11.0
                }
            }

            // Kleinkinder (1-5 Jahre): WHO Formel
            totalAgeInMonths <= 60 -> {
                val ageInYears = totalAgeInMonths / 12.0
                2 * ageInYears + 8  // WHO empfohlene Formel
            }

            // Kinder (5-14 Jahre): Erweiterte Formel
            totalAgeInMonths < 14 * 12 -> {
                val ageInYears = totalAgeInMonths / 12.0
                (ageInYears * 2.5) + 10  // Angepasste Formel für ältere Kinder
            }

            // Jugendliche/Erwachsene: Standard Erwachsenengewicht
            totalAgeInMonths < 18 * 12 -> {
                val ageInYears = totalAgeInMonths / 12.0
                45 + ((ageInYears - 14) * 5)  // Gewichtszunahme in der Pubertät
            }

            // Erwachsene: Geschlechtsabhängig
            else -> when (gender) {
                Gender.MALE -> 75.0
                Gender.FEMALE -> 65.0
                Gender.UNKNOWN -> 70.0
            }
        }
    }

    // Datenexport für andere Komponenten
    fun getPatientSummary(): String {
        val weight = String.format("%.1f", estimatedWeightKg)
        val ageStr = if (ageMonths > 0) "$ageYears Jahre, $ageMonths Monate" else "$ageYears Jahre"
        val genderStr = when (gender) {
            Gender.MALE -> "männlich"
            Gender.FEMALE -> "weiblich"
            Gender.UNKNOWN -> "unbekannt"
        }

        return "$ageStr, $weight kg, $genderStr"
    }
}

enum class Gender {
    MALE, FEMALE, UNKNOWN
}