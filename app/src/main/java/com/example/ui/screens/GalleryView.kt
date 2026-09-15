package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.VaultFileItem
import com.example.ui.SortOption
import com.example.ui.VaultUiState
import com.example.ui.VaultViewModel
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryView(
  viewModel: VaultViewModel,
  state: VaultUiState,
  category: FileCategory,
  onImportClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showSortMenu by remember { mutableStateOf(false) }

  val rawList = if (category == FileCategory.PHOTO) state.photos else state.videos
  val query = state.searchQuery.trim().lowercase()

  val filteredList = rawList.filter { item ->
    query.isEmpty() || item.originalName.lowercase().contains(query) || item.tag.lowercase().contains(query)
  }

  val sortedList = when (state.sortOption) {
    SortOption.DATE_DESC -> filteredList.sortedByDescending { it.createdAt }
    SortOption.DATE_ASC -> filteredList.sortedBy { it.createdAt }
    SortOption.NAME_ASC -> filteredList.sortedBy { it.originalName.lowercase() }
    SortOption.NAME_DESC -> filteredList.sortedByDescending { it.originalName.lowercase() }
    SortOption.SIZE_DESC -> filteredList.sortedByDescending { it.sizeBytes }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Sub-bar with count and sort menu
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (state.isSelectionMode) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.clearSelection() }) {
              Icon(Icons.Default.Close, contentDescription = "Cancelar selección")
            }
            Text(
              text = "${state.selectedFileIds.size} seleccionados",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.selectAll(sortedList.map { it.id }) }) {
              Icon(Icons.Default.SelectAll, contentDescription = "Seleccionar todos")
            }
            IconButton(onClick = { viewModel.deleteSelectedToTrash() }) {
              Icon(Icons.Default.Delete, contentDescription = "Mover a papelera", tint = SafeRose)
            }
          }
        } else {
          Text(
            text = "${sortedList.size} ${if (category == FileCategory.PHOTO) "fotos protegidas" else "videos protegidos"}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
          )

          Box {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showSortMenu = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = "Ordenar",
                modifier = Modifier.size(16.dp),
                tint = SafePrimary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = state.sortOption.label,
                fontSize = 12.sp,
                color = SafePrimary,
                fontWeight = FontWeight.Medium
              )
            }

            DropdownMenu(
              expanded = showSortMenu,
              onDismissRequest = { showSortMenu = false }
            ) {
              SortOption.values().forEach { option ->
                DropdownMenuItem(
                  text = { Text(option.label, fontSize = 13.sp) },
                  onClick = {
                    viewModel.setSortOption(option)
                    showSortMenu = false
                  }
                )
              }
            }
          }
        }
      }

      // Empty State
      if (sortedList.isEmpty()) {
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
                imageVector = if (category == FileCategory.PHOTO) Icons.Default.Image else Icons.Default.Videocam,
                contentDescription = null,
                tint = SafePrimary,
                modifier = Modifier.size(38.dp)
              )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = if (state.searchQuery.isNotEmpty()) "Sin resultados en la galería" else "No hay ${if (category == FileCategory.PHOTO) "fotos" else "videos"} en la bóveda",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Toca el botón '+' para importar y cifrar tus archivos privados con AES-256 de forma segura y local.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onImportClick,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Importar ${if (category == FileCategory.PHOTO) "Fotos" else "Videos"}")
            }
          }
        }
      } else {
        // Grid View
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(sortedList, key = { it.id }) { item ->
            val isSelected = state.selectedFileIds.contains(item.id)

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                  onClick = {
                    if (state.isSelectionMode) {
                      viewModel.toggleSelection(item.id)
                    } else {
                      viewModel.openViewer(item)
                    }
                  },
                  onLongClick = {
                    viewModel.toggleSelection(item.id)
                  }
                )
                .then(
                  if (isSelected) Modifier.border(2.dp, SafePrimary, RoundedCornerShape(12.dp))
                  else Modifier
                ),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
              Box(modifier = Modifier.fillMaxSize()) {
                // Placeholder / Encrypted Thumbnail background
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = if (category == FileCategory.PHOTO) Icons.Default.Image else Icons.Default.Videocam,
                    contentDescription = null,
                    tint = if (category == FileCategory.PHOTO) SafePrimary.copy(alpha = 0.6f) else Color(0xFFEF4444).copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = item.originalName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                  )
                  Text(
                    text = item.getFormattedSize(),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                  )
                }

                // Video badge
                if (category == FileCategory.VIDEO) {
                  Box(
                    modifier = Modifier
                      .align(Alignment.BottomEnd)
                      .padding(6.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color.Black.copy(alpha = 0.6f))
                      .padding(horizontal = 4.dp, vertical = 2.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                      Spacer(modifier = Modifier.width(2.dp))
                      Text("VIDEO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }

                // Favorite star
                if (item.isFavorite) {
                  Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorito",
                    tint = SafeAmber,
                    modifier = Modifier
                      .size(16.dp)
                      .align(Alignment.TopStart)
                      .padding(3.dp)
                  )
                }

                // Selection checkmark
                if (isSelected) {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(SafePrimary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.TopEnd
                  ) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = "Seleccionado",
                      tint = SafePrimary,
                      modifier = Modifier
                        .padding(6.dp)
                        .size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // FAB to import
    FloatingActionButton(
      onClick = onImportClick,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp),
      containerColor = SafePrimary,
      contentColor = Color.White
    ) {
      Icon(Icons.Default.Add, contentDescription = "Importar")
    }
  }
}
