package com.example.data.model

data class DatingProfile(
  val id: String,
  val name: String,
  val age: Int,
  val occupation: String,
  val company: String = "",
  val education: String = "",
  val location: String = "2 miles away",
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
  val name: String = "Alex Rivera",
  val age: Int = 24,
  val gender: String = "Non-binary",
  val pronouns: String = "They/Them",
  val bio: String = "Creative photographer & warm coffee enthusiast ☕ Searching for someone to explore indie bookstores, cook pasta from scratch, and swap vinyl records with.",
  val occupation: String = "UX Designer & Visual Artist",
  val education: String = "NYU Tisch School of the Arts",
  val hometown: String = "Brooklyn, NY",
  val height: String = "5'9\"",
  val zodiac: String = "Sagittarius ♐",
  val datingIntention: String = "Long-term relationship 💖",
  val drinking: String = "Socially 🍷",
  val smoking: String = "Never 🚭",
  val pets: String = "Have 2 rescue cats 🐾",
  val passions: List<String> = listOf("Photography", "Coffee", "Vinyl Records", "Art Galleries", "Cooking", "Cats", "Hiking", "Indie Pop"),
  val photos: List<String> = listOf(
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&q=80",
    "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&q=80",
    "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=800&q=80"
  ),
  val promptQuestion: String = "My simple pleasures in life...",
  val promptAnswer: String = "Freshly baked croissants, golden hour light, and warm purring cats on a Sunday morning."
)

data class ChatMessage(
  val id: String,
  val matchId: String,
  val senderId: String,
  val senderName: String,
  val text: String,
  val photoUri: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val isFromMe: Boolean = false,
  val isRead: Boolean = true
)

data class MatchConversation(
  val matchProfile: DatingProfile,
  val matchTimeMillis: Long,
  val lastMessage: String,
  val lastMessageTimeMillis: Long,
  val unreadCount: Int = 0,
  val isOnline: Boolean = true
)
