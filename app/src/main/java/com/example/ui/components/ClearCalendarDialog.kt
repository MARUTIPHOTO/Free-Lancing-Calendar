package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ClearType

@Composable
fun ClearCalendarDialog(
  isGujarati: Boolean,
  onDismiss: () -> Unit,
  onConfirmClear: (ClearType) -> Unit
) {
  var selectedClearType by remember { mutableStateOf(ClearType.ALL_DATES) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("clear_calendar_dialog"),
    icon = {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFFFEE2E2)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = null,
          tint = Color(0xFFDC2626),
          modifier = Modifier.size(24.dp)
        )
      }
    },
    title = {
      Text(
        text = if (isGujarati) "कैलेंडर रीसेट करें (Clear Calendar)" else "Reset / Clear Calendar",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (isGujarati)
            "क्या आप वास्तव में कैलेंडर तारीखें साफ़ करना चाहते हैं? रीसेट का प्रकार चुनें:"
          else
            "Choose which dates you want to clear from the calendar:",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Option 1: All Dates
        ClearOptionRow(
          title = if (isGujarati) "सभी तारीखें साफ़ करें (Clear All)" else "Clear All Dates",
          subtitle = if (isGujarati) "सभी बुकिंग और पूछताछ हट जाएगी" else "Deletes all bookings, free dates & inquiries",
          isSelected = selectedClearType == ClearType.ALL_DATES,
          onClick = { selectedClearType = ClearType.ALL_DATES },
          testTag = "clear_all_option"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Option 2: Inquiries Only
        ClearOptionRow(
          title = if (isGujarati) "केवल पूछताछ साफ़ करें (Inquiries Only)" else "Clear Inquiries Only",
          subtitle = if (isGujarati) "केवल पीली (Yellow) पूछताछ हटाएगा" else "Clears pending inquiries, keeps confirmed bookings",
          isSelected = selectedClearType == ClearType.INQUIRIES_ONLY,
          onClick = { selectedClearType = ClearType.INQUIRIES_ONLY },
          testTag = "clear_inquiries_option"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Option 3: Current Month Only
        ClearOptionRow(
          title = if (isGujarati) "केवल यह महीना साफ़ करें" else "Reset Current Month Only",
          subtitle = if (isGujarati) "केवल चालू महीने की तारीखें रीसेट होंगी" else "Only clears dates in the currently viewed month",
          isSelected = selectedClearType == ClearType.CURRENT_MONTH_ONLY,
          onClick = { selectedClearType = ClearType.CURRENT_MONTH_ONLY },
          testTag = "clear_month_option"
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmClear(selectedClearType) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("confirm_clear_button")
      ) {
        Text(
          text = if (isGujarati) "हाँ, क्लियर करें" else "Yes, Clear",
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("cancel_clear_button")
      ) {
        Text(if (isGujarati) "रद्द करें" else "Cancel")
      }
    }
  )
}

@Composable
private fun ClearOptionRow(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) Color(0xFFFEE2E2).copy(alpha = 0.5f) else Color.Transparent)
      .clickable { onClick() }
      .padding(6.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    RadioButton(
      selected = isSelected,
      onClick = onClick
    )
    Spacer(modifier = Modifier.width(6.dp))
    Column {
      Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
