// app/src/main/java/com/rdinfo2/logic/DosingCalculator.kt
package com.rdinfo2.logic

import com.rdinfo2.data.model.*
import com.rdinfo2.data.patient.PatientData
import com.rdinfo2.data.patient.PatientDataManager
import kotlin.math.*

/**
 * Erweiterte Dosierungsberechnung für RDInfo2
 * Unterstützt alle Einheiten (mg, g, µg, I.E., Hub, etc.) und Darreichungsformen
 */
class DosingCalculator private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: DosingCalculator? = null

        fun getInstance(): DosingCalculator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DosingCalculator().also { INSTANCE = it }
            }
        }
    }

    /**
     * Hauptberechnungsfunktion
     */
    fun calculateDosage(
        medication: Medication,
        indication: String,
        route: String? = null,
        patientData: PatientData? = null
    ): DosingResult {
        val patient = patientData ?: PatientDataManager.getInstance().currentPatient.value
        val effectiveWeight = if (patient.isManualWeight) patient.weightKg else estimateWeight(patient.ageYears, patient.ageMonths)

        // Passende Dosierungsregel finden
        val applicableRules = medication.dosingRules
            .filter { it.indication.equals(indication, ignoreCase = true) }
            .filter { route == null || it.route.route.equals(route, ignoreCase = true) }
            .filter { matchesPatientCriteria(it, patient) }
            .sortedByDescending { calculateRulePriority(it, patient) }

        if (applicableRules.isEmpty()) {
            return DosingResult(
                success = false,
                errorMessage = "Keine passende Dosierungsregel gefunden für $indication bei ${patient.ageYears} Jahren, ${formatWeight(effectiveWeight)} kg",
                medication = medication,
                indication = indication,
                route = route
            )
        }

        val bestRule = applicableRules.first()

        // Dosierung berechnen
        return calculateFromRule(medication, bestRule, patient, effectiveWeight)
    }

    /**
     * Berechnet Dosierung basierend auf einer spezifischen Regel
     */
    private fun calculateFromRule(
        medication: Medication,
        rule: DosingRule,
        patient: PatientData,
        effectiveWeight: Double
    ): DosingResult {
        val baseAmount = when (rule.calculation.type) {
            CalculationType.FIXED -> rule.calculation.fixedAmount
            CalculationType.PER_KG -> rule.calculation.perKgAmount?.let { it * effectiveWeight }
            CalculationType.AGE_BASED -> calculateAgeBasedDose(rule, patient)
            CalculationType.CUSTOM -> calculateCustomDose(rule, patient, effectiveWeight)
        } ?: return DosingResult(
            success = false,
            errorMessage = "Dosierungsberechnung fehlgeschlagen - keine gültige Berechnungsformel",
            medication = medication,
            indication = rule.indication,
            route = rule.route.route
        )

        // Min/Max Grenzen anwenden
        val clampedAmount = clampDosage(baseAmount, rule.calculation)

        // Maximaldosis prüfen
        val (finalAmount, maxDoseWarning) = checkMaxDose(clampedAmount, rule.maxDose)

        // Rundung anwenden
        val roundedAmount = applyRounding(finalAmount, rule.calculation.roundingRules)

        // Volumen berechnen
        val volumeCalculation = calculateVolume(medication, roundedAmount, rule)

        return DosingResult(
            success = true,
            medication = medication,
            indication = rule.indication,
            route = rule.route.route,
            calculatedDose = DoseAmount(
                value = roundedAmount,
                unit = medication.unit.baseUnit,
                displayText = formatAmount(roundedAmount, medication.unit)
            ),
            volumeToAdminister = volumeCalculation.administerVolume,
            totalPreparationVolume = volumeCalculation.totalVolume,
            preparationInstructions = generatePreparationInstructions(medication, rule, volumeCalculation),
            warnings = buildWarnings(rule, maxDoseWarning, patient),
            notes = rule.notes,
            usedRule = rule
        )
    }

    /**
     * Prüft ob eine Dosierungsregel auf den Patienten zutrifft
     */
    private fun matchesPatientCriteria(rule: DosingRule, patient: PatientData): Boolean {
        // Altersbereich prüfen
        rule.ageRange?.let { ageRange ->
            if (ageRange.minYears != null && patient.ageYears < ageRange.minYears) return false
            if (ageRange.maxYears != null && patient.ageYears >= ageRange.maxYears) return false
        }

        // Gewichtsbereich prüfen
        rule.weightRange?.let { weightRange ->
            val weight = if (patient.isManualWeight) patient.weightKg else estimateWeight(patient.ageYears, patient.ageMonths)
            if (weightRange.minKg != null && weight < weightRange.minKg) return false
            if (weightRange.maxKg != null && weight >= weightRange.maxKg) return false
        }

        return true
    }

    /**
     * Berechnet Priorität einer Regel (höher = besser passend)
     */
    private fun calculateRulePriority(rule: DosingRule, patient: PatientData): Int {
        var priority = rule.priority

        // Spezifischere Regeln bevorzugen
        if (rule.ageRange != null) priority += 10
        if (rule.weightRange != null) priority += 10

        // Exakte Altersgruppen-Treffer bevorzugen
        rule.ageRange?.let { ageRange ->
            val ageSpan = (ageRange.maxYears ?: 120) - (ageRange.minYears ?: 0)
            if (ageSpan <= 5) priority += 20 // Sehr spezifische Altersgruppe
            else if (ageSpan <= 10) priority += 10 // Mäßig spezifisch
        }

        return priority
    }

    /**
     * Berechnet altersbasierte Dosierung
     */
    private fun calculateAgeBasedDose(rule: DosingRule, patient: PatientData): Double? {
        // Implementierung für altersbasierte Dosierung
        // Diese würde spezifische Logik für verschiedene Altersgruppen enthalten
        return rule.calculation.fixedAmount // Placeholder
    }

    /**
     * Berechnet benutzerdefinierte Dosierung
     */
    private fun calculateCustomDose(rule: DosingRule, patient: PatientData, effectiveWeight: Double): Double? {
        // Placeholder für custom formulas - könnte später erweitert werden
        return rule.calculation.fixedAmount
    }

    /**
     * Wendet Min/Max-Grenzen an
     */
    private fun clampDosage(amount: Double, calculation: DosageCalculation): Double {
        var result = amount
        calculation.minAmount?.let { min -> result = maxOf(result, min) }
        calculation.maxAmount?.let { max -> result = minOf(result, max) }
        return result
    }

    /**
     * Prüft Maximaldosis pro Einsatz/Tag
     */
    private fun checkMaxDose(amount: Double, maxDose: MaxDose?): Pair<Double, String?> {
        maxDose?.let { max ->
            if (amount > max.amount) {
                val warning = "Berechnete Dosis (${formatNumber(amount)} ${max.unit}) überschreitet Maximaldosis (${formatNumber(max.amount)} ${max.unit}) ${max.timeframe}. ${max.warning ?: ""}"
                return Pair(minOf(amount, max.amount), warning)
            }
        }
        return Pair(amount, null)
    }

    /**
     * Wendet Rundungsregeln an
     */
    private fun applyRounding(amount: Double, roundingRules: RoundingRule?): Double {
        roundingRules?.let { rules ->
            rules.roundToNearest?.let { step ->
                return round(amount / step) * step
            }
            return round(amount * 10.0.pow(rules.precision)) / 10.0.pow(rules.precision)
        }
        return round(amount * 100.0) / 100.0 // Default: 2 Dezimalstellen
    }

    /**
     * Berechnet Volumen und Zubereitung
     */
    private fun calculateVolume(
        medication: Medication,
        doseAmount: Double,
        rule: DosingRule
    ): VolumeCalculation {
        val defaultForm = medication.forms.firstOrNull { it.isDefault } ?: medication.forms.firstOrNull()

        defaultForm?.let { form ->
            when (form.type) {
                FormType.AMPOULE -> {
                    // Normale Ampulle: z.B. 1mg/1ml
                    val concentration = form.concentration.activeAmount / form.concentration.vehicleVolume
                    val administerVolume = doseAmount / concentration

                    form.preparation?.let { prep ->
                        // Mit Verdünnung
                        val finalConcentration = form.concentration.activeAmount / prep.finalVolume
                        val administerFromDilution = doseAmount / finalConcentration
                        return VolumeCalculation(
                            administerVolume = administerFromDilution,
                            totalVolume = prep.finalVolume,
                            needsDilution = true,
                            carrierFluid = prep.carrierFluid,
                            carrierVolume = prep.carrierVolume
                        )
                    } ?: return VolumeCalculation(
                        administerVolume = administerVolume,
                        totalVolume = administerVolume,
                        needsDilution = false
                    )
                }

                FormType.DRY_SUBSTANCE -> {
                    // Trockensubstanz: z.B. 500mg + 5ml Aqua
                    form.preparation?.let { prep ->
                        val finalConcentration = form.concentration.activeAmount / prep.finalVolume
                        val administerVolume = doseAmount / finalConcentration
                        return VolumeCalculation(
                            administerVolume = administerVolume,
                            totalVolume = prep.finalVolume,
                            needsDilution = true,
                            carrierFluid = prep.carrierFluid,
                            carrierVolume = prep.carrierVolume
                        )
                    }
                }

                FormType.SPRAY -> {
                    // Spray: z.B. 0.4mg/Hub
                    val hubsNeeded = doseAmount / form.concentration.activeAmount
                    return VolumeCalculation(
                        administerVolume = hubsNeeded,
                        totalVolume = hubsNeeded,
                        needsDilution = false,
                        isCountUnit = true,
                        countUnit = "Hub"
                    )
                }

                FormType.INFUSION -> {
                    // Fertige Infusion: z.B. 1000mg/100ml
                    val concentration = form.concentration.activeAmount / form.concentration.vehicleVolume
                    val administerVolume = doseAmount / concentration
                    return VolumeCalculation(
                        administerVolume = administerVolume,
                        totalVolume = form.concentration.vehicleVolume,
                        needsDilution = false
                    )
                }

                else -> {
                    // Andere Darreichungsformen (Tabletten, Tropfen, etc.)
                    val unitsNeeded = doseAmount / form.concentration.activeAmount
                    return VolumeCalculation(
                        administerVolume = unitsNeeded,
                        totalVolume = unitsNeeded,
                        needsDilution = false,
                        isCountUnit = true,
                        countUnit = form.concentration.vehicleUnit
                    )
                }
            }
        }

        // Fallback wenn keine Darreichungsform gefunden
        return VolumeCalculation(
            administerVolume = 0.0,
            totalVolume = 0.0,
            needsDilution = false,
            error = "Keine gültige Darreichungsform gefunden"
        )
    }

    /**
     * Generiert Zubereitungsanweisungen
     */
    private fun generatePreparationInstructions(
        medication: Medication,
        rule: DosingRule,
        volumeCalc: VolumeCalculation
    ): String? {
        if (!volumeCalc.needsDilution) return null

        return buildString {
            append("${medication.name} ${formatNumber(volumeCalc.administerVolume)} ml")
            volumeCalc.carrierFluid?.let { carrier ->
                append(" aus ${formatNumber(volumeCalc.totalVolume)} ml ")
                append("(${formatNumber(volumeCalc.carrierVolume ?: 0.0)} ml $carrier)")
            }
        }
    }

    /**
     * Erstellt Warnungen
     */
    private fun buildWarnings(rule: DosingRule, maxDoseWarning: String?, patient: PatientData): List<String> {
        val warnings = mutableListOf<String>()

        maxDoseWarning?.let { warnings.add(it) }

        rule.warnings.forEach { warnings.add(it) }

        // Patientenspezifische Warnungen
        if (patient.isPregnant) {
            warnings.add("Achtung: Patientin ist schwanger (${patient.gestationalWeek ?: "unbekannte"} SSW)")
        }

        if (patient.allergies.isNotEmpty()) {
            warnings.add("Bekannte Allergien: ${patient.allergies.joinToString(", ")}")
        }

        return warnings
    }

    /**
     * Schätzt Gewicht basierend auf Alter
     */
    private fun estimateWeight(years: Int, months: Int): Double {
        val totalMonths = years * 12 + months

        return when {
            totalMonths <= 12 -> 4.0 + (totalMonths * 0.5)
            years in 1..5 -> 2.0 * years + 8.0
            years in 6..12 -> 3.0 * years + 7.0
            years in 13..17 -> 50.0 + (years - 13) * 5.0
            else -> 70.0
        }
    }

    // Hilfsfunktionen für Formatierung
    private fun formatAmount(amount: Double, unit: MedicationUnit): String {
        val formattedNumber = formatNumber(amount)
        return "$formattedNumber ${unit.displayName}"
    }

    private fun formatNumber(number: Double): String {
        return if (number == number.toInt().toDouble()) {
            number.toInt().toString()
        } else {
            String.format("%.2f", number).trimEnd('0').trimEnd('.')
        }
    }

    private fun formatWeight(weight: Double): String {
        return String.format("%.1f", weight)
    }
}

/**
 * Ergebnis einer Dosierungsberechnung
 */
data class DosingResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val medication: Medication,
    val indication: String,
    val route: String? = null,
    val calculatedDose: DoseAmount? = null,
    val volumeToAdminister: Double? = null,
    val totalPreparationVolume: Double? = null,
    val preparationInstructions: String? = null,
    val warnings: List<String> = emptyList(),
    val notes: String? = null,
    val usedRule: DosingRule? = null
)

/**
 * Dosierungsangabe mit Einheit
 */
data class DoseAmount(
    val value: Double,
    val unit: String,
    val displayText: String
)

/**
 * Volumenberechnung
 */
data class VolumeCalculation(
    val administerVolume: Double,
    val totalVolume: Double,
    val needsDilution: Boolean,
    val carrierFluid: String? = null,
    val carrierVolume: Double? = null,
    val isCountUnit: Boolean = false,
    val countUnit: String? = null,
    val error: String? = null
)