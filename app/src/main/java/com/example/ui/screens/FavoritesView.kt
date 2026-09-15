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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun FavoritesView(
  viewModel: VaultViewModel,
  state: VaultUiState,
  modifier: Modifier = Modifier
) {
  val favPhotos = state.photos.filter { it.isFavorite }
  val favVideos = state.videos.filter { it.isFavorite }
  val favDocs = state.documents.filter { it.isFavorite }
  val favNotes = state.notes.filter { it.isFavorite }
  val favEntries = state.items.filter { it.isFavorite }

  val totalFavs = favPhotos.size + favVideos.size + favDocs.size + favNotes.size + favEntries.size

  Column(modifier = modifier.fillMaxSize()) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(Icons.Default.Star, contentDescription = null, tint = SafeAmber, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Elementos Favoritos ($totalFavs)",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
    }

    if (totalFavs == 0) {
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
              .background(SafeAmber.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = SafeAmber,
              modifier = Modifier.size(38.dp)
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Sin favoritos guardados",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Toca la estrella en cualquier foto, video, documento, nota o contraseña para fijarlo aquí.",
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
        // Photos
        items(favPhotos, key = { it.id }) { photo ->
          FavItemCard(
            icon = Icons.Default.Image,
            iconTint = SafePrimary,
            title = photo.originalName,
            subtitle = "Foto • ${photo.getFormattedSize()}",
            onClick = { viewModel.openViewer(photo) },
            onUnfavorite = { viewModel.toggleFileFavorite(photo.id) }
          )
        }

        // Videos
        items(favVideos, key = { it.id }) { video ->
          FavItemCard(
            icon = Icons.Default.Videocam,
            iconTint = Color(0xFFEF4444),
            title = video.originalName,
            subtitle = "Video • ${video.getFormattedSize()}",
            onClick = { viewModel.openViewer(video) },
            onUnfavorite = { viewModel.toggleFileFavorite(video.id) }
          )
        }

        // Docs
        items(favDocs, key = { it.id }) { doc ->
          FavItemCard(
            icon = Icons.Default.Description,
            iconTint = SafeEmerald,
            title = doc.originalName,
            subtitle = "Documento • ${doc.getFormattedSize()}",
            onClick = { viewModel.openViewer(doc) },
            onUnfavorite = { viewModel.toggleFileFavorite(doc.id) }
          )
        }

        // Notes
        items(favNotes, key = { it.id }) { note ->
          FavItemCard(
            icon = Icons.Default.Description,
            iconTint = SafeAmber,
            title = note.title,
            subtitle = "Nota • ${note.category}",
            onClick = { viewModel.openEditNote(note) },
            onUnfavorite = { viewModel.toggleNoteFavorite(note.id) }
          )
        }

        // Entries
        items(favEntries, key = { it.id }) { entry ->
          FavItemCard(
            icon = Icons.Default.VpnKey,
            iconTint = Color(0xFF8B5CF6),
            title = entry.title,
            subtitle = "${entry.type.displayName} • ${entry.folder}",
            onClick = { viewModel.openEditEntry(entry) },
            onUnfavorite = { viewModel.toggleEntryFavorite(entry.id) }
          )
        }
      }
    }
  }
}

@Composable
private fun FavItemCard(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  onUnfavorite: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
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

      IconButton(onClick = onUnfavorite, modifier = Modifier.size(34.dp)) {
        Icon(Icons.Default.Star, contentDescription = "Quitar favorito", tint = SafeAmber, modifier = Modifier.size(20.dp))
      }
    }
  }
}
