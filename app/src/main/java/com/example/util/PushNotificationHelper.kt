package com.example.util

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
import com.example.MainActivity
import com.example.R
import com.example.data.model.KatkatNotification
import com.example.data.model.KatkatNotificationType

object PushNotificationHelper {

  const val CHANNEL_ID = "katkat_dating_alerts_channel"
  private const val CHANNEL_NAME = "Katkat Dating Alerts"
  private const val CHANNEL_DESC = "Notifications for new matches, messages, and profile likes"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableLights(true)
        enableVibration(true)
        setShowBadge(true)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun postPushNotification(context: Context, notification: KatkatNotification) {
    if (!hasNotificationPermission(context)) {
      return
    }

    createNotificationChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("target_route", notification.deepLinkTarget)
      putExtra("notification_id", notification.id)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      notification.id.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val iconRes = R.mipmap.ic_launcher

    val notificationCategory = when (notification.type) {
      KatkatNotificationType.NEW_MESSAGE -> NotificationCompat.CATEGORY_MESSAGE
      KatkatNotificationType.NEW_MATCH -> NotificationCompat.CATEGORY_SOCIAL
      KatkatNotificationType.PROFILE_ACTIVITY -> NotificationCompat.CATEGORY_EVENT
      KatkatNotificationType.MESSAGE_READ -> NotificationCompat.CATEGORY_STATUS
      KatkatNotificationType.SYSTEM_NOTIFICATION -> NotificationCompat.CATEGORY_SYSTEM
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(iconRes)
      .setContentTitle(notification.title)
      .setContentText(notification.message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(notificationCategory)
      .setAutoCancel(true)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setContentIntent(pendingIntent)

    try {
      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify(notification.id.hashCode(), builder.build())
    } catch (_: SecurityException) {
      // Handled if permissions revoked at runtime
    } catch (_: Exception) {
      // Graceful fallback
    }
  }
}
