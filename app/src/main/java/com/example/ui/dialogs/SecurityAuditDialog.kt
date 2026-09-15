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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SecurityAuditSummary
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@Composable
fun SecurityAuditDialog(
  audit: SecurityAuditSummary,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = SafePrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Auditoría de Seguridad",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Score Header
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = when {
              audit.securityScorePercent >= 80 -> SafeEmerald.copy(alpha = 0.12f)
              audit.securityScorePercent >= 50 -> SafeAmber.copy(alpha = 0.12f)
              else -> SafeRose.copy(alpha = 0.12f)
            }
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Puntuación de la Bóveda",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
              )
              Text(
                text = "${audit.securityScorePercent} / 100",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                  audit.securityScorePercent >= 80 -> SafeEmerald
                  audit.securityScorePercent >= 50 -> SafeAmber
                  else -> SafeRose
                }
              )
            }

            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                  when {
                    audit.securityScorePercent >= 80 -> SafeEmerald
                    audit.securityScorePercent >= 50 -> SafeAmber
                    else -> SafeRose
                  }
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (audit.securityScorePercent >= 80) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(26.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Breakdown items
        AuditRow(
          title = "Total de contraseñas",
          value = "${audit.totalPasswords}",
          isWarning = false
        )
        Spacer(modifier = Modifier.height(8.dp))

        AuditRow(
          title = "Contraseñas débiles",
          value = "${audit.weakPasswordsCount}",
          isWarning = audit.weakPasswordsCount > 0
        )
        Spacer(modifier = Modifier.height(8.dp))

        AuditRow(
          title = "Contraseñas reutilizadas",
          value = "${audit.reusedPasswordsCount}",
          isWarning = audit.reusedPasswordsCount > 0
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Recomendaciones de SafeBox:",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = if (audit.weakPasswordsCount == 0 && audit.reusedPasswordsCount == 0) {
            "✓ Todas tus contraseñas cumplen los estándares criptográficos recomendados."
          } else {
            "• Utiliza el generador integrado para sustituir contraseñas repetidas o con menos de 12 caracteres."
          },
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          lineHeight = 16.sp
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Entendido")
      }
    }
  )
}

@Composable
private fun AuditRow(title: String, value: String, isWarning: Boolean) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    )
    Text(
      text = value,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = if (isWarning) SafeRose else SafeEmerald
    )
  }
}
