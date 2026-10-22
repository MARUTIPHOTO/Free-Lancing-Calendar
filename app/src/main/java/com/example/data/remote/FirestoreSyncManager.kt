package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.InquiryEntity
import com.example.data.local.StudioDao
import com.example.util.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FirestoreSyncManager(
  private val context: Context,
  private val dao: StudioDao,
  private val onNewInquiryNotification: (InquiryEntity) -> Unit
) {
  private val TAG = "FirestoreSyncManager"
  private val databaseId = "ai-studio-android-freelanc-c2b7a674-802c-47dd-bb5b-f701d2a4253c"
  private val apiKey = "AIzaSyCf-_qYg-yQgKt7NPCCwAdxz-3JWOyFbgc"
  private val queryUrl = "https://firestore.googleapis.com/v1/projects/gen-lang-client-0524337453/databases/$databaseId/documents/studios/marutiphoto:runQuery?key=$apiKey"

  private var listenerRegistration: ListenerRegistration? = null
  private var firestore: FirebaseFirestore? = null
  private var pollingJob: Job? = null

  init {
    NotificationHelper.createNotificationChannel(context)
    try {
      val app = try {
        FirebaseApp.getInstance()
      } catch (e: Exception) {
        FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance()
      }
      firestore = try {
        FirebaseFirestore.getInstance(app, databaseId)
      } catch (e: Exception) {
        Log.w(TAG, "Fallback to default database: ${e.message}")
        FirebaseFirestore.getInstance(app)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Firebase SDK init warning: ${e.message}")
    }
  }

  fun startListening(scope: CoroutineScope) {
    // 1. Start continuous periodic REST sync (reliable across all Android devices)
    pollingJob?.cancel()
    pollingJob = scope.launch(Dispatchers.IO) {
      while (isActive) {
        try {
          fetchInquiriesFromCloudRest()
        } catch (e: Exception) {
          Log.w(TAG, "Polling cycle error: ${e.message}")
        }
        delay(7000L) // Poll every 7 seconds while app is active
      }
    }

    // 2. Also attach Firebase SDK Snapshot Listener if available
    try {
      val db = firestore
      if (db != null) {
        listenerRegistration?.remove()
        val collectionRef = db.collection("studios").document("marutiphoto").collection("inquiries")
        listenerRegistration = collectionRef.addSnapshotListener { _, e ->
          if (e == null) {
            scope.launch(Dispatchers.IO) {
              fetchInquiriesFromCloudRest()
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Snapshot listener fallback notice: ${e.message}")
    }
  }

  suspend fun manualRefresh() {
    withContext(Dispatchers.IO) {
      fetchInquiriesFromCloudRest()
    }
  }

  private suspend fun fetchInquiriesFromCloudRest() {
    var connection: HttpURLConnection? = null
    try {
      val url = URL(queryUrl)
      connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        setRequestProperty("Accept", "application/json")
        connectTimeout = 8000
        readTimeout = 8000
        doOutput = true
        doInput = true
      }

      val requestPayload = JSONObject().apply {
        val structuredQuery = JSONObject().apply {
          val fromArray = JSONArray().apply {
            put(JSONObject().apply { put("collectionId", "inquiries") })
          }
          put("from", fromArray)
        }
        put("structuredQuery", structuredQuery)
      }

      OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
        writer.write(requestPayload.toString())
        writer.flush()
      }

      val responseCode = connection.responseCode
      if (responseCode == HttpURLConnection.HTTP_OK) {
        val responseBody = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
          reader.readText()
        }

        parseAndSyncInquiries(responseBody)
      } else {
        Log.w(TAG, "Cloud REST returned status code: $responseCode")
      }
    } catch (e: Exception) {
      Log.w(TAG, "REST sync network error: ${e.message}")
    } finally {
      connection?.disconnect()
    }
  }

  private suspend fun parseAndSyncInquiries(jsonString: String) {
    try {
      val jsonArray = JSONArray(jsonString)
      val existingInquiries = dao.getAllInquiriesFlow().first()

      for (i in 0 until jsonArray.length()) {
        val item = jsonArray.optJSONObject(i) ?: continue
        val doc = item.optJSONObject("document") ?: continue
        val fields = doc.optJSONObject("fields") ?: continue

        val clientName = fields.optJSONObject("clientName")?.optString("stringValue") ?: ""
        val clientPhone = fields.optJSONObject("clientPhone")?.optString("stringValue") ?: ""
        val status = fields.optJSONObject("status")?.optString("stringValue") ?: "PENDING"
        val location = fields.optJSONObject("location")?.optString("stringValue") ?: ""
        val notes = fields.optJSONObject("notes")?.optString("stringValue") ?: ""
        val dates = fields.optJSONObject("dates")?.optString("stringValue") ?: ""
        val eventType = fields.optJSONObject("eventType")?.optString("stringValue") ?: "Online Inquiry"

        val createdAtRaw = fields.optJSONObject("createdAt")?.optString("integerValue")
        val createdAt = createdAtRaw?.toLongOrNull() ?: System.currentTimeMillis()

        if (clientPhone.isNotBlank() && dates.isNotBlank()) {
          // Check if already in local SQLite database
          val alreadySaved = existingInquiries.any {
            it.clientPhone == clientPhone && it.dateKey == dates
          }

          if (!alreadySaved) {
            val isConfirmedState = (status == "CONFIRMED")
            val newInquiry = InquiryEntity(
              dateKey = dates,
              clientName = clientName.ifBlank { "Client" },
              clientPhone = clientPhone,
              eventType = eventType,
              location = location,
              notes = if (notes.isNotBlank()) notes else "Online Web Inquiry",
              isConfirmed = isConfirmedState,
              isRead = false,
              createdAt = createdAt
            )

            val insertedId = dao.insertInquiry(newInquiry)
            val savedInquiry = newInquiry.copy(id = insertedId)

            Log.d(TAG, "Successfully synced inquiry into app: $savedInquiry")

            // 1. Show Real Android Status Bar System Notification with Sound & Vibration
            NotificationHelper.showInquiryNotification(context, savedInquiry)

            // 2. Trigger in-app Compose notification banner
            withContext(Dispatchers.Main) {
              onNewInquiryNotification(savedInquiry)
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse inquiries JSON: ${e.message}", e)
    }
  }

  fun stopListening() {
    pollingJob?.cancel()
    pollingJob = null
    listenerRegistration?.remove()
    listenerRegistration = null
  }

  suspend fun markInquiryConfirmed(clientPhone: String, dates: String) {
    withContext(Dispatchers.IO) {
      try {
        val db = firestore ?: return@withContext
        val query = db.collection("studios").document("marutiphoto").collection("inquiries")
          .whereEqualTo("clientPhone", clientPhone)
          .get()
        query.addOnSuccessListener { snapshots ->
          for (doc in snapshots.documents) {
            doc.reference.update("status", "CONFIRMED")
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Failed to mark inquiry confirmed in cloud: ${e.message}")
      }
    }
  }

  suspend fun markInquiryDeclined(clientPhone: String, dates: String) {
    withContext(Dispatchers.IO) {
      try {
        val db = firestore ?: return@withContext
        val query = db.collection("studios").document("marutiphoto").collection("inquiries")
          .whereEqualTo("clientPhone", clientPhone)
          .get()
        query.addOnSuccessListener { snapshots ->
          for (doc in snapshots.documents) {
            doc.reference.update("status", "DECLINED")
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Failed to mark inquiry declined in cloud: ${e.message}")
      }
    }
  }
}
