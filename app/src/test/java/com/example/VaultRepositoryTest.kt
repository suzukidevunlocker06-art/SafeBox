package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ImportResult
import com.example.data.VaultRepository
import com.example.data.model.FileCategory
import com.example.data.model.VaultCategory
import com.example.data.model.VaultEntry
import com.example.data.model.VaultNoteItem
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VaultRepositoryTest {

  private lateinit var context: Context
  private lateinit var repository: VaultRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    val prefs = context.getSharedPreferences("safebox_vault_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()
    val vaultDir = File(context.filesDir, "vault_files")
    if (vaultDir.exists()) vaultDir.deleteRecursively()

    repository = VaultRepository(context)
    repository.setupVault("5678")

    // Clear seed data to have clean slate for precise counting in tests
    repository.saveItems(emptyList())
    repository.saveNotes(emptyList())
  }

  @Test
  fun testUnlockWithCorrectAndWrongPin() {
    assertTrue(repository.isUnlocked())

    repository.lock()
    assertFalse(repository.isUnlocked())

    val failed = repository.unlock("0000")
    assertFalse(failed)
    assertFalse(repository.isUnlocked())

    val success = repository.unlock("5678")
    assertTrue(success)
    assertTrue(repository.isUnlocked())
  }

  @Test
  fun testAddAndRetrieveVaultEntry() {
    val payload = JSONObject().apply {
      put("username", "testuser@safebox.local")
      put("password", "SuperSecretPwd123!")
    }.toString()

    val entry = VaultEntry(
      id = UUID.randomUUID().toString(),
      type = VaultCategory.PASSWORD,
      title = "Cuenta Bancaria",
      payloadJson = payload,
      folder = "Finanzas"
    )

    repository.addItem(entry)

    val items = repository.getItems(includeTrash = false)
    assertEquals(1, items.size)
    assertEquals("Cuenta Bancaria", items[0].title)
    assertEquals(payload, items[0].payloadJson)

    // Move to trash
    repository.moveItemToTrash(entry.id)
    val afterTrash = repository.getItems(includeTrash = false)
    assertEquals(0, afterTrash.size)

    val trash = repository.getTrashCollection()
    assertEquals(1, trash.entries.size)

    // Restore from trash
    repository.restoreItemFromTrash(entry.id)
    assertEquals(1, repository.getItems(includeTrash = false).size)

    // Permanently delete
    repository.deleteItemPermanently(entry.id)
    assertEquals(0, repository.getItems(includeTrash = true).size)
  }

  @Test
  fun testPrivateNotesLifecycle() {
    val note = VaultNoteItem(
      id = UUID.randomUUID().toString(),
      title = "Notas de Seguridad",
      content = "Semilla de recuperación mnemónica ultra secreta",
      category = "Claves"
    )

    repository.addNote(note)
    val notes = repository.getNotes(includeTrash = false)
    assertEquals(1, notes.size)
    assertEquals("Notas de Seguridad", notes[0].title)
    assertEquals("Semilla de recuperación mnemónica ultra secreta", notes[0].content)

    // Trash and restore
    repository.moveNoteToTrash(note.id)
    assertEquals(0, repository.getNotes(includeTrash = false).size)
    assertEquals(1, repository.getTrashCollection().notes.size)

    repository.restoreNoteFromTrash(note.id)
    assertEquals(1, repository.getNotes(includeTrash = false).size)

    // Shred permanently
    repository.deleteNotePermanently(note.id)
    assertEquals(0, repository.getNotes(includeTrash = true).size)
  }

  @Test
  fun testEncryptedFileStorageAndDecryption() {
    val rawBytes = "Documento Confidencial SafeBox 2.0 - Contrato Privado".toByteArray(Charsets.UTF_8)
    val result = repository.importFileFromBytes(
      bytes = rawBytes,
      category = FileCategory.DOCUMENT,
      originalName = "contrato.txt",
      mimeType = "text/plain"
    )

    assertTrue(result is ImportResult.Success)
    val fileItem = (result as ImportResult.Success).item
    assertNotNull(fileItem)
    assertEquals("contrato.txt", fileItem.originalName)
    assertEquals(FileCategory.DOCUMENT, fileItem.category)

    // Verify raw file on disk is encrypted (does NOT match original bytes)
    val vaultDir = File(context.filesDir, "vault_files")
    val encryptedDiskFile = File(vaultDir, fileItem.storageFileName)
    assertTrue(encryptedDiskFile.exists())
    assertFalse(rawBytes.contentEquals(encryptedDiskFile.readBytes()))

    // Decrypt and verify exact match
    val decrypted = repository.decryptFileBytes(fileItem)
    assertNotNull(decrypted)
    assertTrue(rawBytes.contentEquals(decrypted))

    // Move to trash
    repository.moveFileToTrash(fileItem.id)
    assertEquals(0, repository.getAllFiles(includeTrash = false).size)
    assertEquals(1, repository.getTrashCollection().files.size)

    // Delete permanently (shreds disk file)
    repository.deleteFilePermanently(fileItem.id)
    assertEquals(0, repository.getAllFiles(includeTrash = true).size)
    assertFalse(encryptedDiskFile.exists())
  }

  @Test
  fun testPinChangeAndReEncryption() {
    val note = VaultNoteItem(
      id = UUID.randomUUID().toString(),
      title = "Nota Secreta",
      content = "Contenido protegido con PIN inicial"
    )
    repository.addNote(note)

    val changed = repository.changePin("5678", "9999")
    assertTrue(changed)

    repository.lock()
    assertFalse(repository.unlock("5678"))
    assertTrue(repository.unlock("9999"))

    val notesAfter = repository.getNotes(includeTrash = false)
    assertEquals(1, notesAfter.size)
    assertEquals("Contenido protegido con PIN inicial", notesAfter[0].content)
  }

  @Test
  fun testBackupExportAndRestore() {
    val note = VaultNoteItem(
      id = UUID.randomUUID().toString(),
      title = "Nota Para Backup",
      content = "Datos a respaldar"
    )
    repository.addNote(note)

    val backup = repository.exportVaultEncrypted()
    assertNotNull(backup)
    assertTrue(backup != null && backup.contains("SafeBox"))

    // Clear notes from vault (simulating data deletion)
    repository.saveNotes(emptyList())
    assertEquals(0, repository.getNotes(includeTrash = false).size)

    val restored = repository.restoreVaultFromEncryptedJson(backup!!)
    assertTrue(restored)

    val notesRestored = repository.getNotes(includeTrash = false)
    assertEquals(1, notesRestored.size)
    assertEquals("Nota Para Backup", notesRestored[0].title)
  }
}
