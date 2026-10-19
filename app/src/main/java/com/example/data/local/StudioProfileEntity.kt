package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "studio_profile")
data class StudioProfileEntity(
  @PrimaryKey
  val id: Int = 1,
  val studioName: String = "Sanjay Patel | Freelance Photographer",
  val ownerName: String = "Sanjay Patel",
  val phone: String = "+91 98250 12345",
  val address: String = "Surat / Ahmedabad, Gujarat",
  val instagram: String = "@sanjay_cinematography",
  val customTemplateGu: String = "नमस्ते {client_name}! मैं {owner_name} (फ्रीलांसर फोटोग्राफर). {date} तारीख को मैं शूटिंग के लिए उपलब्ध (फ्री) हूँ। विवरण देखने या बुक करने के लिए लिंक: {link}",
  val customTemplateEn: String = "Hello {client_name}! I am {owner_name} (Freelance Photographer). I am available on {date} for candid/cinematic shoot. Check availability & book: {link}",
  val selectedLanguage: String = "HI", // "HI" or "EN"
  val roleSpecialization: String = "Candid Photography • Cinematic Films • Drone Pilot",
  val cameraGear: String = "Sony A7IV, 24-70mm f/2.8 GM II, DJI RS3 Gimbal, DJI Mini 4 Pro Drone",
  val defaultDayRate: Double = 7000.0,
  val isFreelancer: Boolean = true
) {
  val customTemplateHi: String
    get() = customTemplateGu
}
