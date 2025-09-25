// app/src/main/java/com/rdinfo2/data/patient/PatientDataManager.kt
package com.rdinfo2.data.patient

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.math.*

/**
 * Zentrale Verwaltung der Patientendaten
 * Systemweite Verfügbarkeit für alle Berechnungen und Algorithmen
 */
class PatientDataManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: PatientDataManager? = null

        fun getInstance(): PatientDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PatientDataManager().also { INSTANCE = it }
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Current patient data state
    private val _currentPatient = MutableStateFlow(PatientData())
    val currentPatient: StateFlow<PatientData> = _currentPatient.asStateFlow()

    // Calculated values based on patient data
    private val _calculatedValues = MutableStateFlow(CalculatedPatientValues())
    val calculatedValues: StateFlow<CalculatedPatientValues> = _calculatedValues.asStateFlow()

    /**
     * Aktualisiert Patientenalter
     */
    fun updateAge(years: Int, months: Int = 0) {
        val current = _currentPatient.value
        val updated = current.copy(
            ageYears = years.coerceIn(0, 120),
            ageMonths = months.coerceIn(0, 11),
            lastUpdated = System.currentTimeMillis()
        )
        _currentPatient.value = updated
        recalculateValues()
    }

    /**
     * Aktualisiert Patientengewicht
     */
    fun updateWeight(weightKg: Double, isManual: Boolean = true) {
        val current = _currentPatient.value
        val updated = current.copy(
            weightKg = weightKg.coerceIn(0.5, 300.0),
            isManualWeight = isManual,
            lastUpdated = System.currentTimeMillis()
        )
        _currentPatient.value = updated
        recalculateValues()
    }

    /**
     * Aktualisiert Geschlecht
     */
    fun updateGender(gender: PatientGender) {
        val current = _currentPatient.value
        val updated = current.copy(
            gender = gender,
            lastUpdated = System.currentTimeMillis()
        )
        _currentPatient.value = updated
        recalculateValues()
    }

    /**
     * Aktualisiert Schwangerschaftsstatus
     */
    fun updatePregnancy(isPregnant: Boolean, gestationalWeek: Int? = null) {
        val current = _currentPatient.value
        val updated = current.copy(
            isPregnant = isPregnant,
            gestationalWeek = gestationalWeek,
            lastUpdated = System.currentTimeMillis()
        )
        _currentPatient.value = updated
        recalculateValues()
    }

    /**
     * Setzt zusätzliche klinische Daten
     */
    fun updateClinicalData(
        systolicBP: Int? = null,
        diastolicBP: Int? = null,
        heartRate: Int? = null,
        respiratoryRate: Int? = null,
        temperature: Double? = null,
        oxygenSaturation: Int? = null
    ) {
        val current = _currentPatient.value
        val clinicalData = current.clinicalData.copy(
            systolicBP = systolicBP,
            diastolicBP = diastolicBP,
            heartRate = heartRate,
            respiratoryRate = respiratoryRate,
            temperature = temperature,
            oxygenSaturation = oxygenSaturation,
            lastMeasurement = System.currentTimeMillis()
        )

        val updated = current.copy(
            clinicalData = clinicalData,
            lastUpdated = System.currentTimeMillis()
        )
        _currentPatient.value = updated
        recalculateValues()
    }

    /**
     * Fügt Allergie hinzu
     */
    fun addAllergy(allergy: String) {
        val current = _currentPatient.value
        val allergies = current.allergies.toMutableList()
        if (!allergies.contains(allergy) && allergy.isNotBlank()) {
            allergies.add(allergy)
            val updated = current.copy(
                allergies = allergies,
                lastUpdated = System.currentTimeMillis()
            )
            _currentPatient.value = updated
        }
    }

    /**
     * Entfernt Allergie
     */
    fun removeAllergy(allergy: String) {
        val current = _currentPatient.value
        val allergies = current.allergies.toMutableList()
        if (allergies.remove(allergy)) {
            val updated = current.copy(
                allergies = allergies,
                lastUpdated = System.currentTimeMillis()
            )
            _currentPatient.value = updated
        }
    }

    /**
     * Fügt Medikament zur aktuellen Medikation hinzu
     */
    fun addCurrentMedication(medication: String) {
        val current = _currentPatient.value
        val medications = current.currentMedications.toMutableList()
        if (!medications.contains(medication) && medication.isNotBlank()) {
            medications.add(medication)
            val updated = current.copy(
                currentMedications = medications,
                lastUpdated = System.currentTimeMillis()
            )
            _currentPatient.value = updated
        }
    }

    /**
     * Berechnet Gewichtsvorschlag basierend auf Alter
     */
    fun getEstimatedWeight(): Double {
        val patient = _currentPatient.value
        return estimateWeightByAge(patient.ageYears, patient.ageMonths, patient.gender)
    }

    /**
     * Gibt das effektive Gewicht zurück (manuell oder geschätzt)
     */
    fun getEffectiveWeight(): Double {
        val patient = _currentPatient.value
        return if (patient.isManualWeight) {
            patient.weightKg
        } else {
            getEstimatedWeight()
        }
    }

    /**
     * Setzt alle Patientendaten zurück
     */
    fun clearPatientData() {
        _currentPatient.value = PatientData()
        _calculatedValues.value = CalculatedPatientValues()
    }

    /**
     * Exportiert Patientendaten als JSON (für Backup)
     */
    fun exportPatientData(): String {
        return json.encodeToString(_currentPatient.value)
    }

    /**
     * Importiert Patientendaten aus JSON
     */
    fun importPatientData(jsonData: String): Result<Unit> {
        return try {
            val patientData = json.decodeFromString<PatientData>(jsonData)
            _currentPatient.value = patientData
            recalculateValues()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Berechnet abgeleitete Werte neu
     */
    private fun recalculateValues() {
        val patient = _currentPatient.value
        val effectiveWeight = getEffectiveWeight()

        val calculatedValues = CalculatedPatientValues(
            totalAgeMonths = patient.ageYears * 12 + patient.ageMonths,
            estimatedWeight = getEstimatedWeight(),
            effectiveWeight = effectiveWeight,
            bodyMassIndex = if (patient.ageYears >= 2) calculateBMI(effectiveWeight, patient.ageYears) else null,
            bodySurfaceArea = calculateBSA(effectiveWeight, getHeightEstimate(patient.ageYears, patient.ageMonths)),
            normalVitalSigns = calculateNormalVitalSigns(patient.ageYears, patient.ageMonths),
            isInfant = patient.ageYears == 0,
            isChild = patient.ageYears in 1..11,
            isAdolescent = patient.ageYears in 12..17,
            isAdult = patient.ageYears >= 18,
            isGeriatric = patient.ageYears >= 65,
            riskFactors = calculateRiskFactors(patient)
        )

        _calculatedValues.value = calculatedValues
    }

    private fun estimateWeightByAge(years: Int, months: Int, gender: PatientGender): Double {
        val totalMonths = years * 12 + months

        return when {
            // Säuglinge (0-12 Monate)
            totalMonths <= 12 -> {
                3.5 + (totalMonths * 0.6) // Geburtsgewicht + ~600g pro Monat
            }

            // Kleinkinder (1-5 Jahre)
            years in 1..5 -> {
                8.0 + (years * 2.5) // Basis 8kg + 2.5kg pro Jahr
            }

            // Kinder (6-12 Jahre)
            years in 6..12 -> {
                7.0 + (years * 3.0) // Angepasste Formel für Schulkinder
            }

            // Jugendliche (13-17 Jahre) - geschlechtsspezifisch
            years in 13..17 -> {
                when (gender) {
                    PatientGender.MALE -> 45.0 + ((years - 13) * 7.0) // Jungen
                    PatientGender.FEMALE -> 40.0 + ((years - 13) * 5.0) // Mädchen
                    PatientGender.UNKNOWN -> 42.5 + ((years - 13) * 6.0) // Durchschnitt
                }
            }

            // Erwachsene (18+ Jahre)
            else -> {
                when (gender) {
                    PatientGender.MALE -> 75.0
                    PatientGender.FEMALE -> 65.0
                    PatientGender.UNKNOWN -> 70.0
                }
            }
        }
    }

    private fun calculateBMI(weightKg: Double, ageYears: Int): Double? {
        if (ageYears < 2) return null
        val estimatedHeight = getHeightEstimate(ageYears, 0)
        return weightKg / (estimatedHeight * estimatedHeight) * 10000 // kg/m² * 10000 für cm
    }

    private fun calculateBSA(weightKg: Double, heightCm: Double): Double {
        // Mosteller Formel: BSA = √((Height × Weight) / 3600)
        return sqrt((heightCm * weightKg) / 3600.0)
    }

    private fun getHeightEstimate(years: Int, months: Int): Double {
        val totalMonths = years * 12 + months

        return when {
            totalMonths <= 12 -> 50.0 + (totalMonths * 2.5) // Säuglinge
            years in 1..5 -> 75.0 + (years * 6.0) // Kleinkinder
            years in 6..12 -> 110.0 + (years * 6.0) // Kinder
            years in 13..17 -> 150.0 + ((years - 13) * 4.0) // Jugendliche
            else -> 170.0 // Erwachsene (Durchschnitt)
        }
    }

    private fun calculateNormalVitalSigns(years: Int, months: Int): NormalVitalSigns {
        val totalMonths = years * 12 + months

        return when {
            totalMonths <= 3 -> NormalVitalSigns(
                heartRate = IntRange(100, 160),
                respiratoryRate = IntRange(30, 60),
                systolicBP = IntRange(60, 90),
                diastolicBP = IntRange(30, 60),
                temperature = DoubleRange(36.1, 37.5),
                oxygenSaturation = IntRange(95, 100)
            )
            totalMonths <= 12 -> NormalVitalSigns(
                heartRate = IntRange(80, 140),
                respiratoryRate = IntRange(25, 50),
                systolicBP = IntRange(70, 100),
                diastolicBP = IntRange(40, 70),
                temperature = DoubleRange(36.1, 37.5),
                oxygenSaturation = IntRange(95, 100)
            )
            years in 1..5 -> NormalVitalSigns(
                heartRate = IntRange(70, 120),
                respiratoryRate = IntRange(20, 35),
                systolicBP = IntRange(80, 110),
                diastolicBP = IntRange(50, 75),
                temperature = DoubleRange(36.1, 37.5),
                oxygenSaturation = IntRange(95, 100)
            )
            years in 6..12 -> NormalVitalSigns(
                heartRate = IntRange(60, 100),
                respiratoryRate = IntRange(15, 25),
                systolicBP = IntRange(90, 120),
                diastolicBP = IntRange(55, 80),
                temperature = DoubleRange(36.1, 37.5),
                oxygenSaturation = IntRange(95, 100)
            )
            years in 13..17 -> NormalVitalSigns(
                heartRate = IntRange(55, 95),
                respiratoryRate = IntRange(12, 20),
                systolicBP = IntRange(100, 130),
                diastolicBP = IntRange(60, 85),
                temperature = DoubleRange(36.1, 37.5),
                oxygenSaturation = IntRange(95, 100)
            )
            else -> NormalVitalSigns( // Erwachsene
                heartRate = IntRange(60, 100),
                respiratoryRate = IntRange(12, 20),
                systolicBP = IntRange(110, 140),
                diastolicBP = IntRange(70, 90),
                temperature = DoubleRange(36.1, 37.5),
                oxygenSaturation = IntRange(95, 100)
            )
        }
    }

    private fun calculateRiskFactors(patient: PatientData): List<RiskFactor> {
        val riskFactors = mutableListOf<RiskFactor>()

        // Altersbasierte Risikofaktoren
        when {
            patient.ageYears == 0 -> riskFactors.add(RiskFactor.INFANT)
            patient.ageYears >= 65 -> riskFactors.add(RiskFactor.GERIATRIC)
        }

        // Schwangerschaft
        if (patient.isPregnant) {
            riskFactors.add(RiskFactor.PREGNANCY)
        }

        // Allergien
        if (patient.allergies.isNotEmpty()) {
            riskFactors.add(RiskFactor.KNOWN_ALLERGIES)
        }

        // Medikamenteninteraktionen
        if (patient.currentMedications.size > 5) {
            riskFactors.add(RiskFactor.POLYPHARMACY)
        }

        return riskFactors
    }
}

@Serializable
data class PatientData(
    val sessionId: String = UUID.randomUUID().toString(),
    val ageYears: Int = 35, // Default Erwachsener
    val ageMonths: Int = 0,
    val weightKg: Double = 70.0,
    val isManualWeight: Boolean = false,
    val gender: PatientGender = PatientGender.UNKNOWN,
    val isPregnant: Boolean = false,
    val gestationalWeek: Int? = null,
    val allergies: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
    val clinicalData: ClinicalData = ClinicalData(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class ClinicalData(
    val systolicBP: Int? = null,
    val diastolicBP: Int? = null,
    val heartRate: Int? = null,
    val respiratoryRate: Int? = null,
    val temperature: Double? = null, // Celsius
    val oxygenSaturation: Int? = null, // %
    val lastMeasurement: Long = System.currentTimeMillis()
)

@Serializable
data class CalculatedPatientValues(
    val totalAgeMonths: Int = 0,
    val estimatedWeight: Double = 70.0,
    val effectiveWeight: Double = 70.0,
    val bodyMassIndex: Double? = null,
    val bodySurfaceArea: Double = 1.8, // m²
    val normalVitalSigns: NormalVitalSigns = NormalVitalSigns(),
    val isInfant: Boolean = false,
    val isChild: Boolean = false,
    val isAdolescent: Boolean = false,
    val isAdult: Boolean = true,
    val isGeriatric: Boolean = false,
    val riskFactors: List<RiskFactor> = emptyList()
)

@Serializable
data class NormalVitalSigns(
    val heartRate: IntRange = IntRange(60, 100),
    val respiratoryRate: IntRange = IntRange(12, 20),
    val systolicBP: IntRange = IntRange(110, 140),
    val diastolicBP: IntRange = IntRange(70, 90),
    val temperature: DoubleRange = DoubleRange(36.1, 37.5),
    val oxygenSaturation: IntRange = IntRange(95, 100)
)

@Serializable
data class DoubleRange(
    val min: Double,
    val max: Double
)

@Serializable
enum class PatientGender {
    MALE,
    FEMALE,
    UNKNOWN
}

@Serializable
enum class RiskFactor {
    INFANT,              // Säugling
    GERIATRIC,           // Geriatrisch
    PREGNANCY,           // Schwangerschaft
    KNOWN_ALLERGIES,     // Bekannte Allergien
    POLYPHARMACY,        // Viele Medikamente
    RENAL_IMPAIRMENT,    // Niereninsuffizienz
    HEPATIC_IMPAIRMENT,  // Leberinsuffizienz
    CARDIAC_DISEASE,     // Herzerkrankung
    RESPIRATORY_DISEASE, // Lungenerkrankung
    DIABETES,            // Diabetes
    HYPERTENSION         // Bluthochdruck
}