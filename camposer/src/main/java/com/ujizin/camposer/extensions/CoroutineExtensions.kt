package com.ujizin.camposer.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

internal fun CoroutineScope.async(
    debounceTimeMillis: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: () -> Unit
): Job = async(dispatcher) {
    delay(debounceTimeMillis)
    block()
}
