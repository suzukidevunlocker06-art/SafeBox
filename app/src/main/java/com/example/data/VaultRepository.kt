package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import com.example.data.model.FileCategory
import com.example.data.model.VaultCategory
import com.example.data.model.VaultEntry
import com.example.data.model.VaultFileItem
import com.example.data.model.VaultNoteItem
import com.example.data.model.VaultSettings
import com.example.security.CryptoManager
import com.example.security.PasswordStrengthLevel
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

data class SecurityAuditSummary(
  val totalItems: Int,
  val totalPasswords: Int,
  val weakPasswordsCount: Int,
  val reusedPasswordsCount: Int,
  val securityScorePercent: Int
)

data class TrashCollection(
  val entries: List<VaultEntry>,
  val notes: List<VaultNoteItem>,
  val files: List<VaultFileItem>
) {
  val totalCount: Int get() = entries.size + notes.size + files.size
}

data class VaultStorageStats(
  val photosBytes: Long,
  val videosBytes: Long,
  val documentsBytes: Long,
  val notesBytes: Long,
  val credentialsBytes: Long,
  val totalVaultBytes: Long
)

sealed class ImportResult {
  data class Success(val item: VaultFileItem) : ImportResult()
  data class Duplicate(val existingName: String) : ImportResult()
  data class Error(val message: String) : ImportResult()
}

class VaultRepository(private val context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("safebox_vault_prefs", Context.MODE_PRIVATE)

  private var activeKey: SecretKey? = null

  companion object {
    private const val KEY_SETUP_COMPLETED = "setup_completed"
    private const val KEY_SALT = "master_salt"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_ITEMS_JSON = "encrypted_items_data"
    private const val KEY_ITEMS_IV = "encrypted_items_iv"
    private const val KEY_NOTES_JSON = "encrypted_notes_data"
    private const val KEY_NOTES_IV = "encrypted_notes_iv"
    private const val KEY_FILES_META_JSON = "encrypted_files_meta_data"
    private const val KEY_FILES_META_IV = "encrypted_files_meta_iv"
    private const val KEY_SETTINGS_JSON = "vault_settings_json"
    private const val KEY_KEYSTORE_WRAPPED_KEY = "keystore_wrapped_key"
    private const val KEY_KEYSTORE_WRAPPED_IV = "keystore_wrapped_iv"

    private const val VAULT_FILES_DIR = "vault_files"
    private const val EXPORTS_DIR = "exports"
  }

  private val vaultDir: File
    get() {
      val dir = File(context.filesDir, VAULT_FILES_DIR)
      if (!dir.exists()) dir.mkdirs()
      return dir
    }

  private val exportsDir: File
    get() {
      val dir = File(context.cacheDir, EXPORTS_DIR)
      if (!dir.exists()) dir.mkdirs()
      return dir
    }

  fun isSetup(): Boolean = prefs.getBoolean(KEY_SETUP_COMPLETED, false)

  fun isUnlocked(): Boolean = activeKey != null

  fun getActiveKey(): SecretKey? = activeKey

  fun lock() {
    activeKey = null
  }

  // --- SETTINGS & LOCKOUT ---

  fun getSettings(): VaultSettings {
    val jsonStr = prefs.getString(KEY_SETTINGS_JSON, null) ?: return VaultSettings()
    return try {
      val obj = JSONObject(jsonStr)
      VaultSettings(
        biometricsEnabled = obj.optBoolean("biometricsEnabled", true),
        autoLockSeconds = obj.optInt("autoLockSeconds", 60),
        lockOnBackground = obj.optBoolean("lockOnBackground", true),
        screenCaptureProtection = obj.optBoolean("screenCaptureProtection", true),
        themeMode = obj.optString("themeMode", "DARK"),
        failedAttempts = obj.optInt("failedAttempts", 0),
        lockoutUntil = obj.optLong("lockoutUntil", 0L)
      )
    } catch (e: Exception) {
      VaultSettings()
    }
  }

  fun saveSettings(settings: VaultSettings) {
    try {
      val obj = JSONObject().apply {
        put("biometricsEnabled", settings.biometricsEnabled)
        put("autoLockSeconds", settings.autoLockSeconds)
        put("lockOnBackground", settings.lockOnBackground)
        put("screenCaptureProtection", settings.screenCaptureProtection)
        put("themeMode", settings.themeMode)
        put("failedAttempts", settings.failedAttempts)
        put("lockoutUntil", settings.lockoutUntil)
      }
      prefs.edit().putString(KEY_SETTINGS_JSON, obj.toString()).apply()
    } catch (e: Exception) {
      // ignore
    }
  }

  fun recordFailedAttempt(): Pair<Int, Long> {
    val current = getSettings()
    val attempts = current.failedAttempts + 1
    val lockoutDurationSeconds = when {
      attempts >= 8 -> 60L
      attempts >= 5 -> 30L
      attempts >= 3 -> 15L
      else -> 0L
    }
    val lockoutUntil = if (lockoutDurationSeconds > 0) {
      System.currentTimeMillis() + (lockoutDurationSeconds * 1000L)
    } else 0L

    val updated = current.copy(
      failedAttempts = attempts,
      lockoutUntil = lockoutUntil
    )
    saveSettings(updated)
    return Pair(attempts, lockoutDurationSeconds)
  }

  fun resetFailedAttempts() {
    val current = getSettings()
    if (current.failedAttempts > 0 || current.lockoutUntil > 0L) {
      saveSettings(current.copy(failedAttempts = 0, lockoutUntil = 0L))
    }
  }

  fun getLockoutRemainingSeconds(): Long {
    val until = getSettings().lockoutUntil
    val remainingMs = until - System.currentTimeMillis()
    return if (remainingMs > 0) (remainingMs / 1000L) + 1 else 0L
  }

  // --- SETUP & AUTHENTICATION ---

  fun setupVault(pin: String): Boolean {
    val salt = CryptoManager.generateSalt()
    val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
    val pinHash = CryptoManager.hashPin(pin, salt)
    val key = CryptoManager.deriveKey(pin, salt)
    activeKey = key

    prefs.edit()
      .putBoolean(KEY_SETUP_COMPLETED, true)
      .putString(KEY_SALT, saltBase64)
      .putString(KEY_PIN_HASH, pinHash)
      .apply()

    // Try wrapping with KeyStore for biometric unlock
    tryWrapKeyWithKeyStore(key)

    // Seed initial vault items
    val initialItems = listOf(
      VaultEntry(
        id = UUID.randomUUID().toString(),
        type = VaultCategory.PASSWORD,
        title = "Google Account",
        folder = "Personal",
        isFavorite = true,
        payloadJson = JSONObject().apply {
          put("username", "usuario@gmail.com")
          put("password", "G8#mK9!qL2\$vP7x")
          put("url", "https://accounts.google.com")
          put("notes", "Cuenta principal de correo y sincronización")
        }.toString()
      ),
      VaultEntry(
        id = UUID.randomUUID().toString(),
        type = VaultCategory.CARD,
        title = "Tarjeta Visa Signature",
        folder = "Finanzas",
        isFavorite = false,
        payloadJson = JSONObject().apply {
          put("cardholderName", "TITULAR SEGURO")
          put("cardNumber", "4532890123456789")
          put("expiryMonth", "11")
          put("expiryYear", "28")
          put("cvv", "482")
          put("bank", "Banco Santander")
        }.toString()
      ),
      VaultEntry(
        id = UUID.randomUUID().toString(),
        type = VaultCategory.IDENTITY,
        title = "Documento Nacional de Identidad",
        folder = "Legal",
        isFavorite = false,
        payloadJson = JSONObject().apply {
          put("idType", "DNI / Pasaporte")
          put("idNumber", "54891234-K")
          put("fullName", "Alex Dev")
          put("country", "España")
        }.toString()
      )
    )
    saveItems(initialItems)

    // Seed initial note
    val initialNotes = listOf(
      VaultNoteItem(
        id = UUID.randomUUID().toString(),
        title = "Claves de Respaldo Wi-Fi y Servidores",
        content = "SSID: FibraOptica_5G\nPassword: Safe_Wifi#9982\nRouter IP: 192.168.1.1\n\nClave SSH Servidor Backup:\nssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIExample",
        category = "Hogar",
        isFavorite = true
      )
    )
    saveNotes(initialNotes)

    return true
  }

  fun unlock(pin: String): Boolean {
    if (getLockoutRemainingSeconds() > 0) return false

    val saltBase64 = prefs.getString(KEY_SALT, null) ?: return false
    val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false

    val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
    val inputHash = CryptoManager.hashPin(pin, salt)

    if (inputHash == storedHash) {
      activeKey = CryptoManager.deriveKey(pin, salt)
      resetFailedAttempts()
      // Refresh keystore wrapped key if needed
      tryWrapKeyWithKeyStore(activeKey!!)
      return true
    } else {
      recordFailedAttempt()
      return false
    }
  }

  fun changePin(oldPin: String, newPin: String): Boolean {
    val saltBase64 = prefs.getString(KEY_SALT, null) ?: return false
    val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false

    val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
    val inputHash = CryptoManager.hashPin(oldPin, salt)
    if (inputHash != storedHash) return false

    // Load all data with old key
    val oldKey = activeKey ?: CryptoManager.deriveKey(oldPin, salt)
    activeKey = oldKey

    val items = getItems(includeTrash = true)
    val notes = getNotes(includeTrash = true)
    val files = getAllFiles(includeTrash = true)

    // Generate new salt and derive new key
    val newSalt = CryptoManager.generateSalt()
    val newSaltBase64 = Base64.encodeToString(newSalt, Base64.NO_WRAP)
    val newHash = CryptoManager.hashPin(newPin, newSalt)
    val newKey = CryptoManager.deriveKey(newPin, newSalt)

    // Re-encrypt files on disk with new key
    for (f in files) {
      val originalEncFile = File(vaultDir, f.storageFileName)
      if (originalEncFile.exists()) {
        try {
          val iv = Base64.decode(f.ivBase64, Base64.NO_WRAP)
          val encBytes = originalEncFile.readBytes()
          val decrypted = CryptoManager.decryptBytes(encBytes, iv, oldKey)
          val (newEnc, newIv) = CryptoManager.encryptBytes(decrypted, newKey)
          originalEncFile.writeBytes(newEnc)
        } catch (e: Exception) {
          // ignore
        }
      }
    }

    // Set activeKey to newKey and save items and notes
    activeKey = newKey
    saveItems(items)
    saveNotes(notes)

    prefs.edit()
      .putString(KEY_SALT, newSaltBase64)
      .putString(KEY_PIN_HASH, newHash)
      .apply()

    tryWrapKeyWithKeyStore(newKey)
    return true
  }

  fun unlockWithBiometrics(): Boolean {
    val wrapped = prefs.getString(KEY_KEYSTORE_WRAPPED_KEY, null) ?: return false
    val iv = prefs.getString(KEY_KEYSTORE_WRAPPED_IV, null) ?: return false
    val keyBytes = CryptoManager.KeyStoreHelper.unwrapMasterKey(wrapped, iv) ?: return false
    activeKey = SecretKeySpec(keyBytes, "AES")
    resetFailedAttempts()
    return true
  }

  private fun tryWrapKeyWithKeyStore(key: SecretKey) {
    val result = CryptoManager.KeyStoreHelper.wrapMasterKey(key.encoded)
    if (result != null) {
      prefs.edit()
        .putString(KEY_KEYSTORE_WRAPPED_KEY, result.first)
        .putString(KEY_KEYSTORE_WRAPPED_IV, result.second)
        .apply()
    }
  }

  fun unlockBypassForDemo(): Boolean {
    val saltBase64 = prefs.getString(KEY_SALT, null)
    if (saltBase64 != null) {
      val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
      activeKey = CryptoManager.deriveKey("1234", salt)
      return true
    }
    return false
  }

  // --- VAULT ENTRIES (Passwords, Cards, Identities) ---

  fun getItems(includeTrash: Boolean = false): List<VaultEntry> {
    val key = activeKey ?: return emptyList()
    val encryptedData = prefs.getString(KEY_ITEMS_JSON, null) ?: return emptyList()
    val iv = prefs.getString(KEY_ITEMS_IV, null) ?: return emptyList()

    return try {
      val decryptedJson = CryptoManager.decrypt(encryptedData, iv, key)
      val jsonArray = JSONArray(decryptedJson)
      val list = mutableListOf<VaultEntry>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val isTrash = obj.optBoolean("isTrash", false)
        if (includeTrash || !isTrash) {
          list.add(
            VaultEntry(
              id = obj.getString("id"),
              type = VaultCategory.valueOf(obj.getString("type")),
              title = obj.getString("title"),
              folder = obj.optString("folder", "General"),
              isFavorite = obj.optBoolean("isFavorite", false),
              updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
              payloadJson = obj.optString("payloadJson", "{}"),
              isTrash = isTrash,
              deletedAt = if (obj.has("deletedAt") && !obj.isNull("deletedAt")) obj.getLong("deletedAt") else null
            )
          )
        }
      }
      list.sortedByDescending { it.updatedAt }
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun saveItems(items: List<VaultEntry>): Boolean {
    val key = activeKey ?: return false
    return try {
      val jsonArray = JSONArray()
      for (item in items) {
        val obj = JSONObject().apply {
          put("id", item.id)
          put("type", item.type.name)
          put("title", item.title)
          put("folder", item.folder)
          put("isFavorite", item.isFavorite)
          put("updatedAt", item.updatedAt)
          put("payloadJson", item.payloadJson)
          put("isTrash", item.isTrash)
          if (item.deletedAt != null) put("deletedAt", item.deletedAt)
        }
        jsonArray.put(obj)
      }
      val (encryptedBase64, ivBase64) = CryptoManager.encrypt(jsonArray.toString(), key)
      prefs.edit()
        .putString(KEY_ITEMS_JSON, encryptedBase64)
        .putString(KEY_ITEMS_IV, ivBase64)
        .apply()
      true
    } catch (e: Exception) {
      false
    }
  }

  fun addItem(entry: VaultEntry): Boolean {
    val current = getItems(includeTrash = true).toMutableList()
    current.add(0, entry)
    return saveItems(current)
  }

  fun updateItem(entry: VaultEntry): Boolean {
    val current = getItems(includeTrash = true).toMutableList()
    val index = current.indexOfFirst { it.id == entry.id }
    if (index >= 0) {
      current[index] = entry.copy(updatedAt = System.currentTimeMillis())
      return saveItems(current)
    }
    return false
  }

  fun moveItemToTrash(id: String): Boolean {
    val current = getItems(includeTrash = true).toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index >= 0) {
      current[index] = current[index].copy(isTrash = true, deletedAt = System.currentTimeMillis())
      return saveItems(current)
    }
    return false
  }

  fun restoreItemFromTrash(id: String): Boolean {
    val current = getItems(includeTrash = true).toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index >= 0) {
      current[index] = current[index].copy(isTrash = false, deletedAt = null)
      return saveItems(current)
    }
    return false
  }

  fun deleteItemPermanently(id: String): Boolean {
    val current = getItems(includeTrash = true).filter { it.id != id }
    return saveItems(current)
  }

  fun toggleFavorite(id: String): Boolean {
    val current = getItems(includeTrash = true).toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index >= 0) {
      current[index] = current[index].copy(isFavorite = !current[index].isFavorite)
      return saveItems(current)
    }
    return false
  }

  // --- PRIVATE NOTES ---

  fun getNotes(includeTrash: Boolean = false): List<VaultNoteItem> {
    val key = activeKey ?: return emptyList()
    val encryptedData = prefs.getString(KEY_NOTES_JSON, null) ?: return emptyList()
    val iv = prefs.getString(KEY_NOTES_IV, null) ?: return emptyList()

    return try {
      val decryptedJson = CryptoManager.decrypt(encryptedData, iv, key)
      val jsonArray = JSONArray(decryptedJson)
      val list = mutableListOf<VaultNoteItem>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val isTrash = obj.optBoolean("isTrash", false)
        if (includeTrash || !isTrash) {
          list.add(
            VaultNoteItem(
              id = obj.getString("id"),
              title = obj.getString("title"),
              content = obj.getString("content"),
              category = obj.optString("category", "General"),
              isFavorite = obj.optBoolean("isFavorite", false),
              isTrash = isTrash,
              createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
              updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
              deletedAt = if (obj.has("deletedAt") && !obj.isNull("deletedAt")) obj.getLong("deletedAt") else null
            )
          )
        }
      }
      list.sortedByDescending { it.updatedAt }
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun saveNotes(notes: List<VaultNoteItem>): Boolean {
    val key = activeKey ?: return false
    return try {
      val jsonArray = JSONArray()
      for (note in notes) {
        val obj = JSONObject().apply {
          put("id", note.id)
          put("title", note.title)
          put("content", note.content)
          put("category", note.category)
          put("isFavorite", note.isFavorite)
          put("isTrash", note.isTrash)
          put("createdAt", note.createdAt)
          put("updatedAt", note.updatedAt)
          if (note.deletedAt != null) put("deletedAt", note.deletedAt)
        }
        jsonArray.put(obj)
      }
      val (encryptedBase64, ivBase64) = CryptoManager.encrypt(jsonArray.toString(), key)
      prefs.edit()
        .putString(KEY_NOTES_JSON, encryptedBase64)
        .putString(KEY_NOTES_IV, ivBase64)
        .apply()
      true
    } catch (e: Exception) {
      false
    }
  }

  fun addNote(note: VaultNoteItem): Boolean {
    val current = getNotes(includeTrash = true).toMutableList()
    current.add(0, note)
    return saveNotes(current)
  }

  fun updateNote(note: VaultNoteItem): Boolean {
    val current = getNotes(includeTrash = true).toMutableList()
    val index = current.indexOfFirst { it.id == note.id }
    if (index >= 0) {
      current[index] = note.copy(updatedAt = System.currentTimeMillis())
      return saveNotes(current)
    }
    return false
  }

  fun moveNoteToTrash(id: String): Boolean {
    val current = getNotes(includeTrash = true).toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index >= 0) {
      current[index] = current[index].copy(isTrash = true, deletedAt = System.currentTimeMillis())
      return saveNotes(current)
    }
    return false
  }

  fun restoreNoteFromTrash(id: String): Boolean {
    val current = getNotes(includeTrash = true).toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index >= 0) {
      current[index] = current[index].copy(isTrash = false, deletedAt = null)
      return saveNotes(current)
    }
    return false
  }

  fun deleteNotePermanently(id: String): Boolean {
    val current = getNotes(includeTrash = true).filter { it.id != id }
    return saveNotes(current)
  }

  fun toggleNoteFavorite(id: String): Boolean {
    val current = getNotes(includeTrash = true).toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index >= 0) {
      current[index] = current[index].copy(isFavorite = !current[index].isFavorite)
      return saveNotes(current)
    }
    return false
  }

  // --- ENCRYPTED FILES (Photos, Videos, Documents) ---

  fun getAllFiles(includeTrash: Boolean = false): List<VaultFileItem> {
    val key = activeKey ?: return emptyList()
    val encryptedData = prefs.getString(KEY_FILES_META_JSON, null) ?: return emptyList()
    val iv = prefs.getString(KEY_FILES_META_IV, null) ?: return emptyList()

    return try {
      val decryptedJson = CryptoManager.decrypt(encryptedData, iv, key)
      val jsonArray = JSONArray(decryptedJson)
      val list = mutableListOf<VaultFileItem>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val isTrash = obj.optBoolean("isTrash", false)
        if (includeTrash || !isTrash) {
          list.add(
            VaultFileItem(
              id = obj.getString("id"),
              category = FileCategory.valueOf(obj.getString("category")),
              originalName = obj.getString("originalName"),
              extension = obj.optString("extension", ""),
              mimeType = obj.optString("mimeType", "application/octet-stream"),
              sizeBytes = obj.getLong("sizeBytes"),
              sha256 = obj.getString("sha256"),
              storageFileName = obj.getString("storageFileName"),
              ivBase64 = obj.getString("ivBase64"),
              isFavorite = obj.optBoolean("isFavorite", false),
              isTrash = isTrash,
              createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
              deletedAt = if (obj.has("deletedAt") && !obj.isNull("deletedAt")) obj.getLong("deletedAt") else null,
              tag = obj.optString("tag", "General"),
              extraInfo = obj.optString("extraInfo", "")
            )
          )
        }
      }
      list.sortedByDescending { it.createdAt }
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun getFilesByCategory(category: FileCategory): List<VaultFileItem> {
    return getAllFiles(includeTrash = false).filter { it.category == category }
  }

  private fun saveFilesMeta(files: List<VaultFileItem>): Boolean {
    val key = activeKey ?: return false
    return try {
      val jsonArray = JSONArray()
      for (file in files) {
        val obj = JSONObject().apply {
          put("id", file.id)
          put("category", file.category.name)
          put("originalName", file.originalName)
          put("extension", file.extension)
          put("mimeType", file.mimeType)
          put("sizeBytes", file.sizeBytes)
          put("sha256", file.sha256)
          put("storageFileName", file.storageFileName)
          put("ivBase64", file.ivBase64)
          put("isFavorite", file.isFavorite)
          put("isTrash", file.isTrash)
          put("createdAt", file.createdAt)
          if (file.deletedAt != null) put("deletedAt", file.deletedAt)
          put("tag", file.tag)
          put("extraInfo", file.extraInfo)
        }
        jsonArray.put(obj)
      }
      val (encryptedBase64, ivBase64) = CryptoManager.encrypt(jsonArray.toString(), key)
      prefs.edit()
        .putString(KEY_FILES_META_JSON, encryptedBase64)
        .putString(KEY_FILES_META_IV, ivBase64)
        .apply()
      true
    } catch (e: Exception) {
      false
    }
  }

  fun importFileFromBytes(
    bytes: ByteArray,
    category: FileCategory,
    originalName: String,
    mimeType: String,
    extension: String = ""
  ): ImportResult {
    val key = activeKey ?: return ImportResult.Error("Bóveda bloqueada")

    // Check duplicate using SHA-256
    val sha256 = CryptoManager.calculateSha256(bytes)
    val existing = getAllFiles(includeTrash = false).firstOrNull { it.sha256 == sha256 }
    if (existing != null) {
      return ImportResult.Duplicate(existing.originalName)
    }

    val (encryptedBytes, iv) = CryptoManager.encryptBytes(bytes, key)
    val storageFileName = "${UUID.randomUUID()}.enc"
    val destFile = File(vaultDir, storageFileName)

    try {
      FileOutputStream(destFile).use { it.write(encryptedBytes) }
      if (!destFile.exists() || destFile.length() == 0L) {
        return ImportResult.Error("Error de escritura cifrada en disco")
      }

      val ext = if (extension.isNotEmpty()) extension
      else originalName.substringAfterLast('.', "")

      val item = VaultFileItem(
        id = UUID.randomUUID().toString(),
        category = category,
        originalName = originalName,
        extension = ext,
        mimeType = mimeType,
        sizeBytes = bytes.size.toLong(),
        sha256 = sha256,
        storageFileName = storageFileName,
        ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP),
        isFavorite = false,
        isTrash = false,
        createdAt = System.currentTimeMillis()
      )

      val all = getAllFiles(includeTrash = true).toMutableList()
      all.add(0, item)
      saveFilesMeta(all)
      return ImportResult.Success(item)
    } catch (e: Exception) {
      if (destFile.exists()) destFile.delete()
      return ImportResult.Error(e.message ?: "Fallo de cifrado")
    }
  }

  fun importFileFromUri(
    uri: Uri,
    category: FileCategory,
    context: Context
  ): ImportResult {
    return try {
      val contentResolver = context.contentResolver
      val mimeType = contentResolver.getType(uri) ?: when (category) {
        FileCategory.PHOTO -> "image/jpeg"
        FileCategory.VIDEO -> "video/mp4"
        FileCategory.DOCUMENT -> "application/octet-stream"
      }

      var fileName = "archivo_${System.currentTimeMillis()}"
      var size: Long = 0
      contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
          if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
          val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
          if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
        }
      }

      val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: return ImportResult.Error("No se pudo leer el archivo")

      importFileFromBytes(
        bytes = bytes,
        category = category,
        originalName = fileName,
        mimeType = mimeType
      )
    } catch (e: Exception) {
      ImportResult.Error("Error al importar: ${e.localizedMessage}")
    }
  }

  fun decryptFileBytes(item: VaultFileItem): ByteArray? {
    val key = activeKey ?: return null
    val encFile = File(vaultDir, item.storageFileName)
    if (!encFile.exists()) return null
    return try {
      val iv = Base64.decode(item.ivBase64, Base64.NO_WRAP)
      val encBytes = encFile.readBytes()
      CryptoManager.decryptBytes(encBytes, iv, key)
    } catch (e: Exception) {
      null
    }
  }

  fun decryptFileToTemp(item: VaultFileItem): File? {
    val bytes = decryptFileBytes(item) ?: return null
    return try {
      val tempFile = File(exportsDir, item.originalName)
      FileOutputStream(tempFile).use { it.write(bytes) }
      tempFile
    } catch (e: Exception) {
      null
    }
  }

  fun moveFileToTrash(id: String): Boolean {
    val all = getAllFiles(includeTrash = true).toMutableList()
    val index = all.indexOfFirst { it.id == id }
    if (index >= 0) {
      all[index] = all[index].copy(isTrash = true, deletedAt = System.currentTimeMillis())
      return saveFilesMeta(all)
    }
    return false
  }

  fun restoreFileFromTrash(id: String): Boolean {
    val all = getAllFiles(includeTrash = true).toMutableList()
    val index = all.indexOfFirst { it.id == id }
    if (index >= 0) {
      all[index] = all[index].copy(isTrash = false, deletedAt = null)
      return saveFilesMeta(all)
    }
    return false
  }

  fun deleteFilePermanently(id: String): Boolean {
    val all = getAllFiles(includeTrash = true).toMutableList()
    val item = all.firstOrNull { it.id == id } ?: return false
    val encFile = File(vaultDir, item.storageFileName)
    CryptoManager.secureShredFile(encFile)
    all.removeAll { it.id == id }
    return saveFilesMeta(all)
  }

  fun toggleFileFavorite(id: String): Boolean {
    val all = getAllFiles(includeTrash = true).toMutableList()
    val index = all.indexOfFirst { it.id == id }
    if (index >= 0) {
      all[index] = all[index].copy(isFavorite = !all[index].isFavorite)
      return saveFilesMeta(all)
    }
    return false
  }

  // --- TRASH COLLECTION & RECOVERY ---

  fun getTrashCollection(): TrashCollection {
    val entries = getItems(includeTrash = true).filter { it.isTrash }
    val notes = getNotes(includeTrash = true).filter { it.isTrash }
    val files = getAllFiles(includeTrash = true).filter { it.isTrash }
    return TrashCollection(entries, notes, files)
  }

  fun emptyTrash(): Boolean {
    // Delete files permanently with shredding
    val trashFiles = getAllFiles(includeTrash = true).filter { it.isTrash }
    for (f in trashFiles) {
      val file = File(vaultDir, f.storageFileName)
      CryptoManager.secureShredFile(file)
    }

    val remainingFiles = getAllFiles(includeTrash = true).filter { !it.isTrash }
    saveFilesMeta(remainingFiles)

    val remainingNotes = getNotes(includeTrash = true).filter { !it.isTrash }
    saveNotes(remainingNotes)

    val remainingEntries = getItems(includeTrash = true).filter { !it.isTrash }
    saveItems(remainingEntries)
    return true
  }

  // --- STORAGE STATS ---

  fun calculateStorageStats(): VaultStorageStats {
    val files = getAllFiles(includeTrash = true)
    val photosBytes = files.filter { it.category == FileCategory.PHOTO }.sumOf { it.sizeBytes }
    val videosBytes = files.filter { it.category == FileCategory.VIDEO }.sumOf { it.sizeBytes }
    val docsBytes = files.filter { it.category == FileCategory.DOCUMENT }.sumOf { it.sizeBytes }

    val notes = getNotes(includeTrash = true)
    val notesBytes = notes.sumOf { (it.title.length + it.content.length).toLong() * 2 }

    val items = getItems(includeTrash = true)
    val credentialsBytes = items.sumOf { (it.title.length + it.payloadJson.length).toLong() * 2 }

    val total = photosBytes + videosBytes + docsBytes + notesBytes + credentialsBytes

    return VaultStorageStats(
      photosBytes = photosBytes,
      videosBytes = videosBytes,
      documentsBytes = docsBytes,
      notesBytes = notesBytes,
      credentialsBytes = credentialsBytes,
      totalVaultBytes = total
    )
  }

  // --- AUDIT & BACKUP ---

  fun calculateAudit(): SecurityAuditSummary {
    val items = getItems(includeTrash = false)
    val passwords = items.filter { it.type == VaultCategory.PASSWORD }
    var weakCount = 0
    val passwordValues = mutableListOf<String>()

    for (p in passwords) {
      try {
        val json = JSONObject(p.payloadJson)
        val pass = json.optString("password", "")
        if (pass.isNotEmpty()) {
          passwordValues.add(pass)
          val strength = CryptoManager.evaluatePasswordStrength(pass)
          if (strength.level == PasswordStrengthLevel.WEAK || strength.level == PasswordStrengthLevel.FAIR) {
            weakCount++
          }
        }
      } catch (e: Exception) {
        // ignore
      }
    }

    val duplicatesCount = passwordValues.size - passwordValues.distinct().size
    val total = passwords.size
    val score = if (total == 0) 100 else {
      val deducted = (weakCount * 25 + duplicatesCount * 30).coerceAtMost(90)
      (100 - deducted).coerceAtLeast(10)
    }

    return SecurityAuditSummary(
      totalItems = items.size,
      totalPasswords = total,
      weakPasswordsCount = weakCount,
      reusedPasswordsCount = duplicatesCount,
      securityScorePercent = score
    )
  }

  fun exportVaultEncrypted(): String? {
    val itemsJson = prefs.getString(KEY_ITEMS_JSON, null)
    val itemsIv = prefs.getString(KEY_ITEMS_IV, null)
    val notesJson = prefs.getString(KEY_NOTES_JSON, null)
    val notesIv = prefs.getString(KEY_NOTES_IV, null)
    val filesMetaJson = prefs.getString(KEY_FILES_META_JSON, null)
    val filesMetaIv = prefs.getString(KEY_FILES_META_IV, null)

    if (itemsJson != null && itemsIv != null) {
      val obj = JSONObject().apply {
        put("version", "2.0.0")
        put("app", "SafeBox")
        put("exportTime", System.currentTimeMillis())
        put("itemsIv", itemsIv)
        put("itemsData", itemsJson)
        if (notesJson != null && notesIv != null) {
          put("notesIv", notesIv)
          put("notesData", notesJson)
        }
        if (filesMetaJson != null && filesMetaIv != null) {
          put("filesMetaIv", filesMetaIv)
          put("filesMetaData", filesMetaJson)
        }
      }
      return obj.toString()
    }
    return null
  }

  fun restoreVaultFromEncryptedJson(backupJsonString: String): Boolean {
    val key = activeKey ?: return false
    return try {
      val obj = JSONObject(backupJsonString)
      val itemsIv = obj.getString("itemsIv")
      val itemsData = obj.getString("itemsData")
      // Verify decryption works with current key
      val testDecrypted = CryptoManager.decrypt(itemsData, itemsIv, key)
      if (testDecrypted.isEmpty()) return false

      prefs.edit()
        .putString(KEY_ITEMS_JSON, itemsData)
        .putString(KEY_ITEMS_IV, itemsIv)
        .apply()

      if (obj.has("notesData") && obj.has("notesIv")) {
        prefs.edit()
          .putString(KEY_NOTES_JSON, obj.getString("notesData"))
          .putString(KEY_NOTES_IV, obj.getString("notesIv"))
          .apply()
      }

      if (obj.has("filesMetaData") && obj.has("filesMetaIv")) {
        prefs.edit()
          .putString(KEY_FILES_META_JSON, obj.getString("filesMetaData"))
          .putString(KEY_FILES_META_IV, obj.getString("filesMetaIv"))
          .apply()
      }
      true
    } catch (e: Exception) {
      false
    }
  }
}
