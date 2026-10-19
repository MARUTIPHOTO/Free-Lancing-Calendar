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

@Composable
fun ConfirmInquiryDialog(
  inquiry: InquiryEntity,
  isGujarati: Boolean,
  onDismiss: () -> Unit,
  onConfirmDone: (advanceAmount: Double, totalAmount: Double) -> Unit
) {
  var advanceInput by remember { mutableStateOf("") }
  var totalInput by remember { mutableStateOf("") }

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
        text = if (isGujarati) "बुकिंग कन्फर्म करें (Done 🔴)" else "Confirm Booking (Done 🔴)",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (isGujarati)
            "इस पूछताछ को 'Done' करने से यह तारीख लाल (🔴 Booked) हो जाएगी:"
          else
            "Confirming this will mark the date as Booked (🔴 Red):",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Client info card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = "👤 ${inquiry.clientName}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
            Text(
              text = "📞 ${inquiry.clientPhone}",
              fontSize = 12.sp,
              color = Color(0xFF334155)
            )
            Text(
              text = "📅 ${inquiry.dateKey} ${if (inquiry.eventType.isNotBlank()) "• " + inquiry.eventType else ""}",
              fontSize = 12.sp,
              color = Color(0xFF334155)
            )
            if (inquiry.location.isNotBlank()) {
              Text(
                text = "📍 ${inquiry.location}",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Advance Token input
        OutlinedTextField(
          value = advanceInput,
          onValueChange = { advanceInput = it },
          label = { Text(if (isGujarati) "टोकन / एडवांस राशि (₹)" else "Advance Token (₹)") },
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
          text = if (isGujarati) "Done (कन्फर्म करें 🔴)" else "Done (Confirm 🔴)",
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
