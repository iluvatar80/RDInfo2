// app/src/main/java/com/rdinfo2/data/patient/PatientDataManager.kt
package com.rdinfo2.data.patient

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Zentrale Verwaltung der aktuellen Patientendaten
 * Singleton für app-weiten Zugriff auf Patienteninformationen
 * Daten werden NICHT persistent gespeichert (nur RAM)
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

    // Aktueller Patient (StateFlow für Compose Integration)
    private val _currentPatient = MutableStateFlow(PatientDataFactory.createUnknown())
    val currentPatient: StateFlow<PatientData> = _currentPatient.asStateFlow()

    // UI State für Eingabe-Screens
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    // Letzte Patientendaten für "Zurück" Funktionalität
    private var _previousPatient: PatientData? = null

    /**
     * Aktualisiert die Patientendaten
     */
    fun updatePatient(newPatient: PatientData) {
        _previousPatient = _currentPatient.value
        _currentPatient.value = newPatient
    }

    /**
     * Aktualisiert nur das Alter (behält andere Daten bei)
     */
    fun updateAge(years: Int, months: Int) {
        val current = _currentPatient.value
        val updatedPatient = current.copy(
            ageYears = years,
            ageMonths = months,
            totalAgeMonths = years * 12 + months,
            ageCategory = AgeCategory.fromAge(years, months),
            estimatedWeightKg = PatientDataFactory.estimateWeight(years, months),
            lastUpdated = System.currentTimeMillis()
        )
        updatePatient(updatedPatient)
    }

    /**
     * Aktualisiert nur das Gewicht
     */
    fun updateWeight(weightKg: Double, isManual: Boolean = true) {
        val current = _currentPatient.value
        val updatedPatient = current.copy(
            weightKg = if (isManual) weightKg else current.weightKg,
            isManualWeight = isManual,
            estimatedWeightKg = if (!isManual) weightKg else current.estimatedWeightKg,
            lastUpdated = System.currentTimeMillis()
        )
        updatePatient(updatedPatient)
    }

    /**
     * Aktualisiert nur das Geschlecht
     */
    fun updateGender(gender: PatientGender) {
        val current = _currentPatient.value
        updatePatient(current.copy(
            gender = gender,
            lastUpdated = System.currentTimeMillis()
        ))
    }

    /**
     * Setzt Schwangerschaftsstatus
     */
    fun updatePregnancy(isPregnant: Boolean, gestationalWeek: Int? = null) {
        val current = _currentPatient.value
        updatePatient(current.copy(
            isPregnant = isPregnant,
            gestationalWeek = if (isPregnant) gestationalWeek else null,
            lastUpdated = System.currentTimeMillis()
        ))
    }

    /**
     * Fügt Allergie hinzu
     */
    fun addAllergy(allergy: String) {
        if (allergy.isBlank()) return

        val current = _currentPatient.value
        val updatedAllergies = current.allergies.toMutableList()
        if (!updatedAllergies.contains(allergy)) {
            updatedAllergies.add(allergy)
            updatePatient(current.copy(
                allergies = updatedAllergies,
                lastUpdated = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Entfernt Allergie
     */
    fun removeAllergy(allergy: String) {
        val current = _currentPatient.value
        val updatedAllergies = current.allergies.toMutableList()
        if (updatedAllergies.remove(allergy)) {
            updatePatient(current.copy(
                allergies = updatedAllergies,
                lastUpdated = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Setzt alle Patientendaten zurück
     */
    fun resetPatient() {
        _previousPatient = _currentPatient.value
        _currentPatient.value = PatientDataFactory.createUnknown()
        _isEditing.value = false
    }

    /**
     * Stellt vorherige Patientendaten wieder her
     */
    fun restorePrevious() {
        _previousPatient?.let { previous ->
            _currentPatient.value = previous
            _previousPatient = null
        }
    }

    /**
     * Startet Editing-Modus
     */
    fun startEditing() {
        _isEditing.value = true
    }

    /**
     * Beendet Editing-Modus
     */
    fun stopEditing() {
        _isEditing.value = false
    }

    /**
     * Hilfsfunktion für schnelle Patientenerstellung in Tests/Debug
     */
    fun setQuickPatient(description: String) {
        val patient = when (description.lowercase()) {
            "neugeborenes" -> PatientDataFactory.create(0, 1)
            "säugling" -> PatientDataFactory.create(0, 6)
            "kleinkind" -> PatientDataFactory.create(2)
            "schulkind" -> PatientDataFactory.create(8)
            "jugendlicher" -> PatientDataFactory.create(15)
            "erwachsener" -> PatientDataFactory.create(35)
            "senior" -> PatientDataFactory.create(70)
            else -> PatientDataFactory.createUnknown()
        }
        updatePatient(patient)
    }

    /**
     * Gibt aktuelles effektives Gewicht zurück (für Dosierungsberechnungen)
     */
    fun getCurrentWeight(): Double {
        return _currentPatient.value.getEffectiveWeight()
    }

    /**
     * Gibt aktuelles Alter in Jahren zurück
     */
    fun getCurrentAgeYears(): Int {
        return _currentPatient.value.ageYears
    }

    /**
     * Gibt aktuelles Gesamtalter in Monaten zurück
     */
    fun getCurrentAgeMonths(): Int {
        return _currentPatient.value.totalAgeMonths
    }

    /**
     * Prüft ob Patient-Daten vollständig sind
     */
    fun isCurrentPatientValid(): Boolean {
        return _currentPatient.value.isValid()
    }

    /**
     * Export für Debug/Logging (keine medizinischen Daten)
     */
    fun getPatientSummary(): String {
        val patient = _currentPatient.value
        return "Patient: ${patient.getFormattedAge()}, ${patient.getFormattedWeight()}, ${patient.ageCategory}"
    }
}

/**
 * Extension für PatientDataFactory mit internen Zugriff
 */
private fun PatientDataFactory.estimateWeight(years: Int, months: Int): Double {
    val totalMonths = years * 12 + months

    return when {
        totalMonths == 0 -> 3.5
        totalMonths <= 12 -> 3.5 + (totalMonths * 0.5)
        years in 1..5 -> 2.0 * years + 8.0
        years in 6..12 -> 3.0 * years + 7.0
        years in 13..17 -> 50.0 + (years - 13) * 5.0
        else -> 70.0
    }
}