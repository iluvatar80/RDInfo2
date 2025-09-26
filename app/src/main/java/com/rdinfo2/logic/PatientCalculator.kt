// app/src/main/java/com/rdinfo2/logic/PatientCalculator.kt
package com.rdinfo2.logic

import com.rdinfo2.data.patient.PatientData

data class VitalParameters(
    val heartRateMin: Int,
    val heartRateMax: Int,
    val systolicBPMin: Int,
    val systolicBPMax: Int,
    val respiratoryRateMin: Int,
    val respiratoryRateMax: Int,
    val tidalVolume: Double, // ml/kg
    val bloodVolume: Double, // ml/kg
    val hemoglobinMin: Double, // g/dl
    val hemoglobinMax: Double, // g/dl
    val fluidRequirement: Double, // ml/kg/Tag
    val calorieRequirement: Double // kcal/kg/Tag
)

object PatientCalculator {

    fun estimateWeight(ageYears: Int, ageMonths: Int): Double {
        val totalMonths = ageYears * 12 + ageMonths

        return when {
            // Geburt bis 12 Monate: 3 kg + (Monate × 0.7 kg)
            totalMonths <= 12 -> 3.0 + (totalMonths * 0.7)
            // 1-5 Jahre: (Alter × 2) + 8
            ageYears in 1..5 -> (ageYears * 2.0) + 8.0
            // 6-12 Jahre: (Alter × 3) + 7
            ageYears in 6..12 -> (ageYears * 3.0) + 7.0
            // 13-17 Jahre: 50 + (Alter - 13) × 5
            ageYears in 13..17 -> 50.0 + ((ageYears - 13) * 5.0)
            // Erwachsene: Standard 70kg
            else -> 70.0
        }
    }

    fun calculateVitalParameters(ageYears: Int, ageMonths: Int, weightKg: Double): VitalParameters {
        val totalMonths = ageYears * 12 + ageMonths

        return when {
            // Neugeborene (0-1 Monat)
            totalMonths <= 1 -> VitalParameters(
                heartRateMin = 120, heartRateMax = 160,
                systolicBPMin = 65, systolicBPMax = 95,
                respiratoryRateMin = 30, respiratoryRateMax = 60,
                tidalVolume = 6.0, // ml/kg
                bloodVolume = 80.0, // ml/kg
                hemoglobinMin = 14.0, hemoglobinMax = 20.0,
                fluidRequirement = 100.0, // erste 10kg
                calorieRequirement = 110.0
            )
            // Säuglinge (1-12 Monate)
            totalMonths <= 12 -> VitalParameters(
                heartRateMin = 100, heartRateMax = 150,
                systolicBPMin = 70, systolicBPMax = 100,
                respiratoryRateMin = 25, respiratoryRateMax = 50,
                tidalVolume = 7.0,
                bloodVolume = 75.0,
                hemoglobinMin = 10.0, hemoglobinMax = 14.0,
                fluidRequirement = 100.0,
                calorieRequirement = 100.0
            )
            // Kleinkinder (1-3 Jahre)
            ageYears in 1..3 -> VitalParameters(
                heartRateMin = 90, heartRateMax = 130,
                systolicBPMin = 80, systolicBPMax = 110,
                respiratoryRateMin = 20, respiratoryRateMax = 40,
                tidalVolume = 8.0,
                bloodVolume = 75.0,
                hemoglobinMin = 11.0, hemoglobinMax = 13.0,
                fluidRequirement = if (weightKg <= 10) 100.0 else 100.0 - ((weightKg - 10) * 2),
                calorieRequirement = 90.0
            )
            // Schulkinder (4-12 Jahre)
            ageYears in 4..12 -> VitalParameters(
                heartRateMin = 70, heartRateMax = 120,
                systolicBPMin = 90, systolicBPMax = 120,
                respiratoryRateMin = 15, respiratoryRateMax = 30,
                tidalVolume = 8.0,
                bloodVolume = 70.0,
                hemoglobinMin = 11.5, hemoglobinMax = 15.5,
                fluidRequirement = calculateFluidRequirement(weightKg),
                calorieRequirement = if (ageYears <= 6) 80.0 else 70.0
            )
            // Jugendliche (13-17 Jahre)
            ageYears in 13..17 -> VitalParameters(
                heartRateMin = 60, heartRateMax = 100,
                systolicBPMin = 100, systolicBPMax = 130,
                respiratoryRateMin = 12, respiratoryRateMax = 25,
                tidalVolume = 8.0,
                bloodVolume = 70.0,
                hemoglobinMin = 12.0, hemoglobinMax = 16.0,
                fluidRequirement = calculateFluidRequirement(weightKg),
                calorieRequirement = 50.0
            )
            // Erwachsene (18-65 Jahre)
            ageYears in 18..65 -> VitalParameters(
                heartRateMin = 60, heartRateMax = 100,
                systolicBPMin = 100, systolicBPMax = 140,
                respiratoryRateMin = 12, respiratoryRateMax = 20,
                tidalVolume = 7.0,
                bloodVolume = 70.0,
                hemoglobinMin = 12.0, hemoglobinMax = 16.0,
                fluidRequirement = 30.0, // ml/kg/Tag
                calorieRequirement = 25.0
            )
            // Geriatrische Patienten (>65 Jahre)
            else -> VitalParameters(
                heartRateMin = 60, heartRateMax = 90,
                systolicBPMin = 110, systolicBPMax = 160,
                respiratoryRateMin = 12, respiratoryRateMax = 20,
                tidalVolume = 6.0,
                bloodVolume = 65.0,
                hemoglobinMin = 11.0, hemoglobinMax = 15.0,
                fluidRequirement = 30.0,
                calorieRequirement = 20.0
            )
        }
    }

    private fun calculateFluidRequirement(weightKg: Double): Double {
        return when {
            weightKg <= 10 -> 100.0 // erste 10kg: 100ml/kg
            weightKg <= 20 -> 100.0 - ((weightKg - 10) * 2.5) // nächste 10kg: 50ml/kg
            else -> 75.0 - ((weightKg - 20) * 1.25) // über 20kg: 20ml/kg
        }.coerceAtLeast(20.0) // Minimum 20ml/kg
    }

    fun getAgeCategory(ageYears: Int, ageMonths: Int): String {
        val totalMonths = ageYears * 12 + ageMonths
        return when {
            totalMonths <= 1 -> "Neugeborenes (${totalMonths}M)"
            totalMonths <= 12 -> "Säugling (${totalMonths}M)"
            ageYears in 1..3 -> "Kleinkind (${ageYears}J ${ageMonths}M)"
            ageYears in 4..12 -> "Schulkind (${ageYears}J)"
            ageYears in 13..17 -> "Jugendlich (${ageYears}J)"
            ageYears in 18..65 -> "Erwachsen (${ageYears}J)"
            else -> "Geriatrisch (${ageYears}J)"
        }
    }
}