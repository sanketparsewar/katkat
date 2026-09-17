package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticHelper {

  fun triggerHaptic(context: Context, type: String) {
    try {
      val vibrator = getVibrator(context) ?: return

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        when (type) {
          "match" -> {
            // Heartbeat double pulse celebration pattern for mutual match
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              val timings = longArrayOf(0, 60, 50, 100, 60, 180)
              val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
              vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
              vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 50, 100, 60, 180), -1))
            }
          }
          "swipe_like", "like" -> {
            // Crisp positive tactile confirmation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
              vibrator.vibrate(VibrationEffect.createOneShot(35, 200))
            }
          }
          "swipe_pass", "pass" -> {
            // Subtle, light pass tap
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
              vibrator.vibrate(VibrationEffect.createOneShot(20, 120))
            }
          }
          "swipe_superlike", "superlike" -> {
            // Dynamic ascending burst for Super Like
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              val timings = longArrayOf(0, 40, 40, 80)
              val amplitudes = intArrayOf(0, 160, 0, 240)
              vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
              vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 40, 80), -1))
            }
          }
          "threshold" -> {
            // Very subtle tactile tick when entering swipe threshold
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
              vibrator.vibrate(VibrationEffect.createOneShot(15, 100))
            }
          }
          "rewind" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
              vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 30), -1))
            }
          }
          "boost", "upgrade" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
              vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            }
          }
          "refresh", "save" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
              vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            }
          }
          else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
              vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            }
          }
        }
      } else {
        @Suppress("DEPRECATION")
        when (type) {
          "match" -> vibrator.vibrate(longArrayOf(0, 60, 50, 100, 60, 180), -1)
          "swipe_superlike", "superlike" -> vibrator.vibrate(longArrayOf(0, 40, 40, 80), -1)
          else -> vibrator.vibrate(35)
        }
      }
    } catch (_: Exception) {
      // Gracefully handle devices without vibration hardware
    }
  }

  private fun getVibrator(context: Context): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }
}
