// app/src/main/java/com/rdinfo2/data/model/Medication.kt
package com.rdinfo2.data.model

/**
 * Medikament-Datenmodell für RDInfo2
 * Basis für flexible, erweiterbare Medikamenten-Datenbank
 * Orientiert sich an Hamburger Rettungsdienst-Handbuch
 */
data class Medication(
    val id: String,                              // z.B. "adrenalin"
    val name: String,                            // z.B. "Adrenalin"
    val genericName: String? = null,             // z.B. "Epinephrin"
    val category: MedicationCategory,
    val indications: List<Indication>,           // Verschiedene Einsatzgebiete
    val contraindications: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val notes: String? = null
)

data class Indication(
    val name: String,                            // z.B. "Reanimation"
    val dosageRules: List<DosageRule>,          // Altersabhängige Dosierungen
    val route: String,                          // z.B. "i.v."
    val preparation: String? = null,            // z.B. "unverdünnt"
    val maxDose: MaxDose? = null
)

data class DosageRule(
    val ageGroup: AgeGroup,
    val calculation: DosageCalculation,
    val unit: String,                           // mg, ml, Hub, etc.
    val volume: String? = null,                 // z.B. "1 ml (1:1000)"
    val note: String? = null
)

data class DosageCalculation(
    val type: CalculationType,
    val value: Double,                          // Basis-Wert für Berechnung
    val minDose: Double? = null,
    val maxDose: Double? = null
)

data class MaxDose(
    val amount: Double,
    val unit: String,
    val timeframe: String,                      // z.B. "pro Einsatz"
    val warning: String? = null
)

enum class MedicationCategory {
    CARDIOVASCULAR,     // Herz-Kreislauf
    RESPIRATORY,        // Atemwege
    NEUROLOGICAL,       // Neurologie
    METABOLIC,          // Stoffwechsel
    ANALGESIC,          // Schmerzmittel
    SEDATIVE,           // Sedierung
    ANTIDOTE,           // Gegenmittel
    OTHER
}

enum class AgeGroup {
    NEONATE,           // 0-28 Tage
    INFANT,            // 1-12 Monate
    TODDLER,           // 1-2 Jahre
    CHILD,             // 3-11 Jahre
    ADOLESCENT,        // 12-17 Jahre
    ADULT,             // 18-64 Jahre
    GERIATRIC,         // 65+ Jahre
    ALL_AGES           // Für universelle Dosierungen
}

enum class CalculationType {
    FIXED,             // Feste Dosis
    PER_KG,            // Pro kg Körpergewicht
    PER_YEAR,          // Pro Lebensjahr
    FORMULA            // Spezielle Formel
}

/**
 * Hilfsfunktionen für die Medikamenten-Erstellung
 */
object MedicationFactory {

    /**
     * Erstellt Adrenalin basierend auf Hamburger Rettungsdienst-Handbuch
     */
    fun createAdrenaline(): Medication {
        return Medication(
            id = "adrenalin",
            name = "Adrenalin",
            genericName = "Epinephrin",
            category = MedicationCategory.CARDIOVASCULAR,
            indications = listOf(
                // Reanimation
                Indication(
                    name = "Reanimation",
                    route = "i.v./i.o.",
                    preparation = "unverdünnt (1:1000)",
                    dosageRules = listOf(
                        DosageRule(
                            ageGroup = AgeGroup.NEONATE,
                            calculation = DosageCalculation(
                                type = CalculationType.PER_KG,
                                value = 0.01,
                                minDose = 0.01
                            ),
                            unit = "mg",
                            volume = "entsprechend ml (1:1000)"
                        ),
                        DosageRule(
                            ageGroup = AgeGroup.INFANT,
                            calculation = DosageCalculation(
                                type = CalculationType.PER_KG,
                                value = 0.01,
                                minDose = 0.01,
                                maxDose = 1.0
                            ),
                            unit = "mg",
                            volume = "entsprechend ml (1:1000)"
                        ),
                        DosageRule(
                            ageGroup = AgeGroup.CHILD,
                            calculation = DosageCalculation(
                                type = CalculationType.PER_KG,
                                value = 0.01,
                                maxDose = 1.0
                            ),
                            unit = "mg",
                            volume = "entsprechend ml (1:1000)"
                        ),
                        DosageRule(
                            ageGroup = AgeGroup.ADULT,
                            calculation = DosageCalculation(
                                type = CalculationType.FIXED,
                                value = 1.0
                            ),
                            unit = "mg",
                            volume = "1 ml (1:1000)"
                        )
                    ),
                    maxDose = MaxDose(5.0, "mg", "pro Einsatz", "bei Überschreitung Rücksprache")
                ),

                // Anaphylaxie
                Indication(
                    name = "Anaphylaxie",
                    route = "i.m.",
                    preparation = "unverdünnt",
                    dosageRules = listOf(
                        DosageRule(
                            ageGroup = AgeGroup.CHILD,
                            calculation = DosageCalculation(
                                type = CalculationType.FIXED,
                                value = 0.15
                            ),
                            unit = "mg",
                            volume = "0.15 ml",
                            note = "Oberschenkel lateral"
                        ),
                        DosageRule(
                            ageGroup = AgeGroup.ADOLESCENT,
                            calculation = DosageCalculation(
                                type = CalculationType.FIXED,
                                value = 0.3
                            ),
                            unit = "mg",
                            volume = "0.3 ml"
                        ),
                        DosageRule(
                            ageGroup = AgeGroup.ADULT,
                            calculation = DosageCalculation(
                                type = CalculationType.FIXED,
                                value = 0.5
                            ),
                            unit = "mg",
                            volume = "0.5 ml"
                        )
                    )
                )
            ),
            warnings = listOf(
                "Extravasation vermeiden",
                "Bei Herzrhythmusstörungen Vorsicht"
            ),
            contraindications = listOf(
                "Keine absoluten Kontraindikationen in Notfallsituationen"
            )
        )
    }

    /**
     * Erstellt Atropin basierend auf Rettungsdienst-Protokoll
     */
    fun createAtropine(): Medication {
        return Medication(
            id = "atropin",
            name = "Atropin",
            category = MedicationCategory.CARDIOVASCULAR,
            indications = listOf(
                Indication(
                    name = "Bradykardie",
                    route = "i.v.",
                    preparation = "langsam injizieren",
                    dosageRules = listOf(
                        DosageRule(
                            ageGroup = AgeGroup.CHILD,
                            calculation = DosageCalculation(
                                type = CalculationType.PER_KG,
                                value = 0.02,
                                minDose = 0.1,
                                maxDose = 1.0
                            ),
                            unit = "mg"
                        ),
                        DosageRule(
                            ageGroup = AgeGroup.ADULT,
                            calculation = DosageCalculation(
                                type = CalculationType.FIXED,
                                value = 0.5,
                                maxDose = 3.0
                            ),
                            unit = "mg"
                        )
                    ),
                    maxDose = MaxDose(3.0, "mg", "pro Einsatz")
                )
            ),
            warnings = listOf(
                "Kann Tachykardie auslösen",
                "Bei Glaukom Vorsicht"
            )
        )
    }

    /**
     * Erstellt Glucose 40% für Hypoglykämie
     */
    fun createGlucose40(): Medication {
        return Medication(
            id = "glucose_40",
            name = "Glucose 40%",
            category = MedicationCategory.METABOLIC,
            indications = listOf(
                Indication(
                    name = "Hypoglykämie",
                    route = "i.v.",
                    preparation = "über großlumigen Zugang",
                    dosageRules = listOf(
                        DosageRule(
                            ageGroup = AgeGroup.INFANT,
                            calculation = DosageCalculation(
                                type = CalculationType.PER_KG,
                                value = 2.0
                            ),
                            unit = "ml"
                        ),
                        DosageRule(
                            ageGroup = AgeGroup.CHILD,
                            calculation = DosageCalculation(
                                type = CalculationType.PER_KG,
                                value = 1.0
                            ),
                            unit = "ml"
                        ),
                        DosageRule(
                            ageGroup = AgeGroup.ADULT,
                            calculation = DosageCalculation(
                                type = CalculationType.FIXED,
                                value = 50.0
                            ),
                            unit = "ml"
                        )
                    )
                )
            ),
            warnings = listOf(
                "Paravasation vermeiden!",
                "Kann Gewebe nekrotisieren"
            )
        )
    }
}

/**
 * Standard-Medikamenten-Set für Rettungsdienst
 */
object EmergencyMedications {
    fun getStandardSet(): List<Medication> {
        return listOf(
            MedicationFactory.createAdrenaline(),
            MedicationFactory.createAtropine(),
            MedicationFactory.createGlucose40()
        )
    }
}