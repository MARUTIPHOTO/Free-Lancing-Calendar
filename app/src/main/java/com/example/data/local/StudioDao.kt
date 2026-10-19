package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {

  @Query("SELECT * FROM studio_dates ORDER BY dateKey ASC")
  fun getAllDatesFlow(): Flow<List<StudioDateEntity>>

  @Query("SELECT * FROM studio_dates WHERE dateKey = :dateKey LIMIT 1")
  suspend fun getDate(dateKey: String): StudioDateEntity?

  @Query("SELECT * FROM studio_dates WHERE dateKey LIKE :prefix || '%' ORDER BY dateKey ASC")
  fun getDatesForMonthFlow(prefix: String): Flow<List<StudioDateEntity>>

  @Upsert
  suspend fun upsertDate(date: StudioDateEntity)

  @Upsert
  suspend fun upsertDates(dates: List<StudioDateEntity>)

  @Query("DELETE FROM studio_dates WHERE dateKey = :dateKey")
  suspend fun deleteDate(dateKey: String)

  @Query("DELETE FROM studio_dates")
  suspend fun clearAllDates()

  @Query("DELETE FROM studio_dates WHERE status = 'INQUIRY'")
  suspend fun clearInquiriesOnly()

  @Query("DELETE FROM studio_dates WHERE dateKey LIKE :monthPrefix || '%'")
  suspend fun clearDatesForMonth(monthPrefix: String)

  // Inquiries
  @Query("SELECT * FROM inquiries ORDER BY createdAt DESC")
  fun getAllInquiriesFlow(): Flow<List<InquiryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInquiry(inquiry: InquiryEntity): Long

  @Query("UPDATE inquiries SET isConfirmed = 1 WHERE id = :id")
  suspend fun markInquiryConfirmed(id: Long)

  @Query("UPDATE inquiries SET isRead = 1 WHERE id = :id")
  suspend fun markInquiryRead(id: Long)

  @Query("DELETE FROM inquiries WHERE id = :id")
  suspend fun deleteInquiry(id: Long)

  @Query("DELETE FROM inquiries")
  suspend fun clearAllInquiries()

  // Profile
  @Query("SELECT * FROM studio_profile WHERE id = 1 LIMIT 1")
  fun getProfileFlow(): Flow<StudioProfileEntity?>

  @Query("SELECT * FROM studio_profile WHERE id = 1 LIMIT 1")
  suspend fun getProfile(): StudioProfileEntity?

  @Upsert
  suspend fun upsertProfile(profile: StudioProfileEntity)
}
