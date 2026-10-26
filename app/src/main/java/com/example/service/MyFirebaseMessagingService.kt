package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.local.InquiryEntity
import com.example.data.local.StudioDatabase
import com.example.util.FcmTopicManager
import com.example.util.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

  companion object {
    private const val TAG = "MyFCMService"
    private const val DATABASE_ID = "ai-studio-android-freelanc-c2b7a674-802c-47dd-bb5b-f701d2a4253c"
    private var inquiriesListenerRegistration: ListenerRegistration? = null
    private val notifiedIds = java.util.Collections.synchronizedSet(HashSet<String>())

    /**
     * Listen for new document creation in the specific 'inquiries' collection
     * and trigger a local push notification.
     */
    fun startListeningForInquiries(context: Context, studioSlug: String) {
      if (studioSlug.isBlank()) return
      val cleanSlug = studioSlug.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.lowercase()

      try {
        stopListeningForInquiries()
        val app = try { FirebaseApp.getInstance() } catch (_: Exception) { FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance() }
        val db = try { FirebaseFirestore.getInstance(app, DATABASE_ID) } catch (_: Exception) { FirebaseFirestore.getInstance(app) }

        val collectionRef = db.collection("studios").document(cleanSlug).collection("inquiries")
        inquiriesListenerRegistration = collectionRef.addSnapshotListener { snapshots, e ->
          if (e != null) {
            Log.w(TAG, "Listen failed for inquiries: ${e.message}")
            return@addSnapshotListener
          }

          if (snapshots == null) return@addSnapshotListener

          for (dc in snapshots.documentChanges) {
            if (dc.type == DocumentChange.Type.ADDED) {
              val doc = dc.document
              val docId = doc.id
              if (notifiedIds.contains(docId)) continue
              notifiedIds.add(docId)

              val clientName = doc.getString("clientName")?.trim() ?: "New Client"
              val clientPhone = doc.getString("clientPhone")?.trim() ?: ""
              val dates = doc.getString("dates")?.trim() ?: "General Inquiry"
              val location = doc.getString("location")?.trim() ?: ""
              val notes = doc.getString("notes")?.trim() ?: ""
              val eventType = doc.getString("eventType")?.trim() ?: "Online Inquiry"
              val status = doc.getString("status")?.trim() ?: "PENDING"

              if (clientPhone.isBlank() || status == "DECLINED") continue

              CoroutineScope(Dispatchers.IO).launch {
                triggerInquiryNotification(
                  context = context,
                  clientName = clientName,
                  clientPhone = clientPhone,
                  dates = dates,
                  location = location,
                  notes = notes,
                  eventType = eventType
                )
              }
            }
          }
        }
        Log.d(TAG, "Started Firestore inquiries listener for /studios/$cleanSlug/inquiries")
      } catch (e: Exception) {
        Log.e(TAG, "Error starting inquiries listener: ${e.message}", e)
      }
    }

    fun stopListeningForInquiries() {
      inquiriesListenerRegistration?.remove()
      inquiriesListenerRegistration = null
    }

    suspend fun triggerInquiryNotification(
      context: Context,
      clientName: String,
      clientPhone: String,
      dates: String,
      location: String,
      notes: String,
      eventType: String
    ) {
      try {
        val database = StudioDatabase.getDatabase(context)
        val dao = database.studioDao()

        // Check if any date is already booked in calendar
        val parsedDates = dates.split(",")
          .map { it.trim().substringBefore(" ") }
          .filter { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }

        var isAnyDateBooked = false
        for (d in parsedDates) {
          val dateEntity = dao.getDate(d)
          if (dateEntity?.status == "BOOKED") {
            isAnyDateBooked = true
            break
          }
        }

        val existingInquiries = dao.getAllInquiriesFlow().first()
        val alreadyExists = existingInquiries.any {
          it.clientPhone.trim() == clientPhone && it.dateKey.trim() == dates
        }

        val id = if (!alreadyExists) {
          val entity = InquiryEntity(
            dateKey = dates,
            clientName = clientName,
            clientPhone = clientPhone,
            eventType = eventType,
            location = location,
            notes = notes,
            isConfirmed = false,
            isRead = false,
            createdAt = System.currentTimeMillis()
          )
          dao.insertInquiry(entity)
        } else {
          existingInquiries.first { it.clientPhone.trim() == clientPhone && it.dateKey.trim() == dates }.id
        }

        val inquiryToNotify = InquiryEntity(
          id = id,
          dateKey = dates,
          clientName = clientName,
          clientPhone = clientPhone,
          eventType = eventType,
          location = location,
          notes = notes,
          isConfirmed = false,
          isRead = false,
          createdAt = System.currentTimeMillis()
        )

        NotificationHelper.showInquiryNotification(
          context = context,
          inquiry = inquiryToNotify,
          isBookedConflict = isAnyDateBooked
        )
      } catch (e: Exception) {
        Log.e(TAG, "Error triggering inquiry notification: ${e.message}", e)
      }
    }
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New FCM Device Registration Token: $token")
    val prefs = applicationContext.getSharedPreferences("fcm_token_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("fcm_device_token", token).apply()

    val authPrefs = applicationContext.getSharedPreferences("app_auth_session", Context.MODE_PRIVATE)
    val savedEmail = authPrefs.getString("logged_in_email", null) ?: ""
    if (savedEmail.isNotBlank()) {
      val slug = savedEmail.substringBefore("@").filter { it.isLetterOrDigit() }.lowercase()
      if (slug.isNotBlank()) {
        FcmTopicManager.uploadFcmTokenToFirestore(applicationContext, slug, token)
      }
    }
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

    val data = remoteMessage.data
    val notification = remoteMessage.notification

    val clientName = data["clientName"]?.trim()
      ?: notification?.title?.substringAfter(":")?.trim()
      ?: "New Client"
    val clientPhone = data["clientPhone"]?.trim() ?: ""
    val dates = data["dates"]?.trim()
      ?: data["dateKey"]?.trim()
      ?: "General Inquiry"
    val location = data["location"]?.trim() ?: ""
    val notes = data["notes"]?.trim() ?: notification?.body?.trim() ?: ""
    val eventType = data["eventType"]?.trim() ?: "Online Inquiry"

    // If phone is in declined list, ignore
    val syncPrefs = applicationContext.getSharedPreferences("firestore_sync_prefs", Context.MODE_PRIVATE)
    val declinedPhones = syncPrefs.getStringSet("declined_phones", emptySet()) ?: emptySet()
    if (clientPhone.isNotBlank() && declinedPhones.contains(clientPhone)) {
      Log.d(TAG, "Ignoring FCM inquiry for declined phone: $clientPhone")
      return
    }

    CoroutineScope(Dispatchers.IO).launch {
      triggerInquiryNotification(
        context = applicationContext,
        clientName = clientName,
        clientPhone = clientPhone,
        dates = dates,
        location = location,
        notes = notes,
        eventType = eventType
      )
    }
  }
}
