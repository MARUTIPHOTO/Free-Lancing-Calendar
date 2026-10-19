package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
  entities = [
    StudioDateEntity::class,
    InquiryEntity::class,
    StudioProfileEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class StudioDatabase : RoomDatabase() {
  abstract fun studioDao(): StudioDao

  companion object {
    @Volatile
    private var INSTANCE: StudioDatabase? = null

    fun getDatabase(context: Context): StudioDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          StudioDatabase::class.java,
          "maruti_studio_calendar.db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(DatabaseCallback())
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback : Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          CoroutineScope(Dispatchers.IO).launch {
            seedInitialData(database.studioDao())
          }
        }
      }
    }

    private suspend fun seedInitialData(dao: StudioDao) {
      // 1. Initial Freelancer Profile
      dao.upsertProfile(
        StudioProfileEntity(
          id = 1,
          studioName = "Sanjay Patel | Freelance Photographer",
          ownerName = "Sanjay Patel",
          phone = "+91 98250 12345",
          address = "Surat / Ahmedabad, Gujarat",
          instagram = "@sanjay_cinematography",
          customTemplateGu = "नमस्ते {client_name}! मैं {owner_name} (फ्रीलांसर कैंडिड/सिनेमैटिक शूटर). {date} तारीख को मैं शूटिंग के लिए उपलब्ध हूँ। कैलेंडर देखने और बुक करने के लिए लिंक: {link}",
          customTemplateEn = "Hello {client_name}! I am {owner_name} (Freelance Photographer). I am available on {date} for shoot/event. Check calendar & book: {link}",
          selectedLanguage = "HI",
          roleSpecialization = "Candid Photography • Cinematic Video • Drone Operator",
          cameraGear = "Sony A7IV, 24-70mm GM II, 70-200mm, DJI RS3 Gimbal, DJI Mini 4 Pro Drone",
          defaultDayRate = 7000.0,
          isFreelancer = true
        )
      )

      // 2. Initial Sample Dates tailored for Freelancer
      val today = LocalDate.now()
      val y = today.year
      val m = today.monthValue

      val sampleDates = listOf(
        // Booked dates (🔴) - booked by other studios & direct clients
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 12),
          status = "BOOKED",
          clientName = "Rahul & Pooja",
          clientPhone = "+91 98980 11223",
          eventType = "Wedding Cinematography (सिनेमैटिक शादी)",
          location = "Surat Party Plot",
          customNote = "Cinematic video with DJI Gimbal & Sony FX3",
          advanceAmount = 5000.0,
          totalPackageAmount = 14000.0,
          bookingSourceStudio = "Maruti Studio Surat",
          shiftType = "Full Day (पूरा दिन)",
          gearRequired = "Sony FX3 + DJI RS3 Pro Gimbal"
        ),
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 18),
          status = "BOOKED",
          clientName = "Mehul Patel",
          clientPhone = "+91 97240 44556",
          eventType = "Pre-Wedding Shoot (प्री-वेडिंग)",
          location = "Saputara Hill Station",
          customNote = "Outdoor 2-day shoot with drone & prime lenses",
          advanceAmount = 4000.0,
          totalPackageAmount = 12000.0,
          bookingSourceStudio = "Direct Client",
          shiftType = "Full Day (पूरा दिन)",
          gearRequired = "Sony A7IV + DJI Drone"
        ),
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 24),
          status = "BOOKED",
          clientName = "Aniket Shah",
          clientPhone = "+91 98255 77889",
          eventType = "Reception Candid Shoot (रिसेप्शन कैंडिड)",
          location = "Town Hall, Ahmedabad",
          customNote = "Evening candid portraits & prime lenses",
          advanceAmount = 3000.0,
          totalPackageAmount = 8000.0,
          bookingSourceStudio = "Dream Art Films",
          shiftType = "Evening Shift (शाम का समय)",
          gearRequired = "Sony A7IV + 35mm + 85mm"
        ),

        // Inquiry dates (🟡)
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 15),
          status = "INQUIRY",
          clientName = "Hardik Goswami (Studio Hire)",
          clientPhone = "+91 98790 99887",
          eventType = "Drone Pilot for Sangeet & Ring Ceremony",
          location = "Rajkot Banquet Hall",
          customNote = "Studio inquiry for aerial drone coverage",
          advanceAmount = 0.0,
          totalPackageAmount = 6500.0,
          bookingSourceStudio = "Royal Studio Rajkot",
          shiftType = "Evening Shift (शाम का समय)"
        ),
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 28),
          status = "INQUIRY",
          clientName = "Kunal & Jinal",
          clientPhone = "+91 99130 55667",
          eventType = "Candid Photography",
          location = "Vadodara Club",
          customNote = "Client requested freelance per-day quotation",
          advanceAmount = 0.0,
          totalPackageAmount = 7000.0,
          bookingSourceStudio = "Direct Client",
          shiftType = "Full Day (पूरा दिन)"
        ),

        // Free dates (🟢 Open dates)
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 10),
          status = "FREE",
          customNote = "Available for Studio Booking or Candid Shoot"
        ),
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 14),
          status = "FREE",
          customNote = "Open for Pre-wedding or Haldi / Mehndi"
        ),
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 16),
          status = "FREE",
          customNote = "Available for Full Day Shoot / Drone Pilot"
        ),
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 20),
          status = "FREE",
          customNote = "Free for wedding booking across Gujarat"
        ),
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 21),
          status = "FREE",
          customNote = "Auspicious wedding date open for hire"
        ),
        StudioDateEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 25),
          status = "FREE",
          customNote = "Open slot"
        )
      )
      dao.upsertDates(sampleDates)

      // 3. Initial Inquiries
      dao.insertInquiry(
        InquiryEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 15),
          clientName = "Hardik Goswami (Royal Studio)",
          clientPhone = "+91 98790 99887",
          eventType = "Drone Pilot & Gimbal Video",
          location = "Rajkot Banquet Hall",
          notes = "Need freelance drone operator for evening wedding & sangeet.",
          isConfirmed = false,
          isRead = false
        )
      )
      dao.insertInquiry(
        InquiryEntity(
          dateKey = String.format("%04d-%02d-%02d", y, m, 28),
          clientName = "Kunal & Jinal",
          clientPhone = "+91 99130 55667",
          eventType = "Candid Photography",
          location = "Vadodara Club",
          notes = "Looking for freelance candid photographer with Sony gear.",
          isConfirmed = false,
          isRead = false
        )
      )
    }
  }
}
