package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class DateStatus(
  val labelEn: String,
  val labelHi: String,
  val labelGu: String = labelHi,
  val primaryColor: Color,
  val containerColor: Color,
  val onColor: Color
) {
  FREE(
    labelEn = "Free Date",
    labelHi = "खाली तारीख (उपलब्ध)",
    labelGu = "खाली तारीख (उपलब्ध)",
    primaryColor = Color(0xFF16A34A),       // Rich Green
    containerColor = Color(0xFFDCFCE7),     // Light Green Tint
    onColor = Color(0xFF14532D)
  ),
  INQUIRY(
    labelEn = "Inquiry Date",
    labelHi = "पूछताछ (पेंडिंग)",
    labelGu = "पूछताछ (पेंडिंग)",
    primaryColor = Color(0xFFEAB308),       // Amber Yellow
    containerColor = Color(0xFFFEF9C3),     // Light Yellow Tint
    onColor = Color(0xFF713F12)
  ),
  BOOKED(
    labelEn = "Booked Date",
    labelHi = "बुक तारीख (कन्फर्म)",
    labelGu = "बुक तारीख (कन्फर्म)",
    primaryColor = Color(0xFFDC2626),       // Vibrant Red
    containerColor = Color(0xFFFEE2E2),     // Light Red Tint
    onColor = Color(0xFF7F1D1D)
  ),
  DEFAULT(
    labelEn = "Open / Regular",
    labelHi = "सामान्य दिन",
    labelGu = "सामान्य दिन",
    primaryColor = Color(0xFF64748B),       // Slate
    containerColor = Color(0xFFF1F5F9),
    onColor = Color(0xFF1E293B)
  );

  companion object {
    fun fromString(value: String): DateStatus {
      return try {
        valueOf(value.uppercase())
      } catch (e: Exception) {
        DEFAULT
      }
    }
  }
}

data class DateWorkAssignment(
  val originalDateKey: String,
  val newDateKey: String,
  val eventType: String,
  val location: String = ""
)
