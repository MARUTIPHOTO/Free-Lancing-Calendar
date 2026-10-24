package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudioDateEntity
import com.example.data.model.DateStatus
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedContainer
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.StatusYellowContainer
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DateCell(
  date: LocalDate,
  isCurrentMonth: Boolean,
  isToday: Boolean,
  isSelected: Boolean,
  isMultiSelected: Boolean = false,
  studioDate: StudioDateEntity?,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val dateKey = date.toString()
  val status = studioDate?.dateStatus ?: DateStatus.DEFAULT

  val (baseBgColor, baseBorderColor, indicatorColor) = when (status) {
    DateStatus.FREE -> Triple(
      StatusGreenContainer.copy(alpha = if (isCurrentMonth) 0.65f else 0.25f),
      StatusGreen.copy(alpha = 0.5f),
      StatusGreen
    )
    DateStatus.INQUIRY -> Triple(
      StatusYellowContainer.copy(alpha = if (isCurrentMonth) 0.75f else 0.3f),
      StatusYellow.copy(alpha = 0.7f),
      StatusYellow
    )
    DateStatus.BOOKED -> Triple(
      if (isCurrentMonth) Color(0xFFFECACA) else Color(0xFFFEE2E2),
      Color(0xFFDC2626),
      Color(0xFFDC2626)
    )
    DateStatus.DEFAULT -> Triple(
      if (isCurrentMonth) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
      if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
      Color.Transparent
    )
  }

  val bgColor = if (isMultiSelected) {
    Color(0xFFFEF3C7) // Golden highlight
  } else {
    baseBgColor
  }

  val finalBorder = when {
    isMultiSelected -> Color(0xFFD97706) // Bold Gold/Amber border
    status == DateStatus.BOOKED -> Color(0xFFDC2626) // Solid Bold Red for booked/blocked
    isSelected -> MaterialTheme.colorScheme.primary
    isToday -> Color(0xFFD97706)
    else -> baseBorderColor
  }

  val borderWidth = when {
    isMultiSelected -> 2.5.dp
    status == DateStatus.BOOKED -> 2.dp
    isSelected || isToday -> 2.dp
    status != DateStatus.DEFAULT -> 1.dp
    else -> 0.5.dp
  }

  Box(
    modifier = modifier
      .aspectRatio(0.85f)
      .padding(2.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(bgColor)
      .border(
        width = borderWidth,
        color = if (borderWidth > 0.dp) finalBorder else Color(0xFFE2E8F0),
        shape = RoundedCornerShape(10.dp)
      )
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      )
      .testTag("date_cell_$dateKey"),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 2.dp, vertical = 3.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top row: Date Number + Status Dot / Lock Icon or Multi-Select Checkmark
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Date number
        val dateBoxBg = when {
          isMultiSelected -> Color(0xFFD97706)
          status == DateStatus.BOOKED -> Color(0xFFDC2626) // Solid RED badge for booked day
          isToday -> Color(0xFFD97706)
          isSelected -> MaterialTheme.colorScheme.primary
          else -> Color.Transparent
        }

        val dateTextColor = when {
          isMultiSelected || isToday || isSelected || status == DateStatus.BOOKED -> Color.White
          !isCurrentMonth -> Color.Gray.copy(alpha = 0.6f)
          else -> MaterialTheme.colorScheme.onSurface
        }

        Box(
          modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(dateBoxBg),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "${date.dayOfMonth}",
            fontSize = 11.sp,
            fontWeight = if (isMultiSelected || isToday || isSelected || status != DateStatus.DEFAULT) FontWeight.Bold else FontWeight.Normal,
            color = dateTextColor,
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Multi-select checkmark OR Booked Lock Icon OR status dot
        if (isMultiSelected) {
          Box(
            modifier = Modifier
              .size(14.dp)
              .clip(CircleShape)
              .background(Color(0xFFD97706)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Selected",
              tint = Color.White,
              modifier = Modifier.size(10.dp)
            )
          }
        } else if (status == DateStatus.BOOKED) {
          // Solid Red Lock Icon
          Box(
            modifier = Modifier
              .size(16.dp)
              .clip(CircleShape)
              .background(Color(0xFFDC2626)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Booked & Blocked",
              tint = Color.White,
              modifier = Modifier.size(9.dp)
            )
          }
        } else if (status != DateStatus.DEFAULT) {
          Box(
            modifier = Modifier
              .size(9.dp)
              .clip(CircleShape)
              .background(indicatorColor)
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Status pill / tag text
      when (status) {
        DateStatus.FREE -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(StatusGreen.copy(alpha = 0.18f))
              .padding(vertical = 1.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "🟢 Open",
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF15803D),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
        DateStatus.INQUIRY -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(StatusYellow.copy(alpha = 0.22f))
              .padding(vertical = 1.dp),
            contentAlignment = Alignment.Center
          ) {
            val nameSnippet = studioDate?.clientName?.split(" ")?.firstOrNull() ?: "Inquiry"
            Text(
              text = "🟡 $nameSnippet",
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF854D0E),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
        DateStatus.BOOKED -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFFDC2626))
              .padding(vertical = 1.dp, horizontal = 1.dp),
            contentAlignment = Alignment.Center
          ) {
            val nameSnippet = studioDate?.clientName?.split(" ")?.firstOrNull()?.take(5)
            val label = if (!nameSnippet.isNullOrBlank()) "🔒 $nameSnippet" else "🔒 बुक"
            Text(
              text = label,
              fontSize = 7.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
        DateStatus.DEFAULT -> {
          Spacer(modifier = Modifier.height(2.dp))
        }
      }
    }
  }
}

