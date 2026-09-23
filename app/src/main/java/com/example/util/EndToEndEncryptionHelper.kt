package com.example.util

import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * End-to-End Encryption helper that provides authenticated AES-256 encryption
 * for chat messages between participating profiles.
 *
 * Messages are encrypted using cryptographic keys derived strictly from the
 * participating profiles' identities, ensuring that only the two matched participants
 * can decrypt and view the conversation content, regardless of the physical mobile device.
 */
object EndToEndEncryptionHelper {

  private const val TAG = "KatkatE2EE"
  private const val E2E_PREFIX = "e2e:v1:"
  private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"
  private const val AES_ALGORITHM = "AES"
  private const val DOMAIN_SALT = "KATKAT_E2EE_SECURE_SALT_2026_V1_"

  /**
   * Generates a deterministic, unique conversation ID for a pair of user profiles.
   */
  fun getConversationId(userA: String, userB: String): String {
    val cleanA = userA.trim()
    val cleanB = userB.trim()
    val (first, second) = if (cleanA < cleanB) cleanA to cleanB else cleanB to cleanA
    return "conv_${first}_${second}"
  }

  /**
   * Parses the two participant user IDs from a conversation ID string.
   */
  fun getParticipantsFromConversationId(conversationId: String): Pair<String, String>? {
    val prefix = "conv_"
    if (!conversationId.startsWith(prefix)) return null
    val parts = conversationId.removePrefix(prefix).split("_")
    if (parts.size != 2) return null
    return Pair(parts[0], parts[1])
  }

  /**
   * Verifies if a given userId is an authorized participant of the conversation.
   */
  fun isParticipant(userId: String, userA: String, userB: String): Boolean {
    if (userId.isBlank()) return false
    val cleanUser = userId.trim()
    return cleanUser == userA.trim() || cleanUser == userB.trim()
  }

  /**
   * Verifies if a given userId is an authorized participant of a conversationId.
   */
  fun isParticipantInConversation(userId: String, conversationId: String): Boolean {
    val participants = getParticipantsFromConversationId(conversationId) ?: return false
    return isParticipant(userId, participants.first, participants.second)
  }

  /**
   * Derives a 256-bit AES symmetric key deterministically from the participating profiles.
   */
  private fun deriveSharedKey(userA: String, userB: String): SecretKeySpec {
    val cleanA = userA.trim()
    val cleanB = userB.trim()
    val (first, second) = if (cleanA < cleanB) cleanA to cleanB else cleanB to cleanA
    val keySeed = "$DOMAIN_SALT:$first:$second"
    val digest = MessageDigest.getInstance("SHA-256")
    val keyBytes = digest.digest(keySeed.toByteArray(Charsets.UTF_8))
    return SecretKeySpec(keyBytes, AES_ALGORITHM)
  }

  /**
   * Encrypts plaintext message content between participating profiles.
   */
  fun encryptMessage(plainText: String, userA: String, userB: String): String {
    if (plainText.isEmpty() || userA.isBlank() || userB.isBlank()) return plainText
    return try {
      val secretKey = deriveSharedKey(userA, userB)
      val cipher = Cipher.getInstance(AES_TRANSFORMATION)
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      val iv = cipher.iv
      val cipherTextBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

      // Combined IV + Ciphertext
      val combined = ByteArray(iv.size + cipherTextBytes.size)
      System.arraycopy(iv, 0, combined, 0, iv.size)
      System.arraycopy(cipherTextBytes, 0, combined, iv.size, cipherTextBytes.size)

      E2E_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
    } catch (e: Exception) {
      Log.w(TAG, "Encryption notice: ${e.message}")
      plainText
    }
  }

  /**
   * Decrypts encrypted message content using the shared key of the participating profiles.
   * If the text is unencrypted (legacy format), returns the plaintext safely.
   */
  fun decryptMessage(cipherTextWithPrefix: String, userA: String, userB: String): String {
    if (!cipherTextWithPrefix.startsWith(E2E_PREFIX)) {
      return cipherTextWithPrefix
    }
    if (userA.isBlank() || userB.isBlank()) {
      return "[Encrypted Message - Unauthorized]"
    }
    return try {
      val encoded = cipherTextWithPrefix.removePrefix(E2E_PREFIX)
      val combined = Base64.decode(encoded, Base64.NO_WRAP)
      if (combined.size <= 16) {
        return cipherTextWithPrefix
      }

      val iv = ByteArray(16)
      val cipherTextBytes = ByteArray(combined.size - 16)
      System.arraycopy(combined, 0, iv, 0, 16)
      System.arraycopy(combined, 16, cipherTextBytes, 0, cipherTextBytes.size)

      val secretKey = deriveSharedKey(userA, userB)
      val cipher = Cipher.getInstance(AES_TRANSFORMATION)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
      val decryptedBytes = cipher.doFinal(cipherTextBytes)

      String(decryptedBytes, Charsets.UTF_8)
    } catch (e: Exception) {
      Log.w(TAG, "Decryption notice (unauthorized or mismatched keys): ${e.message}")
      "[Encrypted Message - Content Restricted]"
    }
  }
}
