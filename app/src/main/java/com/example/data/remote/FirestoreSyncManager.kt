package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.InquiryEntity
import com.example.data.local.StudioDao
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager(
  private val context: Context,
  private val dao: StudioDao,
  private val onNewInquiryNotification: (InquiryEntity) -> Unit
) {
  private val TAG = "FirestoreSyncManager"
  private val databaseId = "ai-studio-android-freelanc-c2b7a674-802c-47dd-bb5b-f701d2a4253c"
  private var listenerRegistration: ListenerRegistration? = null
  private var firestore: FirebaseFirestore? = null

  init {
    try {
      val app = try {
        FirebaseApp.getInstance()
      } catch (e: Exception) {
        FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance()
      }
      val resDbId = try {
        val idRes = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
        if (idRes != 0) context.getString(idRes) else databaseId
      } catch (e: Exception) {
        databaseId
      }
      firestore = try {
        FirebaseFirestore.getInstance(app, resDbId)
      } catch (e: Exception) {
        Log.w(TAG, "Fallback to default database instance: ${e.message}")
        FirebaseFirestore.getInstance(app)
      }
      Log.d(TAG, "FirebaseFirestore initialized for database: $resDbId")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize FirebaseFirestore: ${e.message}", e)
    }
  }

  fun startListening(scope: CoroutineScope) {
    val db = firestore ?: return
    try {
      listenerRegistration?.remove()
      val collectionRef = db.collection("studios").document("marutiphoto").collection("inquiries")

      listenerRegistration = collectionRef.addSnapshotListener { snapshots, e ->
        if (e != null) {
          Log.w(TAG, "Listen failed: ${e.message}")
          return@addSnapshotListener
        }

        if (snapshots != null && !snapshots.isEmpty) {
          scope.launch(Dispatchers.IO) {
            val existingInquiries = dao.getAllInquiriesFlow().first()
            val existingPhones = existingInquiries.map { it.clientPhone }.toSet()

            for (doc in snapshots.documents) {
              val status = doc.getString("status") ?: "PENDING"
              val clientName = doc.getString("clientName") ?: ""
              val clientPhone = doc.getString("clientPhone") ?: ""
              val dates = doc.getString("dates") ?: ""
              val eventType = doc.getString("eventType") ?: "Booking Inquiry"
              val location = doc.getString("location") ?: ""
              val notes = doc.getString("notes") ?: ""
              val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

              if (status == "PENDING" && clientPhone.isNotBlank()) {
                // If not already in local database, add it and notify
                val alreadyExists = existingInquiries.any { 
                  it.clientPhone == clientPhone && it.dateKey == dates
                }

                if (!alreadyExists) {
                  val newInquiry = InquiryEntity(
                    dateKey = dates,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    eventType = eventType,
                    location = location,
                    notes = if (notes.isNotBlank()) notes else "Online Web Inquiry",
                    isConfirmed = false,
                    isRead = false,
                    createdAt = createdAt
                  )
                  val newId = dao.insertInquiry(newInquiry)
                  val savedInquiry = newInquiry.copy(id = newId)

                  Log.d(TAG, "New web inquiry inserted into local DB: $savedInquiry")
                  
                  // Trigger in-app notification banner
                  launch(Dispatchers.Main) {
                    onNewInquiryNotification(savedInquiry)
                  }
                }
              }
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error starting snapshot listener: ${e.message}", e)
    }
  }

  fun stopListening() {
    listenerRegistration?.remove()
    listenerRegistration = null
  }

  suspend fun markInquiryConfirmed(clientPhone: String, dates: String) {
    val db = firestore ?: return
    try {
      val query = db.collection("studios").document("marutiphoto").collection("inquiries")
        .whereEqualTo("clientPhone", clientPhone)
        .get()
        .await()
      for (doc in query.documents) {
        doc.reference.update("status", "CONFIRMED").await()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to update Firestore inquiry status: ${e.message}")
    }
  }

  suspend fun markInquiryDeclined(clientPhone: String, dates: String) {
    val db = firestore ?: return
    try {
      val query = db.collection("studios").document("marutiphoto").collection("inquiries")
        .whereEqualTo("clientPhone", clientPhone)
        .get()
        .await()
      for (doc in query.documents) {
        doc.reference.update("status", "DECLINED").await()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to update Firestore inquiry status: ${e.message}")
    }
  }
}
