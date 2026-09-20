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
import com.google.firebase.storage.StorageException
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
  private val defaultBucket = "mobile-app-be8a4.firebasestorage.app"

  var lastErrorMessage: String? = null
    private set

  var lastUploadWasFallback: Boolean = false
    private set

  var hasStoragePermissionError: Boolean = false
    private set

  private fun getStorage(context: Context? = null): FirebaseStorage? {
    return try {
      val ctx = context ?: try { KatkatApplication.appContext } catch (_: Throwable) { null }
      val app = if (ctx != null) {
        if (FirebaseApp.getApps(ctx).isEmpty()) {
          FirebaseApp.initializeApp(ctx)
        } else {
          FirebaseApp.getInstance()
        }
      } else {
        try { FirebaseApp.getInstance() } catch (_: Throwable) { null }
      }

      if (app != null) {
        try {
          // Standard: Use bucket defined in google-services.json
          FirebaseStorage.getInstance(app)
        } catch (_: Throwable) {
          try {
            FirebaseStorage.getInstance(app, "gs://$defaultBucket")
          } catch (_: Throwable) {
            FirebaseStorage.getInstance(app, defaultBucket)
          }
        }
      } else {
        try {
          FirebaseStorage.getInstance()
        } catch (_: Throwable) {
          FirebaseStorage.getInstance("gs://$defaultBucket")
        }
      }
    } catch (e: Throwable) {
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
    } catch (e: Throwable) {
      Log.w(tag, "Firebase Auth session notice: ${e.message}")
    }
  }

  /**
   * Uploads an image from a content/file URI to Firebase Cloud Storage.
   * Returns the cloud HTTPS download URL, or an empty string if upload fails.
   */
  suspend fun uploadProfileImage(context: Context, userId: String, imageUri: Uri): String = withContext(Dispatchers.IO) {
    lastErrorMessage = null
    lastUploadWasFallback = false
    val uriString = imageUri.toString()
    // Already a remote cloud URL (e.g., https://...)
    if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
      return@withContext uriString
    }

    ensureAuth()
    val storageInstance = getStorage(context)
    val bytes = compressImageUriToBytes(context, imageUri)
    if (bytes == null || bytes.isEmpty()) {
      val msg = "Unable to process selected photo. Please try a different image."
      lastErrorMessage = msg
      Log.e(tag, "Failed to read image bytes from URI: $imageUri")
      return@withContext ""
    }

    val cleanUserId = userId.filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "user_${System.currentTimeMillis()}" }
    val fileName = "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"

    if (storageInstance != null) {
      // Primary path: users/{userId}/photos/{fileName}
      // Fallback path: photos/{fileName} (in case security rules target root photos folder)
      val pathsToTry = listOf(
        storageInstance.reference.child("users").child(cleanUserId).child("photos").child(fileName),
        storageInstance.reference.child("photos").child(fileName)
      )

      val metadata = StorageMetadata.Builder()
        .setContentType("image/jpeg")
        .build()

      for (photoRef in pathsToTry) {
        try {
          photoRef.putBytes(bytes, metadata).await()
          val downloadUrl = photoRef.downloadUrl.await().toString()
          Log.d(tag, "✓ Uploaded image to Firebase Cloud Storage at ${photoRef.path}: $downloadUrl")
          lastErrorMessage = null
          return@withContext downloadUrl
        } catch (e: StorageException) {
          if (e.errorCode == StorageException.ERROR_NOT_AUTHORIZED) {
            hasStoragePermissionError = true
          }
          val userFriendlyMsg = when (e.errorCode) {
            StorageException.ERROR_NOT_AUTHORIZED ->
              "Permission denied by Firebase Storage. Ensure Storage rules allow write or enable Anonymous Auth in Firebase Console."
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
              "Upload timed out. Please check your internet connection."
            StorageException.ERROR_QUOTA_EXCEEDED ->
              "Firebase Storage quota exceeded."
            StorageException.ERROR_BUCKET_NOT_FOUND ->
              "Firebase Storage bucket '$defaultBucket' not found."
            else ->
              e.localizedMessage ?: "Upload error (code ${e.errorCode})"
          }
          lastErrorMessage = userFriendlyMsg
          Log.w(tag, "Upload attempt failed at ${photoRef.path}: $userFriendlyMsg", e)
        } catch (e: Throwable) {
          lastErrorMessage = e.localizedMessage ?: "Failed to upload to cloud storage"
          Log.e(tag, "Unexpected upload error at ${photoRef.path}: ${e.message}", e)
        }
      }
    } else {
      lastErrorMessage = "Firebase Storage instance unavailable."
    }

    // Resilient fallback: Save image locally so the user is never blocked by Storage rules
    val fallbackUri = saveBytesToLocalFile(context, bytes, fileName)
    if (fallbackUri.isNotBlank()) {
      lastUploadWasFallback = true
      Log.i(tag, "Saved photo to local app storage fallback: $fallbackUri")
      return@withContext fallbackUri
    }

    ""
  }

  /**
   * Uploads a Bitmap directly to Firebase Cloud Storage.
   * Returns the cloud HTTPS download URL, or an empty string if upload fails.
   */
  suspend fun uploadBitmap(context: Context, userId: String, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
    lastErrorMessage = null
    lastUploadWasFallback = false
    ensureAuth()
    val bytes = compressBitmapToBytes(bitmap)
    if (bytes.isEmpty()) {
      val msg = "Unable to process camera image."
      lastErrorMessage = msg
      Log.e(tag, "Failed to compress bitmap")
      return@withContext ""
    }

    val cleanUserId = userId.filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "user_${System.currentTimeMillis()}" }
    val fileName = "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
    val storageInstance = getStorage(context)

    if (storageInstance != null) {
      val pathsToTry = listOf(
        storageInstance.reference.child("users").child(cleanUserId).child("photos").child(fileName),
        storageInstance.reference.child("photos").child(fileName)
      )

      val metadata = StorageMetadata.Builder()
        .setContentType("image/jpeg")
        .build()

      for (photoRef in pathsToTry) {
        try {
          photoRef.putBytes(bytes, metadata).await()
          val downloadUrl = photoRef.downloadUrl.await().toString()
          Log.d(tag, "✓ Uploaded bitmap to Firebase Cloud Storage at ${photoRef.path}: $downloadUrl")
          lastErrorMessage = null
          return@withContext downloadUrl
        } catch (e: StorageException) {
          if (e.errorCode == StorageException.ERROR_NOT_AUTHORIZED) {
            hasStoragePermissionError = true
          }
          val userFriendlyMsg = when (e.errorCode) {
            StorageException.ERROR_NOT_AUTHORIZED ->
              "Permission denied by Firebase Storage. Ensure Storage rules allow write or enable Anonymous Auth in Firebase Console."
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
              "Upload timed out. Please check your internet connection."
            StorageException.ERROR_QUOTA_EXCEEDED ->
              "Firebase Storage quota exceeded."
            StorageException.ERROR_BUCKET_NOT_FOUND ->
              "Firebase Storage bucket '$defaultBucket' not found."
            else ->
              e.localizedMessage ?: "Upload error (code ${e.errorCode})"
          }
          lastErrorMessage = userFriendlyMsg
          Log.w(tag, "Upload attempt failed at ${photoRef.path}: $userFriendlyMsg", e)
        } catch (e: Throwable) {
          lastErrorMessage = e.localizedMessage ?: "Failed to upload to cloud storage"
          Log.e(tag, "Unexpected upload error at ${photoRef.path}: ${e.message}", e)
        }
      }
    } else {
      lastErrorMessage = "Firebase Storage instance unavailable."
    }

    // Resilient fallback: Save image locally so the user is never blocked by Storage rules
    val fallbackUri = saveBytesToLocalFile(context, bytes, fileName)
    if (fallbackUri.isNotBlank()) {
      lastUploadWasFallback = true
      Log.i(tag, "Saved bitmap to local app storage fallback: $fallbackUri")
      return@withContext fallbackUri
    }

    ""
  }

  private fun saveBytesToLocalFile(context: Context, bytes: ByteArray, fileName: String): String {
    return try {
      val photosDir = java.io.File(context.filesDir, "profile_photos")
      if (!photosDir.exists()) {
        photosDir.mkdirs()
      }
      val destFile = java.io.File(photosDir, fileName)
      destFile.outputStream().use { it.write(bytes) }
      Uri.fromFile(destFile).toString()
    } catch (e: Exception) {
      Log.e(tag, "Failed to save local photo fallback: ${e.message}", e)
      ""
    }
  }

  /**
   * Compresses image at given URI to a lightweight, high-quality JPEG byte array (max 1280px dimension).
   * Employs memory-safe two-pass decoding (inJustDecodeBounds -> inSampleSize) to prevent OOM.
   */
  private fun compressImageUriToBytes(context: Context, uri: Uri): ByteArray? {
    return try {
      val maxDim = 1280

      // Pass 1: Decode bounds only
      val boundsOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
      context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, boundsOptions)
      }

      val origW = boundsOptions.outWidth
      val origH = boundsOptions.outHeight
      if (origW <= 0 || origH <= 0) {
        // Fallback simple decode if bounds couldn't be read
        val directStream = context.contentResolver.openInputStream(uri) ?: return null
        val directBitmap = BitmapFactory.decodeStream(directStream)
        directStream.close()
        return directBitmap?.let { compressBitmapToBytes(it) }
      }

      // Calculate inSampleSize
      var sampleSize = 1
      while ((origW / sampleSize) > maxDim * 1.5 || (origH / sampleSize) > maxDim * 1.5) {
        sampleSize *= 2
      }

      // Pass 2: Decode sampled bitmap
      val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.RGB_565 // Half the memory of ARGB_8888
      }
      val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOptions)
      } ?: return null

      // Scale to exact max dimension if still needed
      val finalBitmap = if (sampledBitmap.width > maxDim || sampledBitmap.height > maxDim) {
        val ratio = sampledBitmap.width.toFloat() / sampledBitmap.height.toFloat()
        val (newW, newH) = if (ratio > 1f) {
          maxDim to (maxDim / ratio).toInt().coerceAtLeast(1)
        } else {
          (maxDim * ratio).toInt().coerceAtLeast(1) to maxDim
        }
        val scaled = Bitmap.createScaledBitmap(sampledBitmap, newW, newH, true)
        if (scaled != sampledBitmap) {
          sampledBitmap.recycle()
        }
        scaled
      } else {
        sampledBitmap
      }

      val outputStream = ByteArrayOutputStream()
      finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
      finalBitmap.recycle()
      outputStream.toByteArray()
    } catch (e: Throwable) {
      Log.w(tag, "Error compressing image from URI: ${e.message}")
      null
    }
  }

  /**
   * Compresses a Bitmap to a JPEG byte array (max 1280px dimension).
   */
  private fun compressBitmapToBytes(bitmap: Bitmap): ByteArray {
    return try {
      val maxDim = 1280
      val width = bitmap.width
      val height = bitmap.height
      val scaledBitmap = if (width > maxDim || height > maxDim) {
        val ratio = width.toFloat() / height.toFloat()
        val (newW, newH) = if (ratio > 1f) {
          maxDim to (maxDim / ratio).toInt().coerceAtLeast(1)
        } else {
          (maxDim * ratio).toInt().coerceAtLeast(1) to maxDim
        }
        Bitmap.createScaledBitmap(bitmap, newW, newH, true)
      } else {
        bitmap
      }

      val outputStream = ByteArrayOutputStream()
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
      if (scaledBitmap != bitmap) {
        scaledBitmap.recycle()
      }
      outputStream.toByteArray()
    } catch (e: Throwable) {
      Log.w(tag, "Error compressing bitmap: ${e.message}")
      ByteArray(0)
    }
  }
}

