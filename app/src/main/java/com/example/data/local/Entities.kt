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
  val gender: String = "",
  val occupation: String,
  val company: String,
  val education: String,
  val location: String,
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
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
  val matchedTimestamp: Long?,
  val isAccountDisabled: Boolean = false,
  val isVip: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
  @PrimaryKey val id: String = "my_profile",
  val name: String = "",
  val age: Int = 0,
  val gender: String = "",
  val pronouns: String = "",
  val bio: String = "",
  val occupation: String = "",
  val education: String = "",
  val hometown: String = "",
  val height: String = "",
  val zodiac: String = "",
  val datingIntention: String = "",
  val drinking: String = "",
  val smoking: String = "",
  val pets: String = "",
  val passionsJoined: String = "",
  val photosJoined: String = "",
  val promptQuestion: String = "My simple pleasures in life...",
  val promptAnswer: String = "",
  val isOnboardingCompleted: Boolean = false,
  val phoneNumber: String = "",
  val countryCode: String = "+91",
  val email: String = "",
  val dob: String = "",
  val currentLocationCity: String = "",
  val currentLocationCountry: String = "",
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
  val isPhoneVerified: Boolean = false,
  val isAccountDisabled: Boolean = false,
  val maxDistanceKm: Int = 50,
  val minAgePreference: Int = 18,
  val maxAgePreference: Int = 35,
  val interestedInGender: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

fun UserProfileEntity.toDomain(): com.example.data.model.UserProfile =
  com.example.data.model.UserProfile(
    id = id,
    name = name,
    age = age,
    gender = gender,
    pronouns = pronouns,
    bio = bio,
    occupation = occupation,
    education = education,
    hometown = hometown,
    height = height,
    zodiac = zodiac,
    datingIntention = datingIntention,
    drinking = drinking,
    smoking = smoking,
    pets = pets,
    passions = Converters.stringToList(passionsJoined),
    photos = Converters.stringToList(photosJoined),
    promptQuestion = promptQuestion,
    promptAnswer = promptAnswer,
    isOnboardingCompleted = isOnboardingCompleted,
    phoneNumber = phoneNumber,
    countryCode = countryCode,
    email = email,
    dob = dob,
    currentLocationCity = currentLocationCity,
    currentLocationCountry = currentLocationCountry,
    latitude = latitude,
    longitude = longitude,
    isPhoneVerified = isPhoneVerified,
    isAccountDisabled = isAccountDisabled,
    maxDistanceKm = maxDistanceKm,
    minAgePreference = minAgePreference,
    maxAgePreference = maxAgePreference,
    interestedInGender = interestedInGender,
    createdAt = createdAt
  )

fun com.example.data.model.UserProfile.toEntity(): UserProfileEntity =
  UserProfileEntity(
    id = id.ifBlank { "my_profile" },
    name = name,
    age = age,
    gender = gender,
    pronouns = pronouns,
    bio = bio,
    occupation = occupation,
    education = education,
    hometown = hometown,
    height = height,
    zodiac = zodiac,
    datingIntention = datingIntention,
    drinking = drinking,
    smoking = smoking,
    pets = pets,
    passionsJoined = Converters.listToString(passions),
    photosJoined = Converters.listToString(photos),
    promptQuestion = promptQuestion,
    promptAnswer = promptAnswer,
    isOnboardingCompleted = isOnboardingCompleted,
    phoneNumber = phoneNumber,
    countryCode = countryCode,
    email = email,
    dob = dob,
    currentLocationCity = currentLocationCity,
    currentLocationCountry = currentLocationCountry,
    latitude = latitude,
    longitude = longitude,
    isPhoneVerified = isPhoneVerified,
    isAccountDisabled = isAccountDisabled,
    maxDistanceKm = maxDistanceKm,
    minAgePreference = minAgePreference,
    maxAgePreference = maxAgePreference,
    interestedInGender = interestedInGender,
    createdAt = createdAt
  )

fun ProfileEntity.toDomain(): com.example.data.model.DatingProfile =
  com.example.data.model.DatingProfile(
    id = id,
    name = name,
    age = age,
    gender = gender,
    occupation = occupation,
    company = company,
    education = education,
    location = location,
    latitude = latitude,
    longitude = longitude,
    bio = bio,
    photos = Converters.stringToList(photosJoined),
    promptQuestion = promptQuestion,
    promptAnswer = promptAnswer,
    passions = Converters.stringToList(passionsJoined),
    zodiac = zodiac,
    height = height,
    datingIntention = datingIntention,
    drinking = drinking,
    smoking = smoking,
    pets = pets,
    anthemSong = anthemSong,
    anthemArtist = anthemArtist,
    isVerified = isVerified,
    likedMe = likedMe,
    isLikedByMe = isLikedByMe,
    isPassedByMe = isPassedByMe,
    isSuperLikedByMe = isSuperLikedByMe,
    isMutualMatch = isMutualMatch,
    matchedTimestamp = matchedTimestamp,
    isAccountDisabled = isAccountDisabled,
    isVip = isVip
  )

fun com.example.data.model.DatingProfile.toEntity(): ProfileEntity =
  ProfileEntity(
    id = id,
    name = name,
    age = age,
    gender = gender,
    occupation = occupation,
    company = company,
    education = education,
    location = location,
    latitude = latitude,
    longitude = longitude,
    bio = bio,
    photosJoined = Converters.listToString(photos),
    promptQuestion = promptQuestion,
    promptAnswer = promptAnswer,
    passionsJoined = Converters.listToString(passions),
    zodiac = zodiac,
    height = height,
    datingIntention = datingIntention,
    drinking = drinking,
    smoking = smoking,
    pets = pets,
    anthemSong = anthemSong,
    anthemArtist = anthemArtist,
    isVerified = isVerified,
    likedMe = likedMe,
    isLikedByMe = isLikedByMe,
    isPassedByMe = isPassedByMe,
    isSuperLikedByMe = isSuperLikedByMe,
    isMutualMatch = isMutualMatch,
    matchedTimestamp = matchedTimestamp,
    isAccountDisabled = isAccountDisabled,
    isVip = isVip
  )

@Entity(tableName = "swipe_records")
data class SwipeRecordEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val profileId: String,
  val actionType: String, // "LIKE", "PASS", "SUPERLIKE"
  val monthKey: String, // "2026-09"
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "chat_messages",
  indices = [
    androidx.room.Index(value = ["matchId"]),
    androidx.room.Index(value = ["conversationId"]),
    androidx.room.Index(value = ["currentUserId"])
  ]
)
data class ChatMessageEntity(
  @PrimaryKey val id: String,
  val matchId: String,
  val senderId: String,
  val senderName: String,
  val text: String,
  val photoUri: String?,
  val timestamp: Long,
  val isFromMe: Boolean,
  val isRead: Boolean,
  val conversationId: String = "",
  val currentUserId: String = "",
  val recipientId: String = "",
  val isSending: Boolean = false,
  val isFailed: Boolean = false
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

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val type: String, // NEW_MATCH, NEW_MESSAGE, MESSAGE_READ, PROFILE_ACTIVITY, SYSTEM_NOTIFICATION
  val title: String,
  val message: String,
  val timestamp: Long,
  val isRead: Boolean,
  val senderProfileId: String?,
  val senderProfileName: String?,
  val senderAvatarUrl: String?,
  val deepLinkTarget: String?
)

fun AppNotificationEntity.toDomain(): com.example.data.model.KatkatNotification =
  com.example.data.model.KatkatNotification(
    id = id,
    userId = userId,
    type = try {
      com.example.data.model.KatkatNotificationType.valueOf(type)
    } catch (_: Exception) {
      com.example.data.model.KatkatNotificationType.SYSTEM_NOTIFICATION
    },
    title = title,
    message = message,
    timestamp = timestamp,
    isRead = isRead,
    senderProfileId = senderProfileId,
    senderProfileName = senderProfileName,
    senderAvatarUrl = senderAvatarUrl,
    deepLinkTarget = deepLinkTarget
  )

fun com.example.data.model.KatkatNotification.toEntity(): AppNotificationEntity =
  AppNotificationEntity(
    id = id,
    userId = userId,
    type = type.name,
    title = title,
    message = message,
    timestamp = timestamp,
    isRead = isRead,
    senderProfileId = senderProfileId,
    senderProfileName = senderProfileName,
    senderAvatarUrl = senderAvatarUrl,
    deepLinkTarget = deepLinkTarget
  )

class Converters {
  companion object {
    fun listToString(list: List<String>): String = list.filter { it.isNotBlank() }.joinToString("|||")
    fun stringToList(data: String): List<String> = if (data.isBlank()) emptyList() else data.split("|||").map { it.trim() }.filter { it.isNotBlank() }
  }
}
