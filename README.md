# SafeBox

> **Caja fuerte digital y gestor seguro de credenciales para Android con cifrado local de conocimiento cero.**

SafeBox es una aplicación móvil nativa desarrollada con Kotlin y Jetpack Compose diseñada para proteger información altamente confidencial: contraseñas, notas privadas, datos de tarjetas bancarias y documentos de identidad. Todos los datos se cifran localmente en el dispositivo utilizando estándares criptográficos avanzados (AES-256 y PBKDF2), garantizando que nadie excepto el propietario del PIN Maestro pueda acceder a la información.

---

## 🛡️ Características Principales

- **Arquitectura Zero-Knowledge (Conocimiento Cero):**
  - Los datos nunca salen de tu dispositivo en texto plano.
  - La clave de cifrado se deriva localmente a partir de tu PIN/contraseña maestra mediante PBKDF2 (`PBKDF2WithHmacSHA256`) con 10,000 iteraciones y salt criptográfico aleatorio.
  - Ningún servidor tiene acceso a tus claves o contraseñas.

- **Bóveda Multicategoría:**
  - 🔑 **Cuentas y Contraseñas:** Nombre de usuario, contraseña, URL del servicio y notas cifradas.
  - 📝 **Notas Seguras:** Bloc de notas cifrado para información sensible, códigos y registros personales.
  - 💳 **Tarjetas de Pago:** Número de tarjeta formateado, fecha de expiración, CVV protegido y banco emisor.
  - 🪪 **Documentos de Identidad:** Pasaportes, DNI, licencias de conducir y números de registro legal.

- **Generador Criptográfico de Contraseñas:**
  - Generación de contraseñas de alta entropía configurables de 8 a 32 caracteres.
  - Opciones de mayúsculas, minúsculas, dígitos numéricos y símbolos especiales.
  - Medidor de robustez criptográfica en tiempo real.

- **Auditoría de Seguridad Integrada:**
  - Análisis automático del estado de seguridad de la bóveda.
  - Detección de contraseñas débiles o reutilizadas.
  - Puntuación de salud de seguridad (0 a 100%).

- **Control de Acceso y Bloqueo:**
  - Bloqueo instantáneo con un solo toque.
  - Teclado numérico seguro en pantalla con retroalimentación visual discreta.
  - Soporte de desbloqueo rápido / biometría.

- **Copias de Seguridad Cifradas:**
  - Exportación de la bóveda completa cifrada con AES-256.
  - Copia segura que puede guardarse sin riesgo de exposición de credenciales.

---

## 💻 Tecnologías Utilizadas

- **Lenguaje:** Kotlin 2.2+
- **Framework UI:** Jetpack Compose (Material Design 3)
- **Criptografía:** `javax.crypto` (AES-256/CBC/PKCS5Padding, PBKDF2WithHmacSHA256, SecureRandom)
- **Arquitectura:** MVVM (Model-View-ViewModel) con StateFlow y corrutinas
- **Gestión de Dependencias:** Gradle Version Catalogs (`gradle/libs.versions.toml`)
- **Herramienta de Compilación:** Gradle 9+ con Android Gradle Plugin (AGP) 9.1+
- **Pruebas:** JUnit 4, Robolectric, Compose UI Testing
- **Automatización:** GitHub Actions CI/CD para compilación automática y generación de APK

---

## ⚙️ Requisitos para Compilar

1. **Java Development Kit (JDK):** Versión 17 o 21 (Temurin o OpenJDK recomendado).
2. **Android SDK:**
   - `compileSdk`: 36 (Android 16 / 15)
   - `minSdk`: 24 (Android 7.0 Nougat o superior)
   - `targetSdk`: 36
3. **Android Studio:** Ladybug (2024.2) o superior (recomendado para desarrollo visual).
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
Conecta un dispositivo Android con depuración USB habilitada o inicia un emulador:
```bash
./gradlew installDebug
```

---

## 📦 Instrucciones para Generar el APK

### Generar APK de Desarrollo (Debug APK)
Para compilar un APK listo para probar en cualquier dispositivo Android:
```bash
./gradlew assembleDebug
```
El archivo APK generado se encontrará en:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Generar APK de Producción (Release APK)
Para generar un APK firmado de producción, configura las variables de entorno para tu almacén de claves (keystore):
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

## 🤖 Integración Continua (GitHub Actions)

El repositorio incluye un flujo de trabajo automatizado en `.github/workflows/android.yml`:
- Se ejecuta automáticamente en cada `push` o `pull request` a la rama `main`.
- Configura el entorno con JDK 21.
- Ejecuta el conjunto de pruebas unitarias.
- Compila automáticamente el APK de prueba (`app-debug.apk`).
- Publica el APK como un **Artifact** descargable en cada ejecución.
- En caso de crear una etiqueta de versión (`git tag v1.0.0`), crea automáticamente una **Release en GitHub** adjuntando el APK compilado.

---

## 🔒 Privacidad y Seguridad

- **Sin Telemetría ni Rastreadores:** SafeBox no recopila ni transmite ningún dato analítico ni información de uso.
- **Sin Dependencias de Servidores Externos:** No requiere registro en la nube de terceros; el control absoluto reside en el usuario.
- **Protección de Secretos en el Repositorio:** Este repositorio excluye estrictamente mediante `.gitignore` cualquier clave de firma (`.keystore`, `.jks`), credenciales locales (`local.properties`), variables de entorno (`.env`) o claves API.
- **Destrucción de Claves en Memoria:** Al presionar "Bloquear", la clave activa se destruye en memoria forzando una nueva derivación PBKDF2 con el PIN Maestro.

---

## 🏷️ Versión
- **Versión:** `v1.0.0`
- **Código de Versión:** `1`
