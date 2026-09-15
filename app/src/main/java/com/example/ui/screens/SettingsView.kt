package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VaultUiState
import com.example.ui.VaultViewModel
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafePrimary
import com.example.ui.theme.SafeRose

@Composable
fun SettingsView(
  viewModel: VaultViewModel,
  state: VaultUiState,
  modifier: Modifier = Modifier
) {
  var showTimeoutDialog by remember { mutableStateOf(false) }
  var showThemeDialog by remember { mutableStateOf(false) }
  var showPrivacyDialog by remember { mutableStateOf(false) }

  val settings = state.settings

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    Text(
      text = "Configuración y Seguridad",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = "Parámetros criptográficos y privacidad de SafeBox",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // SECTION: SEGURIDAD
    SettingsSectionHeader("SEGURIDAD DE LA BÓVEDA")

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(vertical = 4.dp)) {
        // Cambiar PIN
        SettingsRowClickable(
          icon = Icons.Default.LockReset,
          iconColor = SafePrimary,
          title = "Cambiar PIN Maestro",
          subtitle = "Actualiza tu código y recifra los datos",
          onClick = { viewModel.openChangePin() }
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        // Biometría
        SettingsRowSwitch(
          icon = Icons.Default.Fingerprint,
          iconColor = SafeEmerald,
          title = "Desbloqueo con Huella / Biometría",
          subtitle = "Usa Android Keystore para acceso rápido",
          checked = settings.biometricsEnabled,
          onCheckedChange = {
            viewModel.updateSettings(settings.copy(biometricsEnabled = it))
          }
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        // Tiempo de bloqueo automático
        val timeoutLabel = when (settings.autoLockSeconds) {
          0 -> "Inmediatamente"
          30 -> "30 segundos"
          60 -> "1 minuto"
          300 -> "5 minutos"
          600 -> "10 minutos"
          else -> "Desactivado"
        }
        SettingsRowClickable(
          icon = Icons.Default.Timer,
          iconColor = SafeAmber,
          title = "Tiempo de Bloqueo Automático",
          subtitle = timeoutLabel,
          onClick = { showTimeoutDialog = true }
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        // Bloquear al pasar a segundo plano
        SettingsRowSwitch(
          icon = Icons.Default.Lock,
          iconColor = SafePrimary,
          title = "Bloquear al pasar a segundo plano",
          subtitle = "Cierra la bóveda al salir o cambiar de app",
          checked = settings.lockOnBackground,
          onCheckedChange = {
            viewModel.updateSettings(settings.copy(lockOnBackground = it))
          }
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        // Protección contra capturas (FLAG_SECURE)
        SettingsRowSwitch(
          icon = Icons.Default.VisibilityOff,
          iconColor = SafeRose,
          title = "Protección contra capturas de pantalla",
          subtitle = "Impide capturas y grabaciones del sistema (FLAG_SECURE)",
          checked = settings.screenCaptureProtection,
          onCheckedChange = {
            viewModel.updateSettings(settings.copy(screenCaptureProtection = it))
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION: APARIENCIA Y ALMACENAMIENTO
    SettingsSectionHeader("PREFERENCIAS Y ALMACENAMIENTO")

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(vertical = 4.dp)) {
        // Tema
        val themeLabel = when (settings.themeMode) {
          "LIGHT" -> "Tema Claro"
          "SYSTEM" -> "Seguir Sistema"
          else -> "Tema Oscuro (Recomendado)"
        }
        SettingsRowClickable(
          icon = Icons.Default.Brightness4,
          iconColor = SafePrimary,
          title = "Tema de la Aplicación",
          subtitle = themeLabel,
          onClick = { showThemeDialog = true }
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        // Uso de almacenamiento
        SettingsRowClickable(
          icon = Icons.Default.PieChart,
          iconColor = SafeEmerald,
          title = "Uso de Almacenamiento",
          subtitle = "Desglose de fotos, videos, docs y notas",
          onClick = { viewModel.openStorageDialog() }
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        // Copia de seguridad y restauración
        SettingsRowClickable(
          icon = Icons.Default.Save,
          iconColor = Color(0xFF8B5CF6),
          title = "Copia de Seguridad y Restauración",
          subtitle = "Exporta o importa tu contenedor cifrado",
          onClick = { viewModel.openBackup() }
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION: ACERCA DE Y PRIVACIDAD
    SettingsSectionHeader("PRIVACIDAD Y TRANSPARENCIA")

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(vertical = 4.dp)) {
        SettingsRowClickable(
          icon = Icons.Default.Shield,
          iconColor = SafeEmerald,
          title = "Principios de Privacidad SafeBox",
          subtitle = "Sin IA • Sin Servidores • Sin Publicidad",
          onClick = { showPrivacyDialog = true }
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SafePrimary.copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Info, contentDescription = null, tint = SafePrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(text = "Versión de SafeBox", fontSize = 14.sp, fontWeight = FontWeight.Medium)
              Text(text = "SafeBox v2.0.0 (Release Estable)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
          }
          Text(text = "2.0.0", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SafePrimary)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Botón de bloqueo manual
    Button(
      onClick = { viewModel.lockVault() },
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = SafeRose)
    ) {
      Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Bloquear Bóveda Ahora", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(96.dp))
  }

  // Auto Lock Timeout Dialog
  if (showTimeoutDialog) {
    val options = listOf(
      0 to "Inmediatamente al salir",
      30 to "30 segundos",
      60 to "1 minuto (Recomendado)",
      300 to "5 minutos",
      600 to "10 minutos",
      -1 to "Desactivado (Solo manual)"
    )

    AlertDialog(
      onDismissRequest = { showTimeoutDialog = false },
      title = { Text("Tiempo de Bloqueo Automático", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
      text = {
        Column {
          options.forEach { (seconds, label) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.updateSettings(settings.copy(autoLockSeconds = seconds))
                  showTimeoutDialog = false
                }
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = settings.autoLockSeconds == seconds,
                onClick = {
                  viewModel.updateSettings(settings.copy(autoLockSeconds = seconds))
                  showTimeoutDialog = false
                }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = label, fontSize = 14.sp)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showTimeoutDialog = false }) {
          Text("Cerrar")
        }
      }
    )
  }

  // Theme Dialog
  if (showThemeDialog) {
    val themeOptions = listOf(
      "DARK" to "Tema Oscuro Elegante (Por defecto)",
      "LIGHT" to "Tema Claro",
      "SYSTEM" to "Seguir Ajustes del Sistema"
    )

    AlertDialog(
      onDismissRequest = { showThemeDialog = false },
      title = { Text("Tema de SafeBox", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
      text = {
        Column {
          themeOptions.forEach { (code, label) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.updateSettings(settings.copy(themeMode = code))
                  showThemeDialog = false
                }
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = settings.themeMode == code,
                onClick = {
                  viewModel.updateSettings(settings.copy(themeMode = code))
                  showThemeDialog = false
                }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = label, fontSize = 14.sp)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showThemeDialog = false }) {
          Text("Cerrar")
        }
      }
    )
  }

  // Privacy Dialog
  if (showPrivacyDialog) {
    AlertDialog(
      onDismissRequest = { showPrivacyDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Shield, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Privacidad SafeBox 2.0", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(
            text = "SafeBox está construida bajo el principio estricto de Conocimiento Cero (Zero-Knowledge):",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(10.dp))
          PrivacyBullet("🔒 Cifrado Local Robusto", "Cifrado simétrico AES-256 con derivación PBKDF2-HMAC-SHA256 y vector de inicialización seguro para cada archivo.")
          PrivacyBullet("🚫 Cero Inteligencia Artificial", "Tus datos nunca son analizados ni procesados por modelos de IA.")
          PrivacyBullet("🛡️ Sin Servidores Ni Nube", "Tus fotos, videos, documentos y notas residen única y exclusivamente en tu dispositivo.")
          PrivacyBullet("🎯 Sin Publicidad Ni Rastreadores", "Ningún SDK publicitario ni telemetría comercial.")
          PrivacyBullet("🔑 Sin Cuenta Obligatoria", "No requieres correos, registros ni contraseñas externas.")
        }
      },
      confirmButton = {
        Button(
          onClick = { showPrivacyDialog = false },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SafePrimary)
        ) {
          Text("Comprendido")
        }
      }
    )
  }
}

@Composable
private fun PrivacyBullet(title: String, desc: String) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafeEmerald)
    Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 15.sp)
  }
}

@Composable
private fun SettingsSectionHeader(title: String) {
  Text(
    text = title,
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 6.dp)
  )
}

@Composable
private fun SettingsRowClickable(
  icon: ImageVector,
  iconColor: Color,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(iconColor.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
      Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
    }

    Icon(
      imageVector = Icons.Default.ChevronRight,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
private fun SettingsRowSwitch(
  icon: ImageVector,
  iconColor: Color,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(iconColor.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
      Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SafePrimary)
    )
  }
}
