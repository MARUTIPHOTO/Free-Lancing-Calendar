package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudioDateEntity
import com.example.data.model.DateStatus
import com.example.ui.components.DateCell
import com.example.ui.components.StatusLegendBar
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.util.StringsHelper
import com.example.ui.util.WhatsAppShareHelper
import com.example.ui.viewmodel.CalendarUiState
import com.example.ui.viewmodel.DateFilter
import com.example.ui.viewmodel.StudioViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
  uiState: CalendarUiState,
  viewModel: StudioViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isHindi = uiState.profile.selectedLanguage == "HI" || uiState.profile.selectedLanguage == "GU"
  val isGujarati = isHindi
  val currentYearMonth = uiState.currentYearMonth
  val today = remember { LocalDate.now() }

  // Compute Calendar Grid Days
  val daysInGrid = remember(currentYearMonth) {
    val firstDayOfMonth = currentYearMonth.atDay(1)
    val dayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
    val daysInMonth = currentYearMonth.lengthOfMonth()

    val list = mutableListOf<CalendarGridDay>()

    // Leading days from previous month
    val prevMonth = currentYearMonth.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()
    for (i in (daysInPrevMonth - dayOfWeek + 1)..daysInPrevMonth) {
      list.add(
        CalendarGridDay(
          date = prevMonth.atDay(i),
          isCurrentMonth = false
        )
      )
    }

    // Days of current month
    for (day in 1..daysInMonth) {
      list.add(
        CalendarGridDay(
          date = currentYearMonth.atDay(day),
          isCurrentMonth = true
        )
      )
    }

    // Trailing days from next month to complete 35 or 42 cells
    val remaining = (7 - (list.size % 7)) % 7
    val nextMonth = currentYearMonth.plusMonths(1)
    for (day in 1..remaining) {
      list.add(
        CalendarGridDay(
          date = nextMonth.atDay(day),
          isCurrentMonth = false
        )
      )
    }

    list
  }

  // Counts for current month
  val monthPrefix = String.format("%04d-%02d", currentYearMonth.year, currentYearMonth.monthValue)
  val currentMonthDates = uiState.datesMap.values.filter { it.dateKey.startsWith(monthPrefix) }
  val freeCount = currentMonthDates.count { it.dateStatus == DateStatus.FREE }
  val inquiryCount = currentMonthDates.count { it.dateStatus == DateStatus.INQUIRY }
  val bookedCount = currentMonthDates.count { it.dateStatus == DateStatus.BOOKED }

  val isMultiMode = uiState.isMultiSelectMode
  val selectedCount = uiState.selectedMultiDateKeys.size

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 12.dp)
  ) {
    // 1. Studio Header Bar & Multi-Select Toggle
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 10.dp, bottom = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.PhotoCamera,
          contentDescription = null,
          tint = Color(0xFFF59E0B),
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = uiState.profile.studioName,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1
        )
        Text(
          text = if (isMultiMode) {
            if (isHindi) "✨ मल्टी सिलेक्शन मोड चालू" else "✨ Multi-Date Select Active"
          } else {
            if (isHindi) "फ्रीलांसिंग शूटिंग कैलेंडर" else "Freelancer Shoot Calendar"
          },
          fontSize = 11.sp,
          fontWeight = if (isMultiMode) FontWeight.Bold else FontWeight.Normal,
          color = if (isMultiMode) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Multi-Select Toggle Button (KEY FEATURE REQUESTED)
      FilterChip(
        selected = isMultiMode,
        onClick = { viewModel.toggleMultiSelectMode() },
        label = {
          Text(
            text = if (isHindi) "मल्टी तारीख" else "Multi-Date",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        },
        leadingIcon = {
          Icon(
            imageVector = if (isMultiMode) Icons.Default.Check else Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(13.dp)
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = Color(0xFFFEF3C7),
          selectedLabelColor = Color(0xFF78350F),
          selectedLeadingIconColor = Color(0xFF78350F)
        ),
        modifier = Modifier.testTag("toggle_multi_select_btn")
      )

      Spacer(modifier = Modifier.width(4.dp))

      // Language Switcher Button (Hindi <-> English)
      OutlinedButton(
        onClick = { viewModel.toggleLanguage() },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .height(32.dp)
          .testTag("toggle_language_button")
      ) {
        Text(
          text = if (isHindi) "EN" else "हिन्दी",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Clear Calendar Button
      OutlinedButton(
        onClick = { viewModel.showClearDialog(true) },
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = Color(0xFFDC2626)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .height(32.dp)
          .testTag("clear_calendar_button")
      ) {
        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(13.dp))
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 2. DEDICATED 'SELECTION MODE' TOGGLE CARD (User Request)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("selection_mode_card"),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (isMultiMode) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surface
      ),
      border = BorderStroke(
        width = if (isMultiMode) 1.5.dp else 0.5.dp,
        color = if (isMultiMode) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = if (isMultiMode) 2.dp else 0.dp)
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.toggleSelectionMode() },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(if (isMultiMode) Color(0xFFD97706) else Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isMultiMode) Icons.Default.DoneAll else Icons.Default.SelectAll,
              contentDescription = "Selection Mode",
              tint = if (isMultiMode) Color.White else Color(0xFF475569),
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (isHindi) "सिलेक्शन मोड (Selection Mode)" else "Selection Mode",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMultiMode) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurface
              )
              if (isMultiMode) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFD97706))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "${uiState.selectedDatesList.size} SELECTED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                }
              }
            }
            Text(
              text = if (isMultiMode) {
                if (isHindi) "तारीखों पर क्लिक करके हाइलाइट करें • बुकिंग या स्टेटस अपडेट के लिए तैयार"
                else "Click dates to highlight them simultaneously for booking or status updates"
              } else {
                if (isHindi) "एक साथ कई तारीखें चुनने के लिए स्विच ऑन करें"
                else "Toggle ON to click & highlight multiple dates simultaneously"
              },
              fontSize = 11.sp,
              color = if (isMultiMode) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Dedicated Selection Mode Switch (Exact User Request)
          Switch(
            checked = isMultiMode,
            onCheckedChange = { viewModel.toggleSelectionMode(it) },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = Color(0xFFD97706)
            ),
            modifier = Modifier.testTag("selection_mode_switch")
          )
        }

        // Expanded Selected Dates List & Prepared Actions when Selection Mode is Active
        if (isMultiMode) {
          Spacer(modifier = Modifier.height(8.dp))

          // Quick selection helper buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(0.5.dp, Color(0xFF16A34A), RoundedCornerShape(6.dp))
                .clickable { viewModel.selectAllCurrentMonthFreeDates() }
                .padding(horizontal = 7.dp, vertical = 4.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (isHindi) "🟢 सभी खाली चुनें" else "🟢 Select All Free",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF15803D)
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(0.5.dp, Color(0xFF0F172A), RoundedCornerShape(6.dp))
                .clickable { viewModel.selectAllCurrentMonthDates() }
                .padding(horizontal = 7.dp, vertical = 4.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (isHindi) "📅 पूरा महीना" else "📅 All Month",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
            }

            if (selectedCount > 0) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color.White)
                  .border(0.5.dp, Color(0xFFDC2626), RoundedCornerShape(6.dp))
                  .clickable { viewModel.clearMultiSelection() }
                  .padding(horizontal = 7.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (isHindi) "साफ़ करें" else "Clear Selection",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFDC2626)
                )
              }
            }
          }

          // List of Selected Dates Chips
          if (uiState.selectedDatesList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = if (isHindi)
                "हाइलाइट की गई तारीखें (${uiState.selectedDatesList.size}) - अपडेट या बुकिंग के लिए तैयार:"
              else
                "Highlighted Dates (${uiState.selectedDatesList.size}) - Ready for Booking / Status Updates:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF78350F)
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(uiState.selectedDatesList) { dateKey ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = dateKey,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF78350F)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Remove date",
                      tint = Color(0xFF78350F),
                      modifier = Modifier
                        .size(12.dp)
                        .clickable { viewModel.toggleDateSelection(dateKey) }
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dedicated Intent-based WhatsApp Share Button for Selected Dates (with booking link)
            Button(
              onClick = {
                val prefilledMsg = viewModel.getWhatsAppShareMessageForSelectedDates()
                WhatsAppShareHelper.shareToWhatsApp(
                  context = context,
                  messageText = prefilledMsg,
                  chooserTitle = if (isHindi) "चुनी हुई तारीखें WhatsApp पर शेयर करें" else "Share Selected Dates via WhatsApp"
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("whatsapp_share_intent_button")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isHindi)
                  "💬 ${uiState.selectedDatesList.size} तारीखें WhatsApp शेयर करें (बुकिंग लिंक सहित)"
                else
                  "💬 Share ${uiState.selectedDatesList.size} Dates via WhatsApp (With Link)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 3. Month Navigation Bar
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        IconButton(
          onClick = { viewModel.prevMonth() },
          modifier = Modifier.size(34.dp).testTag("prev_month_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = "${StringsHelper.getMonthName(currentYearMonth.monthValue, isGujarati)} ${currentYearMonth.year}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        IconButton(
          onClick = { viewModel.jumpToToday() },
          modifier = Modifier.size(34.dp).testTag("today_button")
        ) {
          Icon(Icons.Default.Today, contentDescription = "Today", tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
        }

        IconButton(
          onClick = { viewModel.nextMonth() },
          modifier = Modifier.size(34.dp).testTag("next_month_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 4. Status Legend Bar with Counts & Interactive Filters
    StatusLegendBar(
      freeCount = freeCount,
      inquiryCount = inquiryCount,
      bookedCount = bookedCount,
      activeFilter = uiState.activeFilter,
      isGujarati = isGujarati,
      onFilterSelected = { viewModel.setFilter(it) }
    )

    Spacer(modifier = Modifier.height(6.dp))

    // 5. Weekdays Header Row
    val weekdays = StringsHelper.getWeekdayNames(isGujarati)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
      weekdays.forEachIndexed { index, day ->
        Text(
          text = day,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (index == 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 6. Calendar Grid of Day Cells with Multi-Select Support
    LazyVerticalGrid(
      columns = GridCells.Fixed(7),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("calendar_month_grid")
    ) {
      items(daysInGrid, key = { it.date.toString() }) { gridDay ->
        val dateKey = gridDay.date.toString()
        val studioDate = uiState.datesMap[dateKey]

        // Check if filtered out
        val matchesFilter = when (uiState.activeFilter) {
          DateFilter.ALL -> true
          DateFilter.FREE -> studioDate?.dateStatus == DateStatus.FREE
          DateFilter.INQUIRY -> studioDate?.dateStatus == DateStatus.INQUIRY
          DateFilter.BOOKED -> studioDate?.dateStatus == DateStatus.BOOKED
        }

        val isMultiSelected = uiState.selectedMultiDateKeys.contains(dateKey)

        DateCell(
          date = gridDay.date,
          isCurrentMonth = gridDay.isCurrentMonth,
          isToday = gridDay.date == today,
          isSelected = uiState.selectedDateKey == dateKey,
          isMultiSelected = isMultiSelected,
          studioDate = if (matchesFilter) studioDate else null,
          onClick = {
            if (isMultiMode) {
              viewModel.toggleDateSelection(dateKey)
            } else {
              viewModel.selectDate(dateKey)
            }
          },
          onLongClick = {
            // Long pressing any date activates multi-select mode instantly!
            if (!isMultiMode) {
              viewModel.enableMultiSelectWithDate(dateKey)
            } else {
              viewModel.toggleDateSelection(dateKey)
            }
          },
          modifier = Modifier.padding(1.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 7. Bottom Action Dock: Switches dynamically based on Multi-Select Mode vs Single Mode
    if (isMultiMode && selectedCount > 0) {
      // MULTI-DATE ACTION DOCK
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        shadowElevation = 6.dp
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(
            text = if (isHindi)
              "चुनी हुई $selectedCount तारीखों पर एक साथ एक्शन:"
            else
              "Bulk Action on $selectedCount selected dates:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFDE68A)
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // 🟢 Bulk Free
            Button(
              onClick = { viewModel.markSelectedDatesAsFree() },
              colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(38.dp).testTag("bulk_mark_free_btn")
            ) {
              Text(
                text = if (isHindi) "🟢 खाली" else "🟢 Free",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            // 🔴 Bulk Book
            Button(
              onClick = { viewModel.openBulkBookDialog() },
              colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(38.dp).testTag("bulk_mark_book_btn")
            ) {
              Text(
                text = if (isHindi) "🔴 बुक" else "🔴 Book",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            // 🟡 Bulk Inquiry
            Button(
              onClick = { viewModel.openBulkInquiryDialog() },
              colors = ButtonDefaults.buttonColors(containerColor = StatusYellow),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(38.dp).testTag("bulk_mark_inquiry_btn")
            ) {
              Text(
                text = if (isHindi) "🟡 पूछताछ" else "🟡 Inquiry",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78350F)
              )
            }

            // 💬 WhatsApp Multi-Date Intent-based Share Button
            Button(
              onClick = {
                val prefilledMsg = viewModel.getWhatsAppShareMessageForSelectedDates()
                WhatsAppShareHelper.shareToWhatsApp(
                  context = context,
                  messageText = prefilledMsg,
                  chooserTitle = if (isHindi) "चुनी हुई तारीखें WhatsApp पर शेयर करें" else "Share Selected Dates via WhatsApp"
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1.1f).height(38.dp).testTag("bulk_share_whatsapp_btn")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = if (isHindi) "WhatsApp" else "Share",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            // ⚪ Reset
            IconButton(
              onClick = { viewModel.resetSelectedDates() },
              modifier = Modifier.size(38.dp).testTag("bulk_reset_btn")
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Reset Selected", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    } else {
      // STANDARD ACTION BAR
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Quick Selection Mode Button
          OutlinedButton(
            onClick = { viewModel.toggleSelectionMode(true) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f).testTag("enable_selection_mode_btn")
          ) {
            Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isHindi) "🔘 सिलेक्शन मोड" else "🔘 Selection Mode",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Quick Share Month Free Dates Button
          Button(
            onClick = {
              val firstFree = currentMonthDates.firstOrNull { it.dateStatus == DateStatus.FREE }?.dateKey
                ?: String.format("%04d-%02d-01", currentYearMonth.year, currentYearMonth.monthValue)
              viewModel.openShareDialog(firstFree)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1.2f).testTag("quick_share_dates_btn")
          ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isHindi) "खाली तारीखें WhatsApp शेयर करें" else "Share Dates to WhatsApp",
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

private data class CalendarGridDay(
  val date: LocalDate,
  val isCurrentMonth: Boolean
)
