package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DatingDao {
  // Profiles
  @Query("SELECT * FROM dating_profiles WHERE isLikedByMe = 0 AND isPassedByMe = 0 AND isSuperLikedByMe = 0 AND isMutualMatch = 0 AND (:excludeUserId IS NULL OR id != :excludeUserId) ORDER BY id ASC")
  fun getActiveDeckProfiles(excludeUserId: String? = null): Flow<List<ProfileEntity>>

  @Query("SELECT * FROM dating_profiles WHERE isMutualMatch = 1 ORDER BY matchedTimestamp DESC")
  fun getMutualMatches(): Flow<List<ProfileEntity>>

  @Query("SELECT * FROM dating_profiles WHERE likedMe = 1")
  fun getProfilesWhoLikedMe(): Flow<List<ProfileEntity>>

  @Query("SELECT * FROM dating_profiles WHERE id = :id")
  suspend fun getProfileById(id: String): ProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfiles(profiles: List<ProfileEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfile(profile: ProfileEntity)

  @Query("DELETE FROM dating_profiles WHERE id = :id")
  suspend fun deleteProfileById(id: String)

  @Query("DELETE FROM dating_profiles")
  suspend fun deleteAllProfiles()

  @Update
  suspend fun updateProfile(profile: ProfileEntity)

  @Query("UPDATE dating_profiles SET isLikedByMe = 1, isMutualMatch = :isMutual, matchedTimestamp = :matchedTimestamp WHERE id = :id")
  suspend fun markLiked(id: String, isMutual: Boolean, matchedTimestamp: Long?)

  @Query("UPDATE dating_profiles SET likedMe = 1 WHERE id = :id")
  suspend fun markIncomingLike(id: String)

  @Query("UPDATE dating_profiles SET isLikedByMe = 1, isMutualMatch = 1, matchedTimestamp = :matchedTimestamp WHERE id = :id")
  suspend fun markMutualMatch(id: String, matchedTimestamp: Long)

  @Query("UPDATE dating_profiles SET isPassedByMe = 1 WHERE id = :id")
  suspend fun markPassed(id: String)

  @Query("UPDATE dating_profiles SET isSuperLikedByMe = 1, isMutualMatch = 1, matchedTimestamp = :matchedTimestamp WHERE id = :id")
  suspend fun markSuperLiked(id: String, matchedTimestamp: Long)

  @Query("UPDATE dating_profiles SET isLikedByMe = 0, isPassedByMe = 0, isSuperLikedByMe = 0, isMutualMatch = 0, matchedTimestamp = NULL WHERE id = :id")
  suspend fun rewindSwipe(id: String)

  @Query("UPDATE dating_profiles SET isPassedByMe = 0 WHERE isMutualMatch = 0 AND isLikedByMe = 0 AND isSuperLikedByMe = 0")
  suspend fun resetPassedProfiles()

  // Swipes and Tier tracking
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSwipeRecord(record: SwipeRecordEntity)

  @Query("SELECT * FROM swipe_records WHERE profileId = :profileId AND monthKey = :monthKey LIMIT 1")
  suspend fun getSwipeRecordForProfile(profileId: String, monthKey: String = "2026-09"): SwipeRecordEntity?

  @Query("SELECT COUNT(*) FROM swipe_records WHERE monthKey = :monthKey")
  fun getMonthlySwipeCountFlow(monthKey: String): Flow<Int>

  @Query("SELECT * FROM swipe_records ORDER BY timestamp DESC LIMIT 1")
  suspend fun getLastSwipeRecord(): SwipeRecordEntity?

  @Query("DELETE FROM swipe_records WHERE id = :id")
  suspend fun deleteSwipeRecord(id: Long)

  // User Profile
  @Query("SELECT * FROM user_profile ORDER BY isOnboardingCompleted DESC, id DESC LIMIT 1")
  fun getUserProfileFlow(): Flow<UserProfileEntity?>

  @Query("SELECT * FROM user_profile WHERE phoneNumber = :phone LIMIT 1")
  suspend fun getUserByPhone(phone: String): UserProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveUserProfile(user: UserProfileEntity)

  @Query("DELETE FROM user_profile WHERE id != :currentId")
  suspend fun deleteOtherUserProfiles(currentId: String)

  @Query("UPDATE user_profile SET isAccountDisabled = :disabled")
  suspend fun setAccountDisabled(disabled: Boolean)

  @Query("DELETE FROM user_profile")
  suspend fun deleteUserProfile()

  @Query("DELETE FROM chat_messages")
  suspend fun deleteAllMessages()

  @Query("DELETE FROM swipe_records")
  suspend fun deleteAllSwipeRecords()

  // Chat Messages
  @Query("SELECT * FROM chat_messages WHERE matchId = :matchId ORDER BY timestamp ASC")
  fun getMessagesForMatch(matchId: String): Flow<List<ChatMessageEntity>>

  @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
  fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity)

  @Query("SELECT * FROM chat_messages WHERE matchId = :matchId ORDER BY timestamp DESC LIMIT 1")
  suspend fun getLatestMessage(matchId: String): ChatMessageEntity?

  @Query("UPDATE chat_messages SET isRead = 1 WHERE matchId = :matchId AND isFromMe = 0")
  suspend fun markMessagesAsRead(matchId: String)

  @Query("DELETE FROM chat_messages WHERE matchId = :matchId")
  suspend fun deleteMessagesForMatch(matchId: String)

  @Query("UPDATE dating_profiles SET isMutualMatch = 0, isLikedByMe = 0, isSuperLikedByMe = 0, matchedTimestamp = NULL WHERE id = :matchId")
  suspend fun unmatchProfile(matchId: String)

  // Subscription
  @Query("SELECT * FROM subscription_info WHERE id = 'current_sub' LIMIT 1")
  fun getSubscriptionFlow(): Flow<SubscriptionEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveSubscription(subscription: SubscriptionEntity)

  @Query("SELECT COUNT(*) FROM dating_profiles")
  suspend fun getProfilesCount(): Int
}
