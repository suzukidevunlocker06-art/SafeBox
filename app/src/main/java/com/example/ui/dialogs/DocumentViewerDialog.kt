package com.example.ui.dialogs

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.VaultFileItem
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose
import java.io.File

@Composable
fun DocumentViewerDialog(
  item: VaultFileItem,
  decryptedBytes: ByteArray?,
  tempFile: File?,
  onDismiss: () -> Unit,
  onToggleFavorite: () -> Unit,
  onDelete: () -> Unit,
  onCopyContent: ((String) -> Unit)? = null
) {
  val context = LocalContext.current
  val isTextDocument = item.extension.lowercase() in listOf("txt", "json", "xml", "csv", "md", "log", "html")
  val textContent = remember(decryptedBytes) {
    if (isTextDocument && decryptedBytes != null) {
      try {
        String(decryptedBytes, Charsets.UTF_8)
      } catch (e: Exception) {
        null
      }
    } else null
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(SafeEmerald.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = item.originalName,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
            Text(
              text = "${item.getFormattedSize()} • ${item.extension.uppercase()}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
          }
        }

        IconButton(onClick = onToggleFavorite) {
          Icon(
            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Favorito",
            tint = if (item.isFavorite) SafeAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
          )
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        if (textContent != null) {
          Text(
            text = "Vista previa del texto descifrado:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
              .padding(10.dp)
              .verticalScroll(rememberScrollState())
          ) {
            Text(
              text = textContent,
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          if (onCopyContent != null) {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
              onClick = { onCopyContent(textContent) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Copiar texto descifrado", fontSize = 12.sp)
            }
          }
        } else {
          // Non-text document metadata
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
              .padding(12.dp)
          ) {
            Column {
              Text(
                text = "Documento cifrado con AES-256",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SafeEmerald
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(text = "Fecha: ${item.getFormattedDate()}", fontSize = 12.sp)
              Text(text = "Tamaño: ${item.getFormattedSize()}", fontSize = 12.sp)
              Text(text = "MIME: ${item.mimeType}", fontSize = 12.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "SHA-256: ${item.sha256}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Open & Share actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              if (tempFile != null && tempFile.exists()) {
                try {
                  val uri = FileProvider.getUriForFile(context, "com.example.fileprovider", tempFile)
                  val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, item.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                  }
                  context.startActivity(intent)
                } catch (e: Exception) {
                  // ignore
                }
              }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
          ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Abrir", fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = {
              if (tempFile != null && tempFile.exists()) {
                try {
                  val uri = FileProvider.getUriForFile(context, "com.example.fileprovider", tempFile)
                  val intent = Intent(Intent.ACTION_SEND).apply {
                    type = item.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                  }
                  context.startActivity(Intent.createChooser(intent, "Compartir documento seguro"))
                } catch (e: Exception) {
                  // ignore
                }
              }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Compartir", fontSize = 12.sp)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Cerrar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDelete) {
        Icon(Icons.Default.Delete, contentDescription = null, tint = SafeRose, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Papelera", color = SafeRose)
      }
    }
  )
}
