package com.ujizin.camposer.internal.core.camerax

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.NONE
import androidx.camera.video.VideoRecordEvent

internal class RecordEvent(
  internal val event: VideoRecordEvent?,
) {
  private val finalizeEvent = event as? VideoRecordEvent.Finalize
  internal val cause: Throwable? = finalizeEvent?.cause
  internal val error: Int? = finalizeEvent?.error

  internal var isFinalized = event is VideoRecordEvent.Finalize
    private set
  internal var hasError: Boolean = finalizeEvent?.hasError() ?: false
    private set

  internal var outputUri: Uri? = finalizeEvent?.outputResults?.outputUri
    private set

  @VisibleForTesting(NONE)
  internal constructor(
    isFinalized: Boolean,
    hasError: Boolean,
    outputUri: Uri,
  ) : this(null) {
    this.isFinalized = isFinalized
    this.hasError = hasError
    this.outputUri = outputUri
  }

  override fun equals(other: Any?): Boolean = event == other

  override fun hashCode(): Int = event.hashCode()
}
