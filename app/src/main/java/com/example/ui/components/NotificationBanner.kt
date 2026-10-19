package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InquiryEntity
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusYellow

@Composable
fun NotificationBanner(
  inquiry: InquiryEntity?,
  isHindi: Boolean = false,
  isGujarati: Boolean = isHindi,
  onDoneClick: (InquiryEntity) -> Unit,
  onDismissClick: () -> Unit,
  onViewClick: (InquiryEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = inquiry != null,
    enter = slideInVertically() + fadeIn(),
    exit = slideOutVertically() + fadeOut(),
    modifier = modifier
  ) {
    if (inquiry != null) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFEF3C7), // Warm Amber Light
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
        shadowElevation = 4.dp
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
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "New Notification",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isHindi) "नई पूछताछ आई है! (New Inquiry)" else "New Booking Inquiry!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78350F)
              )
              Text(
                text = "${inquiry.clientName} • ${inquiry.clientPhone}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF92400E)
              )
            }

            IconButton(
              onClick = onDismissClick,
              modifier = Modifier.size(28.dp).testTag("dismiss_notification_button")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color(0xFF78350F),
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "📅 ${inquiry.dateKey} ${if (inquiry.eventType.isNotBlank()) "• " + inquiry.eventType else ""}",
            fontSize = 11.sp,
            color = Color(0xFF451A03),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // "Done" Confirm Button
            Button(
              onClick = { onDoneClick(inquiry) },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDC2626) // Booked Red color
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .height(34.dp)
                .testTag("notification_done_button")
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.White
              )
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
}
