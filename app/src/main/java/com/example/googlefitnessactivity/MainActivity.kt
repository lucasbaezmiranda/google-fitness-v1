package com.example.googlefitnessactivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : AppCompatActivity() {
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var accuracyTextView: TextView
    private lateinit var altitudeTextView: TextView
    private lateinit var speedTextView: TextView
    private lateinit var permissionStatusTextView: TextView
    private lateinit var gpsStatusTextView: TextView
    private lateinit var updatesCountTextView: TextView
    private lateinit var lastLogTextView: TextView
    
    private var updatesCount = 0
    private val requestCode = 123
    private val TAG = "MainActivity"
    private val cancellationTokenSource = CancellationTokenSource()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        latitudeTextView = findViewById(R.id.latitudeTextView)
        longitudeTextView = findViewById(R.id.longitudeTextView)
        accuracyTextView = findViewById(R.id.accuracyTextView)
        altitudeTextView = findViewById(R.id.altitudeTextView)
        speedTextView = findViewById(R.id.speedTextView)
        permissionStatusTextView = findViewById(R.id.permissionStatusTextView)
        gpsStatusTextView = findViewById(R.id.gpsStatusTextView)
        updatesCountTextView = findViewById(R.id.updatesCountTextView)
        lastLogTextView = findViewById(R.id.lastLogTextView)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Verificar Google Play Services
        checkGooglePlayServices()
        
        // Verificar y solicitar permisos
        checkLocationPermission()
    }
    
    private fun checkLocationPermission() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasPermission = hasFineLocation || hasCoarseLocation
        
        updatePermissionStatus(hasPermission)
        
        if (!hasPermission) {
            Log.d(TAG, "Solicitando permisos de ubicación")
            logStatus("Solicitando permisos de ubicación...")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestCode
            )
        } else {
            Log.d(TAG, "Permisos de ubicación ya concedidos")
            logStatus("Permisos concedidos. Obteniendo ubicación...")
            startLocationUpdates()
        }
    }
    
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            logStatus("Sin permisos de ubicación")
            return
        }
        
        // Verificar si el GPS está habilitado
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Por favor activa tu ubicación...", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            updateGpsStatus("GPS: ⚠ Desactivado")
            logStatus("GPS desactivado. Abriendo configuración...")
            return
        }
        
        try {
            updateGpsStatus("Obteniendo ubicación...")
            
            // Primero intentar obtener la última ubicación conocida (más rápido)
            getLastKnownLocation()
            
            // Configurar actualizaciones periódicas cada 5 segundos
            requestLocationUpdates()
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad al obtener ubicación: ${e.message}", e)
            logStatus("Error de permisos: ${e.message}")
            updateGpsStatus("GPS: ❌ Sin permisos")
        }
    }
    
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    private fun getLastKnownLocation() {
        if (!hasLocationPermission()) return
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    updatesCount++
                    Log.d(TAG, "Última ubicación conocida: lat=${location.latitude}, lng=${location.longitude}")
                    logStatus("Ubicación obtenida (${updatesCount} actualizaciones)")
                    updateLocationUI(location)
                    updateGpsStatus("GPS: ✓ Activo")
                    updateUpdatesCount()
                } else {
                    logStatus("No hay ubicación disponible. Solicitando nueva ubicación...")
                    updateGpsStatus("GPS: ⚠ Esperando señal...")
                    // Si no hay última ubicación, solicitar una nueva
                    requestNewLocation()
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad: ${e.message}", e)
        }
    }
    
    private fun requestNewLocation() {
        if (!hasLocationPermission()) return
        
        try {
            // Solicitar ubicación con alta precisión
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    updatesCount++
                    Log.d(TAG, "Nueva ubicación obtenida: lat=${location.latitude}, lng=${location.longitude}")
                    logStatus("Nueva ubicación obtenida (${updatesCount} actualizaciones)")
                    updateLocationUI(location)
                    updateGpsStatus("GPS: ✓ Activo")
                    updateUpdatesCount()
                } else {
                    logStatus("No se pudo obtener nueva ubicación")
                    updateGpsStatus("GPS: ⚠ Sin señal")
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener nueva ubicación: ${e.message}", e)
                logStatus("Error: ${e.message}")
                updateGpsStatus("GPS: ❌ Error")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad: ${e.message}", e)
        }
    }
    
    private fun requestLocationUpdates() {
        if (!hasLocationPermission()) return
        
        try {
            // Crear LocationRequest para actualizaciones periódicas
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000 // intervalo de 5 segundos
            ).apply {
                setMinUpdateIntervalMillis(3000) // actualización mínima de 3 segundos
                setMaxUpdateDelayMillis(10000) // máximo delay de 10 segundos
            }.build()
            
            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        updatesCount++
                        Log.d(TAG, "Actualización periódica: lat=${location.latitude}, lng=${location.longitude}")
                        logStatus("Actualización periódica (${updatesCount} actualizaciones)")
                        updateLocationUI(location)
                        updateGpsStatus("GPS: ✓ Activo")
                        updateUpdatesCount()
                    }
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
            
            logStatus("Solicitando actualizaciones periódicas...")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad: ${e.message}", e)
        }
    }
    
    private fun updateLocationUI(location: Location) {
        latitudeTextView.text = "Latitud: ${String.format("%.6f", location.latitude)}°"
        longitudeTextView.text = "Longitud: ${String.format("%.6f", location.longitude)}°"
        
        if (location.hasAccuracy()) {
            accuracyTextView.text = "Precisión: ${String.format("%.1f", location.accuracy)} m"
        } else {
            accuracyTextView.text = "Precisión: No disponible"
        }
        
        if (location.hasAltitude()) {
            altitudeTextView.text = "Altitud: ${String.format("%.1f", location.altitude)} m"
        } else {
            altitudeTextView.text = "Altitud: No disponible"
        }
        
        if (location.hasSpeed()) {
            val speedKmh = location.speed * 3.6 // convertir m/s a km/h
            speedTextView.text = "Velocidad: ${String.format("%.1f", speedKmh)} km/h"
        } else {
            speedTextView.text = "Velocidad: 0.0 km/h"
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == this.requestCode) {
            val granted = grantResults.isNotEmpty() && 
                grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            
            updatePermissionStatus(granted)
            
            if (granted) {
                Log.d(TAG, "Permisos de ubicación concedidos")
                logStatus("Permisos concedidos. Obteniendo ubicación...")
                startLocationUpdates()
            } else {
                Log.w(TAG, "Permisos de ubicación denegados")
                logStatus("Permisos denegados. La app no funcionará correctamente.")
                Toast.makeText(
                    this,
                    "Se requieren permisos de ubicación para usar esta app",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun checkGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        
        if (resultCode != ConnectionResult.SUCCESS) {
            val errorMsg = apiAvailability.getErrorString(resultCode)
            logStatus("Google Play Services no disponible: $errorMsg")
            gpsStatusTextView.text = "Google Play Services: ❌ No disponible"
            gpsStatusTextView.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            logStatus("Google Play Services disponible ✓")
        }
    }
    
    private fun updatePermissionStatus(granted: Boolean) {
        val statusText = if (granted) {
            "Permiso: ✓ Concedido"
        } else {
            "Permiso: ❌ Denegado"
        }
        permissionStatusTextView.text = statusText
        permissionStatusTextView.setTextColor(
            if (granted) getColor(android.R.color.holo_green_dark)
            else getColor(android.R.color.holo_red_dark)
        )
    }
    
    private fun updateGpsStatus(status: String) {
        gpsStatusTextView.text = status
        gpsStatusTextView.setTextColor(
            when {
                status.contains("✓") -> getColor(android.R.color.holo_green_dark)
                status.contains("❌") -> getColor(android.R.color.holo_red_dark)
                else -> getColor(android.R.color.holo_orange_dark)
            }
        )
    }
    
    private fun updateUpdatesCount() {
        updatesCountTextView.text = "Actualizaciones recibidas: $updatesCount"
    }
    
    private fun logStatus(message: String) {
        Log.d(TAG, "Status: $message")
        lastLogTextView.text = "Último log: $message"
    }
    
    override fun onResume() {
        super.onResume()
        // Volver a obtener ubicación cuando la app vuelve al foreground
        if (hasLocationPermission()) {
            getLastKnownLocation()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource.cancel()
        // Las actualizaciones se detendrán automáticamente cuando se destruya la actividad
    }
}
