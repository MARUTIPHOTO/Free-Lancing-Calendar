package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.data.local.StudioDatabase
import com.example.data.repository.StudioRepository
import com.example.ui.components.BulkDateActionDialog
import com.example.ui.components.ClearCalendarDialog
import com.example.ui.components.ConfirmInquiryDialog
import com.example.ui.components.DateDetailDialog
import com.example.ui.components.NotificationBanner
import com.example.ui.components.ShareMessageDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ClientPortalScreen
import com.example.ui.screens.InquiryInboxScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.UpcomingBookingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.BulkActionType
import com.example.ui.viewmodel.StudioViewModel
import com.example.ui.viewmodel.StudioViewModelFactory

import com.example.ui.components.AdMobBanner
import com.example.ui.components.AppOpenAdDialog
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Google Mobile Ads (AdMob) on physical Android devices
    if (!com.example.ui.components.isRunningOnEmulator()) {
      try {
        val testConfig = com.google.android.gms.ads.RequestConfiguration.Builder()
          .setTestDeviceIds(listOf(com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR))
          .build()
        MobileAds.setRequestConfiguration(testConfig)
      } catch (e: Exception) {
        // Ignored
      }

      CoroutineScope(Dispatchers.IO).launch {
        try {
          MobileAds.initialize(this@MainActivity) {}
        } catch (e: Exception) {
          // Ignored in test/preview environment
        }
      }
    }

    val database = StudioDatabase.getDatabase(applicationContext)
    val repository = StudioRepository(database.studioDao())
    val factory = StudioViewModelFactory(repository)

    setContent {
      MyApplicationTheme {
        val viewModel: StudioViewModel = viewModel(factory = factory)

        // Request Notification Permission on Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
          val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
          ) { /* permission result */ }
          androidx.compose.runtime.LaunchedEffect(Unit) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
          }
        }

        // Navigate to inquiries if opened from system notification
        androidx.compose.runtime.LaunchedEffect(intent) {
          if (intent?.getStringExtra("NAV_DESTINATION") == "NOTIFICATIONS") {
            viewModel.navigateTo(AppNavDestination.NOTIFICATIONS)
          }
        }

        val authPrefs = androidx.compose.runtime.remember {
          applicationContext.getSharedPreferences("app_auth_session", android.content.Context.MODE_PRIVATE)
        }
        val auth = androidx.compose.runtime.remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
        var currentUser by androidx.compose.runtime.remember { mutableStateOf(auth.currentUser) }
        var savedEmail by rememberSaveable { mutableStateOf(authPrefs.getString("logged_in_email", null)) }

        val isLoggedIn = (currentUser != null) || !savedEmail.isNullOrBlank()
        val activeEmail = currentUser?.email ?: savedEmail ?: ""

        if (activeEmail.isNotBlank() && viewModel.currentUserEmail != activeEmail) {
          viewModel.currentUserEmail = activeEmail
        }

        if (!isLoggedIn) {
          LoginScreen(
            onLoginSuccess = { user, emailStr ->
              currentUser = user ?: auth.currentUser
              val effectiveEmail = user?.email ?: emailStr.trim()
              savedEmail = effectiveEmail
              authPrefs.edit().putString("logged_in_email", effectiveEmail).apply()
              viewModel.currentUserEmail = effectiveEmail
              val slug = viewModel.getActiveStudioSlug()
              com.example.util.FcmTopicManager.fetchAndSyncFcmToken(applicationContext, slug)
            },
            isHindi = true
          )
        } else {
          // Real-time Firestore sync & FCM topic subscription ONLY when logged in, for this photographer's private studio
          val activeStudioSlug = viewModel.getActiveStudioSlug()

          androidx.compose.runtime.DisposableEffect(activeStudioSlug) {
            // Subscribe device to FCM topic for this photographer's studio slug
            com.example.util.FcmTopicManager.subscribeToStudioTopic(applicationContext, activeStudioSlug)

            // Start listening for new document creation in the specific 'inquiries' collection
            com.example.service.MyFirebaseMessagingService.startListeningForInquiries(applicationContext, activeStudioSlug)

            val syncManager = com.example.data.remote.FirestoreSyncManager(
              context = applicationContext,
              dao = database.studioDao(),
              onNewInquiryNotification = { newInquiry ->
                viewModel.showIncomingInquiryNotification(newInquiry)
              }
            ).apply {
              currentStudioSlug = activeStudioSlug
            }
            viewModel.syncManager = syncManager
            val syncJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
              syncManager.startListening(this)
            }
            onDispose {
              com.example.service.MyFirebaseMessagingService.stopListeningForInquiries()
              syncManager.stopListening()
              syncJob.cancel()
              viewModel.syncManager = null
            }
          }

          MainAppContent(
            viewModel = viewModel,
            currentUserEmail = activeEmail,
            onLogout = {
              try { auth.signOut() } catch (e: Exception) {}
              authPrefs.edit().remove("logged_in_email").apply()
              com.example.util.FcmTopicManager.unsubscribeCurrentTopic(applicationContext)
              currentUser = null
              savedEmail = null
              viewModel.currentUserEmail = ""
            }
          )
        }
      }
    }
  }
}

@Composable
fun MainAppContent(
  viewModel: StudioViewModel,
  currentUserEmail: String = "",
  onLogout: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()
  val isHindi = uiState.profile.selectedLanguage == "HI" || uiState.profile.selectedLanguage == "GU"
  val isGujarati = isHindi

  // App launch ad banner state (defaults to false so app view is immediately visible on launch just like preview)
  var showLaunchAd by rememberSaveable { mutableStateOf(false) }

  // Handle hardware back button
  if (uiState.currentNav != AppNavDestination.CALENDAR) {
    BackHandler {
      viewModel.navigateTo(AppNavDestination.CALENDAR)
    }
  }

  // Unconfirmed inquiries count for badge
  val pendingCount = uiState.inquiries.count { !it.isConfirmed }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Official Google AdMob Test Banner (displayed at bottom without disturbing content)
        AdMobBanner()

        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
        // 1. Calendar Tab
        NavigationBarItem(
          selected = uiState.currentNav == AppNavDestination.CALENDAR,
          onClick = { viewModel.navigateTo(AppNavDestination.CALENDAR) },
          icon = {
            Icon(
              imageVector = if (uiState.currentNav == AppNavDestination.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
              contentDescription = "Calendar"
            )
          },
          label = {
            Text(
              text = if (isHindi) "कैलेंडर" else "Calendar",
              fontSize = 11.sp,
              fontWeight = if (uiState.currentNav == AppNavDestination.CALENDAR) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color(0xFFFEF3C7)
          ),
          modifier = Modifier.testTag("nav_calendar")
        )

        // 2. Inquiries Tab (with badge)
        NavigationBarItem(
          selected = uiState.currentNav == AppNavDestination.NOTIFICATIONS,
          onClick = { viewModel.navigateTo(AppNavDestination.NOTIFICATIONS) },
          icon = {
            BadgedBox(
              badge = {
                if (pendingCount > 0) {
                  Badge(containerColor = Color(0xFFEAB308)) {
                    Text("$pendingCount", color = Color(0xFF713F12), fontWeight = FontWeight.Bold)
                  }
                }
              }
            ) {
              Icon(
                imageVector = if (uiState.currentNav == AppNavDestination.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                contentDescription = "Inquiries"
              )
            }
          },
          label = {
            Text(
              text = if (isHindi) "पूछताछ" else "Inquiries",
              fontSize = 11.sp,
              fontWeight = if (uiState.currentNav == AppNavDestination.NOTIFICATIONS) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color(0xFFFEF3C7)
          ),
          modifier = Modifier.testTag("nav_inquiries")
        )

        // 3. Client Portal Preview Tab
        NavigationBarItem(
          selected = uiState.currentNav == AppNavDestination.CLIENT_PORTAL,
          onClick = { viewModel.navigateTo(AppNavDestination.CLIENT_PORTAL) },
          icon = {
            Icon(
              imageVector = if (uiState.currentNav == AppNavDestination.CLIENT_PORTAL) Icons.Filled.Public else Icons.Outlined.Public,
              contentDescription = "Client Portal"
            )
          },
          label = {
            Text(
              text = if (isHindi) "पोर्टल लिंक" else "Client Link",
              fontSize = 11.sp,
              fontWeight = if (uiState.currentNav == AppNavDestination.CLIENT_PORTAL) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color(0xFFFEF3C7)
          ),
          modifier = Modifier.testTag("nav_client_portal")
        )

        // 4. Bookings List Tab
        NavigationBarItem(
          selected = uiState.currentNav == AppNavDestination.BOOKINGS_LIST,
          onClick = { viewModel.navigateTo(AppNavDestination.BOOKINGS_LIST) },
          icon = {
            Icon(
              imageVector = if (uiState.currentNav == AppNavDestination.BOOKINGS_LIST) Icons.Filled.EventNote else Icons.Outlined.EventNote,
              contentDescription = "Bookings"
            )
          },
          label = {
            Text(
              text = if (isHindi) "बुकिंग्स" else "Bookings",
              fontSize = 11.sp,
              fontWeight = if (uiState.currentNav == AppNavDestination.BOOKINGS_LIST) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color(0xFFFEF3C7)
          ),
          modifier = Modifier.testTag("nav_bookings")
        )

        // 5. Profile Tab
        NavigationBarItem(
          selected = uiState.currentNav == AppNavDestination.PROFILE,
          onClick = { viewModel.navigateTo(AppNavDestination.PROFILE) },
          icon = {
            Icon(
              imageVector = if (uiState.currentNav == AppNavDestination.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
              contentDescription = "Profile"
            )
          },
          label = {
            Text(
              text = if (isHindi) "प्रोफ़ाइल" else "Profile",
              fontSize = 11.sp,
              fontWeight = if (uiState.currentNav == AppNavDestination.PROFILE) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color(0xFFFEF3C7)
          ),
          modifier = Modifier.testTag("nav_profile")
        )
      }
    }
  }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Real-Time Notification Banner at Top (Requirement 3)
        NotificationBanner(
          inquiry = uiState.bannerNotification,
          isGujarati = isGujarati,
          onDoneClick = { inquiry ->
            viewModel.showConfirmInquiryDialog(inquiry)
          },
          onDismissClick = {
            viewModel.dismissBanner()
          },
          onViewClick = { inquiry ->
            viewModel.selectDate(inquiry.dateKey)
            viewModel.navigateTo(AppNavDestination.CALENDAR)
          }
        )

        // Screen Routing
        Box(modifier = Modifier.weight(1f)) {
          when (uiState.currentNav) {
            AppNavDestination.CALENDAR -> CalendarScreen(uiState = uiState, viewModel = viewModel)
            AppNavDestination.NOTIFICATIONS -> InquiryInboxScreen(uiState = uiState, viewModel = viewModel)
            AppNavDestination.CLIENT_PORTAL -> ClientPortalScreen(uiState = uiState, viewModel = viewModel)
            AppNavDestination.BOOKINGS_LIST -> UpcomingBookingsScreen(uiState = uiState, viewModel = viewModel)
            AppNavDestination.PROFILE -> ProfileSettingsScreen(
              uiState = uiState,
              viewModel = viewModel,
              currentUserEmail = currentUserEmail,
              onLogout = onLogout
            )
          }
        }
      }

      // Dialog 1: Date Detail & Quick Editor Dialog
      if (uiState.showDateDetailSheet && uiState.dateDetailSheetKey != null) {
        val dateKey = uiState.dateDetailSheetKey!!
        val dateEntity = uiState.datesMap[dateKey]

        DateDetailDialog(
          dateKey = dateKey,
          studioDate = dateEntity,
          profile = uiState.profile,
          isGujarati = isGujarati,
          onDismiss = { viewModel.dismissDateDetailSheet() },
          onSave = { updated ->
            viewModel.setDateStatusBooked(
              dateKey = updated.dateKey,
              clientName = updated.clientName,
              clientPhone = updated.clientPhone,
              eventType = updated.eventType,
              location = updated.location,
              advance = updated.advanceAmount,
              total = updated.totalPackageAmount,
              note = updated.customNote
            )
          },
          onReset = { key ->
            viewModel.resetDate(key)
          },
          onShareToWhatsApp = { key ->
            viewModel.dismissDateDetailSheet()
            viewModel.openShareDialog(key)
          }
        )
      }

      // Dialog 2: WhatsApp Share Message & Link Dialog (Requirement 2)
      if (uiState.showShareDialog && uiState.shareDialogDateKey != null) {
        val shareKey = uiState.shareDialogDateKey!!
        val initialMsg = viewModel.getShareMessage(shareKey)
        val link = viewModel.getBookingLink(shareKey)

        ShareMessageDialog(
          dateKey = shareKey,
          profile = uiState.profile,
          initialMessage = initialMsg,
          bookingLink = link,
          isGujarati = isGujarati,
          onDismiss = { viewModel.dismissShareDialog() },
          onOpenClientPortalPreview = {
            viewModel.navigateTo(AppNavDestination.CLIENT_PORTAL)
          }
        )
      }

      // Dialog 3: Clear Calendar Dialog (Requirement 5)
      if (uiState.showClearDialog) {
        ClearCalendarDialog(
          isGujarati = isGujarati,
          onDismiss = { viewModel.showClearDialog(false) },
          onConfirmClear = { clearType ->
            viewModel.clearCalendar(clearType)
          }
        )
      }

      // Dialog 4: Confirm Inquiry with Advance Token (Done -> 🔴 Red)
      if (uiState.inquiryToConfirm != null) {
        ConfirmInquiryDialog(
          inquiry = uiState.inquiryToConfirm!!,
          isGujarati = isGujarati,
          onDismiss = { viewModel.dismissConfirmInquiryDialog() },
          onConfirmDone = { advanceAmount, totalAmount ->
            viewModel.confirmInquiryDone(
              inquiry = uiState.inquiryToConfirm!!,
              advanceAmount = advanceAmount,
              totalAmount = totalAmount
            )
          }
        )
      }

      // Dialog 5: Bulk Date Action Dialog (Multi-Date Selection)
      if (uiState.showBulkActionDialog && uiState.bulkActionType != null) {
        BulkDateActionDialog(
          selectedDates = uiState.selectedMultiDateKeys.toList(),
          bulkType = uiState.bulkActionType!!,
          isGujarati = isGujarati,
          onDismiss = { viewModel.dismissBulkActionDialog() },
          onConfirm = { clientName, clientPhone, eventType, location, advance, total, note ->
            if (uiState.bulkActionType == BulkActionType.BOOKED) {
              viewModel.executeBulkBooking(
                clientName = clientName,
                clientPhone = clientPhone,
                eventType = eventType,
                location = location,
                advance = advance,
                total = total,
                note = note
              )
            } else {
              viewModel.executeBulkInquiry(
                clientName = clientName,
                clientPhone = clientPhone,
                eventType = eventType,
                location = location,
                note = note
              )
            }
          }
        )
      }

      // Dialog 6: App Open Ad Banner (Ad on launch with close button to enter app)
      if (showLaunchAd) {
        AppOpenAdDialog(
          isHindi = isHindi,
          onCloseAndEnterApp = {
            showLaunchAd = false
          }
        )
      }
    }
  }
}
