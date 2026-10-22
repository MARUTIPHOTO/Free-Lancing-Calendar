package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.InquiryEntity
import com.example.data.local.StudioDateEntity
import com.example.data.local.StudioProfileEntity
import com.example.data.model.DateStatus
import com.example.data.repository.ClearType
import com.example.data.repository.StudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class AppNavDestination {
  CALENDAR,
  NOTIFICATIONS,
  CLIENT_PORTAL,
  BOOKINGS_LIST,
  PROFILE
}

enum class DateFilter {
  ALL,
  FREE,
  INQUIRY,
  BOOKED
}

enum class BulkActionType {
  BOOKED,
  INQUIRY
}

data class CalendarUiState(
  val currentYearMonth: YearMonth = YearMonth.now(),
  val selectedDateKey: String? = null,
  val datesMap: Map<String, StudioDateEntity> = emptyMap(),
  val inquiries: List<InquiryEntity> = emptyList(),
  val profile: StudioProfileEntity = StudioProfileEntity(),
  val currentNav: AppNavDestination = AppNavDestination.CALENDAR,
  val activeFilter: DateFilter = DateFilter.ALL,
  val bannerNotification: InquiryEntity? = null,
  val showClearDialog: Boolean = false,
  val showShareDialog: Boolean = false,
  val shareDialogDateKey: String? = null,
  val showDateDetailSheet: Boolean = false,
  val dateDetailSheetKey: String? = null,
  val inquiryToConfirm: InquiryEntity? = null,
  // Multi-date selection features
  val isMultiSelectMode: Boolean = false,
  val selectedMultiDateKeys: Set<String> = emptySet(),
  val showBulkActionDialog: Boolean = false,
  val bulkActionType: BulkActionType? = null,
  val customShareMessage: String? = null
) {
  val selectedDatesList: List<String>
    get() = selectedMultiDateKeys.sorted()
  val isSelectionMode: Boolean
    get() = isMultiSelectMode
}

class StudioViewModel(private val repository: StudioRepository) : ViewModel() {

  private val _currentYearMonth = MutableStateFlow(YearMonth.now())
  private val _selectedDateKey = MutableStateFlow<String?>(null)
  private val _currentNav = MutableStateFlow(AppNavDestination.CALENDAR)
  private val _activeFilter = MutableStateFlow(DateFilter.ALL)
  private val _bannerNotification = MutableStateFlow<InquiryEntity?>(null)
  private val _showClearDialog = MutableStateFlow(false)
  private val _showShareDialog = MutableStateFlow(false)
  private val _shareDialogDateKey = MutableStateFlow<String?>(null)
  private val _showDateDetailSheet = MutableStateFlow(false)
  private val _dateDetailSheetKey = MutableStateFlow<String?>(null)
  private val _inquiryToConfirm = MutableStateFlow<InquiryEntity?>(null)

  // Multi-selection flows
  private val _isMultiSelectMode = MutableStateFlow(false)
  private val _selectedMultiDateKeys = MutableStateFlow<Set<String>>(emptySet())
  private val _showBulkActionDialog = MutableStateFlow(false)
  private val _bulkActionType = MutableStateFlow<BulkActionType?>(null)
  private val _customShareMessage = MutableStateFlow<String?>(null)

  val uiState: StateFlow<CalendarUiState> = combine(
    listOf(
      _currentYearMonth,
      _selectedDateKey,
      _currentNav,
      _activeFilter,
      _bannerNotification,
      _showClearDialog,
      _showShareDialog,
      _shareDialogDateKey,
      _showDateDetailSheet,
      _dateDetailSheetKey,
      _inquiryToConfirm,
      _isMultiSelectMode,
      _selectedMultiDateKeys,
      _showBulkActionDialog,
      _bulkActionType,
      _customShareMessage,
      repository.allDatesFlow,
      repository.allInquiriesFlow,
      repository.profileFlow
    )
  ) { args ->
    @Suppress("UNCHECKED_CAST")
    val ym = args[0] as YearMonth
    val selectedDate = args[1] as String?
    val nav = args[2] as AppNavDestination
    val filter = args[3] as DateFilter
    val banner = args[4] as InquiryEntity?
    val clearDialog = args[5] as Boolean
    val shareDialog = args[6] as Boolean
    val shareKey = args[7] as String?
    val detailSheet = args[8] as Boolean
    val detailKey = args[9] as String?
    val confirmInquiry = args[10] as InquiryEntity?
    val multiMode = args[11] as Boolean
    val multiKeys = args[12] as Set<String>
    val bulkDialog = args[13] as Boolean
    val bulkType = args[14] as BulkActionType?
    val customShare = args[15] as String?
    val datesList = args[16] as List<StudioDateEntity>
    val inqList = args[17] as List<InquiryEntity>
    val prof = (args[18] as StudioProfileEntity?) ?: StudioProfileEntity()

    val map = datesList.associateBy { it.dateKey }

    CalendarUiState(
      currentYearMonth = ym,
      selectedDateKey = selectedDate,
      datesMap = map,
      inquiries = inqList,
      profile = prof,
      currentNav = nav,
      activeFilter = filter,
      bannerNotification = banner,
      showClearDialog = clearDialog,
      showShareDialog = shareDialog,
      shareDialogDateKey = shareKey,
      showDateDetailSheet = detailSheet,
      dateDetailSheetKey = detailKey,
      inquiryToConfirm = confirmInquiry,
      isMultiSelectMode = multiMode,
      selectedMultiDateKeys = multiKeys,
      showBulkActionDialog = bulkDialog,
      bulkActionType = bulkType,
      customShareMessage = customShare
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = CalendarUiState()
  )

  fun navigateTo(destination: AppNavDestination) {
    _currentNav.value = destination
  }

  fun setFilter(filter: DateFilter) {
    _activeFilter.value = filter
  }

  fun prevMonth() {
    _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
  }

  fun nextMonth() {
    _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
  }

  fun jumpToToday() {
    val today = LocalDate.now()
    _currentYearMonth.value = YearMonth.of(today.year, today.month)
    selectDate(today.toString())
  }

  fun selectDate(dateKey: String) {
    if (_isMultiSelectMode.value) {
      toggleDateSelection(dateKey)
    } else {
      _selectedDateKey.value = dateKey
      _dateDetailSheetKey.value = dateKey
      _showDateDetailSheet.value = true
    }
  }

  fun dismissDateDetailSheet() {
    _showDateDetailSheet.value = false
    _dateDetailSheetKey.value = null
  }

  fun openShareDialog(dateKey: String) {
    _shareDialogDateKey.value = dateKey
    _customShareMessage.value = null
    _showShareDialog.value = true
  }

  fun dismissShareDialog() {
    _showShareDialog.value = false
    _shareDialogDateKey.value = null
    _customShareMessage.value = null
  }

  fun showClearDialog(show: Boolean) {
    _showClearDialog.value = show
  }

  fun dismissBanner() {
    _bannerNotification.value = null
  }

  fun showConfirmInquiryDialog(inquiry: InquiryEntity) {
    _inquiryToConfirm.value = inquiry
  }

  fun dismissConfirmInquiryDialog() {
    _inquiryToConfirm.value = null
  }

  fun showIncomingInquiryNotification(inquiry: InquiryEntity) {
    _bannerNotification.value = inquiry
  }

  // --- MULTI-DATE SELECTION FEATURES ---

  fun toggleSelectionMode(enabled: Boolean? = null) {
    val nextMode = enabled ?: !_isMultiSelectMode.value
    _isMultiSelectMode.value = nextMode
    if (!nextMode) {
      _selectedMultiDateKeys.value = emptySet()
    }
  }

  fun toggleMultiSelectMode() {
    toggleSelectionMode()
  }

  fun enableMultiSelectWithDate(dateKey: String) {
    _isMultiSelectMode.value = true
    _selectedMultiDateKeys.value = setOf(dateKey)
  }

  fun toggleDateSelection(dateKey: String) {
    val current = _selectedMultiDateKeys.value.toMutableSet()
    if (current.contains(dateKey)) {
      current.remove(dateKey)
    } else {
      current.add(dateKey)
    }
    _selectedMultiDateKeys.value = current
  }

  fun selectAllCurrentMonthDates() {
    val ym = _currentYearMonth.value
    val allDays = (1..ym.lengthOfMonth()).map {
      String.format("%04d-%02d-%02d", ym.year, ym.monthValue, it)
    }
    _selectedMultiDateKeys.value = allDays.toSet()
  }

  fun selectAllCurrentMonthFreeDates() {
    val ym = _currentYearMonth.value
    val prefix = String.format("%04d-%02d", ym.year, ym.monthValue)
    val freeDays = uiState.value.datesMap.values
      .filter { it.dateKey.startsWith(prefix) && it.dateStatus == DateStatus.FREE }
      .map { it.dateKey }
    _selectedMultiDateKeys.value = freeDays.toSet()
  }

  fun clearMultiSelection() {
    _selectedMultiDateKeys.value = emptySet()
  }

  fun exitMultiSelectMode() {
    _isMultiSelectMode.value = false
    _selectedMultiDateKeys.value = emptySet()
  }

  // Bulk actions on multiple selected dates
  fun markSelectedDatesAsFree(note: String = "") {
    val dates = _selectedMultiDateKeys.value.toList()
    if (dates.isEmpty()) return
    viewModelScope.launch {
      repository.markMultipleDatesFree(dates, note)
      exitMultiSelectMode()
    }
  }

  fun openBulkBookDialog() {
    _bulkActionType.value = BulkActionType.BOOKED
    _showBulkActionDialog.value = true
  }

  fun openBulkInquiryDialog() {
    _bulkActionType.value = BulkActionType.INQUIRY
    _showBulkActionDialog.value = true
  }

  fun dismissBulkActionDialog() {
    _showBulkActionDialog.value = false
    _bulkActionType.value = null
  }

  fun executeBulkBooking(
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    advance: Double,
    total: Double,
    note: String
  ) {
    val dates = _selectedMultiDateKeys.value.toList()
    if (dates.isEmpty()) return
    viewModelScope.launch {
      repository.markMultipleDatesBooked(
        dateKeys = dates,
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        advanceAmount = advance,
        totalPackageAmount = total,
        note = note
      )
      _showBulkActionDialog.value = false
      exitMultiSelectMode()
    }
  }

  fun executeBulkInquiry(
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    note: String
  ) {
    val dates = _selectedMultiDateKeys.value.toList()
    if (dates.isEmpty()) return
    viewModelScope.launch {
      repository.markMultipleDatesInquiry(
        dateKeys = dates,
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        note = note
      )
      _showBulkActionDialog.value = false
      exitMultiSelectMode()
    }
  }

  fun resetSelectedDates() {
    val dates = _selectedMultiDateKeys.value.toList()
    if (dates.isEmpty()) return
    viewModelScope.launch {
      repository.resetMultipleDates(dates)
      exitMultiSelectMode()
    }
  }

  fun openMultiDateShareDialog() {
    val dates = _selectedMultiDateKeys.value.toList()
    if (dates.isEmpty()) return
    val msg = repository.formatMultiDateShareMessage(dates, uiState.value.profile)
    _customShareMessage.value = msg
    _shareDialogDateKey.value = dates.firstOrNull()
    _showShareDialog.value = true
  }

  // --- SINGLE DATE STATUS UPDATES ---

  fun setDateStatusFree(dateKey: String, note: String = "") {
    viewModelScope.launch {
      repository.markDateFree(dateKey, note)
    }
  }

  fun setDateStatusBooked(
    dateKey: String,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    advance: Double,
    total: Double,
    note: String
  ) {
    viewModelScope.launch {
      repository.markDateBooked(
        dateKey = dateKey,
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        advanceAmount = advance,
        totalPackageAmount = total,
        note = note
      )
    }
  }

  fun setDateStatusInquiry(
    dateKey: String,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    note: String
  ) {
    viewModelScope.launch {
      repository.markDateInquiry(
        dateKey = dateKey,
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        note = note
      )
    }
  }

  fun resetDate(dateKey: String) {
    viewModelScope.launch {
      repository.resetDate(dateKey)
    }
  }

  var syncManager: com.example.data.remote.FirestoreSyncManager? = null

  // Inquiry Flow: Admin clicks "Done" -> Date becomes Red (Booked)
  fun confirmInquiryDone(inquiry: InquiryEntity, advanceAmount: Double = 0.0, totalAmount: Double = 0.0) {
    viewModelScope.launch {
      repository.confirmInquiry(
        inquiryId = inquiry.id,
        dateKey = inquiry.dateKey,
        advanceAmount = advanceAmount,
        totalAmount = totalAmount
      )
      syncManager?.markInquiryConfirmed(inquiry.clientPhone, inquiry.dateKey)
      _inquiryToConfirm.value = null
      if (_bannerNotification.value?.id == inquiry.id) {
        _bannerNotification.value = null
      }
    }
  }

  fun declineInquiry(inquiry: InquiryEntity, makeFree: Boolean) {
    viewModelScope.launch {
      repository.declineInquiry(inquiry.id, inquiry.dateKey, makeFree)
      syncManager?.markInquiryDeclined(inquiry.clientPhone, inquiry.dateKey)
      if (_bannerNotification.value?.id == inquiry.id) {
        _bannerNotification.value = null
      }
    }
  }

  fun deleteInquiry(inquiry: InquiryEntity) {
    viewModelScope.launch {
      repository.declineInquiry(inquiry.id, inquiry.dateKey, makeFree = true)
      syncManager?.markInquiryDeclined(inquiry.clientPhone, inquiry.dateKey)
      if (_bannerNotification.value?.id == inquiry.id) {
        _bannerNotification.value = null
      }
    }
  }

  fun deleteOrCancelBooking(dateKeys: List<String>) {
    viewModelScope.launch {
      repository.deleteBookingDates(dateKeys)
    }
  }

  fun markInquiryRead(inquiryId: Long) {
    viewModelScope.launch {
      repository.markInquiryAsRead(inquiryId)
    }
  }

  // Client Portal Form Submission (Single date)
  fun submitClientBookingInquiry(
    dateKey: String,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    notes: String,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      val id = repository.submitClientInquiry(
        dateKey = dateKey,
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        notes = notes
      )
      val createdInquiry = InquiryEntity(
        id = id,
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
      _bannerNotification.value = createdInquiry
      onSuccess()
    }
  }

  // Client Portal Multi-Date Submission
  fun submitClientMultiDateBookingInquiry(
    dateKeys: List<String>,
    clientName: String,
    clientPhone: String,
    eventType: String,
    location: String,
    notes: String,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      val id = repository.submitClientMultiDateInquiry(
        dateKeys = dateKeys,
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        notes = notes
      )
      val createdInquiry = InquiryEntity(
        id = id,
        dateKey = dateKeys.joinToString(", "),
        clientName = clientName,
        clientPhone = clientPhone,
        eventType = eventType,
        location = location,
        notes = notes,
        isConfirmed = false,
        isRead = false,
        createdAt = System.currentTimeMillis()
      )
      _bannerNotification.value = createdInquiry
      onSuccess()
    }
  }

  // Clear Calendar Actions
  fun clearCalendar(clearType: ClearType) {
    viewModelScope.launch {
      val prefix = String.format("%04d-%02d", _currentYearMonth.value.year, _currentYearMonth.value.monthValue)
      repository.clearCalendar(clearType, currentMonthPrefix = prefix)
      _showClearDialog.value = false
    }
  }

  // Studio Profile Settings
  fun updateProfile(profile: StudioProfileEntity) {
    viewModelScope.launch {
      repository.saveProfile(profile)
    }
  }

  fun toggleLanguage() {
    viewModelScope.launch {
      val current = uiState.value.profile
      val nextLang = if (current.selectedLanguage == "HI" || current.selectedLanguage == "GU") "EN" else "HI"
      repository.saveProfile(current.copy(selectedLanguage = nextLang))
    }
  }

  fun getShareMessage(dateKey: String, clientName: String = ""): String {
    if (uiState.value.customShareMessage != null) {
      return uiState.value.customShareMessage!!
    }
    return repository.formatShareMessage(
      dateKey = dateKey,
      clientName = clientName,
      profile = uiState.value.profile
    )
  }

  fun getBookingLink(dateKey: String): String {
    val selected = _selectedMultiDateKeys.value.toList()
    return if (selected.size > 1 && selected.contains(dateKey)) {
      repository.generateBookingLink(selected, uiState.value.profile.studioName)
    } else {
      repository.generateBookingLink(dateKey, uiState.value.profile.studioName)
    }
  }

  fun getWhatsAppShareMessageForSelectedDates(): String {
    val dates = _selectedMultiDateKeys.value.toList()
    return repository.formatMultiDateShareMessage(dates, uiState.value.profile)
  }

  fun getBookingLinkForSelectedDates(): String {
    val dates = _selectedMultiDateKeys.value.toList()
    return repository.generateBookingLink(dates, uiState.value.profile.studioName)
  }
}

class StudioViewModelFactory(private val repository: StudioRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(StudioViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return StudioViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
