package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InquiryEntity
import com.example.ui.util.StringsHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConfirmInquiryDialog(
  inquiry: InquiryEntity,
  isGujarati: Boolean,
  onDismiss: () -> Unit,
  onConfirmDone: (advanceAmount: Double, totalAmount: Double) -> Unit
) {
  val prefilledTotal = remember(inquiry) {
    val regex = "₹([0-9]+)".toRegex()
    val match = regex.find(inquiry.notes) ?: regex.find(inquiry.eventType)
    match?.groupValues?.get(1) ?: ""
  }
  var advanceInput by remember { mutableStateOf("") }
  var totalInput by remember { mutableStateOf(prefilledTotal) }

  val datesList = remember(inquiry.dateKey) {
    StringsHelper.parseDateKeys(inquiry.dateKey)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("confirm_inquiry_dialog"),
    icon = {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFFDC2626)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(26.dp)
        )
      }
    },
    title = {
      Text(
        text = if (isGujarati) "पूछताछ स्वीकार और बुक करें (Accept 🔴)" else "Accept & Confirm Booking (Accept 🔴)",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (isGujarati)
            "स्वीकार करने से नीचे की सभी (${datesList.size}) तारीखें कैलेंडर में लाल (🔴 Booked) हो जाएंगी:"
          else
            "Accepting will mark all (${datesList.size}) date(s) as Booked (🔴 Red) in the calendar:",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Client info & Date chips card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F5F9))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = "👤 ${inquiry.clientName}",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
            if (inquiry.eventType.isNotBlank()) {
              Text(
                text = "📸 ${inquiry.eventType}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD97706)
              )
            }
            if (inquiry.clientPhone.isNotBlank()) {
              Text(
                text = "📞 ${inquiry.clientPhone}",
                fontSize = 12.sp,
                color = Color(0xFF334155)
              )
            }
            if (inquiry.location.isNotBlank()) {
              Text(
                text = "📍 ${inquiry.location}",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dates Badge List
            Text(
              text = if (isGujarati) "📅 चुनी हुई तारीखें (${datesList.size} दिन):" else "📅 Selected Dates (${datesList.size} Days):",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF991B1B)
            )

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
                  color = Color(0xFFDC2626)
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Advance Token input
        OutlinedTextField(
          value = advanceInput,
          onValueChange = { advanceInput = it },
          label = { Text(if (isGujarati) "एडवांस टोकन राशि (₹)" else "Advance Token (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("confirm_advance_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Total Package input
        OutlinedTextField(
          value = totalInput,
          onValueChange = { totalInput = it },
          label = { Text(if (isGujarati) "कुल पैकेज राशि (₹)" else "Total Package (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("confirm_total_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val adv = advanceInput.toDoubleOrNull() ?: 0.0
          val tot = totalInput.toDoubleOrNull() ?: 0.0
          onConfirmDone(adv, tot)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("confirm_done_final_button")
      ) {
        Text(
          text = if (isGujarati) "स्वीकारें और बुक करें (${datesList.size} दिन 🔴)" else "Accept & Book All (${datesList.size} Days 🔴)",
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("cancel_confirm_button")
      ) {
        Text(if (isGujarati) "रद्द करें" else "Cancel")
      }
    }
  )
}
