// app/src/main/java/com/rdinfo2/data/json/JsonMedicationLoader.kt
package com.rdinfo2.data.json

import android.content.Context
import android.util.Log
import com.rdinfo2.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Lädt Medikamenten-Daten aus JSON v3.0
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
            Log.d(TAG, "Starte Laden der Medikamente...")
            val jsonString = loadJsonFromAssets("medications.json")
            val rootJson = JSONObject(jsonString)
            val medicationsArray = rootJson.getJSONArray("medications")

            val medications = mutableListOf<Medication>()
            for (i in 0 until medicationsArray.length()) {
                try {
                    medications.add(parseMedication(medicationsArray.getJSONObject(i)))
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler beim Parsen von Medikament $i: ${e.message}", e)
                }
            }

            Log.d(TAG, "Erfolgreich ${medications.size} Medikamente geladen")
            medications.forEach { med ->
                Log.d(TAG, "  - ${med.name} (${med.id})")
            }

            medications
        } catch (e: Exception) {
            Log.e(TAG, "Schwerer Fehler beim Laden: ${e.message}", e)
            emptyList()
        }
    }

    private fun loadJsonFromAssets(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IOException("Konnte $fileName nicht laden: ${e.message}")
        }
    }

    private fun parseMedication(json: JSONObject): Medication {
        return Medication(
            id = json.getString("id"),
            name = json.getString("name"),
            genericName = json.optString("genericName").takeIf { it.isNotEmpty() },
            category = parseMedicationCategory(json.getString("category")),
            globalInfo = parseGlobalInfo(json.getJSONObject("globalInfo")),
            preparations = parsePreparations(json.getJSONArray("preparations")),
            useCases = parseUseCases(json.getJSONArray("useCases"))
        )
    }

    private fun parseGlobalInfo(json: JSONObject): GlobalInfo {
        return GlobalInfo(
            indications = json.getString("indications"),
            mechanism = json.getString("mechanism"),
            sideEffects = json.getString("sideEffects"),
            contraindications = json.getString("contraindications")
        )
    }

    private fun parsePreparations(jsonArray: JSONArray): List<Preparation> {
        val preparations = mutableListOf<Preparation>()
        for (i in 0 until jsonArray.length()) {
            val prepJson = jsonArray.getJSONObject(i)
            preparations.add(
                Preparation(
                    id = prepJson.getString("id"),
                    type = parsePreparationType(prepJson.getString("type")),
                    concentration = prepJson.getDouble("concentration"),
                    concentrationUnit = prepJson.getString("concentrationUnit"),
                    volume = prepJson.getDouble("volume"),
                    volumeUnit = prepJson.getString("volumeUnit"),
                    description = prepJson.getString("description")
                )
            )
        }
        return preparations
    }

    private fun parseUseCases(jsonArray: JSONArray): List<UseCase> {
        val useCases = mutableListOf<UseCase>()
        for (i in 0 until jsonArray.length()) {
            val useCaseJson = jsonArray.getJSONObject(i)
            useCases.add(
                UseCase(
                    id = useCaseJson.getString("id"),
                    name = useCaseJson.getString("name"),
                    route = useCaseJson.getString("route"),
                    preparation = parseUseCasePreparation(useCaseJson.getJSONObject("preparation")),
                    dosingRules = parseDosingRules(useCaseJson.getJSONArray("dosingRules"))
                )
            )
        }
        return useCases
    }

    private fun parseUseCasePreparation(json: JSONObject): UseCasePreparation {
        return UseCasePreparation(
            preparationId = json.getString("preparationId"),
            dilution = json.optJSONObject("dilution")?.let { parseDilution(it) },
            nebulization = json.optJSONObject("nebulization")?.let { parseNebulization(it) }
        )
    }

    private fun parseDilution(json: JSONObject): Dilution {
        return Dilution(
            ratio = json.optString("ratio").takeIf { it.isNotEmpty() },
            solvent = json.optString("solvent").takeIf { it.isNotEmpty() },
            finalConcentration = if (json.has("finalConcentration")) json.getDouble("finalConcentration") else null,
            finalConcentrationUnit = json.optString("finalConcentrationUnit").takeIf { it.isNotEmpty() },
            finalVolume = if (json.has("finalVolume")) json.getDouble("finalVolume") else null,
            finalVolumeUnit = json.optString("finalVolumeUnit").takeIf { it.isNotEmpty() },
            note = json.optString("note").takeIf { it.isNotEmpty() }
        )
    }

    private fun parseNebulization(json: JSONObject): Nebulization {
        return Nebulization(
            dose = if (json.has("dose")) json.getDouble("dose") else null,
            doseUnit = json.optString("doseUnit").takeIf { it.isNotEmpty() },
            oxygenFlow = if (json.has("oxygenFlow")) json.getInt("oxygenFlow") else null,
            oxygenFlowUnit = json.optString("oxygenFlowUnit").takeIf { it.isNotEmpty() }
        )
    }

    private fun parseDosingRules(jsonArray: JSONArray): List<DosingRule> {
        val rules = mutableListOf<DosingRule>()
        for (i in 0 until jsonArray.length()) {
            val ruleJson = jsonArray.getJSONObject(i)
            rules.add(
                DosingRule(
                    ruleId = ruleJson.getString("ruleId"),
                    minAge = if (ruleJson.has("minAge") && !ruleJson.isNull("minAge")) ruleJson.getInt("minAge") else null,
                    maxAge = if (ruleJson.has("maxAge") && !ruleJson.isNull("maxAge")) ruleJson.getInt("maxAge") else null,
                    minWeight = if (ruleJson.has("minWeight") && !ruleJson.isNull("minWeight")) ruleJson.getDouble("minWeight") else null,
                    maxWeight = if (ruleJson.has("maxWeight") && !ruleJson.isNull("maxWeight")) ruleJson.getDouble("maxWeight") else null,
                    calculation = parseDosageCalculation(ruleJson.getJSONObject("calculation")),
                    maxSingleDose = if (ruleJson.has("maxSingleDose") && !ruleJson.isNull("maxSingleDose")) ruleJson.getDouble("maxSingleDose") else null,
                    maxTotalDose = ruleJson.optJSONObject("maxTotalDose")?.let { parseMaxDose(it) },
                    repetitionInterval = ruleJson.optString("repetitionInterval").takeIf { it.isNotEmpty() },
                    abortCriteria = ruleJson.optString("abortCriteria").takeIf { it.isNotEmpty() },
                    note = ruleJson.optString("note").takeIf { it.isNotEmpty() }
                )
            )
        }
        return rules
    }

    private fun parseDosageCalculation(json: JSONObject): DosageCalculation {
        return DosageCalculation(
            type = parseCalculationType(json.getString("type")),
            value = json.getDouble("value"),
            unit = json.getString("unit")
        )
    }

    private fun parseMaxDose(json: JSONObject): MaxDose {
        return MaxDose(
            amount = json.getDouble("amount"),
            unit = json.getString("unit"),
            timeframe = json.getString("timeframe")
        )
    }

    private fun parseMedicationCategory(category: String): MedicationCategory {
        return try {
            MedicationCategory.valueOf(category)
        } catch (e: Exception) {
            Log.w(TAG, "Unbekannte Kategorie: $category, verwende OTHER")
            MedicationCategory.OTHER
        }
    }

    private fun parsePreparationType(type: String): PreparationType {
        return try {
            PreparationType.valueOf(type)
        } catch (e: Exception) {
            Log.w(TAG, "Unbekannter Typ: $type, verwende OTHER")
            PreparationType.OTHER
        }
    }

    private fun parseCalculationType(type: String): CalculationType {
        return try {
            CalculationType.valueOf(type)
        } catch (e: Exception) {
            Log.w(TAG, "Unbekannter Berechnungstyp: $type, verwende FIXED")
            CalculationType.FIXED
        }
    }
}