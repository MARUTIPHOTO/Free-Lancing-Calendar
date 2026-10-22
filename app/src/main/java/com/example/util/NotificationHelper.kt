package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.local.InquiryEntity

object NotificationHelper {
  const val CHANNEL_ID = "maruti_booking_alerts_v6"
  const val CHANNEL_NAME = "Booking Alerts (મેસેજ ટોન એલર્ટ)"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      val importance = NotificationManager.IMPORTANCE_HIGH

      // Delete older corrupted/wrong-attribute channels if present
      try {
        notificationManager?.deleteNotificationChannel("maruti_booking_msg_tone_v5")
        notificationManager?.deleteNotificationChannel("maruti_booking_inquiries")
      } catch (_: Exception) {}

      val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build()

      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = "Instant alerts for client inquiries from booking portal using device message tone"
        enableLights(true)
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 400, 200, 400)
        if (soundUri != null) {
          setSound(soundUri, audioAttributes)
        }
        setShowBadge(true)
        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
      }
      notificationManager?.createNotificationChannel(channel)
    }
  }

  fun showInquiryNotification(context: Context, inquiry: InquiryEntity, isBookedConflict: Boolean = false) {
    try {
      createNotificationChannel(context)

      // Open MainActivity and navigate to inquiries tab
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("NAV_DESTINATION", "NOTIFICATIONS")
      }

      val pendingIntent = PendingIntent.getActivity(
        context,
        (inquiry.id.toInt() and 0x7FFFFFFF) + 100,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val title = if (isBookedConflict) {
        "⚠️ નવી ઇન્ક્વાયરી: ${inquiry.clientName} (તારીખ પહેલેથી બુક છે!)"
      } else {
        "🔔 નવી બુકિંગ ઇન્ક્વાયરી: ${inquiry.clientName} (તારીખ ખાલી છે)"
      }

      val summaryText = if (isBookedConflict) {
        "⚠️ તારીખ બુક થયેલ છે! 📅 ${inquiry.dateKey} | 📞 ${inquiry.clientPhone}"
      } else {
        "🟢 ખાલી તારીખ | 📅 ${inquiry.dateKey} | 📞 ${inquiry.clientPhone}"
      }

      val bigDetails = buildString {
        if (isBookedConflict) {
          append("⚠️ એલર્ટ: આ તારીખ તમારા કેલેન્ડરમાં પહેલેથી બુક છે!\n")
        } else {
          append("🟢 તારીખ તમારા કેલેન્ડરમાં ખાલી (Available) છે!\n")
        }
        append("👤 ક્લાયન્ટ: ${inquiry.clientName}\n")
        append("📞 મોબાઈલ: ${inquiry.clientPhone}\n")
        append("📅 તારીખ: ${inquiry.dateKey}\n")
        if (inquiry.location.isNotBlank()) append("📍 સ્થળ: ${inquiry.location}\n")
        if (inquiry.notes.isNotBlank()) append("📝 નોંધ: ${inquiry.notes}\n")
        append("👉 ઇન્ક્વાયરી જોવા અને એક્શન લેવા અહીં ટેપ કરો")
      }

      val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(summaryText)
        .setStyle(NotificationCompat.BigTextStyle().bigText(bigDetails))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(pendingIntent)

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder.setSound(soundUri)
        builder.setVibrate(longArrayOf(0, 400, 200, 400))
        builder.setLights(0xFFD97706.toInt(), 1000, 500)
      }

      val notification = builder.build()

      val notifId = ((inquiry.id.toInt() and 0x7FFFFFFF) % 10000) + 1000
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
          NotificationManagerCompat.from(context).notify(notifId, notification)
        }
      } else {
        NotificationManagerCompat.from(context).notify(notifId, notification)
      }

      // Explicitly trigger device's message tone so it always rings audibly
      try {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, soundUri)
        ringtone?.play()
      } catch (_: Exception) {}

      // Trigger dual vibration pulse
      try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
          vibratorManager?.defaultVibrator
        } else {
          @Suppress("DEPRECATION")
          context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 350, 150, 350), -1))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(longArrayOf(0, 350, 150, 350), -1)
        }
      } catch (_: Exception) {}
    } catch (e: Exception) {
      android.util.Log.e("NotificationHelper", "Failed to display system notification: ${e.message}", e)
    }
  }
}


