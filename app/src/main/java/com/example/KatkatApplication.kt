package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

class KatkatApplication : Application(), ImageLoaderFactory {

  companion object {
    lateinit var instance: KatkatApplication
      private set
    val appContext: Context
      get() = instance.applicationContext

    var isAppInForeground: Boolean = false
      private set

    @Volatile
    var activeChatPartnerId: String? = null
  }

  override fun onCreate() {
    super.onCreate()
    instance = this

    registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
      private var runningActivities = 0

      override fun onActivityStarted(activity: android.app.Activity) {
        runningActivities++
        isAppInForeground = runningActivities > 0
      }

      override fun onActivityStopped(activity: android.app.Activity) {
        runningActivities = (runningActivities - 1).coerceAtLeast(0)
        isAppInForeground = runningActivities > 0
        if (!isAppInForeground) {
          activeChatPartnerId = null
        }
      }

      override fun onActivityResumed(activity: android.app.Activity) {
        isAppInForeground = true
      }

      override fun onActivityPaused(activity: android.app.Activity) {}
      override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {}
      override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
      override fun onActivityDestroyed(activity: android.app.Activity) {}
    })

    try {
      if (FirebaseApp.getApps(this).isEmpty()) {
        FirebaseApp.initializeApp(this)
      }
    } catch (e: Exception) {
      Log.w("KatkatApplication", "FirebaseApp initialization notice: ${e.message}")
    }
    try {
      com.example.util.PushNotificationHelper.createNotificationChannel(this)
    } catch (e: Exception) {
      Log.w("KatkatApplication", "Notification channel creation notice: ${e.message}")
    }
    try {
      com.example.service.KatkatNotificationSyncService.startService(this)
    } catch (e: Exception) {
      Log.w("KatkatApplication", "Sync service launch notice: ${e.message}")
    }
  }

  override fun newImageLoader(): ImageLoader {
    val okHttpClient = OkHttpClient.Builder()
      .followRedirects(true)
      .followSslRedirects(true)
      .connectTimeout(20, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .addInterceptor { chain ->
        val original = chain.request()
        val requestWithHeaders = original.newBuilder()
          .header(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
          )
          .header("Accept", "image/jpeg,image/png,image/webp,image/*;q=0.8")
          .build()
        chain.proceed(requestWithHeaders)
      }
      .build()

    return ImageLoader.Builder(this)
      .okHttpClient(okHttpClient)
      .allowHardware(false)
      .memoryCache {
        MemoryCache.Builder(this)
          .maxSizePercent(0.25)
          .build()
      }
      .diskCache {
        DiskCache.Builder()
          .directory(cacheDir.resolve("image_cache"))
          .maxSizePercent(0.05)
          .build()
      }
      .respectCacheHeaders(false)
      .crossfade(true)
      .build()
  }
}
