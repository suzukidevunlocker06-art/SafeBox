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

  fun encryptBytes(data: ByteArray, secretKey: SecretKey): Pair<ByteArray, ByteArray> {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    val iv = ByteArray(16)
    random.nextBytes(iv)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
    val encrypted = cipher.doFinal(data)
    return Pair(encrypted, iv)
  }

  fun decryptBytes(encryptedData: ByteArray, iv: ByteArray, secretKey: SecretKey): ByteArray {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
    return cipher.doFinal(encryptedData)
  }

  fun calculateSha256(data: ByteArray): String {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(data)
    return hash.joinToString("") { "%02x".format(it) }
  }

  fun calculateSha256(inputStream: java.io.InputStream): String {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(8192)
    var read: Int
    while (inputStream.read(buffer).also { read = it } != -1) {
      digest.update(buffer, 0, read)
    }
    val hash = digest.digest()
    return hash.joinToString("") { "%02x".format(it) }
  }

  fun secureShredFile(file: java.io.File): Boolean {
    return try {
      if (file.exists()) {
        val length = file.length()
        val randomBytes = ByteArray(4096)
        java.io.RandomAccessFile(file, "rws").use { raf ->
          var written = 0L
          while (written < length) {
            random.nextBytes(randomBytes)
            val toWrite = minOf(randomBytes.size.toLong(), length - written).toInt()
            raf.write(randomBytes, 0, toWrite)
            written += toWrite
          }
        }
        file.delete()
      } else {
        true
      }
    } catch (e: Exception) {
      file.delete()
    }
  }

  // Android KeyStore Hardware-backed wrapper
  object KeyStoreHelper {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val MASTER_ALIAS = "SafeBoxMasterKeyAlias"
    private const val GCM_TRANSFORMATION = "AES/GCM/NoPadding"

    private fun getKeystore(): java.security.KeyStore {
      val ks = java.security.KeyStore.getInstance(KEYSTORE_PROVIDER)
      ks.load(null)
      return ks
    }

    fun getOrCreateKey(): SecretKey? {
      return try {
        val ks = getKeystore()
        if (ks.containsAlias(MASTER_ALIAS)) {
          val entry = ks.getEntry(MASTER_ALIAS, null) as? java.security.KeyStore.SecretKeyEntry
          entry?.secretKey
        } else {
          val keyGenerator = javax.crypto.KeyGenerator.getInstance(
            android.security.keystore.KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
          )
          val spec = android.security.keystore.KeyGenParameterSpec.Builder(
            MASTER_ALIAS,
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
          )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
          keyGenerator.init(spec)
          keyGenerator.generateKey()
        }
      } catch (e: Exception) {
        null
      }
    }

    fun wrapMasterKey(masterKeyBytes: ByteArray): Pair<String, String>? {
      val key = getOrCreateKey() ?: return null
      return try {
        val cipher = Cipher.getInstance(GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(masterKeyBytes)
        Pair(
          Base64.encodeToString(encrypted, Base64.NO_WRAP),
          Base64.encodeToString(iv, Base64.NO_WRAP)
        )
      } catch (e: Exception) {
        null
      }
    }

    fun unwrapMasterKey(encryptedBase64: String, ivBase64: String): ByteArray? {
      val key = getOrCreateKey() ?: return null
      return try {
        val cipher = Cipher.getInstance(GCM_TRANSFORMATION)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        val encrypted = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        val spec = javax.crypto.spec.GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        cipher.doFinal(encrypted)
      } catch (e: Exception) {
        null
      }
    }
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

  fun shredFile(file: java.io.File): Boolean {
    return try {
      if (file.exists()) {
        val length = file.length()
        if (length > 0) {
          java.io.RandomAccessFile(file, "rws").use { raf ->
            val buffer = ByteArray(4096)
            // Pass 1: Cryptographic zeros
            var written = 0L
            while (written < length) {
              val toWrite = minOf(buffer.size.toLong(), length - written).toInt()
              raf.write(buffer, 0, toWrite)
              written += toWrite
            }
            // Pass 2: Cryptographic random bytes
            raf.seek(0)
            written = 0L
            while (written < length) {
              random.nextBytes(buffer)
              val toWrite = minOf(buffer.size.toLong(), length - written).toInt()
              raf.write(buffer, 0, toWrite)
              written += toWrite
            }
          }
        }
        file.delete()
      } else {
        false
      }
    } catch (e: Exception) {
      file.delete()
    }
  }
}
