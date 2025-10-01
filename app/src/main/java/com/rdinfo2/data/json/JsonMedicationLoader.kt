// app/src/main/java/com/rdinfo2/data/json/JsonMedicationLoader.kt
package com.rdinfo2.data.json

import android.content.Context
import android.util.Log
import com.rdinfo2.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * VERBESSERTER Loader - Lädt Medikamenten-Daten aus JSON-Datei
 * Kompatibel mit der aktuellen medications.json Struktur
 */
class JsonMedicationLoader(private val context: Context) {

    private val TAG = "JsonMedicationLoader"

    /**
     * Lädt alle Medikamente aus medications.json
     */
    fun loadMedications(): List<Medication> {
        return try {
            Log.d(TAG, "Starte Laden der Medikamente...")
            val jsonString = loadJsonFromAssets("medications.json")
            val rootJson = JSONObject(jsonString)
            val medicationsArray = rootJson.getJSONArray("medications")

            Log.d(TAG, "JSON enthält ${medicationsArray.length()} Medikamente")

            val medications = mutableListOf<Medication>()
            for (i in 0 until medicationsArray.length()) {
                try {
                    val medication = parseMedication(medicationsArray.getJSONObject(i))
                    medications.add(medication)
                    Log.d(TAG, "✓ ${medication.name} erfolgreich geladen")
                } catch (e: Exception) {
                    Log.e(TAG, "✗ Fehler beim Parsen von Medikament $i: ${e.message}")
                }
            }

            Log.d(TAG, "Insgesamt ${medications.size} Medikamente erfolgreich geladen")
            medications
        } catch (e: Exception) {
            Log.e(TAG, "Schwerer Fehler beim Laden: ${e.message}", e)
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
        val id = json.getString("id")
        val name = json.getString("name")

        Log.d(TAG, "Parse Medikament: $name ($id)")

        return Medication(
            id = id,
            name = name,
            genericName = json.optString("genericName").takeIf { it.isNotEmpty() },
            category = parseCategory(json.optString("category", "OTHER")),
            indications = parseIndications(json.getJSONArray("indications")),
            contraindications = parseStringArray(json.optJSONArray("contraindications")),
            warnings = parseStringArray(json.optJSONArray("warnings")),
            notes = json.optString("notes").takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Kategorie parsen mit Fehlerbehandlung
     */
    private fun parseCategory(categoryString: String): MedicationCategory {
        return try {
            MedicationCategory.valueOf(categoryString)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unbekannte Kategorie: $categoryString, verwende OTHER")
            MedicationCategory.OTHER
        }
    }

    /**
     * Indikationen aus JSON parsen
     */
    private fun parseIndications(jsonArray: JSONArray): List<Indication> {
        val indications = mutableListOf<Indication>()
        for (i in 0 until jsonArray.length()) {
            try {
                val indicationJson = jsonArray.getJSONObject(i)
                indications.add(
                    Indication(
                        name = indicationJson.getString("name"),
                        dosageRules = parseDosageRules(indicationJson.getJSONArray("dosageRules")),
                        route = indicationJson.getString("route"),
                        preparation = indicationJson.optString("preparation").takeIf { it.isNotEmpty() },
                        maxDose = if (indicationJson.has("maxDose") && !indicationJson.isNull("maxDose")) {
                            parseMaxDose(indicationJson.getJSONObject("maxDose"))
                        } else null
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Parsen von Indikation $i: ${e.message}")
            }
        }
        return indications
    }

    /**
     * Dosierungsregeln aus JSON parsen
     */
    private fun parseDosageRules(jsonArray: JSONArray): List<DosageRule> {
        val rules = mutableListOf<DosageRule>()
        for (i in 0 until jsonArray.length()) {
            try {
                val ruleJson = jsonArray.getJSONObject(i)
                val ageGroupString = ruleJson.getString("ageGroup")
                val ageGroup = parseAgeGroup(ageGroupString)

                if (ageGroup != null) {
                    rules.add(
                        DosageRule(
                            ageGroup = ageGroup,
                            calculation = parseDosageCalculation(ruleJson.getJSONObject("calculation")),
                            unit = ruleJson.getString("unit"),
                            volume = ruleJson.optString("volume").takeIf { it.isNotEmpty() },
                            note = ruleJson.optString("note").takeIf { it.isNotEmpty() }
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Parsen von DosageRule $i: ${e.message}")
            }
        }
        return rules
    }

    /**
     * AgeGroup parsen mit Fehlerbehandlung
     */
    private fun parseAgeGroup(ageGroupString: String): AgeGroup? {
        return try {
            AgeGroup.valueOf(ageGroupString)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unbekannte AgeGroup: $ageGroupString")
            // Versuche Mapping von häufigen Varianten
            when (ageGroupString.uppercase()) {
                "ALL_AGES", "ALL" -> AgeGroup.ALL_AGES
                "CHILD", "CHILDREN" -> AgeGroup.CHILD
                "ADULT", "ADULTS" -> AgeGroup.ADULT
                "ADOLESCENT", "ADOLESCENTS" -> AgeGroup.ADOLESCENT
                "INFANT", "INFANTS" -> AgeGroup.INFANT
                "NEONATE", "NEONATES" -> AgeGroup.NEONATE
                "TODDLER", "TODDLERS" -> AgeGroup.TODDLER
                "GERIATRIC", "ELDERLY" -> AgeGroup.GERIATRIC
                else -> null
            }
        }
    }

    /**
     * Dosierungsberechnung aus JSON parsen
     */
    private fun parseDosageCalculation(json: JSONObject): DosageCalculation {
        val typeString = json.getString("type")
        val type = parseCalculationType(typeString)

        return DosageCalculation(
            type = type,
            value = json.getDouble("value"),
            minDose = if (json.has("minDose") && !json.isNull("minDose")) {
                json.getDouble("minDose")
            } else null,
            maxDose = if (json.has("maxDose") && !json.isNull("maxDose")) {
                json.getDouble("maxDose")
            } else null
        )
    }

    /**
     * CalculationType parsen mit Fehlerbehandlung
     */
    private fun parseCalculationType(typeString: String): CalculationType {
        return try {
            CalculationType.valueOf(typeString)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unbekannter CalculationType: $typeString, verwende FIXED")
            CalculationType.FIXED
        }
    }

    /**
     * Maximaldosis aus JSON parsen
     */
    private fun parseMaxDose(json: JSONObject): MaxDose {
        return MaxDose(
            amount = json.getDouble("amount"),
            unit = json.getString("unit"),
            timeframe = json.getString("timeframe"),
            warning = json.optString("warning").takeIf { it.isNotEmpty() }
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