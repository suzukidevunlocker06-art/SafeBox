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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.VaultFileItem
import com.example.ui.DocumentFilter
import com.example.ui.SortOption
import com.example.ui.VaultUiState
import com.example.ui.VaultViewModel
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@Composable
fun DocumentsView(
  viewModel: VaultViewModel,
  state: VaultUiState,
  onImportClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showSortMenu by remember { mutableStateOf(false) }

  val query = state.searchQuery.trim().lowercase()

  val filteredByType = state.documents.filter { doc ->
    val ext = doc.extension.lowercase()
    when (state.documentFilter) {
      DocumentFilter.ALL -> true
      DocumentFilter.PDF -> ext == "pdf"
      DocumentFilter.TEXT -> ext in listOf("txt", "md", "json", "log", "xml", "csv")
      DocumentFilter.OFFICE -> ext in listOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt")
      DocumentFilter.OTHER -> ext !in listOf("pdf", "txt", "md", "json", "log", "xml", "csv", "doc", "docx", "xls", "xlsx", "ppt", "pptx")
    }
  }

  val filteredBySearch = filteredByType.filter { doc ->
    query.isEmpty() || doc.originalName.lowercase().contains(query) || doc.tag.lowercase().contains(query)
  }

  val sortedList = when (state.sortOption) {
    SortOption.DATE_DESC -> filteredBySearch.sortedByDescending { it.createdAt }
    SortOption.DATE_ASC -> filteredBySearch.sortedBy { it.createdAt }
    SortOption.NAME_ASC -> filteredBySearch.sortedBy { it.originalName.lowercase() }
    SortOption.NAME_DESC -> filteredBySearch.sortedByDescending { it.originalName.lowercase() }
    SortOption.SIZE_DESC -> filteredBySearch.sortedByDescending { it.sizeBytes }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Filter Chips
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(DocumentFilter.values()) { filter ->
          FilterChip(
            selected = state.documentFilter == filter,
            onClick = { viewModel.setDocumentFilter(filter) },
            label = { Text(filter.label, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SafePrimary,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      // Count and Sort Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${sortedList.size} documentos cifrados",
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
                .background(SafeEmerald.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = SafeEmerald,
                modifier = Modifier.size(38.dp)
              )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = if (query.isNotEmpty()) "Sin documentos encontrados" else "Sin documentos en la bóveda",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Importa archivos PDF, contratos, hojas de cálculo o notas de texto para protegerlos de miradas indiscretas.",
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
              Text("Importar Documentos")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(sortedList, key = { it.id }) { doc ->
            DocumentCard(
              item = doc,
              onClick = { viewModel.openViewer(doc) },
              onFavoriteToggle = { viewModel.toggleFileFavorite(doc.id) }
            )
          }
        }
      }
    }

    // FAB
    FloatingActionButton(
      onClick = onImportClick,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp),
      containerColor = SafePrimary,
      contentColor = Color.White
    ) {
      Icon(Icons.Default.Add, contentDescription = "Importar Documento")
    }
  }
}

@Composable
private fun DocumentCard(
  item: VaultFileItem,
  onClick: () -> Unit,
  onFavoriteToggle: () -> Unit
) {
  val ext = item.extension.lowercase()
  val iconColor = when (ext) {
    "pdf" -> SafeRose
    in listOf("txt", "md", "json", "log") -> SafePrimary
    in listOf("doc", "docx", "odt") -> Color(0xFF2563EB)
    in listOf("xls", "xlsx", "csv") -> SafeEmerald
    else -> SafeAmber
  }

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
          .background(iconColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (ext == "pdf") Icons.Default.PictureAsPdf else Icons.Default.Description,
          contentDescription = null,
          tint = iconColor,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.originalName,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = item.extension.uppercase().ifEmpty { "DOC" },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = iconColor
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "• ${item.getFormattedSize()} • ${item.getFormattedDate()}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
          )
        }
      }

      IconButton(
        onClick = onFavoriteToggle,
        modifier = Modifier.size(34.dp)
      ) {
        Icon(
          imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = "Favorito",
          tint = if (item.isFavorite) SafeAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
