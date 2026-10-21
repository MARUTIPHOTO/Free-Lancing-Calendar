package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudioDateEntity
import com.example.data.model.DateStatus
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
          text = if (isHindi) "कन्फ़र्म बुकिंग्स (Bookings 🔴)" else "Confirmed Bookings List 🔴",
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
            text = if (isHindi) "ક્લાયન્ટ્સ / દિવસો" else "Clients / Days",
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
            text = if (isHindi) "એડવાન્સ જમા" else "Advance Recv",
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
            text = if (isHindi) "કુલ બિઝનેસ" else "Total Business",
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
      label = { Text(if (isHindi) "ક્લાયન્ટ નામ, ઇવેન્ટ કે તારીખ શોધો..." else "Search client, event, or date...") },
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
            text = if (isHindi) "કોઈ કન્ફર્મ બુકિંગ મળ્યું નથી" else "No confirmed bookings found",
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
            }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
  }

  // 1. Delete ENTIRE Client Booking Confirmation Dialog
  bookingToDelete?.let { booking ->
    val clientLabel = booking.clientName.ifBlank { if (isGujarati) "આ ક્લાયન્ટ" else "this client" }
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupedBookedCard(
  group: GroupedClientBooking,
  isGujarati: Boolean,
  onDateChipClick: (String) -> Unit,
  onDeleteDateClick: (StudioDateEntity) -> Unit,
  onDeleteBookingClick: () -> Unit
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
            text = group.clientName.ifBlank { if (isGujarati) "ક્લાયન્ટ" else "Client" },
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

      // 2. Date Chips Section: [ 24-Dec-2026 ] [ 25-Dec-2026 ] ... (કુલ X દિવસ)
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
            text = if (isGujarati) "📅 તારીખો:" else "📅 Dates:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF991B1B)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isGujarati) "(કુલ ${group.dates.size} દિવસ)" else "(Total ${group.dates.size} Days)",
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
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Extra shoot details if present
      if (group.bookingSourceStudio.isNotBlank()) {
        Text(
          text = "🏢 ${if (isGujarati) "હાયર કર્તા / સ્ટુડિયો:" else "Hired By Studio:"} ${group.bookingSourceStudio}",
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF1E293B)
        )
      }

      if (group.gearRequired.isNotBlank()) {
        Text(
          text = "🎥 ${if (isGujarati) "કેમેરા / ગિયર:" else "Gear:"} ${group.gearRequired}",
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
              "એડવાન્સ: ₹${group.totalAdvance.toInt()} • બાકી: ₹${if (balance > 0) balance.toInt() else 0}"
            else
              "Advance: ₹${group.totalAdvance.toInt()} • Bal: ₹${if (balance > 0) balance.toInt() else 0}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF15803D)
          )
          if (group.totalPackageAmount > 0) {
            Text(
              text = if (isGujarati) "કુલ પેકેજ: ₹${group.totalPackageAmount.toInt()}" else "Total Package: ₹${group.totalPackageAmount.toInt()}",
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
            Text(text = if (isGujarati) "કોલ" else "Call", fontSize = 10.sp)
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
    }
  }
}
