// app/src/main/java/com/rdinfo2/data/repository/MedicationRepository.kt
package com.rdinfo2.data.repository

import android.content.Context
import com.rdinfo2.data.model.*
import com.rdinfo2.data.json.JsonMedicationLoader

/**
 * Repository für Medikamenten-Daten v3.0
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

        fun getInstance(): MedicationRepository {
            return INSTANCE ?: throw IllegalStateException(
                "MedicationRepository muss erst mit Context initialisiert werden"
            )
        }
    }

    private val jsonLoader = JsonMedicationLoader(context)

    private var _medications: List<Medication>? = null
    private val medications: List<Medication>
        get() {
            if (_medications == null) {
                _medications = jsonLoader.loadMedications()
            }
            return _medications!!
        }

    fun getAllMedications(): List<Medication> {
        return medications
    }

    fun getMedicationById(id: String): Medication? {
        return medications.find { it.id == id }
    }

    fun getMedicationByName(name: String): Medication? {
        return medications.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getMedicationNames(): List<String> {
        return medications.map { it.name }.sorted()
    }

    fun getUseCasesFor(medicationId: String): List<UseCase> {
        return getMedicationById(medicationId)?.useCases ?: emptyList()
    }
}