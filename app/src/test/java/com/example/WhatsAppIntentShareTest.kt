package com.example

import android.content.Intent
import com.example.ui.util.WhatsAppShareHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WhatsAppIntentShareTest {

  @Test
  fun testCreateWhatsAppSendIntent() {
    val sampleMessage = "My Available Dates:\n• 2026-11-20\n• 2026-11-21\nBooking Link: https://marutiphoto.app/book?dates=2026-11-20,2026-11-21&studio=MarutiStudio"
    val intent = WhatsAppShareHelper.createWhatsAppSendIntent(sampleMessage)

    assertEquals(Intent.ACTION_SEND, intent.action)
    assertEquals("text/plain", intent.type)
    assertEquals("com.whatsapp", intent.`package`)
    assertEquals(sampleMessage, intent.getStringExtra(Intent.EXTRA_TEXT))
  }

  @Test
  fun testCreateWhatsAppUriIntent() {
    val sampleMessage = "Available: 2026-11-20, Booking: https://marutiphoto.app/book"
    val intent = WhatsAppShareHelper.createWhatsAppUriIntent(sampleMessage)

    assertEquals(Intent.ACTION_VIEW, intent.action)
    assertNotNull(intent.data)
    assertTrue(intent.data.toString().startsWith("https://api.whatsapp.com/send?text="))
  }

  @Test
  fun testCreateGenericChooserIntent() {
    val sampleMessage = "Share message with client"
    val chooserIntent = WhatsAppShareHelper.createGenericChooserIntent(sampleMessage, "Share Booking Dates")

    assertEquals(Intent.ACTION_CHOOSER, chooserIntent.action)
    val targetIntent = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
    assertNotNull(targetIntent)
    assertEquals(Intent.ACTION_SEND, targetIntent?.action)
    assertEquals("text/plain", targetIntent?.type)
    assertEquals(sampleMessage, targetIntent?.getStringExtra(Intent.EXTRA_TEXT))
  }
}
