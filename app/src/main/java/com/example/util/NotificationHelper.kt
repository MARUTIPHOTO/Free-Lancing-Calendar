package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.local.InquiryEntity

object NotificationHelper {
  const val CHANNEL_ID = "maruti_booking_inquiries_v2"
  const val CHANNEL_NAME = "Booking Inquiries (નવી બુકિંગ ઇન્ક્વાયરી)"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = "Real-time alerts for new client inquiries from web booking portal"
        enableLights(true)
        enableVibration(true)
        setShowBadge(true)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }

  fun showInquiryNotification(context: Context, inquiry: InquiryEntity) {
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

      val title = "🔔 નવી બુકિંગ ઇન્ક્વાયરી: ${inquiry.clientName}"
      val summaryText = "📅 તારીખ: ${inquiry.dateKey} | 📞 ${inquiry.clientPhone}"
      val bigDetails = buildString {
        append("👤 ક્લાયન્ટ: ${inquiry.clientName}\n")
        append("📞 મોબાઈલ: ${inquiry.clientPhone}\n")
        append("📅 તારીખ: ${inquiry.dateKey}\n")
        if (inquiry.location.isNotBlank()) append("📍 સ્થળ: ${inquiry.location}\n")
        if (inquiry.notes.isNotBlank()) append("📝 નોંધ: ${inquiry.notes}\n")
        append("✨ ઇન્ક્વાયરી જોવા માટે ટેપ કરો")
      }

      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(summaryText)
        .setStyle(NotificationCompat.BigTextStyle().bigText(bigDetails))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      val notificationManager = NotificationManagerCompat.from(context)
      val notifId = ((inquiry.id.toInt() and 0x7FFFFFFF) % 10000) + 1000
      notificationManager.notify(notifId, notification)
    } catch (e: Exception) {
      android.util.Log.e("NotificationHelper", "Failed to display system notification: ${e.message}", e)
    }
  }
}
