package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.local.InquiryEntity

object NotificationHelper {
  const val CHANNEL_ID = "maruti_booking_alerts_v3"
  const val CHANNEL_NAME = "Booking Alerts (નવી બુકિંગ ઇન્ક્વાયરી)"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      val importance = NotificationManager.IMPORTANCE_HIGH
      val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build()

      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = "Instant alerts for client inquiries from booking portal"
        enableLights(true)
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 500, 250, 500)
        setSound(soundUri, audioAttributes)
        setShowBadge(true)
        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
      }
      notificationManager?.createNotificationChannel(channel)
    }
  }

  fun showInquiryNotification(context: Context, inquiry: InquiryEntity) {
    try {
      createNotificationChannel(context)

      // Direct ringtone and vibration playback to ensure user hears and feels alert on all phone brands
      try {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
        ringtone?.play()
      } catch (_: Exception) {}

      try {
        val vibrator = androidx.core.content.ContextCompat.getSystemService(context, Vibrator::class.java)
        if (vibrator?.hasVibrator() == true) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
          } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
          }
        }
      } catch (_: Exception) {}

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

      val title = "🔔 નવી બુકિંગ ઇન્ક્વાયરી: ${inquiry.clientName}"
      val summaryText = "📅 ${inquiry.dateKey} | 📞 ${inquiry.clientPhone}"
      val bigDetails = buildString {
        append("👤 ક્લાયન્ટ: ${inquiry.clientName}\n")
        append("📞 મોબાઈલ: ${inquiry.clientPhone}\n")
        append("📅 તારીખ: ${inquiry.dateKey}\n")
        if (inquiry.location.isNotBlank()) append("📍 સ્થળ: ${inquiry.location}\n")
        if (inquiry.notes.isNotBlank()) append("📝 નોંધ: ${inquiry.notes}\n")
        append("👉 ઇન્ક્વાયરી જોવા માટે અહીં ટેપ કરો")
      }

      val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(summaryText)
        .setStyle(NotificationCompat.BigTextStyle().bigText(bigDetails))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setSound(soundUri)
        .setVibrate(longArrayOf(0, 500, 250, 500))
        .setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(pendingIntent)
        .build()

      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      val notifId = ((inquiry.id.toInt() and 0x7FFFFFFF) % 10000) + 1000
      notificationManager?.notify(notifId, notification)

      val compatManager = NotificationManagerCompat.from(context)
      compatManager.notify(notifId + 1, notification)
    } catch (e: Exception) {
      android.util.Log.e("NotificationHelper", "Failed to display system notification: ${e.message}", e)
    }
  }
}

