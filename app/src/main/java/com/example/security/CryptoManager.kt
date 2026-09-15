package com.example.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

enum class PasswordStrengthLevel(val label: String, val score: Int) {
  WEAK("Débil", 1),
  FAIR("Media", 2),
  GOOD("Fuerte", 3),
  EXCELLENT("Excelente", 4)
}

data class PasswordStrength(
  val level: PasswordStrengthLevel,
  val feedback: String
)

object CryptoManager {
  private const val ITERATIONS = 10000
  private const val KEY_LENGTH = 256
  private const val ALGORITHM = "AES"
  private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
  private val random = SecureRandom()

  fun generateSalt(): ByteArray {
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt
  }

  fun deriveKey(pinOrPassword: String, salt: ByteArray): SecretKey {
    val spec = PBEKeySpec(pinOrPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val keyBytes = factory.generateSecret(spec).encoded
    return SecretKeySpec(keyBytes, ALGORITHM)
  }

  fun hashPin(pin: String, salt: ByteArray): String {
    val key = deriveKey(pin, salt)
    return Base64.encodeToString(key.encoded, Base64.NO_WRAP)
  }

  fun encrypt(plainText: String, secretKey: SecretKey): Pair<String, String> {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    val iv = ByteArray(16)
    random.nextBytes(iv)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
    val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
    val encryptedBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
    val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
    return Pair(encryptedBase64, ivBase64)
  }

  fun decrypt(encryptedBase64: String, ivBase64: String, secretKey: SecretKey): String {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
    val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return String(decryptedBytes, Charsets.UTF_8)
  }

  fun generateSecurePassword(
    length: Int = 16,
    includeUpper: Boolean = true,
    includeLower: Boolean = true,
    includeDigits: Boolean = true,
    includeSymbols: Boolean = true
  ): String {
    val uppers = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    val lowers = "abcdefghijkmnpqrstuvwxyz"
    val digits = "23456789"
    val symbols = "!@#$%^&*-_+=?"

    val pool = StringBuilder()
    val result = StringBuilder()

    if (includeUpper) {
      pool.append(uppers)
      result.append(uppers[random.nextInt(uppers.length)])
    }
    if (includeLower) {
      pool.append(lowers)
      result.append(lowers[random.nextInt(lowers.length)])
    }
    if (includeDigits) {
      pool.append(digits)
      result.append(digits[random.nextInt(digits.length)])
    }
    if (includeSymbols) {
      pool.append(symbols)
      result.append(symbols[random.nextInt(symbols.length)])
    }

    if (pool.isEmpty()) {
      pool.append(lowers).append(digits)
    }

    val remaining = (length - result.length).coerceAtLeast(0)
    for (i in 0 until remaining) {
      result.append(pool[random.nextInt(pool.length)])
    }

    // Shuffle characters
    val chars = result.toString().toCharArray()
    for (i in chars.indices.reversed()) {
      val j = random.nextInt(i + 1)
      val temp = chars[i]
      chars[i] = chars[j]
      chars[j] = temp
    }
    return String(chars)
  }

  fun evaluatePasswordStrength(password: String): PasswordStrength {
    if (password.length < 6) {
      return PasswordStrength(PasswordStrengthLevel.WEAK, "Demasiado corta (mínimo 6 caracteres)")
    }

    var score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
      score <= 2 -> PasswordStrength(PasswordStrengthLevel.WEAK, "Fácil de adivinar")
      score == 3 -> PasswordStrength(PasswordStrengthLevel.FAIR, "Seguridad aceptable")
      score == 4 -> PasswordStrength(PasswordStrengthLevel.GOOD, "Contraseña robusta")
      else -> PasswordStrength(PasswordStrengthLevel.EXCELLENT, "Máxima seguridad criptográfica")
    }
  }
}
