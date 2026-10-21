package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

// Official Google AdMob Test Banner Unit ID
const val ADMOB_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

fun isRunningOnEmulator(): Boolean {
  return (Build.FINGERPRINT.startsWith("generic")
      || Build.FINGERPRINT.startsWith("unknown")
      || Build.MODEL.contains("google_sdk")
      || Build.MODEL.contains("Emulator")
      || Build.MODEL.contains("Android SDK built for x86")
      || Build.MANUFACTURER.contains("Genymotion")
      || Build.HARDWARE.contains("goldfish")
      || Build.HARDWARE.contains("ranchu")
      || Build.PRODUCT.contains("sdk")
      || Build.PRODUCT.contains("google_sdk"))
}

fun Context.findActivity(): Activity? {
  var currentContext = this
  while (currentContext is ContextWrapper) {
    if (currentContext is Activity) return currentContext
    currentContext = currentContext.baseContext
  }
  return null
}

@Composable
fun AdMobBanner(
  modifier: Modifier = Modifier,
  adUnitId: String = ADMOB_TEST_BANNER_ID
) {
  if (LocalInspectionMode.current) return

  val context = LocalContext.current
  val activity = context.findActivity()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFFF1F5F9))
      .testTag("admob_banner_container")
  ) {
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
      contentAlignment = Alignment.Center
    ) {
      if (isRunningOnEmulator() || activity == null) {
        // High-Fidelity Test Ad Banner in preview/emulator (avoids headless Chromium variation seed loader warning)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFF0F172A))
            .padding(horizontal = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFF59E0B)
          ) {
            Text(
              text = "Ad",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A),
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Google AdMob Test Banner • 320x50",
            fontSize = 11.sp,
            color = Color(0xFFFDE68A),
            fontWeight = FontWeight.SemiBold
          )
        }
      } else {
        // Real Native AdMob View on physical Android device
        AndroidView(
          modifier = Modifier.fillMaxWidth(),
          factory = { _ ->
            try {
              AdView(activity).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                  override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d("AdMobBanner", "Test Ad loaded successfully")
                  }

                  override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.w("AdMobBanner", "Test Ad failed: ${error.message}")
                  }
                }
                loadAd(AdRequest.Builder().build())
              }
            } catch (t: Throwable) {
              Log.e("AdMobBanner", "Safe fallback on AdView error", t)
              android.view.View(activity)
            }
          }
        )
      }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0))
  }
}
