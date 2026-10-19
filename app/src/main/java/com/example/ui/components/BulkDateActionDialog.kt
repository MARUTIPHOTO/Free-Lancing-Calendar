package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.BulkActionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkDateActionDialog(
  selectedDates: List<String>,
  bulkType: BulkActionType,
  isGujarati: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (clientName: String, clientPhone: String, eventType: String, location: String, advance: Double, total: Double, note: String) -> Unit
) {
  var clientName by remember { mutableStateOf("") }
  var clientPhone by remember { mutableStateOf("") }
  var eventType by remember { mutableStateOf(if (isGujarati) "विवाह / शादी (Wedding Ceremony)" else "Wedding Ceremony") }
  var location by remember { mutableStateOf("") }
  var advanceInput by remember { mutableStateOf("") }
  var totalInput by remember { mutableStateOf("") }
  var noteInput by remember { mutableStateOf("") }

  var isEventTypeExpanded by remember { mutableStateOf(false) }
  val eventOptions = if (isGujarati) StringsHelper.eventTypesHi else StringsHelper.eventTypesEn

  val isBooking = bulkType == BulkActionType.BOOKED
  val themeColor = if (isBooking) StatusRed else StatusYellow

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("bulk_date_action_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isBooking) {
              if (isGujarati) "मल्टी तारीख बुकिंग (🔴 Booked)" else "Bulk Date Booking (🔴 Booked)"
            } else {
              if (isGujarati) "मल्टी तारीख पूछताछ (🟡 Inquiry)" else "Bulk Date Inquiry (🟡 Inquiry)"
            },
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = themeColor
          )
          Text(
            text = if (isGujarati) "${selectedDates.size} तारीखें एक साथ चुनी हैं" else "${selectedDates.size} dates selected simultaneously",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Selected dates pills
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEF3C7).copy(alpha = 0.6f))
            .padding(8.dp)
        ) {
          Column {
            Text(
              text = if (isGujarati) "चुनी हुई तारीखें:" else "Selected Dates:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF78350F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = selectedDates.sorted().joinToString(", "),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF92400E)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Client Name
        OutlinedTextField(
          value = clientName,
          onValueChange = { clientName = it },
          label = { Text(if (isGujarati) "क्लाइंट नाम *" else "Client Name *") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("bulk_client_name_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Client Phone
        OutlinedTextField(
          value = clientPhone,
          onValueChange = { clientPhone = it },
          label = { Text(if (isGujarati) "मोबाइल नंबर *" else "Mobile Number *") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("bulk_client_phone_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Event Type
        ExposedDropdownMenuBox(
          expanded = isEventTypeExpanded,
          onExpandedChange = { isEventTypeExpanded = !isEventTypeExpanded }
        ) {
          OutlinedTextField(
            value = eventType,
            onValueChange = { eventType = it },
            label = { Text(if (isGujarati) "इवेंट प्रकार" else "Event Type") },
            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isEventTypeExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("bulk_event_type_input")
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

        Spacer(modifier = Modifier.height(8.dp))

        // Venue Location
        OutlinedTextField(
          value = location,
          onValueChange = { location = it },
          label = { Text(if (isGujarati) "स्थान / शहर (Venue)" else "Venue / Location") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("bulk_venue_input")
        )

        if (isBooking) {
          Spacer(modifier = Modifier.height(8.dp))

          // Financial inputs
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = advanceInput,
              onValueChange = { advanceInput = it },
              label = { Text(if (isGujarati) "कुल एडवांस (₹)" else "Advance (₹)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bulk_advance_input")
            )

            OutlinedTextField(
              value = totalInput,
              onValueChange = { totalInput = it },
              label = { Text(if (isGujarati) "कुल पैकेज (₹)" else "Total (₹)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bulk_total_input")
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Notes
        OutlinedTextField(
          value = noteInput,
          onValueChange = { noteInput = it },
          label = { Text(if (isGujarati) "नोट / कैमरा टीम विवरण" else "Notes / Requirements") },
          modifier = Modifier.fillMaxWidth().testTag("bulk_note_input"),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val adv = advanceInput.toDoubleOrNull() ?: 0.0
          val tot = totalInput.toDoubleOrNull() ?: 0.0
          onConfirm(clientName, clientPhone, eventType, location, adv, tot, noteInput)
        },
        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("bulk_confirm_action_btn")
      ) {
        Text(
          text = if (isBooking) {
            if (isGujarati) "सभी तारीखें बुक करें (🔴 Done)" else "Book All Dates (🔴 Done)"
          } else {
            if (isGujarati) "पूछताछ सेव करें (🟡 Save)" else "Save Inquiry (🟡)"
          },
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("bulk_cancel_btn")
      ) {
        Text(if (isGujarati) "रद्द करें" else "Cancel")
      }
    }
  )
}
