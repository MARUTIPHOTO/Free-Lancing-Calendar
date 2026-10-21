package com.example.ui.util

object StringsHelper {

  fun getMonthName(monthNumber: Int, isHindi: Boolean): String {
    return if (isHindi) {
      when (monthNumber) {
        1 -> "जनवरी"
        2 -> "फ़रवरी"
        3 -> "मार्च"
        4 -> "अप्रैल"
        5 -> "मई"
        6 -> "जून"
        7 -> "जुलाई"
        8 -> "अगस्त"
        9 -> "सितम्बर"
        10 -> "अक्टूबर"
        11 -> "नवम्बर"
        12 -> "दिसम्बर"
        else -> ""
      }
    } else {
      when (monthNumber) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> ""
      }
    }
  }

  fun getWeekdayNames(isHindi: Boolean): List<String> {
    return if (isHindi) {
      listOf("रवि", "सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि")
    } else {
      listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    }
  }

  val eventTypesHi = listOf(
    "विवाह / शादी (Wedding Ceremony)",
    "प्री-वेडिंग शूट (Pre-Wedding)",
    "सगाई / रिंग सेरेमनी (Engagement / Ring)",
    "रिसेप्शन (Reception)",
    "संगीत संध्या (Sangeet)",
    "हल्दी / मेहंदी (Haldi / Mehndi)",
    "बेबी शॉवर (Baby Shower)",
    "बर्थडे पार्टी (Birthday)",
    "कमर्शियल / इवेंट (Corporate / Event)"
  )

  val eventTypesEn = listOf(
    "Wedding Ceremony",
    "Pre-Wedding Shoot",
    "Engagement / Ring Ceremony",
    "Reception",
    "Sangeet Sandhya",
    "Haldi / Mehndi",
    "Baby Shower / Maternity",
    "Birthday Celebration",
    "Commercial / Corporate Event"
  )

  val freelancerRolesHi = listOf(
    "कैंडिड फोटोग्राफर (Candid Photography)",
    "सिनेमैटिक वीडियोग्राफर (Cinematic Films)",
    "ड्रोन पायलट / ऑपरेटर (Drone Pilot)",
    "ट्रेडिशनल फोटोग्राफर (Traditional Photography)",
    "गिम्बल ऑपरेटर (Gimbal Operator)",
    "प्री-वेडिंग शूटर (Pre-Wedding Shooter)",
    "वीडियो एडिटर / कलरिस्ट (Colorist / Editor)"
  )

  val freelancerRolesEn = listOf(
    "Candid Photographer",
    "Cinematic Videographer",
    "Drone Pilot / Aerial Operator",
    "Traditional Photographer",
    "Gimbal Operator",
    "Pre-Wedding Specialist",
    "Video Editor / Colorist"
  )

  val shiftTypesHi = listOf(
    "Full Day (पूरा दिन शूट)",
    "Half Day (हाफ डे शिफ्ट)",
    "Morning Shift (सुबह की शिफ्ट)",
    "Evening Reception (शाम / रिसेप्शन)",
    "Night Lagna (रात्रि विवाह)"
  )

  val shiftTypesEn = listOf(
    "Full Day Shoot",
    "Half Day Shift",
    "Morning Shift",
    "Evening Reception",
    "Night Wedding Shoot"
  )

  // Backward compatibility aliases
  val eventTypesGu: List<String> get() = eventTypesHi
  val freelancerRolesGu: List<String> get() = freelancerRolesHi
  val shiftTypesGu: List<String> get() = shiftTypesHi

  val gearPresets = listOf(
    "Sony A7IV + 24-70mm GM II + 70-200mm",
    "Sony FX3 + DJI RS3 Pro Gimbal",
    "DJI Mini 4 Pro / Mavic 3 Drone",
    "Canon R6 Mark II + 28-70mm f/2",
    "Nikon Z6 III + 24-70mm f/2.8",
    "Godox V1 Flash + Light Stands"
  )

  fun formatDateChip(dateKey: String): String {
    return try {
      val parts = dateKey.trim().split("-")
      if (parts.size == 3) {
        val year = parts[0]
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthName = if (month in 1..12) months[month] else parts[1]
        String.format("%02d-%s-%s", day, monthName, year)
      } else {
        dateKey
      }
    } catch (e: Exception) {
      dateKey
    }
  }

  fun parseDateKeys(dateKeysString: String): List<String> {
    return if (dateKeysString.contains(",")) {
      dateKeysString.split(",").map { it.trim() }.filter { it.isNotEmpty() }.sorted()
    } else if (dateKeysString.isNotBlank()) {
      listOf(dateKeysString.trim())
    } else {
      emptyList()
    }
  }

  data class ServiceItem(
    val id: String,
    val nameHi: String,
    val nameEn: String,
    val icon: String,
    val defaultPrice: Double
  )

  val availableServices = listOf(
    ServiceItem("candid_photo", "कैंडिड फोटोग्राफी", "Candid Photography", "📸", 10000.0),
    ServiceItem("traditional_photo", "ट्रेडिशनल फोटोग्राफी", "Traditional Photography", "📷", 7000.0),
    ServiceItem("cinematic_video", "सिनेमैटिक वीडियोग्राफी", "Cinematic Videography", "🎥", 12000.0),
    ServiceItem("traditional_video", "ट्रेडिशनल वीडियो", "Traditional Videography", "📹", 7000.0),
    ServiceItem("drone", "ड्रोन पायलट / शूट", "Drone Shoot", "🚁", 8000.0),
    ServiceItem("pre_wedding", "प्री-वेडिंग शूट", "Pre-Wedding Shoot", "💍", 15000.0)
  )

  fun getServiceRate(serviceId: String, profile: com.example.data.local.StudioProfileEntity): Double {
    return when (serviceId) {
      "candid_photo" -> if (profile.rateCandidPhoto > 0) profile.rateCandidPhoto else 10000.0
      "traditional_photo" -> if (profile.rateTraditionalPhoto > 0) profile.rateTraditionalPhoto else 7000.0
      "cinematic_video" -> if (profile.rateCinematicVideo > 0) profile.rateCinematicVideo else 12000.0
      "traditional_video" -> if (profile.rateTraditionalVideo > 0) profile.rateTraditionalVideo else 7000.0
      "drone" -> if (profile.rateDrone > 0) profile.rateDrone else 8000.0
      "pre_wedding" -> if (profile.ratePreWedding > 0) profile.ratePreWedding else 15000.0
      else -> profile.defaultDayRate
    }
  }

  fun getServiceName(serviceId: String, isHindi: Boolean): String {
    val s = availableServices.firstOrNull { it.id == serviceId }
    return if (s != null) {
      if (isHindi) "${s.icon} ${s.nameHi}" else "${s.icon} ${s.nameEn}"
    } else {
      serviceId
    }
  }
}
