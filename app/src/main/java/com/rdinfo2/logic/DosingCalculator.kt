// app/src/main/java/com/rdinfo2/logic/DosingCalculator.kt
package com.rdinfo2.logic

import com.rdinfo2.data.patient.PatientData

/**
 * TEMPORÄRE Version für Tests
 * TODO: Später durch vollständiges System mit Medikamenten-Datenbank ersetzen
 */
object DosingCalculator {

    // Temporäre Sample-Medikamente - werden später durch DB ersetzt
    private val sampleMedications = listOf(
        "Paracetamol",
        "Ibuprofen",
        "Midazolam",
        "Adrenalin"
    )

    fun getSampleMedications(): List<String> {
        return sampleMedications
    }

    fun calculateDosage(medication: String, weightKg: Double, ageYears: Int): DosingResult {
        val dose = when (medication) {
            "Paracetamol" -> {
                val calculatedDose = (weightKg * 15).coerceAtMost(1000.0)
                DosingResult(
                    medication = medication,
                    dose = calculatedDose,
                    unit = "mg",
                    formula = "15 mg/kg",
                    maxDose = "1000 mg",
                    route = "p.o./i.v."
                )
            }
            "Ibuprofen" -> {
                val calculatedDose = (weightKg * 10).coerceAtMost(600.0)
                DosingResult(
                    medication = medication,
                    dose = calculatedDose,
                    unit = "mg",
                    formula = "10 mg/kg",
                    maxDose = "600 mg",
                    route = "p.o."
                )
            }
            "Midazolam" -> {
                val calculatedDose = (weightKg * 0.1).coerceAtMost(10.0)
                DosingResult(
                    medication = medication,
                    dose = calculatedDose,
                    unit = "mg",
                    formula = "0,1 mg/kg",
                    maxDose = "10 mg",
                    route = "i.v./i.m."
                )
            }
            "Adrenalin" -> {
                val calculatedDose = (weightKg * 0.01).coerceAtMost(1.0)
                DosingResult(
                    medication = medication,
                    dose = calculatedDose,
                    unit = "mg",
                    formula = "0,01 mg/kg",
                    maxDose = "1 mg",
                    route = "i.v./i.m."
                )
            }
            else -> DosingResult(
                medication = "Unbekannt",
                dose = 0.0,
                unit = "",
                formula = "",
                maxDose = "",
                route = ""
            )
        }

        return dose
    }
}

data class DosingResult(
    val medication: String,
    val dose: Double,
    val unit: String,
    val formula: String,
    val maxDose: String,
    val route: String
) {
    fun getDisplayText(): String {
        val formattedDose = if (dose % 1.0 == 0.0) {
            dose.toInt().toString()
        } else {
            String.format("%.1f", dose)
        }

        return "$medication: $formattedDose $unit\n($formula, max. $maxDose)\nApplikation: $route"
    }
}