package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.CalendarToday
import com.example.data.local.StudioDateEntity
import com.example.data.model.DateStatus
import com.example.data.model.DateWorkAssignment
import com.example.ui.theme.StatusRed
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.CalendarUiState
import com.example.ui.viewmodel.StudioViewModel

data class GroupedClientBooking(
  val groupKey: String,
  val clientName: String,
  val clientPhone: String,
  val eventType: String,
  val location: String,
  val bookingSourceStudio: String,
  val gearRequired: String,
  val customNote: String,
  val dates: List<StudioDateEntity>,
  val totalAdvance: Double,
  val totalPackageAmount: Double
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpcomingBookingsScreen(
  uiState: CalendarUiState,
  viewModel: StudioViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isHindi = uiState.profile.selectedLanguage == "HI" || uiState.profile.selectedLanguage == "GU"
  val isGujarati = isHindi

  var searchQuery by remember { mutableStateOf("") }
  var bookingToDelete by remember { mutableStateOf<GroupedClientBooking?>(null) }
  var singleDateToDelete by remember { mutableStateOf<StudioDateEntity?>(null) }
  var bookingDetailToView by remember { mutableStateOf<GroupedClientBooking?>(null) }
  var bookingToEdit by remember { mutableStateOf<GroupedClientBooking?>(null) }

  // Group multiple dates for the same client into a single card
  val groupedBookings = remember(uiState.datesMap, searchQuery) {
    val bookedEntities = uiState.datesMap.values
      .filter { it.dateStatus == DateStatus.BOOKED }
      .sortedBy { it.dateKey }

    val groups = linkedMapOf<String, MutableList<StudioDateEntity>>()
    for (entity in bookedEntities) {
      val key = if (entity.clientName.isNotBlank() || entity.clientPhone.isNotBlank()) {
        "${entity.clientName.trim().lowercase()}__${entity.clientPhone.trim()}"
      } else {
        entity.dateKey
      }
      groups.getOrPut(key) { mutableListOf() }.add(entity)
    }

    groups.map { (key, dateList) ->
      val first = dateList.first()
      val sortedDates = dateList.sortedBy { it.dateKey }
      GroupedClientBooking(
        groupKey = key,
        clientName = first.clientName,
        clientPhone = first.clientPhone,
        eventType = first.eventType.ifBlank { dateList.firstOrNull { it.eventType.isNotBlank() }?.eventType ?: "" },
        location = first.location.ifBlank { dateList.firstOrNull { it.location.isNotBlank() }?.location ?: "" },
        bookingSourceStudio = first.bookingSourceStudio.ifBlank { dateList.firstOrNull { it.bookingSourceStudio.isNotBlank() }?.bookingSourceStudio ?: "" },
        gearRequired = first.gearRequired.ifBlank { dateList.firstOrNull { it.gearRequired.isNotBlank() }?.gearRequired ?: "" },
        customNote = first.customNote.ifBlank { dateList.firstOrNull { it.customNote.isNotBlank() }?.customNote ?: "" },
        dates = sortedDates,
        totalAdvance = sortedDates.sumOf { it.advanceAmount },
        totalPackageAmount = sortedDates.sumOf { it.totalPackageAmount }
      )
    }.filter { group ->
      if (searchQuery.isBlank()) true
      else {
        group.clientName.contains(searchQuery, ignoreCase = true) ||
          group.eventType.contains(searchQuery, ignoreCase = true) ||
          group.clientPhone.contains(searchQuery, ignoreCase = true) ||
          group.dates.any { it.dateKey.contains(searchQuery, ignoreCase = true) }
      }
    }
  }

  val totalBookedDays = remember(uiState.datesMap) {
    uiState.datesMap.values.count { it.dateStatus == DateStatus.BOOKED }
  }
  val totalAdvance = remember(uiState.datesMap) {
    uiState.datesMap.values.filter { it.dateStatus == DateStatus.BOOKED }.sumOf { it.advanceAmount }
  }
  val totalPackages = remember(uiState.datesMap) {
    uiState.datesMap.values.filter { it.dateStatus == DateStatus.BOOKED }.sumOf { it.totalPackageAmount }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 14.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp, bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFFFEE2E2)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.EventNote,
          contentDescription = null,
          tint = StatusRed,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isHindi) "कन्फर्म बुकिंग्स (Bookings 🔴)" else "Confirmed Bookings List 🔴",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (isHindi) "सभी क्लाइंट्स और तारीखों की बुकिंग" else "All clients & multi-date photography schedules",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Quick Language Switcher Button
      OutlinedButton(
        onClick = { viewModel.toggleLanguage() },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(30.dp)
      ) {
        Text(
          text = if (isHindi) "EN" else "हिन्दी",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Statistics Overview Card
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      color = Color(0xFF0F172A)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "${groupedBookings.size} (${totalBookedDays}d)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF87171)
          )
          Text(
            text = if (isHindi) "क्लाइंट्स / दिन" else "Clients / Days",
            fontSize = 10.sp,
            color = Color(0xFFCBD5E1)
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "₹${totalAdvance.toInt()}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4ADE80)
          )
          Text(
            text = if (isHindi) "एडवांस जमा" else "Advance Recv",
            fontSize = 10.sp,
            color = Color(0xFFCBD5E1)
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "₹${totalPackages.toInt()}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFBBF24)
          )
          Text(
            text = if (isHindi) "कुल व्यापार" else "Total Business",
            fontSize = 10.sp,
            color = Color(0xFFCBD5E1)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = { Text(if (isHindi) "क्लाइंट नाम, इवेंट या तारीख खोजें..." else "Search client, event, or date...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("search_bookings_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Bookings List (Single card per client with multi-dates chips!)
    if (groupedBookings.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (isHindi) "कोई कन्फर्म बुकिंग नहीं मिली" else "No confirmed bookings found",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("booked_events_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(groupedBookings, key = { it.groupKey }) { group ->
          GroupedBookedCard(
            group = group,
            isGujarati = isGujarati,
            onDateChipClick = { dateKey ->
              viewModel.selectDate(dateKey)
            },
            onDeleteDateClick = { dateEntity ->
              singleDateToDelete = dateEntity
            },
            onDeleteBookingClick = {
              bookingToDelete = group
            },
            onEditBookingClick = {
              bookingToEdit = group
            },
            onViewDetailsClick = {
              bookingDetailToView = group
            }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
  }

  // 1. Delete ENTIRE Client Booking Confirmation Dialog
  bookingToDelete?.let { booking ->
    val clientLabel = booking.clientName.ifBlank { if (isGujarati) "यह क्लाइंट" else "this client" }
    AlertDialog(
      onDismissRequest = { bookingToDelete = null },
      icon = {
        Icon(
          Icons.Default.Delete,
          contentDescription = null,
          tint = Color(0xFFDC2626),
          modifier = Modifier.size(36.dp)
        )
      },
      title = {
        Text(
          text = if (isHindi) "बुकिंग रद्द / Unaccept करें?" else "Unaccept / Cancel Booking?",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      },
      text = {
        Column {
          Text(
            text = if (isHindi)
              "क्या आप वाकई $clientLabel की यह बुकिंग Unaccept / रद्द करना चाहते हैं?\n\nइस बुकिंग की सभी (${booking.dates.size}) तारीखें कैलेंडर में दोबारा खाली (🟢 Free) हो जाएंगी।"
            else
              "Are you sure you want to unaccept / cancel this booking for $clientLabel?\n\nAll (${booking.dates.size}) dates will turn back to Free (🟢 Green) in the calendar.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteOrCancelBooking(booking.dates.map { it.dateKey })
            Toast.makeText(
              context,
              if (isHindi) "बुकिंग रद्द हो गई और तारीखें Free (🟢) हो गईं" else "Booking cancelled & dates set to Free (🟢)",
              Toast.LENGTH_SHORT
            ).show()
            bookingToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
          modifier = Modifier.testTag("confirm_delete_booking_btn")
        ) {
          Text(if (isHindi) "हाँ, रद्द करें" else "Yes, Cancel", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { bookingToDelete = null },
          modifier = Modifier.testTag("dismiss_delete_booking_btn")
        ) {
          Text(if (isHindi) "नहीं / रद्द करें" else "Dismiss")
        }
      }
    )
  }

  // 2. Delete SINGLE Date from multi-date booking Confirmation Dialog
  singleDateToDelete?.let { dateEntity ->
    val formattedDate = StringsHelper.formatDateChip(dateEntity.dateKey)
    AlertDialog(
      onDismissRequest = { singleDateToDelete = null },
      icon = {
        Icon(
          Icons.Default.Delete,
          contentDescription = null,
          tint = Color(0xFFDC2626),
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text(
          text = if (isHindi) "तारीख $formattedDate रद्द करें?" else "Cancel Date $formattedDate?",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      },
      text = {
        Text(
          text = if (isHindi)
            "क्या आप सिर्फ इस एक तारीख [$formattedDate] की बुकिंग रद्द करना चाहते हैं? यह तारीख कैलेंडर में दोबारा खाली (🟢 Free) हो जाएगी।"
          else
            "Do you want to cancel booking for this specific date [$formattedDate]? It will turn Free (🟢 Green) in the calendar.",
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteOrCancelBooking(listOf(dateEntity.dateKey))
            Toast.makeText(
              context,
              if (isHindi) "तारीख $formattedDate Free (🟢) हो गई" else "Date $formattedDate is now Free (🟢)",
              Toast.LENGTH_SHORT
            ).show()
            singleDateToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
          modifier = Modifier.testTag("confirm_single_date_delete_btn")
        ) {
          Text(if (isHindi) "हाँ, रद्द करें" else "Yes, Cancel", color = Color.White)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { singleDateToDelete = null }) {
          Text(if (isHindi) "नहीं" else "No")
        }
      }
    )
  }

  // 3. Full Booking Detail Dialog (विवरण देखें 👁️)
  bookingDetailToView?.let { booking ->
    BookingDetailDialog(
      booking = booking,
      isGujarati = isGujarati,
      onDismiss = { bookingDetailToView = null },
      onEditClick = {
        bookingToEdit = booking
      }
    )
  }

  // 4. Edit Booking Dialog (विवरण संपादित करें ✏️)
  bookingToEdit?.let { booking ->
    EditBookingDialog(
      booking = booking,
      isGujarati = isGujarati,
      onDismiss = { bookingToEdit = null },
      onSave = { totalPkg, advance, assignments, notes ->
        viewModel.updateBookingDateAssignments(
          assignments = assignments,
          totalPackageAmount = totalPkg,
          advanceAmount = advance,
          commonNotes = notes
        )
        Toast.makeText(
          context,
          if (isGujarati) "बुकिंग विवरण और तारीखें सफलतापूर्वक अपडेट हो गईं 💾" else "Booking details & dates updated successfully 💾",
          Toast.LENGTH_SHORT
        ).show()
        bookingToEdit = null
        if (bookingDetailToView?.groupKey == booking.groupKey) {
          bookingDetailToView = null
        }
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupedBookedCard(
  group: GroupedClientBooking,
  isGujarati: Boolean,
  onDateChipClick: (String) -> Unit,
  onDeleteDateClick: (StudioDateEntity) -> Unit,
  onDeleteBookingClick: () -> Unit,
  onEditBookingClick: () -> Unit,
  onViewDetailsClick: () -> Unit
) {
  val context = LocalContext.current
  val balance = group.totalPackageAmount - group.totalAdvance

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("client_card_${group.groupKey}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.35f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // 1. Client Name & Event Badge Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFFEE2E2)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Person, contentDescription = null, tint = StatusRed, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = group.clientName.ifBlank { if (isGujarati) "क्लाइंट" else "Client" },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (group.eventType.isNotBlank()) {
            Text(
              text = "📸 ${group.eventType}",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFD97706)
            )
          }
        }

        // Edit Booking Button (✏️)
        IconButton(
          onClick = onEditBookingClick,
          modifier = Modifier
            .size(32.dp)
            .testTag("edit_booking_btn_${group.groupKey}")
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Booking",
            tint = Color(0xFFD97706),
            modifier = Modifier.size(19.dp)
          )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Delete Booking Button (🗑️)
        IconButton(
          onClick = onDeleteBookingClick,
          modifier = Modifier
            .size(32.dp)
            .testTag("delete_booking_btn_${group.groupKey}")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Booking",
            tint = Color(0xFFDC2626),
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Date Chips Section: [ 24-Dec-2026 ] [ 25-Dec-2026 ] ... (कुल X दिन)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFFFEF2F2))
          .padding(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isGujarati) "📅 तारीखें:" else "📅 Dates:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF991B1B)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isGujarati) "(कुल ${group.dates.size} दिन)" else "(Total ${group.dates.size} Days)",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFDC2626)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          group.dates.forEach { dateEntity ->
            val formatted = StringsHelper.formatDateChip(dateEntity.dateKey)
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFDC2626),
              modifier = Modifier
                .clickable { onDateChipClick(dateEntity.dateKey) }
                .testTag("date_chip_${dateEntity.dateKey}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = formatted,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                if (group.dates.size > 1) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel single date",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                      .size(12.dp)
                      .clickable { onDeleteDateClick(dateEntity) }
                  )
                }
              }
            }
          }
        }

        // Date-wise work breakdown list (e.g. • 12-Nov: Traditional Video)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          group.dates.forEach { dateEntity ->
            val formatted = StringsHelper.formatDateChip(dateEntity.dateKey)
            val work = dateEntity.eventType.ifBlank { group.eventType.ifBlank { if (isGujarati) "फ़ोटोग्राफ़ी / वीडियो" else "Shoot" } }
            val loc = if (dateEntity.location.isNotBlank()) " • 📍 ${dateEntity.location}" else ""
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.95f))
                .border(0.5.dp, Color(0xFFFECACA), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "• $formatted: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF991B1B)
              )
              Text(
                text = "$work$loc",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Extra shoot details if present
      if (group.bookingSourceStudio.isNotBlank()) {
        Text(
          text = "🏢 ${if (isGujarati) "हायर कर्ता / स्टूडियो:" else "Hired By Studio:"} ${group.bookingSourceStudio}",
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF1E293B)
        )
      }

      if (group.gearRequired.isNotBlank()) {
        Text(
          text = "🎥 ${if (isGujarati) "कैमरा / गियर:" else "Gear:"} ${group.gearRequired}",
          fontSize = 11.sp,
          color = Color(0xFF475569)
        )
      }

      if (group.location.isNotBlank()) {
        Text(
          text = "📍 ${group.location}",
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Financial breakdown & Action buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isGujarati)
              "एडवांस: ₹${group.totalAdvance.toInt()} • शेष: ₹${if (balance > 0) balance.toInt() else 0}"
            else
              "Advance: ₹${group.totalAdvance.toInt()} • Bal: ₹${if (balance > 0) balance.toInt() else 0}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF15803D)
          )
          if (group.totalPackageAmount > 0) {
            Text(
              text = if (isGujarati) "कुल पैकेज: ₹${group.totalPackageAmount.toInt()}" else "Total Package: ₹${group.totalPackageAmount.toInt()}",
              fontSize = 10.sp,
              color = Color(0xFF64748B)
            )
          }
        }

        // Contact buttons
        if (group.clientPhone.isNotBlank()) {
          OutlinedButton(
            onClick = {
              val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${group.clientPhone}"))
              try { context.startActivity(intent) } catch (e: Exception) {}
            },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = if (isGujarati) "कॉल" else "Call", fontSize = 10.sp)
          }

          Spacer(modifier = Modifier.width(6.dp))

          OutlinedButton(
            onClick = {
              val clean = group.clientPhone.replace("[^0-9]".toRegex(), "")
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$clean"))
              try { context.startActivity(intent) } catch (e: Exception) {}
            },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text(text = "WhatsApp", fontSize = 10.sp, color = Color(0xFF25D366), fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Action Buttons: View Details & Edit Details
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onViewDetailsClick,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("view_details_btn_${group.groupKey}")
        ) {
          Icon(
            imageVector = Icons.Default.EventNote,
            contentDescription = null,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isGujarati) "विवरण देखें 👁️" else "View Details 👁️",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
          )
        }

        Button(
          onClick = onEditBookingClick,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("edit_details_btn_${group.groupKey}")
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isGujarati) "विवरण संपादित करें ✏️" else "Edit Details ✏️",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingDetailDialog(
  booking: GroupedClientBooking,
  isGujarati: Boolean,
  onDismiss: () -> Unit,
  onEditClick: () -> Unit
) {
  val context = LocalContext.current
  val balance = (booking.totalPackageAmount - booking.totalAdvance).coerceAtLeast(0.0)

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("booking_detail_dialog"),
    icon = {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(Color(0xFFDC2626)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.EventNote,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
    },
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
          text = booking.clientName.ifBlank { if (isGujarati) "क्लाइंट बुकिंग" else "Client Booking" },
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (booking.clientPhone.isNotBlank()) {
          Text(
            text = "📞 ${booking.clientPhone}",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // 1. Financial Package Card
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF8FAFC),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isGujarati) "💰 कुल पैकेज राशि:" else "💰 Total Package:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
              Text(
                text = "₹${booking.totalPackageAmount.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF047857)
              )
            }
            androidx.compose.material3.HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isGujarati) "🟢 एडवांस प्राप्त:" else "🟢 Advance Paid:",
                fontSize = 12.sp,
                color = Color(0xFF475569)
              )
              Text(
                text = "₹${booking.totalAdvance.toInt()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF15803D)
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isGujarati) "⏳ शेष राशि (Balance):" else "⏳ Remaining Balance:",
                fontSize = 12.sp,
                color = Color(0xFF475569)
              )
              Text(
                text = "₹${balance.toInt()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (balance > 0) Color(0xFFB91C1C) else Color(0xFF047857)
              )
            }
          }
        }

        // 2. Day-wise Services List
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFEF2F2),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = if (isGujarati) "📅 किस तारीख को क्या काम करना है:" else "📅 Day-wise Shoot Services:",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF991B1B)
            )

            booking.dates.forEach { dateEntity ->
              val formattedDate = StringsHelper.formatDateChip(dateEntity.dateKey)
              val noteLines = booking.customNote.lines()
              val serviceTitle = if (dateEntity.eventType.isNotBlank()) {
                dateEntity.eventType + if (dateEntity.location.isNotBlank()) " (📍 ${dateEntity.location})" else ""
              } else {
                val matchingNoteLine = noteLines.find { it.contains(dateEntity.dateKey) }
                if (!matchingNoteLine.isNullOrBlank()) {
                  matchingNoteLine.replace("•", "").trim()
                } else {
                  booking.eventType.ifBlank { "📸 Shoot" }
                }
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.White)
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color(0xFFDC2626)
                ) {
                  Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = serviceTitle,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF1E293B),
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }

        // 3. Location / Venue
        if (booking.location.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${if (isGujarati) "स्थान / वेन्यू: " else "Location: "}${booking.location}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF334155)
              )
            }
          }
        }

        // 4. Notes (excluding availability tag)
        val cleanNotes = booking.customNote.lines()
          .filter { !it.contains("तारीख") && !it.contains("बुक") && !it.contains("खाली") && !it.startsWith("•") }
          .joinToString("\n").trim()
        if (cleanNotes.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFFBEB),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = if (isGujarati) "📝 क्लाइंट विशेष नोट:" else "📝 Special Notes:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = cleanNotes,
                fontSize = 12.sp,
                color = Color(0xFF78350F)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Prominent Full-Width Edit Details Button
        Button(
          onClick = onEditClick,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("edit_booking_prominent_dialog_btn")
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isGujarati) "यह बुकिंग विवरण संपादित करें ✏️" else "Edit Booking Details ✏️",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    },
    confirmButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Edit Details Button (संपादित करें ✏️)
          Button(
            onClick = onEditClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("edit_booking_from_detail_btn")
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(if (isGujarati) "संपादित करें ✏️" else "Edit ✏️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          if (booking.clientPhone.isNotBlank()) {
            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.clientPhone}"))
                try { context.startActivity(intent) } catch (_: Exception) {}
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(if (isGujarati) "कॉल" else "Call", fontSize = 11.sp)
            }

            Button(
              onClick = {
                val clean = booking.clientPhone.replace("[^0-9]".toRegex(), "")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$clean"))
                try { context.startActivity(intent) } catch (_: Exception) {}
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(34.dp)
        ) {
          Text(if (isGujarati) "बंद करें" else "Close", fontSize = 11.sp)
        }
      }
    }
  )
}

class EditableBookingDate(
  val originalKey: String,
  initialKey: String,
  initialWorkType: String,
  initialLocation: String
) {
  var currentDateKey by mutableStateOf(initialKey)
  var currentWorkType by mutableStateOf(initialWorkType)
  var currentLocation by mutableStateOf(initialLocation)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditBookingDialog(
  booking: GroupedClientBooking,
  isGujarati: Boolean,
  onDismiss: () -> Unit,
  onSave: (totalPkg: Double, advance: Double, assignments: List<DateWorkAssignment>, notes: String) -> Unit
) {
  val context = LocalContext.current

  var totalAmountText by remember {
    mutableStateOf(if (booking.totalPackageAmount > 0) booking.totalPackageAmount.toInt().toString() else "")
  }
  var advanceAmountText by remember {
    mutableStateOf(if (booking.totalAdvance > 0) booking.totalAdvance.toInt().toString() else "")
  }
  var customNote by remember { mutableStateOf(booking.customNote) }

  // Date-wise editable state items
  val editableDates = remember(booking) {
    booking.dates.map { d ->
      EditableBookingDate(
        originalKey = d.dateKey,
        initialKey = d.dateKey,
        initialWorkType = d.eventType.ifBlank { booking.eventType },
        initialLocation = d.location.ifBlank { booking.location }
      )
    }
  }

  val total = totalAmountText.toDoubleOrNull() ?: 0.0
  val advance = advanceAmountText.toDoubleOrNull() ?: 0.0
  val balance = (total - advance).coerceAtLeast(0.0)

  val quickServices = listOf(
    "Traditional Video",
    "Traditional Photo",
    "Candid Photography",
    "Cinematic Video",
    "Drone Shoot",
    "Pre-Wedding",
    "Haldi / Mehendi",
    "Reception"
  )

  fun showDatePicker(item: EditableBookingDate) {
    val parts = item.currentDateKey.split("-")
    val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val m = (parts.getOrNull(1)?.toIntOrNull() ?: 11) - 1
    val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
    android.app.DatePickerDialog(
      context,
      { _, selectedYear, selectedMonth, selectedDay ->
        val newKey = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
        item.currentDateKey = newKey
      },
      y,
      m,
      d
    ).show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("edit_booking_dialog"),
    icon = {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(Color(0xFFD97706)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
    },
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (isGujarati) "बुकिंग विवरण और तारीख बदलें ✏️" else "Edit Booking & Dates ✏️",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = booking.clientName.ifBlank { if (isGujarati) "क्लाइंट" else "Client" } +
            if (booking.clientPhone.isNotBlank()) " (${booking.clientPhone})" else "",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // 1. Total Package Amount
        OutlinedTextField(
          value = totalAmountText,
          onValueChange = { totalAmountText = it.filter { ch -> ch.isDigit() } },
          label = { Text(if (isGujarati) "कुल पैकेज राशि (₹)" else "Total Package (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("edit_total_package_input")
        )

        // 2. Advance Amount
        OutlinedTextField(
          value = advanceAmountText,
          onValueChange = { advanceAmountText = it.filter { ch -> ch.isDigit() } },
          label = { Text(if (isGujarati) "एडवांस जमा राशि (₹)" else "Advance Paid (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("edit_advance_input")
        )

        // Live Balance Indicator
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (balance > 0) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (balance > 0) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isGujarati) "शेष राशि (Balance):" else "Remaining Balance:",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (balance > 0) Color(0xFF991B1B) else Color(0xFF166534)
            )
            Text(
              text = "₹${balance.toInt()}",
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (balance > 0) Color(0xFFDC2626) else Color(0xFF15803D)
            )
          }
        }

        // Section Divider Header: Date & Work Assignment
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = if (isGujarati) "📅 तारीख और काम का विवरण (Date & Work):" else "📅 Date & Work Breakdown:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF991B1B)
          )
          Text(
            text = if (isGujarati) "तारीख बदलने या प्रत्येक तारीख का काम सेट करने के लिए नीचे देखें:" else "Assign work per date or change booking dates below:",
            fontSize = 10.sp,
            color = Color(0xFF64748B)
          )
        }

        // Each date card
        editableDates.forEachIndexed { index, dateItem ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFFBEB),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Date Row with Change Date button
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "📅 ${StringsHelper.formatDateChip(dateItem.currentDateKey)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF92400E)
                  )
                  if (dateItem.currentDateKey != dateItem.originalKey) {
                    Text(
                      text = if (isGujarati) "🔄 नई तारीख (पुरानी: ${dateItem.originalKey})" else "🔄 Changed from ${dateItem.originalKey}",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFB45309)
                    )
                  }
                }

                Button(
                  onClick = { showDatePicker(dateItem) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  modifier = Modifier.height(32.dp).testTag("change_date_btn_${dateItem.originalKey}")
                ) {
                  Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (isGujarati) "तारीख बदलें" else "Change Date",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Work / Service Type Header
              Text(
                text = if (isGujarati) "🎥 इस तारीख को क्या काम करना है? (Work):" else "🎥 Service / Work for this date:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78350F)
              )

              // Quick Service Chips
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                quickServices.forEach { service ->
                  val isSelected = dateItem.currentWorkType.contains(service, ignoreCase = true)
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) Color(0xFFD97706) else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                      0.5.dp,
                      if (isSelected) Color(0xFFB45309) else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.clickable {
                      dateItem.currentWorkType = service
                    }
                  ) {
                    Text(
                      text = service,
                      fontSize = 10.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) Color.White else Color(0xFF334155),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                  }
                }
              }

              // Work Type Text Field
              OutlinedTextField(
                value = dateItem.currentWorkType,
                onValueChange = { dateItem.currentWorkType = it },
                label = { Text(if (isGujarati) "काम का विवरण (जैसे Traditional Video)" else "Service (e.g. Traditional Video)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("edit_date_work_${dateItem.originalKey}")
              )

              // Location / Venue for this Date
              OutlinedTextField(
                value = dateItem.currentLocation,
                onValueChange = { dateItem.currentLocation = it },
                label = { Text(if (isGujarati) "स्थान / वेन्यू (Location / Venue)" else "Location / Venue") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("edit_date_loc_${dateItem.originalKey}")
              )
            }
          }
        }

        // 4. Notes / Service Breakdown
        OutlinedTextField(
          value = customNote,
          onValueChange = { customNote = it },
          label = { Text(if (isGujarati) "विशेष नोट / सामान्य विवरण" else "Notes / Overall Details") },
          minLines = 2,
          maxLines = 5,
          modifier = Modifier.fillMaxWidth().testTag("edit_notes_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val assignments = editableDates.map {
            DateWorkAssignment(
              originalDateKey = it.originalKey,
              newDateKey = it.currentDateKey,
              eventType = it.currentWorkType.trim(),
              location = it.currentLocation.trim()
            )
          }
          onSave(total, advance, assignments, customNote.trim())
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("save_edit_booking_btn")
      ) {
        Text(if (isGujarati) "सेव करें 💾" else "Save Changes 💾", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_edit_booking_btn")
      ) {
        Text(if (isGujarati) "रद्द करें" else "Cancel")
      }
    }
  )
}

