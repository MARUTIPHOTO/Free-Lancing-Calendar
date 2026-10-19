package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.local.StudioDateEntity
import com.example.data.local.StudioProfileEntity
import com.example.data.model.DateStatus
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedContainer
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.StatusYellowContainer
import com.example.ui.util.StringsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDetailDialog(
  dateKey: String,
  studioDate: StudioDateEntity?,
  profile: StudioProfileEntity,
  isHindi: Boolean = false,
  isGujarati: Boolean = isHindi,
  onDismiss: () -> Unit,
  onSave: (StudioDateEntity) -> Unit,
  onReset: (String) -> Unit,
  onShareToWhatsApp: (String) -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var selectedStatus by remember(studioDate) {
    mutableStateOf(studioDate?.dateStatus ?: DateStatus.FREE)
  }
  var clientName by remember(studioDate) { mutableStateOf(studioDate?.clientName ?: "") }
  var clientPhone by remember(studioDate) { mutableStateOf(studioDate?.clientPhone ?: "") }
  var eventType by remember(studioDate) { mutableStateOf(studioDate?.eventType ?: "") }
  var location by remember(studioDate) { mutableStateOf(studioDate?.location ?: "") }
  var customNote by remember(studioDate) { mutableStateOf(studioDate?.customNote ?: "") }
  var advanceAmount by remember(studioDate) {
    mutableStateOf(if ((studioDate?.advanceAmount ?: 0.0) > 0) studioDate!!.advanceAmount.toString() else "")
  }
  var totalAmount by remember(studioDate) {
    mutableStateOf(if ((studioDate?.totalPackageAmount ?: 0.0) > 0) studioDate!!.totalPackageAmount.toString() else "")
  }
  var bookingSourceStudio by remember(studioDate) { mutableStateOf(studioDate?.bookingSourceStudio ?: "") }
  var shiftType by remember(studioDate) { mutableStateOf(studioDate?.shiftType ?: "Full Day (पूरा दिन)") }
  var gearRequired by remember(studioDate) { mutableStateOf(studioDate?.gearRequired ?: "") }

  var isEventTypeExpanded by remember { mutableStateOf(false) }
  val eventOptions = if (isHindi) StringsHelper.eventTypesHi else StringsHelper.eventTypesEn

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("date_detail_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header: Date & Close
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "📅 $dateKey",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isGujarati) "तारीख स्टेटस और विवरण" else "Date Status & Booking Details",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 1. Color Status Selector: Green (Free) / Yellow (Inquiry) / Red (Booked)
      Text(
        text = if (isGujarati) "तारीख का स्टेटस चुनें:" else "Select Date Status:",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // 🟢 Free Status
        StatusOptionCard(
          title = if (isGujarati) "🟢 खाली" else "🟢 Free",
          subtitle = if (isGujarati) "उपलब्ध" else "Available",
          color = StatusGreen,
          containerColor = StatusGreenContainer,
          isSelected = selectedStatus == DateStatus.FREE,
          testTag = "status_select_free",
          onClick = { selectedStatus = DateStatus.FREE },
          modifier = Modifier.weight(1f)
        )

        // 🟡 Inquiry Status
        StatusOptionCard(
          title = if (isGujarati) "🟡 पूछताछ" else "🟡 Inquiry",
          subtitle = if (isGujarati) "पेंडिंग" else "Pending",
          color = StatusYellow,
          containerColor = StatusYellowContainer,
          isSelected = selectedStatus == DateStatus.INQUIRY,
          testTag = "status_select_inquiry",
          onClick = { selectedStatus = DateStatus.INQUIRY },
          modifier = Modifier.weight(1f)
        )

        // 🔴 Booked Status
        StatusOptionCard(
          title = if (isGujarati) "🔴 बुकिंग" else "🔴 Booked",
          subtitle = if (isGujarati) "कन्फर्म" else "Confirmed",
          color = StatusRed,
          containerColor = StatusRedContainer,
          isSelected = selectedStatus == DateStatus.BOOKED,
          testTag = "status_select_booked",
          onClick = { selectedStatus = DateStatus.BOOKED },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // If status is FREE, provide instant share & booking link card
      if (selectedStatus == DateStatus.FREE) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = StatusGreenContainer.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = if (isGujarati) "✨ यह तारीख खाली (Free) है!" else "✨ This Date is Open!",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF14532D)
            )
            Text(
              text = if (isGujarati)
                "आप क्लाइंट को इस तारीख की बुकिंग लिंक WhatsApp पर शेयर कर सकते हैं।"
              else
                "Share the direct booking link with your clients on WhatsApp.",
              fontSize = 12.sp,
              color = Color(0xFF166534)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
              onClick = { onShareToWhatsApp(dateKey) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("share_whatsapp_free_btn")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isGujarati) "WhatsApp पर लिंक शेयर करें" else "Share Booking Link on WhatsApp",
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // 2. Client Details (shown when INQUIRY or BOOKED, or optional note for FREE)
      if (selectedStatus == DateStatus.INQUIRY || selectedStatus == DateStatus.BOOKED) {
        Text(
          text = if (isHindi) "क्लाइंट और इवेंट विवरण:" else "Client & Event Details:",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Client Name
        OutlinedTextField(
          value = clientName,
          onValueChange = { clientName = it },
          label = { Text(if (isHindi) "क्लाइंट का नाम" else "Client Name") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("client_name_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Client Phone
        OutlinedTextField(
          value = clientPhone,
          onValueChange = { clientPhone = it },
          label = { Text(if (isHindi) "मोबाइल नंबर" else "Mobile Number") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("client_phone_input")
        )

        if (clientPhone.isNotBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clientPhone"))
                try { context.startActivity(intent) } catch (e: Exception) {
                  Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
                }
              },
              modifier = Modifier.weight(1f).testTag("call_client_button")
            ) {
              Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isHindi) "कॉल करें" else "Call")
            }

            Button(
              onClick = {
                val cleanPhone = clientPhone.replace("[^0-9]".toRegex(), "")
                val url = "https://api.whatsapp.com/send?phone=$cleanPhone"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try { context.startActivity(intent) } catch (e: Exception) {
                  Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
              modifier = Modifier.weight(1f).testTag("chat_whatsapp_button")
            ) {
              Text(if (isHindi) "WhatsApp चैट" else "WhatsApp", color = Color.White)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Event Type Dropdown
        ExposedDropdownMenuBox(
          expanded = isEventTypeExpanded,
          onExpandedChange = { isEventTypeExpanded = !isEventTypeExpanded }
        ) {
          OutlinedTextField(
            value = eventType,
            onValueChange = { eventType = it },
            label = { Text(if (isHindi) "इवेंट / फंक्शन का प्रकार" else "Event / Function Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isEventTypeExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("event_type_input")
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

        // Location / Venue
        OutlinedTextField(
          value = location,
          onValueChange = { location = it },
          label = { Text(if (isHindi) "स्थान / वेन्यू / हॉल (Venue)" else "Venue / Location") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("venue_location_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Freelance specific: Hired By Studio / Client
        OutlinedTextField(
          value = bookingSourceStudio,
          onValueChange = { bookingSourceStudio = it },
          label = { Text(if (isHindi) "हायर करने वाला स्टूडियो / पार्टी" else "Hired by Studio / Agency / Client") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("hired_by_studio_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Freelance specific: Gear to carry
        OutlinedTextField(
          value = gearRequired,
          onValueChange = { gearRequired = it },
          label = { Text(if (isHindi) "कैमरा गियर (Sony / Gimbal / Drone)" else "Gear to Carry (Camera/Gimbal/Drone)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("gear_required_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Advance & Total Payment (for Booked / Inquiry)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = advanceAmount,
            onValueChange = { advanceAmount = it },
            label = { Text(if (isHindi) "टोकन / एडवांस (₹)" else "Advance Token (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("advance_amount_input")
          )

          OutlinedTextField(
            value = totalAmount,
            onValueChange = { totalAmount = it },
            label = { Text(if (isHindi) "कुल पैकेज / शिफ्ट रेट (₹)" else "Total / Shift Rate (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("total_amount_input")
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Custom Note
      OutlinedTextField(
        value = customNote,
        onValueChange = { customNote = it },
        label = { Text(if (isHindi) "नोट्स / टीम आवश्यकताएं" else "Notes / Requirements") },
        modifier = Modifier.fillMaxWidth().testTag("notes_input"),
        maxLines = 3
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Action Buttons: Save & Reset
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (studioDate != null) {
          OutlinedButton(
            onClick = {
              onReset(dateKey)
              onDismiss()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
            modifier = Modifier.weight(1f).testTag("reset_date_button")
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isHindi) "रीसेट करें" else "Reset")
          }
        }

        Button(
          onClick = {
            val adv = advanceAmount.toDoubleOrNull() ?: 0.0
            val tot = totalAmount.toDoubleOrNull() ?: 0.0
            val entity = StudioDateEntity(
              dateKey = dateKey,
              status = selectedStatus.name,
              clientName = clientName,
              clientPhone = clientPhone,
              eventType = eventType,
              location = location,
              customNote = customNote,
              advanceAmount = adv,
              totalPackageAmount = tot,
              bookingSourceStudio = bookingSourceStudio,
              shiftType = shiftType,
              gearRequired = gearRequired,
              updatedAt = System.currentTimeMillis()
            )
            onSave(entity)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.weight(1.5f).testTag("save_date_details_button")
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isHindi) "सेव करें (Save)" else "Save Changes",
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun StatusOptionCard(
  title: String,
  subtitle: String,
  color: Color,
  containerColor: Color,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (isSelected) containerColor else containerColor.copy(alpha = 0.4f))
      .border(
        width = if (isSelected) 2.dp else 0.5.dp,
        color = if (isSelected) color else color.copy(alpha = 0.3f),
        shape = RoundedCornerShape(10.dp)
      )
      .clickable { onClick() }
      .padding(vertical = 10.dp, horizontal = 4.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = color.copy(alpha = 0.8f)
      )
    }
  }
}
