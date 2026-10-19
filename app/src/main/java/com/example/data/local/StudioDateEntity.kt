package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DateStatus

@Entity(tableName = "studio_dates")
data class StudioDateEntity(
  @PrimaryKey
  val dateKey: String, // Format: YYYY-MM-DD e.g. "2026-11-20"
  val status: String = DateStatus.DEFAULT.name,
  val clientName: String = "",
  val clientPhone: String = "",
  val eventType: String = "", // Lagna / Wedding, Pre-Wedding, Reception, Sagai, etc.
  val location: String = "",
  val customNote: String = "",
  val advanceAmount: Double = 0.0,
  val totalPackageAmount: Double = 0.0,
  val customShareMessage: String = "",
  val updatedAt: Long = System.currentTimeMillis(),
  // Freelancer specific fields:
  val bookingSourceStudio: String = "", // e.g. "Maruti Studio", "Dream Art Films", or "Direct Party"
  val shiftType: String = "Full Day (पूरा दिन)", // Full Day, Half Day, Morning, Evening, Night
  val gearRequired: String = "" // e.g. "Camera + Gimbal + Drone"
) {
  val dateStatus: DateStatus
    get() = DateStatus.fromString(status)
}
