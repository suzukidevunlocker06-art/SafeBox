package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ImportProgress
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary

@Composable
fun ImportProgressDialog(
  progress: ImportProgress
) {
  val ratio = if (progress.total > 0) progress.current.toFloat() / progress.total.toFloat() else 0f

  AlertDialog(
    onDismissRequest = { /* non-cancellable during cryptographic write */ },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = SafeEmerald,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Protegiendo Archivos",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = progress.currentFileName,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        LinearProgressIndicator(
          progress = ratio,
          modifier = Modifier.fillMaxWidth(),
          color = SafePrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Cifrando con AES-256: ${progress.current} de ${progress.total}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
      }
    },
    confirmButton = {}
  )
}
