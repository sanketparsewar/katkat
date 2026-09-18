package com.example.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.UUID

class FirebaseStorageManager {
  private val tag = "FirebaseStorageManager"

  private val storage: FirebaseStorage? by lazy {
    try {
      if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
        FirebaseStorage.getInstance()
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w(tag, "Firebase Storage not initialized: ${e.message}")
      null
    }
  }

  val isAvailable: Boolean
    get() = storage != null

  /**
   * Uploads an image URI to Firebase Cloud Storage under users/{userId}/photos/{uuid}.jpg
   * Returns the publicly accessible download URL string, or the original URI string on fallback.
   */
  suspend fun uploadProfileImage(context: Context, userId: String, imageUri: Uri): String {
    val uriString = imageUri.toString()
    if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
      return uriString
    }

    val storageInstance = storage
    if (storageInstance == null) {
      Log.d(tag, "Storage not available, using local image URI: $uriString")
      return uriString
    }

    return try {
      val cleanUserId = userId.ifBlank { "guest_${System.currentTimeMillis()}" }
      val fileName = "photo_${UUID.randomUUID()}.jpg"
      val photoRef = storageInstance.reference
        .child("users")
        .child(cleanUserId)
        .child("photos")
        .child(fileName)

      val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
      if (inputStream != null) {
        val metadata = StorageMetadata.Builder()
          .setContentType("image/jpeg")
          .build()

        photoRef.putStream(inputStream, metadata).await()
        val downloadUrl = photoRef.downloadUrl.await().toString()
        Log.d(tag, "Successfully uploaded image to Firebase Storage: $downloadUrl")
        downloadUrl
      } else {
        uriString
      }
    } catch (e: Exception) {
      Log.w(tag, "Firebase storage upload notice: ${e.message}. Using local image URI.")
      uriString
    }
  }
}
