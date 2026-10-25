package com.example.data.remote

import android.app.NotificationManager
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
  var currentStudioSlug: String = "marutiphoto"
  private val queryUrl: String
    get() = "https://firestore.googleapis.com/v1/projects/gen-lang-client-0524337453/databases/$databaseId/documents/studios/$currentStudioSlug:runQuery?key=$apiKey"

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
          syncBookedDatesToCloudRest()
        } catch (_: kotlinx.coroutines.CancellationException) {
          break
        } catch (e: Exception) {
          Log.w(TAG, "Polling cycle error: ${e.message}")
        }
        try {
          delay(3500L) // Poll every 3.5 seconds while app is active
        } catch (_: kotlinx.coroutines.CancellationException) {
          break
        }
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
              syncBookedDatesToCloudRest()
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
      syncBookedDatesToCloudRest()
    }
  }

  suspend fun syncBookedDatesToCloudRest() {
    var connection: HttpURLConnection? = null
    try {
      val bookedList = dao.getAllDates().filter { it.status == "BOOKED" }.map { it.dateKey }
      val scheduleUrl = "https://firestore.googleapis.com/v1/projects/gen-lang-client-0524337453/databases/ai-studio-android-freelanc-c2b7a674-802c-47dd-bb5b-f701d2a4253c/documents/studios/$currentStudioSlug/schedule/booked_dates?updateMask.fieldPaths=dates&updateMask.fieldPaths=updatedAt&key=$apiKey"
      val url = URL(scheduleUrl)
      connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("X-HTTP-Method-Override", "PATCH")
        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connectTimeout = 6000
        readTimeout = 6000
        doOutput = true
      }
      val arrayValues = JSONArray().apply {
        bookedList.forEach { d ->
          put(JSONObject().apply { put("stringValue", d) })
        }
      }
      val arrayValueObj = JSONObject().apply {
        if (bookedList.isNotEmpty()) {
          put("values", arrayValues)
        }
      }
      val payload = JSONObject().apply {
        put("fields", JSONObject().apply {
          put("dates", JSONObject().apply {
            put("arrayValue", arrayValueObj)
          })
          put("updatedAt", JSONObject().apply {
            put("integerValue", System.currentTimeMillis().toString())
          })
        })
      }
      OutputStreamWriter(connection.outputStream, "UTF-8").use { it.write(payload.toString()) }
      val code = connection.responseCode
      Log.d(TAG, "syncBookedDatesToCloudRest code: $code, count: ${bookedList.size}")
    } catch (e: Exception) {
      Log.w(TAG, "Sync booked dates error: ${e.message}")
    } finally {
      connection?.disconnect()
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

  private val prefs = context.getSharedPreferences("firestore_sync_prefs", Context.MODE_PRIVATE)
  private val knownDocIds = java.util.Collections.synchronizedSet(
    HashSet(prefs.getStringSet("known_doc_ids", emptySet()) ?: emptySet())
  )
  private val declinedPhones = java.util.Collections.synchronizedSet(
    HashSet(prefs.getStringSet("declined_phones", emptySet()) ?: emptySet())
  )
  private var isFirstSyncCycle = !prefs.getBoolean("has_completed_initial_sync", false)

  private suspend fun parseAndSyncInquiries(jsonString: String) {
    try {
      val jsonArray = JSONArray(jsonString)
      val existingInquiries = dao.getAllInquiriesFlow().first()
      val newlyKnownIds = mutableSetOf<String>()
      var isFirstRun = isFirstSyncCycle

      for (i in 0 until jsonArray.length()) {
        val item = jsonArray.optJSONObject(i) ?: continue
        val doc = item.optJSONObject("document") ?: continue
        val docName = doc.optString("name")
        val docId = docName.substringAfterLast("/")
        if (docId.isBlank()) continue

        val fields = doc.optJSONObject("fields") ?: continue
        val clientName = fields.optJSONObject("clientName")?.optString("stringValue")?.trim() ?: ""
        val clientPhone = fields.optJSONObject("clientPhone")?.optString("stringValue")?.trim() ?: ""
        val status = fields.optJSONObject("status")?.optString("stringValue") ?: "PENDING"

        // If this inquiry was Unaccepted (Declined), NEVER re-insert it and NEVER notify!
        if (clientPhone.isBlank() || declinedPhones.contains(clientPhone) || status == "DECLINED") {
          newlyKnownIds.add(docId)
          knownDocIds.add(docId)
          continue
        }

        val location = fields.optJSONObject("location")?.optString("stringValue") ?: ""
        val notes = fields.optJSONObject("notes")?.optString("stringValue") ?: ""
        val rawDates = fields.optJSONObject("dates")?.optString("stringValue")?.trim() ?: ""
        val effectiveDates = if (rawDates.isNotBlank()) rawDates else "General Inquiry (सामान्य पूछताछ)"
        val eventType = fields.optJSONObject("eventType")?.optString("stringValue") ?: "Online Inquiry"

        val createdAtRaw = fields.optJSONObject("createdAt")?.optString("integerValue")
          ?: fields.optJSONObject("timestamp")?.optString("integerValue")
        val createdAt = createdAtRaw?.toLongOrNull() ?: doc.optString("createTime").let { iso ->
          try {
            java.time.Instant.parse(iso).toEpochMilli()
          } catch (_: Exception) {
            val h = docId.hashCode().toLong()
            if (h < 0) -h else h
          }
        }

        // Check if this document has already been processed and notified
        val isAlreadyKnown = knownDocIds.contains(docId)

        // Check if already in local SQLite database
        val existingInDb = existingInquiries.find {
          (it.clientPhone.trim() == clientPhone.trim() && it.createdAt == createdAt) ||
          (it.clientPhone.trim() == clientPhone.trim() && it.dateKey == effectiveDates && it.clientName == clientName)
        }

        if (existingInDb == null && clientPhone.isNotBlank()) {
          val isConfirmedState = (status == "CONFIRMED")

          // Check whether requested dates are already BOOKED in the photographer's calendar
          val parsedDates = effectiveDates.split(",")
            .map { it.trim().substringBefore(" ") }
            .filter { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }

          var isAnyDateBooked = false
          for (d in parsedDates) {
            val dateEntity = dao.getDate(d)
            if (dateEntity != null && dateEntity.status == "BOOKED") {
              isAnyDateBooked = true
              break
            }
          }

          val availabilityTag = if (isAnyDateBooked) {
            "⚠️ तारीख पहले से बुक है (Date Already Booked)"
          } else {
            "🟢 तारीख खाली है (Date Free / Available)"
          }

          val finalNotes = buildString {
            append(availabilityTag)
            if (notes.isNotBlank()) {
              append("\n")
              append(notes)
            }
          }

          val newInquiry = InquiryEntity(
            dateKey = effectiveDates,
            clientName = clientName.ifBlank { "Client (${clientPhone})" },
            clientPhone = clientPhone,
            eventType = eventType,
            location = location,
            notes = finalNotes,
            isConfirmed = isConfirmedState,
            isRead = false,
            createdAt = createdAt
          )

          val insertedId = dao.insertInquiry(newInquiry)
          val savedInquiry = newInquiry.copy(id = insertedId)

          // If date is unbooked, reflect as INQUIRY (Yellow) on photographer's calendar
          if (!isAnyDateBooked && parsedDates.isNotEmpty() && status == "PENDING") {
            for (d in parsedDates) {
              val existing = dao.getDate(d)
              if (existing == null || existing.status != "BOOKED") {
                dao.upsertDate(
                  (existing ?: com.example.data.local.StudioDateEntity(dateKey = d)).copy(
                    status = "INQUIRY",
                    clientName = clientName.ifBlank { "Client ($clientPhone)" },
                    clientPhone = clientPhone,
                    eventType = eventType,
                    location = location,
                    customNote = if (notes.isNotBlank()) notes else "Online Web Inquiry",
                    updatedAt = System.currentTimeMillis()
                  )
                )
              }
            }
          }

          // Trigger notification ONLY if this is a genuinely NEW inquiry and NOT the initial load of old inquiries
          if (!isFirstRun && !isAlreadyKnown && status == "PENDING") {
            Log.d(TAG, "New incoming inquiry detected! Showing notification for: $clientName ($clientPhone)")
            NotificationHelper.showInquiryNotification(context, savedInquiry, isBookedConflict = isAnyDateBooked)
            withContext(Dispatchers.Main) {
              onNewInquiryNotification(savedInquiry)
            }
          }
        }

        newlyKnownIds.add(docId)
        knownDocIds.add(docId)
      }

      // Mark initial sync complete so future new inquiries will ring
      if (isFirstSyncCycle) {
        isFirstSyncCycle = false
        prefs.edit().putBoolean("has_completed_initial_sync", true).apply()
      }

      if (newlyKnownIds.isNotEmpty()) {
        prefs.edit().putStringSet("known_doc_ids", HashSet(knownDocIds)).apply()
      }
    } catch (e: kotlinx.coroutines.CancellationException) {
      throw e
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
      val trimmedPhone = clientPhone.trim()
      if (trimmedPhone.isBlank()) return@withContext

      // 1. Add to permanent declined sets in SharedPreferences so it NEVER notifies again
      declinedPhones.add(trimmedPhone)
      prefs.edit().putStringSet("declined_phones", HashSet(declinedPhones)).apply()

      // 2. Dismiss any active notification from the system status bar
      try {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancelAll()
      } catch (_: Exception) {}

      // 3. Immediately DELETE the inquiry document from cloud Firestore via REST
      try {
        deleteInquiriesByPhoneFromCloudRest(trimmedPhone)
      } catch (e: Exception) {
        Log.w(TAG, "Failed to delete inquiry from cloud REST: ${e.message}")
      }

      // 4. Also delete via Firestore SDK if initialized
      try {
        firestore?.collection("studios")?.document("marutiphoto")?.collection("inquiries")
          ?.whereEqualTo("clientPhone", trimmedPhone)
          ?.get()
          ?.addOnSuccessListener { snapshots ->
            for (doc in snapshots.documents) {
              doc.reference.delete()
            }
          }
      } catch (_: Exception) {}
    }
  }

  private fun deleteInquiriesByPhoneFromCloudRest(phone: String) {
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

      if (connection.responseCode == HttpURLConnection.HTTP_OK) {
        val responseBody = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
          reader.readText()
        }
        val jsonArray = JSONArray(responseBody)
        for (i in 0 until jsonArray.length()) {
          val item = jsonArray.optJSONObject(i) ?: continue
          val doc = item.optJSONObject("document") ?: continue
          val fields = doc.optJSONObject("fields") ?: continue
          val docPhone = fields.optJSONObject("clientPhone")?.optString("stringValue")?.trim() ?: ""
          if (docPhone == phone) {
            val docName = doc.optString("name")
            if (docName.isNotBlank()) {
              deleteSingleDocRest(docName)
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error finding inquiries to delete: ${e.message}")
    } finally {
      connection?.disconnect()
    }
  }

  private fun deleteSingleDocRest(docName: String) {
    var delConn: HttpURLConnection? = null
    try {
      val delUrl = URL("https://firestore.googleapis.com/v1/$docName?key=$apiKey")
      delConn = (delUrl.openConnection() as HttpURLConnection).apply {
        requestMethod = "DELETE"
        connectTimeout = 8000
        readTimeout = 8000
      }
      val code = delConn.responseCode
      Log.d(TAG, "Deleted declined inquiry from cloud REST ($docName), code: $code")
    } catch (e: Exception) {
      Log.w(TAG, "Error sending DELETE to cloud: ${e.message}")
    } finally {
      delConn?.disconnect()
    }
  }
}
