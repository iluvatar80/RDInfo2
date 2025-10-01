// app/src/main/java/com/rdinfo2/data/model/Medication.kt
package com.rdinfo2.data.model

/**
 * Medikament-Datenmodell v3.0 für RDInfo2
 * Unterstützt flexible Konzentrationen, Darreichungsformen und Dosierungsregeln
 */
data class Medication(
    val id: String,
    val name: String,
    val genericName: String? = null,
    val category: MedicationCategory,
    val globalInfo: GlobalInfo,
    val preparations: List<Preparation>,
    val useCases: List<UseCase>
)

data class GlobalInfo(
    val indications: String,
    val mechanism: String,
    val sideEffects: String,
    val contraindications: String
)

data class Preparation(
    val id: String,
    val type: PreparationType,
    val concentration: Double,
    val concentrationUnit: String,
    val volume: Double,
    val volumeUnit: String,
    val description: String
) {
    /**
     * Berechnet Konzentration pro ml (nur wenn volumeUnit = "ml")
     */
    fun getConcentrationPerMl(): Double? {
        return if (volumeUnit == "ml" && volume > 0) {
            concentration / volume
        } else null
    }

    /**
     * Formatierte Anzeige der Konzentration
     */
    fun getConcentrationDisplay(): String {
        return "$concentration $concentrationUnit / $volume $volumeUnit"
    }

    /**
     * Formatierte Anzeige "1ml = X mg" (nur wenn relevant)
     */
    fun getConcentrationPerMlDisplay(): String? {
        return getConcentrationPerMl()?.let { perMl ->
            "1 ml = ${String.format("%.2f", perMl)} $concentrationUnit"
        }
    }

    /**
     * Prüft ob Trockensubstanz (volume = 0)
     */
    fun isDrySubstance(): Boolean {
        return volume == 0.0
    }
}

data class UseCase(
    val id: String,
    val name: String,
    val route: String,
    val preparation: UseCasePreparation,
    val dosingRules: List<DosingRule>
)

data class UseCasePreparation(
    val preparationId: String,
    val dilution: Dilution? = null,
    val nebulization: Nebulization? = null
)

data class Dilution(
    val ratio: String? = null,
    val solvent: String? = null,
    val finalConcentration: Double? = null,
    val finalConcentrationUnit: String? = null,
    val finalVolume: Double? = null,
    val finalVolumeUnit: String? = null,
    val note: String? = null
)

data class Nebulization(
    val dose: Double? = null,
    val doseUnit: String? = null,
    val oxygenFlow: Int? = null,
    val oxygenFlowUnit: String? = null
)

data class DosingRule(
    val ruleId: String,
    val minAge: Int?,
    val maxAge: Int?,
    val minWeight: Double?,
    val maxWeight: Double?,
    val calculation: DosageCalculation,
    val maxSingleDose: Double? = null,
    val maxTotalDose: MaxDose? = null,
    val repetitionInterval: String? = null,
    val abortCriteria: String? = null,
    val note: String? = null
)

data class DosageCalculation(
    val type: CalculationType,
    val value: Double,
    val unit: String
)

data class MaxDose(
    val amount: Double,
    val unit: String,
    val timeframe: String
)

enum class PreparationType {
    AMPULE,
    DRY_SUBSTANCE,
    SUPPOSITORY,
    SPRAY,
    NEBULIZER_SOLUTION,
    INFUSION,
    TABLET,
    AUTOINJECTOR,
    OTHER
}

enum class MedicationCategory {
    CARDIOVASCULAR,
    RESPIRATORY,
    NEUROLOGICAL,
    METABOLIC,
    ANALGESIC,
    SEDATIVE,
    ANTIDOTE,
    ANTIHISTAMINE,
    CORTICOSTEROID,
    ANTIHYPERTENSIVE,
    OTHER
}

enum class CalculationType {
    FIXED,
    PER_KG
}

/**
 * Manuelle Konzentrations-Override für Session
 */
data class ConcentrationOverride(
    val medicationId: String,
    val concentration: Double,
    val concentrationUnit: String,
    val volume: Double,
    val volumeUnit: String
) {
    fun getConcentrationDisplay(): String {
        return "$concentration $concentrationUnit / $volume $volumeUnit"
    }

    fun getConcentrationPerMl(): Double? {
        return if (volumeUnit == "ml" && volume > 0) {
            concentration / volume
        } else null
    }

    fun getConcentrationPerMlDisplay(): String? {
        return getConcentrationPerMl()?.let { perMl ->
            "1 ml = ${String.format("%.2f", perMl)} $concentrationUnit"
        }
    }
}

/**
 * Berechnungsergebnis mit allen Details
 */
data class DoseCalculationResult(
    val isValid: Boolean,
    val dose: Double,
    val doseUnit: String,
    val volume: Double,
    val volumeUnit: String,
    val calculation: String,
    val warnings: List<String> = emptyList(),
    val errorMessage: String? = null,
    val usedRule: DosingRule? = null,
    val usedPreparation: Preparation? = null,
    val usedOverride: ConcentrationOverride? = null
)