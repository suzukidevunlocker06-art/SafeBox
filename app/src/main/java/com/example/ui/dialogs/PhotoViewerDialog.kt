package com.example.ui.dialogs

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.model.VaultFileItem
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeRose
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerDialog(
  item: VaultFileItem,
  decryptedBytes: ByteArray?,
  tempFile: File?,
  onDismiss: () -> Unit,
  onToggleFavorite: () -> Unit,
  onDelete: () -> Unit
) {
  val context = LocalContext.current
  var scale by remember { mutableFloatStateOf(1f) }
  var offset by remember { mutableStateOf(Offset.Zero) }
  var showInfoSheet by remember { mutableStateOf(false) }

  val transformState = rememberTransformableState { zoomChange, panChange, _ ->
    scale = (scale * zoomChange).coerceIn(1f, 5f)
    if (scale == 1f) {
      offset = Offset.Zero
    } else {
      offset += panChange
    }
  }

  val bitmap = remember(decryptedBytes) {
    decryptedBytes?.let { bytes ->
      try {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
      } catch (e: Exception) {
        null
      }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
      // Photo Canvas
      Box(
        modifier = Modifier
          .fillMaxSize()
          .transformable(state = transformState),
        contentAlignment = Alignment.Center
      ) {
        if (bitmap != null) {
          Image(
            bitmap = bitmap,
            contentDescription = item.originalName,
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
              ),
            contentScale = ContentScale.Fit
          )
        } else {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Descifrando con AES-256...",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 13.sp
            )
          }
        }
      }

      // Top Toolbar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .background(Color.Black.copy(alpha = 0.55f))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = item.originalName,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        IconButton(onClick = onToggleFavorite) {
          Icon(
            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Favorito",
            tint = if (item.isFavorite) SafeAmber else Color.White
          )
        }
      }

      // Bottom Toolbar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .background(Color.Black.copy(alpha = 0.6f))
          .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Share
        IconButton(
          onClick = {
            if (tempFile != null && tempFile.exists()) {
              try {
                val uri = FileProvider.getUriForFile(
                  context,
                  "com.example.fileprovider",
                  tempFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                  type = item.mimeType
                  putExtra(Intent.EXTRA_STREAM, uri)
                  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir archivo seguro"))
              } catch (e: Exception) {
                // ignore
              }
            }
          }
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White)
            Text("Compartir", color = Color.White, fontSize = 11.sp)
          }
        }

        // Info
        IconButton(onClick = { showInfoSheet = true }) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = "Información", tint = Color.White)
            Text("Detalles", color = Color.White, fontSize = 11.sp)
          }
        }

        // Delete
        IconButton(onClick = onDelete) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = SafeRose)
            Text("Papelera", color = SafeRose, fontSize = 11.sp)
          }
        }
      }
    }

    // Info Modal Bottom Sheet
    if (showInfoSheet) {
      ModalBottomSheet(
        onDismissRequest = { showInfoSheet = false }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
          Text(
            text = "Información del archivo",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(14.dp))

          InfoRow("Nombre original", item.originalName)
          InfoRow("Tamaño", item.getFormattedSize())
          InfoRow("Tipo MIME", item.mimeType)
          InfoRow("Fecha de importación", item.getFormattedDate())
          InfoRow("Algoritmo", "AES-256-CBC (PKCS5Padding)")
          InfoRow("Hash SHA-256", item.sha256, isMonospace = true)

          Spacer(modifier = Modifier.height(16.dp))

          TextButton(
            onClick = { showInfoSheet = false },
            modifier = Modifier.align(Alignment.End)
          ) {
            Text("Cerrar")
          }
        }
      }
    }
  }
}

@Composable
private fun InfoRow(label: String, value: String, isMonospace: Boolean = false) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(
      text = label,
      fontSize = 11.sp,
      color = Color.Gray,
      fontWeight = FontWeight.Medium
    )
    Text(
      text = value,
      fontSize = 13.sp,
      fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
      fontWeight = FontWeight.Normal
    )
  }
}
