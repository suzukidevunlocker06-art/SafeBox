package com.example

import com.example.security.CryptoManager
import com.example.security.PasswordStrengthLevel
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CryptoManagerTest {

  @Test
  fun testPasswordGeneratorLengthAndComplexity() {
    val pwd16 = CryptoManager.generateSecurePassword(16, true, true, true, true)
    assertEquals(16, pwd16.length)

    val pwd24 = CryptoManager.generateSecurePassword(24, true, true, true, true)
    assertEquals(24, pwd24.length)
  }

  @Test
  fun testPasswordStrengthEvaluation() {
    val weak = CryptoManager.evaluatePasswordStrength("1234")
    assertEquals(PasswordStrengthLevel.WEAK, weak.level)

    val strong = CryptoManager.evaluatePasswordStrength("X9#mK\$pL2@vQ7!zR")
    assertTrue(strong.level == PasswordStrengthLevel.GOOD || strong.level == PasswordStrengthLevel.EXCELLENT)
  }

  @Test
  fun testPinHashing() {
    val salt = CryptoManager.generateSalt()
    val hash1 = CryptoManager.hashPin("1234", salt)
    val hash2 = CryptoManager.hashPin("1234", salt)
    val hashWrong = CryptoManager.hashPin("9999", salt)

    assertEquals(hash1, hash2)
    assertNotEquals(hash1, hashWrong)
  }

  @Test
  fun testEncryptionAndDecryptionRoundtrip() {
    val salt = CryptoManager.generateSalt()
    val key = CryptoManager.deriveKey("MySecretPin", salt)
    val originalText = "Informacion Ultra Secreta de SafeBox 2026"

    val (encryptedBase64, ivBase64) = CryptoManager.encrypt(originalText, key)
    assertNotNull(encryptedBase64)
    assertNotNull(ivBase64)
    assertNotEquals(originalText, encryptedBase64)

    val decrypted = CryptoManager.decrypt(encryptedBase64, ivBase64, key)
    assertEquals(originalText, decrypted)
  }

  @Test
  fun testByteEncryptionAndDecryptionRoundtrip() {
    val salt = CryptoManager.generateSalt()
    val key = CryptoManager.deriveKey("MasterPIN2026", salt)
    val rawBytes = "SafeBox Binary Encrypted Data Payload".toByteArray(Charsets.UTF_8)

    val (cipherBytes, ivBytes) = CryptoManager.encryptBytes(rawBytes, key)
    assertNotNull(cipherBytes)
    assertNotNull(ivBytes)
    assertFalse(rawBytes.contentEquals(cipherBytes))

    val decryptedBytes = CryptoManager.decryptBytes(cipherBytes, ivBytes, key)
    assertArrayEquals(rawBytes, decryptedBytes)
  }

  @Test
  fun testSha256Checksum() {
    val data = "SafeBox Integrity Check".toByteArray(Charsets.UTF_8)
    val hash1 = CryptoManager.calculateSha256(data)
    val hash2 = CryptoManager.calculateSha256(data)
    val diffHash = CryptoManager.calculateSha256("Different Content".toByteArray(Charsets.UTF_8))

    assertEquals(hash1, hash2)
    assertEquals(64, hash1.length) // 64 hex characters
    assertNotEquals(hash1, diffHash)
  }

  @Test
  fun testFileShredding() {
    val tempFile = File.createTempFile("safebox_test_shred", ".bin")
    tempFile.writeBytes("Super sensitive confidential data to be shredded".toByteArray(Charsets.UTF_8))
    assertTrue(tempFile.exists())

    CryptoManager.shredFile(tempFile)
    assertFalse(tempFile.exists())
  }
}
