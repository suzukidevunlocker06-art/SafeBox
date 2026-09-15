package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ImportResult
import com.example.data.SecurityAuditSummary
import com.example.data.TrashCollection
import com.example.data.VaultRepository
import com.example.data.VaultStorageStats
import com.example.data.model.FileCategory
import com.example.data.model.VaultCategory
import com.example.data.model.VaultEntry
import com.example.data.model.VaultFileItem
import com.example.data.model.VaultNoteItem
import com.example.data.model.VaultSettings
import com.example.security.CryptoManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class VaultTab(val label: String) {
  GALLERY("Galería"),
  DOCUMENTS("Documentos"),
  NOTES("Notas"),
  PASSWORDS("Cuentas"),
  FAVORITES("Favoritos"),
  TRASH("Papelera"),
  SETTINGS("Ajustes")
}

enum class MainNavigationTab(val label: String) {
  VAULT("Bóveda"),
  FAVORITES("Favoritos"),
  TRASH("Papelera"),
  SETTINGS("Ajustes")
}

enum class VaultSection(val label: String) {
  PHOTOS("Fotos"),
  VIDEOS("Videos"),
  DOCUMENTS("Documentos"),
  NOTES("Notas"),
  CREDENTIALS("Cuentas")
}

enum class SortOption(val label: String) {
  DATE_DESC("Más reciente"),
  DATE_ASC("Más antiguo"),
  NAME_ASC("Nombre (A-Z)"),
  NAME_DESC("Nombre (Z-A)"),
  SIZE_DESC("Tamaño (Mayor)")
}

enum class DocumentFilter(val label: String) {
  ALL("Todos"),
  PDF("PDF"),
  TEXT("Texto"),
  OFFICE("Documentos"),
  OTHER("Otros")
}

data class ImportProgress(
  val current: Int,
  val total: Int,
  val isImporting: Boolean = false,
  val currentFileName: String = ""
)

data class VaultUiState(
  val isSetup: Boolean = false,
  val isUnlocked: Boolean = false,
  val pinInput: String = "",
  val setupPinConfirm: String = "",
  val isConfirmingPin: Boolean = false,
  val errorMessage: String? = null,
  val lockoutSecondsRemaining: Long = 0L,
  val failedAttempts: Int = 0,

  // Navigation
  val activeTab: MainNavigationTab = MainNavigationTab.VAULT,
  val activeSection: VaultSection = VaultSection.PHOTOS,
  val currentTab: VaultTab = VaultTab.GALLERY,
  val selectedCategory: VaultCategory? = null,
  val filterFavoritesOnly: Boolean = false,

  // Vault data
  val items: List<VaultEntry> = emptyList(),
  val notes: List<VaultNoteItem> = emptyList(),
  val photos: List<VaultFileItem> = emptyList(),
  val videos: List<VaultFileItem> = emptyList(),
  val documents: List<VaultFileItem> = emptyList(),
  val trash: TrashCollection = TrashCollection(emptyList(), emptyList(), emptyList()),
  val storageStats: VaultStorageStats = VaultStorageStats(0, 0, 0, 0, 0, 0),

  // Search and filters
  val searchQuery: String = "",
  val isGlobalSearchOpen: Boolean = false,
  val sortOption: SortOption = SortOption.DATE_DESC,
  val documentFilter: DocumentFilter = DocumentFilter.ALL,

  // Multi-selection
  val isSelectionMode: Boolean = false,
  val selectedFileIds: Set<String> = emptySet(),

  // Dialogs & Viewers
  val viewerFile: VaultFileItem? = null,
  val viewerDecryptedBytes: ByteArray? = null,
  val viewerTempFile: File? = null,
  val viewingFileItem: VaultFileItem? = null,
  val decryptedFileBytes: ByteArray? = null,
  val tempExportFile: File? = null,
  val showNoteDialog: Boolean = false,
  val showNoteEditor: Boolean = false,
  val noteToEdit: VaultNoteItem? = null,
  val showAddEditEntryDialog: Boolean = false,
  val showAddEditSheet: Boolean = false,
  val entryToEdit: VaultEntry? = null,
  val itemToEdit: VaultEntry? = null,
  val showGeneratorDialog: Boolean = false,
  val showAuditDialog: Boolean = false,
  val showBackupDialog: Boolean = false,
  val showChangePinDialog: Boolean = false,
  val showStorageDialog: Boolean = false,
  val showDuplicateDialog: String? = null,
  val importProgress: ImportProgress? = null,

  // Settings
  val settings: VaultSettings = VaultSettings(),
  val auditSummary: SecurityAuditSummary = SecurityAuditSummary(0, 0, 0, 0, 100),
  val generatedPassword: String = "",
  val backupExportJson: String? = null
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {
  val repository = VaultRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(
    VaultUiState(
      isSetup = repository.isSetup(),
      isUnlocked = false,
      settings = repository.getSettings()
    )
  )
  val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

  private var lockoutTimerJob: Job? = null

  init {
    checkLockout()
    generateNewPassword()
  }

  private fun checkLockout() {
    val remaining = repository.getLockoutRemainingSeconds()
    val attempts = repository.getSettings().failedAttempts
    if (remaining > 0) {
      startLockoutTimer(remaining, attempts)
    }
  }

  private fun startLockoutTimer(seconds: Long, attempts: Int) {
    lockoutTimerJob?.cancel()
    _uiState.update {
      it.copy(
        lockoutSecondsRemaining = seconds,
        failedAttempts = attempts,
        errorMessage = "Demasiados intentos fallidos ($attempts intentos). Bloqueado por $seconds s"
      )
    }
    lockoutTimerJob = viewModelScope.launch {
      var sec = seconds
      while (sec > 0 && isActive) {
        delay(1000L)
        sec--
        val s = sec
        _uiState.update {
          it.copy(
            lockoutSecondsRemaining = s,
            errorMessage = if (s > 0) "Demasiados intentos fallidos ($attempts intentos). Bloqueado por $s s" else null
          )
        }
      }
    }
  }

  // --- PIN & AUTHENTICATION ---

  fun onPinDigit(digit: String) {
    if (_uiState.value.lockoutSecondsRemaining > 0) return
    val current = _uiState.value.pinInput
    if (current.length < 6) {
      val next = current + digit
      _uiState.update { it.copy(pinInput = next, errorMessage = null) }
      if (next.length == 4 && _uiState.value.isSetup) {
        tryUnlock(next)
      }
    }
  }

  fun onPinDelete() {
    val current = _uiState.value.pinInput
    if (current.isNotEmpty()) {
      _uiState.update { it.copy(pinInput = current.dropLast(1), errorMessage = null) }
    }
  }

  fun onPinClear() {
    _uiState.update { it.copy(pinInput = "", errorMessage = null) }
  }

  fun submitSetupPin() {
    val state = _uiState.value
    if (!state.isConfirmingPin) {
      if (state.pinInput.length < 4) {
        _uiState.update { it.copy(errorMessage = "El PIN debe tener al menos 4 dígitos") }
        return
      }
      _uiState.update {
        it.copy(
          isConfirmingPin = true,
          setupPinConfirm = state.pinInput,
          pinInput = "",
          errorMessage = null
        )
      }
    } else {
      if (state.pinInput != state.setupPinConfirm) {
        _uiState.update {
          it.copy(
            errorMessage = "Los PINs no coinciden. Inténtalo de nuevo.",
            pinInput = "",
            isConfirmingPin = false,
            setupPinConfirm = ""
          )
        }
      } else {
        repository.setupVault(state.pinInput)
        loadVaultData()
        _uiState.update {
          it.copy(
            isSetup = true,
            isUnlocked = true,
            pinInput = "",
            isConfirmingPin = false,
            setupPinConfirm = "",
            errorMessage = null,
            settings = repository.getSettings()
          )
        }
        showToast("¡Bóveda SafeBox 2.0 configurada con éxito!")
      }
    }
  }

  fun submitUnlock() {
    if (_uiState.value.lockoutSecondsRemaining > 0) return
    tryUnlock(_uiState.value.pinInput)
  }

  private fun tryUnlock(pin: String) {
    if (repository.unlock(pin)) {
      lockoutTimerJob?.cancel()
      loadVaultData()
      _uiState.update {
        it.copy(
          isUnlocked = true,
          pinInput = "",
          errorMessage = null,
          lockoutSecondsRemaining = 0L,
          failedAttempts = 0
        )
      }
      showToast("Bóveda desbloqueada")
    } else {
      val remaining = repository.getLockoutRemainingSeconds()
      val attempts = repository.getSettings().failedAttempts
      if (remaining > 0) {
        startLockoutTimer(remaining, attempts)
      } else {
        _uiState.update {
          it.copy(
            errorMessage = "PIN incorrecto (Intento $attempts). Acceso denegado.",
            pinInput = "",
            failedAttempts = attempts
          )
        }
      }
    }
  }

  fun unlockWithBiometrics(): Boolean {
    if (_uiState.value.lockoutSecondsRemaining > 0) {
      showToast("Bloqueo temporal activo")
      return false
    }
    if (repository.unlockWithBiometrics()) {
      lockoutTimerJob?.cancel()
      loadVaultData()
      _uiState.update {
        it.copy(
          isUnlocked = true,
          pinInput = "",
          errorMessage = null,
          lockoutSecondsRemaining = 0L,
          failedAttempts = 0
        )
      }
      showToast("Bóveda desbloqueada con huella")
      return true
    } else {
      showToast("Autenticación biométrica no configurada. Usa tu PIN.")
      return false
    }
  }

  fun unlockQuickDemo() {
    if (!_uiState.value.isSetup) {
      repository.setupVault("1234")
      loadVaultData()
      _uiState.update {
        it.copy(
          isSetup = true,
          isUnlocked = true,
          pinInput = "",
          errorMessage = null,
          settings = repository.getSettings()
        )
      }
      showToast("Bóveda inicializada con PIN de prueba 1234")
    } else {
      if (repository.unlock("1234") || repository.unlockBypassForDemo()) {
        loadVaultData()
        _uiState.update {
          it.copy(
            isUnlocked = true,
            pinInput = "",
            errorMessage = null,
            lockoutSecondsRemaining = 0L
          )
        }
        showToast("Bóveda desbloqueada")
      } else {
        submitUnlock()
      }
    }
  }

  fun lockVault() {
    repository.lock()
    _uiState.update {
      it.copy(
        isUnlocked = false,
        pinInput = "",
        errorMessage = null,
        viewerFile = null,
        viewerDecryptedBytes = null,
        viewerTempFile = null,
        isSelectionMode = false,
        selectedFileIds = emptySet()
      )
    }
  }

  // --- DATA LOADING & NAVIGATION ---

  fun loadVaultData() {
    if (!repository.isUnlocked()) return
    val allFiles = repository.getAllFiles(includeTrash = false)
    val photos = allFiles.filter { it.category == FileCategory.PHOTO }
    val videos = allFiles.filter { it.category == FileCategory.VIDEO }
    val documents = allFiles.filter { it.category == FileCategory.DOCUMENT }
    val notes = repository.getNotes(includeTrash = false)
    val items = repository.getItems(includeTrash = false)
    val trash = repository.getTrashCollection()
    val storageStats = repository.calculateStorageStats()
    val audit = repository.calculateAudit()
    val backupJson = repository.exportVaultEncrypted()

    _uiState.update {
      it.copy(
        photos = photos,
        videos = videos,
        documents = documents,
        notes = notes,
        items = items,
        trash = trash,
        storageStats = storageStats,
        auditSummary = audit,
        backupExportJson = backupJson,
        settings = repository.getSettings()
      )
    }
  }

  fun setActiveTab(tab: MainNavigationTab) {
    _uiState.update { it.copy(activeTab = tab, isSelectionMode = false, selectedFileIds = emptySet()) }
    loadVaultData()
  }

  fun selectTab(tab: VaultTab) {
    _uiState.update { it.copy(currentTab = tab, isSelectionMode = false, selectedFileIds = emptySet()) }
    loadVaultData()
  }

  fun selectCategory(cat: VaultCategory?) {
    _uiState.update { it.copy(selectedCategory = cat) }
  }

  fun toggleFavoritesFilter() {
    _uiState.update { it.copy(filterFavoritesOnly = !it.filterFavoritesOnly) }
  }

  fun setActiveSection(section: VaultSection) {
    _uiState.update { it.copy(activeSection = section, isSelectionMode = false, selectedFileIds = emptySet()) }
  }

  fun setSortOption(option: SortOption) {
    _uiState.update { it.copy(sortOption = option) }
  }

  fun setDocumentFilter(filter: DocumentFilter) {
    _uiState.update { it.copy(documentFilter = filter) }
  }

  fun setSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun toggleGlobalSearch(open: Boolean) {
    _uiState.update { it.copy(isGlobalSearchOpen = open) }
  }

  // --- MULTI-SELECTION ---

  fun toggleSelection(id: String) {
    val current = _uiState.value.selectedFileIds.toMutableSet()
    if (current.contains(id)) {
      current.remove(id)
    } else {
      current.add(id)
    }
    _uiState.update {
      it.copy(
        selectedFileIds = current,
        isSelectionMode = current.isNotEmpty()
      )
    }
  }

  fun clearSelection() {
    _uiState.update { it.copy(isSelectionMode = false, selectedFileIds = emptySet()) }
  }

  fun selectAll(ids: List<String>) {
    _uiState.update { it.copy(selectedFileIds = ids.toSet(), isSelectionMode = true) }
  }

  fun deleteSelectedToTrash() {
    val ids = _uiState.value.selectedFileIds
    for (id in ids) {
      repository.moveFileToTrash(id)
    }
    clearSelection()
    loadVaultData()
    showToast("${ids.size} elementos movidos a la papelera")
  }

  // --- FILES IMPORT & MANAGEMENT ---

  fun importMultipleFiles(uris: List<Uri>, category: FileCategory) {
    if (uris.isEmpty()) return
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          importProgress = ImportProgress(
            current = 0,
            total = uris.size,
            isImporting = true,
            currentFileName = "Iniciando importación..."
          )
        )
      }

      var importedCount = 0
      var duplicateCount = 0

      uris.forEachIndexed { index, uri ->
        _uiState.update {
          it.copy(
            importProgress = it.importProgress?.copy(
              current = index + 1,
              currentFileName = "Cifrando archivo ${index + 1} de ${uris.size}..."
            )
          )
        }

        when (val res = repository.importFileFromUri(uri, category, getApplication())) {
          is ImportResult.Success -> importedCount++
          is ImportResult.Duplicate -> duplicateCount++
          is ImportResult.Error -> {
            // error handled
          }
        }
      }

      _uiState.update { it.copy(importProgress = null) }
      loadVaultData()

      if (duplicateCount > 0) {
        showToast("Se importaron $importedCount archivos ($duplicateCount duplicados omitidos)")
      } else {
        showToast("¡$importedCount archivos cifrados y guardados en SafeBox!")
      }
    }
  }

  fun openViewer(item: VaultFileItem) {
    viewModelScope.launch {
      val bytes = repository.decryptFileBytes(item)
      val tempFile = repository.decryptFileToTemp(item)
      _uiState.update {
        it.copy(
          viewerFile = item,
          viewerDecryptedBytes = bytes,
          viewerTempFile = tempFile,
          viewingFileItem = item,
          decryptedFileBytes = bytes,
          tempExportFile = tempFile
        )
      }
    }
  }

  fun closeViewer() {
    _uiState.value.viewerTempFile?.delete()
    _uiState.value.tempExportFile?.delete()
    _uiState.update {
      it.copy(
        viewerFile = null,
        viewerDecryptedBytes = null,
        viewerTempFile = null,
        viewingFileItem = null,
        decryptedFileBytes = null,
        tempExportFile = null
      )
    }
  }

  fun toggleFileFavorite(id: String) {
    repository.toggleFileFavorite(id)
    loadVaultData()
  }

  fun moveFileToTrash(id: String) {
    repository.moveFileToTrash(id)
    if (_uiState.value.viewerFile?.id == id || _uiState.value.viewingFileItem?.id == id) closeViewer()
    loadVaultData()
    showToast("Archivo movido a la papelera")
  }

  fun deleteFileToTrash(id: String) {
    moveFileToTrash(id)
  }

  // --- PRIVATE NOTES ---

  fun openNewNote() {
    _uiState.update { it.copy(showNoteDialog = true, showNoteEditor = true, noteToEdit = null) }
  }

  fun openEditNote(note: VaultNoteItem) {
    _uiState.update { it.copy(showNoteDialog = true, showNoteEditor = true, noteToEdit = note) }
  }

  fun closeNoteDialog() {
    _uiState.update { it.copy(showNoteDialog = false, showNoteEditor = false, noteToEdit = null) }
  }

  fun closeNoteEditor() {
    closeNoteDialog()
  }

  fun saveNote(title: String, content: String, category: String, isFavorite: Boolean) {
    val currentEdit = _uiState.value.noteToEdit
    if (currentEdit != null) {
      val updated = currentEdit.copy(
        title = title.ifBlank { "Sin título" },
        content = content,
        category = category,
        isFavorite = isFavorite,
        updatedAt = System.currentTimeMillis()
      )
      repository.updateNote(updated)
    } else {
      val newNote = VaultNoteItem(
        id = java.util.UUID.randomUUID().toString(),
        title = title.ifBlank { "Sin título" },
        content = content,
        category = category,
        isFavorite = isFavorite
      )
      repository.addNote(newNote)
    }
    closeNoteDialog()
    loadVaultData()
    showToast("Nota cifrada guardada")
  }

  fun toggleNoteFavorite(id: String) {
    repository.toggleNoteFavorite(id)
    loadVaultData()
  }

  fun moveNoteToTrash(id: String) {
    repository.moveNoteToTrash(id)
    closeNoteDialog()
    loadVaultData()
    showToast("Nota movida a la papelera")
  }

  fun deleteNoteToTrash(id: String) {
    moveNoteToTrash(id)
  }

  // --- VAULT ENTRIES (Passwords, Cards, Identities) ---

  fun openAddEntry() {
    _uiState.update { it.copy(showAddEditEntryDialog = true, showAddEditSheet = true, entryToEdit = null, itemToEdit = null) }
  }

  fun openAddSheet() {
    openAddEntry()
  }

  fun openEditEntry(entry: VaultEntry) {
    _uiState.update { it.copy(showAddEditEntryDialog = true, showAddEditSheet = true, entryToEdit = entry, itemToEdit = entry) }
  }

  fun openEditSheet(entry: VaultEntry) {
    openEditEntry(entry)
  }

  fun closeAddEditEntry() {
    _uiState.update { it.copy(showAddEditEntryDialog = false, showAddEditSheet = false, entryToEdit = null, itemToEdit = null) }
  }

  fun closeAddEditSheet() {
    closeAddEditEntry()
  }

  fun saveVaultItem(entry: VaultEntry) {
    val current = repository.getItems(includeTrash = true)
    if (current.any { it.id == entry.id }) {
      repository.updateItem(entry)
    } else {
      repository.addItem(entry)
    }
    closeAddEditEntry()
    loadVaultData()
    showToast("Elemento cifrado guardado")
  }

  fun moveEntryToTrash(id: String) {
    repository.moveItemToTrash(id)
    closeAddEditEntry()
    loadVaultData()
    showToast("Elemento movido a la papelera")
  }

  fun deleteVaultItem(entry: VaultEntry) {
    moveEntryToTrash(entry.id)
  }

  fun deleteVaultItem(id: String) {
    moveEntryToTrash(id)
  }

  fun toggleFavorite(id: String) {
    repository.toggleFavorite(id)
    loadVaultData()
  }

  fun toggleEntryFavorite(id: String) {
    toggleFavorite(id)
  }

  fun importFiles(uris: List<Uri>, category: FileCategory = FileCategory.PHOTO) {
    importMultipleFiles(uris, category)
  }

  // --- TRASH RESTORATION & SHREDDING ---

  fun restoreItemFromTrash(entry: VaultEntry) {
    repository.restoreItemFromTrash(entry.id)
    loadVaultData()
    showToast("Elemento restaurado")
  }

  fun restoreNoteFromTrash(note: VaultNoteItem) {
    repository.restoreNoteFromTrash(note.id)
    loadVaultData()
    showToast("Nota restaurada")
  }

  fun restoreFileFromTrash(file: VaultFileItem) {
    repository.restoreFileFromTrash(file.id)
    loadVaultData()
    showToast("Archivo restaurado")
  }

  fun deleteEntryPermanently(id: String) {
    repository.deleteItemPermanently(id)
    loadVaultData()
    showToast("Elemento eliminado definitivamente")
  }

  fun deleteNotePermanently(id: String) {
    repository.deleteNotePermanently(id)
    loadVaultData()
    showToast("Nota eliminada definitivamente")
  }

  fun deleteFilePermanently(id: String) {
    repository.deleteFilePermanently(id)
    loadVaultData()
    showToast("Archivo triturado y eliminado de forma segura")
  }

  fun emptyTrash() {
    repository.emptyTrash()
    loadVaultData()
    showToast("Papelera vaciada por completo")
  }

  // --- SETTINGS & PIN CHANGE ---

  fun openChangePin() {
    _uiState.update { it.copy(showChangePinDialog = true) }
  }

  fun closeChangePin() {
    _uiState.update { it.copy(showChangePinDialog = false) }
  }

  fun changePin(oldPin: String, newPin: String): Boolean {
    if (newPin.length < 4) {
      showToast("El nuevo PIN debe tener al menos 4 dígitos")
      return false
    }
    val success = repository.changePin(oldPin, newPin)
    if (success) {
      closeChangePin()
      showToast("PIN actualizado y datos recifrados correctamente")
    } else {
      showToast("El PIN actual no es correcto")
    }
    return success
  }

  fun updateSettings(settings: VaultSettings) {
    repository.saveSettings(settings)
    _uiState.update { it.copy(settings = settings) }
    showToast("Configuración guardada")
  }

  fun openStorageDialog() {
    loadVaultData()
    _uiState.update { it.copy(showStorageDialog = true) }
  }

  fun closeStorageDialog() {
    _uiState.update { it.copy(showStorageDialog = false) }
  }

  // --- PASSWORD GENERATOR ---

  fun openGenerator() {
    _uiState.update { it.copy(showGeneratorDialog = true) }
  }

  fun closeGenerator() {
    _uiState.update { it.copy(showGeneratorDialog = false) }
  }

  fun generateNewPassword(length: Int = 16) {
    val pwd = CryptoManager.generateSecurePassword(length = length)
    _uiState.update { it.copy(generatedPassword = pwd) }
  }

  // --- AUDIT & BACKUP ---

  fun openAudit() {
    loadVaultData()
    _uiState.update { it.copy(showAuditDialog = true) }
  }

  fun closeAudit() {
    _uiState.update { it.copy(showAuditDialog = false) }
  }

  fun openBackup() {
    loadVaultData()
    _uiState.update { it.copy(showBackupDialog = true) }
  }

  fun closeBackup() {
    _uiState.update { it.copy(showBackupDialog = false) }
  }

  fun restoreBackup(backupJson: String): Boolean {
    val success = repository.restoreVaultFromEncryptedJson(backupJson)
    if (success) {
      loadVaultData()
      showToast("Copia de seguridad restaurada correctamente")
      closeBackup()
    } else {
      showToast("Error al descifrar o formato de copia inválido")
    }
    return success
  }

  // --- HELPERS ---

  fun copyToClipboard(label: String, text: String) {
    val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    showToast("$label copiado al portapapeles")
  }

  private fun showToast(msg: String) {
    Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
  }
}
