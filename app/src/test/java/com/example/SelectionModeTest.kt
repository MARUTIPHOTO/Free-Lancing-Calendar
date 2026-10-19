package com.example

import com.example.data.local.InquiryEntity
import com.example.data.local.StudioDao
import com.example.data.local.StudioDateEntity
import com.example.data.local.StudioProfileEntity
import com.example.data.repository.StudioRepository
import com.example.ui.viewmodel.StudioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelectionModeTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private class FakeStudioDao : StudioDao {
    val dates = mutableListOf<StudioDateEntity>()
    override fun getAllDatesFlow() = flowOf(dates)
    override suspend fun getDate(dateKey: String) = dates.find { it.dateKey == dateKey }
    override fun getDatesForMonthFlow(prefix: String) = flowOf(dates.filter { it.dateKey.startsWith(prefix) })
    override suspend fun upsertDate(date: StudioDateEntity) {
      dates.removeIf { it.dateKey == date.dateKey }
      dates.add(date)
    }
    override suspend fun upsertDates(datesList: List<StudioDateEntity>) {
      datesList.forEach { upsertDate(it) }
    }
    override suspend fun deleteDate(dateKey: String) {
      dates.removeIf { it.dateKey == dateKey }
    }
    override suspend fun clearAllDates() { dates.clear() }
    override suspend fun clearInquiriesOnly() { dates.removeIf { it.status == "INQUIRY" } }
    override suspend fun clearDatesForMonth(monthPrefix: String) { dates.removeIf { it.dateKey.startsWith(monthPrefix) } }
    override fun getAllInquiriesFlow() = flowOf(emptyList<InquiryEntity>())
    override suspend fun insertInquiry(inquiry: InquiryEntity) = 1L
    override suspend fun markInquiryConfirmed(id: Long) {}
    override suspend fun markInquiryRead(id: Long) {}
    override suspend fun deleteInquiry(id: Long) {}
    override suspend fun clearAllInquiries() {}
    override fun getProfileFlow() = flowOf(StudioProfileEntity())
    override suspend fun getProfile() = StudioProfileEntity()
    override suspend fun upsertProfile(profile: StudioProfileEntity) {}
  }

  @Test
  fun testSelectionModeToggleAndMultipleDatesHighlight() = runTest {
    val repository = StudioRepository(FakeStudioDao())
    val viewModel = StudioViewModel(repository)

    // Collect uiState in backgroundScope so state flows are active
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect {}
    }

    // Initial state: Selection Mode is OFF
    assertFalse(viewModel.uiState.value.isSelectionMode)
    assertTrue(viewModel.uiState.value.selectedDatesList.isEmpty())

    // 1. Toggle Selection Mode ON
    viewModel.toggleSelectionMode(true)
    assertTrue(viewModel.uiState.value.isSelectionMode)

    // 2. Click multiple dates at once to highlight them
    viewModel.selectDate("2026-11-14")
    viewModel.selectDate("2026-11-15")
    viewModel.selectDate("2026-11-16")

    // 3. Verify ViewModel maintains the list of selected dates
    val selected = viewModel.uiState.value.selectedDatesList
    assertEquals(3, selected.size)
    assertEquals(listOf("2026-11-14", "2026-11-15", "2026-11-16"), selected)

    // 4. Click a date again to deselect / unhighlight
    viewModel.selectDate("2026-11-15")
    val updated = viewModel.uiState.value.selectedDatesList
    assertEquals(2, updated.size)
    assertEquals(listOf("2026-11-14", "2026-11-16"), updated)

    // 5. Toggle Selection Mode OFF -> clears selection
    viewModel.toggleSelectionMode(false)
    assertFalse(viewModel.uiState.value.isSelectionMode)
    assertTrue(viewModel.uiState.value.selectedDatesList.isEmpty())
  }

  @Test
  fun testWhatsAppShareMessageConstructionWithSelectedDatesAndBookingLink() = runTest {
    val repository = StudioRepository(FakeStudioDao())
    val viewModel = StudioViewModel(repository)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect {}
    }

    // Toggle Selection Mode ON and select 3 dates
    viewModel.toggleSelectionMode(true)
    viewModel.selectDate("2026-11-20")
    viewModel.selectDate("2026-11-21")
    viewModel.selectDate("2026-11-22")

    // Construct pre-filled WhatsApp message
    val message = viewModel.getWhatsAppShareMessageForSelectedDates()

    // Verify all selected dates are present in the message
    assertTrue(message.contains("2026-11-20"))
    assertTrue(message.contains("2026-11-21"))
    assertTrue(message.contains("2026-11-22"))

    // Verify studio's booking link is generated and included in the message
    val bookingLink = viewModel.getBookingLinkForSelectedDates()
    assertTrue(bookingLink.contains("https://marutiphoto.app/book?dates="))
    assertTrue(bookingLink.contains("2026-11-20,2026-11-21,2026-11-22"))
    assertTrue(message.contains(bookingLink))
  }
}
