package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.InquiryEntity
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.CalendarUiState
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InquiryInboxScreen(
  uiState: CalendarUiState,
  viewModel: StudioViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
  val isHindi = uiState.profile.selectedLanguage == "HI" || uiState.profile.selectedLanguage == "GU"

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabTitles = if (isHindi) {
    listOf("लंबित (Pending 🟡)", "स्वीकृत (Booked 🔴)", "सभी (All)")
  } else {
    listOf("Pending (🟡)", "Booked (🔴)", "All Inquiries")
  }

  // Delete & Unaccept state dialogs
  var inquiryToDelete by remember { mutableStateOf<InquiryEntity?>(null) }
  var inquiryToUnaccept by remember { mutableStateOf<InquiryEntity?>(null) }

  // Filter inquiries based on tab (Tab 0 is Pending, so once Accepted it moves out to Booked/Bookings tab!)
  val filteredInquiries = remember(uiState.inquiries, selectedTab) {
    when (selectedTab) {
      0 -> uiState.inquiries.filter { !it.isConfirmed }
      1 -> uiState.inquiries.filter { it.isConfirmed }
      else -> uiState.inquiries
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 14.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 10.dp, bottom = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = { viewModel.navigateTo(AppNavDestination.CALENDAR) },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
      }

      Spacer(modifier = Modifier.width(6.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isHindi) "पूछताछ इनबॉक्स (Inquiries)" else "Client Inquiries Inbox",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (isHindi) "ऑनलाइन बुकिंग पोर्टल से प्राप्त पूछताछ" else "Real-time client requests from booking portal",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Test Notification Button
      IconButton(
        onClick = {
          val testInquiry = InquiryEntity(
            id = System.currentTimeMillis(),
            dateKey = "2026-11-20",
            clientName = "Rajesh Patel (टेस्ट क्लाइंट)",
            clientPhone = "9876543210",
            eventType = "Wedding Shoot (शूटिंग)",
            location = "Ahmedabad (अहमदाबाद)",
            notes = "टेस्ट नोटिफिकेशन चेक (Test Notification)",
            isConfirmed = false,
            isRead = false,
            createdAt = System.currentTimeMillis()
          )
          com.example.util.NotificationHelper.showInquiryNotification(context, testInquiry)
          Toast.makeText(context, "🔔 नोटिफिकेशन भेजा गया! ऊपर स्टेटस बार चेक करें।", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.NotificationsActive, contentDescription = "Test Notification", tint = Color(0xFFD97706))
      }

      // Refresh Button
      IconButton(
        onClick = {
          Toast.makeText(context, if (isHindi) "पूछताछ जाँची जा रही है... 🔄" else "Checking for new inquiries... 🔄", Toast.LENGTH_SHORT).show()
          viewModel.syncManager?.let { sm ->
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
              sm.manualRefresh()
            }
          }
        },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = "Refresh Inquiries")
      }

      // Quick Language Toggle Button
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
            text = if (isHindi) "इस टैब में कोई पूछताछ नहीं है" else "No inquiries in this tab",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (isHindi)
              "क्लाइंट पोर्टल से नई पूछताछ सबमिट करें"
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
            onAcceptClick = { viewModel.showConfirmInquiryDialog(inquiry) },
            onUnacceptClick = { inquiryToUnaccept = inquiry },
            onDeleteClick = { inquiryToDelete = inquiry },
            onViewOnCalendar = {
              val firstDate = StringsHelper.parseDateKeys(inquiry.dateKey).firstOrNull() ?: inquiry.dateKey
              viewModel.selectDate(firstDate)
              viewModel.navigateTo(AppNavDestination.CALENDAR)
            }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
  }

  // Delete Inquiry Confirmation Dialog
  inquiryToDelete?.let { inquiry ->
    AlertDialog(
      onDismissRequest = { inquiryToDelete = null },
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
          text = if (isHindi) "पूछताछ डिलीट करनी है?" else "Delete Inquiry?",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      },
      text = {
        Text(
          text = if (isHindi)
            "क्या आप वाकई ${inquiry.clientName} की इस पूछताछ को डिलीट करना चाहते हैं?\n\nइसकी सभी तारीखें कैलेंडर में खाली (🟢 Free) रहेंगी।"
          else
            "Are you sure you want to delete inquiry for ${inquiry.clientName}?\n\nAll associated dates will remain Free (🟢 Green) in the calendar.",
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteInquiry(inquiry)
            Toast.makeText(
              context,
              if (isHindi) "पूछताछ डिलीट हो गई" else "Inquiry deleted",
              Toast.LENGTH_SHORT
            ).show()
            inquiryToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
          modifier = Modifier.testTag("confirm_delete_inquiry_btn")
        ) {
          Text(if (isHindi) "हाँ, डिलीट करें" else "Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { inquiryToDelete = null }) {
          Text(if (isHindi) "रद्द करें" else "Cancel")
        }
      }
    )
  }

  // Unaccept (Decline / Cancel) Confirmation Dialog
  inquiryToUnaccept?.let { inquiry ->
    val datesList = StringsHelper.parseDateKeys(inquiry.dateKey)
    val isAlreadyConfirmed = inquiry.isConfirmed

    AlertDialog(
      onDismissRequest = { inquiryToUnaccept = null },
      icon = {
        Icon(
          imageVector = if (isAlreadyConfirmed) Icons.AutoMirrored.Filled.Undo else Icons.Default.Close,
          contentDescription = null,
          tint = Color(0xFFDC2626),
          modifier = Modifier.size(36.dp)
        )
      },
      title = {
        Text(
          text = if (isAlreadyConfirmed) {
            if (isHindi) "बुकिंग रद्द / Unaccept करें?" else "Unaccept / Cancel Booking?"
          } else {
            if (isHindi) "पूछताछ अस्वीकार (Unaccept) करें?" else "Unaccept Inquiry?"
          },
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      },
      text = {
        Text(
          text = if (isAlreadyConfirmed) {
            if (isHindi)
              "क्या आप वाकई ${inquiry.clientName} की इस बुकिंग को Unaccept / रद्द करना चाहते हैं?\n\nकैलेंडर में सभी ${datesList.size} तारीखें वापस खाली (🟢 Free) हो जाएंगी।"
            else
              "Are you sure you want to unaccept / cancel booking for ${inquiry.clientName}?\n\nAll ${datesList.size} dates will be restored to Free (🟢 Green) in the calendar."
          } else {
            if (isHindi)
              "क्या आप वाकई ${inquiry.clientName} की इस पूछताछ को अस्वीकार (Unaccept) करना चाहते हैं?\n\nइसकी सभी तारीखें कैलेंडर में दोबारा खाली (🟢 Free) हो जाएंगी।"
            else
              "Are you sure you want to unaccept this inquiry from ${inquiry.clientName}?\n\nAll dates will return to Free (🟢 Green) in the calendar."
          },
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (isAlreadyConfirmed) {
              viewModel.deleteOrCancelBooking(datesList)
              viewModel.declineInquiry(inquiry, makeFree = true)
            } else {
              viewModel.declineInquiry(inquiry, makeFree = true)
            }
            Toast.makeText(
              context,
              if (isHindi) "तारीखें वापस Free (🟢) हो गईं" else "Dates restored to Free (🟢)",
              Toast.LENGTH_SHORT
            ).show()
            inquiryToUnaccept = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
          modifier = Modifier.testTag("confirm_unaccept_btn")
        ) {
          Text(
            text = if (isHindi) "हाँ, अस्वीकार (Unaccept) करें" else "Yes, Unaccept",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { inquiryToUnaccept = null }) {
          Text(if (isHindi) "नहीं / रद्द करें" else "Cancel")
        }
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InquiryCard(
  inquiry: InquiryEntity,
  isHindi: Boolean,
  onAcceptClick: () -> Unit,
  onUnacceptClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onViewOnCalendar: () -> Unit
) {
  val context = LocalContext.current
  val timeFormatted = remember(inquiry.createdAt) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    sdf.format(Date(inquiry.createdAt))
  }

  val datesList = remember(inquiry.dateKey) {
    StringsHelper.parseDateKeys(inquiry.dateKey)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("inquiry_card_${inquiry.id}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (inquiry.isConfirmed) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp,
      color = if (inquiry.isConfirmed) Color(0xFFFECACA) else Color(0xFFFDE68A)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      // Top header: Client Name + Status Badge + 🗑️ Delete Icon
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = inquiry.clientName.ifBlank { if (isHindi) "क्लाइंट" else "Client" },
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A),
          modifier = Modifier.weight(1f)
        )

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

        // Date Availability Status Badge (Booked Conflict vs Free)
        val isClash = inquiry.notes.contains("Date Already Booked") || inquiry.notes.contains("तारीख पहले से बुक")
        Spacer(modifier = Modifier.width(4.dp))
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(if (isClash) Color(0xFFFEE2E2) else Color(0xFFDCFCE7))
            .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Text(
            text = if (isClash) "⚠️ बुक है" else "🟢 तारीख खाली है",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isClash) Color(0xFF991B1B) else Color(0xFF166534)
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // 🗑️ Delete Button on Inquiry Card
        IconButton(
          onClick = onDeleteClick,
          modifier = Modifier
            .size(28.dp)
            .testTag("delete_inquiry_btn_${inquiry.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Inquiry",
            tint = Color(0xFFDC2626),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      if (inquiry.eventType.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "📸 ${inquiry.eventType}",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFB45309)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Multi-date chips in inquiry card!
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color.White.copy(alpha = 0.9f))
          .padding(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isHindi) "📅 तारीखें:" else "📅 Dates:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF92400E)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isHindi) "(कुल ${datesList.size} दिन)" else "(Total ${datesList.size} Days)",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFD97706)
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          datesList.forEach { dateKey ->
            val formatted = StringsHelper.formatDateChip(dateKey)
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFF0F172A)
            ) {
              Text(
                text = formatted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

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
        if (inquiry.clientPhone.isNotBlank()) {
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

      // Action Row: ACCEPT & UNACCEPT (DECLINE) OPTIONS
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // View on Calendar button
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
          // ❌ UNACCEPT / DECLINE BUTTON
          OutlinedButton(
            onClick = onUnacceptClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
            modifier = Modifier.weight(1.1f).height(34.dp).testTag("inquiry_unaccept_btn")
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (isHindi) "अस्वीकारें ❌" else "Unaccept ❌",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // ✅ ACCEPT / BOOK BUTTON
          Button(
            onClick = onAcceptClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1.3f).height(34.dp).testTag("inquiry_done_confirm_btn")
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (isHindi) "स्वीकारें 🔴" else "Accept 🔴",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        } else {
          // For ALREADY BOOKED inquiries: "Unaccept / Cancel Booking" Button
          OutlinedButton(
            onClick = onUnacceptClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
            modifier = Modifier.weight(1.3f).height(34.dp).testTag("inquiry_unaccept_booked_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (isHindi) "Unaccept / रद्द करें" else "Unaccept Booking",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}
