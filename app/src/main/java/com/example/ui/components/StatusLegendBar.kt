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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedContainer
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.StatusYellowContainer
import com.example.ui.viewmodel.DateFilter

@Composable
fun StatusLegendBar(
  freeCount: Int,
  inquiryCount: Int,
  bookedCount: Int,
  activeFilter: DateFilter,
  isHindi: Boolean = false,
  isGujarati: Boolean = isHindi,
  onFilterSelected: (DateFilter) -> Unit,
  modifier: Modifier = Modifier
) {
  val useHindi = isHindi || isGujarati
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    shadowElevation = 1.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 🟢 Free Chip
      LegendChip(
        label = if (useHindi) "खाली" else "Free",
        count = freeCount,
        dotColor = StatusGreen,
        containerColor = StatusGreenContainer,
        textColor = Color(0xFF14532D),
        isSelected = activeFilter == DateFilter.FREE,
        testTag = "filter_free_chip",
        onClick = {
          onFilterSelected(if (activeFilter == DateFilter.FREE) DateFilter.ALL else DateFilter.FREE)
        },
        modifier = Modifier.weight(1f)
      )

      // 🟡 Inquiry Chip
      LegendChip(
        label = if (useHindi) "पूछताछ" else "Inquiry",
        count = inquiryCount,
        dotColor = StatusYellow,
        containerColor = StatusYellowContainer,
        textColor = Color(0xFF713F12),
        isSelected = activeFilter == DateFilter.INQUIRY,
        testTag = "filter_inquiry_chip",
        onClick = {
          onFilterSelected(if (activeFilter == DateFilter.INQUIRY) DateFilter.ALL else DateFilter.INQUIRY)
        },
        modifier = Modifier.weight(1f)
      )

      // 🔴 Booked Chip
      LegendChip(
        label = if (useHindi) "बुकिंग" else "Booked",
        count = bookedCount,
        dotColor = StatusRed,
        containerColor = StatusRedContainer,
        textColor = Color(0xFF7F1D1D),
        isSelected = activeFilter == DateFilter.BOOKED,
        testTag = "filter_booked_chip",
        onClick = {
          onFilterSelected(if (activeFilter == DateFilter.BOOKED) DateFilter.ALL else DateFilter.BOOKED)
        },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun LegendChip(
  label: String,
  count: Int,
  dotColor: Color,
  containerColor: Color,
  textColor: Color,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderModifier = if (isSelected) {
    Modifier.border(2.dp, dotColor, RoundedCornerShape(10.dp))
  } else {
    Modifier.border(0.5.dp, dotColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (isSelected) containerColor else containerColor.copy(alpha = 0.5f))
      .then(borderModifier)
      .clickable { onClick() }
      .padding(horizontal = 6.dp, vertical = 7.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(dotColor)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
      Spacer(modifier = Modifier.width(4.dp))
      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(dotColor.copy(alpha = 0.2f))
          .padding(horizontal = 5.dp, vertical = 1.dp)
      ) {
        Text(
          text = "$count",
          fontSize = 10.sp,
          fontWeight = FontWeight.ExtraBold,
          color = textColor
        )
      }
    }
  }
}
