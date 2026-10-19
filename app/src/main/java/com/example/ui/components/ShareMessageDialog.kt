package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudioProfileEntity
import com.example.ui.util.WhatsAppShareHelper
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ShareMessageDialog(
  dateKey: String,
  profile: StudioProfileEntity,
  initialMessage: String,
  bookingLink: String,
  isHindi: Boolean = false,
  isGujarati: Boolean = isHindi,
  onDismiss: () -> Unit,
  onOpenClientPortalPreview: () -> Unit
) {
  val context = LocalContext.current
  var messageText by remember(initialMessage) { mutableStateOf(initialMessage) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("share_message_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isHindi) "व्हाट्सएप लिंक और मैसेज 💬" else "WhatsApp Message & Link 💬",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (isHindi)
            "यह मैसेज क्लाइंट को व्हाट्सएप पर भेजें। क्लाइंट लिंक खोलकर तारीख की बुकिंग कर सकते हैं:"
          else
            "Send this customized message to your client. They can click the link to submit their inquiry:",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Editable Message Field
        OutlinedTextField(
          value = messageText,
          onValueChange = { messageText = it },
          label = { Text(if (isHindi) "कस्टम मैसेज" else "Message Text") },
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .testTag("share_message_text_input"),
          maxLines = 6
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Direct Portal Test Button
        OutlinedButton(
          onClick = {
            onDismiss()
            onOpenClientPortalPreview()
          },
          modifier = Modifier.fillMaxWidth().testTag("preview_client_link_btn")
        ) {
          Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isHindi) "इस लिंक का पोर्टल टेस्ट करें (Preview)" else "Test Client Portal Preview",
            fontSize = 12.sp
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          WhatsAppShareHelper.shareToWhatsApp(
            context = context,
            messageText = messageText,
            chooserTitle = if (isHindi) "बुकिंग लिंक WhatsApp शेयर करें" else "Share Booking via WhatsApp"
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("send_whatsapp_btn")
      ) {
        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isHindi) "WhatsApp शेयर करें" else "Share WhatsApp",
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = {
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          val clip = ClipData.newPlainText("Booking Link", messageText)
          clipboard.setPrimaryClip(clip)
          Toast.makeText(
            context,
            if (isHindi) "मैसेज कॉपी हो गया!" else "Copied to Clipboard!",
            Toast.LENGTH_SHORT
          ).show()
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("copy_message_btn")
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (isHindi) "कॉपी करें" else "Copy")
      }
    }
  )
}
