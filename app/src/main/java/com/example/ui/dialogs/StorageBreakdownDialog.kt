package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultStorageStats
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import java.util.Locale

@Composable
fun StorageBreakdownDialog(
  stats: VaultStorageStats,
  onDismiss: () -> Unit
) {
  fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
      gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
      mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
      kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
      else -> "$bytes B"
    }
  }

  val total = stats.totalVaultBytes.coerceAtLeast(1L)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.PieChart,
          contentDescription = null,
          tint = SafePrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Uso de Almacenamiento",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Espacio total cifrado en SafeBox:",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
          text = formatSize(stats.totalVaultBytes),
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Progress bar
        LinearProgressIndicator(
          progress = 1.0f,
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = SafePrimary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        StorageItemRow(
          icon = Icons.Default.Image,
          name = "Fotos Cifradas",
          sizeStr = formatSize(stats.photosBytes),
          color = SafePrimary
        )

        StorageItemRow(
          icon = Icons.Default.Videocam,
          name = "Videos Cifrados",
          sizeStr = formatSize(stats.videosBytes),
          color = SafeRoseLocal
        )

        StorageItemRow(
          icon = Icons.Default.Description,
          name = "Documentos",
          sizeStr = formatSize(stats.documentsBytes),
          color = SafeEmerald
        )

        StorageItemRow(
          icon = Icons.Default.FolderZip,
          name = "Notas Seguras",
          sizeStr = formatSize(stats.notesBytes),
          color = SafeAmber
        )

        StorageItemRow(
          icon = Icons.Default.VpnKey,
          name = "Credenciales y Tarjetas",
          sizeStr = formatSize(stats.credentialsBytes),
          color = Color(0xFF8B5CF6)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Todos los archivos residen únicamente en el almacenamiento interno protegido de esta app, sin subidas externas.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
          lineHeight = 15.sp
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
      ) {
        Text("Entendido")
      }
    }
  )
}

private val SafeRoseLocal = Color(0xFFEF4444)

@Composable
private fun StorageItemRow(
  icon: ImageVector,
  name: String,
  sizeStr: String,
  color: Color
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.width(10.dp))
      Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }

    Text(
      text = sizeStr,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    )
  }
}
