// app/src/main/java/com/rdinfo2/data/json/JsonMedicationLoader.kt
package com.rdinfo2.data.json

import android.content.Context
import android.util.Log
import com.rdinfo2.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Lädt Medikamenten-Daten aus JSON-Datei
 * Ersetzt die hardcoded Factory-Methoden
 */
class JsonMedicationLoader(private val context: Context) {

    companion object {
        private const val TAG = "JsonMedicationLoader"
    }

    /**
     * Lädt alle Medikamente aus medications.json
     */
    fun loadMedications(): List<Medication> {
        return try {
            Log.d(TAG, "Starte Laden...")
            val jsonString = loadJsonFromAssets("medications.json")
            val rootJson = JSONObject(jsonString)
            val medicationsArray = rootJson.getJSONArray("medications")

            val medications = mutableListOf<Medication>()
            for (i in 0 until medicationsArray.length()) {
                medications.add(parseMedication(medicationsArray.getJSONObject(i)))
            }

            Log.d(TAG, "✓ ${medications.size} Medikamente geladen")
            medications
        } catch (e: Exception) {
            Log.e(TAG, "Fehler: ${e.message}", e)
            // Fallback auf Factory-Medikamente bei Fehler
            EmergencyMedications.getStandardSet()
        }
    }

    /**
     * JSON-Datei aus Assets laden
     */
    private fun loadJsonFromAssets(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IOException("Konnte $fileName nicht laden: ${e.message}")
        }
    }

    /**
     * Einzelnes Medikament aus JSON parsen
     */
    private fun parseMedication(json: JSONObject): Medication {
        return Medication(
            id = json.getString("id"),
            name = json.getString("name"),
            genericName = if (json.has("genericName") && !json.isNull("genericName")) json.getString("genericName") else null,
            category = MedicationCategory.valueOf(json.getString("category")),
            indications = parseIndications(json.getJSONArray("indications")),
            contraindications = parseStringArray(json.optJSONArray("contraindications")),
            warnings = parseStringArray(json.optJSONArray("warnings")),
            notes = if (json.has("notes") && !json.isNull("notes")) json.getString("notes") else null
        )
    }

    /**
     * Indikationen aus JSON parsen
     */
    private fun parseIndications(jsonArray: JSONArray): List<Indication> {
        val indications = mutableListOf<Indication>()
        for (i in 0 until jsonArray.length()) {
            val indicationJson = jsonArray.getJSONObject(i)
            indications.add(
                Indication(
                    name = indicationJson.getString("name"),
                    dosageRules = parseDosageRules(indicationJson.getJSONArray("dosageRules")),
                    route = indicationJson.getString("route"),
                    preparation = if (indicationJson.has("preparation") && !indicationJson.isNull("preparation")) indicationJson.getString("preparation") else null,
                    maxDose = if (indicationJson.has("maxDose") && !indicationJson.isNull("maxDose")) parseMaxDose(indicationJson.getJSONObject("maxDose")) else null
                )
            )
        }
        return indications
    }

    /**
     * Dosierungsregeln aus JSON parsen
     */
    private fun parseDosageRules(jsonArray: JSONArray): List<DosageRule> {
        val rules = mutableListOf<DosageRule>()
        for (i in 0 until jsonArray.length()) {
            val ruleJson = jsonArray.getJSONObject(i)
            rules.add(
                DosageRule(
                    ageGroup = AgeGroup.valueOf(ruleJson.getString("ageGroup")),
                    calculation = parseDosageCalculation(ruleJson.getJSONObject("calculation")),
                    unit = ruleJson.getString("unit"),
                    volume = if (ruleJson.has("volume") && !ruleJson.isNull("volume")) ruleJson.getString("volume") else null,
                    note = if (ruleJson.has("note") && !ruleJson.isNull("note")) ruleJson.getString("note") else null
                )
            )
        }
        return rules
    }

    /**
     * Dosierungsberechnung aus JSON parsen
     */
    private fun parseDosageCalculation(json: JSONObject): DosageCalculation {
        return DosageCalculation(
            type = CalculationType.valueOf(json.getString("type")),
            value = json.getDouble("value"),
            minDose = if (json.has("minDose") && !json.isNull("minDose")) json.getDouble("minDose") else null,
            maxDose = if (json.has("maxDose") && !json.isNull("maxDose")) json.getDouble("maxDose") else null
        )
    }

    /**
     * Maximaldosis aus JSON parsen
     */
    private fun parseMaxDose(json: JSONObject): MaxDose {
        return MaxDose(
            amount = json.getDouble("amount"),
            unit = json.getString("unit"),
            timeframe = json.getString("timeframe"),
            warning = if (json.has("warning") && !json.isNull("warning")) json.getString("warning") else null
        )
    }

    /**
     * String-Array aus JSON parsen
     */
    private fun parseStringArray(jsonArray: JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()

        val strings = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            strings.add(jsonArray.getString(i))
        }
        return strings
    }
}