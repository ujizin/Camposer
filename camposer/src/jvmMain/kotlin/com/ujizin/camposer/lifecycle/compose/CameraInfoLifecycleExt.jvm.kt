package com.ujizin.camposer.lifecycle.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.CameraInfoState
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
public fun CameraInfo.collectStateWithLifecycle(
  context: CoroutineContext = EmptyCoroutineContext,
): State<CameraInfoState> = state.collectAsState(context = context)
