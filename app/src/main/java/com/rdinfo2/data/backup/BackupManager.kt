// app/src/main/java/com/rdinfo2/data/backup/BackupManager.kt
package com.rdinfo2.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Zentrale Backup-Verwaltung für RDInfo2
 * Unterstützt automatische und manuelle Backups sowie Import/Export
 */
class BackupManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: BackupManager? = null

        fun getInstance(context: Context): BackupManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BackupManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val BACKUP_DIR = "backups"
        private const val BACKUP_PREFIX = "rdinfo2_backup"
        private const val BACKUP_EXTENSION = ".json"
        private const val MAX_AUTO_BACKUPS = 10
        private const val MAX_MANUAL_BACKUPS = 20
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val backupDir = File(context.filesDir, BACKUP_DIR).apply {
        if (!exists()) mkdirs()
    }

    /**
     * Erstellt ein vollständiges Backup aller App-Daten
     */
    suspend fun createFullBackup(
        isAutoBackup: Boolean = false,
        note: String? = null
    ): Result<BackupInfo> = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val backupData = BackupData(
                version = "1.0.0",
                timestamp = timestamp,
                isAutoBackup = isAutoBackup,
                note = note,
                // TODO: Diese Daten werden später aus Repository geholt
                medications = emptyList(),
                algorithms = emptyList(),
                references = emptyList(),
                settings = SettingsManager.getInstance(context).exportSettings(),
                patientDataRetentionPolicy = PatientDataRetentionPolicy.UNTIL_APP_CLOSE
            )

            val backupJson = json.encodeToString(backupData)
            val backupFile = createBackupFile(timestamp, isAutoBackup)

            backupFile.writeText(backupJson)

            val backupInfo = BackupInfo(
                id = generateBackupId(timestamp),
                timestamp = timestamp,
                filePath = backupFile.absolutePath,
                fileName = backupFile.name,
                size = backupFile.length(),
                isAutoBackup = isAutoBackup,
                note = note,
                checksum = calculateChecksum(backupJson)
            )

            // Alte Backups bereinigen
            cleanupOldBackups(isAutoBackup)

            // Backup-Zeitstempel in Settings aktualisieren
            SettingsManager.getInstance(context).lastBackupTimestamp = timestamp

            Result.success(backupInfo)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stellt ein Backup wieder her
     */
    suspend fun restoreBackup(backupInfo: BackupInfo): Result<RestoreResult> = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupInfo.filePath)
            if (!backupFile.exists()) {
                return@withContext Result.failure(IOException("Backup-Datei nicht gefunden: ${backupInfo.fileName}"))
            }

            val backupJson = backupFile.readText()
            val calculatedChecksum = calculateChecksum(backupJson)

            if (calculatedChecksum != backupInfo.checksum) {
                return@withContext Result.failure(IOException("Backup-Datei ist beschädigt (Checksum-Fehler)"))
            }

            val backupData = json.decodeFromString<BackupData>(backupJson)

            // Validierung der Backup-Version
            if (!isBackupVersionCompatible(backupData.version)) {
                return@withContext Result.failure(IOException("Backup-Version ${backupData.version} ist nicht kompatibel"))
            }

            // Settings wiederherstellen
            SettingsManager.getInstance(context).importSettings(backupData.settings)

            // TODO: Medications, Algorithms, References wiederherstellen
            // Diese Funktionalität wird implementiert wenn Repository fertig ist

            val restoreResult = RestoreResult(
                backupVersion = backupData.version,
                restoredItems = RestoreItems(
                    medications = backupData.medications.size,
                    algorithms = backupData.algorithms.size,
                    references = backupData.references.size,
                    settingsRestored = true
                ),
                restorationTimestamp = System.currentTimeMillis()
            )

            Result.success(restoreResult)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exportiert Backup in externe Datei (für Sharing/Cloud)
     */
    suspend fun exportBackup(backupInfo: BackupInfo, destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupInfo.filePath)
            if (!backupFile.exists()) {
                return@withContext Result.failure(IOException("Backup-Datei nicht gefunden"))
            }

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                backupFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Result.failure(IOException("Konnte nicht in Ziel-Uri schreiben"))

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Importiert Backup aus externer Datei
     */
    suspend fun importBackup(sourceUri: Uri, note: String? = null): Result<BackupInfo> = withContext(Dispatchers.IO) {
        try {
            val backupJson = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            } ?: return@withContext Result.failure(IOException("Konnte Datei nicht lesen"))

            // Validierung des JSON-Formats
            val backupData = json.decodeFromString<BackupData>(backupJson)

            // Als neues Backup speichern
            val timestamp = System.currentTimeMillis()
            val backupFile = createBackupFile(timestamp, false)
            backupFile.writeText(backupJson)

            val backupInfo = BackupInfo(
                id = generateBackupId(timestamp),
                timestamp = timestamp,
                filePath = backupFile.absolutePath,
                fileName = backupFile.name,
                size = backupFile.length(),
                isAutoBackup = false,
                note = note ?: "Importiert am ${formatTimestamp(timestamp)}",
                checksum = calculateChecksum(backupJson)
            )

            Result.success(backupInfo)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listet alle verfügbaren Backups auf
     */
    suspend fun listBackups(): Result<List<BackupInfo>> = withContext(Dispatchers.IO) {
        try {
            val backupFiles = backupDir.listFiles { _, name ->
                name.startsWith(BACKUP_PREFIX) && name.endsWith(BACKUP_EXTENSION)
            }?.sortedByDescending { it.lastModified() } ?: emptyList()

            val backupInfos = backupFiles.mapNotNull { file ->
                try {
                    val backupJson = file.readText()
                    val backupData = json.decodeFromString<BackupData>(backupJson)

                    BackupInfo(
                        id = generateBackupId(backupData.timestamp),
                        timestamp = backupData.timestamp,
                        filePath = file.absolutePath,
                        fileName = file.name,
                        size = file.length(),
                        isAutoBackup = backupData.isAutoBackup,
                        note = backupData.note,
                        checksum = calculateChecksum(backupJson)
                    )
                } catch (e: Exception) {
                    // Beschädigte Backup-Datei ignorieren
                    null
                }
            }

            Result.success(backupInfos)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Löscht ein Backup
     */
    suspend fun deleteBackup(backupInfo: BackupInfo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupInfo.filePath)
            if (backupFile.exists() && backupFile.delete()) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Konnte Backup-Datei nicht löschen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createBackupFile(timestamp: Long, isAutoBackup: Boolean): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateString = dateFormat.format(Date(timestamp))
        val autoPrefix = if (isAutoBackup) "auto_" else "manual_"
        val fileName = "${BACKUP_PREFIX}_${autoPrefix}${dateString}${BACKUP_EXTENSION}"
        return File(backupDir, fileName)
    }

    private fun generateBackupId(timestamp: Long): String {
        return "backup_$timestamp"
    }

    private fun calculateChecksum(data: String): String {
        // Einfache CRC32-basierte Prüfsumme
        val crc = java.util.zip.CRC32()
        crc.update(data.toByteArray())
        return crc.value.toString(16)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    private fun isBackupVersionCompatible(version: String): Boolean {
        // Vereinfachte Versionsprüfung
        val supportedVersions = listOf("1.0.0")
        return supportedVersions.contains(version)
    }

    private fun cleanupOldBackups(isAutoBackup: Boolean) {
        val maxBackups = if (isAutoBackup) MAX_AUTO_BACKUPS else MAX_MANUAL_BACKUPS
        val backupFiles = backupDir.listFiles { _, name ->
            name.startsWith(BACKUP_PREFIX) &&
                    name.endsWith(BACKUP_EXTENSION) &&
                    name.contains(if (isAutoBackup) "auto_" else "manual_")
        }?.sortedByDescending { it.lastModified() } ?: return

        if (backupFiles.size > maxBackups) {
            backupFiles.drop(maxBackups).forEach { file ->
                file.delete()
            }
        }
    }
}

@Serializable
data class BackupData(
    val version: String,
    val timestamp: Long,
    val isAutoBackup: Boolean,
    val note: String? = null,
    val medications: List<String> = emptyList(), // TODO: Replace with actual Medication objects
    val algorithms: List<String> = emptyList(),  // TODO: Replace with actual Algorithm objects
    val references: List<String> = emptyList(),  // TODO: Replace with actual Reference objects
    val settings: Map<String, @Serializable(with = AnyValueSerializer::class) Any?>,
    val patientDataRetentionPolicy: PatientDataRetentionPolicy
)

@Serializable
data class BackupInfo(
    val id: String,
    val timestamp: Long,
    val filePath: String,
    val fileName: String,
    val size: Long,
    val isAutoBackup: Boolean,
    val note: String? = null,
    val checksum: String
) {
    val formattedDate: String
        get() {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return dateFormat.format(Date(timestamp))
        }

    val formattedSize: String
        get() {
            return when {
                size < 1024 -> "${size} B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                else -> "${size / (1024 * 1024)} MB"
            }
        }
}

@Serializable
data class RestoreResult(
    val backupVersion: String,
    val restoredItems: RestoreItems,
    val restorationTimestamp: Long
)

@Serializable
data class RestoreItems(
    val medications: Int,
    val algorithms: Int,
    val references: Int,
    val settingsRestored: Boolean
)

@Serializable
enum class PatientDataRetentionPolicy {
    UNTIL_APP_CLOSE,    // Daten nur bis App geschlossen wird
    ONE_HOUR,           // 1 Stunde
    EIGHT_HOURS,        // 8 Stunden (Schichtende)
    TWENTY_FOUR_HOURS,  // 24 Stunden
    NEVER_STORE         // Niemals speichern
}

// Serializer für Any?-Werte in Settings Map
object AnyValueSerializer : kotlinx.serialization.KSerializer<Any?> {
    override val descriptor = kotlinx.serialization.descriptors.buildClassSerialDescriptor("AnyValue")

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Any?) {
        when (value) {
            null -> encoder.encodeNull()
            is String -> encoder.encodeString(value)
            is Boolean -> encoder.encodeBoolean(value)
            is Int -> encoder.encodeInt(value)
            is Long -> encoder.encodeLong(value)
            is Float -> encoder.encodeFloat(value)
            is Double -> encoder.encodeDouble(value)
            is List<*> -> {
                val list = value.filterIsInstance<String>()
                encoder.encodeSerializableValue(kotlinx.serialization.builtins.ListSerializer(kotlinx.serialization.builtins.serializer()), list)
            }
            else -> encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Any? {
        // Simplified deserialization - in real app, this would be more sophisticated
        return try {
            decoder.decodeString()
        } catch (e: Exception) {
            null
        }
    }
}