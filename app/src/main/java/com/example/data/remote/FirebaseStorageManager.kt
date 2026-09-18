package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.KatkatApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Handles uploading photos directly to Firebase Cloud Storage.
 *
 * All photos uploaded here yield public HTTPS download URLs that are shareable across devices.
 * Under no circumstances does this class return local device paths (e.g. file:// or content://)
 * as valid cloud URLs.
 */
class FirebaseStorageManager {
  private val tag = "FirebaseStorageManager"
  private val storageBucketUrl = "gs://mobile-app-be8a4.firebasestorage.app"

  private fun getStorage(context: Context? = null): FirebaseStorage? {
    return try {
      val ctx = context ?: try { KatkatApplication.appContext } catch (_: Exception) { null }
      val app = if (ctx != null) {
        if (FirebaseApp.getApps(ctx).isEmpty()) {
          FirebaseApp.initializeApp(ctx)
        } else {
          FirebaseApp.getInstance()
        }
      } else {
        try { FirebaseApp.getInstance() } catch (_: Exception) { null }
      }

      if (app != null) {
        try {
          FirebaseStorage.getInstance(app, storageBucketUrl)
        } catch (_: Exception) {
          FirebaseStorage.getInstance(app)
        }
      } else {
        try {
          FirebaseStorage.getInstance(storageBucketUrl)
        } catch (_: Exception) {
          FirebaseStorage.getInstance()
        }
      }
    } catch (e: Exception) {
      Log.w(tag, "Firebase Storage instance error: ${e.message}")
      null
    }
  }

  val isAvailable: Boolean
    get() = getStorage() != null

  /**
   * Ensures an active Firebase Auth session exists so Storage security rules
   * allowing authenticated read/write succeed.
   */
  private suspend fun ensureAuth() {
    try {
      val auth = FirebaseAuth.getInstance()
      if (auth.currentUser == null) {
        auth.signInAnonymously().await()
        Log.d(tag, "Authenticated session established for Storage: ${auth.currentUser?.uid}")
      }
    } catch (e: Exception) {
      Log.w(tag, "Firebase Auth session notice: ${e.message}")
    }
  }

  /**
   * Uploads an image from a content/file URI to Firebase Cloud Storage.
   * Returns the cloud HTTPS download URL, or an empty string if upload fails.
   */
  suspend fun uploadProfileImage(context: Context, userId: String, imageUri: Uri): String = withContext(Dispatchers.IO) {
    val uriString = imageUri.toString()
    // Already a remote cloud URL (e.g., https://...)
    if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
      return@withContext uriString
    }

    ensureAuth()
    val storageInstance = getStorage(context)
    if (storageInstance == null) {
      Log.e(tag, "Cloud Storage unavailable. Cannot upload image.")
      return@withContext ""
    }

    val bytes = compressImageUriToBytes(context, imageUri)
    if (bytes == null || bytes.isEmpty()) {
      Log.e(tag, "Failed to read image bytes from URI: $imageUri")
      return@withContext ""
    }

    try {
      val cleanUserId = userId.filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "user_${System.currentTimeMillis()}" }
      val fileName = "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
      val photoRef = storageInstance.reference
        .child("users")
        .child(cleanUserId)
        .child("photos")
        .child(fileName)

      val metadata = StorageMetadata.Builder()
        .setContentType("image/jpeg")
        .setCustomMetadata("uploadedBy", cleanUserId)
        .build()

      photoRef.putBytes(bytes, metadata).await()
      val downloadUrl = photoRef.downloadUrl.await().toString()
      Log.d(tag, "✓ Uploaded image to Firebase Cloud Storage: $downloadUrl")
      downloadUrl
    } catch (e: Exception) {
      Log.e(tag, "Failed to upload image to Firebase Cloud Storage: ${e.message}", e)
      ""
    }
  }

  /**
   * Uploads a Bitmap directly to Firebase Cloud Storage.
   * Returns the cloud HTTPS download URL, or an empty string if upload fails.
   */
  suspend fun uploadBitmap(context: Context, userId: String, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
    ensureAuth()
    val storageInstance = getStorage(context)
    if (storageInstance == null) {
      Log.e(tag, "Cloud Storage unavailable. Cannot upload bitmap.")
      return@withContext ""
    }

    val bytes = compressBitmapToBytes(bitmap)
    if (bytes.isEmpty()) {
      Log.e(tag, "Failed to compress bitmap")
      return@withContext ""
    }

    try {
      val cleanUserId = userId.filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "user_${System.currentTimeMillis()}" }
      val fileName = "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
      val photoRef = storageInstance.reference
        .child("users")
        .child(cleanUserId)
        .child("photos")
        .child(fileName)

      val metadata = StorageMetadata.Builder()
        .setContentType("image/jpeg")
        .setCustomMetadata("uploadedBy", cleanUserId)
        .build()

      photoRef.putBytes(bytes, metadata).await()
      val downloadUrl = photoRef.downloadUrl.await().toString()
      Log.d(tag, "✓ Uploaded bitmap to Firebase Cloud Storage: $downloadUrl")
      downloadUrl
    } catch (e: Exception) {
      Log.e(tag, "Failed to upload bitmap to Firebase Cloud Storage: ${e.message}", e)
      ""
    }
  }

  /**
   * Compresses image at given URI to a lightweight, high-quality JPEG byte array (max 1440px dimension).
   */
  private fun compressImageUriToBytes(context: Context, uri: Uri): ByteArray? {
    return try {
      val inputStream = context.contentResolver.openInputStream(uri) ?: return null
      val originalBitmap = BitmapFactory.decodeStream(inputStream)
      inputStream.close()
      if (originalBitmap == null) return null

      val maxDim = 1440
      val width = originalBitmap.width
      val height = originalBitmap.height
      val scaledBitmap = if (width > maxDim || height > maxDim) {
        val ratio = width.toFloat() / height.toFloat()
        val (newW, newH) = if (ratio > 1f) {
          maxDim to (maxDim / ratio).toInt()
        } else {
          (maxDim * ratio).toInt() to maxDim
        }
        Bitmap.createScaledBitmap(originalBitmap, newW, newH, true)
      } else {
        originalBitmap
      }

      val outputStream = ByteArrayOutputStream()
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
      outputStream.toByteArray()
    } catch (e: Exception) {
      Log.w(tag, "Error compressing image from URI: ${e.message}")
      null
    }
  }

  /**
   * Compresses a Bitmap to a JPEG byte array (max 1440px dimension).
   */
  private fun compressBitmapToBytes(bitmap: Bitmap): ByteArray {
    return try {
      val maxDim = 1440
      val width = bitmap.width
      val height = bitmap.height
      val scaledBitmap = if (width > maxDim || height > maxDim) {
        val ratio = width.toFloat() / height.toFloat()
        val (newW, newH) = if (ratio > 1f) {
          maxDim to (maxDim / ratio).toInt()
        } else {
          (maxDim * ratio).toInt() to maxDim
        }
        Bitmap.createScaledBitmap(bitmap, newW, newH, true)
      } else {
        bitmap
      }

      val outputStream = ByteArrayOutputStream()
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
      outputStream.toByteArray()
    } catch (e: Exception) {
      Log.w(tag, "Error compressing bitmap: ${e.message}")
      ByteArray(0)
    }
  }
}
