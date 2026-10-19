package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.data.model.DateStatus
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.CalendarUiState
import com.example.ui.viewmodel.StudioViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClientPortalScreen(
  uiState: CalendarUiState,
  viewModel: StudioViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isHindi = uiState.profile.selectedLanguage == "HI" || uiState.profile.selectedLanguage == "GU"
  val isGujarati = isHindi

  // Find all Free (🟢) dates
  val availableFreeDates = remember(uiState.datesMap) {
    uiState.datesMap.values
      .filter { it.dateStatus == DateStatus.FREE }
      .sortedBy { it.dateKey }
  }

  // Multi-date selection set for client
  val initialDate = availableFreeDates.firstOrNull()?.dateKey ?: LocalDate.now().plusDays(5).toString()
  var selectedDatesSet by remember { mutableStateOf(setOf(initialDate)) }
  var manualDateInput by remember { mutableStateOf("") }

  var clientName by remember { mutableStateOf("") }
  var clientPhone by remember { mutableStateOf("") }
  var eventType by remember { mutableStateOf(if (isHindi) "विवाह / शादी (Wedding Ceremony)" else "Wedding Ceremony") }
  var venueLocation by remember { mutableStateOf("") }
  var requirementsNotes by remember { mutableStateOf("") }

  var isEventTypeExpanded by remember { mutableStateOf(false) }
  val eventOptions = if (isHindi) StringsHelper.eventTypesHi else StringsHelper.eventTypesEn

  var showSuccessDialog by remember { mutableStateOf(false) }
  var submittedClientName by remember { mutableStateOf("") }
  var submittedDatesCount by remember { mutableStateOf(1) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
      .verticalScroll(rememberScrollState())
  ) {
    // 1. Studio Branding Header Card
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF0F172A),
      shadowElevation = 4.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { viewModel.navigateTo(AppNavDestination.CALENDAR) },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
          }

          Spacer(modifier = Modifier.width(6.dp))

          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color(0xFFF59E0B)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = null,
              tint = Color(0xFF0F172A),
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = uiState.profile.studioName,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "📞 ${uiState.profile.phone} • ${uiState.profile.address}",
              fontSize = 11.sp,
              color = Color(0xFFCBD5E1)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .padding(8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isHindi)
              "🌟 क्लाइंट बुकिंग पोर्टल (एक या अधिक तारीखें चुनकर फॉर्म भरें)"
            else
              "🌟 Client Booking Portal (Select One or Multiple Dates)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFDE68A)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Form Container
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Step 1: Multi-Date Selection for Client
        Text(
          text = if (isHindi)
            "१. तारीख चुनें (एक से अधिक तारीखें भी चुन सकते हैं):"
          else
            "1. Select Dates (Select One or Multiple Dates):",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (availableFreeDates.isNotEmpty()) {
          Text(
            text = if (isHindi)
              "🟢 उपलब्ध खाली तारीखों पर क्लिक करके जोड़ें/हटाएं:"
            else
              "🟢 Click open dates to select/unselect:",
            fontSize = 12.sp,
            color = Color(0xFF15803D),
            fontWeight = FontWeight.SemiBold
          )

          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(availableFreeDates, key = { it.dateKey }) { freeDate ->
              val isSelected = selectedDatesSet.contains(freeDate.dateKey)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) Color(0xFF16A34A) else StatusGreenContainer)
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFF14532D) else Color(0xFF86EFAC),
                    shape = RoundedCornerShape(10.dp)
                  )
                  .clickable {
                    val current = selectedDatesSet.toMutableSet()
                    if (current.contains(freeDate.dateKey)) {
                      if (current.size > 1) current.remove(freeDate.dateKey)
                    } else {
                      current.add(freeDate.dateKey)
                    }
                    selectedDatesSet = current
                  }
                  .padding(horizontal = 12.dp, vertical = 8.dp)
                  .testTag("portal_free_date_${freeDate.dateKey}"),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                  }
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                      text = freeDate.dateKey,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color.White else Color(0xFF14532D)
                    )
                    Text(
                      text = if (freeDate.customNote.isNotBlank()) freeDate.customNote else if (isHindi) "उपलब्ध" else "Available",
                      fontSize = 9.sp,
                      color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF166534)
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
        }

        // Selected Dates Chips Display
        Text(
          text = if (isHindi)
            "चुनी हुई ${selectedDatesSet.size} तारीखें:"
          else
            "Selected ${selectedDatesSet.size} Date(s):",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          selectedDatesSet.sorted().forEach { dateKey ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFEF3C7))
                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "📅 $dateKey",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF78350F)
                )
                if (selectedDatesSet.size > 1) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color(0xFF78350F),
                    modifier = Modifier
                      .size(14.dp)
                      .clickable {
                        selectedDatesSet = selectedDatesSet - dateKey
                      }
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Manual date addition row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = manualDateInput,
            onValueChange = { manualDateInput = it },
            placeholder = { Text(if (isHindi) "अन्य तारीख जोड़ें (YYYY-MM-DD)" else "Add another date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("manual_date_add_input")
          )
          Spacer(modifier = Modifier.width(8.dp))
          OutlinedButton(
            onClick = {
              if (manualDateInput.isNotBlank()) {
                selectedDatesSet = selectedDatesSet + manualDateInput.trim()
                manualDateInput = ""
              }
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("add_custom_date_btn")
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(2.dp))
            Text(if (isHindi) "जोड़ें" else "Add")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Contact Details
        Text(
          text = if (isHindi) "२. आपका विवरण (Your Contact Details):" else "2. Your Contact Details:",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Full Name
        OutlinedTextField(
          value = clientName,
          onValueChange = { clientName = it },
          label = { Text(if (isHindi) "पूरा नाम *" else "Full Name *") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("portal_client_name")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Phone Number
        OutlinedTextField(
          value = clientPhone,
          onValueChange = { clientPhone = it },
          label = { Text(if (isHindi) "मोबाइल नंबर (WhatsApp) *" else "Mobile Number (WhatsApp) *") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("portal_client_phone")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Event Type
        ExposedDropdownMenuBox(
          expanded = isEventTypeExpanded,
          onExpandedChange = { isEventTypeExpanded = !isEventTypeExpanded }
        ) {
          OutlinedTextField(
            value = eventType,
            onValueChange = { eventType = it },
            label = { Text(if (isHindi) "इवेंट प्रकार (Event Type)" else "Event / Function Type") },
            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isEventTypeExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("portal_event_type")
          )
          ExposedDropdownMenu(
            expanded = isEventTypeExpanded,
            onDismissRequest = { isEventTypeExpanded = false }
          ) {
            eventOptions.forEach { option ->
              DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                  eventType = option
                  isEventTypeExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Venue Location
        OutlinedTextField(
          value = venueLocation,
          onValueChange = { venueLocation = it },
          label = { Text(if (isHindi) "स्थान / शहर (Venue / City)" else "Venue / City") },
          leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("portal_venue_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Requirements / Notes
        OutlinedTextField(
          value = requirementsNotes,
          onValueChange = { requirementsNotes = it },
          label = { Text(if (isHindi) "कैमरा / फोटोग्राफी आवश्यकताएं (Optional Notes)" else "Photography Requirements / Notes") },
          leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth().testTag("portal_notes_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button (Submits multi-date inquiry!)
        Button(
          onClick = {
            if (selectedDatesSet.isEmpty()) {
              Toast.makeText(context, if (isHindi) "कृपया कम से कम एक तारीख चुनें" else "Please select at least one date", Toast.LENGTH_SHORT).show()
              return@Button
            }
            if (clientName.isBlank()) {
              Toast.makeText(context, if (isHindi) "कृपया अपना नाम लिखें" else "Please enter your name", Toast.LENGTH_SHORT).show()
              return@Button
            }
            if (clientPhone.isBlank()) {
              Toast.makeText(context, if (isHindi) "कृपया मोबाइल नंबर लिखें" else "Please enter your mobile number", Toast.LENGTH_SHORT).show()
              return@Button
            }

            submittedClientName = clientName
            submittedDatesCount = selectedDatesSet.size
            viewModel.submitClientMultiDateBookingInquiry(
              dateKeys = selectedDatesSet.toList(),
              clientName = clientName,
              clientPhone = clientPhone,
              eventType = eventType,
              location = venueLocation,
              notes = requirementsNotes,
              onSuccess = {
                showSuccessDialog = true
                clientName = ""
                clientPhone = ""
                venueLocation = ""
                requirementsNotes = ""
              }
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("submit_booking_inquiry_button")
        ) {
          Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isHindi)
              "बुकिंग विवरण सबमिट करें (${selectedDatesSet.size} तारीखें 🚀)"
            else
              "Submit Booking Request (${selectedDatesSet.size} Dates 🚀)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }

  // Success Confirmation Dialog
  if (showSuccessDialog) {
    AlertDialog(
      onDismissRequest = { showSuccessDialog = false },
      icon = {
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color(0xFF16A34A),
          modifier = Modifier.size(48.dp)
        )
      },
      title = {
        Text(
          text = if (isHindi) "पूछताछ सफलतापूर्वक भेजी गई! 🎉" else "Inquiry Submitted Successfully! 🎉",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      },
      text = {
        Column {
          Text(
            text = if (isHindi)
              "धन्यवाद $submittedClientName! आपकी चुनी गई $submittedDatesCount तारीखों की पूछताछ स्टूडियो को प्राप्त हो गई है। सभी तारीखें पीली (🟡 Inquiry) हो गई हैं और ऐप में रियल-टाइम नोटिफिकेशन आ गया है।"
            else
              "Thank you $submittedClientName! Your inquiry for $submittedDatesCount date(s) has been dispatched to Studio Admin. All selected dates have turned Yellow (🟡 Inquiry) and real-time notification generated in admin inbox.",
            fontSize = 13.sp
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showSuccessDialog = false
            viewModel.navigateTo(AppNavDestination.NOTIFICATIONS)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
          modifier = Modifier.testTag("go_to_inbox_btn")
        ) {
          Text(if (isHindi) "नोटिफिकेशन इनबॉक्स देखें" else "View in Admin Inbox", color = Color.White)
        }
      },
      dismissButton = {
        Button(
          onClick = {
            showSuccessDialog = false
            viewModel.navigateTo(AppNavDestination.CALENDAR)
          }
        ) {
          Text(if (isHindi) "कैलेंडर पर जाएं" else "View Calendar")
        }
      }
    )
  }
}
