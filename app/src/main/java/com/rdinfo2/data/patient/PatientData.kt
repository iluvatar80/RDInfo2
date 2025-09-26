// app/src/main/java/com/rdinfo2/data/patient/PatientData.kt
package com.rdinfo2.data.patient

enum class PatientGender {
    MALE, FEMALE, UNKNOWN
}

data class PatientData(
    val ageYears: Int = 0,
    val ageMonths: Int = 0,
    val weightKg: Double = 0.0,
    val gender: PatientGender = PatientGender.UNKNOWN
)