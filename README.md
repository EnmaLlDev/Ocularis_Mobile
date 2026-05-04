# Ocularis Mobile

Aplicacion Android para gestion clinica oftalmologica construida con **Jetpack Compose**, **MVVM** y consumo de API REST con **Retrofit/OkHttp**.

## 1. Que hace la app

La app permite autenticacion y gestion de:
- Pacientes
- Doctores
- Citas
- Detalles clinicos

Tambien incluye dashboard principal y control de sesion robusto (expiracion/refresh de tokens).

## 2. Funcionalidad por rol

La UI y navegacion se adaptan por rol:

- **ADMIN**
  - Ve todo: Inicio, Pacientes, Doctores, Citas, Detalles.
  - Tiene capacidades de gestion completas.

- **DOCTOR**
  - Ve solo lo pertinente: Inicio, Pacientes, Agenda/Citas y Detalles.
  - No ve la seccion de Doctores.

- **PATIENT**
  - Ve una experiencia simplificada: Inicio, Mis citas y Mi historial (Detalles).
  - No ve secciones de gestion global (Pacientes/Doctores).

Las reglas de acceso centralizadas se definen en:
- `app/src/main/java/fp/practices/ocularis_mobile/ui/auth/RoleAccess.kt`

## 3. Arquitectura usada

La app sigue una arquitectura **MVVM + Repository + Data Layer**:

- **UI (Compose)**
  - Pantallas en `ui/screens/*`.
  - Navegacion principal en `MainActivity.kt`.
  - El estado de sesion determina rutas (`login` / `app`) y secciones visibles por rol.

- **ViewModel**
  - Manejan estado de carga, error, mensajes y datos de pantalla.
  - Exponen `LiveData`/`StateFlow` para Compose.
  - Ejemplos: `AuthViewModel`, `PatientsViewModel`, `DoctorsViewModel`, `AppointmentsViewModel`, `DetailsViewModel`, `DashboardViewModel`.

- **Repository**
  - Capa de acceso a datos remotos, encapsula llamadas a `ApiService`.
  - Ejemplos: `AuthRepository`, `PatientsRepository`, `DoctorsRepository`, `AppointmentsRepository`, `DetailsRepository`.

- **Network / Auth**
  - Cliente Retrofit centralizado en `RetrofitClient`.
  - Interceptor para header Bearer y manejo de 401/403.
  - Authenticator para refresh de token automatico.

## 4. Integracion con API REST

### 4.1 Cliente HTTP

Configurado en:
- `app/src/main/java/fp/practices/ocularis_mobile/data/network/RetrofitClient.kt`

Puntos importantes:
- `BASE_URL = http://10.0.2.2:8080/` (loopback del host cuando usas emulador Android).
- Se crean dos servicios:
  - `authApiService`: cliente sin interceptor para `/auth/*`.
  - `apiService`: cliente autenticado con interceptor + authenticator para endpoints protegidos.

### 4.2 Contrato de endpoints

Definido en:
- `app/src/main/java/fp/practices/ocularis_mobile/data/network/ApiService.kt`

Incluye endpoints de:
- Auth: `/auth/login`, `/auth/refresh`, `/auth/me`, `/auth/logout`
- Pacientes: CRUD + busqueda por direccion
- Doctores: CRUD + busqueda por licencia/especialidad
- Citas: CRUD
- Detalles: CRUD + filtro por cita

### 4.3 Flujo de autenticacion y sesion

Componentes:
- `TokenStore.kt`
- `AuthHeaderInterceptor.kt`
- `TokenAuthenticator.kt`
- `AuthRepository.kt`
- `AuthViewModel.kt`

Funcionamiento:
1. Login (`/auth/login`) guarda access token y refresh token.
2. Se consulta `/auth/me` para obtener usuario y roles.
3. Cada request protegida agrega `Authorization: Bearer <token>`.
4. Si backend responde **401/403** en endpoint protegido:
   - `TokenAuthenticator` intenta refresh una vez (si hay refresh token).
   - Si falla refresh o no hay token, se limpia sesion.
   - `AuthHeaderInterceptor` tambien invalida sesion ante 401/403 protegidos.
5. `AuthViewModel` observa cambios del token; si queda nulo, fuerza estado no autenticado y retorno a Login.

## 5. Persistencia de credenciales

- **Access token** y `userInfo`: DataStore Preferences.
- **Refresh token**: `EncryptedSharedPreferences` con `MasterKey` (AES).

Archivo clave:
- `app/src/main/java/fp/practices/ocularis_mobile/data/auth/TokenStore.kt`

## 6. Estructura del proyecto (resumen)

```text
app/src/main/java/fp/practices/ocularis_mobile/
  MainActivity.kt
  OcularisMobileApp.kt
  data/
    auth/
    model/
    network/
    repository/
  ui/
    auth/
    screens/
    theme/
  viewmodel/
```

## 7. Inicializacion de la app

La inicializacion de red/autenticacion ocurre en:
- `app/src/main/java/fp/practices/ocularis_mobile/OcularisMobileApp.kt`

Este `Application` llama `RetrofitClient.initialize(this)` al arrancar.

`AndroidManifest.xml` referencia esta clase en `android:name=".OcularisMobileApp"`.

## 8. Tecnologias y librerias

- Kotlin
- Jetpack Compose + Material 3
- AndroidX Navigation Compose
- Lifecycle ViewModel + LiveData
- Coroutines
- Retrofit + Gson
- OkHttp
- DataStore Preferences
- AndroidX Security Crypto

Versiones declaradas en:
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`

## 9. Como ejecutar

Requisitos:
- Android Studio
- SDK Android (minSdk 26, targetSdk 36)
- API backend disponible en `http://10.0.2.2:8080/` (si usas emulador)

Comandos:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## 10. Notas de entorno

- Si ejecutas en dispositivo fisico, `10.0.2.2` no aplica; debes usar la IP de tu maquina en la red local.
- La app usa `usesCleartextTraffic=true` para permitir HTTP en desarrollo (ver `AndroidManifest.xml`).

---

Si quieres, puedo ampliar este README con:
1. diagrama de flujo (auth + refresh) en Mermaid,
2. tabla de permisos por rol y modulo,
3. ejemplos JSON de requests/responses por endpoint.

