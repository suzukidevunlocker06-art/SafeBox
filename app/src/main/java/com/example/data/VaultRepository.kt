package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.data.model.VaultCategory
import com.example.data.model.VaultEntry
import com.example.security.CryptoManager
import com.example.security.PasswordStrengthLevel
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.crypto.SecretKey

data class SecurityAuditSummary(
  val totalItems: Int,
  val totalPasswords: Int,
  val weakPasswordsCount: Int,
  val reusedPasswordsCount: Int,
  val securityScorePercent: Int
)

class VaultRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("safebox_vault_prefs", Context.MODE_PRIVATE)

  private var activeKey: SecretKey? = null

  companion object {
    private const val KEY_SETUP_COMPLETED = "setup_completed"
    private const val KEY_SALT = "master_salt"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_ITEMS_JSON = "encrypted_items_data"
    private const val KEY_IV = "encrypted_items_iv"
  }

  fun isSetup(): Boolean {
    return prefs.getBoolean(KEY_SETUP_COMPLETED, false)
  }

  fun isUnlocked(): Boolean {
    return activeKey != null
  }

  fun lock() {
    activeKey = null
  }

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

    // Seed initial vault items for the user
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
        type = VaultCategory.NOTE,
        title = "Claves de Respaldo Wi-Fi",
        folder = "Hogar",
        isFavorite = true,
        payloadJson = JSONObject().apply {
          put("content", "SSID: FibraOptica_5G\nPassword: Safe_Wifi#9982\nRouter IP: 192.168.1.1")
          put("tag", "Red")
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
    return true
  }

  fun unlock(pin: String): Boolean {
    val saltBase64 = prefs.getString(KEY_SALT, null) ?: return false
    val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false

    val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
    val inputHash = CryptoManager.hashPin(pin, salt)

    if (inputHash == storedHash) {
      activeKey = CryptoManager.deriveKey(pin, salt)
      return true
    }
    return false
  }

  fun unlockBypassForDemo(): Boolean {
    // Allows testing/demo unlock when needed
    val saltBase64 = prefs.getString(KEY_SALT, null)
    if (saltBase64 != null) {
      val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
      activeKey = CryptoManager.deriveKey("1234", salt)
      return true
    }
    return false
  }

  fun getItems(): List<VaultEntry> {
    val key = activeKey ?: return emptyList()
    val encryptedData = prefs.getString(KEY_ITEMS_JSON, null) ?: return emptyList()
    val iv = prefs.getString(KEY_IV, null) ?: return emptyList()

    return try {
      val decryptedJson = CryptoManager.decrypt(encryptedData, iv, key)
      val jsonArray = JSONArray(decryptedJson)
      val list = mutableListOf<VaultEntry>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          VaultEntry(
            id = obj.getString("id"),
            type = VaultCategory.valueOf(obj.getString("type")),
            title = obj.getString("title"),
            folder = obj.optString("folder", "General"),
            isFavorite = obj.optBoolean("isFavorite", false),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
            payloadJson = obj.optString("payloadJson", "{}")
          )
        )
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
        }
        jsonArray.put(obj)
      }
      val (encryptedBase64, ivBase64) = CryptoManager.encrypt(jsonArray.toString(), key)
      prefs.edit()
        .putString(KEY_ITEMS_JSON, encryptedBase64)
        .putString(KEY_IV, ivBase64)
        .apply()
      true
    } catch (e: Exception) {
      false
    }
  }

  fun addItem(entry: VaultEntry): Boolean {
    val current = getItems().toMutableList()
    current.add(0, entry)
    return saveItems(current)
  }

  fun updateItem(entry: VaultEntry): Boolean {
    val current = getItems().toMutableList()
    val index = current.indexOfFirst { it.id == entry.id }
    if (index >= 0) {
      current[index] = entry.copy(updatedAt = System.currentTimeMillis())
      return saveItems(current)
    }
    return false
  }

  fun deleteItem(id: String): Boolean {
    val current = getItems().filter { it.id != id }
    return saveItems(current)
  }

  fun toggleFavorite(id: String): Boolean {
    val current = getItems().toMutableList()
    val index = current.indexOfFirst { it.id == id }
    if (index >= 0) {
      val item = current[index]
      current[index] = item.copy(isFavorite = !item.isFavorite)
      return saveItems(current)
    }
    return false
  }

  fun calculateAudit(): SecurityAuditSummary {
    val items = getItems()
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
    val iv = prefs.getString(KEY_IV, null)
    if (itemsJson != null && iv != null) {
      val obj = JSONObject().apply {
        put("version", "1.0.0")
        put("app", "SafeBox")
        put("exportTime", System.currentTimeMillis())
        put("iv", iv)
        put("data", itemsJson)
      }
      return obj.toString()
    }
    return null
  }
}
