package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.VaultEntry
import com.example.data.model.VaultFileItem
import com.example.data.model.VaultNoteItem
import com.example.ui.VaultUiState
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchDialog(
  state: VaultUiState,
  onDismiss: () -> Unit,
  onSelectFile: (VaultFileItem) -> Unit,
  onSelectNote: (VaultNoteItem) -> Unit,
  onSelectEntry: (VaultEntry) -> Unit
) {
  var query by remember { mutableStateOf("") }
  val q = query.trim().lowercase()

  val matchedPhotos = if (q.isEmpty()) emptyList() else state.photos.filter {
    it.originalName.lowercase().contains(q) || it.tag.lowercase().contains(q)
  }
  val matchedVideos = if (q.isEmpty()) emptyList() else state.videos.filter {
    it.originalName.lowercase().contains(q) || it.tag.lowercase().contains(q)
  }
  val matchedDocs = if (q.isEmpty()) emptyList() else state.documents.filter {
    it.originalName.lowercase().contains(q) || it.tag.lowercase().contains(q) || it.extension.lowercase().contains(q)
  }
  val matchedNotes = if (q.isEmpty()) emptyList() else state.notes.filter {
    it.title.lowercase().contains(q) || it.content.lowercase().contains(q) || it.category.lowercase().contains(q)
  }
  val matchedEntries = if (q.isEmpty()) emptyList() else state.items.filter {
    it.title.lowercase().contains(q) || it.folder.lowercase().contains(q) || it.getPreviewText().lowercase().contains(q)
  }

  val totalMatches = matchedPhotos.size + matchedVideos.size + matchedDocs.size + matchedNotes.size + matchedEntries.size

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        // Top search bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar en toda la bóveda...", fontSize = 14.sp) },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = null, tint = SafePrimary)
            },
            trailingIcon = {
              if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f)
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (q.isNotEmpty()) {
          Text(
            text = "$totalMatches resultados encontrados localmente",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (q.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Buscador Global Seguro",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
              )
              Text(
                text = "Busca al instante en fotos, videos, documentos, notas y credenciales cifradas.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
              )
            }
          }
        } else if (totalMatches == 0) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No se encontraron resultados para '$query'",
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
              fontSize = 14.sp
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Photos
            if (matchedPhotos.isNotEmpty()) {
              item { SectionHeader("Fotos (${matchedPhotos.size})") }
              items(matchedPhotos) { photo ->
                SearchResultRow(
                  icon = Icons.Default.Image,
                  iconTint = SafePrimary,
                  title = photo.originalName,
                  subtitle = "${photo.getFormattedSize()} • ${photo.getFormattedDate()}",
                  onClick = { onDismiss(); onSelectFile(photo) }
                )
              }
            }

            // Videos
            if (matchedVideos.isNotEmpty()) {
              item { SectionHeader("Videos (${matchedVideos.size})") }
              items(matchedVideos) { video ->
                SearchResultRow(
                  icon = Icons.Default.Videocam,
                  iconTint = Color(0xFFEF4444),
                  title = video.originalName,
                  subtitle = "${video.getFormattedSize()} • ${video.getFormattedDate()}",
                  onClick = { onDismiss(); onSelectFile(video) }
                )
              }
            }

            // Documents
            if (matchedDocs.isNotEmpty()) {
              item { SectionHeader("Documentos (${matchedDocs.size})") }
              items(matchedDocs) { doc ->
                SearchResultRow(
                  icon = Icons.Default.Description,
                  iconTint = SafeEmerald,
                  title = doc.originalName,
                  subtitle = "${doc.getFormattedSize()} • ${doc.extension.uppercase()}",
                  onClick = { onDismiss(); onSelectFile(doc) }
                )
              }
            }

            // Notes
            if (matchedNotes.isNotEmpty()) {
              item { SectionHeader("Notas Seguras (${matchedNotes.size})") }
              items(matchedNotes) { note ->
                SearchResultRow(
                  icon = Icons.Default.Description,
                  iconTint = SafeAmber,
                  title = note.title,
                  subtitle = "${note.category} • ${note.getFormattedDate()}",
                  onClick = { onDismiss(); onSelectNote(note) }
                )
              }
            }

            // Accounts
            if (matchedEntries.isNotEmpty()) {
              item { SectionHeader("Cuentas y Credenciales (${matchedEntries.size})") }
              items(matchedEntries) { entry ->
                SearchResultRow(
                  icon = Icons.Default.VpnKey,
                  iconTint = Color(0xFF8B5CF6),
                  title = entry.title,
                  subtitle = "${entry.folder} • ${entry.getPreviewText()}",
                  onClick = { onDismiss(); onSelectEntry(entry) }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    fontSize = 13.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 4.dp)
  )
}

@Composable
private fun SearchResultRow(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
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
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
