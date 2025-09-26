// File: app/src/main/java/com/rdinfo2/logic/VitalParameters.kt
package com.rdinfo2.logic

/**
 * Data class for storing vital parameters/normal values
 * Used for age-specific reference values in emergency medicine
 */
data class VitalParameters(
    val ageMinYears: Int = 0,
    val ageMaxYears: Int = Int.MAX_VALUE,
    val heartRateMin: Int? = null,
    val heartRateMax: Int? = null,
    val respiratoryRateMin: Int? = null,
    val respiratoryRateMax: Int? = null,
    val systolicBPMin: Int? = null,
    val systolicBPMax: Int? = null,
    val diastolicBpMin: Int? = null,
    val diastolicBpMax: Int? = null,
    val hemoglobinMin: Double? = null,
    val hemoglobinMax: Double? = null,
    val tidalVolume: Int? = null,
    val bloodVolume: Int? = null,
    val fluidRequirement: Int? = null,
    val calorieRequirement: Int? = null,
    val temperature: Double? = null,
    val oxygenSaturation: Int? = null
)