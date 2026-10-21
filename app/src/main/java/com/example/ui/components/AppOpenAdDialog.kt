package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

// Official Google AdMob Test Interstitial Ad Unit ID
const val ADMOB_TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

object AdMobInterstitialHelper {
  private var interstitialAd: InterstitialAd? = null
  private var isAdLoading = false

  fun loadAd(context: Context, onLoaded: (() -> Unit)? = null) {
    if (isRunningOnEmulator()) {
      onLoaded?.invoke()
      return
    }
    if (interstitialAd != null || isAdLoading) return

    isAdLoading = true
    val adRequest = AdRequest.Builder().build()
    try {
      InterstitialAd.load(
        context,
        ADMOB_TEST_INTERSTITIAL_ID,
        adRequest,
        object : InterstitialAdLoadCallback() {
          override fun onAdLoaded(ad: InterstitialAd) {
            interstitialAd = ad
            isAdLoading = false
            Log.d("AdMobInterstitial", "Interstitial Ad successfully loaded")
            onLoaded?.invoke()
          }

          override fun onAdFailedToLoad(error: LoadAdError) {
            interstitialAd = null
            isAdLoading = false
            Log.w("AdMobInterstitial", "Interstitial Ad failed: ${error.message}")
          }
        }
      )
    } catch (e: Exception) {
      isAdLoading = false
      Log.e("AdMobInterstitial", "Error loading interstitial ad", e)
    }
  }

  fun showAd(activity: Activity, onDismissed: () -> Unit) {
    val ad = interstitialAd
    if (ad != null) {
      ad.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
          interstitialAd = null
          isAdLoading = false
          loadAd(activity)
          onDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
          interstitialAd = null
          isAdLoading = false
          onDismissed()
        }
      }
      try {
        ad.show(activity)
      } catch (t: Throwable) {
        interstitialAd = null
        onDismissed()
      }
    } else {
      onDismissed()
    }
  }

  fun isAdReady(): Boolean = interstitialAd != null
}

/**
 * Launch Banner Ad Dialog that allows instant entrance into the app.
 * Guaranteed to dismiss cleanly when 'X', outside, or button is clicked.
 */
@Composable
fun AppOpenAdDialog(
  isHindi: Boolean,
  onCloseAndEnterApp: () -> Unit
) {
  val context = LocalContext.current

  LaunchedEffect(Unit) {
    try {
      AdMobInterstitialHelper.loadAd(context)
    } catch (e: Exception) {
      // Ignored
    }
  }

  Dialog(
    onDismissRequest = { onCloseAndEnterApp() },
    properties = DialogProperties(
      dismissOnBackPress = true,
      dismissOnClickOutside = true,
      usePlatformDefaultWidth = true
    )
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
        .testTag("app_open_ad_card"),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top Header: Ad Badge & Close (X) button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = Color(0xFFFEF3C7),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Ad • Google AdMob Test",
                color = Color(0xFF92400E),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Close 'X' Button - immediately closes and reveals app
          IconButton(
            onClick = {
              onCloseAndEnterApp()
            },
            modifier = Modifier
              .size(36.dp)
              .background(Color(0xFFF1F5F9), CircleShape)
              .testTag("btn_close_launch_ad")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Ad",
              tint = Color(0xFF334155),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ad Banner Visual Area
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
              Brush.verticalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
              )
            )
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .background(Color(0xFFEAB308), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "📸 Pro Studio Gear & Lighting Deals",
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Special 30% OFF on Sony & Canon Cine Gear Rentals",
              color = Color(0xFFCBD5E1),
              fontSize = 11.sp,
              textAlign = TextAlign.Center
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ad Description
        Text(
          text = if (isHindi)
            "Google AdMob टेस्ट विज्ञापन। ऊपर '✕' અથવા નીચે બટન દબાવી સીધા એપમાં જાઓ."
          else
            "Google AdMob Test Launch Ad. Tap '✕' or the button below to enter the app.",
          fontSize = 12.sp,
          color = Color(0xFF64748B),
          textAlign = TextAlign.Center,
          lineHeight = 16.sp,
          modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Enter App Button - immediately closes and enters app
        Button(
          onClick = {
            onCloseAndEnterApp()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("btn_enter_app")
        ) {
          Icon(
            imageVector = Icons.Default.Stars,
            contentDescription = null,
            tint = Color(0xFFFACC15),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isHindi) "✕ विज्ञापन बंद करें અને એપમાં જાઓ" else "✕ Close Ad & Enter App",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}
