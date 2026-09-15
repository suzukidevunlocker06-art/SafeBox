package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.VaultCategory
import com.example.data.model.VaultEntry
import com.example.ui.VaultTab
import com.example.ui.VaultUiState
import com.example.ui.VaultViewModel
import com.example.ui.dialogs.AddEditItemDialog
import com.example.ui.dialogs.BackupDialog
import com.example.ui.dialogs.ChangePinDialog
import com.example.ui.dialogs.DocumentViewerDialog
import com.example.ui.dialogs.GlobalSearchDialog
import com.example.ui.dialogs.ImportProgressDialog
import com.example.ui.dialogs.NoteEditorDialog
import com.example.ui.dialogs.PasswordGeneratorDialog
import com.example.ui.dialogs.PhotoViewerDialog
import com.example.ui.dialogs.SecurityAuditDialog
import com.example.ui.dialogs.StorageBreakdownDialog
import com.example.ui.dialogs.VideoPlayerDialog
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
  viewModel: VaultViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()
  var showGlobalSearch by remember { mutableStateOf(false) }
  var gallerySubTab by remember { mutableIntStateOf(0) } // 0 = Fotos, 1 = Videos

  // File picker launchers
  val mediaPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      viewModel.importFiles(uris)
    }
  }

  val docPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      viewModel.importFiles(uris)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SafePrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SafePrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "SafeBox 2.0",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = "Cifrado AES-256 local",
                fontSize = 10.sp,
                color = SafeEmerald,
                fontWeight = FontWeight.Medium
              )
            }
          }
        },
        actions = {
          // Global Search
          IconButton(onClick = { showGlobalSearch = true }) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Búsqueda Global",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          // Password Generator
          IconButton(onClick = { viewModel.openGenerator() }) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "Generador de contraseñas",
              tint = SafePrimary
            )
          }

          // Security Audit shortcut
          IconButton(onClick = { viewModel.openAudit() }) {
            BadgedBox(
              badge = {
                Badge(
                  containerColor = if (state.auditSummary.securityScorePercent >= 80) SafeEmerald else SafeAmber
                ) {
                  Text("${state.auditSummary.securityScorePercent}%", fontSize = 9.sp)
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Auditoría",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Trash shortcut with badge
          IconButton(onClick = { viewModel.selectTab(VaultTab.TRASH) }) {
            if (state.trash.totalCount > 0) {
              BadgedBox(
                badge = {
                  Badge(containerColor = SafeRose) {
                    Text("${state.trash.totalCount}", fontSize = 9.sp)
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Papelera",
                  tint = if (state.currentTab == VaultTab.TRASH) SafeRose else MaterialTheme.colorScheme.onSurface
                )
              }
            } else {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Papelera",
                tint = if (state.currentTab == VaultTab.TRASH) SafeRose else MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Lock Now button
          IconButton(onClick = { viewModel.lockVault() }) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Bloquear ahora",
              tint = SafeRose
            )
          }
        }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
      ) {
        // Galería (Fotos & Videos)
        NavigationBarItem(
          selected = state.currentTab == VaultTab.GALLERY,
          onClick = { viewModel.selectTab(VaultTab.GALLERY) },
          icon = { Icon(Icons.Default.Image, contentDescription = "Galería") },
          label = { Text("Galería", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(selectedIconColor = SafePrimary, indicatorColor = SafePrimary.copy(alpha = 0.15f))
        )

        // Documentos
        NavigationBarItem(
          selected = state.currentTab == VaultTab.DOCUMENTS,
          onClick = { viewModel.selectTab(VaultTab.DOCUMENTS) },
          icon = { Icon(Icons.Default.Description, contentDescription = "Documentos") },
          label = { Text("Docs", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(selectedIconColor = SafeEmerald, indicatorColor = SafeEmerald.copy(alpha = 0.15f))
        )

        // Notas
        NavigationBarItem(
          selected = state.currentTab == VaultTab.NOTES,
          onClick = { viewModel.selectTab(VaultTab.NOTES) },
          icon = { Icon(Icons.Default.Description, contentDescription = "Notas") },
          label = { Text("Notas", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(selectedIconColor = SafeAmber, indicatorColor = SafeAmber.copy(alpha = 0.15f))
        )

        // Cuentas / Contraseñas
        NavigationBarItem(
          selected = state.currentTab == VaultTab.PASSWORDS,
          onClick = { viewModel.selectTab(VaultTab.PASSWORDS) },
          icon = { Icon(Icons.Default.VpnKey, contentDescription = "Cuentas") },
          label = { Text("Cuentas", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF8B5CF6), indicatorColor = Color(0xFF8B5CF6).copy(alpha = 0.15f))
        )

        // Favoritos
        NavigationBarItem(
          selected = state.currentTab == VaultTab.FAVORITES,
          onClick = { viewModel.selectTab(VaultTab.FAVORITES) },
          icon = { Icon(Icons.Default.Star, contentDescription = "Favoritos") },
          label = { Text("Favoritos", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(selectedIconColor = SafeAmber, indicatorColor = SafeAmber.copy(alpha = 0.15f))
        )

        // Ajustes
        NavigationBarItem(
          selected = state.currentTab == VaultTab.SETTINGS,
          onClick = { viewModel.selectTab(VaultTab.SETTINGS) },
          icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
          label = { Text("Ajustes", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(selectedIconColor = SafePrimary, indicatorColor = SafePrimary.copy(alpha = 0.15f))
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (state.currentTab) {
        VaultTab.GALLERY -> {
          Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
              selectedTabIndex = gallerySubTab,
              containerColor = MaterialTheme.colorScheme.background
            ) {
              Tab(
                selected = gallerySubTab == 0,
                onClick = { gallerySubTab = 0 },
                text = { Text("Fotos (${state.photos.size})", fontSize = 13.sp) }
              )
              Tab(
                selected = gallerySubTab == 1,
                onClick = { gallerySubTab = 1 },
                text = { Text("Videos (${state.videos.size})", fontSize = 13.sp) }
              )
            }

            GalleryView(
              viewModel = viewModel,
              state = state,
              category = if (gallerySubTab == 0) FileCategory.PHOTO else FileCategory.VIDEO,
              onImportClick = {
                val mime = if (gallerySubTab == 0) "image/*" else "video/*"
                mediaPickerLauncher.launch(mime)
              }
            )
          }
        }

        VaultTab.DOCUMENTS -> {
          DocumentsView(
            viewModel = viewModel,
            state = state,
            onImportClick = {
              docPickerLauncher.launch("*/*")
            }
          )
        }

        VaultTab.NOTES -> {
          NotesView(
            viewModel = viewModel,
            state = state
          )
        }

        VaultTab.PASSWORDS -> {
          AccountsView(
            viewModel = viewModel,
            state = state
          )
        }

        VaultTab.FAVORITES -> {
          FavoritesView(
            viewModel = viewModel,
            state = state
          )
        }

        VaultTab.TRASH -> {
          TrashView(
            viewModel = viewModel,
            state = state
          )
        }

        VaultTab.SETTINGS -> {
          SettingsView(
            viewModel = viewModel,
            state = state
          )
        }
      }
    }
  }

  // DIALOGS & OVERLAYS

  // Add / Edit Credential Dialog
  if (state.showAddEditSheet) {
    AddEditItemDialog(
      initialItem = state.itemToEdit,
      onDismiss = { viewModel.closeAddEditSheet() },
      onSave = { viewModel.saveVaultItem(it) },
      onDelete = { viewModel.deleteVaultItem(it) }
    )
  }

  // Note Editor Dialog
  if (state.showNoteEditor) {
    NoteEditorDialog(
      initialNote = state.noteToEdit,
      onDismiss = { viewModel.closeNoteEditor() },
      onSave = { title, content, category, isFav ->
        viewModel.saveNote(title, content, category, isFav)
      },
      onDelete = if (state.noteToEdit != null) {
        {
          viewModel.deleteNoteToTrash(state.noteToEdit!!.id)
          viewModel.closeNoteEditor()
        }
      } else null
    )
  }

  // Password Generator Dialog
  if (state.showGeneratorDialog) {
    PasswordGeneratorDialog(
      onDismiss = { viewModel.closeGenerator() },
      onCopy = { viewModel.copyToClipboard("Contraseña generada", it) }
    )
  }

  // Security Audit Dialog
  if (state.showAuditDialog) {
    SecurityAuditDialog(
      audit = state.auditSummary,
      onDismiss = { viewModel.closeAudit() }
    )
  }

  // Backup & Restore Dialog
  if (state.showBackupDialog) {
    BackupDialog(
      encryptedJson = state.backupExportJson,
      onDismiss = { viewModel.closeBackup() },
      onCopy = { viewModel.copyToClipboard("Copia de seguridad SafeBox", it) },
      onRestore = { backupJson ->
        val ok = viewModel.restoreBackup(backupJson)
        if (ok) {
          viewModel.closeBackup()
        }
      }
    )
  }

  // Storage Breakdown Dialog
  if (state.showStorageDialog && state.storageStats != null) {
    StorageBreakdownDialog(
      stats = state.storageStats!!,
      onDismiss = { viewModel.closeStorageDialog() }
    )
  }

  // Change PIN Dialog
  if (state.showChangePinDialog) {
    ChangePinDialog(
      onDismiss = { viewModel.closeChangePin() },
      onChangePin = { oldPin, newPin ->
        val success = viewModel.changePin(oldPin, newPin)
        if (success) {
          viewModel.closeChangePin()
        }
        success
      }
    )
  }

  // Global Search Dialog
  if (showGlobalSearch) {
    GlobalSearchDialog(
      state = state,
      onDismiss = { showGlobalSearch = false },
      onSelectFile = { file -> viewModel.openViewer(file) },
      onSelectNote = { note -> viewModel.openEditNote(note) },
      onSelectEntry = { entry -> viewModel.openEditSheet(entry) }
    )
  }

  // Import Progress Dialog
  if (state.importProgress != null) {
    ImportProgressDialog(progress = state.importProgress!!)
  }

  // Photo Viewer Dialog
  if (state.viewingFileItem != null && state.viewingFileItem!!.category == FileCategory.PHOTO) {
    PhotoViewerDialog(
      item = state.viewingFileItem!!,
      decryptedBytes = state.decryptedFileBytes,
      tempFile = state.tempExportFile,
      onDismiss = { viewModel.closeViewer() },
      onToggleFavorite = { viewModel.toggleFileFavorite(state.viewingFileItem!!.id) },
      onDelete = {
        viewModel.deleteFileToTrash(state.viewingFileItem!!.id)
        viewModel.closeViewer()
      }
    )
  }

  // Video Player Dialog
  if (state.viewingFileItem != null && state.viewingFileItem!!.category == FileCategory.VIDEO) {
    VideoPlayerDialog(
      item = state.viewingFileItem!!,
      tempFile = state.tempExportFile,
      onDismiss = { viewModel.closeViewer() },
      onToggleFavorite = { viewModel.toggleFileFavorite(state.viewingFileItem!!.id) },
      onDelete = {
        viewModel.deleteFileToTrash(state.viewingFileItem!!.id)
        viewModel.closeViewer()
      }
    )
  }

  // Document Viewer Dialog
  if (state.viewingFileItem != null && state.viewingFileItem!!.category == FileCategory.DOCUMENT) {
    DocumentViewerDialog(
      item = state.viewingFileItem!!,
      decryptedBytes = state.decryptedFileBytes,
      tempFile = state.tempExportFile,
      onDismiss = { viewModel.closeViewer() },
      onToggleFavorite = { viewModel.toggleFileFavorite(state.viewingFileItem!!.id) },
      onDelete = {
        viewModel.deleteFileToTrash(state.viewingFileItem!!.id)
        viewModel.closeViewer()
      },
      onCopyContent = { text ->
        viewModel.copyToClipboard("Documento ${state.viewingFileItem!!.originalName}", text)
      }
    )
  }
}

/**
 * Accounts / Credentials tab
 */
@Composable
private fun AccountsView(
  viewModel: VaultViewModel,
  state: VaultUiState,
  modifier: Modifier = Modifier
) {
  val query = state.searchQuery.trim().lowercase()

  val filteredItems = state.items.filter { item ->
    val matchesCategory = state.selectedCategory == null || item.type == state.selectedCategory
    val matchesSearch = query.isEmpty() ||
      item.title.lowercase().contains(query) ||
      item.folder.lowercase().contains(query) ||
      item.getPreviewText().lowercase().contains(query)

    matchesCategory && matchesSearch
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Category Chips
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          FilterChip(
            selected = state.selectedCategory == null,
            onClick = { viewModel.selectCategory(null) },
            label = { Text("Todos (${state.items.size})", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SafePrimary,
              selectedLabelColor = Color.White
            )
          )
        }

        items(VaultCategory.values()) { cat ->
          val isSelected = state.selectedCategory == cat
          val count = state.items.count { it.type == cat }
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.selectCategory(cat) },
            label = { Text("${cat.displayName} ($count)", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SafePrimary,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      // Vault Items List
      if (filteredItems.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SafePrimary.copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.VpnKey,
                contentDescription = null,
                tint = SafePrimary,
                modifier = Modifier.size(38.dp)
              )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = if (state.searchQuery.isNotEmpty()) "Sin coincidencias" else "Sin credenciales guardadas",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Almacena contraseñas, tarjetas y credenciales protegidas con cifrado AES-256.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Button(
              onClick = { viewModel.openAddSheet() },
              shape = RoundedCornerShape(12.dp),
              colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SafePrimary)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Nueva Cuenta")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredItems, key = { it.id }) { item ->
            VaultItemCard(
              entry = item,
              onClick = { viewModel.openEditSheet(item) },
              onFavoriteToggle = { viewModel.toggleFavorite(item.id) },
              onCopyUsername = {
                try {
                  val json = JSONObject(item.payloadJson)
                  val user = json.optString("username", "")
                  if (user.isNotEmpty()) viewModel.copyToClipboard("Usuario", user)
                } catch (e: Exception) {
                  // ignore
                }
              },
              onCopyPassword = {
                try {
                  val json = JSONObject(item.payloadJson)
                  val pwd = json.optString("password", "")
                  if (pwd.isNotEmpty()) viewModel.copyToClipboard("Contraseña", pwd)
                } catch (e: Exception) {
                  // ignore
                }
              }
            )
          }
        }
      }
    }

    ExtendedFloatingActionButton(
      onClick = { viewModel.openAddSheet() },
      icon = { Icon(Icons.Default.Add, contentDescription = null) },
      text = { Text("Nueva Cuenta", fontWeight = FontWeight.SemiBold) },
      containerColor = SafePrimary,
      contentColor = Color.White,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
    )
  }
}

@Composable
private fun VaultItemCard(
  entry: VaultEntry,
  onClick: () -> Unit,
  onFavoriteToggle: () -> Unit,
  onCopyUsername: () -> Unit,
  onCopyPassword: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(
            when (entry.type) {
              VaultCategory.PASSWORD -> SafePrimary.copy(alpha = 0.12f)
              VaultCategory.NOTE -> SafeAmber.copy(alpha = 0.12f)
              VaultCategory.CARD -> SafeEmerald.copy(alpha = 0.12f)
              VaultCategory.IDENTITY -> Color(0xFF8B5CF6).copy(alpha = 0.12f)
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = when (entry.type) {
            VaultCategory.PASSWORD -> Icons.Default.VpnKey
            VaultCategory.NOTE -> Icons.Default.Description
            VaultCategory.CARD -> Icons.Default.CreditCard
            VaultCategory.IDENTITY -> Icons.Default.Security
          },
          contentDescription = null,
          tint = when (entry.type) {
            VaultCategory.PASSWORD -> SafePrimary
            VaultCategory.NOTE -> SafeAmber
            VaultCategory.CARD -> SafeEmerald
            VaultCategory.IDENTITY -> Color(0xFF8B5CF6)
          },
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = entry.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "• ${entry.folder}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = entry.getPreviewText(),
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (entry.type == VaultCategory.PASSWORD) {
          IconButton(onClick = onCopyPassword, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Default.Password, contentDescription = "Copiar contraseña", tint = SafePrimary, modifier = Modifier.size(18.dp))
          }
        }

        IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(34.dp)) {
          Icon(
            imageVector = if (entry.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Favorito",
            tint = if (entry.isFavorite) SafeAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            modifier = Modifier.size(19.dp)
          )
        }
      }
    }
  }
}
