// app/src/main/java/com/rdinfo2/data/json/JsonMedicationLoader.kt
package com.rdinfo2.data.json

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import com.rdinfo2.data.model.*
import java.io.IOException

/**
 * Lädt Medikamenten-Daten aus JSON mittels Kotlinx Serialization
 * Verwendet MedicationModels.kt Schema
 */
class JsonMedicationLoader(private val context: Context) {

    companion object {
        private const val TAG = "JsonMedicationLoader"

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        }
    }

    fun loadMedications(): List<Medication> {
        return try {
            val jsonString = loadJsonFromAssets("medications.json")
            Log.d(TAG, "JSON geladen, Länge: ${jsonString.length}")

            // Parse als JSON Object und hole medications Array
            val jsonObject = org.json.JSONObject(jsonString)
            val medicationsJson = jsonObject.getJSONArray("medications").toString()

            val medications = json.decodeFromString<List<Medication>>(medicationsJson)
            Log.d(TAG, "✓ Erfolgreich ${medications.size} Medikamente geladen")

            medications
        } catch (e: Exception) {
            Log.e(TAG, "FEHLER beim Laden: ${e.message}", e)
            Log.e(TAG, "Stack trace:", e)
            emptyList()
        }
    }

    private fun loadJsonFromAssets(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e(TAG, "Konnte $fileName nicht laden: ${e.message}")
            throw IOException("Konnte $fileName nicht laden: ${e.message}")
        }
    }
}