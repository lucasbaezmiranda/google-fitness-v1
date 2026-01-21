# Resumen de App GPS Coordinates - Para Integración

## 📱 Descripción General

App Android que muestra coordenadas GPS en tiempo real y registra la ubicación en un archivo de texto. Diseñada para ser integrada como una pestaña en otra aplicación.

## ✨ Funcionalidades Principales

1. **Visualización GPS en tiempo real**
   - Muestra latitud, longitud, precisión, altitud y velocidad
   - Actualiza cada 30 segundos cuando la app está abierta
   - Panel de estado con información de permisos y GPS

2. **Registro automático de ubicaciones**
   - Guarda cada ubicación en `gps_log.txt` (en cache)
   - Formato: `Fecha|Latitud|Longitud|Precisión(m)|Altitud(m)|Velocidad(km/h)`
   - Registra automáticamente cada vez que se obtiene una ubicación

3. **Exportar y compartir datos**
   - Botón "Exportar Datos GPS" que muestra total de registros
   - Opciones: Compartir por WhatsApp, Correo, o Ver datos
   - Usa FileProvider para compartir el archivo txt

4. **Verificación de estado**
   - Verifica Google Play Services
   - Verifica permisos de ubicación
   - Verifica si GPS está habilitado
   - Muestra contador de actualizaciones recibidas

## 📁 Estructura de Archivos

```
app/src/main/
├── java/com/example/googlefitnessactivity/
│   ├── MainActivity.kt          # Actividad principal con toda la lógica GPS
│   └── LocationLogger.kt        # Clase para guardar/leer datos GPS en archivo
├── res/
│   ├── layout/
│   │   └── activity_main.xml    # Layout con TextViews y botón de exportar
│   ├── values/
│   │   ├── strings.xml          # "GPS Coordinates"
│   │   ├── colors.xml
│   │   └── themes.xml
│   └── xml/
│       └── file_paths.xml       # Configuración FileProvider
└── AndroidManifest.xml           # Permisos y FileProvider
```

## 🔧 Dependencias (build.gradle)

```gradle
dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Google Play Services Location (GPS)
    implementation 'com.google.android.gms:play-services-location:21.0.1'
}
```

## 🔐 Permisos Requeridos (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## 📦 Componentes Principales

### 1. MainActivity.kt
- **Responsabilidades:**
  - Solicitar permisos de ubicación
  - Obtener ubicación GPS cada 30 segundos
  - Mostrar coordenadas en UI
  - Registrar cada ubicación usando LocationLogger
  - Exportar y compartir datos

- **Métodos clave:**
  - `checkLocationPermission()` - Verifica y solicita permisos
  - `startLocationUpdates()` - Inicia actualizaciones GPS
  - `getLastKnownLocation()` - Obtiene última ubicación conocida
  - `requestLocationUpdates()` - Configura actualizaciones periódicas (30 seg)
  - `updateLocationUI(location)` - Actualiza TextViews con datos GPS
  - `exportAndShareData()` - Maneja exportación y compartir
  - `shareViaWhatsApp()` - Comparte por WhatsApp
  - `shareViaEmail()` - Comparte por correo

### 2. LocationLogger.kt
- **Responsabilidades:**
  - Guardar ubicaciones en archivo txt
  - Leer contenido del archivo
  - Formatear datos para compartir

- **Métodos clave:**
  - `logLocation(location)` - Guarda una ubicación en el archivo
  - `getLogContent()` - Lee todo el contenido del archivo
  - `getLogCount()` - Retorna cantidad de registros
  - `formatLogForSharing()` - Formatea datos para compartir

### 3. activity_main.xml
- **Elementos principales:**
  - TextViews para: latitud, longitud, precisión, altitud, velocidad
  - TextViews de estado: permisos, GPS, contador, logs
  - Button: "Exportar Datos GPS"

## 🔄 Flujo de Funcionamiento

1. **Al abrir la app:**
   - Verifica Google Play Services
   - Verifica permisos de ubicación (solicita si no tiene)
   - Verifica si GPS está habilitado
   - Obtiene última ubicación conocida
   - Inicia actualizaciones cada 30 segundos

2. **Cada actualización GPS:**
   - Actualiza TextViews con nueva ubicación
   - Guarda ubicación en `gps_log.txt` usando LocationLogger
   - Incrementa contador de actualizaciones

3. **Al presionar "Exportar Datos GPS":**
   - Muestra diálogo con total de registros
   - Opciones: WhatsApp, Correo, Ver datos
   - Comparte archivo txt usando FileProvider

## 📝 Formato del Archivo de Log

```
2025-01-16 14:30:45|-34.603722|-58.381592|10.5|25.0|0.0
2025-01-16 14:31:15|-34.603750|-58.381600|12.0|25.0|5.2
```

Formato: `Timestamp|Latitud|Longitud|Precisión(m)|Altitud(m)|Velocidad(km/h)`

## 🔌 Para Integrar como Pestaña

### Opción 1: Convertir MainActivity a Fragment
- Convertir `MainActivity` a `GPSFragment`
- El Fragment puede ser usado en ViewPager/TabLayout
- Mantener toda la lógica igual, solo cambiar herencia

### Opción 2: Mantener como Activity y usar Intent
- Crear Intent para abrir MainActivity desde otra app
- Pasar datos entre actividades si es necesario

### Archivos a Copiar/Adaptar:
1. **LocationLogger.kt** - Copiar tal cual (solo cambiar package si es necesario)
2. **MainActivity.kt** - Convertir a Fragment o adaptar como Activity
3. **activity_main.xml** - Copiar layout (ajustar IDs si hay conflictos)
4. **file_paths.xml** - Copiar para FileProvider
5. **Permisos en AndroidManifest.xml** - Agregar permisos de ubicación
6. **FileProvider en AndroidManifest.xml** - Agregar provider si no existe

### Dependencias a Agregar:
- `com.google.android.gms:play-services-location:21.0.1`

### Consideraciones:
- El archivo `gps_log.txt` se guarda en `context.cacheDir`
- Si se integra como Fragment, usar `requireContext()` en lugar de `this`
- El requestCode para permisos (123) puede cambiar si hay conflictos
- FileProvider authority debe ser único: `${applicationId}.fileprovider`

## 🎯 Puntos Clave para Integración

1. **Package name:** Actualmente `com.example.googlefitnessactivity` - cambiar al de la app destino
2. **Namespace:** Actualmente `com.example.googlefitnessactivity` - ajustar
3. **R.layout.activity_main:** Si hay conflicto, renombrar a `fragment_gps` o similar
4. **FileProvider authority:** Debe coincidir con el applicationId de la app destino
5. **ViewBinding:** La app usa viewBinding, asegurar que esté habilitado en build.gradle

## 📊 Estado Actual

- ✅ Funciona como app independiente
- ✅ Registra GPS cada 30 segundos cuando está abierta
- ✅ Guarda en archivo txt
- ✅ Exporta y comparte por WhatsApp/Correo
- ⏳ Pendiente: Servicio en background (no implementado aún)
- ⏳ Pendiente: Subida a nube (no implementado aún)

## 🔄 Próximos Pasos Sugeridos

1. Convertir MainActivity a Fragment (GPSFragment)
2. Integrar en TabLayout/ViewPager de la app destino
3. Ajustar package names y namespaces
4. Probar permisos y funcionalidad
5. (Opcional) Agregar servicio en background para registrar cuando app está cerrada

