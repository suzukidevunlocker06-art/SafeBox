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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultNoteItem
import com.example.ui.VaultUiState
import com.example.ui.VaultViewModel
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary

@Composable
fun NotesView(
  viewModel: VaultViewModel,
  state: VaultUiState,
  modifier: Modifier = Modifier
) {
  val query = state.searchQuery.trim().lowercase()

  val filteredNotes = state.notes.filter { note ->
    query.isEmpty() || note.title.lowercase().contains(query) ||
      note.content.lowercase().contains(query) ||
      note.category.lowercase().contains(query)
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Sub header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${filteredNotes.size} notas privadas",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
          fontWeight = FontWeight.Medium
        )
      }

      if (filteredNotes.isEmpty()) {
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
                .background(SafeAmber.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = SafeAmber,
                modifier = Modifier.size(38.dp)
              )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = if (query.isNotEmpty()) "Sin notas coincidentes" else "Bóveda de notas vacía",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Escribe pensamientos, códigos de recuperación, secretos personales o listas con cifrado de grado militar.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { viewModel.openNewNote() },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Crear Nueva Nota")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredNotes, key = { it.id }) { note ->
            NoteCard(
              note = note,
              onClick = { viewModel.openEditNote(note) },
              onFavoriteToggle = { viewModel.toggleNoteFavorite(note.id) }
            )
          }
        }
      }
    }

    // FAB
    FloatingActionButton(
      onClick = { viewModel.openNewNote() },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp),
      containerColor = SafePrimary,
      contentColor = Color.White
    ) {
      Icon(Icons.Default.Add, contentDescription = "Nueva Nota")
    }
  }
}

@Composable
private fun NoteCard(
  note: VaultNoteItem,
  onClick: () -> Unit,
  onFavoriteToggle: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(SafeAmber)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = note.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        IconButton(
          onClick = onFavoriteToggle,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Favorito",
            tint = if (note.isFavorite) SafeAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            modifier = Modifier.size(19.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = if (note.content.isBlank()) "Sin contenido adicional" else note.content,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 17.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SafeAmber.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = note.category,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = SafeAmber
          )
        }

        Text(
          text = "${note.wordCount()} palabras • ${note.getFormattedDate()}",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
      }
    }
  }
}
