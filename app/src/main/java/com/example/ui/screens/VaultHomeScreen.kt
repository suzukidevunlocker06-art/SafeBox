package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultCategory
import com.example.data.model.VaultEntry
import com.example.ui.VaultViewModel
import com.example.ui.dialogs.AddEditItemDialog
import com.example.ui.dialogs.BackupDialog
import com.example.ui.dialogs.PasswordGeneratorDialog
import com.example.ui.dialogs.SecurityAuditDialog
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeCardDark
import com.example.ui.theme.SafeCardLight
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafeNavyDark
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

  // Filter items based on search and category
  val filteredItems = state.items.filter { item ->
    val matchesCategory = state.selectedCategory == null || item.type == state.selectedCategory
    val matchesFavorites = !state.filterFavoritesOnly || item.isFavorite
    val query = state.searchQuery.trim().lowercase()
    val matchesSearch = query.isEmpty() ||
      item.title.lowercase().contains(query) ||
      item.folder.lowercase().contains(query) ||
      item.getPreviewText().lowercase().contains(query)

    matchesCategory && matchesFavorites && matchesSearch
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
                text = "SafeBox",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = "Bóveda Cifrada AES-256",
                fontSize = 11.sp,
                color = SafeEmerald,
                fontWeight = FontWeight.Medium
              )
            }
          }
        },
        actions = {
          // Generator shortcut
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
                contentDescription = "Auditoría de seguridad",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Backup
          IconButton(onClick = { viewModel.openBackup() }) {
            Icon(
              imageVector = Icons.Default.FileDownload,
              contentDescription = "Copia de seguridad",
              tint = MaterialTheme.colorScheme.onSurface
            )
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
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { viewModel.openAddSheet() },
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Nuevo elemento", fontWeight = FontWeight.SemiBold) },
        containerColor = SafePrimary,
        contentColor = Color.White
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Search Bar
      OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        placeholder = { Text("Buscar en la bóveda...", fontSize = 14.sp) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Buscar", modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
          if (state.searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setSearchQuery("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
      )

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
            selected = state.selectedCategory == null && !state.filterFavoritesOnly,
            onClick = {
              viewModel.selectCategory(null)
              if (state.filterFavoritesOnly) viewModel.toggleFavoritesFilter()
            },
            label = { Text("Todos (${state.items.size})", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SafePrimary,
              selectedLabelColor = Color.White
            )
          )
        }

        item {
          FilterChip(
            selected = state.filterFavoritesOnly,
            onClick = { viewModel.toggleFavoritesFilter() },
            leadingIcon = {
              Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (state.filterFavoritesOnly) Color.White else SafeAmber,
                modifier = Modifier.size(16.dp)
              )
            },
            label = { Text("Favoritos", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SafeAmber,
              selectedLabelColor = Color.White
            )
          )
        }

        items(VaultCategory.values()) { cat ->
          val isSelected = state.selectedCategory == cat && !state.filterFavoritesOnly
          val count = state.items.count { it.type == cat }
          FilterChip(
            selected = isSelected,
            onClick = {
              if (state.filterFavoritesOnly) viewModel.toggleFavoritesFilter()
              viewModel.selectCategory(cat)
            },
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
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (state.searchQuery.isNotEmpty()) "Sin coincidencias" else "Bóveda vacía",
              fontSize = 17.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
              text = "Toca el botón '+ Nuevo elemento' para almacenar credenciales o notas con cifrado AES-256.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
              lineHeight = 17.sp
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
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
  }

  // Add / Edit Dialog
  if (state.showAddEditSheet) {
    AddEditItemDialog(
      initialItem = state.itemToEdit,
      onDismiss = { viewModel.closeAddEditSheet() },
      onSave = { viewModel.saveVaultItem(it) },
      onDelete = { viewModel.deleteVaultItem(it) }
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

  // Backup Dialog
  if (state.showBackupDialog) {
    BackupDialog(
      encryptedJson = state.backupExportJson,
      onDismiss = { viewModel.closeBackup() },
      onCopy = { viewModel.copyToClipboard("Copia de seguridad SafeBox", it) }
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
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Category Icon
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

      // Main Content Info
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

        val sec = entry.getSecondaryText()
        if (sec.isNotEmpty()) {
          Text(
            text = sec,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Action Buttons
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (entry.type == VaultCategory.PASSWORD) {
          IconButton(
            onClick = onCopyPassword,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Password,
              contentDescription = "Copiar contraseña",
              tint = SafePrimary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        IconButton(
          onClick = onFavoriteToggle,
          modifier = Modifier.size(34.dp)
        ) {
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
