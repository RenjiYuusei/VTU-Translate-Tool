package com.vtu.translate.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vtu.translate.R
import com.vtu.translate.VtuTranslateApp
import com.vtu.translate.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class TranslationService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var translationJob: Job? = null
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    
    companion object {
        const val ACTION_STOP = "com.vtu.translate.ACTION_STOP"
        const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check if this is a stop action
        if (intent?.action == ACTION_STOP) {
            stopTranslation()
            return START_NOT_STICKY
        }
        
        // Create a notification channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "translation_channel",
                getString(R.string.translation_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.translation_notification_channel_description)
            }
            // Register the channel with the system
            val notificationManagerChannel: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManagerChannel.createNotificationChannel(channel)
        }

        val app = application as VtuTranslateApp
        val translationRepository = app.translationRepository

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open app when notification is clicked
        val openAppIntent = Intent(this, MainActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val openAppPendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create a notification builder with a stop action
        val stopIntent = Intent(this, TranslationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent: PendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Check current translation state
        val resources = translationRepository.stringResources.value
        // Count translated (including errors) for consistency with app UI
        val translatedCount = resources.count { it.translatedValue.isNotBlank() || it.hasError }
        val totalCount = resources.size
        val contentText = if (totalCount > 0) {
            "$translatedCount/$totalCount"
        } else {
            getString(R.string.preparing_translation)
        }

        notificationBuilder = NotificationCompat.Builder(this, "translation_channel")
            .setSmallIcon(R.drawable.ic_translate)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_stop_translate, getString(R.string.stop_translation), stopPendingIntent)

        // If there are resources, show progress
        if (totalCount > 0) {
            notificationBuilder.setProgress(totalCount, translatedCount, false)
        }

        // Start the service in the foreground
        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        // Start translation in a coroutine
        translationJob = serviceScope.launch {
            val initialResources = translationRepository.stringResources.value
            val total = initialResources.size
            
            if (total == 0) {
                stopSelf()
                return@launch
            }
            
            // Check if translation is already running
            if (!translationRepository.isTranslating.value) {
                // If not translating but service is started, just monitor the state
                // This happens when app is reopened
                // Count both translated and error items for consistency
                notifyProgress(
                    initialResources.count { it.translatedValue.isNotBlank() || it.hasError },
                    total
                )
            }
            
            // Monitor translation progress
            translationRepository.stringResources.collect { updatedResources ->
                if (!translationRepository.isTranslating.value) {
                    // Translation completed or stopped
                    notificationBuilder.setContentText(getString(R.string.translation_complete))
                        .setProgress(0, 0, false)
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                    delay(2000) // Show completion for 2 seconds
                    stopSelf()
                    return@collect
                }
                
                // Count translated items (including both successful and error)
                val currentTranslatedCount = updatedResources.count { it.translatedValue.isNotBlank() || it.hasError }
                
                // Update notification with progress
                notifyProgress(currentTranslatedCount, total)
                
                // If all items are processed (translated or errored), prepare to stop
                if (currentTranslatedCount >= total && translationRepository.isTranslating.value) {
                    // Double check if translation is actually complete
                    delay(500) // Small delay to ensure state is updated
                }
            }
        }

        // Start background translation logic here

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun stopTranslation() {
        val app = application as VtuTranslateApp
        app.translationRepository.stopTranslation()
        translationJob?.cancel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }
    
    private fun notifyProgress(current: Int, total: Int) {
        val percentage = if (total > 0) (current * 100 / total) else 0
        val contentText = "$current/$total ($percentage%)"
        
        notificationBuilder.setContentText(contentText)
            .setProgress(total, current, false)
        
        // Update notification
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
    
    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
