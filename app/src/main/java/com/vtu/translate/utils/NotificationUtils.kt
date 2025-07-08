package com.vtu.translate.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vtu.translate.R

object NotificationUtils {
    private const val CHANNEL_ID = "translation_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.translation_notification_channel_name)
            val descriptionText = context.getString(R.string.translation_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showProgressNotification(context: Context, progress: Int, maxProgress: Int) {
        val percentage = if (maxProgress > 0) (progress * 100 / maxProgress) else 0
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_translate)
            .setContentTitle(context.getString(R.string.translation_in_progress))
            .setContentText("$progress/$maxProgress ($percentage%)")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(maxProgress, progress, false)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }

    fun showCompletionNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_translate)
            .setContentTitle(context.getString(R.string.translation_complete))
            .setPriority(NotificationCompat.PRIORITY_LOW)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }
}
