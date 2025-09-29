// app/src/main/java/com/rdinfo2/logic/DosingCalculator.kt
package com.rdinfo2.logic

import com.rdinfo2.data.model.*
import kotlin.math.roundToInt

/**
 * Berechnet Medikamentendosierungen basierend auf Patientendaten
 */
object DosingCalculator {

    /**
     * Berechnet Dosis für ein Medikament und einen spezifischen UseCase
     */
    fun calculateDose(
        medication: Medication,
        useCase: UseCase,
        weightKg: Double,
        ageYears: Int,
        concentrationOverride: ConcentrationOverride? = null
    ): DoseCalculationResult {

        // Passende Dosierungsregel finden
        val applicableRule = findApplicableRule(useCase.dosingRules, ageYears, weightKg)
            ?: return DoseCalculationResult(
                isValid = false,
                dose = 0.0,
                doseUnit = "",
                volume = 0.0,
                volumeUnit = "ml",
                calculation = "",
                errorMessage = "Keine passende Dosierungsregel für Alter $ageYears Jahre und Gewicht $weightKg kg gefunden"
            )

        // Preparation ermitteln
        val preparation = medication.preparations.find { it.id == useCase.preparation.preparationId }
            ?: return DoseCalculationResult(
                isValid = false,
                dose = 0.0,
                doseUnit = "",
                volume = 0.0,
                volumeUnit = "ml",
                calculation = "",
                errorMessage = "Preparation nicht gefunden: ${useCase.preparation.preparationId}"
            )

        // Dosis berechnen
        val calculatedDose = when (applicableRule.calculation.type) {
            CalculationType.FIXED -> applicableRule.calculation.value
            CalculationType.PER_KG -> applicableRule.calculation.value * weightKg
        }

        // Konzentration bestimmen (Override oder Standard)
        val effectiveConcentration: Double
        val effectiveConcentrationUnit: String
        val effectiveVolume: Double
        val effectiveVolumeUnit: String

        if (concentrationOverride != null) {
            effectiveConcentration = concentrationOverride.concentration
            effectiveConcentrationUnit = concentrationOverride.concentrationUnit
            effectiveVolume = concentrationOverride.volume
            effectiveVolumeUnit = concentrationOverride.volumeUnit
        } else {
            effectiveConcentration = preparation.concentration
            effectiveConcentrationUnit = preparation.concentrationUnit
            effectiveVolume = preparation.volume
            effectiveVolumeUnit = preparation.volumeUnit
        }

        // Volumen berechnen
        val calculatedVolume = if (effectiveVolume > 0) {
            calculatedDose / (effectiveConcentration / effectiveVolume)
        } else {
            0.0 // Trockensubstanz
        }

        // Berechnungstext erstellen
        val calculationText = buildCalculationText(
            applicableRule = applicableRule,
            calculatedDose = calculatedDose,
            weightKg = weightKg,
            effectiveConcentration = effectiveConcentration,
            effectiveConcentrationUnit = effectiveConcentrationUnit,
            effectiveVolume = effectiveVolume,
            effectiveVolumeUnit = effectiveVolumeUnit,
            calculatedVolume = calculatedVolume,
            concentrationOverride = concentrationOverride
        )

        // Warnungen sammeln
        val warnings = mutableListOf<String>()

        // MaxDose prüfen
        applicableRule.maxTotalDose?.let { maxDose ->
            if (calculatedDose > maxDose.amount) {
                warnings.add("Maximaldosis überschritten: ${maxDose.amount} ${maxDose.unit} ${maxDose.timeframe}")
            }
        }

        // Abbruchkriterien hinzufügen
        applicableRule.abortCriteria?.let {
            warnings.add("Abbruchkriterien: $it")
        }

        return DoseCalculationResult(
            isValid = true,
            dose = calculatedDose,
            doseUnit = applicableRule.calculation.unit,
            volume = calculatedVolume,
            volumeUnit = effectiveVolumeUnit,
            calculation = calculationText,
            warnings = warnings,
            usedRule = applicableRule,
            usedPreparation = preparation,
            usedOverride = concentrationOverride
        )
    }

    /**
     * Findet die passende Dosierungsregel basierend auf Alter und Gewicht
     */
    private fun findApplicableRule(
        rules: List<DosingRule>,
        ageYears: Int,
        weightKg: Double
    ): DosingRule? {
        return rules.find { rule ->
            val ageMatch = (rule.minAge == null || ageYears >= rule.minAge) &&
                    (rule.maxAge == null || ageYears <= rule.maxAge)
            val weightMatch = (rule.minWeight == null || weightKg >= rule.minWeight) &&
                    (rule.maxWeight == null || weightKg <= rule.maxWeight)
            ageMatch && weightMatch
        }
    }

    /**
     * Erstellt lesbaren Berechnungstext
     */
    private fun buildCalculationText(
        applicableRule: DosingRule,
        calculatedDose: Double,
        weightKg: Double,
        effectiveConcentration: Double,
        effectiveConcentrationUnit: String,
        effectiveVolume: Double,
        effectiveVolumeUnit: String,
        calculatedVolume: Double,
        concentrationOverride: ConcentrationOverride?
    ): String {
        val sb = StringBuilder()

        // Dosisberechnung
        when (applicableRule.calculation.type) {
            CalculationType.FIXED -> {
                sb.append("Dosis: ${applicableRule.calculation.value} ${applicableRule.calculation.unit} (fix)\n")
            }
            CalculationType.PER_KG -> {
                sb.append("Dosis: ${applicableRule.calculation.value} ${applicableRule.calculation.unit}/kg × $weightKg kg = ")
                sb.append("${String.format("%.2f", calculatedDose)} ${applicableRule.calculation.unit}\n")
            }
        }

        // Konzentration
        if (concentrationOverride != null) {
            sb.append("\nKonzentration (manuell): ${concentrationOverride.getConcentrationDisplay()}\n")
        } else {
            sb.append("\nKonzentration: $effectiveConcentration $effectiveConcentrationUnit / $effectiveVolume $effectiveVolumeUnit\n")
        }

        // Volumenberechnung
        if (effectiveVolume > 0) {
            val concPerUnit = effectiveConcentration / effectiveVolume
            sb.append("= ${String.format("%.2f", concPerUnit)} $effectiveConcentrationUnit/$effectiveVolumeUnit\n")
            sb.append("\nVolumen: ${String.format("%.2f", calculatedDose)} $effectiveConcentrationUnit ÷ ")
            sb.append("${String.format("%.2f", concPerUnit)} $effectiveConcentrationUnit/$effectiveVolumeUnit = ")
            sb.append("${String.format("%.2f", calculatedVolume)} $effectiveVolumeUnit")
        } else {
            sb.append("\n⚠ Trockensubstanz - vor Gabe auflösen")
        }

        // Hinweis aus Rule
        applicableRule.note?.let {
            sb.append("\n\nHinweis: $it")
        }

        return sb.toString()
    }
}