// app/src/main/java/com/rdinfo2/data/repository/MedicationRepository.kt
package com.rdinfo2.data.repository

import android.content.Context
import com.rdinfo2.data.model.*
import com.rdinfo2.data.json.JsonMedicationLoader

/**
 * Repository für Medikamenten-Daten
 * JETZT MIT JSON-INTEGRATION!
 * Lädt Medikamente aus medications.json statt hardcoded Factory
 */
class MedicationRepository private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: MedicationRepository? = null

        fun getInstance(context: Context): MedicationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MedicationRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        // Backward compatibility - für bestehende Aufrufe ohne Context
        fun getInstance(): MedicationRepository {
            return INSTANCE ?: throw IllegalStateException(
                "MedicationRepository muss erst mit Context initialisiert werden"
            )
        }
    }

    // JSON-Loader
    private val jsonLoader = JsonMedicationLoader(context)

    // Medikamenten-Datenbank - JETZT AUS JSON!
    private var _medications: List<Medication>? = null
    private val medications: List<Medication>
        get() {
            if (_medications == null) {
                _medications = jsonLoader.loadMedications()
            }
            return _medications!!
        }

    /**
     * Alle verfügbaren Medikamente
     */
    fun getAllMedications(): List<Medication> {
        return medications
    }

    /**
     * Medikament nach ID finden
     */
    fun getMedicationById(id: String): Medication? {
        return medications.find { it.id == id }
    }

    /**
     * Medikament nach Namen finden
     */
    fun getMedicationByName(name: String): Medication? {
        return medications.find { it.name.equals(name, ignoreCase = true) }
    }

    /**
     * Dosierung für spezifische Situation berechnen
     * DIESE Funktion ersetzt die hardcoded when-statements!
     */
    fun calculateDosage(
        medicationId: String,
        indication: String,
        ageYears: Int,
        ageMonths: Int,
        weightKg: Double
    ): DosageResult {

        val medication = getMedicationById(medicationId)
            ?: return DosageResult.error("Medikament '$medicationId' nicht gefunden")

        val matchingIndication = medication.indications.find {
            it.name.equals(indication, ignoreCase = true)
        } ?: return DosageResult.error("Indikation '$indication' für ${medication.name} nicht verfügbar")

        val ageGroup = determineAgeGroup(ageYears, ageMonths)
        val dosageRule = findBestDosageRule(matchingIndication.dosageRules, ageGroup)
            ?: return DosageResult.error("Keine passende Dosierungsregel gefunden")

        val calculatedDose = performCalculation(dosageRule.calculation, weightKg)

        return DosageResult.success(
            medication = medication.name,
            indication = indication,
            dose = calculatedDose,
            unit = dosageRule.unit,
            volume = dosageRule.volume,
            route = matchingIndication.route,
            preparation = matchingIndication.preparation,
            note = dosageRule.note,
            warnings = getWarnings(medication, matchingIndication, calculatedDose),
            maxDose = matchingIndication.maxDose
        )
    }

    /**
     * Bestimmt Altersgruppe basierend auf Alter
     */
    private fun determineAgeGroup(years: Int, months: Int): AgeGroup {
        val totalMonths = years * 12 + months

        return when {
            totalMonths == 0 -> AgeGroup.NEONATE
            totalMonths <= 12 -> AgeGroup.INFANT
            years <= 2 -> AgeGroup.TODDLER
            years <= 11 -> AgeGroup.CHILD
            years <= 17 -> AgeGroup.ADOLESCENT
            years <= 64 -> AgeGroup.ADULT
            else -> AgeGroup.GERIATRIC
        }
    }

    /**
     * Findet beste passende Dosierungsregel
     */
    private fun findBestDosageRule(rules: List<DosageRule>, ageGroup: AgeGroup): DosageRule? {
        // Exakte Übereinstimmung bevorzugen
        rules.find { it.ageGroup == ageGroup }?.let { return it }

        // ALL_AGES als Fallback
        rules.find { it.ageGroup == AgeGroup.ALL_AGES }?.let { return it }

        // Erwachsenen-Dosierung als letzter Fallback für ältere Patienten
        if (ageGroup == AgeGroup.GERIATRIC) {
            rules.find { it.ageGroup == AgeGroup.ADULT }?.let { return it }
        }

        return null
    }

    /**
     * Führt Dosierungsberechnung durch
     */
    private fun performCalculation(calculation: DosageCalculation, weightKg: Double): Double {
        val rawDose = when (calculation.type) {
            CalculationType.FIXED -> calculation.value
            CalculationType.PER_KG -> calculation.value * weightKg
            CalculationType.PER_YEAR -> calculation.value // Vereinfacht für jetzt
            CalculationType.FORMULA -> calculation.value // Vereinfacht für jetzt
        }

        // Min/Max Limits anwenden
        var finalDose = rawDose
        calculation.minDose?.let { min -> finalDose = maxOf(finalDose, min) }
        calculation.maxDose?.let { max -> finalDose = minOf(finalDose, max) }

        return finalDose
    }

    /**
     * Sammelt alle Warnungen für die Dosierung
     */
    private fun getWarnings(
        medication: Medication,
        indication: Indication,
        calculatedDose: Double
    ): List<String> {
        val warnings = mutableListOf<String>()

        // Medikamenten-spezifische Warnungen
        warnings.addAll(medication.warnings)

        // Maximaldosis-Warnung
        indication.maxDose?.let { maxDose ->
            if (calculatedDose > maxDose.amount) {
                val warning = "Berechnete Dosis (${String.format("%.2f", calculatedDose)}) " +
                        "überschreitet Maximaldosis (${maxDose.amount} ${maxDose.unit}) ${maxDose.timeframe}"
                warnings.add(warning)

                maxDose.warning?.let { warnings.add(it) }
            }
        }

        return warnings
    }

    /**
     * Hilfsfunktion: Alle verfügbaren Indikationen für ein Medikament
     */
    fun getIndicationsFor(medicationId: String): List<String> {
        return getMedicationById(medicationId)?.indications?.map { it.name } ?: emptyList()
    }

    /**
     * Hilfsfunktion: Medikamenten-Namen für UI-Dropdown
     */
    fun getMedicationNames(): List<String> {
        return medications.map { it.name }.sorted()
    }
}

/**
 * Ergebnis einer Dosierungsberechnung
 */
data class DosageResult(
    val isSuccess: Boolean,
    val medication: String? = null,
    val indication: String? = null,
    val dose: Double? = null,
    val unit: String? = null,
    val volume: String? = null,
    val route: String? = null,
    val preparation: String? = null,
    val note: String? = null,
    val warnings: List<String> = emptyList(),
    val maxDose: MaxDose? = null,
    val errorMessage: String? = null
) {
    companion object {
        fun success(
            medication: String,
            indication: String,
            dose: Double,
            unit: String,
            volume: String? = null,
            route: String? = null,
            preparation: String? = null,
            note: String? = null,
            warnings: List<String> = emptyList(),
            maxDose: MaxDose? = null
        ): DosageResult {
            return DosageResult(
                isSuccess = true,
                medication = medication,
                indication = indication,
                dose = dose,
                unit = unit,
                volume = volume,
                route = route,
                preparation = preparation,
                note = note,
                warnings = warnings,
                maxDose = maxDose
            )
        }

        fun error(message: String): DosageResult {
            return DosageResult(
                isSuccess = false,
                errorMessage = message
            )
        }
    }

    /**
     * Formatierte Dosierungsanzeige für UI
     */
    fun getFormattedDose(): String? {
        return dose?.let { "${String.format("%.2f", it)} $unit" }
    }

    /**
     * Vollständige Anweisungstext für UI
     */
    fun getFullInstruction(): String? {
        if (!isSuccess) return errorMessage

        val parts = mutableListOf<String>()

        getFormattedDose()?.let { parts.add("Dosis: $it") }
        volume?.let { parts.add("Volumen: $it") }
        route?.let { parts.add("Route: $it") }
        preparation?.let { parts.add("Zubereitung: $it") }
        note?.let { parts.add("Hinweis: $it") }

        return if (parts.isNotEmpty()) parts.joinToString("\n") else null
    }
}