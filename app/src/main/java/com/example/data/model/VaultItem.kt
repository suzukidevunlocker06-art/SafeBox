package com.example.data.model

import org.json.JSONObject

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
  val payloadJson: String = "{}"
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
