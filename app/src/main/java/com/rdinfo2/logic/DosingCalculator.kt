// app/src/main/java/com/rdinfo2/logic/DosingCalculator.kt
package com.rdinfo2.logic

import com.rdinfo2.data.model.*
import kotlin.math.round

/**
 * Vereinfachter DosingCalculator ohne Konflikte
 * Funktioniert mit bereinigten Datenmodellen
 */
object DosingCalculator {

    /**
     * Hauptberechnung für Medikamentendosierung
     */
    fun calculateDose(
        medication: SimpleMedication,
        weightKg: Double,
        ageMonths: Int
    ): SimpleDoseCalculation {

        // Input validation
        if (weightKg <= 0) {
            return SimpleDoseCalculation.invalid("Gewicht muss größer als 0 kg sein")
        }

        if (ageMonths < 0) {
            return SimpleDoseCalculation.invalid("Alter kann nicht negativ sein")
        }

        // Altersbereich prüfen
        medication.ageRestrictions?.let { restrictions ->
            val minAge = restrictions.minAgeMonths ?: 0
            val maxAge = restrictions.maxAgeMonths ?: Int.MAX_VALUE

            if (ageMonths < minAge || ageMonths > maxAge) {
                val ageYears = ageMonths / 12.0
                return SimpleDoseCalculation.invalid(
                    "Medikament nicht für Alter ${String.format("%.1f", ageYears)} Jahre zugelassen"
                )
            }
        }

        // Dosisberechnung
        val calculatedDose = when {
            medication.dosePerKg != null -> {
                // Gewichtsbasierte Dosierung
                weightKg * medication.dosePerKg
            }
            medication.fixedDose != null -> {
                // Fixe Dosierung
                medication.fixedDose
            }
            else -> {
                return SimpleDoseCalculation.invalid("Keine Dosierungsregel definiert")
            }
        }

        // Maximaldosis anwenden
        val finalDose = medication.maxDose?.let { maxDose ->
            if (calculatedDose > maxDose) maxDose else calculatedDose
        } ?: calculatedDose

        // Rundung (medizinisch sinnvoll)
        val roundedDose = round(finalDose * 100) / 100  // 2 Dezimalstellen

        // Volumen berechnen
        val volume = roundedDose / medication.concentration
        val roundedVolume = round(volume * 10) / 10  // 1 Dezimalstelle

        // Berechnung als Text
        val calculation = buildCalculationText(medication, weightKg, ageMonths, calculatedDose, finalDose, roundedDose)

        // Warnungen generieren
        val warnings = generateWarnings(medication, roundedDose, weightKg, ageMonths)

        return SimpleDoseCalculation(
            dose = roundedDose,
            volume = roundedVolume,
            unit = medication.unit,
            calculation = calculation,
            warnings = warnings,
            isValid = true
        )
    }

    /**
     * Berechnung als lesbarer Text
     */
    private fun buildCalculationText(
        medication: SimpleMedication,
        weightKg: Double,
        ageMonths: Int,
        calculatedDose: Double,
        finalDose: Double,
        roundedDose: Double
    ): String {
        val ageYears = ageMonths / 12.0

        return buildString {
            appendLine("Berechnung für ${medication.name}:")
            appendLine("Patient: ${String.format("%.1f", ageYears)} Jahre, ${weightKg} kg")

            when {
                medication.dosePerKg != null -> {
                    appendLine("Dosierung: ${medication.dosePerKg} ${medication.unit}/kg")
                    appendLine("Berechnet: ${weightKg} kg × ${medication.dosePerKg} = ${String.format("%.2f", calculatedDose)} ${medication.unit}")
                }
                medication.fixedDose != null -> {
                    appendLine("Fixdosis: ${calculatedDose} ${medication.unit}")
                }
            }

            if (finalDose != calculatedDose) {
                appendLine("Maximum angewendet: ${String.format("%.2f", finalDose)} ${medication.unit}")
            }

            if (roundedDose != finalDose) {
                appendLine("Gerundet: ${String.format("%.2f", roundedDose)} ${medication.unit}")
            }

            val volume = roundedDose / medication.concentration
            appendLine("Volumen: ${String.format("%.1f", volume)} ml")
        }
    }

    /**
     * Warnungen generieren
     */
    private fun generateWarnings(
        medication: SimpleMedication,
        dose: Double,
        weightKg: Double,
        ageMonths: Int
    ): List<String> {
        val warnings = mutableListOf<String>()

        // Maximaldosis erreicht
        medication.maxDose?.let { maxDose ->
            if (dose >= maxDose * 0.95) {  // 95% der Maximaldosis
                warnings.add("⚠️ Nahe Maximaldosis erreicht")
            }
        }

        // Alterswarnung
        val ageYears = ageMonths / 12.0
        when {
            ageYears < 1 -> warnings.add("⚠️ Säugling - besondere Vorsicht")
            ageYears > 65 -> warnings.add("⚠️ Geriatrischer Patient")
        }

        // Gewichtswarnung
        when {
            weightKg < 5 -> warnings.add("⚠️ Sehr niedriges Gewicht")
            weightKg > 100 -> warnings.add("⚠️ Adipositas - Dosisanpassung erwägen")
        }

        return warnings
    }

    /**
     * Alle verfügbaren Medikamente abrufen
     */
    fun getAllMedications(): List<SimpleMedication> {
        return StandardMedications.medications
    }

    /**
     * Medikament nach ID finden
     */
    fun getMedicationById(id: String): SimpleMedication? {
        return StandardMedications.medications.find { it.id == id }
    }

    /**
     * Medikamente nach Kategorie filtern
     */
    fun getMedicationsByCategory(category: SimpleMedicationCategory): List<SimpleMedication> {
        return StandardMedications.medications.filter { it.category == category }
    }
}