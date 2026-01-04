package com.ujizin.camposer.internal.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T> asyncDistinctConfig(
  mutex: Mutex,
  value: T,
  check: (T) -> Unit = {},
  dispatcher: CoroutineDispatcher = Dispatchers.IO,
  predicate: (old: T, new: T) -> Boolean = { old, new -> old != new },
  onDispose: (old: T) -> Unit = {},
  onSet: (field: T) -> T = { it },
  block: (new: T) -> Unit = {},
): ReadWriteProperty<Any?, T> =
  object : ReadWriteProperty<Any?, T> {
    private var currentValue by mutableStateOf(value)

    private var job: Job? = null

    override fun getValue(
      thisRef: Any?,
      property: KProperty<*>,
    ): T = currentValue

    override fun setValue(
      thisRef: Any?,
      property: KProperty<*>,
      value: T,
    ) {
      if (!predicate(currentValue, value)) return
      check(value)
      val tmpValue = currentValue
      currentValue = onSet(value)
      job?.cancel()
      job = CoroutineScope(dispatcher).launch {
        mutex.withLock {
          withContext(NonCancellable) {
            onDispose(tmpValue)
            block(currentValue)
          }
        }
      }
    }
  }
