package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WhatsAppShareHelper {

  /**
   * Creates an explicit ACTION_SEND Intent targeted specifically at WhatsApp.
   */
  fun createWhatsAppSendIntent(messageText: String): Intent {
    return Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, messageText)
      setPackage("com.whatsapp")
    }
  }

  /**
   * Creates a Deep Link / Web Intent for the WhatsApp Send API.
   */
  fun createWhatsAppUriIntent(messageText: String): Intent {
    val encoded = try {
      URLEncoder.encode(messageText, StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
      ""
    }
    return Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?text=$encoded"))
  }

  /**
   * Creates a standard Android Chooser Intent as graceful fallback for other messaging apps.
   */
  fun createGenericChooserIntent(messageText: String, title: String = "Share Booking Dates"): Intent {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, messageText)
    }
    return Intent.createChooser(shareIntent, title)
  }

  /**
   * Launches WhatsApp sharing via Intent with robust fallbacks:
   * 1. Try com.whatsapp direct intent.
   * 2. Try api.whatsapp.com URI Intent.
   * 3. Fallback to Android share chooser if WhatsApp is not directly callable.
   */
  fun shareToWhatsApp(
    context: Context,
    messageText: String,
    chooserTitle: String = "Share via WhatsApp"
  ): Boolean {
    // 1. Try Direct WhatsApp Package Intent
    try {
      val sendIntent = createWhatsAppSendIntent(messageText)
      context.startActivity(sendIntent)
      return true
    } catch (e: Exception) {
      // WhatsApp app might not be installed or intent cannot resolve directly
    }

    // 2. Try WhatsApp API URL intent (can open WhatsApp or browser)
    try {
      val uriIntent = createWhatsAppUriIntent(messageText)
      context.startActivity(uriIntent)
      return true
    } catch (e: Exception) {
      // Failed to open URI
    }

    // 3. Fallback to generic chooser
    return try {
      val chooser = createGenericChooserIntent(messageText, chooserTitle)
      context.startActivity(chooser)
      true
    } catch (e: Exception) {
      Toast.makeText(context, "Could not open sharing application", Toast.LENGTH_SHORT).show()
      false
    }
  }
}
