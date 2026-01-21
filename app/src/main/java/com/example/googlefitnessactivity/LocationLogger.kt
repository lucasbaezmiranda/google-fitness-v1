package com.example.googlefitnessactivity

import android.content.Context
import android.location.Location
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LocationLogger(private val context: Context) {
    
    private val fileName = "gps_log.txt"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val TAG = "LocationLogger"
    
    fun logLocation(location: Location) {
        try {
            val file = File(context.cacheDir, fileName)
            val timestamp = dateFormat.format(Date())
            val entry = String.format(
                Locale.getDefault(),
                "%s|%.6f|%.6f|%.1f|%.1f|%.1f\n",
                timestamp,
                location.latitude,
                location.longitude,
                if (location.hasAccuracy()) location.accuracy else 0.0,
                if (location.hasAltitude()) location.altitude else 0.0,
                if (location.hasSpeed()) location.speed * 3.6 else 0.0 // km/h
            )
            
            file.appendText(entry)
            Log.d(TAG, "Ubicación guardada: $entry")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar ubicación: ${e.message}", e)
        }
    }
    
    fun getLogFile(): File {
        return File(context.cacheDir, fileName)
    }
    
    fun getLogContent(): String {
        return try {
            val file = getLogFile()
            if (file.exists()) {
                file.readText()
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al leer log: ${e.message}", e)
            ""
        }
    }
    
    fun getLogCount(): Int {
        val content = getLogContent()
        return if (content.isEmpty()) 0 else content.lines().size
    }
    
    fun clearLog() {
        try {
            val file = getLogFile()
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Log borrado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al borrar log: ${e.message}", e)
        }
    }
    
    fun formatLogForSharing(): String {
        val content = getLogContent()
        if (content.isEmpty()) {
            return "No hay datos GPS registrados."
        }
        
        val lines = content.lines().filter { it.isNotEmpty() }
        val header = "Registro GPS - ${lines.size} ubicaciones\n" +
                "Formato: Fecha|Latitud|Longitud|Precisión(m)|Altitud(m)|Velocidad(km/h)\n\n"
        
        return header + lines.joinToString("\n")
    }
}

