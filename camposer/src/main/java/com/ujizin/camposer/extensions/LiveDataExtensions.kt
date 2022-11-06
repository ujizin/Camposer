package com.ujizin.camposer.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal fun <T> LiveData<T>.observeLatest(
    lifecycleOwner: LifecycleOwner,
    debounceTimeMillis: Long = 10L,
    block: (T) -> Unit
) {
    var job: Job? = null
    observe(lifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            job?.cancel()
            job = async(debounceTimeMillis) { block(it) }
        }
    }
}
