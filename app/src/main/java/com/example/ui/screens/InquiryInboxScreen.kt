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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.InquiryEntity
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.CalendarUiState
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InquiryInboxScreen(
  uiState: CalendarUiState,
  viewModel: StudioViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isHindi = uiState.profile.selectedLanguage == "HI" || uiState.profile.selectedLanguage == "GU"
  val isGujarati = isHindi

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabTitles = if (isHindi) {
    listOf("सभी (${uiState.inquiries.size})", "पेंडिंग 🟡", "पुष्ट / Done 🔴")
  } else {
    listOf("All (${uiState.inquiries.size})", "Pending 🟡", "Confirmed 🔴")
  }

  val filteredInquiries = remember(uiState.inquiries, selectedTab) {
    when (selectedTab) {
      1 -> uiState.inquiries.filter { !it.isConfirmed }
      2 -> uiState.inquiries.filter { it.isConfirmed }
      else -> uiState.inquiries
    }
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
          .background(Color(0xFFFEF3C7)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.NotificationsActive,
          contentDescription = null,
          tint = Color(0xFFD97706),
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isHindi) "पूछताछ इनबॉक्स (Inquiries)" else "Client Inquiries Inbox",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (isHindi) "लिंक द्वारा प्राप्त नई पूछताछ और बुकिंग" else "Real-time client requests from online booking link",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Filter Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      edgePadding = 0.dp,
      containerColor = Color.Transparent,
      divider = {}
    ) {
      tabTitles.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              fontSize = 12.sp,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Inquiries List
    if (filteredInquiries.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(52.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isHindi) "फिलहाल कोई पूछताछ नहीं है" else "No inquiries in this tab",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (isHindi)
              "क्लाइंट पोर्टल से पूछताछ भेजें या लिंक शेयर करें"
            else
              "Submit from client portal or share booking links",
            fontSize = 11.sp,
            color = Color.Gray
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("inquiries_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredInquiries, key = { it.id }) { inquiry ->
          InquiryCard(
            inquiry = inquiry,
            isHindi = isHindi,
            onDoneClick = { viewModel.showConfirmInquiryDialog(inquiry) },
            onDeclineClick = { viewModel.declineInquiry(inquiry, makeFree = true) },
            onViewOnCalendar = {
              viewModel.selectDate(inquiry.dateKey)
              viewModel.navigateTo(AppNavDestination.CALENDAR)
            }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
  }
}

@Composable
private fun InquiryCard(
  inquiry: InquiryEntity,
  isHindi: Boolean = false,
  isGujarati: Boolean = isHindi,
  onDoneClick: () -> Unit,
  onDeclineClick: () -> Unit,
  onViewOnCalendar: () -> Unit
) {
  val context = LocalContext.current
  val timeFormatted = remember(inquiry.createdAt) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    sdf.format(Date(inquiry.createdAt))
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("inquiry_item_${inquiry.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (inquiry.isConfirmed) Color(0xFFFEE2E2).copy(alpha = 0.5f) else Color(0xFFFFFBEB)
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp,
      color = if (inquiry.isConfirmed) StatusRed.copy(alpha = 0.4f) else StatusYellow.copy(alpha = 0.6f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      // Top status row: Date + Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0F172A))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "📅 ${inquiry.dateKey}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (inquiry.eventType.isNotBlank()) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFFFEF3C7))
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Text(
              text = inquiry.eventType,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF78350F)
            )
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Status Badge
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(if (inquiry.isConfirmed) StatusRed else StatusYellow)
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = if (inquiry.isConfirmed) "🔴 Booked" else "🟡 Pending",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (inquiry.isConfirmed) Color.White else Color(0xFF78350F)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Client Name & Phone
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = inquiry.clientName,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
          text = timeFormatted,
          fontSize = 10.sp,
          color = Color.Gray
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Phone + Quick Call & WhatsApp Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "📞 ${inquiry.clientPhone}",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF334155),
          modifier = Modifier.weight(1f)
        )

        // Call button
        OutlinedButton(
          onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${inquiry.clientPhone}"))
            try { context.startActivity(intent) } catch (e: Exception) {
              Toast.makeText(context, "Dialer unavailable", Toast.LENGTH_SHORT).show()
            }
          },
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.height(28.dp).padding(horizontal = 2.dp).testTag("inquiry_call_btn")
        ) {
          Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(2.dp))
          Text(text = if (isHindi) "कॉल" else "Call", fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.width(4.dp))

        // WhatsApp Chat button
        Button(
          onClick = {
            val cleanPhone = inquiry.clientPhone.replace("[^0-9]".toRegex(), "")
            val url = "https://api.whatsapp.com/send?phone=$cleanPhone"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try { context.startActivity(intent) } catch (e: Exception) {
              Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.height(28.dp).testTag("inquiry_whatsapp_btn")
        ) {
          Text(text = "WhatsApp", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
      }

      // Location & Notes if any
      if (inquiry.location.isNotBlank() || inquiry.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.8f))
            .padding(6.dp)
        ) {
          Column {
            if (inquiry.location.isNotBlank()) {
              Text(
                text = "📍 ${inquiry.location}",
                fontSize = 11.sp,
                color = Color(0xFF475569)
              )
            }
            if (inquiry.notes.isNotBlank()) {
              Text(
                text = "💬 ${inquiry.notes}",
                fontSize = 11.sp,
                color = Color(0xFF334155)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action Row: "Done" (Confirm Booking -> Turns Red), "View Calendar", "Reject"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onViewOnCalendar,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f).height(34.dp).testTag("view_calendar_btn")
        ) {
          Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(if (isHindi) "कैलेंडर" else "Calendar", fontSize = 11.sp)
        }

        if (!inquiry.isConfirmed) {
          // Reject button
          OutlinedButton(
            onClick = onDeclineClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
            modifier = Modifier.height(34.dp).testTag("decline_inquiry_btn")
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(13.dp))
          }

          // "Done" Confirm Booking Button (Requirement 3: "Done dabavtaj date confirm Red thai jashe")
          Button(
            onClick = onDoneClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1.5f).height(34.dp).testTag("inquiry_done_confirm_btn")
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isHindi) "Done (कन्फर्म करें 🔴)" else "Done (Confirm 🔴)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}
