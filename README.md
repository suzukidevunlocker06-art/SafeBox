# SafeBox 2.0

> **Bóveda digital integral, gestor de credenciales y almacén seguro de archivos para Android con cifrado local de conocimiento cero.**

SafeBox 2.0 es una aplicación móvil nativa desarrollada con Kotlin y Jetpack Compose diseñada para proteger información altamente confidencial: fotos, videos, documentos, notas privadas, credenciales de inicio de sesión, tarjetas bancarias y documentos de identidad. Todos los datos se cifran localmente en el dispositivo utilizando estándares criptográficos recomendados por Android (AES-256-CBC con Android Keystore y PBKDF2WithHmacSHA256 con salt dinámico), garantizando que nadie excepto el propietario del PIN o huella biométrica pueda acceder a la información.

---

## 🛡️ Principios Fundamentales

- **Sin Inteligencia Artificial:** Cero algoritmos opacos, todo el control es determinista y transparente.
- **Sin Publicidad:** Experiencia limpia y libre de rastreadores o anuncios dentro de la bóveda.
- **Sin Cuenta Obligatoria:** No requiere registro, correo electrónico ni número telefónico.
- **Sin Servidor Obligatorio:** Funcionamiento 100% offline y local.
- **Sin Subida Automática de Archivos:** Tus datos nunca abandonan tu dispositivo sin tu consentimiento explícito.

---

## 🔐 1. Seguridad Avanzada

- **PIN Seguro y Desbloqueo Biométrico:** Acceso protegido por PIN maestro y soporte de huella dactilar mediante `BiometricPrompt` y Android Keystore.
- **Cifrado Fuerte:** Todos los datos y archivos privados se cifran con AES-256 antes de guardarse en el almacenamiento interno privado.
- **Protección contra Capturas de Pantalla:** Activación de `FLAG_SECURE` en todas las pantallas sensibles de la bóveda.
- **Bloqueo Automático Configurable:** Opciones de bloqueo inmediato, tras 30 segundos, 1 minuto, 5 minutos, 10 minutos o al pasar la aplicación a segundo plano (`onPause`/`onStop`).
- **Control contra Fuerza Bruta:** Registro local de intentos fallidos con retardo progresivo y bloqueo temporal tras fallos consecutivos.
- **Trituración Segura de Archivos (File Shredding):** Sobrescritura criptográfica en múltiples pasadas (ceros y bytes aleatorios generados con `SecureRandom`) antes de la eliminación en disco.

---

## 📁 2. Funcionalidades de la Bóveda v2.0

- 🖼️ **Galería Multimedia Privada:**
  - Importación y cifrado seguro de fotos personales y familiares.
  - Visor interactivo con zoom gestual, rotación y soporte para compartir seguro vía `FileProvider`.
- 🎥 **Videos Ocultos:**
  - Cifrado seguro de videos confidenciales.
  - Reproductor integrado en entorno protegido con controles de reproducción.
- 📄 **Documentos Confidenciales:**
  - Almacén cifrado para archivos PDF, TXT, JSON, DOCX, XLS y hojas de cálculo.
  - Visor de texto integrado con búsqueda y opciones para copiar contenido al portapapeles.
- 📝 **Notas Privadas Protegidas:**
  - Creación, edición, categorización y conteo de palabras en tiempo real.
  - Búsqueda instantánea dentro del cuerpo de las notas.
- 🔑 **Cuentas y Contraseñas:**
  - Gestor de credenciales con generador de contraseñas de alta entropía.
  - Auditoría de seguridad con detección de contraseñas débiles o reutilizadas.
  - Copia rápida al portapapeles con borrado automático tras 30 segundos.
- 💳 **Tarjetas de Pago e Identidades:**
  - Almacenamiento seguro de tarjetas con validación y documentos de identidad.
- 🗑️ **Papelera de Reciclaje Segura:**
  - Los archivos eliminados se mueven temporalmente a la papelera con retención configurable de 30 días.
  - Opciones para restaurar a su ubicación original o triturar definitivamente.
- ⭐ **Favoritos y Búsqueda Global:**
  - Marcado de favoritos en todas las categorías (fotos, documentos, notas, contraseñas).
  - Búsqueda global en tiempo real a través de toda la bóveda.
- 💾 **Copias de Seguridad Cifradas:**
  - Exportación de respaldo cifrado con AES-256 en formato JSON.
  - Restauración completa verificando integridad.

---

## 💻 Tecnologías Utilizadas

- **Lenguaje:** Kotlin 2.2+
- **Framework UI:** Jetpack Compose (Material Design 3)
- **Criptografía:** `javax.crypto` (AES-256/CBC/PKCS5Padding, PBKDF2WithHmacSHA256, Android Keystore, SecureRandom)
- **Biometría:** `androidx.biometric:biometric` (`BiometricPrompt`)
- **Arquitectura:** MVVM (Model-View-ViewModel) con StateFlow y corrutinas
- **Gestión de Dependencias:** Gradle Version Catalogs (`gradle/libs.versions.toml`)
- **Compilación:** Gradle 9+ con Android Gradle Plugin 9.1+
- **Pruebas:** JUnit 4, Robolectric, Roborazzi Screenshot Testing

---

## ⚙️ Requisitos para Compilar

1. **Java Development Kit (JDK):** Versión 17 o 21 (Temurin u OpenJDK).
2. **Android SDK:**
   - `compileSdk`: 36
   - `minSdk`: 24 (Android 7.0 Nougat o superior)
   - `targetSdk`: 36
3. **Android Studio:** Ladybug (2024.2) o superior.
4. **Sistema Operativo:** Linux, macOS o Windows.

---

## 🚀 Instrucciones para Ejecutar el Proyecto

### 1. Clonar el repositorio
```bash
git clone https://github.com/<tu-usuario>/SafeBox.git
cd SafeBox
```

### 2. Dar permisos de ejecución al wrapper de Gradle (Linux / macOS)
```bash
chmod +x gradlew
```

### 3. Ejecutar las pruebas unitarias
```bash
./gradlew testDebugUnitTest
```

### 4. Instalar y ejecutar en un dispositivo o emulador conectado
```bash
./gradlew installDebug
```

---

## 📦 Instrucciones para Generar el APK

### Generar APK de Desarrollo (Debug APK)
```bash
./gradlew assembleDebug
```
El archivo APK generado se encontrará en:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Generar APK de Producción (Release APK)
```bash
export KEYSTORE_PATH="/ruta/a/tu/upload-keystore.jks"
export STORE_PASSWORD="tu_password_almacen"
export KEY_PASSWORD="tu_password_clave"

./gradlew assembleRelease
```
El archivo se generará en:
```
app/build/outputs/apk/release/app-release.apk
```

---

## 🔒 Privacidad y Seguridad

- **Sin Telemetría ni Rastreadores:** SafeBox no recopila ni transmite ningún dato analítico ni información de uso.
- **Sin Dependencias de Servidores Externos:** No requiere registro en la nube de terceros; el control absoluto reside en el usuario.
- **Protección de Secretos en el Repositorio:** Este repositorio excluye estrictamente mediante `.gitignore` cualquier clave de firma (`.keystore`, `.jks`), credenciales locales (`local.properties`), variables de entorno (`.env`) o claves API.
- **Destrucción de Claves en Memoria:** Al presionar "Bloquear" o al pasar a segundo plano, la clave activa se destruye en memoria.

---

## 🏷️ Versión
- **Versión:** `v2.0.0`
- **Código de Versión:** `2`
