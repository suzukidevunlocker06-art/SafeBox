package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultNoteItem
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@Composable
fun NoteEditorDialog(
  initialNote: VaultNoteItem?,
  onDismiss: () -> Unit,
  onSave: (title: String, content: String, category: String, isFavorite: Boolean) -> Unit,
  onDelete: (() -> Unit)? = null
) {
  var title by remember { mutableStateOf(initialNote?.title ?: "") }
  var content by remember { mutableStateOf(initialNote?.content ?: "") }
  var category by remember { mutableStateOf(initialNote?.category ?: "General") }
  var isFavorite by remember { mutableStateOf(initialNote?.isFavorite ?: false) }

  val wordCount = if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
  val charCount = content.length

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = SafeAmber,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (initialNote != null) "Editar Nota" else "Nueva Nota Segura",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
        }

        IconButton(onClick = { isFavorite = !isFavorite }) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Favorito",
            tint = if (isFavorite) SafeAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
          )
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Título de la nota") },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Categoría / Etiqueta") },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Contenido cifrado...") },
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Words and character counter
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "$wordCount palabras • $charCount caracteres",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
          )
          Text(
            text = "Cifrado AES-256",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = SafeAmber
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(title, content, category, isFavorite)
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
      ) {
        Text("Guardar Nota")
      }
    },
    dismissButton = {
      Row {
        if (initialNote != null && onDelete != null) {
          TextButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = SafeRose, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Eliminar", color = SafeRose)
          }
        }
        TextButton(onClick = onDismiss) {
          Text("Cancelar")
        }
      }
    }
  )
}
