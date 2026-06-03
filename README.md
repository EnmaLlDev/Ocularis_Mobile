# Ocularis Mobile

**Versión:** 1.0  
**Estado:** Desarrollo Activo

Aplicacion Android para gestion clinica oftalmologica construida con **Jetpack Compose**, **MVVM** y consumo de API REST con **Retrofit/OkHttp**.

---

## 0. Informacion Tecnica General

| Propiedad | Valor |
|-----------|-------|
| **Versión de App** | 1.0 |
| **Versión Code** | 1 |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 36 (Android 15) |
| **Compile SDK** | 36 (Android 15.1) |
| **Java Compatibility** | 11 |
| **Kotlin** | 2.2.10 |
| **Compose (BOM)** | 2024.09.00 |
| **Namespace** | `fp.practices.ocularis_mobile` |
| **Application ID** | `fp.practices.ocularis_mobile` |

### Herramientas Build
- **AGP (Android Gradle Plugin):** 9.2.0
- **Gradle Wrapper:** Compatible con AGP 9.2.0
- **Build Features:** Compose UI habilitada

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

## 3. Patrones de Diseño

La aplicacion implementa los siguientes patrones de arquitectura y diseño:

### 3.1 Patrones Arquitectonicos
| Patrón | Descripión | Ubicación |
|--------|-----------|-----------|
| **MVVM** | Model-View-ViewModel separa logica de negocio de presentación | `ui/screens/*` + `viewmodel/*` |
| **Repository** | Abstrae acceso a datos remotos y locales | `data/repository/*` |
| **Singleton** | Instancias unicas para `RetrofitClient`, `TokenStore` | `data/network/`, `data/auth/` |
| **Dependency Injection** | Inyección manual de dependencias en constructores | Pasado a `ViewModel` y `Repository` |
| **Observer** | LiveData/StateFlow para reactividad UI | `viewmodel/*` exponiendo `LiveData<T>` |
| **Interceptor** | Manejo centralizado de headers y errores HTTP | `AuthHeaderInterceptor`, `TokenAuthenticator` |
| **Factory** | Creación de objetos Retrofit y interceptores | `RetrofitClient` |
| **Data Transfer Object (DTO)** | Modelos para serialización/deserialización JSON | `data/model/*` |

### 3.2 Patrones de Red y Autenticacion
| Patrón | Descripión |
|--------|-----------|
| **Bearer Token** | Autenticación con tokens JWT en headers Authorization |
| **Token Refresh** | Automatic refresh de access token mediante refresh token |
| **Retry Logic** | Reintentos inteligentes ante fallos 401/403 |
| **Circuit Breaker** | Invalidación de sesión ante errores persistentes |

---

## 4. Arquitectura Detallada

La app sigue una arquitectura **MVVM + Repository + Data Layer**:

### 4.1 Capas de la Arquitectura

```
┌─────────────────────────────────────────┐
│         UI Layer (Compose)              │
│  ├─ Screens                             │
│  ├─ MainActivity (Navigation)            │
│  └─ Theme                               │
└────────────┬────────────────────────────┘
             │ observa
┌────────────▼────────────────────────────┐
│       ViewModel Layer                   │
│  ├─ AuthViewModel                       │
│  ├─ PatientsViewModel                   │
│  ├─ DoctorsViewModel                    │
│  ├─ AppointmentsViewModel               │
│  ├─ DetailsViewModel                    │
│  └─ DashboardViewModel                  │
└────────────┬────────────────────────────┘
             │ usa
┌────────────▼────────────────────────────┐
│       Repository Layer                  │
│  ├─ AuthRepository                      │
│  ├─ PatientsRepository                  │
│  ├─ DoctorsRepository                   │
│  ├─ AppointmentsRepository              │
│  └─ DetailsRepository                   │
└────────────┬────────────────────────────┘
             │ llama
┌────────────▼────────────────────────────┐
│       Network Layer                     │
│  ├─ ApiService (Retrofit)               │
│  ├─ AuthHeaderInterceptor               │
│  ├─ TokenAuthenticator                  │
│  └─ RetrofitClient                      │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│    Auth & Storage Layer                 │
│  ├─ TokenStore (Preferences/Encrypted)  │
│  └─ EncryptedSharedPreferences          │
└─────────────────────────────────────────┘
```

### 4.2 Detalles de cada Capa

#### UI Layer (Compose)
- **Descripción:** Interfaz de usuario declarativa con Jetpack Compose.
- **Responsabilidades:**
  - Renderizar componentes visuales (Screens).
  - Observar cambios en LiveData del ViewModel.
  - Responder a eventos del usuario (clicks, inputs).
  - Mostrar estados de carga y errores.
- **Ubicación:** `ui/screens/*`, `MainActivity.kt`, `ui/theme/*`
- **Características:**
  - Composables reutilizables.
  - Material Design 3.
  - Navegación con Navigation Compose.
  - Adaptacion por rol de usuario.

#### ViewModel Layer
- **Descripción:** Mantiene estado UI y ejecuta logica de presentacion.
- **Responsabilidades:**
  - Gestionar estado de pantalla (loading, error, datos).
  - Validar entrada del usuario.
  - Invocar repositorio.
  - Exponer LiveData/StateFlow reactivos.
- **Ubicación:** `viewmodel/*`
- **Clases principales:**
  - `AuthViewModel` - Autenticación y sesión.
  - `PatientsViewModel` - Listado/CRUD de pacientes.
  - `DoctorsViewModel` - Listado/CRUD de doctores.
  - `AppointmentsViewModel` - Gestión de citas.
  - `DetailsViewModel` - Detalles clínicos.
  - `DashboardViewModel` - Dashboard principal.

#### Repository Layer
- **Descripción:** Abstrae lógica de acceso a datos.
- **Responsabilidades:**
  - Centralizar llamadas a `ApiService`.
  - Transformar DTOs a modelos de dominio.
  - Manejar excepciones y comunicar errores.
  - Cachear datos si es necesario.
- **Ubicación:** `data/repository/*`
- **Clases principales:**
  - `AuthRepository` - Login, logout, refresh.
  - `PatientsRepository` - CRUD, búsqueda.
  - `DoctorsRepository` - CRUD, filtrado.
  - `AppointmentsRepository` - CRUD.
  - `DetailsRepository` - CRUD, filtrado.

#### Network Layer
- **Descripción:** Comunicación HTTP con backend.
- **Responsabilidades:**
  - Definir endpoints (ApiService).
  - Construir cliente Retrofit.
  - Interceptar y procesar requests/responses.
  - Manejar reautenticación automática.
- **Ubicación:** `data/network/*`
- **Clases principales:**
  - `RetrofitClient` - Singleton que configura y provee cliente Retrofit.
  - `ApiService` - Interface con endpoints.
  - `AuthHeaderInterceptor` - Agrega header Authorization.
  - `TokenAuthenticator` - Maneja refresh de token y 401/403.

#### Auth & Storage Layer
- **Descripción:** Persistencia segura de credenciales.
- **Responsabilidades:**
  - Guardar/recuperar tokens JWT.
  - Encriptar datos sensibles.
  - Limpiar credenciales en logout.
- **Ubicación:** `data/auth/*`
- **Clases principales:**
  - `TokenStore` - Gestiona persistencia de tokens.
  - Usa `DataStore Preferences` para access token y userInfo.
  - Usa `EncryptedSharedPreferences` para refresh token.

### 4.3 Flujo de Datos (Data Flow)

#### Flujo de Lectura (obtener datos)
```
User toca pantalla
    ↓
Composable llama ViewModel.obtenerDatos()
    ↓
ViewModel invoca Repository.obtenerDatos()
    ↓
Repository llama ApiService.obtenerDatos()
    ↓
Network Layer hace request HTTP
    ↓
Response llega y se deserializa (DTO)
    ↓
Repository transforma DTO → Modelo
    ↓
ViewModel emite resultado en LiveData<Resultado>
    ↓
Composable observa y re-renderiza
```

#### Flujo de Autenticación (Login + Refresh)
```
User ingresa credenciales y toca "Login"
    ↓
AuthViewModel.login(user, pass)
    ↓
AuthRepository.login()
    ↓
ApiService.login() → response con tokens
    ↓
TokenStore guarda access_token + refresh_token
    ↓
Consulta /auth/me para obtener currentUser
    ↓
AuthViewModel emite estado autenticado
    ↓
UI navega a home (ruta /app)
    │
    ├─→ Durante sesión, si endpoint retorna 401:
    │      ↓
    │      TokenAuthenticator intenta refresh
    │      ↓
    │      ApiService.refresh(refreshToken)
    │      ↓
    │      Si OK: guarda nuevo access_token, reintentar request
    │      Si FALLA: invalidar sesión, volver a login
```

## 5. Integracion con API REST

### 5.1 Cliente HTTP

Configurado en:
- `app/src/main/java/fp/practices/ocularis_mobile/data/network/RetrofitClient.kt`

Puntos importantes:
- `BASE_URL = http://10.0.2.2:8080/` (loopback del host cuando usas emulador Android).
- Se crean dos servicios:
  - `authApiService`: cliente sin interceptor para `/auth/*`.
  - `apiService`: cliente autenticado con interceptor + authenticator para endpoints protegidos.

### 5.2 Contrato de endpoints

Definido en:
- `app/src/main/java/fp/practices/ocularis_mobile/data/network/ApiService.kt`

Incluye endpoints de:
- Auth: `/auth/login`, `/auth/refresh`, `/auth/me`, `/auth/logout`
- Pacientes: CRUD + busqueda por direccion
- Doctores: CRUD + busqueda por licencia/especialidad
- Citas: CRUD
- Detalles: CRUD + filtro por cita

### 5.3 Flujo de autenticacion y sesion

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

## 6. Persistencia de credenciales

- **Access token** y `userInfo`: DataStore Preferences.
- **Refresh token**: `EncryptedSharedPreferences` con `MasterKey` (AES).

Archivo clave:
- `app/src/main/java/fp/practices/ocularis_mobile/data/auth/TokenStore.kt`

## 7. Estructura del proyecto (resumen)

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

## 8. Inicializacion de la app

La inicializacion de red/autenticacion ocurre en:
- `app/src/main/java/fp/practices/ocularis_mobile/OcularisMobileApp.kt`

Este `Application` llama `RetrofitClient.initialize(this)` al arrancar.

`AndroidManifest.xml` referencia esta clase en `android:name=".OcularisMobileApp"`.

## 9. Tecnologias y librerias

- **Kotlin** - v2.2.10
- **Jetpack Compose** - BOM 2024.09.00
- **Material 3** - Included in Compose BOM
- **AndroidX Navigation Compose** - v2.9.0
- **Lifecycle ViewModel + LiveData** - v2.6.1
- **Coroutines** - v1.8.1
- **Retrofit** - v2.11.0
- **Gson** (JSON) - v2.11.0
- **OkHttp** - v4.12.0
- **DataStore Preferences** - v1.1.1
- **AndroidX Security Crypto** - v1.1.0-alpha06
- **RecyclerView** - v1.3.2
- **Coil Compose** (Carga de imágenes) - v2.4.0
- **Material Icons Extended** - Included in Compose BOM

### Dependencias de Test
- **JUnit 4** - v4.13.2
- **AndroidX Test** - JUnit v1.1.5, Espresso v3.5.1
- **Compose UI Test** - JUnit4, Manifest (Included in BOM)

## 10. Como ejecutar

Requisitos:
- Android Studio
- SDK Android (minSdk 26, targetSdk 36)
- API backend disponible en `http://10.0.2.2:8080/` (si usas emulador)

Comandos:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## 11. Notas de entorno

- Si ejecutas en dispositivo fisico, `10.0.2.2` no aplica; debes usar la IP de tu maquina en la red local.
- La app usa `usesCleartextTraffic=true` para permitir HTTP en desarrollo (ver `AndroidManifest.xml`).

---

## 12. Ciclo de Vida de Componentes

### 12.1 ViewModel
- **Creación:** Se instancia cuando la pantalla se crea por primera vez.
- **Persistencia:** Sobrevive a cambios de configuracion (rotacion).
- **Limpieza:** Se destruye cuando la pantalla deja la composicion y no se espera su regreso.
- **Scope:** `viewModelScope` para corrutinas que se cancelan con el ViewModel.

### 12.2 Composables
- **Recomposicion:** Se ejecutan multiples veces con diferentes valores de estado.
- **Observacion de LiveData:** `observeAsState()` dispara recomposicion cuando cambia el valor.
- **Efectos:** `LaunchedEffect` para ejecutar codigo una sola vez o cuando cambian dependencias.

### 12.3 Repository
- **Instancia:** Se crea manualmente en ViewModel (no se gestiona ciclo de vida).
- **Solicitudes:** Sin estado; cada llamada hace una solicitud HTTP.
- **Caching:** Basico; pueden cachear DTOs si es necesario.

---

## 13. Manejo de Corrutinas y Asincronía

### 13.1 Scope de Corrutinas
| Scope | Ubicación | Cancelacion |
|-------|-----------|------------|
| `viewModelScope` | ViewModel | Cuando ViewModel se destruye |
| `LaunchedEffect` | Composable | Cuando sale de composicion o cambian dependencias |
| `lifecycleScope` (si aplica) | Fragment/Activity | Cuando se destruye |

### 13.2 Manejo de Errores
- **Try-Catch:** En Repository para capturar excepciones HTTP.
- **LiveData de Error:** ViewModel expone `errorLiveData` para mostrar mensajes.
- **Logging:** Usar logs para debugging en desarrollo.

### 13.3 Estados de Solicitud
```
┌─────────────┐
│  IDLE       │ (inicial)
└──────┬──────┘
       ↓ usuario activa action
┌─────────────┐
│  LOADING    │ (fetching data)
└──────┬──────┘
       ├─→ ┌──────────┐
       │   │ SUCCESS  │ (mostrar datos)
       │   └──────────┘
       │
       └─→ ┌──────────┐
           │  ERROR   │ (mostrar mensaje)
           └──────────┘
```

---

## 14. Best Practices Implementadas

### 14.1 Arquitectura
- ✅ **Separación de concernimientos:** UI, ViewModel, Repository, Network layers están separados.
- ✅ **No dejar lógica en Composables:** Solo observan y renderizam.
- ✅ **Inyección de dependencias:** Manual pero explícito.
- ✅ **Singleton para cliente HTTP:** Una única instancia de Retrofit reutilizada.

### 14.2 Persistencia
- ✅ **Tokens sensibles encriptados:** Refresh token en EncryptedSharedPreferences.
- ✅ **Access token en DataStore:** Mejor que SharedPreferences tradicional.
- ✅ **Limpieza al logout:** Se eliminan tokens y userInfo del almacenamiento local.

### 14.3 Seguridad
- ✅ **Bearer Token en headers:** Authorization + token JWT.
- ✅ **Certificados SSL:** En producción, validar certificados.
- ✅ **Manejo de 401/403:** Refresh automático o logout forzado.
- ✅ **No guardar contraseñas:** Solo tokens.

### 14.4 Rendimiento
- ✅ **Lazy composition:** Composables solo se evalúan si su estado cambió.
- ✅ **Corrutinas:** No bloquean UI.
- ✅ **Image caching:** Coil maneja caching de imágenes.
- ✅ **Minify en release:** ProGuard configurado (aunque actualmente deshabilitado para desarrollo).

### 14.5 Testing
- ✅ **Estructura preparada:** `src/test/` y `src/androidTest/` presentes.
- ✅ **Dependencias de test:** JUnit, Espresso, Compose UI Test disponibles.
- ⏳ **Cobertura:** Se recomienda agregar tests unitarios y de UI.

---

## 15. Soporte

Para preguntas técnicas o problemas:
- Revisar logs de **Logcat** en Android Studio.
- Verificar que el backend esté disponible en `http://10.0.2.2:8080/`.
- Confirmar que minSdk (26) sea compatible con dispositivo/emulador.

---

**Última actualización:** Marzo 2026

