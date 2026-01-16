package com.example.googlefitnessactivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_ACTIVITY_UPDATE = "com.example.googlefitnessactivity.ACTIVITY_UPDATE"
        const val EXTRA_ACTIVITY_TYPE = "activity_type"
        const val EXTRA_CONFIDENCE = "confidence"
        private const val TAG = "ActivityRecognitionReceiver"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive llamado")
        if (intent == null) {
            Log.w(TAG, "Intent es null")
            return
        }
        
        if (!ActivityRecognitionResult.hasResult(intent)) {
            Log.w(TAG, "No hay resultado de Activity Recognition en el intent")
            return
        }
        
        val result = ActivityRecognitionResult.extractResult(intent)
        val mostProbableActivity: DetectedActivity? = result?.mostProbableActivity
        
        if (mostProbableActivity == null) {
            Log.w(TAG, "mostProbableActivity es null")
            return
        }
        
        val activityType = mostProbableActivity.type
        val confidence = mostProbableActivity.confidence
        
        Log.d(TAG, "Actividad detectada: tipo=$activityType, confianza=$confidence")
        
        // Enviar broadcast local a MainActivity
        if (context != null) {
            val updateIntent = Intent(ACTION_ACTIVITY_UPDATE).apply {
                putExtra(EXTRA_ACTIVITY_TYPE, getActivityTypeString(activityType))
                putExtra(EXTRA_CONFIDENCE, confidence)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
            Log.d(TAG, "Broadcast local enviado: $activityType")
        } else {
            Log.w(TAG, "Context es null, no se puede enviar broadcast")
        }
    }
    
    private fun getActivityTypeString(type: Int): String {
        return when (type) {
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN"
        }
    }
}


