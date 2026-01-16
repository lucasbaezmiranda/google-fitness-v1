package com.example.googlefitnessactivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_ACTIVITY_UPDATE = "com.example.googlefitnessactivity.ACTIVITY_UPDATE"
        const val EXTRA_ACTIVITY_TYPE = "activity_type"
        const val EXTRA_CONFIDENCE = "confidence"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val mostProbableActivity: DetectedActivity? = result?.mostProbableActivity
            
            mostProbableActivity?.let { activity ->
                val activityType = activity.type
                val confidence = activity.confidence
                
                // Enviar broadcast local a MainActivity
                if (context != null) {
                    val updateIntent = Intent(ACTION_ACTIVITY_UPDATE).apply {
                        putExtra(EXTRA_ACTIVITY_TYPE, getActivityTypeString(activityType))
                        putExtra(EXTRA_CONFIDENCE, confidence)
                    }
                    LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
                }
            }
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


