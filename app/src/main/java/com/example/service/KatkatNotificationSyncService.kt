package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.KatkatDatabase
import com.example.data.repository.KatkatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Persistent background sync service that maintains real-time listeners for
 * matches, incoming likes, and chat messages even when the app is closed
 * and removed from recent tasks.
 */
class KatkatNotificationSyncService : Service() {

  companion object {
    private const val TAG = "KatkatSyncService"
    const val SYNC_CHANNEL_ID = "katkat_sync_service_channel"
    private const val SYNC_CHANNEL_NAME = "Background Match & Message Sync"
    const val NOTIFICATION_ID = 9901

    fun startService(context: Context) {
      try {
        val intent = Intent(context, KatkatNotificationSyncService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(intent)
        } else {
          context.startService(intent)
        }
      } catch (e: Exception) {
        Log.w(TAG, "Notice starting sync service: ${e.message}")
        // Fallback: schedule alarm tick to perform direct sync in background
        KatkatNotificationReceiver.scheduleAlarmKeepAlive(context)
      }
    }
  }

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "KatkatNotificationSyncService created")
    createSyncNotificationChannel()
    startAsForeground()
    startBackgroundObservation()
    KatkatNotificationReceiver.scheduleAlarmKeepAlive(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startAsForeground()
    startBackgroundObservation()
    return START_STICKY
  }

  private fun createSyncNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      val channel = NotificationChannel(
        SYNC_CHANNEL_ID,
        SYNC_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_MIN
      ).apply {
        description = "Keeps Katkat active in background for real-time notifications"
        setShowBadge(false)
        enableLights(false)
        enableVibration(false)
      }
      notificationManager?.createNotificationChannel(channel)
    }
  }

  private fun startAsForeground() {
    createSyncNotificationChannel()

    val pendingIntent = PendingIntent.getActivity(
      this,
      0,
      Intent(this, MainActivity::class.java),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(this, SYNC_CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(getString(R.string.app_name))
      .setContentText("Monitoring new matches and messages")
      .setPriority(NotificationCompat.PRIORITY_MIN)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setOngoing(true)
      .setShowWhen(false)
      .setContentIntent(pendingIntent)
      .build()

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startForeground(
          NOTIFICATION_ID,
          notification,
          ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
      } else {
        startForeground(NOTIFICATION_ID, notification)
      }
    } catch (e: Exception) {
      Log.w(TAG, "startForeground notice: ${e.message}")
    }
  }

  private fun startBackgroundObservation() {
    serviceScope.launch {
      try {
        val dao = KatkatDatabase.getDatabase(applicationContext).datingDao()
        val user = dao.getUserProfileFlow().firstOrNull()
        if (user != null && user.isOnboardingCompleted && user.id.isNotBlank()) {
          val repo = KatkatRepository.getInstance(applicationContext)
          repo.startRealtimeCloudSync(user.id)
          Log.d(TAG, "Background realtime cloud sync active for user: ${user.id}")
        }
      } catch (e: Exception) {
        Log.w(TAG, "Error starting background observation: ${e.message}")
      }
    }
  }

  /**
   * Called when the user removes the app from the recent tasks list (swipes it away).
   * We immediately schedule an AlarmManager wake-up tick and restart intent to ensure
   * notifications continue to be received.
   */
  override fun onTaskRemoved(rootIntent: Intent?) {
    super.onTaskRemoved(rootIntent)
    Log.d(TAG, "onTaskRemoved: app swiped away from recents. Scheduling alarm keep-alive.")
    KatkatNotificationReceiver.scheduleAlarmKeepAlive(applicationContext)

    // Schedule immediate restart via AlarmManager
    try {
      val restartIntent = Intent(applicationContext, KatkatNotificationReceiver::class.java).apply {
        action = KatkatNotificationReceiver.ACTION_SYNC_TICK
      }
      val pendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        8802,
        restartIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager?.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          System.currentTimeMillis() + 2000L,
          pendingIntent
        )
      } else {
        alarmManager?.set(
          AlarmManager.RTC_WAKEUP,
          System.currentTimeMillis() + 2000L,
          pendingIntent
        )
      }
    } catch (e: Exception) {
      Log.w(TAG, "onTaskRemoved alarm scheduling notice: ${e.message}")
    }
  }

  override fun onDestroy() {
    Log.d(TAG, "KatkatNotificationSyncService destroyed, scheduling keep-alive alarm")
    KatkatNotificationReceiver.scheduleAlarmKeepAlive(applicationContext)
    serviceScope.cancel()
    super.onDestroy()
  }
}
