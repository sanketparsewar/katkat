package com.example.data.remote

import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager {

  private val tag = "FirestoreManager"

  val firestore: FirebaseFirestore? by lazy {
    try {
      val db = FirebaseFirestore.getInstance()
      // Attempt anonymous auth in background to establish credentials
      try {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
          auth.signInAnonymously().addOnSuccessListener {
            Log.d(tag, "Firebase anonymous sign-in success: ${it.user?.uid}")
          }.addOnFailureListener {
            Log.w(tag, "Firebase anonymous sign-in notice: ${it.message}")
          }
        }
      } catch (e: Exception) {
        Log.w(tag, "FirebaseAuth init skipped: ${e.message}")
      }
      db
    } catch (e: Exception) {
      Log.w(tag, "Firestore initialization skipped/unavailable: ${e.message}")
      null
    }
  }

  val isAvailable: Boolean
    get() = firestore != null

  // ==========================================
  // User Profile Real-Time Syncing
  // ==========================================

  suspend fun syncUserProfile(profile: UserProfile): Boolean {
    val db = firestore ?: return false
    return try {
      val data = mapOf(
        "id" to profile.id,
        "name" to profile.name,
        "age" to profile.age,
        "gender" to profile.gender,
        "pronouns" to profile.pronouns,
        "bio" to profile.bio,
        "occupation" to profile.occupation,
        "education" to profile.education,
        "hometown" to profile.hometown,
        "height" to profile.height,
        "zodiac" to profile.zodiac,
        "datingIntention" to profile.datingIntention,
        "drinking" to profile.drinking,
        "smoking" to profile.smoking,
        "pets" to profile.pets,
        "passions" to profile.passions,
        "photos" to profile.photos,
        "promptQuestion" to profile.promptQuestion,
        "promptAnswer" to profile.promptAnswer,
        "isOnboardingCompleted" to profile.isOnboardingCompleted,
        "phoneNumber" to profile.phoneNumber,
        "countryCode" to profile.countryCode,
        "email" to profile.email,
        "dob" to profile.dob,
        "currentLocationCity" to profile.currentLocationCity,
        "currentLocationCountry" to profile.currentLocationCountry,
        "latitude" to profile.latitude,
        "longitude" to profile.longitude,
        "isPhoneVerified" to profile.isPhoneVerified,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("users")
        .document(profile.id)
        .set(data, SetOptions.merge())
        .await()
      true
    } catch (e: Exception) {
      if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        Log.w(tag, "Firestore profile sync notice: Permission denied in Firebase console rules. Using local database.")
      } else {
        Log.w(tag, "Firestore profile sync notice: ${e.message}")
      }
      false
    }
  }

  fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(null)
      close()
      return@callbackFlow
    }

    val docRef = db.collection("users").document(userId)
    val listenerRegistration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
          Log.w(tag, "Firestore profile listener: Firestore rules are locked or pending. Serving data from local Room database.")
        } else {
          Log.w(tag, "Firestore profile listener notice: ${error.message}")
        }
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        try {
          val data = snapshot.data
          if (data != null) {
            @Suppress("UNCHECKED_CAST")
            val profile = UserProfile(
              id = snapshot.id,
              name = data["name"] as? String ?: "Alex Rivera",
              age = (data["age"] as? Number)?.toInt() ?: 24,
              gender = data["gender"] as? String ?: "Non-binary",
              pronouns = data["pronouns"] as? String ?: "They/Them",
              bio = data["bio"] as? String ?: "",
              occupation = data["occupation"] as? String ?: "",
              education = data["education"] as? String ?: "",
              hometown = data["hometown"] as? String ?: "",
              height = data["height"] as? String ?: "5'9\"",
              zodiac = data["zodiac"] as? String ?: "Sagittarius ♐",
              datingIntention = data["datingIntention"] as? String ?: "Long-term relationship 💖",
              drinking = data["drinking"] as? String ?: "Socially 🍷",
              smoking = data["smoking"] as? String ?: "Never 🚭",
              pets = data["pets"] as? String ?: "Have 2 rescue cats 🐾",
              passions = (data["passions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
              photos = (data["photos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
              promptQuestion = data["promptQuestion"] as? String ?: "My simple pleasures in life...",
              promptAnswer = data["promptAnswer"] as? String ?: "Freshly baked croissants, golden hour light, and warm purring cats on a Sunday morning.",
              isOnboardingCompleted = data["isOnboardingCompleted"] as? Boolean ?: false,
              phoneNumber = data["phoneNumber"] as? String ?: "",
              countryCode = data["countryCode"] as? String ?: "+91",
              email = data["email"] as? String ?: "",
              dob = data["dob"] as? String ?: "",
              currentLocationCity = data["currentLocationCity"] as? String ?: "",
              currentLocationCountry = data["currentLocationCountry"] as? String ?: "",
              latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
              longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
              isPhoneVerified = data["isPhoneVerified"] as? Boolean ?: false
            )
            trySend(profile)
          }
        } catch (e: Exception) {
          Log.w(tag, "Failed parsing user profile snapshot: ${e.message}")
        }
      }
    }

    awaitClose {
      listenerRegistration.remove()
    }
  }

  // ==========================================
  // Real-Time Messaging System
  // ==========================================

  suspend fun sendChatMessage(matchId: String, message: ChatMessage): Boolean {
    val db = firestore ?: return false
    return try {
      val msgData = mapOf(
        "id" to message.id,
        "matchId" to message.matchId,
        "senderId" to message.senderId,
        "senderName" to message.senderName,
        "text" to message.text,
        "photoUri" to message.photoUri,
        "timestamp" to message.timestamp,
        "isFromMe" to message.isFromMe,
        "isRead" to message.isRead
      )

      db.collection("chats")
        .document(matchId)
        .collection("messages")
        .document(message.id)
        .set(msgData)
        .await()

      // Update conversation metadata
      db.collection("chats")
        .document(matchId)
        .set(
          mapOf(
            "lastMessage" to message.text,
            "lastMessageTimestamp" to message.timestamp,
            "lastSenderName" to message.senderName
          ),
          SetOptions.merge()
        )
        .await()

      true
    } catch (e: Exception) {
      if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        Log.w(tag, "Firestore message send notice: Firestore rules are locked. Stored locally.")
      } else {
        Log.w(tag, "Firestore message send notice: ${e.message}")
      }
      false
    }
  }

  fun observeChatMessages(matchId: String): Flow<List<ChatMessage>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val collectionRef = db.collection("chats")
      .document(matchId)
      .collection("messages")
      .orderBy("timestamp", Query.Direction.ASCENDING)

    val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
          Log.w(tag, "Firestore chat listener: Firestore rules are locked. Serving chat messages from local Room database.")
        } else {
          Log.w(tag, "Firestore chat listener notice: ${error.message}")
        }
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val messages = snapshot.documents.mapNotNull { doc ->
          try {
            val data = doc.data ?: return@mapNotNull null
            ChatMessage(
              id = doc.id,
              matchId = data["matchId"] as? String ?: matchId,
              senderId = data["senderId"] as? String ?: "",
              senderName = data["senderName"] as? String ?: "",
              text = data["text"] as? String ?: "",
              photoUri = data["photoUri"] as? String,
              timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
              isFromMe = data["isFromMe"] as? Boolean ?: false,
              isRead = data["isRead"] as? Boolean ?: true
            )
          } catch (e: Exception) {
            null
          }
        }
        trySend(messages)
      }
    }

    awaitClose {
      listenerRegistration.remove()
    }
  }

  // ==========================================
  // Discovery Profiles Real-Time Syncing
  // ==========================================

  suspend fun syncDiscoveryProfiles(profiles: List<DatingProfile>): Boolean {
    val db = firestore ?: return false
    return try {
      val batch = db.batch()
      profiles.forEach { profile ->
        val doc = db.collection("discovery_profiles").document(profile.id)
        val data = mapOf(
          "id" to profile.id,
          "name" to profile.name,
          "age" to profile.age,
          "occupation" to profile.occupation,
          "company" to profile.company,
          "education" to profile.education,
          "location" to profile.location,
          "bio" to profile.bio,
          "photos" to profile.photos,
          "promptQuestion" to profile.promptQuestion,
          "promptAnswer" to profile.promptAnswer,
          "passions" to profile.passions,
          "zodiac" to profile.zodiac,
          "height" to profile.height,
          "datingIntention" to profile.datingIntention,
          "drinking" to profile.drinking,
          "smoking" to profile.smoking,
          "pets" to profile.pets,
          "anthemSong" to profile.anthemSong,
          "anthemArtist" to profile.anthemArtist,
          "isVerified" to profile.isVerified,
          "likedMe" to profile.likedMe,
          "updatedAt" to System.currentTimeMillis()
        )
        batch.set(doc, data, SetOptions.merge())
      }
      batch.commit().await()
      true
    } catch (e: Exception) {
      if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        Log.w(tag, "Firestore discovery sync notice: Firestore rules are locked. Serving from local database.")
      } else {
        Log.w(tag, "Firestore discovery sync notice: ${e.message}")
      }
      false
    }
  }
}

