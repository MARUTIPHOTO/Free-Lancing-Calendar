package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.CalendarUiState
import com.example.ui.viewmodel.StudioViewModel

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

  val bookedList = remember(uiState.datesMap, searchQuery) {
    uiState.datesMap.values
      .filter { it.dateStatus == DateStatus.BOOKED }
      .filter {
        if (searchQuery.isBlank()) true
        else it.clientName.contains(searchQuery, ignoreCase = true) ||
          it.eventType.contains(searchQuery, ignoreCase = true) ||
          it.dateKey.contains(searchQuery, ignoreCase = true)
      }
      .sortedBy { it.dateKey }
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
          text = if (isHindi) "कन्फर्म बुकिंग लिस्ट 🔴" else "Confirmed Bookings List 🔴",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (isHindi) "सभी शूटिंग और इवेंट ऑर्डर्स" else "All photography shoots & wedding schedules",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
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
            text = "${bookedList.size}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF87171)
          )
          Text(
            text = if (isHindi) "कुल बुकिंग (🔴)" else "Total Shoots",
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
            text = if (isHindi) "एडवांस प्राप्त" else "Advance Recv",
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
            text = if (isHindi) "कुल बिजनेस" else "Total Business",
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
      label = { Text(if (isHindi) "क्लाइंट नाम या तारीख खोजें..." else "Search client, event, or date...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("search_bookings_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Bookings List
    if (bookedList.isEmpty()) {
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(bookedList, key = { it.dateKey }) { item ->
          BookedEventCard(
            item = item,
            isGujarati = isGujarati,
            onClick = {
              viewModel.selectDate(item.dateKey)
            }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
  }
}

@Composable
private fun BookedEventCard(
  item: StudioDateEntity,
  isGujarati: Boolean,
  onClick: () -> Unit
) {
  val context = LocalContext.current
  val balance = item.totalPackageAmount - item.advanceAmount

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("booked_item_${item.dateKey}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.3f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFDC2626))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "📅 ${item.dateKey}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = item.clientName.ifBlank { if (isGujarati) "क्लाइंट" else "Client" },
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
          Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Details")
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      if (item.eventType.isNotBlank()) {
        Text(
          text = "📸 ${item.eventType}",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFD97706)
        )
      }

      if (item.bookingSourceStudio.isNotBlank()) {
        Text(
          text = "🏢 ${if (isGujarati) "हायर कर्ता:" else "Hired By:"} ${item.bookingSourceStudio}",
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF1E293B)
        )
      }

      if (item.gearRequired.isNotBlank()) {
        Text(
          text = "🎥 ${if (isGujarati) "गियर:" else "Gear:"} ${item.gearRequired}",
          fontSize = 11.sp,
          color = Color(0xFF475569)
        )
      }

      if (item.location.isNotBlank()) {
        Text(
          text = "📍 ${item.location}",
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Financial status & quick contact
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isGujarati)
              "एडवांस: ₹${item.advanceAmount.toInt()} • शेष: ₹${if (balance > 0) balance.toInt() else 0}"
            else
              "Advance: ₹${item.advanceAmount.toInt()} • Bal: ₹${if (balance > 0) balance.toInt() else 0}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF15803D)
          )
        }

        if (item.clientPhone.isNotBlank()) {
          OutlinedButton(
            onClick = {
              val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.clientPhone}"))
              try { context.startActivity(intent) } catch (e: Exception) {}
            },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = if (isGujarati) "कॉल" else "Call", fontSize = 10.sp)
          }

          Spacer(modifier = Modifier.width(4.dp))

          OutlinedButton(
            onClick = {
              val clean = item.clientPhone.replace("[^0-9]".toRegex(), "")
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$clean"))
              try { context.startActivity(intent) } catch (e: Exception) {}
            },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text(text = "WhatsApp", fontSize = 10.sp, color = Color(0xFF25D366), fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
