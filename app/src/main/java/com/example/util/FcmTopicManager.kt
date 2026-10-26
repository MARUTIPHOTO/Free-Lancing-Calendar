package com.example.util

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FcmTopicManager {
  private const val TAG = "FcmTopicManager"
  private const val PREFS_NAME = "fcm_topic_prefs"
  private const val KEY_CURRENT_TOPIC = "current_subscribed_topic"
  private const val KEY_CACHED_TOKEN = "cached_fcm_token"
  private const val DATABASE_ID = "ai-studio-android-freelanc-c2b7a674-802c-47dd-bb5b-f701d2a4253c"
  private const val API_KEY = "AIzaSyCf-_qYg-yQgKt7NPCCwAdxz-3JWOyFbgc"

  fun getTopicName(studioSlug: String): String {
    val clean = studioSlug.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.lowercase()
    val effective = if (clean.isNotBlank()) clean else "marutiphoto"
    return "studio_$effective"
  }

  fun subscribeToStudioTopic(context: Context, studioSlug: String) {
    if (studioSlug.isBlank()) return
    val newTopic = getTopicName(studioSlug)
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val lastTopic = prefs.getString(KEY_CURRENT_TOPIC, null)

    // Fetch & store device FCM token into /studios/{studioId}/profile/fcmToken
    fetchAndSyncFcmToken(context, studioSlug)

    // If already subscribed to this exact topic, don't re-subscribe unnecessarily
    if (lastTopic == newTopic) {
      Log.d(TAG, "Already subscribed to FCM topic: $newTopic")
      return
    }

    // Unsubscribe from previous studio topic if switched
    if (!lastTopic.isNullOrBlank()) {
      try {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(lastTopic)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              Log.d(TAG, "Successfully unsubscribed from old FCM topic: $lastTopic")
            }
          }
      } catch (e: Exception) {
        Log.w(TAG, "Error unsubscribing from $lastTopic: ${e.message}")
      }
    }

    // Subscribe to new photographer's studio topic
    try {
      FirebaseMessaging.getInstance().subscribeToTopic(newTopic)
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            prefs.edit().putString(KEY_CURRENT_TOPIC, newTopic).apply()
            Log.d(TAG, "Successfully subscribed to FCM topic: $newTopic")
          } else {
            Log.e(TAG, "Failed to subscribe to FCM topic $newTopic: ${task.exception?.message}")
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Error subscribing to $newTopic: ${e.message}")
    }
  }

  fun fetchAndSyncFcmToken(context: Context, studioSlug: String) {
    if (studioSlug.isBlank()) return
    try {
      FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful && !task.result.isNullOrBlank()) {
          val token = task.result
          val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          prefs.edit().putString(KEY_CACHED_TOKEN, token).apply()
          Log.d(TAG, "FCM Token obtained: $token for studio: $studioSlug")
          uploadFcmTokenToFirestore(context, studioSlug, token)
        } else {
          Log.w(TAG, "Failed to fetch FCM token: ${task.exception?.message}")
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching FCM token: ${e.message}")
    }
  }

  fun uploadFcmTokenToFirestore(context: Context, studioSlug: String, token: String) {
    if (studioSlug.isBlank() || token.isBlank()) return
    val cleanSlug = studioSlug.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.lowercase()

    // 1. Save via Firestore SDK to /studios/{studioId}/profile/fcmToken
    try {
      val app = try { FirebaseApp.getInstance() } catch (_: Exception) { FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance() }
      val db = try { FirebaseFirestore.getInstance(app, DATABASE_ID) } catch (_: Exception) { FirebaseFirestore.getInstance(app) }
      val data = hashMapOf(
        "fcmToken" to token,
        "token" to token,
        "studioId" to cleanSlug,
        "updatedAt" to System.currentTimeMillis(),
        "platform" to "android"
      )

      // Store in /studios/{studioId}/profile/fcmToken document
      db.collection("studios").document(cleanSlug)
        .collection("profile").document("fcmToken")
        .set(data, SetOptions.merge())
        .addOnSuccessListener {
          Log.d(TAG, "FCM Token stored at /studios/$cleanSlug/profile/fcmToken via SDK")
        }
        .addOnFailureListener { e ->
          Log.w(TAG, "SDK save failed for /profile/fcmToken: ${e.message}")
        }

      // Also merge token into main /studios/{studioId} doc
      db.collection("studios").document(cleanSlug)
        .set(hashMapOf("fcmToken" to token, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
    } catch (e: Exception) {
      Log.w(TAG, "Firestore SDK upload warning: ${e.message}")
    }

    // 2. Also save via REST API PATCH to guarantee it is saved even if SDK connection is pending
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val restUrl = "https://firestore.googleapis.com/v1/projects/gen-lang-client-0524337453/databases/$DATABASE_ID/documents/studios/$cleanSlug/profile/fcmToken?updateMask.fieldPaths=token&updateMask.fieldPaths=fcmToken&updateMask.fieldPaths=studioId&updateMask.fieldPaths=updatedAt&updateMask.fieldPaths=platform&key=$API_KEY"
        val connection = (URL(restUrl).openConnection() as HttpURLConnection).apply {
          requestMethod = "POST"
          setRequestProperty("X-HTTP-Method-Override", "PATCH")
          setRequestProperty("Content-Type", "application/json; charset=UTF-8")
          connectTimeout = 7000
          readTimeout = 7000
          doOutput = true
        }

        val payload = JSONObject().apply {
          put("fields", JSONObject().apply {
            put("token", JSONObject().apply { put("stringValue", token) })
            put("fcmToken", JSONObject().apply { put("stringValue", token) })
            put("studioId", JSONObject().apply { put("stringValue", cleanSlug) })
            put("platform", JSONObject().apply { put("stringValue", "android") })
            put("updatedAt", JSONObject().apply { put("integerValue", System.currentTimeMillis().toString()) })
          })
        }

        OutputStreamWriter(connection.outputStream, "UTF-8").use { it.write(payload.toString()) }
        val code = connection.responseCode
        Log.d(TAG, "FCM token REST upload response code: $code for /studios/$cleanSlug/profile/fcmToken")
        connection.disconnect()
      } catch (e: Exception) {
        Log.w(TAG, "FCM token REST upload error: ${e.message}")
      }
    }
  }

  fun unsubscribeCurrentTopic(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val lastTopic = prefs.getString(KEY_CURRENT_TOPIC, null)
    if (!lastTopic.isNullOrBlank()) {
      try {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(lastTopic)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              Log.d(TAG, "Unsubscribed from FCM topic on logout: $lastTopic")
            }
          }
      } catch (e: Exception) {
        Log.w(TAG, "Error unsubscribing on logout: ${e.message}")
      }
      prefs.edit().remove(KEY_CURRENT_TOPIC).apply()
    }
  }
}
