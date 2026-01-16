# Google Fitness Activity - App Android

Una aplicación Android simple que muestra la actividad actual del usuario usando Google Play Services Activity Recognition API.

## Descripción

Esta es una aplicación "Hello World" de Google Fitness / Activity Recognition que detecta y muestra en tiempo real la actividad que el usuario está realizando (caminando, corriendo, en vehículo, quieto, etc.).

## Características

- ✅ Detecta actividad en tiempo real cada 5 segundos
- ✅ Muestra la actividad actual en español
- ✅ Muestra el nivel de confianza de la detección
- ✅ Solicita permisos automáticamente
- ✅ Interfaz simple y clara

## Requisitos

- Android Studio Hedgehog (2023.1.1) o superior
- Android SDK mínimo: API 24 (Android 7.0)
- Android SDK objetivo: API 34 (Android 14)
- Google Play Services instalado en el dispositivo/emulador

## Configuración

1. **Abrir el proyecto en Android Studio**
   ```bash
   cd /home/luke/Desktop/repositorios/google-fitness-v1
   # Abrir Android Studio y seleccionar "Open an Existing Project"
   ```

2. **Sincronizar Gradle**
   - Android Studio debería sincronizar automáticamente
   - Si no, ve a `File > Sync Project with Gradle Files`

3. **Ejecutar la aplicación**
   - Conecta un dispositivo Android o inicia un emulador
   - Haz clic en "Run" (botón verde de play)
   - Selecciona tu dispositivo/emulador

## Permisos

La aplicación solicita automáticamente el permiso `ACTIVITY_RECOGNITION` cuando se inicia por primera vez. Este permiso es necesario para que Google Play Services pueda detectar la actividad del usuario.

## Cómo funciona

1. Al abrir la app, se solicita el permiso `ACTIVITY_RECOGNITION`
2. Una vez concedido, se registra un `ActivityRecognitionClient` de Google Play Services
3. Cada 5 segundos, se recibe una actualización con la actividad detectada
4. La actividad se muestra en pantalla junto con su nivel de confianza

## Actividades detectadas

- **STILL**: Quieto / Descansando
- **WALKING**: Caminando
- **RUNNING**: Corriendo
- **ON_FOOT**: A pie
- **ON_BICYCLE**: En bicicleta
- **IN_VEHICLE**: En vehículo
- **TILTING**: Inclinando dispositivo
- **UNKNOWN**: Actividad desconocida

## Estructura del proyecto

```
google-fitness-v1/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/googlefitnessactivity/
│   │       │   ├── MainActivity.kt              # Actividad principal
│   │       │   └── ActivityRecognitionReceiver.kt  # Receptor de actualizaciones
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml        # Interfaz de usuario
│   │       │   └── values/
│   │       │       ├── strings.xml              # Cadenas de texto
│   │       │       ├── colors.xml               # Colores
│   │       │       └── themes.xml               # Temas
│   │       └── AndroidManifest.xml              # Configuración de la app
│   └── build.gradle                             # Dependencias
├── build.gradle                                 # Configuración del proyecto
├── settings.gradle                              # Configuración de módulos
└── README.md                                    # Este archivo
```

## Tecnologías utilizadas

- **Kotlin**: Lenguaje de programación
- **Google Play Services Location**: API de Activity Recognition
- **AndroidX**: Bibliotecas modernas de Android
- **Material Design**: Componentes de interfaz

## Notas importantes

- Esta app usa la API **Activity Recognition** de Google Play Services, que es diferente de Google Fit
- Google Fit se está migrando a Android Health / Health Connect, pero Activity Recognition sigue funcionando
- La detección puede tardar unos segundos en comenzar a funcionar
- Para mejores resultados, prueba la app en un dispositivo físico moviéndote o usando diferentes transportes

## Solución de problemas

**La app no detecta actividad:**
- Verifica que el permiso `ACTIVITY_RECOGNITION` esté concedido
- Asegúrate de tener Google Play Services instalado y actualizado
- Prueba en un dispositivo físico en lugar de un emulador
- Muévete o cambia de posición para activar la detección

**Errores de compilación:**
- Asegúrate de tener Android SDK API 34 instalado
- Sincroniza Gradle: `File > Sync Project with Gradle Files`
- Limpia el proyecto: `Build > Clean Project`
- Reconstruye: `Build > Rebuild Project`

## Licencia

Este es un proyecto de ejemplo educativo. Siéntete libre de usarlo y modificarlo según tus necesidades.

