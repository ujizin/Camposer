package com.ujizin.sample.feature.camera.components

import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ujizin.sample.CamposerTheme
import kotlinx.coroutines.delay

@Composable
fun QrCodeBox(modifier: Modifier = Modifier, qrCodeText: String?) {
    var latestQrCode by remember(Unit) { mutableStateOf(qrCodeText.orEmpty()) }
    var showQrCode by remember { mutableStateOf(false) }
    if (showQrCode) {
        val context = LocalContext.current
        val intent = remember(latestQrCode) {
            Intent(Intent.ACTION_VIEW, Uri.parse(latestQrCode)).takeIf {
                URLUtil.isValidUrl(latestQrCode)
            }
        }
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier
                    .clickable(enabled = intent != null) { context.startActivity(intent) }
                    .width(240.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                text = latestQrCode,
                fontWeight = FontWeight.SemiBold,
                color = if (intent != null) Color(0xFF6891E4) else Color.Black,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }

    LaunchedEffect(qrCodeText) {
        if (qrCodeText != null) {
            showQrCode = true
            latestQrCode = qrCodeText
        } else {
            delay(1000)
            showQrCode = false
        }
    }
}

@Preview
@Composable
private fun PreviewQrCodeBox() {
    CamposerTheme {
        QrCodeBox(qrCodeText = "#UJI")
    }
}

@Preview
@Composable
private fun PreviewFullQrCodeBox() {
    CamposerTheme {
        QrCodeBox(qrCodeText = "https://www.google.com")
    }
}