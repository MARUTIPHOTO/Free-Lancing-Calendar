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
  const val CHANNEL_ID = "maruti_booking_alerts_v8"
  const val CHANNEL_NAME = "Booking Alerts (संदेश टोन अलर्ट)"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      val importance = NotificationManager.IMPORTANCE_HIGH

      // Delete older channels if present to ensure high importance and sound are fresh
      try {
        notificationManager?.deleteNotificationChannel("maruti_booking_alerts_v7")
        notificationManager?.deleteNotificationChannel("maruti_booking_alerts_v6")
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
        "⚠️ नई पूछताछ: ${inquiry.clientName} (तारीख पहले से बुक है!)"
      } else {
        "🔔 नई बुकिंग पूछताछ: ${inquiry.clientName} (तारीख खाली है)"
      }

      val summaryText = if (isBookedConflict) {
        "⚠️ तारीख बुक है! 📅 ${inquiry.dateKey} | 📞 ${inquiry.clientPhone}"
      } else {
        "🟢 खाली तारीख | 📅 ${inquiry.dateKey} | 📞 ${inquiry.clientPhone}"
      }

      val bigDetails = buildString {
        if (isBookedConflict) {
          append("⚠️ अलर्ट: यह तारीख आपके कैलेंडर में पहले से बुक है!\n")
        } else {
          append("🟢 तारीख आपके कैलेंडर में खाली (Available) है!\n")
        }
        append("👤 क्लाइंट: ${inquiry.clientName}\n")
        append("📞 मोबाइल: ${inquiry.clientPhone}\n")
        append("📅 तारीख: ${inquiry.dateKey}\n")
        if (inquiry.location.isNotBlank()) append("📍 स्थान: ${inquiry.location}\n")
        if (inquiry.notes.isNotBlank()) append("📝 नोट: ${inquiry.notes}\n")
        append("👉 पूछताछ देखने और एक्शन लेने के लिए यहाँ टैप करें")
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

      // Play device notification ringtone
      try {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
          ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        if (soundUri != null) {
          val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
          ringtone?.play()
        }
      } catch (_: Exception) {}

      // 3. Trigger dual vibration pulse
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


