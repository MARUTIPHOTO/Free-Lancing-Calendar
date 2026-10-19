package com.example.data.repository

import com.example.data.local.InquiryEntity
import com.example.data.local.StudioDao
import com.example.data.local.StudioDateEntity
import com.example.data.local.StudioProfileEntity
import com.example.data.model.DateStatus
import kotlinx.coroutines.flow.Flow
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class ClearType {
  ALL_DATES,
  INQUIRIES_ONLY,
  CURRENT_MONTH_ONLY
}

class StudioRepository(private val dao: StudioDao) {

  val allDatesFlow: Flow<List<StudioDateEntity>> = dao.getAllDatesFlow()
  val allInquiriesFlow: Flow<List<InquiryEntity>> = dao.getAllInquiriesFlow()
  val profileFlow: Flow<StudioProfileEntity?> = dao.getProfileFlow()

  suspend fun getProfile(): StudioProfileEntity {
    return dao.getProfile() ?: StudioProfileEntity()
  }

  suspend fun getDate(dateKey: String): StudioDateEntity? {
    return dao.getDate(dateKey)
  }

  suspend fun saveDate(date: StudioDateEntity) {
    dao.upsertDate(date)
  }

  suspend fun markDateFree(dateKey: String, note: String = "") {
    val existing = dao.getDate(dateKey)
    val entity = (existing ?: StudioDateEntity(dateKey = dateKey)).copy(
      status = DateStatus.FREE.name,
      customNote = note,
      updatedAt = System.currentTimeMillis()
    )
    dao.upsertDate(entity)
  }

  suspend fun markDateBooked(
    dateKey: String,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    advanceAmount: Double,
    totalPackageAmount: Double,
    note: String
  ) {
    val existing = dao.getDate(dateKey)
    val entity = (existing ?: StudioDateEntity(dateKey = dateKey)).copy(
      status = DateStatus.BOOKED.name,
      clientName = clientName,
      clientPhone = clientPhone,
      eventType = eventType,
      location = location,
      advanceAmount = advanceAmount,
      totalPackageAmount = totalPackageAmount,
      customNote = note,
      updatedAt = System.currentTimeMillis()
    )
    dao.upsertDate(entity)
  }

  suspend fun markDateInquiry(
    dateKey: String,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    note: String
  ) {
    val existing = dao.getDate(dateKey)
    val entity = (existing ?: StudioDateEntity(dateKey = dateKey)).copy(
      status = DateStatus.INQUIRY.name,
      clientName = clientName,
      clientPhone = clientPhone,
      eventType = eventType,
      location = location,
      customNote = note,
      updatedAt = System.currentTimeMillis()
    )
    dao.upsertDate(entity)
  }

  suspend fun resetDate(dateKey: String) {
    dao.deleteDate(dateKey)
  }

  // Client Portal Inquiry Submission
  suspend fun submitClientInquiry(
    dateKey: String,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    notes: String
  ): Long {
    // 1. Create inquiry record
    val inquiry = InquiryEntity(
      dateKey = dateKey,
      clientName = clientName,
      clientPhone = clientPhone,
      eventType = eventType,
      location = location,
      notes = notes,
      isConfirmed = false,
      isRead = false,
      createdAt = System.currentTimeMillis()
    )
    val id = dao.insertInquiry(inquiry)

    // 2. Turn date into Yellow (INQUIRY)
    val existing = dao.getDate(dateKey)
    val updatedDate = (existing ?: StudioDateEntity(dateKey = dateKey)).copy(
      status = DateStatus.INQUIRY.name,
      clientName = clientName,
      clientPhone = clientPhone,
      eventType = eventType,
      location = location,
      customNote = notes,
      updatedAt = System.currentTimeMillis()
    )
    dao.upsertDate(updatedDate)

    return id
  }

  // Admin clicks "Done" on notification -> Confirms booking (turns Red)
  suspend fun confirmInquiry(
    inquiryId: Long,
    dateKey: String,
    advanceAmount: Double = 0.0,
    totalAmount: Double = 0.0
  ) {
    dao.markInquiryConfirmed(inquiryId)
    val keys = if (dateKey.contains(",")) {
      dateKey.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    } else {
      listOf(dateKey)
    }
    val count = keys.size.coerceAtLeast(1)
    val advPerDate = advanceAmount / count
    val totPerDate = totalAmount / count

    keys.forEach { key ->
      val existing = dao.getDate(key)
      if (existing != null) {
        dao.upsertDate(
          existing.copy(
            status = DateStatus.BOOKED.name,
            advanceAmount = if (advPerDate > 0) advPerDate else existing.advanceAmount,
            totalPackageAmount = if (totPerDate > 0) totPerDate else existing.totalPackageAmount,
            updatedAt = System.currentTimeMillis()
          )
        )
      } else {
        dao.upsertDate(
          StudioDateEntity(
            dateKey = key,
            status = DateStatus.BOOKED.name,
            advanceAmount = advPerDate,
            totalPackageAmount = totPerDate,
            updatedAt = System.currentTimeMillis()
          )
        )
      }
    }
  }

  suspend fun declineInquiry(inquiryId: Long, dateKey: String, makeFree: Boolean) {
    dao.deleteInquiry(inquiryId)
    if (makeFree) {
      markDateFree(dateKey, note = "Free after inquiry declined")
    } else {
      resetDate(dateKey)
    }
  }

  suspend fun markInquiryAsRead(inquiryId: Long) {
    dao.markInquiryRead(inquiryId)
  }

  suspend fun clearCalendar(clearType: ClearType, currentMonthPrefix: String = "") {
    when (clearType) {
      ClearType.ALL_DATES -> {
        dao.clearAllDates()
        dao.clearAllInquiries()
      }
      ClearType.INQUIRIES_ONLY -> {
        dao.clearInquiriesOnly()
      }
      ClearType.CURRENT_MONTH_ONLY -> {
        if (currentMonthPrefix.isNotEmpty()) {
          dao.clearDatesForMonth(currentMonthPrefix)
        }
      }
    }
  }

  suspend fun saveProfile(profile: StudioProfileEntity) {
    dao.upsertProfile(profile)
  }

  fun generateBookingLink(dateKeys: List<String>, studioName: String): String {
    val encodedStudio = try {
      URLEncoder.encode(studioName, StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
      "MarutiStudio"
    }
    val datesParam = if (dateKeys.isEmpty()) "" else dateKeys.sorted().joinToString(",")
    return "https://gen-lang-client-0524337453.web.app/?dates=$datesParam&studio=$encodedStudio"
  }

  fun generateBookingLink(dateKey: String, studioName: String): String {
    return generateBookingLink(if (dateKey.isBlank()) emptyList() else listOf(dateKey), studioName)
  }

  suspend fun markMultipleDatesFree(dateKeys: List<String>, note: String = "") {
    val entities = dateKeys.map { key ->
      val existing = dao.getDate(key)
      (existing ?: StudioDateEntity(dateKey = key)).copy(
        status = DateStatus.FREE.name,
        customNote = note,
        updatedAt = System.currentTimeMillis()
      )
    }
    dao.upsertDates(entities)
  }

  suspend fun markMultipleDatesBooked(
    dateKeys: List<String>,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    advanceAmount: Double,
    totalPackageAmount: Double,
    note: String
  ) {
    val count = dateKeys.size.coerceAtLeast(1)
    val advPerDate = advanceAmount / count
    val totPerDate = totalPackageAmount / count

    val entities = dateKeys.map { key ->
      val existing = dao.getDate(key)
      (existing ?: StudioDateEntity(dateKey = key)).copy(
        status = DateStatus.BOOKED.name,
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        advanceAmount = advPerDate,
        totalPackageAmount = totPerDate,
        customNote = note,
        updatedAt = System.currentTimeMillis()
      )
    }
    dao.upsertDates(entities)
  }

  suspend fun markMultipleDatesInquiry(
    dateKeys: List<String>,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    note: String
  ) {
    val entities = dateKeys.map { key ->
      val existing = dao.getDate(key)
      (existing ?: StudioDateEntity(dateKey = key)).copy(
        status = DateStatus.INQUIRY.name,
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        customNote = note,
        updatedAt = System.currentTimeMillis()
      )
    }
    dao.upsertDates(entities)
  }

  suspend fun resetMultipleDates(dateKeys: List<String>) {
    dateKeys.forEach { dao.deleteDate(it) }
  }

  suspend fun submitClientMultiDateInquiry(
    dateKeys: List<String>,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    notes: String
  ): Long {
    val primaryDate = dateKeys.firstOrNull() ?: ""
    val datesString = dateKeys.joinToString(", ")
    
    // Create combined inquiry record
    val inquiry = InquiryEntity(
      dateKey = datesString,
      clientName = clientName,
      clientPhone = clientPhone,
      eventType = eventType,
      location = location,
      notes = if (dateKeys.size > 1) "[$datesString] $notes" else notes,
      isConfirmed = false,
      isRead = false,
      createdAt = System.currentTimeMillis()
    )
    val id = dao.insertInquiry(inquiry)

    // Mark each selected date as INQUIRY (Yellow)
    val entities = dateKeys.map { key ->
      val existing = dao.getDate(key)
      (existing ?: StudioDateEntity(dateKey = key)).copy(
        status = DateStatus.INQUIRY.name,
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        customNote = notes,
        updatedAt = System.currentTimeMillis()
      )
    }
    dao.upsertDates(entities)

    return id
  }

  fun formatMultiDateShareMessage(
    dateKeys: List<String>,
    profile: StudioProfileEntity,
    clientGreeting: String = ""
  ): String {
    val isHindi = profile.selectedLanguage == "HI" || profile.selectedLanguage == "GU"
    val greeting = if (clientGreeting.isNotBlank()) clientGreeting else if (isHindi) "नमस्ते स्टूडियो ओनर्स & क्लाइंट्स! 🙏" else "Hello Studio Owners & Clients! 🙏"
    val formattedDates = dateKeys.sorted().joinToString("\n• ")
    val link = generateBookingLink(dateKeys, profile.studioName)

    return if (isHindi) {
      """$greeting
📸 *${profile.studioName}*
🎯 *रोल / स्पेशलाइजेशन:* ${profile.roleSpecialization}
🎥 *कैमरा गियर:* ${profile.cameraGear}
💰 *डे शिफ्ट चार्ज:* ₹${profile.defaultDayRate.toInt()} / दिन

🟢 *उपलब्ध खाली तारीखें (Available Free Dates):*
• $formattedDates

✨ वेडिंग कैंडिड शूट, सिनेमैटिक वीडियो या ड्रोन कवरेज बुकिंग के लिए संपर्क करें:
📞 फोन: ${profile.phone}
📍 स्थान: ${profile.address}
🌐 कैलेंडर लिंक:
$link"""
    } else {
      """$greeting
📸 *${profile.studioName}*
🎯 *Role:* ${profile.roleSpecialization}
🎥 *Camera Gear:* ${profile.cameraGear}
💰 *Day Rate:* ₹${profile.defaultDayRate.toInt()} / day

🟢 *My Available Free Dates:*
• $formattedDates

✨ Available for freelance wedding candid, cinematic video & drone shoots:
📞 Phone: ${profile.phone}
📍 Base: ${profile.address}
🌐 Calendar Link:
$link"""
    }
  }

  fun formatShareMessage(
    dateKey: String,
    clientName: String = "",
    profile: StudioProfileEntity,
    customOverrideTemplate: String = ""
  ): String {
    val isHindi = profile.selectedLanguage == "HI" || profile.selectedLanguage == "GU"
    val template = if (customOverrideTemplate.isNotBlank()) {
      customOverrideTemplate
    } else if (isHindi) {
      profile.customTemplateGu
    } else {
      profile.customTemplateEn
    }

    val bookingLink = generateBookingLink(dateKey, profile.studioName)
    val clientGreeting = if (clientName.isNotBlank()) clientName else if (isHindi) "प्रिय मित्र" else "Valued Client"

    return template
      .replace("{client_name}", clientGreeting)
      .replace("{owner_name}", profile.ownerName)
      .replace("{studio_name}", profile.studioName)
      .replace("{date}", dateKey)
      .replace("{phone}", profile.phone)
      .replace("{link}", bookingLink)
  }
}
