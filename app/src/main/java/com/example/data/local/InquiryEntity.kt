package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inquiries")
data class InquiryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val dateKey: String, // YYYY-MM-DD
  val clientName: String,
  val clientPhone: String,
  val eventType: String = "",
  val location: String = "",
  val notes: String = "",
  val isConfirmed: Boolean = false,
  val isRead: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)
