package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateStatus
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.CalendarUiState
import com.example.ui.viewmodel.StudioViewModel
import java.time.LocalDate
import java.time.YearMonth

private data class PortalGridDay(
  val date: LocalDate,
  val isCurrentMonth: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClientPortalScreen(
  uiState: CalendarUiState,
  viewModel: StudioViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isHindi = uiState.profile.selectedLanguage == "HI" || uiState.profile.selectedLanguage == "GU"

  // Calendar YearMonth state for Client Portal
  var portalYearMonth by remember { mutableStateOf(YearMonth.now()) }
  val today = remember { LocalDate.now() }

  // Map of selected date -> selected serviceId (e.g., "2026-12-13" -> "candid_photo")
  var dateServicesMap by remember { mutableStateOf(mapOf<String, String>()) }

  var clientName by remember { mutableStateOf("") }
  var clientPhone by remember { mutableStateOf("") }
  var venueLocation by remember { mutableStateOf("") }
  var requirementsNotes by remember { mutableStateOf("") }

  var showSuccessDialog by remember { mutableStateOf(false) }
  var submittedClientName by remember { mutableStateOf("") }
  var submittedDatesCount by remember { mutableStateOf(1) }
  var submittedTotalAmount by remember { mutableStateOf(0.0) }

  // Total calculated package amount based on photographer's rates per service
  val totalPackageAmount = remember(dateServicesMap, uiState.profile) {
    dateServicesMap.entries.sumOf { StringsHelper.getServiceRate(it.value, uiState.profile) }
  }

  // Build grid days for portalYearMonth (Sunday start, standard 7 columns)
  val daysInGrid = remember(portalYearMonth) {
    val list = mutableListOf<PortalGridDay>()
    val firstDayOfMonth = portalYearMonth.atDay(1)
    val daysInMonth = portalYearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0, Mon = 1 ...

    // Leading days from previous month
    if (firstDayOfWeek > 0) {
      val prevMonth = portalYearMonth.minusMonths(1)
      val daysInPrevMonth = prevMonth.lengthOfMonth()
      for (i in (daysInPrevMonth - firstDayOfWeek + 1)..daysInPrevMonth) {
        list.add(PortalGridDay(date = prevMonth.atDay(i), isCurrentMonth = false))
      }
    }

    // Days in current month
    for (day in 1..daysInMonth) {
      list.add(PortalGridDay(date = portalYearMonth.atDay(day), isCurrentMonth = true))
    }

    // Trailing days from next month
    val remaining = (7 - (list.size % 7)) % 7
    val nextMonth = portalYearMonth.plusMonths(1)
    for (day in 1..remaining) {
      list.add(PortalGridDay(date = nextMonth.atDay(day), isCurrentMonth = false))
    }

    list
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
      .verticalScroll(rememberScrollState())
  ) {
    // 1. Studio Branding Header Card with Quick Language Switcher
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF0F172A),
      shadowElevation = 4.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { viewModel.navigateTo(AppNavDestination.CALENDAR) },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "📞 ${uiState.profile.phone} • ${uiState.profile.address}",
              fontSize = 11.sp,
              color = Color(0xFFCBD5E1)
            )
          }

          // In-Portal Language Toggle Button
          OutlinedButton(
            onClick = { viewModel.toggleLanguage() },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFDE68A)),
            modifier = Modifier.height(30.dp)
          ) {
            Text(
              text = if (isHindi) "EN" else "हिन्दी",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .padding(horizontal = 10.dp, vertical = 7.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isHindi)
              "🌟 क्लाइंट बुकिंग पोर्टल • तारीख और काम चुनें (Candid, Traditional, Video)"
            else
              "🌟 Client Booking Portal • Pick Dates & Services (Candid, Traditional, Video)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFDE68A),
            textAlign = TextAlign.Center
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Interactive Monthly Calendar Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        // Section Title
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isHindi)
              "१. कैलेंडर से तारीखें चुनें (खाली तारीखों पर टैप करें):"
            else
              "1. Select Dates from Calendar (Tap open dates):",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Month Navigation Bar
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFF1F5F9)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            IconButton(
              onClick = { portalYearMonth = portalYearMonth.minusMonths(1) },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month", modifier = Modifier.size(18.dp))
            }

            Text(
              text = "${StringsHelper.getMonthName(portalYearMonth.monthValue, isHindi)} ${portalYearMonth.year}",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { portalYearMonth = YearMonth.now() },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(Icons.Default.Today, contentDescription = "Today", tint = Color(0xFFD97706), modifier = Modifier.size(17.dp))
              }

              IconButton(
                onClick = { portalYearMonth = portalYearMonth.plusMonths(1) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", modifier = Modifier.size(18.dp))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Legend Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Free Legend
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF16A34A))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isHindi) "उपलब्ध (Free)" else "Open (Free)",
              fontSize = 11.sp,
              color = Color(0xFF15803D),
              fontWeight = FontWeight.SemiBold
            )
          }

          // Booked Legend (100% Privacy Protected)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFDC2626))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isHindi) "ब्लॉक (Booked 🔒)" else "Blocked 🔒",
              fontSize = 11.sp,
              color = Color(0xFFDC2626),
              fontWeight = FontWeight.SemiBold
            )
          }

          // Selected Legend
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFD97706))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isHindi) "चुनी हुई (Selected)" else "Selected",
              fontSize = 11.sp,
              color = Color(0xFFB45309),
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday Headers (7 Columns)
        val weekdays = StringsHelper.getWeekdayNames(isHindi)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          weekdays.forEachIndexed { index, day ->
            Text(
              text = day,
              modifier = Modifier.weight(1f),
              textAlign = TextAlign.Center,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (index == 0) Color(0xFFDC2626) else Color(0xFF64748B)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Calendar Grid
        val chunkedWeeks = remember(daysInGrid) { daysInGrid.chunked(7) }

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          chunkedWeeks.forEach { week ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              week.forEach { gridDay ->
                val dateKey = gridDay.date.toString()
                val isCurrentMonth = gridDay.isCurrentMonth
                val studioDate = uiState.datesMap[dateKey]
                val isBooked = studioDate?.dateStatus == DateStatus.BOOKED
                val isSelected = dateServicesMap.containsKey(dateKey)
                val isToday = gridDay.date == today

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                      when {
                        !isCurrentMonth -> Color(0xFFF1F5F9).copy(alpha = 0.5f)
                        isBooked -> Color(0xFFFEE2E2)
                        isSelected -> Color(0xFFD97706)
                        else -> Color(0xFFF0FDF4)
                      }
                    )
                    .border(
                      width = if (isSelected || isToday) 1.5.dp else 0.5.dp,
                      color = when {
                        isSelected -> Color(0xFFB45309)
                        isBooked -> Color(0xFFFCA5A5)
                        isToday -> Color(0xFF2563EB)
                        !isCurrentMonth -> Color(0xFFE2E8F0)
                        else -> Color(0xFFBBF7D0)
                      },
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      if (!isCurrentMonth) return@clickable
                      if (isBooked) {
                        Toast.makeText(
                          context,
                          if (isHindi) "यह तारीख पहले से बुक (Block) है!" else "This date is already booked / blocked!",
                          Toast.LENGTH_SHORT
                        ).show()
                      } else {
                        // Toggle date selection
                        if (dateServicesMap.containsKey(dateKey)) {
                          dateServicesMap = dateServicesMap - dateKey
                        } else {
                          dateServicesMap = dateServicesMap + (dateKey to "candid_photo")
                        }
                      }
                    }
                    .padding(2.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Text(
                      text = "${gridDay.date.dayOfMonth}",
                      fontSize = 12.sp,
                      fontWeight = if (isSelected || isBooked || isToday) FontWeight.Bold else FontWeight.Normal,
                      color = when {
                        !isCurrentMonth -> Color(0xFF94A3B8)
                        isSelected -> Color.White
                        isBooked -> Color(0xFF991B1B)
                        else -> Color(0xFF15803D)
                      }
                    )

                    if (isCurrentMonth) {
                      when {
                        isSelected -> {
                          Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                          )
                        }
                        isBooked -> {
                          Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Booked",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(10.dp)
                          )
                        }
                        else -> {
                          Box(
                            modifier = Modifier
                              .size(4.dp)
                              .clip(CircleShape)
                              .background(Color(0xFF16A34A))
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Dates with Per-Date Service & Rate Selection!
        if (dateServicesMap.isNotEmpty()) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFFEF3C7))
              .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
              .padding(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isHindi)
                  "📅 चुनी हुई तारीखें और सर्विस (${dateServicesMap.size} दिन):"
                else
                  "📅 Selected Dates & Services (${dateServicesMap.size} Days):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78350F),
                modifier = Modifier.weight(1f)
              )

              Text(
                text = if (isHindi) "साफ़ करें" else "Clear",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626),
                modifier = Modifier.clickable { dateServicesMap = emptyMap() }
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Render each selected date with its individual service chooser!
            dateServicesMap.keys.sorted().forEach { dateKey ->
              val formatted = StringsHelper.formatDateChip(dateKey)
              val currentServiceId = dateServicesMap[dateKey] ?: "candid_photo"
              val currentRate = StringsHelper.getServiceRate(currentServiceId, uiState.profile).toInt()

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
              ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "📅 $formatted",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF0F172A),
                      modifier = Modifier.weight(1f)
                    )

                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = Color(0xFFDCFCE7)
                    ) {
                      Text(
                        text = "₹$currentRate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Remove",
                      tint = Color(0xFFDC2626),
                      modifier = Modifier
                        .size(16.dp)
                        .clickable { dateServicesMap = dateServicesMap - dateKey }
                    )
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  Text(
                    text = if (isHindi) "इस दिन के लिए क्या काम कराना है? (टैप करें):" else "Select service for this date:",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                  )

                  Spacer(modifier = Modifier.height(4.dp))

                  // Horizontal Service Selection Chips for this date
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    StringsHelper.availableServices.forEach { service ->
                      val isSelected = currentServiceId == service.id
                      val serviceRate = StringsHelper.getServiceRate(service.id, uiState.profile).toInt()

                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .background(if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                          .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(8.dp)
                          )
                          .clickable {
                            dateServicesMap = dateServicesMap + (dateKey to service.id)
                          }
                          .padding(horizontal = 8.dp, vertical = 4.dp)
                      ) {
                        Text(
                          text = "${service.icon} ${if (isHindi) service.nameHi else service.nameEn} • ₹$serviceRate",
                          fontSize = 10.sp,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                          color = if (isSelected) Color(0xFFFDE68A) else Color(0xFF334155)
                        )
                      }
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Package Breakdown Banner
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF0F172A)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color(0xFFFDE68A), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isHindi) "कुल अनुमानित बजट (Total):" else "Total Package Estimate:",
                  fontSize = 11.sp,
                  color = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                  text = "₹${totalPackageAmount.toInt()}",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFDE68A)
                )
              }
            }
          }
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFF1F5F9))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (isHindi)
                "👆 कैलेंडर में उपलब्ध (🟢) तारीख पर टैप करके तारीखें और काम चुनें"
              else
                "👆 Tap any open (🟢) date in calendar to select date and service",
              fontSize = 11.sp,
              color = Color(0xFF475569),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Client Details Form Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Text(
          text = if (isHindi) "२. अपनी जानकारी भरें:" else "2. Enter Your Details:",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Client Full Name
        OutlinedTextField(
          value = clientName,
          onValueChange = { clientName = it },
          label = { Text(if (isHindi) "आपका पूरा नाम / स्टूडियो नाम *" else "Your Name / Studio Name *") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFD97706)) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("portal_client_name_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Client Phone Number
        OutlinedTextField(
          value = clientPhone,
          onValueChange = { clientPhone = it },
          label = { Text(if (isHindi) "मोबाइल नंबर (WhatsApp) *" else "Phone / WhatsApp Number *") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF16A34A)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("portal_client_phone_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Venue Location
        OutlinedTextField(
          value = venueLocation,
          onValueChange = { venueLocation = it },
          label = { Text(if (isHindi) "स्थान / वेन्यू / शहर" else "Venue / Location / City") },
          leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("portal_venue_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Requirements & Notes
        OutlinedTextField(
          value = requirementsNotes,
          onValueChange = { requirementsNotes = it },
          label = { Text(if (isHindi) "अतिरिक्त नोट या विशेष आवश्यकता" else "Additional Notes / Special Instructions") },
          leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
          modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .testTag("portal_notes_input"),
          maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Booking Inquiry Button
        Button(
          onClick = {
            if (dateServicesMap.isEmpty()) {
              Toast.makeText(
                context,
                if (isHindi) "कृपया कैलेंडर से कम से कम एक तारीख चुनें!" else "Please select at least one date from the calendar!",
                Toast.LENGTH_SHORT
              ).show()
              return@Button
            }
            if (clientName.trim().isBlank()) {
              Toast.makeText(
                context,
                if (isHindi) "कृपया अपना नाम लिखें!" else "Please enter your name!",
                Toast.LENGTH_SHORT
              ).show()
              return@Button
            }
            if (clientPhone.trim().isBlank()) {
              Toast.makeText(
                context,
                if (isHindi) "कृपया अपना मोबाइल नंबर लिखें!" else "Please enter your phone number!",
                Toast.LENGTH_SHORT
              ).show()
              return@Button
            }

            // Build detailed services description per date
            val breakdown = dateServicesMap.entries.sortedBy { it.key }.joinToString(" • ") {
              "${StringsHelper.formatDateChip(it.key)}: ${StringsHelper.getServiceName(it.value, isHindi)} (₹${StringsHelper.getServiceRate(it.value, uiState.profile).toInt()})"
            }

            val eventTypeDesc = if (dateServicesMap.size == 1) {
              "${StringsHelper.getServiceName(dateServicesMap.values.first(), isHindi)} (₹${totalPackageAmount.toInt()})"
            } else {
              "Multi-Date Shoot (${dateServicesMap.size} Days • ₹${totalPackageAmount.toInt()})"
            }

            val fullNotes = buildString {
              append("Shoot Plan: $breakdown")
              if (requirementsNotes.isNotBlank()) {
                append("\nNote: $requirementsNotes")
              }
            }

            viewModel.submitClientMultiDateBookingInquiry(
              dateKeys = dateServicesMap.keys.toList(),
              clientName = clientName.trim(),
              clientPhone = clientPhone.trim(),
              eventType = eventTypeDesc,
              location = venueLocation.trim(),
              notes = fullNotes
            ) {
              submittedClientName = clientName
              submittedDatesCount = dateServicesMap.size
              submittedTotalAmount = totalPackageAmount
              showSuccessDialog = true

              val submittedInquiry = com.example.data.local.InquiryEntity(
                id = System.currentTimeMillis(),
                dateKey = dateServicesMap.keys.sorted().joinToString(", "),
                clientName = clientName.trim(),
                clientPhone = clientPhone.trim(),
                eventType = eventTypeDesc,
                location = venueLocation.trim(),
                notes = fullNotes,
                isConfirmed = false,
                isRead = false,
                createdAt = System.currentTimeMillis()
              )
              com.example.util.NotificationHelper.showInquiryNotification(context, submittedInquiry)

              // Clear inputs
              clientName = ""
              clientPhone = ""
              venueLocation = ""
              requirementsNotes = ""
              dateServicesMap = emptyMap()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("portal_submit_inquiry_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFFFDE68A), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isHindi) {
              if (dateServicesMap.isEmpty()) "तारीख चुनें 🚀" else "बुकिंग भेजें (₹${totalPackageAmount.toInt()} • ${dateServicesMap.size} दिन) 🚀"
            } else {
              if (dateServicesMap.isEmpty()) "Pick Dates 🚀" else "Submit Inquiry (₹${totalPackageAmount.toInt()} • ${dateServicesMap.size} Days) 🚀"
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFDE68A)
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
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color(0xFF16A34A),
          modifier = Modifier.size(48.dp)
        )
      },
      title = {
        Text(
          text = if (isHindi) "पूछताछ सफलतापूर्वक भेजी गई! 🎉" else "Inquiry Submitted Successfully! 🎉",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = if (isHindi)
              "नमस्ते $submittedClientName,\nआपकी $submittedDatesCount तारीखों के लिए पूछताछ (अनुमानित कुल राशि: ₹${submittedTotalAmount.toInt()}) '${uiState.profile.studioName}' को प्राप्त हो गई है। हम जल्द ही आपसे संपर्क करेंगे।"
            else
              "Hello $submittedClientName,\nYour inquiry for $submittedDatesCount date(s) (Est. Package: ₹${submittedTotalAmount.toInt()}) has been received by '${uiState.profile.studioName}'. We will contact you shortly.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF334155)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "📞 ${uiState.profile.phone}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
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
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("success_dialog_close_button")
        ) {
          Text(if (isHindi) "ठीक है (OK)" else "Done", color = Color.White)
        }
      }
    )
  }
}
