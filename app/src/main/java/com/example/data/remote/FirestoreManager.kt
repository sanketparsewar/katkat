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
      val cloudPhotos = profile.photos.filter { it.isNotBlank() }
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
        "photos" to cloudPhotos,
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
        "maxDistanceKm" to profile.maxDistanceKm,
        "minAgePreference" to profile.minAgePreference,
        "maxAgePreference" to profile.maxAgePreference,
        "interestedInGender" to profile.interestedInGender,
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

  suspend fun deleteUserProfile(userId: String): Boolean {
    val db = firestore ?: return false
    return try {
      db.collection("users").document(userId).delete().await()
      true
    } catch (e: Exception) {
      Log.w(tag, "Firestore profile delete notice: ${e.message}")
      false
    }
  }

  private fun parseUserProfileData(docId: String, data: Map<String, Any?>, fallbackCountryCode: String): UserProfile {
    @Suppress("UNCHECKED_CAST")
    return UserProfile(
      id = docId,
      name = data["name"] as? String ?: "",
      age = (data["age"] as? Number)?.toInt() ?: 0,
      gender = data["gender"] as? String ?: "",
      pronouns = data["pronouns"] as? String ?: "",
      bio = data["bio"] as? String ?: "",
      occupation = data["occupation"] as? String ?: "",
      education = data["education"] as? String ?: "",
      hometown = data["hometown"] as? String ?: "",
      height = data["height"] as? String ?: "",
      zodiac = data["zodiac"] as? String ?: "",
      datingIntention = data["datingIntention"] as? String ?: "",
      drinking = data["drinking"] as? String ?: "",
      smoking = data["smoking"] as? String ?: "",
      pets = data["pets"] as? String ?: "",
      passions = (data["passions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
      photos = (data["photos"] as? List<*>)?.filterIsInstance<String>()?.filter { it.startsWith("http://") || it.startsWith("https://") } ?: emptyList(),
      promptQuestion = data["promptQuestion"] as? String ?: "My simple pleasures in life...",
      promptAnswer = data["promptAnswer"] as? String ?: "",
      isOnboardingCompleted = data["isOnboardingCompleted"] as? Boolean ?: false,
      phoneNumber = data["phoneNumber"] as? String ?: "",
      countryCode = data["countryCode"] as? String ?: fallbackCountryCode,
      email = data["email"] as? String ?: "",
      dob = data["dob"] as? String ?: "",
      currentLocationCity = data["currentLocationCity"] as? String ?: "",
      currentLocationCountry = data["currentLocationCountry"] as? String ?: "",
      latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
      longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
      isPhoneVerified = data["isPhoneVerified"] as? Boolean ?: false,
      isAccountDisabled = data["isAccountDisabled"] as? Boolean ?: false,
      maxDistanceKm = (data["maxDistanceKm"] as? Number)?.toInt() ?: 50,
      minAgePreference = (data["minAgePreference"] as? Number)?.toInt() ?: 18,
      maxAgePreference = (data["maxAgePreference"] as? Number)?.toInt() ?: 35,
      interestedInGender = data["interestedInGender"] as? String ?: ""
    )
  }

  suspend fun fetchUserByPhone(phoneNumber: String, countryCode: String = "+91"): UserProfile? {
    val db = firestore ?: return null
    val cleanDigits = phoneNumber.filter { it.isDigit() }
    val cleanCountryCode = countryCode.filter { it.isDigit() }
    val fullWithPlus = if (phoneNumber.startsWith("+")) phoneNumber else "+$cleanCountryCode$cleanDigits"
    val fullWithSpace = "$countryCode $phoneNumber"

    // 1. Direct document lookup by predictable document IDs in users collection
    val docIdsToTry = listOf(
      "user_$cleanDigits",
      "user_$cleanCountryCode$cleanDigits",
      "user_${phoneNumber.trim()}",
      "user_$fullWithPlus"
    ).distinct()

    for (docId in docIdsToTry) {
      try {
        val doc = db.collection("users").document(docId).get().await()
        if (doc.exists()) {
          val data = doc.data
          if (data != null) {
            val user = parseUserProfileData(doc.id, data, countryCode)
            if (user.isOnboardingCompleted && user.name.isNotBlank()) {
              Log.d(tag, "Firestore fetchUserByPhone found by docId '$docId': ${user.name}")
              return user
            }
          }
        }
      } catch (e: Exception) {
        Log.w(tag, "Firestore fetchUserByPhone docId '$docId' notice: ${e.message}")
      }
    }

    // 2. Query candidates by phoneNumber field in users collection
    val candidates = listOf(
      phoneNumber.trim(),
      cleanDigits,
      fullWithPlus,
      fullWithSpace,
      "$cleanCountryCode$cleanDigits",
      "+$cleanCountryCode$cleanDigits"
    ).distinct()

    for (candidate in candidates) {
      try {
        val querySnapshot = db.collection("users")
          .whereEqualTo("phoneNumber", candidate)
          .limit(1)
          .get()
          .await()
        val doc = querySnapshot.documents.firstOrNull()
        if (doc != null) {
          val data = doc.data ?: continue
          val user = parseUserProfileData(doc.id, data, countryCode)
          if (user.isOnboardingCompleted && user.name.isNotBlank()) {
            Log.d(tag, "Firestore fetchUserByPhone found by candidate field '$candidate': ${user.name}")
            return user
          }
        }
      } catch (e: Exception) {
        Log.w(tag, "Firestore fetchUserByPhone candidate '$candidate' notice: ${e.message}")
      }
    }

    // 3. Fallback check: Look up dating_pool in case registered as public discovery profile
    for (docId in docIdsToTry) {
      try {
        val doc = db.collection("dating_pool").document(docId).get().await()
        if (doc.exists()) {
          val data = doc.data
          if (data != null) {
            val user = parseUserProfileData(doc.id, data, countryCode)
            if (user.name.isNotBlank()) {
              Log.d(tag, "Firestore fetchUserByPhone found in dating_pool '$docId': ${user.name}")
              return user.copy(isOnboardingCompleted = true)
            }
          }
        }
      } catch (e: Exception) {
        Log.w(tag, "Firestore dating_pool check notice: ${e.message}")
      }
    }

    return null
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

  /**
   * Helper to compute deterministic chat ID between two users.
   */
  fun getCanonicalChatId(userA: String, userB: String): String {
    val cleanA = userA.trim()
    val cleanB = userB.trim()
    return if (cleanA < cleanB) "${cleanA}_$cleanB" else "${cleanB}_$cleanA"
  }

  suspend fun sendChatMessage(matchId: String, message: ChatMessage, myUserId: String = ""): Boolean {
    val db = firestore ?: return false
    return try {
      val targetChatId = if (myUserId.isNotBlank() && myUserId != matchId) {
        getCanonicalChatId(myUserId, matchId)
      } else {
        matchId
      }

      val msgData = mapOf(
        "id" to message.id,
        "matchId" to matchId,
        "senderId" to message.senderId,
        "senderName" to message.senderName,
        "text" to message.text,
        "photoUri" to null,
        "timestamp" to message.timestamp,
        "isRead" to false
      )

      // Write ONLY to the private canonical chat document for this user pair
      db.collection("chats")
        .document(targetChatId)
        .collection("messages")
        .document(message.id)
        .set(msgData)
        .await()

      // Update conversation metadata on the private pair conversation
      db.collection("chats")
        .document(targetChatId)
        .set(
          mapOf(
            "lastMessage" to message.text,
            "lastMessageTimestamp" to message.timestamp,
            "lastSenderName" to message.senderName,
            "lastSenderId" to message.senderId,
            "participants" to listOfNotNull(myUserId.takeIf { it.isNotBlank() }, matchId).distinct()
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

  fun observeChatMessages(matchId: String, myUserId: String = ""): Flow<List<ChatMessage>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val targetChatId = if (myUserId.isNotBlank() && myUserId != matchId) {
      getCanonicalChatId(myUserId, matchId)
    } else {
      matchId
    }

    val messagesMap = java.util.concurrent.ConcurrentHashMap<String, ChatMessage>()

    fun emitCurrentMessages() {
      val sorted = messagesMap.values.sortedBy { it.timestamp }
      trySend(sorted)
    }

    val collectionRef = db.collection("chats")
      .document(targetChatId)
      .collection("messages")
      .orderBy("timestamp", Query.Direction.ASCENDING)

    val reg = collectionRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
          Log.w(tag, "Firestore chat listener: rules pending. Serving local.")
        } else {
          Log.w(tag, "Firestore chat listener notice: ${error.message}")
        }
        return@addSnapshotListener
      }
      if (snapshot != null) {
        for (doc in snapshot.documents) {
          try {
            val data = doc.data ?: continue
            val senderId = data["senderId"] as? String ?: ""
            val isMine = if (myUserId.isNotBlank()) senderId == myUserId else (data["isFromMe"] as? Boolean ?: false)
            val isReadCloud = (data["isRead"] as? Boolean) ?: false
            val chatMessage = ChatMessage(
              id = doc.id,
              matchId = matchId,
              senderId = senderId,
              senderName = data["senderName"] as? String ?: "",
              text = data["text"] as? String ?: "",
              photoUri = null,
              timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
              isFromMe = isMine,
              isRead = if (isMine) true else isReadCloud
            )
            messagesMap[doc.id] = chatMessage
          } catch (_: Exception) {}
        }
        emitCurrentMessages()
      }
    }

    awaitClose {
      reg.remove()
    }
  }

  suspend fun markChatMessagesAsRead(matchId: String, myUserId: String = ""): Boolean {
    val db = firestore ?: return false
    if (myUserId.isBlank() || matchId.isBlank()) return false
    return try {
      val targetChatId = getCanonicalChatId(myUserId, matchId)
      val unreadSnapshot = db.collection("chats")
        .document(targetChatId)
        .collection("messages")
        .whereEqualTo("senderId", matchId)
        .whereEqualTo("isRead", false)
        .get()
        .await()

      for (doc in unreadSnapshot.documents) {
        doc.reference.update("isRead", true).await()
      }
      true
    } catch (e: Exception) {
      Log.w(tag, "Notice marking messages as read in cloud: ${e.message}")
      false
    }
  }

  suspend fun deleteChatMessages(matchId: String, myUserId: String = ""): Boolean {
    val db = firestore ?: return false
    return try {
      val targetChatId = if (myUserId.isNotBlank() && myUserId != matchId) {
        getCanonicalChatId(myUserId, matchId)
      } else {
        matchId
      }

      val messagesSnapshot = db.collection("chats")
        .document(targetChatId)
        .collection("messages")
        .get()
        .await()

      for (msgDoc in messagesSnapshot.documents) {
        msgDoc.reference.delete().await()
      }

      db.collection("chats").document(targetChatId).delete().await()
      true
    } catch (e: Exception) {
      Log.w(tag, "Failed to clear cloud chat history: ${e.message}")
      false
    }
  }

  // ==========================================
  // Real-Time Likes and Mutual Matches Syncing
  // ==========================================

  data class CloudLike(
    val fromUserId: String,
    val toUserId: String,
    val isSuperLike: Boolean,
    val timestamp: Long
  )

  data class CloudMatch(
    val user1Id: String,
    val user2Id: String,
    val matchedTimestamp: Long
  )

  /**
   * Pushes a like or super-like event to Cloud Firestore in real time.
   * Stored under "likes/{fromUserId}_{toUserId}" for fast deterministic lookup and listening.
   */
  suspend fun sendLike(fromUserId: String, toUserId: String, isSuperLike: Boolean = false): Boolean {
    val db = firestore ?: return false
    return try {
      val docId = "${fromUserId}_$toUserId"
      val data = mapOf(
        "fromUserId" to fromUserId,
        "toUserId" to toUserId,
        "isSuperLike" to isSuperLike,
        "timestamp" to System.currentTimeMillis()
      )
      db.collection("likes").document(docId).set(data, SetOptions.merge()).await()
      Log.d(tag, "Cloud like recorded: $fromUserId -> $toUserId (superLike=$isSuperLike)")
      true
    } catch (e: Exception) {
      Log.w(tag, "Notice recording cloud like: ${e.message}")
      false
    }
  }

  /**
   * Pushes a pass event to Cloud Firestore.
   * Stored under "passes/{fromUserId}_{toUserId}".
   */
  suspend fun sendPass(fromUserId: String, toUserId: String): Boolean {
    val db = firestore ?: return false
    if (fromUserId.isBlank() || toUserId.isBlank()) return false
    return try {
      val docId = "${fromUserId}_$toUserId"
      val data = mapOf(
        "fromUserId" to fromUserId,
        "toUserId" to toUserId,
        "timestamp" to System.currentTimeMillis()
      )
      db.collection("passes").document(docId).set(data, SetOptions.merge()).await()
      Log.d(tag, "Cloud pass recorded: $fromUserId -> $toUserId")
      true
    } catch (e: Exception) {
      Log.w(tag, "Notice recording cloud pass: ${e.message}")
      false
    }
  }

  /**
   * Checks if the other user already liked this user in Firestore.
   */
  suspend fun checkMutualLike(fromUserId: String, targetUserId: String): Boolean {
    val db = firestore ?: return false
    return try {
      val reverseDocId = "${targetUserId}_$fromUserId"
      val reverseDoc = db.collection("likes").document(reverseDocId).get().await()
      if (reverseDoc.exists()) {
        return true
      }
      // Also query by from/to fields
      val q = db.collection("likes")
        .whereEqualTo("fromUserId", targetUserId)
        .whereEqualTo("toUserId", fromUserId)
        .limit(1)
        .get()
        .await()
      !q.isEmpty
    } catch (e: Exception) {
      Log.w(tag, "Notice checking mutual like: ${e.message}")
      false
    }
  }

  /**
   * Registers a mutual match in Firestore under "matches/{canonicalMatchId}"
   * and inside the match document.
   */
  suspend fun registerMutualMatch(userA: String, userB: String): Boolean {
    val db = firestore ?: return false
    return try {
      val (u1, u2) = if (userA < userB) Pair(userA, userB) else Pair(userB, userA)
      val matchDocId = "${u1}_$u2"
      val now = System.currentTimeMillis()
      val data = mapOf(
        "id" to matchDocId,
        "user1Id" to u1,
        "user2Id" to u2,
        "users" to listOf(u1, u2),
        "matchedTimestamp" to now
      )
      db.collection("matches").document(matchDocId).set(data, SetOptions.merge()).await()
      Log.d(tag, "Cloud mutual match registered: $matchDocId")
      true
    } catch (e: Exception) {
      Log.w(tag, "Notice registering cloud mutual match: ${e.message}")
      false
    }
  }

  /**
   * Listens in real time for any incoming likes sent TO the current user.
   * Whenever another user (e.g. Phone 2) likes the current user (e.g. Phone 1),
   * this callback flow emits immediately.
   */
  fun observeIncomingLikes(myUserId: String): Flow<List<CloudLike>> = callbackFlow {
    val db = firestore
    if (db == null || myUserId.isBlank()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val query = db.collection("likes")
      .whereEqualTo("toUserId", myUserId)

    val listenerRegistration = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.w(tag, "observeIncomingLikes notice: ${error.message}")
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val likes = snapshot.documents.mapNotNull { doc ->
          try {
            val data = doc.data ?: return@mapNotNull null
            CloudLike(
              fromUserId = data["fromUserId"] as? String ?: return@mapNotNull null,
              toUserId = data["toUserId"] as? String ?: myUserId,
              isSuperLike = data["isSuperLike"] as? Boolean ?: false,
              timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
          } catch (_: Exception) {
            null
          }
        }
        trySend(likes)
      }
    }

    awaitClose {
      listenerRegistration.remove()
    }
  }

  /**
   * Listens in real time for mutual matches involving the current user.
   * Triggers on both Phone 1 and Phone 2 simultaneously when a match is created in Firestore.
   */
  fun observeMutualMatches(myUserId: String): Flow<List<CloudMatch>> = callbackFlow {
    val db = firestore
    if (db == null || myUserId.isBlank()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val query = db.collection("matches")
      .whereArrayContains("users", myUserId)

    val listenerRegistration = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.w(tag, "observeMutualMatches notice: ${error.message}")
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val matches = snapshot.documents.mapNotNull { doc ->
          try {
            val data = doc.data ?: return@mapNotNull null
            CloudMatch(
              user1Id = data["user1Id"] as? String ?: "",
              user2Id = data["user2Id"] as? String ?: "",
              matchedTimestamp = (data["matchedTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
          } catch (_: Exception) {
            null
          }
        }
        trySend(matches)
      }
    }

    awaitClose {
      listenerRegistration.remove()
    }
  }

  /**
   * Fetches the set of user IDs that the current user has already passed in Firestore.
   */
  suspend fun fetchOutgoingPassedUserIds(myUserId: String): Set<String> {
    val db = firestore ?: return emptySet()
    if (myUserId.isBlank()) return emptySet()
    return try {
      val querySnapshot = db.collection("passes")
        .whereEqualTo("fromUserId", myUserId)
        .get()
        .await()
      querySnapshot.documents.mapNotNull { it.getString("toUserId") }.toSet()
    } catch (e: Exception) {
      Log.w(tag, "Notice fetching outgoing passes: ${e.message}")
      emptySet()
    }
  }

  /**
   * Fetches the set of user IDs that the current user has already liked in Firestore.
   */
  suspend fun fetchOutgoingLikedUserIds(myUserId: String): Set<String> {
    val db = firestore ?: return emptySet()
    if (myUserId.isBlank()) return emptySet()
    return try {
      val querySnapshot = db.collection("likes")
        .whereEqualTo("fromUserId", myUserId)
        .get()
        .await()
      querySnapshot.documents.mapNotNull { it.getString("toUserId") }.toSet()
    } catch (e: Exception) {
      Log.w(tag, "Notice fetching outgoing likes: ${e.message}")
      emptySet()
    }
  }

  /**
   * Fetches the set of user IDs with whom the current user already has a mutual match in Firestore.
   */
  suspend fun fetchMutualMatchedUserIds(myUserId: String): Set<String> {
    val db = firestore ?: return emptySet()
    if (myUserId.isBlank()) return emptySet()
    return try {
      val querySnapshot = db.collection("matches")
        .whereArrayContains("users", myUserId)
        .get()
        .await()
      querySnapshot.documents.mapNotNull { doc ->
        val u1 = doc.getString("user1Id")
        val u2 = doc.getString("user2Id")
        if (u1 == myUserId) u2 else u1
      }.filter { it.isNotBlank() }.toSet()
    } catch (e: Exception) {
      Log.w(tag, "Notice fetching mutual matched IDs: ${e.message}")
      emptySet()
    }
  }

  /**
   * Blocks a user in Cloud Firestore.
   * Records the block, removes mutual match, likes, and cleans up messages.
   */
  suspend fun blockUser(fromUserId: String, targetUserId: String): Boolean {
    val db = firestore ?: return false
    if (fromUserId.isBlank() || targetUserId.isBlank()) return false
    return try {
      val docId = "${fromUserId}_$targetUserId"
      val data = mapOf(
        "fromUserId" to fromUserId,
        "blockedUserId" to targetUserId,
        "timestamp" to System.currentTimeMillis()
      )
      db.collection("blocks").document(docId).set(data, SetOptions.merge()).await()

      // Delete mutual match record if existing
      val (u1, u2) = if (fromUserId < targetUserId) Pair(fromUserId, targetUserId) else Pair(targetUserId, fromUserId)
      val matchDocId = "${u1}_$u2"
      try {
        db.collection("matches").document(matchDocId).delete().await()
      } catch (_: Exception) {}

      // Delete like entries between them
      try {
        db.collection("likes").document("${fromUserId}_$targetUserId").delete().await()
        db.collection("likes").document("${targetUserId}_$fromUserId").delete().await()
      } catch (_: Exception) {}

      // Delete canonical chat messages
      val chatId = getCanonicalChatId(fromUserId, targetUserId)
      deleteChatMessages(targetUserId, fromUserId)

      Log.d(tag, "User successfully blocked: $fromUserId blocked $targetUserId")
      true
    } catch (e: Exception) {
      Log.w(tag, "Notice blocking user: ${e.message}")
      false
    }
  }

  /**
   * Fetches all user IDs that either:
   * 1. The current user has blocked (outgoing)
   * 2. Have blocked the current user (incoming)
   * Ensures two-way blocking everywhere in discovery and chats.
   */
  suspend fun fetchAllBlockedOrBlockingUserIds(myUserId: String): Set<String> {
    val db = firestore ?: return emptySet()
    if (myUserId.isBlank()) return emptySet()
    return try {
      val blockedByMe = db.collection("blocks")
        .whereEqualTo("fromUserId", myUserId)
        .get()
        .await()
        .documents
        .mapNotNull { it.getString("blockedUserId") }

      val blockingMe = db.collection("blocks")
        .whereEqualTo("blockedUserId", myUserId)
        .get()
        .await()
        .documents
        .mapNotNull { it.getString("fromUserId") }

      (blockedByMe + blockingMe).filter { it.isNotBlank() }.toSet()
    } catch (e: Exception) {
      Log.w(tag, "Notice fetching blocked users: ${e.message}")
      emptySet()
    }
  }

  /**
   * Listens in real time for any blocks involving the current user (incoming or outgoing).
   */
  fun observeBlockedUserIds(myUserId: String): Flow<Set<String>> = callbackFlow {
    val db = firestore
    if (db == null || myUserId.isBlank()) {
      trySend(emptySet())
      close()
      return@callbackFlow
    }

    var outgoingBlocked = setOf<String>()
    var incomingBlocked = setOf<String>()

    fun emitCombined() {
      trySend(outgoingBlocked + incomingBlocked)
    }

    val reg1 = db.collection("blocks")
      .whereEqualTo("fromUserId", myUserId)
      .addSnapshotListener { snapshot, error ->
        if (error == null && snapshot != null) {
          outgoingBlocked = snapshot.documents.mapNotNull { it.getString("blockedUserId") }.toSet()
          emitCombined()
        }
      }

    val reg2 = db.collection("blocks")
      .whereEqualTo("blockedUserId", myUserId)
      .addSnapshotListener { snapshot, error ->
        if (error == null && snapshot != null) {
          incomingBlocked = snapshot.documents.mapNotNull { it.getString("fromUserId") }.toSet()
          emitCombined()
        }
      }

    awaitClose {
      reg1.remove()
      reg2.remove()
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

  suspend fun publishUserToDiscovery(profile: UserProfile): Boolean {
    val db = firestore ?: return false
    return try {
      // CRITICAL: Only publish remote cloud URLs (HTTP/HTTPS) to discovery so all devices can display photos
      val cloudPhotos = profile.photos.filter { it.startsWith("http://") || it.startsWith("https://") }
      val data = mapOf(
        "id" to profile.id,
        "name" to profile.name,
        "age" to profile.age,
        "gender" to profile.gender,
        "latitude" to profile.latitude,
        "longitude" to profile.longitude,
        "occupation" to profile.occupation.ifBlank { "Katkat Member" },
        "company" to profile.education,
        "education" to profile.education,
        "location" to profile.currentLocationCity.ifBlank { profile.hometown.ifBlank { "Nearby" } },
        "bio" to profile.bio,
        "photos" to cloudPhotos,
        "promptQuestion" to profile.promptQuestion,
        "promptAnswer" to profile.promptAnswer,
        "passions" to profile.passions,
        "zodiac" to profile.zodiac,
        "height" to profile.height,
        "datingIntention" to profile.datingIntention,
        "drinking" to profile.drinking,
        "smoking" to profile.smoking,
        "pets" to profile.pets,
        "anthemSong" to "",
        "anthemArtist" to "",
        "isVerified" to profile.isPhoneVerified,
        "likedMe" to false,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("discovery_profiles").document(profile.id)
        .set(data, SetOptions.merge())
        .await()
      true
    } catch (e: Exception) {
      Log.w(tag, "Failed to publish user to discovery: ${e.message}")
      false
    }
  }

  suspend fun fetchAllCommunityProfiles(excludeUserId: String): List<DatingProfile> {
    val db = firestore ?: return emptyList()
    return try {
      val snapshot = db.collection("users")
        .whereEqualTo("isOnboardingCompleted", true)
        .get()
        .await()
      snapshot.documents.mapNotNull { doc ->
        if (doc.id == excludeUserId) return@mapNotNull null
        val data = doc.data ?: return@mapNotNull null
        val name = data["name"] as? String ?: return@mapNotNull null
        if (name.isBlank()) return@mapNotNull null
        val photos = (data["photos"] as? List<*>)?.filterIsInstance<String>()?.filter { it.startsWith("http://") || it.startsWith("https://") } ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        DatingProfile(
          id = doc.id,
          name = name,
          age = (data["age"] as? Number)?.toInt() ?: 0,
          gender = data["gender"] as? String ?: "",
          occupation = data["occupation"] as? String ?: "",
          company = data["education"] as? String ?: "",
          education = data["education"] as? String ?: "",
          location = data["currentLocationCity"] as? String ?: (data["hometown"] as? String ?: ""),
          latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
          longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
          bio = data["bio"] as? String ?: "",
          photos = photos,
          promptQuestion = data["promptQuestion"] as? String ?: "",
          promptAnswer = data["promptAnswer"] as? String ?: "",
          passions = (data["passions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
          zodiac = data["zodiac"] as? String ?: "",
          height = data["height"] as? String ?: "",
          datingIntention = data["datingIntention"] as? String ?: "",
          drinking = data["drinking"] as? String ?: "",
          smoking = data["smoking"] as? String ?: "",
          pets = data["pets"] as? String ?: "",
          anthemSong = "",
          anthemArtist = "",
          isVerified = data["isPhoneVerified"] as? Boolean ?: true,
          likedMe = false
        )
      }
    } catch (e: Exception) {
      Log.w(tag, "Failed to fetch community profiles: ${e.message}")
      emptyList()
    }
  }
}

