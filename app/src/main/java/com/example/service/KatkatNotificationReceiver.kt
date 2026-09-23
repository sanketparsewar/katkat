package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.KatkatDatabase
import com.example.data.model.KatkatNotification
import com.example.data.model.KatkatNotificationType
import com.example.data.repository.KatkatRepository
import com.example.util.PushNotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * BroadcastReceiver responsible for receiving AlarmManager heartbeats and
 * system BOOT_COMPLETED signals. Performs direct Firestore checks for incoming
 * likes, matches, and messages to guarantee notifications are delivered even
 * when the app is completely closed and cleared from recent tasks.
 */
class KatkatNotificationReceiver : BroadcastReceiver() {

  companion object {
    private const val TAG = "KatkatNotifReceiver"
    const val ACTION_SYNC_TICK = "com.example.ACTION_NOTIFICATION_SYNC_ALARM"
    private const val ALARM_INTERVAL_MS = 60_000L // 1 minute interval

    fun scheduleAlarmKeepAlive(context: Context) {
      try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, KatkatNotificationReceiver::class.java).apply {
          action = ACTION_SYNC_TICK
        }
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          8801,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + ALARM_INTERVAL_MS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
          alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
      } catch (e: Exception) {
        Log.w(TAG, "Failed to schedule alarm: ${e.message}")
      }
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    Log.d(TAG, "KatkatNotificationReceiver onReceive: action=${intent.action}")

    // Schedule the next heartbeat to keep checking periodically
    scheduleAlarmKeepAlive(context)

    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        // Attempt to ensure foreground/background service is active
        KatkatNotificationSyncService.startService(context)

        // Perform active direct check against Firestore
        performDirectSync(context)
      } catch (e: Exception) {
        Log.w(TAG, "Error in notification receiver sync: ${e.message}")
      } finally {
        pendingResult.finish()
      }
    }
  }

  private suspend fun performDirectSync(context: Context) {
    val database = KatkatDatabase.getDatabase(context.applicationContext)
    val dao = database.datingDao()
    val user = dao.getUserProfileFlow().firstOrNull() ?: return
    if (!user.isOnboardingCompleted || user.id.isBlank()) return
    val currentUserId = user.id

    val repo = KatkatRepository.getInstance(context)

    // Ensure realtime listeners are connected
    repo.startRealtimeCloudSync(currentUserId)

    val firestore = try {
      FirebaseFirestore.getInstance()
    } catch (_: Exception) {
      null
    } ?: return

    // 1. Direct query check for recent cloud notifications
    try {
      val notifSnapshot = firestore.collection("users")
        .document(currentUserId)
        .collection("notifications")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(10)
        .get()
        .await()

      for (doc in notifSnapshot.documents) {
        val d = doc.data ?: continue
        val notifId = doc.id
        val existing = dao.getNotificationById(notifId)
        if (existing == null) {
          val notif = KatkatNotification(
            id = notifId,
            userId = d["userId"] as? String ?: currentUserId,
            type = try {
              KatkatNotificationType.valueOf(d["type"] as? String ?: "")
            } catch (_: Exception) {
              KatkatNotificationType.SYSTEM_NOTIFICATION
            },
            title = d["title"] as? String ?: "New Notification",
            message = d["message"] as? String ?: "",
            timestamp = (d["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            isRead = d["isRead"] as? Boolean ?: false,
            senderProfileId = d["senderProfileId"] as? String,
            senderProfileName = d["senderProfileName"] as? String,
            senderAvatarUrl = d["senderAvatarUrl"] as? String,
            deepLinkTarget = d["deepLinkTarget"] as? String
          )
          repo.saveAndPushNotificationPublic(notif)
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Direct notifications fetch notice: ${e.message}")
    }

    // 2. Direct query check for recent incoming likes
    try {
      val likesSnapshot = firestore.collection("likes")
        .whereEqualTo("likedUserId", currentUserId)
        .limit(20)
        .get()
        .await()

      for (doc in likesSnapshot.documents) {
        val senderId = doc.getString("fromUserId") ?: continue
        if (senderId.isBlank() || senderId == currentUserId) continue

        val profileEntity = dao.getProfileById(senderId)
        val wasAlreadyLiked = profileEntity?.likedMe == true
        if (!wasAlreadyLiked) {
          dao.markIncomingLike(senderId)
          val senderName = profileEntity?.name ?: doc.getString("senderName") ?: "Someone"
          val avatar = profileEntity?.photosJoined?.split("|||")?.firstOrNull()
            ?: doc.getString("senderAvatarUrl")

          val likeNotif = KatkatNotification(
            id = "like_$senderId",
            userId = currentUserId,
            type = KatkatNotificationType.PROFILE_ACTIVITY,
            title = "New Like! ✨",
            message = "Someone liked you",
            timestamp = System.currentTimeMillis(),
            senderProfileId = senderId,
            senderProfileName = senderName,
            senderAvatarUrl = avatar,
            deepLinkTarget = "likes_you"
          )
          repo.saveAndPushNotificationPublic(likeNotif)
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Direct likes check notice: ${e.message}")
    }
  }
}
