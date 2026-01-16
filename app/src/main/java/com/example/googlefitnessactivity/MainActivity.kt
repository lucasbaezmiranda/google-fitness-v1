package com.example.googlefitnessactivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient

class MainActivity : AppCompatActivity() {
    
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var pendingIntent: android.app.PendingIntent
    private lateinit var activityTextView: TextView
    private lateinit var confidenceTextView: TextView
    
    private val requestCode = 123
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        activityTextView = findViewById(R.id.activityTextView)
        confidenceTextView = findViewById(R.id.confidenceTextView)
        
        // Verificar y solicitar permisos
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                requestCode
            )
        } else {
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
            
            updateUI(activityType, confidence)
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
        activityRecognitionClient = ActivityRecognition.getClient(this)
        val intent = Intent(this, ActivityRecognitionReceiver::class.java)
        pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        // Solicitar actualizaciones cada 5 segundos
        activityRecognitionClient.requestActivityUpdates(
            5000, // intervalo en milisegundos
            pendingIntent
        )
        
        activityTextView.text = getString(R.string.activity_detecting)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startActivityRecognition()
        } else {
            Toast.makeText(
                this,
                getString(R.string.permission_required),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::activityRecognitionClient.isInitialized && ::pendingIntent.isInitialized) {
            activityRecognitionClient.removeActivityUpdates(pendingIntent)
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityUpdateReceiver)
    }
}

