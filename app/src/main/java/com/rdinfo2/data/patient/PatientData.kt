// app/src/main/java/com/rdinfo2/data/patient/PatientData.kt
package com.rdinfo2.data.patient

import kotlinx.serialization.Serializable

@Serializable
data class PatientData(
    val ageYears: Int = 0,
    val ageMonths: Int = 0,
    val weightKg: Double = 0.0,
    val isManualWeight: Boolean = false,
    val gender: PatientGender = PatientGender.UNKNOWN,
    val isPregnant: Boolean = false,
    val gestationalWeek: Int? = null,
    val allergies: List<String> = emptyList(),
    val medications: List<String> = emptyList(),
    val medicalHistory: List<String> = emptyList()
)

@Serializable
enum class PatientGender {
    MALE, FEMALE, UNKNOWN
}

@Serializable
data class CalculatedValues(
    val totalAgeMonths: Int = 0,
    val effectiveWeight: Double = 0.0,
    val estimatedWeight: Double = 0.0,
    val isInfant: Boolean = false,
    val isChild: Boolean = false,
    val isAdolescent: Boolean = false,
    val isGeriatric: Boolean = false,
    val riskFactors: List<RiskFactor> = emptyList()
)

@Serializable
data class CalculatedPatientValues(
    val totalAgeMonths: Int = 0,
    val effectiveWeight: Double = 0.0,
    val estimatedWeight: Double = 0.0,
    val isInfant: Boolean = false,
    val isChild: Boolean = false,
    val isAdolescent: Boolean = false,
    val isGeriatric: Boolean = false,
    val riskFactors: List<RiskFactor> = emptyList()
)

@Serializable
data class RiskFactor(
    val id: String,
    val name: String,
    val description: String,
    val severity: RiskSeverity = RiskSeverity.MEDIUM
)

@Serializable
enum class RiskSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}