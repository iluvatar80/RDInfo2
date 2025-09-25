// app/src/main/java/com/rdinfo2/data/preferences/SettingsManager.kt
package com.rdinfo2.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Zentrale Verwaltung aller App-Einstellungen
 * Unterstützt Backup/Restore und systemweite Konfiguration
 */
class SettingsManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        // SharedPreferences Keys
        private const val PREFS_NAME = "rdinfo2_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_SHOW_ADVANCED_FEATURES = "show_advanced_features"
        private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val KEY_BACKUP_INTERVAL_HOURS = "backup_interval_hours"
        private const val KEY_LAST_BACKUP_TIMESTAMP = "last_backup_timestamp"
        private const val KEY_PATIENT_DATA_RETENTION = "patient_data_retention"
        private const val KEY_GESTURE_NAVIGATION = "gesture_navigation"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_SOUND_ALERTS = "sound_alerts"
        private const val KEY_TIMER_ENABLED = "timer_enabled"
        private const val KEY_VOICE_ANNOUNCEMENTS = "voice_announcements"
        private const val KEY_HIGH_CONTRAST_MODE = "high_contrast_mode"
        private const val KEY_FONT_SIZE_MULTIPLIER = "font_size_multiplier"
        private const val KEY_DOSING_SAFETY_CHECKS = "dosing_safety_checks"
        private const val KEY_CONFIRM_DANGEROUS_ACTIONS = "confirm_dangerous_actions"
        private const val KEY_SHOW_TOOLTIPS = "show_tooltips"
        private const val KEY_PREFERRED_UNITS = "preferred_units"
        private const val KEY_DECIMAL_PLACES = "decimal_places"
        private const val KEY_QUICK_ACCESS_ITEMS = "quick_access_items"
        private const val KEY_RECENTLY_USED_MEDS = "recently_used_meds"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Theme Settings
    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun getThemeMode(): ThemeMode {
        val mode = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return ThemeMode.valueOf(mode ?: ThemeMode.SYSTEM.name)
    }

    // User Role Settings (prepared for future use)
    private val _userRole = MutableStateFlow(getUserRole())
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    fun setUserRole(role: UserRole) {
        prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
        _userRole.value = role
    }

    private fun getUserRole(): UserRole {
        val role = prefs.getString(KEY_USER_ROLE, UserRole.NOTSAN.name)
        return UserRole.valueOf(role ?: UserRole.NOTSAN.name)
    }

    // Backup Settings
    var autoBackupEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, value).apply()

    var backupIntervalHours: Int
        get() = prefs.getInt(KEY_BACKUP_INTERVAL_HOURS, 24)
        set(value) = prefs.edit().putInt(KEY_BACKUP_INTERVAL_HOURS, value).apply()

    var lastBackupTimestamp: Long
        get() = prefs.getLong(KEY_LAST_BACKUP_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_BACKUP_TIMESTAMP, value).apply()

    // UI/UX Settings
    var gestureNavigationEnabled: Boolean
        get() = prefs.getBoolean(KEY_GESTURE_NAVIGATION, true)
        set(value) = prefs.edit().putBoolean(KEY_GESTURE_NAVIGATION, value).apply()

    var hapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, value).apply()

    var soundAlertsEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ALERTS, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ALERTS, value).apply()

    var timerEnabled: Boolean
        get() = prefs.getBoolean(KEY_TIMER_ENABLED, false) // Initially disabled
        set(value) = prefs.edit().putBoolean(KEY_TIMER_ENABLED, value).apply()

    var voiceAnnouncementsEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_ANNOUNCEMENTS, false)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_ANNOUNCEMENTS, value).apply()

    // Accessibility Settings
    var highContrastMode: Boolean
        get() = prefs.getBoolean(KEY_HIGH_CONTRAST_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_HIGH_CONTRAST_MODE, value).apply()

    var fontSizeMultiplier: Float
        get() = prefs.getFloat(KEY_FONT_SIZE_MULTIPLIER, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_FONT_SIZE_MULTIPLIER, value).apply()

    // Safety Settings
    var dosingSafetyChecks: Boolean
        get() = prefs.getBoolean(KEY_DOSING_SAFETY_CHECKS, true)
        set(value) = prefs.edit().putBoolean(KEY_DOSING_SAFETY_CHECKS, value).apply()

    var confirmDangerousActions: Boolean
        get() = prefs.getBoolean(KEY_CONFIRM_DANGEROUS_ACTIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_CONFIRM_DANGEROUS_ACTIONS, value).apply()

    var showTooltips: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TOOLTIPS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TOOLTIPS, value).apply()

    // Calculation Settings
    var preferredUnits: String
        get() = prefs.getString(KEY_PREFERRED_UNITS, "mg") ?: "mg"
        set(value) = prefs.edit().putString(KEY_PREFERRED_UNITS, value).apply()

    var decimalPlaces: Int
        get() = prefs.getInt(KEY_DECIMAL_PLACES, 2)
        set(value) = prefs.edit().putInt(KEY_DECIMAL_PLACES, value).apply()

    // Patient Data Retention
    var patientDataRetentionHours: Int
        get() = prefs.getInt(KEY_PATIENT_DATA_RETENTION, 0) // 0 = until app close
        set(value) = prefs.edit().putInt(KEY_PATIENT_DATA_RETENTION, value).apply()

    // Advanced Features
    var showAdvancedFeatures: Boolean
        get() = prefs.getBoolean(KEY_SHOW_ADVANCED_FEATURES, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_ADVANCED_FEATURES, value).apply()

    // Quick Access customization
    fun getQuickAccessItems(): List<String> {
        val items = prefs.getString(KEY_QUICK_ACCESS_ITEMS, "")
        return if (items.isNullOrEmpty()) {
            getDefaultQuickAccessItems()
        } else {
            items.split(",")
        }
    }

    fun setQuickAccessItems(items: List<String>) {
        val itemsString = items.joinToString(",")
        prefs.edit().putString(KEY_QUICK_ACCESS_ITEMS, itemsString).apply()
    }

    private fun getDefaultQuickAccessItems(): List<String> {
        return listOf("adrenalin", "acetylsalicylsaeure", "amiodaron", "atropin", "glucose_20")
    }

    // Recently used medications
    fun getRecentlyUsedMedications(): List<String> {
        val recent = prefs.getString(KEY_RECENTLY_USED_MEDS, "")
        return if (recent.isNullOrEmpty()) {
            emptyList()
        } else {
            recent.split(",").take(10) // Keep last 10
        }
    }

    fun addRecentlyUsedMedication(medicationId: String) {
        val current = getRecentlyUsedMedications().toMutableList()
        current.remove(medicationId) // Remove if already exists
        current.add(0, medicationId) // Add to front
        val updated = current.take(10) // Keep only 10 most recent
        val updatedString = updated.joinToString(",")
        prefs.edit().putString(KEY_RECENTLY_USED_MEDS, updatedString).apply()
    }

    // Settings Export/Import for Backup
    fun exportSettings(): Map<String, Any?> {
        return mapOf(
            KEY_THEME_MODE to themeMode.value.name,
            KEY_USER_ROLE to userRole.value.name,
            KEY_AUTO_BACKUP_ENABLED to autoBackupEnabled,
            KEY_BACKUP_INTERVAL_HOURS to backupIntervalHours,
            KEY_GESTURE_NAVIGATION to gestureNavigationEnabled,
            KEY_HAPTIC_FEEDBACK to hapticFeedbackEnabled,
            KEY_SOUND_ALERTS to soundAlertsEnabled,
            KEY_TIMER_ENABLED to timerEnabled,
            KEY_VOICE_ANNOUNCEMENTS to voiceAnnouncementsEnabled,
            KEY_HIGH_CONTRAST_MODE to highContrastMode,
            KEY_FONT_SIZE_MULTIPLIER to fontSizeMultiplier,
            KEY_DOSING_SAFETY_CHECKS to dosingSafetyChecks,
            KEY_CONFIRM_DANGEROUS_ACTIONS to confirmDangerousActions,
            KEY_SHOW_TOOLTIPS to showTooltips,
            KEY_PREFERRED_UNITS to preferredUnits,
            KEY_DECIMAL_PLACES to decimalPlaces,
            KEY_PATIENT_DATA_RETENTION to patientDataRetentionHours,
            KEY_SHOW_ADVANCED_FEATURES to showAdvancedFeatures,
            KEY_QUICK_ACCESS_ITEMS to getQuickAccessItems(),
            KEY_RECENTLY_USED_MEDS to getRecentlyUsedMedications()
        )
    }

    fun importSettings(settings: Map<String, Any?>) {
        val editor = prefs.edit()

        settings.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                is List<*> -> {
                    val stringValue = (value as? List<String>)?.joinToString(",") ?: ""
                    editor.putString(key, stringValue)
                }
            }
        }

        editor.apply()

        // Update StateFlows
        _themeMode.value = getThemeMode()
        _userRole.value = getUserRole()
    }

    // Reset to defaults
    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _themeMode.value = ThemeMode.SYSTEM
        _userRole.value = UserRole.NOTSAN
    }
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class UserRole {
    NOTSAN,          // Notfallsanitäter
    NOTARZT,         // Notarzt
    RETTUNGSASSISTENT, // Rettungsassistent
    STUDENT,         // Student/Azubi
    ADMIN            // Administrator/Editor
}