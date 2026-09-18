package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
   * Saves an image to local permanent storage and uploads to Firebase Cloud Storage.
   * Returns the cloud download URL, or the persistent local file URI if cloud storage is offline.
   */
  suspend fun uploadProfileImage(context: Context, userId: String, imageUri: Uri): String = withContext(Dispatchers.IO) {
    val uriString = imageUri.toString()
    if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
      return@withContext uriString
    }

    // 1. Always save a durable copy to local persistent files directory
    val localFile = saveUriToPersistentFile(context, imageUri)
    val fallbackLocalUri = if (localFile != null) Uri.fromFile(localFile).toString() else uriString

    val storageInstance = storage
    if (storageInstance == null) {
      Log.d(tag, "Cloud Storage not configured, using persistent local URI: $fallbackLocalUri")
      return@withContext fallbackLocalUri
    }

    // 2. Upload to Firebase Storage
    try {
      val cleanUserId = userId.ifBlank { "guest_${System.currentTimeMillis()}" }
      val fileName = "photo_${UUID.randomUUID()}.jpg"
      val photoRef = storageInstance.reference
        .child("users")
        .child(cleanUserId)
        .child("photos")
        .child(fileName)

      val inputStream: InputStream? = if (localFile != null && localFile.exists()) {
        localFile.inputStream()
      } else {
        context.contentResolver.openInputStream(imageUri)
      }

      if (inputStream != null) {
        val metadata = StorageMetadata.Builder()
          .setContentType("image/jpeg")
          .build()

        photoRef.putStream(inputStream, metadata).await()
        val downloadUrl = photoRef.downloadUrl.await().toString()
        Log.d(tag, "Successfully uploaded image to Firebase Storage: $downloadUrl")
        downloadUrl
      } else {
        fallbackLocalUri
      }
    } catch (e: Exception) {
      Log.w(tag, "Firebase storage upload notice: ${e.message}. Using persistent local URI: $fallbackLocalUri")
      fallbackLocalUri
    }
  }

  /**
   * Saves a Bitmap to local persistent storage and uploads to Firebase Cloud Storage.
   */
  suspend fun uploadBitmap(context: Context, userId: String, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
    val localFile = saveBitmapToPersistentFile(context, bitmap)
    val fallbackLocalUri = if (localFile != null) Uri.fromFile(localFile).toString() else ""

    val storageInstance = storage
    if (storageInstance == null || localFile == null) {
      return@withContext fallbackLocalUri
    }

    try {
      val cleanUserId = userId.ifBlank { "guest_${System.currentTimeMillis()}" }
      val fileName = "photo_${UUID.randomUUID()}.jpg"
      val photoRef = storageInstance.reference
        .child("users")
        .child(cleanUserId)
        .child("photos")
        .child(fileName)

      val inputStream = localFile.inputStream()
      val metadata = StorageMetadata.Builder()
        .setContentType("image/jpeg")
        .build()

      photoRef.putStream(inputStream, metadata).await()
      val downloadUrl = photoRef.downloadUrl.await().toString()
      Log.d(tag, "Successfully uploaded bitmap to Firebase Storage: $downloadUrl")
      downloadUrl
    } catch (e: Exception) {
      Log.w(tag, "Firebase storage bitmap upload notice: ${e.message}. Using local URI: $fallbackLocalUri")
      fallbackLocalUri
    }
  }

  private fun saveUriToPersistentFile(context: Context, uri: Uri): File? {
    return try {
      val photosDir = File(context.filesDir, "profile_photos").apply { if (!exists()) mkdirs() }
      val file = File(photosDir, "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
          input.copyTo(output)
        }
      }
      file
    } catch (e: Exception) {
      Log.w(tag, "Error saving persistent image file: ${e.message}")
      null
    }
  }

  private fun saveBitmapToPersistentFile(context: Context, bitmap: Bitmap): File? {
    return try {
      val photosDir = File(context.filesDir, "profile_photos").apply { if (!exists()) mkdirs() }
      val file = File(photosDir, "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
      FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
        output.flush()
      }
      file
    } catch (e: Exception) {
      Log.w(tag, "Error saving persistent bitmap file: ${e.message}")
      null
    }
  }
}
