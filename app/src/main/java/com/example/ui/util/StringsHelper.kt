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
}
