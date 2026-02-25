package com.ujizin.camposer.internal.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

internal class Debouncer(
  private val duration: Duration,
  private val scope: CoroutineScope = MainScope(),
) {
  private var job: Job? = null

  fun submit(block: suspend () -> Unit) {
    job?.cancel()
    job =
      scope.launch {
        delay(duration)
        block()
      }
  }
}
