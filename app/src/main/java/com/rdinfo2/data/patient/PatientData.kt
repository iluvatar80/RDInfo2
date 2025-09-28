// app/src/main/java/com/rdinfo2/data/patient/PatientData.kt
package com.rdinfo2.data.patient

/**
 * BEREINIGTE VERSION - Patientendaten ohne Duplikate
 * Entfernt Konflikte mit PatientDataManager
 */

// Einfache Patient-Datenklasse
data class SimplePatientData(
    val ageYears: Int = 0,
    val ageMonths: Int = 0,
    val weightKg: Double = 0.0,
    val isManualWeight: Boolean = false,
    val gender: SimplePatientGender = SimplePatientGender.UNKNOWN,
    val notes: String = ""
)

// Vereinfachtes Gender-Enum (ohne Konflikt)
enum class SimplePatientGender {
    MALE, FEMALE, UNKNOWN
}

// Berechnete Werte
data class SimpleCalculatedValues(
    val totalAgeMonths: Int = 0,
    val effectiveWeight: Double = 0.0,
    val estimatedWeight: Double = 0.0,
    val isInfant: Boolean = false,
    val isChild: Boolean = false,
    val isAdult: Boolean = false
)