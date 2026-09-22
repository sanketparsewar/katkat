package com.example.data.model

import java.util.UUID

enum class KatkatNotificationType {
  NEW_MATCH,
  NEW_MESSAGE,
  MESSAGE_READ,
  PROFILE_ACTIVITY,
  SYSTEM_NOTIFICATION
}

data class KatkatNotification(
  val id: String = UUID.randomUUID().toString(),
  val userId: String = "",
  val type: KatkatNotificationType = KatkatNotificationType.SYSTEM_NOTIFICATION,
  val title: String = "",
  val message: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false,
  val senderProfileId: String? = null,
  val senderProfileName: String? = null,
  val senderAvatarUrl: String? = null,
  val deepLinkTarget: String? = null
)
