# Focus Guard

**Aplicación móvil Android anti-procrastinación**

Aplicación nativa que mide el uso real de las aplicaciones del teléfono, identifica distracciones de forma automática, gestiona tareas con fechas límite y motiva al usuario a mantener el enfoque mediante notificaciones inteligentes, un temporizador Pomodoro y un índice de productividad semanal.

| | |
|---|---|
| **Curso** | Tecnología en Sistematización de Datos — Programación por Componentes (578-303) |
| **Profesor** | J. E. Hernández Rodríguez |
| **Integrantes** | O. González · J. Bernal |
| **Plataforma** | Android (Kotlin + Jetpack Compose) |

---

## Tabla de contenido

1. [Descarga e instalación (APK)](#descarga-e-instalación-apk)
2. [Cumplimiento de requisitos](#cumplimiento-de-requisitos-del-proyecto)
3. [Arquitectura del proyecto](#arquitectura-del-proyecto)
4. [Tecnologías utilizadas](#tecnologías-utilizadas)
5. [Estructura de carpetas](#estructura-de-carpetas)
6. [Decisiones técnicas destacadas](#decisiones-técnicas-destacadas)
7. [Cómo compilar](#cómo-compilar-el-proyecto)
8. [Distribución del trabajo](#distribución-del-trabajo)

---

## Descarga e instalación (APK)

El archivo APK está disponible en la raíz de este repositorio: **`FocusGuard.apk`**

**Para instalarlo en un dispositivo Android:**
1. Descargar `FocusGuard.apk` al teléfono.
2. Abrir el archivo y permitir la instalación de orígenes desconocidos si se solicita.
3. Al abrir la app por primera vez, conceder el permiso de **"Acceso a uso"** (la app redirige automáticamente a la pantalla de ajustes del sistema). Este permiso es indispensable: sin él la app no puede medir el tiempo de uso de otras aplicaciones.

> **Nota:** El permiso de "Acceso a uso" no es un permiso estándar de Android (no muestra el diálogo emergente habitual), por lo que debe activarse manualmente desde Ajustes del sistema. La app guía al usuario en este proceso.

---

## Cumplimiento de requisitos del proyecto

Esta sección mapea cada requisito del enunciado con su implementación, para facilitar la evaluación.

### 1. Repositorio público con código fuente y APK
- Código fuente publicado en este repositorio público.
- Archivo `FocusGuard.apk` incluido en la raíz.

### 2. Consumo de servicio web (REST)
- Se consume la API REST pública **ZenQuotes** (`https://zenquotes.io/api/random`) mediante **Retrofit**.
- La app realiza una petición HTTP GET a un servidor remoto, recibe una respuesta en formato JSON y la parsea con Gson para mostrar una frase motivacional en la pantalla principal.
- Flujo completo siguiendo la arquitectura: `QuoteDto` → `QuoteApi` → `QuoteRepository` → `AppViewModel` → `HomeScreen`.
- Incluye manejo de errores (estado `QuoteResult.Success` / `QuoteResult.Error`) con texto de respaldo si no hay conexión.
- **Archivos relevantes:** `data/remote/QuoteApi.kt`, `data/remote/QuoteDto.kt`, `domain/repository/QuoteRepository.kt`.

### 3. Soporte de dos idiomas (inglés y español)
- Textos externalizados en archivos de recursos `strings.xml`.
- Carpeta `values/` (inglés, por defecto) y `values-es/` (español).
- La aplicación se adapta automáticamente a la configuración de idioma del teléfono.
- Selector de idioma dentro de la app mediante `AppCompatDelegate.setApplicationLocales`.
- **Archivos relevantes:** `res/values/strings.xml`, `res/values-es/strings.xml`, `ui/components/settings/LanguageSelector.kt`.

### 4. Formulario
- Pantalla **Nueva Tarea** (`NewTaskScreen`) con campos de nombre, descripción y fecha límite.
- Validación de campos obligatorios y selector de fecha que impide seleccionar fechas pasadas.
- Los datos capturados se persisten en la base de datos local Room.
- **Archivo relevante:** `ui/screens/NewTaskScreen.kt`.

### 5. Notificaciones internas
- Dos tipos de notificación gestionadas por `NotificationHelper`: estándar e interactiva (heads-up).
- Alertas cuando el usuario excede el tiempo en una app de distracción.
- Recordatorios sobre tareas pendientes y vencidas.
- Canal de notificaciones configurado para Android 8.0+ (API 26+).
- **Archivo relevante:** `domain/notifications/NotificationHelper.kt`.

### 6. Arquitectura MVVM
- **Modelo:** entidades de dominio y repositorios (`domain/model`, `domain/repository`).
- **Vista:** pantallas declarativas en Jetpack Compose (`ui/screens`).
- **ViewModel:** `AppViewModel` expone el estado mediante `StateFlow` y orquesta la lógica.
- La Vista nunca accede directamente a los datos; siempre pasa por el ViewModel.
- **Archivo relevante:** `ui/viewmodel/AppViewModel.kt`.

---

## Arquitectura del proyecto

El proyecto sigue el patrón **MVVM** con una separación por capas inspirada en principios de Clean Architecture.

```
┌─────────────────────────────────────────────┐
│                    VISTA                      │
│         (Jetpack Compose - ui/screens)        │
│   HomeScreen · TasksScreen · NewTaskScreen    │
│   PomodoroScreen · StatsScreen · Settings     │
└───────────────────────┬───────────────────────┘
                        │ observa StateFlow
                        │ llama funciones
┌───────────────────────▼───────────────────────┐
│                  VIEWMODEL                     │
│                (AppViewModel)                  │
│   Expone el estado · Orquesta la lógica        │
└───────────────────────┬───────────────────────┘
                        │ usa
┌───────────────────────▼───────────────────────┐
│             REPOSITORIOS (interfaces)          │
│   UsageRepository · SettingsRepository         │
│   QuoteRepository                              │
└───────────────────────┬───────────────────────┘
                        │ implementadas por
┌───────────────────────▼───────────────────────┐
│               FUENTES DE DATOS                 │
│   Room (tareas) · SharedPreferences (ajustes)  │
│   UsageStatsManager (uso) · Retrofit (REST)    │
└────────────────────────────────────────────────┘
```

**Capas y responsabilidades:**

- **Vista:** interfaz reactiva en Compose. Solo observa el estado y reenvía eventos al ViewModel.
- **ViewModel:** intermediario único entre la UI y los datos. Mantiene el estado en `StateFlow`.
- **Repositorios:** interfaces que abstraen las fuentes de datos (inversión de dependencias).
- **Casos de uso:** lógica de negocio pura sin dependencias de Android (`CalculateTaskScoreUseCase`), testeable con JUnit.
- **Fuentes de datos:** Room para datos estructurados, SharedPreferences para preferencias, UsageStatsManager para el rastreo de uso y Retrofit para el servicio REST.

---

## Tecnologías utilizadas

| Tecnología | Uso en el proyecto |
|---|---|
| **Kotlin** | Lenguaje principal |
| **Jetpack Compose** | Interfaz de usuario declarativa |
| **Room** | Persistencia local de tareas y distracciones aprendidas |
| **SharedPreferences** | Almacenamiento de preferencias y límites |
| **Retrofit + Gson** | Consumo del servicio REST y parseo de JSON |
| **WorkManager** | Monitoreo periódico en segundo plano (intervalos ≥ 15 min) |
| **AlarmManager** | Monitoreo exacto en segundo plano (intervalos < 15 min) |
| **UsageStatsManager** | Medición del tiempo de uso de aplicaciones |
| **Coroutines + Flow** | Programación asíncrona y flujos reactivos |
| **AppCompat** | Soporte del cambio de idioma en tiempo de ejecución |
| **JUnit** | Pruebas unitarias de la lógica de negocio |

---

## Estructura de carpetas

```
app/src/main/java/com/example/antiprocrastination/
├── MainActivity.kt                  # Punto de entrada, navegación, permisos
├── data/                            # Implementaciones y acceso a datos
│   ├── AppDatabase.kt               # Configuración de Room (Singleton)
│   ├── TaskDao.kt                   # Operaciones sobre tareas
│   ├── DistractionDao.kt            # Operaciones sobre distracciones aprendidas
│   ├── SettingsManagerImpl.kt       # Preferencias (SharedPreferences)
│   ├── UsageTrackerImpl.kt          # Rastreo de uso (UsageStatsManager)
│   └── remote/                      # Capa de red (servicio REST)
│       ├── QuoteApi.kt              # Interfaz Retrofit
│       └── QuoteDto.kt              # Modelo de la respuesta JSON
├── domain/                          # Lógica de negocio y contratos
│   ├── model/Models.kt              # Entidades del dominio
│   ├── repository/                  # Interfaces de repositorio
│   ├── usecase/                     # Lógica pura testeable
│   ├── usage/                       # Monitoreo en segundo plano
│   └── notifications/               # Sistema de notificaciones
└── ui/                              # Capa de presentación
    ├── screens/                     # Pantallas (Compose)
    ├── components/                  # Componentes reutilizables
    ├── viewmodel/                   # ViewModel y Factory
    ├── navigation/                  # Definición de rutas
    └── theme/                       # Colores y tipografía

app/src/test/                        # Pruebas unitarias
└── CalculateTaskScoreTest.kt        # Tests de la lógica de puntuación
```

---

## Decisiones técnicas destacadas

Estas son las decisiones de diseño más relevantes del proyecto y su justificación.

### Medición de uso precisa combinando dos APIs
`UsageStatsManager` ofrece dos métodos: `queryUsageStats` (totales agregados, pero el sistema los actualiza con retraso) y `queryEvents` (eventos en tiempo real). El proyecto combina ambos: usa los totales como base y los corrige sumando el tiempo de la sesión activa detectada en los eventos recientes. Esto logra precisión casi al segundo.

### Monitoreo en segundo plano híbrido (WorkManager + AlarmManager)
WorkManager impone un intervalo mínimo de 15 minutos por diseño de Android. Para permitir monitoreo más frecuente, el proyecto usa `AlarmManager` con alarmas exactas en intervalos menores a 15 minutos, y WorkManager para intervalos mayores (más eficiente en batería).

### Reinicio diario implícito del conteo
El uso diario no se reinicia con un contador manual. Cada consulta usa una ventana de tiempo que arranca en la medianoche del día actual, de modo que al cambiar de día el conteo anterior queda automáticamente excluido. La fuente de verdad es el sistema operativo.

### Inversión de dependencias y testeo
Los repositorios se definen como interfaces, lo que permite inyectar implementaciones falsas (`FakeUsageRepository`, `FakeSettingsRepository`) en las pruebas unitarias sin depender del entorno Android. La lógica de puntuación de tareas se aisló en un caso de uso puro para poder probarla con JUnit.

### Aprendizaje automático de distracciones
Más allá de una lista fija, la app detecta nuevas aplicaciones de distracción mientras se usa (mediante categorías del sistema) y las guarda en la base de datos para reconocerlas en el futuro.

---

## Cómo compilar el proyecto

**Requisitos:** Android Studio (versión reciente), JDK 17, SDK de Android.

```bash
# Clonar el repositorio
git clone [URL_DEL_REPOSITORIO]

# Abrir en Android Studio y sincronizar Gradle,
# o compilar el APK desde la terminal:
./gradlew assembleDebug
```

El APK generado queda en `app/build/outputs/apk/debug/app-debug.apk`.

---

## Distribución del trabajo

| Integrante | Contribución |
|---|---|
| **O. González** | [Completar: p. ej. capa de datos, rastreo de uso, monitoreo en segundo plano] |
| **J. Bernal** | [Completar: p. ej. interfaz Compose, ViewModel, servicio REST, idiomas] |

---

## Repositorio

**Código fuente y APK:** [PEGAR_AQUÍ_EL_LINK_DEL_REPOSITORIO]
