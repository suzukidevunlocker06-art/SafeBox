package com.example.data.model

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VaultCategory(val displayName: String) {
  PASSWORD("Contraseñas"),
  NOTE("Notas Seguras"),
  CARD("Tarjetas"),
  IDENTITY("Identidad")
}

data class VaultEntry(
  val id: String,
  val type: VaultCategory,
  val title: String,
  val folder: String = "General",
  val isFavorite: Boolean = false,
  val updatedAt: Long = System.currentTimeMillis(),
  val payloadJson: String = "{}",
  val isTrash: Boolean = false,
  val deletedAt: Long? = null
) {
  fun getPreviewText(): String {
    return try {
      val json = JSONObject(payloadJson)
      when (type) {
        VaultCategory.PASSWORD -> json.optString("username", "Sin usuario")
        VaultCategory.NOTE -> {
          val content = json.optString("content", "")
          if (content.length > 40) content.take(37) + "..." else content
        }
        VaultCategory.CARD -> {
          val num = json.optString("cardNumber", "****")
          val last4 = if (num.length >= 4) num.takeLast(4) else num
          "Terminada en •••• $last4"
        }
        VaultCategory.IDENTITY -> {
          val idType = json.optString("idType", "Documento")
          val idNum = json.optString("idNumber", "")
          "$idType: $idNum"
        }
      }
    } catch (e: Exception) {
      "Protegido"
    }
  }

  fun getSecondaryText(): String {
    return try {
      val json = JSONObject(payloadJson)
      when (type) {
        VaultCategory.PASSWORD -> json.optString("url", "")
        VaultCategory.NOTE -> json.optString("tag", "Privado")
        VaultCategory.CARD -> json.optString("bank", "Banco")
        VaultCategory.IDENTITY -> json.optString("fullName", "")
      }
    } catch (e: Exception) {
      ""
    }
  }
}

enum class FileCategory(val displayName: String) {
  PHOTO("Fotos"),
  VIDEO("Videos"),
  DOCUMENT("Documentos")
}

data class VaultFileItem(
  val id: String,
  val category: FileCategory,
  val originalName: String,
  val extension: String,
  val mimeType: String,
  val sizeBytes: Long,
  val sha256: String,
  val storageFileName: String,
  val ivBase64: String,
  val isFavorite: Boolean = false,
  val isTrash: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val deletedAt: Long? = null,
  val tag: String = "General",
  val extraInfo: String = ""
) {
  fun getFormattedSize(): String {
    val kb = sizeBytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
      gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
      mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
      kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
      else -> "$sizeBytes B"
    }
  }

  fun getFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(createdAt))
  }
}

data class VaultNoteItem(
  val id: String,
  val title: String,
  val content: String,
  val category: String = "General",
  val isFavorite: Boolean = false,
  val isTrash: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val deletedAt: Long? = null
) {
  fun wordCount(): Int {
    if (content.isBlank()) return 0
    return content.trim().split("\\s+".toRegex()).size
  }

  fun characterCount(): Int = content.length

  fun getFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(updatedAt))
  }
}

data class VaultSettings(
  val biometricsEnabled: Boolean = true,
  val autoLockSeconds: Int = 60,
  val lockOnBackground: Boolean = true,
  val screenCaptureProtection: Boolean = true,
  val themeMode: String = "DARK",
  val failedAttempts: Int = 0,
  val lockoutUntil: Long = 0L
)

data class PasswordPayload(
  val username: String,
  val password: String,
  val url: String = "",
  val notes: String = ""
)

data class NotePayload(
  val content: String,
  val tag: String = "General"
)

data class CardPayload(
  val cardholderName: String,
  val cardNumber: String,
  val expiryMonth: String,
  val expiryYear: String,
  val cvv: String,
  val bank: String = ""
)

data class IdentityPayload(
  val idType: String,
  val idNumber: String,
  val fullName: String,
  val country: String = ""
)
