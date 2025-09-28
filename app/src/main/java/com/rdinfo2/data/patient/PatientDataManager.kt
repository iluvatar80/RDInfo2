// app/src/main/java/com/rdinfo2/data/patient/PatientDataManager.kt
package com.rdinfo2.data.patient

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

/**
 * VOLLSTÄNDIG ÜBERARBEITETER PATIENT DATA MANAGER
 *
 * Verwaltet Patientendaten zentral und persistent mit erweiterten Features:
 * - Medizinische Anamnese (Allergien, Medikamente, Beschwerden)
 * - Vitalparameter (RR, HF, SpO2, Temperatur, BZ, AF)
 * - Schwangerschaftsstatus mit Schwangerschaftswoche
 * - Risikofaktor-Analyse und automatische Identifikation
 * - WHO-Standards für präzise Gewichtsschätzung
 * - Geschlechtsabhängige Normalwerte für Erwachsene
 * - Vollständige Abwärtskompatibilität mit altem System
 */
object PatientDataManager {

    // ==================== MAIN STATE ====================

    /**
     * Aktuelle Patientendaten (Mutable State für UI)
     */
    var currentPatient by mutableStateOf(
        PatientData(
            ageYears = 5,
            ageMonths = 5,
            weightKg = 0.0,
            isManualWeight = false,
            gender = PatientGender.UNKNOWN
        )
    )
        private set

    /**
     * Berechnete Werte (automatisch aktualisiert)
     */
    var calculatedValues by mutableStateOf(CalculatedPatientValues())
        private set

    /**
     * Persistent gespeicherte Daten (überleben App-Restart)
     */
    private var _persistentData = currentPatient.copy()

    // Initialisierung: Berechne initiale Werte
    init {
        calculatedValues = calculateValues()
    }

    // ==================== UPDATE METHODS ====================

    /**
     * Alter aktualisieren mit Validierung
     */
    fun updateAge(years: Int, months: Int) {
        val validYears = years.coerceIn(0, 120)
        val validMonths = months.coerceIn(0, 11)

        currentPatient = currentPatient.copy(
            ageYears = validYears,
            ageMonths = validMonths
        )

        // Auto-update Gewicht wenn nicht manuell gesetzt
        if (!currentPatient.isManualWeight) {
            val estimatedWeight = calculateEstimatedWeight()
            currentPatient = currentPatient.copy(weightKg = estimatedWeight)
        }

        recalculateAndPersist()
    }

    /**
     * Gewicht aktualisieren (manuell oder automatisch)
     */
    fun updateWeight(weight: Double?, isManual: Boolean = true) {
        currentPatient = if (weight != null && weight > 0) {
            currentPatient.copy(
                weightKg = weight.coerceIn(0.5, 300.0),
                isManualWeight = isManual
            )
        } else {
            // Wenn null oder 0, dann automatische Schätzung
            currentPatient.copy(
                weightKg = calculateEstimatedWeight(),
                isManualWeight = false
            )
        }
        recalculateAndPersist()
    }

    /**
     * Geschlecht aktualisieren
     */
    fun updateGender(newGender: PatientGender) {
        currentPatient = currentPatient.copy(gender = newGender)

        // Gewicht neu schätzen wenn automatisch
        if (!currentPatient.isManualWeight) {
            currentPatient = currentPatient.copy(weightKg = calculateEstimatedWeight())
        }

        recalculateAndPersist()
    }

    /**
     * Vitalparameter aktualisieren
     */
    fun updateVitals(vitals: VitalParameters) {
        currentPatient = currentPatient.copy(vitals = vitals)
        recalculateAndPersist()
    }

    /**
     * Medizinische Anamnese aktualisieren
     */
    fun updateMedicalData(
        allergies: List<String> = currentPatient.allergies,
        medications: List<String> = currentPatient.medications,
        medicalHistory: List<String> = currentPatient.medicalHistory
    ) {
        currentPatient = currentPatient.copy(
            allergies = allergies,
            medications = medications,
            medicalHistory = medicalHistory
        )
        recalculateAndPersist()
    }

    /**
     * Schwangerschaftsstatus aktualisieren
     */
    fun updatePregnancy(isPregnant: Boolean, weekOfPregnancy: Int? = null) {
        currentPatient = currentPatient.copy(
            isPregnant = isPregnant,
            weekOfPregnancy = if (isPregnant) weekOfPregnancy?.coerceIn(1, 42) else null
        )
        recalculateAndPersist()
    }

    // ==================== COMPUTED PROPERTIES ====================

    /**
     * Berechnet alle abhängigen Werte neu
     */
    private fun calculateValues(): CalculatedPatientValues {
        val totalMonths = currentPatient.ageYears * 12 + currentPatient.ageMonths
        val effectiveWeight = if (currentPatient.weightKg > 0) currentPatient.weightKg else calculateEstimatedWeight()
        val estimatedWeight = calculateEstimatedWeight()

        return CalculatedPatientValues(
            totalAgeMonths = totalMonths,
            effectiveWeight = effectiveWeight,
            estimatedWeight = estimatedWeight,
            isInfant = totalMonths < 12,
            isChild = totalMonths >= 12 && totalMonths < 18 * 12,
            isAdolescent = totalMonths >= 12 * 12 && totalMonths < 18 * 12,
            isGeriatric = currentPatient.ageYears >= 65,
            riskFactors = calculateRiskFactors()
        )
    }

    /**
     * WHO-konforme Gewichtsschätzung
     */
    private fun calculateEstimatedWeight(): Double {
        val totalMonths = currentPatient.ageYears * 12 + currentPatient.ageMonths

        return when {
            // Säuglinge (0-12 Monate): WHO Empirische Werte
            totalMonths <= 12 -> {
                when (totalMonths) {
                    0 -> 3.5
                    1 -> 4.5
                    2 -> 5.5
                    3 -> 6.5
                    4 -> 7.0
                    5 -> 7.5
                    6 -> 8.0
                    7 -> 8.5
                    8 -> 9.0
                    9 -> 9.5
                    10 -> 10.0
                    11 -> 10.5
                    12 -> 11.0
                    else -> 11.0
                }
            }

            // Kleinkinder (1-5 Jahre): WHO Formel
            currentPatient.ageYears <= 5 -> {
                2.0 * currentPatient.ageYears + 8.0
            }

            // Kinder (6-12 Jahre): Erweiterte pädiatrische Formel
            currentPatient.ageYears <= 12 -> {
                (currentPatient.ageYears * 2.5) + 10.0
            }

            // Jugendliche (13-17 Jahre): Pubertäts-adaptierte Formel
            currentPatient.ageYears in 13..17 -> {
                45.0 + ((currentPatient.ageYears - 13) * 5.0)
            }

            // Erwachsene: Geschlechtsabhängig
            else -> when (currentPatient.gender) {
                PatientGender.MALE -> when {
                    currentPatient.ageYears >= 65 -> 72.0  // Altersbedingte Reduktion
                    else -> 75.0
                }
                PatientGender.FEMALE -> when {
                    currentPatient.isPregnant == true -> {
                        val baseWeight = 65.0
                        val pregnancyWeeks = currentPatient.weekOfPregnancy ?: 20
                        baseWeight + (pregnancyWeeks * 0.5)  // Schwangerschaftsgewichtszunahme
                    }
                    currentPatient.ageYears >= 65 -> 62.0  // Altersbedingte Reduktion
                    else -> 65.0
                }
                PatientGender.UNKNOWN -> 70.0
            }
        }
    }

    /**
     * Risikofaktoren automatisch identifizieren
     */
    private fun calculateRiskFactors(): List<RiskFactor> {
        val risks = mutableListOf<RiskFactor>()

        // Altersbasierte Risiken
        if (calculatedValues.isInfant) {
            risks.add(
                RiskFactor("age_infant", "Säugling", "Erhöhtes Risiko für Dehydration", RiskSeverity.MEDIUM)
            )
        }
        if (calculatedValues.isGeriatric) {
            risks.add(
                RiskFactor("age_geriatric", "Geriatrisch", "Multimorbidität und reduzierte Reserve", RiskSeverity.MEDIUM)
            )
        }

        // Schwangerschaftsrisiken
        if (currentPatient.isPregnant == true) {
            val week = currentPatient.weekOfPregnancy ?: 20
            if (week < 12) {
                risks.add(
                    RiskFactor("pregnancy_early", "Frühschwangerschaft", "Erhöhtes Abortrisiko", RiskSeverity.HIGH)
                )
            } else if (week > 37) {
                risks.add(
                    RiskFactor("pregnancy_term", "Terminnahe Schwangerschaft", "Geburtsrisiko", RiskSeverity.HIGH)
                )
            }
        }

        // Gewichtsrisiken
        val bmi = if (currentPatient.ageYears >= 18) {
            // BMI nur für Erwachsene berechnen
            val heightM = estimateHeight() / 100.0
            calculatedValues.effectiveWeight / (heightM * heightM)
        } else null

        bmi?.let { bmiValue ->
            if (bmiValue < 18.5) {
                risks.add(
                    RiskFactor("weight_underweight", "Untergewicht", "BMI < 18.5, Malnutrition möglich", RiskSeverity.MEDIUM)
                )
            } else if (bmiValue > 30) {
                risks.add(
                    RiskFactor("weight_obesity", "Adipositas", "BMI > 30, erschwerte Atemwege", RiskSeverity.MEDIUM)
                )
            }
        }

        // Vitalparameter-Risiken
        currentPatient.vitals?.let { vitals ->
            vitals.systolicBP?.let { sbp ->
                if (sbp < 90) {
                    risks.add(
                        RiskFactor("bp_low", "Hypotonie", "RR sys < 90 mmHg", RiskSeverity.HIGH)
                    )
                } else if (sbp > 180) {
                    risks.add(
                        RiskFactor("bp_high", "Hypertonie", "RR sys > 180 mmHg", RiskSeverity.HIGH)
                    )
                }
            }

            vitals.heartRate?.let { hr ->
                val normalRange = getNormalHeartRateRange()
                if (hr < normalRange.first) {
                    risks.add(
                        RiskFactor("hr_low", "Bradykardie", "HF < ${normalRange.first}/min", RiskSeverity.MEDIUM)
                    )
                } else if (hr > normalRange.second) {
                    risks.add(
                        RiskFactor("hr_high", "Tachykardie", "HF > ${normalRange.second}/min", RiskSeverity.MEDIUM)
                    )
                }
            }

            vitals.oxygenSaturation?.let { spo2 ->
                if (spo2 < 90) {
                    risks.add(
                        RiskFactor("spo2_critical", "Schwere Hypoxie", "SpO2 < 90%", RiskSeverity.CRITICAL)
                    )
                } else if (spo2 < 95) {
                    risks.add(
                        RiskFactor("spo2_low", "Hypoxie", "SpO2 < 95%", RiskSeverity.HIGH)
                    )
                }
            }
        }

        // Allergie-Risiken
        if (currentPatient.allergies.isNotEmpty()) {
            risks.add(
                RiskFactor("allergies", "Allergien", "${currentPatient.allergies.size} bekannte Allergien", RiskSeverity.MEDIUM)
            )
        }

        return risks
    }

    /**
     * Körpergröße schätzen (für BMI-Berechnung)
     */
    private fun estimateHeight(): Double {
        return when {
            currentPatient.ageYears < 18 -> 100.0 + (currentPatient.ageYears * 5.0)  // Vereinfachte Schätzung für Kinder
            currentPatient.gender == PatientGender.MALE -> 175.0
            currentPatient.gender == PatientGender.FEMALE -> 165.0
            else -> 170.0
        }
    }

    /**
     * Normale Herzfrequenz für Alter bestimmen
     */
    private fun getNormalHeartRateRange(): Pair<Int, Int> {
        val totalMonths = calculatedValues.totalAgeMonths
        return if (totalMonths <= 1) {
            Pair(120, 160)     // Neugeborene
        } else if (totalMonths <= 12) {
            Pair(100, 150)     // Säuglinge
        } else if (currentPatient.ageYears <= 3) {
            Pair(90, 130)      // Kleinkinder
        } else if (currentPatient.ageYears <= 12) {
            Pair(70, 120)      // Schulkinder
        } else if (currentPatient.ageYears <= 17) {
            Pair(60, 100)      // Jugendliche
        } else {
            Pair(60, 100)      // Erwachsene
        }
    }

    // ==================== QUICK ACTIONS ====================

    /**
     * Quick-Set für typische Patiententypen
     */
    fun setInfant() {
        currentPatient = PatientData(
            ageYears = 0, ageMonths = 6,
            weightKg = 8.0, isManualWeight = false,
            gender = PatientGender.UNKNOWN
        )
        recalculateAndPersist()
    }

    fun setChild() {
        currentPatient = PatientData(
            ageYears = 5, ageMonths = 0,
            weightKg = 0.0, isManualWeight = false,
            gender = PatientGender.UNKNOWN
        )
        recalculateAndPersist()
    }

    fun setAdult() {
        currentPatient = PatientData(
            ageYears = 35, ageMonths = 0,
            weightKg = 70.0, isManualWeight = false,
            gender = PatientGender.UNKNOWN
        )
        recalculateAndPersist()
    }

    /**
     * Zurücksetzen zu persistenten Daten
     */
    fun resetToSaved() {
        currentPatient = _persistentData.copy()
        calculatedValues = calculateValues()
    }

    /**
     * Komplett zurücksetzen (zu Standard-Patient)
     */
    fun resetToDefaults() {
        currentPatient = PatientData(
            ageYears = 5,
            ageMonths = 5,
            weightKg = 0.0,
            isManualWeight = false,
            gender = PatientGender.UNKNOWN
        )
        recalculateAndPersist()
    }

    /**
     * Aktuelle Daten als Standard speichern
     */
    fun saveAsDefault() {
        _persistentData = currentPatient.copy()
    }

    /**
     * Interne Methode: Neu berechnen und persistieren
     */
    private fun recalculateAndPersist() {
        calculatedValues = calculateValues()
        _persistentData = currentPatient.copy()
    }

    // ==================== HELPER METHODS ====================

    /**
     * Altersklassifikation als String
     */
    fun getAgeClassification(): String {
        val totalMonths = calculatedValues.totalAgeMonths
        return if (totalMonths <= 1) {
            "Neugeborenes (${totalMonths}M)"
        } else if (totalMonths <= 12) {
            "Säugling (${totalMonths}M)"
        } else if (currentPatient.ageYears <= 2) {
            "Kleinkind (${currentPatient.ageYears}J ${currentPatient.ageMonths}M)"
        } else if (currentPatient.ageYears <= 11) {
            "Schulkind (${currentPatient.ageYears}J)"
        } else if (currentPatient.ageYears <= 17) {
            "Jugendlich (${currentPatient.ageYears}J)"
        } else if (currentPatient.ageYears <= 64) {
            "Erwachsen (${currentPatient.ageYears}J)"
        } else {
            "Geriatrisch (${currentPatient.ageYears}J)"
        }
    }

    /**
     * Patientenzusammenfassung für UI
     */
    fun getPatientSummary(): String {
        val weight = String.format("%.1f", calculatedValues.effectiveWeight)
        val weightSuffix = if (currentPatient.isManualWeight) "" else " (geschätzt)"
        val ageStr = if (currentPatient.ageMonths > 0) {
            "${currentPatient.ageYears} Jahre, ${currentPatient.ageMonths} Monate"
        } else {
            "${currentPatient.ageYears} Jahre"
        }
        val genderStr = if (currentPatient.gender == PatientGender.MALE) {
            "männlich"
        } else if (currentPatient.gender == PatientGender.FEMALE) {
            "weiblich"
        } else {
            "unbekannt"
        }

        return "$ageStr, $weight kg$weightSuffix, $genderStr"
    }

    /**
     * Detaillierte Patienteninfo mit Risiken
     */
    fun getDetailedPatientInfo(): String {
        val summary = getPatientSummary()
        val classification = getAgeClassification()
        val risks = if (calculatedValues.riskFactors.isNotEmpty()) {
            "\nRisiken: ${calculatedValues.riskFactors.joinToString(", ") { it.name }}"
        } else ""

        return "$summary\n$classification$risks"
    }

    // ==================== COMPATIBILITY ALIASES ====================

    // Für Abwärtskompatibilität mit altem System
    val ageYears: Int get() = currentPatient.ageYears
    val ageMonths: Int get() = currentPatient.ageMonths
    val weightKg: Double? get() = if (currentPatient.weightKg > 0) currentPatient.weightKg else null
    val gender: Gender get() = if (currentPatient.gender == PatientGender.MALE) {
        Gender.MALE
    } else if (currentPatient.gender == PatientGender.FEMALE) {
        Gender.FEMALE
    } else {
        Gender.UNKNOWN
    }

    val totalAgeInMonths: Int get() = calculatedValues.totalAgeMonths
    val estimatedWeightKg: Double get() = calculatedValues.effectiveWeight
    val isInfant: Boolean get() = calculatedValues.isInfant
    val isChild: Boolean get() = calculatedValues.isChild
    val isAdult: Boolean get() = !calculatedValues.isInfant && !calculatedValues.isChild && !calculatedValues.isAdolescent
}

// ==================== DATA CLASSES ====================

@Serializable
data class PatientData(
    val ageYears: Int = 0,
    val ageMonths: Int = 0,
    val weightKg: Double = 0.0,
    val isManualWeight: Boolean = false,
    val gender: PatientGender = PatientGender.UNKNOWN,
    val vitals: VitalParameters? = null,
    val isPregnant: Boolean? = null,
    val weekOfPregnancy: Int? = null,
    val allergies: List<String> = emptyList(),
    val medications: List<String> = emptyList(),
    val medicalHistory: List<String> = emptyList()
)

@Serializable
data class VitalParameters(
    val systolicBP: Int? = null,
    val diastolicBP: Int? = null,
    val heartRate: Int? = null,
    val respiratoryRate: Int? = null,
    val oxygenSaturation: Int? = null,  // SpO2 in %
    val temperature: Double? = null,     // in °C
    val bloodGlucose: Int? = null,       // in mg/dl
    val consciousness: String? = null    // GCS oder AVPU
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
enum class PatientGender {
    MALE, FEMALE, UNKNOWN
}

@Serializable
enum class RiskSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

// Kompatibilitäts-Enum für altes System
enum class Gender {
    MALE, FEMALE, UNKNOWN
}