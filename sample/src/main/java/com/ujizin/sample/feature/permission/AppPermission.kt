package com.ujizin.sample.feature.permission

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.ujizin.sample.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppPermission(content: @Composable () -> Unit) {
    val permissionsState = rememberMultiplePermissionsState(
        mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
                add(android.Manifest.permission.READ_MEDIA_AUDIO)
                add(android.Manifest.permission.READ_MEDIA_VIDEO)
                add(android.Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    )

    if (permissionsState.allPermissionsGranted) {
        content()
    } else {
        DeniedSection {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }
}

@Composable
private fun DeniedSection(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(
                id = R.string.request_allow_permissions,
                stringResource(id = R.string.app_name),
            ).replaceFirstChar { it.uppercase() },
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Button(
            modifier = Modifier.width(120.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
            onClick = onClick
        ) {
            Text(stringResource(android.R.string.ok), color = Color.White)
        }
    }
}