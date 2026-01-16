package com.example.googlefitnessactivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient

class MainActivity : AppCompatActivity() {
    
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var pendingIntent: android.app.PendingIntent
    private lateinit var activityTextView: TextView
    private lateinit var confidenceTextView: TextView
    private lateinit var permissionStatusTextView: TextView
    private lateinit var apiStatusTextView: TextView
    private lateinit var updatesCountTextView: TextView
    private lateinit var lastLogTextView: TextView
    
    private var updatesCount = 0
    private val requestCode = 123
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        activityTextView = findViewById(R.id.activityTextView)
        confidenceTextView = findViewById(R.id.confidenceTextView)
        permissionStatusTextView = findViewById(R.id.permissionStatusTextView)
        apiStatusTextView = findViewById(R.id.apiStatusTextView)
        updatesCountTextView = findViewById(R.id.updatesCountTextView)
        lastLogTextView = findViewById(R.id.lastLogTextView)
        
        // Verificar Google Play Services
        checkGooglePlayServices()
        
        // Verificar y solicitar permisos
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
        
        updatePermissionStatus(hasPermission)
        
        if (!hasPermission) {
            Log.d(TAG, "Solicitando permiso ACTIVITY_RECOGNITION")
            logStatus("Solicitando permiso ACTIVITY_RECOGNITION...")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                requestCode
            )
        } else {
            Log.d(TAG, "Permiso ACTIVITY_RECOGNITION ya concedido")
            logStatus("Permiso concedido. Iniciando Activity Recognition...")
            startActivityRecognition()
        }
        
        // Registrar receiver para actualizaciones locales
        LocalBroadcastManager.getInstance(this).registerReceiver(
            activityUpdateReceiver,
            android.content.IntentFilter(ActivityRecognitionReceiver.ACTION_ACTIVITY_UPDATE)
        )
    }
    
    private val activityUpdateReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            val activityType = intent?.getStringExtra(ActivityRecognitionReceiver.EXTRA_ACTIVITY_TYPE) ?: ""
            val confidence = intent?.getIntExtra(ActivityRecognitionReceiver.EXTRA_CONFIDENCE, 0) ?: 0
            
            updatesCount++
            Log.d(TAG, "Actividad recibida: $activityType con confianza: $confidence%")
            logStatus("Actividad detectada: $activityType ($confidence%)")
            updateUI(activityType, confidence)
            updateUpdatesCount()
        }
    }
    
    private fun updateUI(activityType: String, confidence: Int) {
        activityTextView.text = getActivityText(activityType)
        confidenceTextView.text = getString(R.string.confidence, confidence)
    }
    
    private fun getActivityText(activityType: String): String {
        return when (activityType) {
            "STILL" -> "Quieto / Descansando"
            "WALKING" -> "Caminando"
            "RUNNING" -> "Corriendo"
            "ON_FOOT" -> "A pie"
            "ON_BICYCLE" -> "En bicicleta"
            "IN_VEHICLE" -> "En vehículo"
            "TILTING" -> "Inclinando dispositivo"
            "UNKNOWN" -> getString(R.string.activity_unknown)
            else -> getString(R.string.activity_unknown)
        }
    }
    
    private fun startActivityRecognition() {
        try {
            activityRecognitionClient = ActivityRecognition.getClient(this)
            val intent = Intent(this, ActivityRecognitionReceiver::class.java)
            pendingIntent = android.app.PendingIntent.getBroadcast(
                this,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            // Solicitar actualizaciones cada 10 segundos (mínimo recomendado: 5-10 segundos)
            // Nota: Activity Recognition funciona mejor en dispositivos físicos
            activityRecognitionClient.requestActivityUpdates(
                10000, // intervalo en milisegundos (10 segundos)
                pendingIntent
            ).addOnSuccessListener {
                Log.d(TAG, "Activity Recognition iniciado correctamente")
                logStatus("Activity Recognition iniciado correctamente ✓")
                updateApiStatus(true, null)
                activityTextView.text = getString(R.string.activity_detecting)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error al iniciar Activity Recognition: ${e.message}", e)
                val errorMsg = "Error: ${e.message}"
                logStatus(errorMsg)
                updateApiStatus(false, e.message)
                Toast.makeText(this, "Error al iniciar detección de actividad: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al iniciar Activity Recognition: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = requestCode == this.requestCode && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        
        updatePermissionStatus(granted)
        
        if (granted) {
            Log.d(TAG, "Permiso ACTIVITY_RECOGNITION concedido")
            logStatus("Permiso concedido. Iniciando Activity Recognition...")
            startActivityRecognition()
        } else {
            Log.w(TAG, "Permiso ACTIVITY_RECOGNITION denegado")
            logStatus("Permiso denegado. La app no funcionará correctamente.")
            Toast.makeText(
                this,
                getString(R.string.permission_required),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun checkGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        
        if (resultCode != ConnectionResult.SUCCESS) {
            val errorMsg = apiAvailability.getErrorString(resultCode)
            logStatus("Google Play Services no disponible: $errorMsg")
            apiStatusTextView.text = "Google Play Services: ❌ No disponible"
            apiStatusTextView.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            logStatus("Google Play Services disponible ✓")
            apiStatusTextView.text = "Google Play Services: ✓ Disponible"
            apiStatusTextView.setTextColor(getColor(android.R.color.holo_green_dark))
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
    
    private fun updateApiStatus(success: Boolean, error: String?) {
        val statusText = if (success) {
            "Activity Recognition: ✓ Activo"
        } else {
            "Activity Recognition: ❌ Error${if (error != null) ": $error" else ""}"
        }
        apiStatusTextView.text = statusText
        apiStatusTextView.setTextColor(
            if (success) getColor(android.R.color.holo_green_dark)
            else getColor(android.R.color.holo_red_dark)
        )
    }
    
    private fun updateUpdatesCount() {
        updatesCountTextView.text = "Actualizaciones recibidas: $updatesCount"
    }
    
    private fun logStatus(message: String) {
        Log.d(TAG, "Status: $message")
        lastLogTextView.text = "Último log: $message"
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::activityRecognitionClient.isInitialized && ::pendingIntent.isInitialized) {
            activityRecognitionClient.removeActivityUpdates(pendingIntent)
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityUpdateReceiver)
    }
}


