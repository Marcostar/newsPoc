package com.m10.everystory.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.m10.everystory.R

// Opens the Saved tab; the nav graph and the manifest both know this address.
const val SAVED_DEEP_LINK = "everystory://saved"

private const val CHANNEL_ID = "saved_stories"
private const val NOTIFICATION_ID = 1

class SavedReminderNotifier(private val context: Context) {

    // Its own channel, so readers can turn these off in Settings without losing anything else.
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_saved),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.notification_channel_saved_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun show(storyCount: Int) {
        // On Android 13+ the reader may have said no; then we stay quiet.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
            .setContentTitle(context.getString(R.string.notification_saved_title))
            .setContentText(
                context.resources.getQuantityString(R.plurals.notification_saved_text, storyCount, storyCount),
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openSavedIntent())
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    // A fresh task, so the nav graph builds the stack from the link: Today underneath, Saved on top.
    private fun openSavedIntent(): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, SAVED_DEEP_LINK.toUri())
            .setPackage(context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
