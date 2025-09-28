// app/src/main/java/com/rdinfo2/data/patient/PatientDataManager.kt
package com.rdinfo2.data.patient

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Einfacher aber funktionierender PatientDataManager
 * FIXED: Alle Compile-Fehler behoben, funktioniert mit bestehendem System
 */
object PatientDataManager {

    // Basis-Patientendaten mit korrekter State-Delegation
    var ageYears by mutableStateOf(5)
        private set

    var ageMonths by mutableStateOf(5)
        private set

    var weightKg by mutableStateOf<Double?>(null)
        private set

    var gender by mutableStateOf(PatientGender.UNKNOWN)
        private set

    // Persistente Speicherung
    private var _persistentAgeYears = 5
    private var _persistentAgeMonths = 5
    private var _persistentWeight: Double? = null
    private var _persistentGender = PatientGender.UNKNOWN

    // Berechnete Eigenschaften
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

    // ==================== PUBLIC API ====================

    /**
     * Alter aktualisieren
     */
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

    /**
     * Gewicht aktualisieren
     */
    fun updateWeight(weight: Double?) {
        weightKg = weight?.coerceIn(0.5, 300.0)
        _persistentWeight = weightKg
    }

    /**
     * Geschlecht aktualisieren
     */
    fun updateGender(newGender: PatientGender) {
        gender = newGender
        _persistentGender = newGender

        // Gewicht neu schätzen wenn automatisch
        if (weightKg == null) {
            weightKg = calculateEstimatedWeight()
        }
    }

    // ==================== QUICK-SET METHODEN ====================

    /**
     * Säugling setzen
     */
    fun setInfant() {
        updateAge(0, 6)  // 6 Monate
        updateWeight(8.0)
        updateGender(PatientGender.UNKNOWN)
    }

    /**
     * Kind setzen
     */
    fun setChild() {
        updateAge(5, 0)  // 5 Jahre
        updateWeight(null)  // Auto-Schätzung
        updateGender(PatientGender.UNKNOWN)
    }

    /**
     * Erwachsener setzen
     */
    fun setAdult() {
        updateAge(35, 0)  // 35 Jahre
        updateWeight(70.0)
        updateGender(PatientGender.UNKNOWN)
    }

    // ==================== GEWICHTSSCHÄTZUNG ====================

    /**
     * WHO-konforme Gewichtsschätzung
     */
    private fun calculateEstimatedWeight(): Double {
        return when {
            // Säuglinge (0-12 Monate)
            totalAgeInMonths <= 12 -> {
                when (totalAgeInMonths) {
                    0 -> 3.5   // Neugeborenes
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

            // Kleinkinder (1-5 Jahre): WHO-Formel
            totalAgeInMonths <= 60 -> {
                val ageInYears = totalAgeInMonths / 12.0
                2 * ageInYears + 8
            }

            // Kinder (5-14 Jahre)
            totalAgeInMonths < 14 * 12 -> {
                val ageInYears = totalAgeInMonths / 12.0
                (ageInYears * 2.5) + 10
            }

            // Jugendliche (14-18 Jahre)
            totalAgeInMonths < 18 * 12 -> {
                val ageInYears = totalAgeInMonths / 12.0
                45 + ((ageInYears - 14) * 5)
            }

            // Erwachsene: Geschlechtsabhängig
            else -> when (gender) {
                PatientGender.MALE -> 75.0
                PatientGender.FEMALE -> 65.0
                PatientGender.UNKNOWN -> 70.0
            }
        }
    }

    // ==================== EXPORT & SUMMARY ====================

    /**
     * Patientenzusammenfassung für UI
     */
    fun getPatientSummary(): String {
        val weight = String.format("%.1f", estimatedWeightKg)
        val ageStr = if (ageMonths > 0) {
            "$ageYears Jahre, $ageMonths Monate"
        } else {
            "$ageYears Jahre"
        }
        val genderStr = when (gender) {
            PatientGender.MALE -> "männlich"
            PatientGender.FEMALE -> "weiblich"
            PatientGender.UNKNOWN -> "unbekannt"
        }

        return "$ageStr, $weight kg, $genderStr"
    }

    /**
     * Altersklassifikation
     */
    fun getAgeClassification(): String {
        return when {
            isInfant -> "👶 Säugling (0-12 Monate)"
            isChild -> "🧒 Kind (1-17 Jahre)"
            isAdult -> "👨 Erwachsener (18+ Jahre)"
            else -> "👤 Patient"
        }
    }

    // ==================== RESET & PERSISTENCE ====================

    /**
     * Zu gespeicherten Werten zurücksetzen
     */
    fun resetToDefaults() {
        ageYears = _persistentAgeYears
        ageMonths = _persistentAgeMonths
        weightKg = _persistentWeight
        gender = _persistentGender
    }

    /**
     * Komplett zurücksetzen
     */
    fun resetToStandard() {
        updateAge(5, 5)
        updateWeight(null)
        updateGender(PatientGender.UNKNOWN)
    }

    /**
     * Aktuelle Werte als Standard speichern
     */
    fun saveAsDefault() {
        _persistentAgeYears = ageYears
        _persistentAgeMonths = ageMonths
        _persistentWeight = weightKg
        _persistentGender = gender
    }
}

/**
 * FINAL: PatientDataManager ohne Duplikate
 * Verwendet eindeutige Enum-Namen
 */
enum class PatientGender {
    MALE, FEMALE, UNKNOWN
}

/**
 * Kompatibilitäts-Alias für altes System
 */
typealias Gender = PatientGender