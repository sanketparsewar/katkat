package com.example.data.model

data class DatingProfile(
  val id: String,
  val name: String,
  val age: Int,
  val gender: String = "",
  val occupation: String,
  val company: String = "",
  val education: String = "",
  val location: String = "2 miles away",
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
  val bio: String,
  val photos: List<String>,
  val promptQuestion: String? = null,
  val promptAnswer: String? = null,
  val passions: List<String> = emptyList(),
  val zodiac: String = "Leo ♌",
  val height: String = "5'8\" (173cm)",
  val datingIntention: String = "Long-term relationship",
  val drinking: String = "Socially",
  val smoking: String = "No",
  val pets: String = "Cat lover 🐱",
  val anthemSong: String? = null,
  val anthemArtist: String? = null,
  val isVerified: Boolean = true,
  val likedMe: Boolean = false,
  val isLikedByMe: Boolean = false,
  val isPassedByMe: Boolean = false,
  val isSuperLikedByMe: Boolean = false,
  val isMutualMatch: Boolean = false,
  val matchedTimestamp: Long? = null
)

data class UserProfile(
  val id: String = "my_profile",
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
  val passions: List<String> = emptyList(),
  val photos: List<String> = emptyList(),
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
) {
  fun calculateProfileStrength(): Int {
    return calculateProfileCompletion().first
  }

  fun calculateProfileCompletion(): Pair<Int, List<String>> {
    val remaining = mutableListOf<String>()
    var score = 0

    if (name.isNotBlank()) score += 10 else remaining.add("Name")
    if (age > 0 || dob.isNotBlank()) score += 10 else remaining.add("Age / Birthday")
    if (gender.isNotBlank()) score += 5 else remaining.add("Gender")

    val validPhotos = photos.filter { it.isNotBlank() }
    if (validPhotos.isNotEmpty()) {
      score += 15
      if (validPhotos.size >= 2) {
        score += 10
      } else {
        remaining.add("Add 2+ photos")
      }
    } else {
      remaining.add("Add photos")
    }

    if (bio.isNotBlank()) score += 15 else remaining.add("Bio")
    if (currentLocationCity.isNotBlank() || hometown.isNotBlank() || currentLocationCountry.isNotBlank()) score += 10 else remaining.add("Location")
    if (occupation.isNotBlank() || education.isNotBlank()) score += 10 else remaining.add("Work or Education")
    if (passions.isNotEmpty()) score += 10 else remaining.add("Interests")
    if (promptAnswer.isNotBlank()) score += 10 else remaining.add("Profile Prompt")
    if (datingIntention.isNotBlank() || drinking.isNotBlank() || smoking.isNotBlank() || pets.isNotBlank() || zodiac.isNotBlank()) {
      score += 5
    } else {
      remaining.add("Lifestyle info")
    }

    return Pair(score.coerceIn(0, 100), remaining)
  }
}

data class ChatMessage(
  val id: String,
  val matchId: String,
  val senderId: String,
  val senderName: String,
  val text: String,
  val photoUri: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val isFromMe: Boolean = false,
  val isRead: Boolean = true,
  val conversationId: String = "",
  val currentUserId: String = "",
  val recipientId: String = "",
  val isSending: Boolean = false,
  val isFailed: Boolean = false
)

data class MatchConversation(
  val matchProfile: DatingProfile,
  val conversationId: String = "",
  val matchTimeMillis: Long,
  val lastMessage: String,
  val lastMessageTimeMillis: Long,
  val unreadCount: Int = 0,
  val isOnline: Boolean = true
)
