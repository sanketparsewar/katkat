package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.model.SubscriptionTier

@Entity(tableName = "dating_profiles")
data class ProfileEntity(
  @PrimaryKey val id: String,
  val name: String,
  val age: Int,
  val occupation: String,
  val company: String,
  val education: String,
  val location: String,
  val bio: String,
  val photosJoined: String, // comma or pipe separated
  val promptQuestion: String?,
  val promptAnswer: String?,
  val passionsJoined: String,
  val zodiac: String,
  val height: String,
  val datingIntention: String,
  val drinking: String,
  val smoking: String,
  val pets: String,
  val anthemSong: String?,
  val anthemArtist: String?,
  val isVerified: Boolean,
  val likedMe: Boolean,
  val isLikedByMe: Boolean,
  val isPassedByMe: Boolean,
  val isSuperLikedByMe: Boolean,
  val isMutualMatch: Boolean,
  val matchedTimestamp: Long?
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
  @PrimaryKey val id: String = "my_profile",
  val name: String,
  val age: Int,
  val gender: String,
  val pronouns: String,
  val bio: String,
  val occupation: String,
  val education: String,
  val hometown: String,
  val height: String,
  val zodiac: String,
  val datingIntention: String,
  val drinking: String,
  val smoking: String,
  val pets: String,
  val passionsJoined: String,
  val photosJoined: String,
  val promptQuestion: String,
  val promptAnswer: String,
  val isOnboardingCompleted: Boolean = false
)

@Entity(tableName = "swipe_records")
data class SwipeRecordEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val profileId: String,
  val actionType: String, // "LIKE", "PASS", "SUPERLIKE"
  val monthKey: String, // "2026-09"
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey val id: String,
  val matchId: String,
  val senderId: String,
  val senderName: String,
  val text: String,
  val photoUri: String?,
  val timestamp: Long,
  val isFromMe: Boolean,
  val isRead: Boolean
)

@Entity(tableName = "subscription_info")
data class SubscriptionEntity(
  @PrimaryKey val id: String = "current_sub",
  val tierName: String, // "FREE", "TIER_1", "TIER_2"
  val swipesUsedThisMonth: Int,
  val currentMonthKey: String,
  val isAnnualBilling: Boolean,
  val subscriptionExpiryDate: String
)

class Converters {
  companion object {
    fun listToString(list: List<String>): String = list.joinToString("|||")
    fun stringToList(data: String): List<String> = if (data.isBlank()) emptyList() else data.split("|||")
  }
}
