package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudioProfileEntity
import com.example.data.model.DateStatus
import com.example.ui.util.StringsHelper
import com.example.ui.viewmodel.CalendarUiState
import com.example.ui.viewmodel.StudioViewModel
import com.example.ui.components.AppOpenAdDialog
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ProfileSettingsScreen(
  uiState: CalendarUiState,
  viewModel: StudioViewModel,
  currentUserEmail: String = "",
  onLogout: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val profile = uiState.profile
  val isHindi = profile.selectedLanguage == "HI" || profile.selectedLanguage == "GU"
  val isGujarati = isHindi

  var showLogoutConfirmDialog by remember { mutableStateOf(false) }
  var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

  var showAdPreview by remember { mutableStateOf(false) }

  if (showAdPreview) {
    AppOpenAdDialog(
      isHindi = isHindi,
      onCloseAndEnterApp = { showAdPreview = false }
    )
  }

  var studioName by remember(profile) { mutableStateOf(profile.studioName) }
  var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
  var phone by remember(profile) { mutableStateOf(profile.phone) }
  var address by remember(profile) { mutableStateOf(profile.address) }
  var instagram by remember(profile) { mutableStateOf(profile.instagram) }
  var roleSpecialization by remember(profile) { mutableStateOf(profile.roleSpecialization) }
  var cameraGear by remember(profile) { mutableStateOf(profile.cameraGear) }
  var defaultDayRate by remember(profile) { mutableStateOf(if (profile.defaultDayRate > 0) profile.defaultDayRate.toInt().toString() else "7000") }
  var rateTraditionalPhoto by remember(profile) { mutableStateOf(profile.rateTraditionalPhoto.toInt().toString()) }
  var rateCandidPhoto by remember(profile) { mutableStateOf(profile.rateCandidPhoto.toInt().toString()) }
  var rateCinematicVideo by remember(profile) { mutableStateOf(profile.rateCinematicVideo.toInt().toString()) }
  var rateTraditionalVideo by remember(profile) { mutableStateOf(profile.rateTraditionalVideo.toInt().toString()) }
  var rateDrone by remember(profile) { mutableStateOf(profile.rateDrone.toInt().toString()) }
  var ratePreWedding by remember(profile) { mutableStateOf(profile.ratePreWedding.toInt().toString()) }
  var customTemplateGu by remember(profile) { mutableStateOf(profile.customTemplateGu) }
  var customTemplateEn by remember(profile) { mutableStateOf(profile.customTemplateEn) }
  var selectedLanguage by remember(profile) { mutableStateOf(if (profile.selectedLanguage == "GU") "HI" else profile.selectedLanguage) }

  val tokens = listOf(
    Pair("{owner_name}", if (isHindi) "आपका नाम" else "Name"),
    Pair("{studio_name}", if (isHindi) "ब्रांड नाम" else "Brand"),
    Pair("{date}", if (isHindi) "तारीख" else "Date"),
    Pair("{client_name}", if (isHindi) "क्लाइंट नाम" else "Client"),
    Pair("{phone}", if (isHindi) "फ़ोन नंबर" else "Phone"),
    Pair("{link}", if (isHindi) "बुकिंग लिंक" else "Link")
  )

  val freelancerRoles = if (isHindi) StringsHelper.freelancerRolesHi else StringsHelper.freelancerRolesEn

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp, bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CameraAlt,
          contentDescription = null,
          tint = Color(0xFFF59E0B),
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isHindi) "फ्रीलांसर फोटोग्राफर प्रोफ़ाइल" else "Freelancer Photographer Profile",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (isHindi) "कैमरा गियर, शिफ्ट रेट & WhatsApp ब्रॉडकास्ट" else "Camera gear, day rates & broadcast templates",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 1. Language Toggle & Freelancer Badge
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFEF3C7))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "📸 FREELANCE PRO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF78350F)
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilterChip(
            selected = selectedLanguage == "HI" || selectedLanguage == "GU",
            onClick = { selectedLanguage = "HI" },
            label = { Text("हिन्दी") },
            modifier = Modifier.testTag("lang_hi_chip")
          )
          FilterChip(
            selected = selectedLanguage == "EN",
            onClick = { selectedLanguage = "EN" },
            label = { Text("English") },
            modifier = Modifier.testTag("lang_en_chip")
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Freelancer Personal & Brand Details
    Text(
      text = if (isHindi) "फोटोग्राफर विवरण (Personal & Brand):" else "Photographer & Brand Info:",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Photographer Name
    OutlinedTextField(
      value = ownerName,
      onValueChange = { ownerName = it },
      label = { Text(if (isHindi) "आपका पूरा नाम (Photographer Name)" else "Your Full Name") },
      leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("profile_owner_name")
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Studio / Brand Name
    OutlinedTextField(
      value = studioName,
      onValueChange = { studioName = it },
      label = { Text(if (isHindi) "ब्रांड / स्टूडियो नाम (Brand Title)" else "Brand / Profile Title") },
      leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("profile_studio_name")
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Phone
    OutlinedTextField(
      value = phone,
      onValueChange = { phone = it },
      label = { Text(if (isHindi) "मोबाइल नंबर (WhatsApp)" else "Contact Mobile (WhatsApp)") },
      leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("profile_phone")
    )

    Spacer(modifier = Modifier.height(8.dp))

    // City / Base Address
    OutlinedTextField(
      value = address,
      onValueChange = { address = it },
      label = { Text(if (isHindi) "आपका शहर / लोकेशन (City / Base)" else "City / Base Location") },
      leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("profile_address")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Freelancer Roles & Skills (Candid, Cinematic, Drone)
    Text(
      text = if (isHindi) "आपकी स्पेशलाइजेशन / रोल (Role & Skills):" else "Your Skills & Specializations:",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(freelancerRoles) { role ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
            .clickable {
              roleSpecialization = if (roleSpecialization.isBlank()) role else "$roleSpecialization • $role"
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "+ $role", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
      value = roleSpecialization,
      onValueChange = { roleSpecialization = it },
      label = { Text(if (isHindi) "स्पेशलाइजेशन (Selected Roles)" else "Specialization Roles") },
      leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
      modifier = Modifier.fillMaxWidth().testTag("profile_role_input")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // 4. Camera Gear & Equipment
    Text(
      text = if (isHindi) "कैमरा गियर और उपकरण (Camera Gear):" else "Camera Gear & Equipment:",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = if (isHindi)
        "अन्य स्टूडियो आपको हायर करने से पहले आपका कैमरा गियर देखते हैं।"
      else
        "Studios check your equipment before booking you for shoots.",
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(StringsHelper.gearPresets) { preset ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEF3C7))
            .border(0.5.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
            .clickable {
              cameraGear = if (cameraGear.isBlank()) preset else "$cameraGear, $preset"
            }
            .padding(horizontal = 8.dp, vertical = 5.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "+ $preset", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
      value = cameraGear,
      onValueChange = { cameraGear = it },
      label = { Text(if (isHindi) "आपका कैमरा सेटअप (Camera Bodies, Lenses, Drone, Gimbal)" else "Camera Gear List") },
      leadingIcon = { Icon(Icons.Default.FlashOn, contentDescription = null) },
      maxLines = 3,
      modifier = Modifier.fillMaxWidth().testTag("profile_gear_input")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // 5. Daily Shift Rate (₹)
    OutlinedTextField(
      value = defaultDayRate,
      onValueChange = { defaultDayRate = it },
      label = { Text(if (isHindi) "डे बेस चार्ज (₹ प्रति दिन)" else "Base Day Rate (₹ / Day)") },
      leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("profile_day_rate_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 5.1 Service Rates Card (Candid, Traditional, Cinematic, Drone, etc.)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = if (isHindi) "💰 सर्विस रेट कार्ड (प्रति दिन चार्ज)" else "💰 Service Rate Card (Per Day Charge)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF78350F)
          )
        }
        Text(
          text = if (isHindi) "क्लाइंट बुकिंग में तारीख के अनुसार यह चार्ज अपने आप जुड़ेगा" else "These rates apply automatically when clients pick services per date",
          fontSize = 10.sp,
          color = Color(0xFF92400E)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = rateCandidPhoto,
            onValueChange = { rateCandidPhoto = it },
            label = { Text("📸 Candid (₹)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = rateTraditionalPhoto,
            onValueChange = { rateTraditionalPhoto = it },
            label = { Text("📷 Traditional (₹)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = rateCinematicVideo,
            onValueChange = { rateCinematicVideo = it },
            label = { Text("🎥 Cinematic (₹)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = rateTraditionalVideo,
            onValueChange = { rateTraditionalVideo = it },
            label = { Text("📹 Video (₹)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = rateDrone,
            onValueChange = { rateDrone = it },
            label = { Text("🚁 Drone (₹)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = ratePreWedding,
            onValueChange = { ratePreWedding = it },
            label = { Text("💍 Pre-Wedding (₹)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 6. 📢 Freelance WhatsApp Status Availability Broadcast Card (HIGH VALUE FOR FREELANCERS)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0xFF25D366)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (isHindi) "📢 WhatsApp स्टेटस ब्रॉडकास्ट जनरेटर" else "📢 WhatsApp Status Availability Broadcast",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = if (isHindi) "स्टूडियो ग्रुप और स्टेटस में तारीखें शेयर करें" else "Broadcast free dates to studio owners & groups",
              fontSize = 10.sp,
              color = Color(0xFFCBD5E1)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Get free dates
        val freeDates = uiState.datesMap.values
          .filter { it.dateStatus == DateStatus.FREE }
          .map { it.dateKey }

        Button(
          onClick = {
            val broadcastText = if (freeDates.isNotEmpty()) {
              viewModel.getShareMessage(freeDates.first())
            } else {
              """📸 *$studioName*
🎯 $roleSpecialization
🎥 $cameraGear
💰 Day Rate: ₹$defaultDayRate / day

🟢 Contact for Wedding & Event Bookings:
📞 $phone • $address"""
            }

            val encoded = try {
              URLEncoder.encode(broadcastText, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) { "" }
            val uri = Uri.parse("https://api.whatsapp.com/send?text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
              context.startActivity(intent)
            } catch (e: Exception) {
              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, broadcastText)
              }
              context.startActivity(Intent.createChooser(shareIntent, "Broadcast Free Dates"))
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().testTag("broadcast_status_btn")
        ) {
          Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isHindi) "WhatsApp स्टेटस पर शेयर करें (Broadcast)" else "Post on WhatsApp Status / Groups",
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 7. Custom Message Template Editor
    Text(
      text = if (isHindi) "कस्टम मैसेज टेम्पलेट (WhatsApp Message):" else "Custom WhatsApp Message Template:",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(tokens) { token ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEF3C7))
            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
            .clickable {
              if (selectedLanguage == "HI" || selectedLanguage == "GU") {
                customTemplateGu = customTemplateGu + " " + token.first
              } else {
                customTemplateEn = customTemplateEn + " " + token.first
              }
            }
            .padding(horizontal = 8.dp, vertical = 5.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "${token.first} (${token.second})",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF78350F)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (selectedLanguage == "HI" || selectedLanguage == "GU") {
      OutlinedTextField(
        value = customTemplateGu,
        onValueChange = { customTemplateGu = it },
        label = { Text("हिन्दी मैसेज टेम्पलेट") },
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .testTag("template_hi_input"),
        maxLines = 6
      )
    } else {
      OutlinedTextField(
        value = customTemplateEn,
        onValueChange = { customTemplateEn = it },
        label = { Text("English Message Template") },
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .testTag("template_en_input"),
        maxLines = 6
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Google AdMob Ads Status & Testing Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
      shape = RoundedCornerShape(12.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isHindi) "Google AdMob Testing (दोनों विज्ञापन सक्रिय हैं ✅)" else "Google AdMob Testing (Both Ads Active ✅)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF0F172A)
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = if (isHindi)
            "१. ऐप ओपन बैनर (क्लोज़ '✕' करके ऐप में प्रवेश करें)\n२. नीचे का फिक्स बैनर (बिना रुकावट के रहता है)"
          else
            "1. App Open Ad Banner (with close '✕' to enter app)\n2. Persistent Bottom Banner (without disturbing app)",
          fontSize = 12.sp,
          color = Color(0xFF475569),
          lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
          onClick = { showAdPreview = true },
          modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .testTag("btn_test_open_ad_preview"),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = if (isHindi) "📢 ऐप खोलते समय का विज्ञापन फिर से टेस्ट करें (Test Open Ad)" else "📢 Test Launch Ad Again",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Save Button
    Button(
      onClick = {
        val rate = defaultDayRate.toDoubleOrNull() ?: 7000.0
        val rCandid = rateCandidPhoto.toDoubleOrNull() ?: 10000.0
        val rTrad = rateTraditionalPhoto.toDoubleOrNull() ?: 7000.0
        val rCine = rateCinematicVideo.toDoubleOrNull() ?: 12000.0
        val rTradVid = rateTraditionalVideo.toDoubleOrNull() ?: 7000.0
        val rDrone = rateDrone.toDoubleOrNull() ?: 8000.0
        val rPre = ratePreWedding.toDoubleOrNull() ?: 15000.0
        val updated = StudioProfileEntity(
          id = 1,
          studioName = studioName,
          ownerName = ownerName,
          phone = phone,
          address = address,
          instagram = instagram,
          customTemplateGu = customTemplateGu,
          customTemplateEn = customTemplateEn,
          selectedLanguage = selectedLanguage,
          roleSpecialization = roleSpecialization,
          cameraGear = cameraGear,
          defaultDayRate = rate,
          isFreelancer = true,
          rateTraditionalPhoto = rTrad,
          rateCandidPhoto = rCandid,
          rateCinematicVideo = rCine,
          rateTraditionalVideo = rTradVid,
          rateDrone = rDrone,
          ratePreWedding = rPre
        )
        viewModel.updateProfile(updated)
        Toast.makeText(
          context,
          if (isHindi) "फ्रीलांसर प्रोफ़ाइल सफलतापूर्वक सेव हो गई! ✅" else "Freelancer Profile Saved Successfully! ✅",
          Toast.LENGTH_SHORT
        ).show()
      },
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("save_profile_button")
    ) {
      Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (isHindi) "प्रोफ़ाइल सेव करें (Save Freelancer Profile)" else "Save Freelancer Profile",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    if (showLogoutConfirmDialog) {
      AlertDialog(
        onDismissRequest = { showLogoutConfirmDialog = false },
        title = { Text(if (isHindi) "लॉगआउट करें? 🚪" else "Log Out? 🚪") },
        text = { Text(if (isHindi) "क्या आप निश्चित रूप से अपने अकाउंट से लॉगआउट करना चाहते हैं?" else "Are you sure you want to log out from this account?") },
        confirmButton = {
          Button(
            onClick = {
              showLogoutConfirmDialog = false
              onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
          ) {
            Text(if (isHindi) "लॉगआउट" else "Log Out", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
            Text(if (isHindi) "रद्द करें" else "Cancel")
          }
        }
      )
    }

    if (showPrivacyPolicyDialog) {
      val privacyPolicyUrl = "https://marutiphoto.github.io/Free-Lancing-Calendar/public/privacy-policy.html"
      AlertDialog(
        onDismissRequest = { showPrivacyPolicyDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Privacy Policy",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = Color(0xFF0F172A)
            )
          }
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .height(340.dp)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "FreeLancing Calendar • Maruti Photo & Films",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color(0xFF1E293B)
            )
            Text(
              text = "Effective Date: October 24, 2026\nDeveloper Email: marutiphoto1234@gmail.com",
              fontSize = 11.sp,
              color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "1. Information We Collect",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color(0xFF0F172A)
            )
            Text(
              text = "• Account: Your name and email address through Google Sign-In via Firebase Auth.\n• Bookings: Event dates, client contact numbers, rates, and photoshoot assignments stored on-device and synced via secure Google Firestore.\n• Network state: To ensure seamless online/offline data persistence.",
              fontSize = 11.sp,
              color = Color(0xFF334155),
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "2. Device Permissions",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color(0xFF0F172A)
            )
            Text(
              text = "• INTERNET & ACCESS_NETWORK_STATE: For Google Sign-In and cloud calendar backup.\n• POST_NOTIFICATIONS: For schedule reminders and incoming client booking inquiries.",
              fontSize = 11.sp,
              color = Color(0xFF334155),
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "3. Third-Party Services",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color(0xFF0F172A)
            )
            Text(
              text = "• Google Firebase (Authentication & Cloud Firestore)\n• Google Play Services / Credential Manager\n• Google AdMob (Advertising)",
              fontSize = 11.sp,
              color = Color(0xFF334155),
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "4. User Rights & Data Deletion",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color(0xFF0F172A)
            )
            Text(
              text = "You have full ownership of your data. You can delete individual events directly in the app. To request permanent deletion of your account and cloud records, email marutiphoto1234@gmail.com with subject 'Data Deletion Request'.",
              fontSize = 11.sp,
              color = Color(0xFF334155),
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "5. Children's Privacy",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color(0xFF0F172A)
            )
            Text(
              text = "FreeLancing Calendar is intended for professionals aged 18+ and does not knowingly collect data from children under 13.",
              fontSize = 11.sp,
              color = Color(0xFF334155),
              lineHeight = 16.sp
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                context.startActivity(intent)
              } catch (e: Exception) {
                Toast.makeText(context, "Cannot open browser: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
          ) {
            Text("Open Web Link ↗", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showPrivacyPolicyDialog = false }) {
            Text(if (isHindi) "बंद करें" else "Close", fontSize = 12.sp)
          }
        }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Privacy Policy Card (Google Play Store compliance)
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { showPrivacyPolicyDialog = true }
        .testTag("privacy_policy_card")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0xFF16A34A),
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = if (isHindi) "गोपनीयता नीति (Privacy Policy) 📜" else "Privacy Policy 📜",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF166534)
            )
            Text(
              text = if (isHindi) "Google Play Store के अनुसार डेटा सुरक्षा व नीति पढ़ें" else "Google Play Store compliant data & privacy policy",
              fontSize = 11.sp,
              color = Color(0xFF15803D)
            )
          }
        }

        OutlinedButton(
          onClick = { showPrivacyPolicyDialog = true },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E)),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(text = if (isHindi) "देखें" else "View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Account Details & Logout Card
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isHindi) "सक्रिय लॉगिन अकाउंट (Active Account)" else "Active Login Account",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF991B1B)
            )
            Text(
              text = if (currentUserEmail.isNotBlank()) currentUserEmail else if (isHindi) "सक्रिय खाता" else "Active Account",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF1E293B)
            )
          }

          Surface(
            shape = CircleShape,
            color = Color(0xFFDC2626),
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
        }

        OutlinedButton(
          onClick = { showLogoutConfirmDialog = true },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("logout_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isHindi) "अकाउंट से लॉगआउट करें (Log Out 🚪)" else "Log Out from Account 🚪",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))
  }
}
