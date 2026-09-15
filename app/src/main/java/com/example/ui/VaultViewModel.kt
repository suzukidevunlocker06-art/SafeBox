package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.data.SecurityAuditSummary
import com.example.data.VaultRepository
import com.example.data.model.VaultCategory
import com.example.data.model.VaultEntry
import com.example.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VaultUiState(
  val isSetup: Boolean = false,
  val isUnlocked: Boolean = false,
  val pinInput: String = "",
  val setupPinConfirm: String = "",
  val isConfirmingPin: Boolean = false,
  val errorMessage: String? = null,
  val items: List<VaultEntry> = emptyList(),
  val searchQuery: String = "",
  val selectedCategory: VaultCategory? = null,
  val filterFavoritesOnly: Boolean = false,
  val auditSummary: SecurityAuditSummary = SecurityAuditSummary(0, 0, 0, 0, 100),
  val showAddEditSheet: Boolean = false,
  val itemToEdit: VaultEntry? = null,
  val showGeneratorDialog: Boolean = false,
  val showAuditDialog: Boolean = false,
  val showBackupDialog: Boolean = false,
  val generatedPassword: String = "",
  val generatedPasswordLength: Int = 16,
  val backupExportJson: String? = null
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = VaultRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(
    VaultUiState(
      isSetup = repository.isSetup(),
      isUnlocked = false
    )
  )
  val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

  init {
    generateNewPassword()
  }

  fun onPinDigit(digit: String) {
    val current = _uiState.value.pinInput
    if (current.length < 6) {
      val next = current + digit
      _uiState.update { it.copy(pinInput = next, errorMessage = null) }
      if (next.length >= 4 && !_uiState.value.isSetup) {
        // In setup mode, don't auto-submit yet so user can choose length
      } else if (next.length == 4 && _uiState.value.isSetup) {
        // If regular 4-digit pin, try auto unlock
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
            errorMessage = null
          )
        }
        showToast("¡Bóveda SafeBox configurada con éxito!")
      }
    }
  }

  fun submitUnlock() {
    tryUnlock(_uiState.value.pinInput)
  }

  private fun tryUnlock(pin: String) {
    if (repository.unlock(pin)) {
      loadVaultData()
      _uiState.update {
        it.copy(
          isUnlocked = true,
          pinInput = "",
          errorMessage = null
        )
      }
      showToast("Bóveda desbloqueada")
    } else {
      _uiState.update {
        it.copy(
          errorMessage = "PIN incorrecto. Acceso denegado.",
          pinInput = ""
        )
      }
    }
  }

  fun unlockQuickDemo() {
    if (!_uiState.value.isSetup) {
      repository.setupVault("1234")
      _uiState.update { it.copy(isSetup = true) }
    }
    if (repository.unlockBypassForDemo()) {
      loadVaultData()
      _uiState.update {
        it.copy(
          isUnlocked = true,
          pinInput = "",
          errorMessage = null
        )
      }
      showToast("Acceso de prueba concedido")
    }
  }

  fun lockVault() {
    repository.lock()
    _uiState.update {
      it.copy(
        isUnlocked = false,
        pinInput = "",
        errorMessage = null,
        showAddEditSheet = false,
        showGeneratorDialog = false,
        showAuditDialog = false
      )
    }
    showToast("Bóveda bloqueada de forma segura")
  }

  fun loadVaultData() {
    val items = repository.getItems()
    val audit = repository.calculateAudit()
    _uiState.update {
      it.copy(
        items = items,
        auditSummary = audit
      )
    }
  }

  fun setSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun selectCategory(category: VaultCategory?) {
    _uiState.update {
      if (it.selectedCategory == category) {
        it.copy(selectedCategory = null)
      } else {
        it.copy(selectedCategory = category)
      }
    }
  }

  fun toggleFavoritesFilter() {
    _uiState.update { it.copy(filterFavoritesOnly = !it.filterFavoritesOnly) }
  }

  fun saveVaultItem(entry: VaultEntry) {
    val existing = _uiState.value.items.any { it.id == entry.id }
    if (existing) {
      repository.updateItem(entry)
      showToast("Elemento actualizado")
    } else {
      repository.addItem(entry)
      showToast("Elemento guardado con cifrado")
    }
    loadVaultData()
    _uiState.update { it.copy(showAddEditSheet = false, itemToEdit = null) }
  }

  fun deleteVaultItem(id: String) {
    repository.deleteItem(id)
    loadVaultData()
    _uiState.update { it.copy(showAddEditSheet = false, itemToEdit = null) }
    showToast("Elemento eliminado de la bóveda")
  }

  fun toggleFavorite(id: String) {
    repository.toggleFavorite(id)
    loadVaultData()
  }

  fun openAddSheet(type: VaultCategory = VaultCategory.PASSWORD) {
    _uiState.update {
      it.copy(
        showAddEditSheet = true,
        itemToEdit = null
      )
    }
  }

  fun openEditSheet(item: VaultEntry) {
    _uiState.update {
      it.copy(
        showAddEditSheet = true,
        itemToEdit = item
      )
    }
  }

  fun closeAddEditSheet() {
    _uiState.update { it.copy(showAddEditSheet = false, itemToEdit = null) }
  }

  fun openGenerator() {
    generateNewPassword()
    _uiState.update { it.copy(showGeneratorDialog = true) }
  }

  fun closeGenerator() {
    _uiState.update { it.copy(showGeneratorDialog = false) }
  }

  fun openAudit() {
    _uiState.update { it.copy(showAuditDialog = true) }
  }

  fun closeAudit() {
    _uiState.update { it.copy(showAuditDialog = false) }
  }

  fun openBackup() {
    val export = repository.exportVaultEncrypted()
    _uiState.update { it.copy(showBackupDialog = true, backupExportJson = export) }
  }

  fun closeBackup() {
    _uiState.update { it.copy(showBackupDialog = false) }
  }

  fun generateNewPassword(
    length: Int = 16,
    includeUpper: Boolean = true,
    includeLower: Boolean = true,
    includeDigits: Boolean = true,
    includeSymbols: Boolean = true
  ) {
    val pwd = CryptoManager.generateSecurePassword(
      length = length,
      includeUpper = includeUpper,
      includeLower = includeLower,
      includeDigits = includeDigits,
      includeSymbols = includeSymbols
    )
    _uiState.update {
      it.copy(
        generatedPassword = pwd,
        generatedPasswordLength = length
      )
    }
  }

  fun copyToClipboard(label: String, text: String) {
    val clipboard =
      getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    showToast("$label copiado al portapapeles")
  }

  private fun showToast(msg: String) {
    Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
  }
}
