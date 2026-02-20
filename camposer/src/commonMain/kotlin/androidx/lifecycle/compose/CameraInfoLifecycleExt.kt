package androidx.lifecycle.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.CameraInfoState
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects [CameraInfo.state] as Compose [State], scoped to the provided [LifecycleOwner].
 *
 * Collection starts when lifecycle reaches [minActiveState] and stops when below it.
 */
@Composable
public fun CameraInfo.collectAsStateWithLifecycle(
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  context: CoroutineContext = EmptyCoroutineContext,
): State<CameraInfoState> =
  state.collectAsStateWithLifecycle(
    lifecycleOwner = lifecycleOwner,
    minActiveState = minActiveState,
    context = context,
  )

/**
 * Collects [CameraInfo.state] as Compose [State], scoped to the provided [Lifecycle].
 *
 * Collection starts when lifecycle reaches [minActiveState] and stops when below it.
 */
@Composable
public fun CameraInfo.collectAsStateWithLifecycle(
  lifecycle: Lifecycle,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  context: CoroutineContext = EmptyCoroutineContext,
): State<CameraInfoState> =
  state.collectAsStateWithLifecycle(
    lifecycle = lifecycle,
    minActiveState = minActiveState,
    context = context,
  )
