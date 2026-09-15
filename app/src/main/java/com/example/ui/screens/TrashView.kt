package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.ui.VaultUiState
import com.example.ui.VaultViewModel
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@Composable
fun TrashView(
  viewModel: VaultViewModel,
  state: VaultUiState,
  modifier: Modifier = Modifier
) {
  var showEmptyConfirmDialog by remember { mutableStateOf(false) }

  val totalCount = state.trash.totalCount

  Column(modifier = modifier.fillMaxSize()) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Papelera de Reciclaje",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "$totalCount elementos eliminados temporalmente",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
      }

      if (totalCount > 0) {
        OutlinedButton(
          onClick = { showEmptyConfirmDialog = true },
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.DeleteForever, contentDescription = null, tint = SafeRose, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Vaciar", color = SafeRose, fontSize = 12.sp)
        }
      }
    }

    if (totalCount == 0) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(SafeRose.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = null,
              tint = SafeRose,
              modifier = Modifier.size(38.dp)
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "La papelera está vacía",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Los elementos y archivos que elimines aparecerán aquí antes de ser triturados definitivamente.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Trashed Files
        items(state.trash.files, key = { it.id }) { file ->
          TrashItemRow(
            icon = when (file.category) {
              FileCategory.PHOTO -> Icons.Default.Image
              FileCategory.VIDEO -> Icons.Default.Videocam
              FileCategory.DOCUMENT -> Icons.Default.Description
            },
            iconTint = when (file.category) {
              FileCategory.PHOTO -> SafePrimary
              FileCategory.VIDEO -> SafeRose
              FileCategory.DOCUMENT -> SafeEmerald
            },
            title = file.originalName,
            subtitle = "${file.category.displayName} • ${file.getFormattedSize()}",
            onRestore = { viewModel.restoreFileFromTrash(file) },
            onDeleteForever = { viewModel.deleteFilePermanently(file.id) }
          )
        }

        // Trashed Notes
        items(state.trash.notes, key = { it.id }) { note ->
          TrashItemRow(
            icon = Icons.Default.Description,
            iconTint = SafeAmber,
            title = note.title,
            subtitle = "Nota Segura • ${note.category}",
            onRestore = { viewModel.restoreNoteFromTrash(note) },
            onDeleteForever = { viewModel.deleteNotePermanently(note.id) }
          )
        }

        // Trashed Entries
        items(state.trash.entries, key = { it.id }) { entry ->
          TrashItemRow(
            icon = Icons.Default.VpnKey,
            iconTint = Color(0xFF8B5CF6),
            title = entry.title,
            subtitle = "${entry.type.displayName} • ${entry.folder}",
            onRestore = { viewModel.restoreItemFromTrash(entry) },
            onDeleteForever = { viewModel.deleteEntryPermanently(entry.id) }
          )
        }
      }
    }
  }

  // Confirm Empty Trash Dialog
  if (showEmptyConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showEmptyConfirmDialog = false },
      title = {
        Text("¿Vaciar papelera?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Text(
          text = "Esta acción triturará y sobrescribirá los archivos en el almacenamiento de forma permanente. No se podrán recuperar.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.emptyTrash()
            showEmptyConfirmDialog = false
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SafeRose)
        ) {
          Text("Triturar y Eliminar Todo")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEmptyConfirmDialog = false }) {
          Text("Cancelar")
        }
      }
    )
  }
}

@Composable
private fun TrashItemRow(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  onRestore: () -> Unit,
  onDeleteForever: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onRestore, modifier = Modifier.size(34.dp)) {
          Icon(Icons.Default.Restore, contentDescription = "Restaurar", tint = SafeEmerald, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onDeleteForever, modifier = Modifier.size(34.dp)) {
          Icon(Icons.Default.DeleteForever, contentDescription = "Eliminar permanentemente", tint = SafeRose, modifier = Modifier.size(20.dp))
        }
      }
    }
  }
}
