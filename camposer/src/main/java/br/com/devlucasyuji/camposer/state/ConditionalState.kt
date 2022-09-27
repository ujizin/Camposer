package br.com.devlucasyuji.camposer.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class ConditionalState<T>(
    startWith: T,
    private val defaultValue: T,
    private val predicate: () -> Boolean,
) : MutableState<T> {
    private fun getMode(flashMode: T): T = when {
        predicate() -> flashMode
        else -> defaultValue
    }

    private var _value by mutableStateOf(getMode(startWith))
    override var value: T
        get() = getMode(_value)
        set(value) {
            _value = getMode(value)
        }

    override fun component1(): T = _value

    override fun component2(): (T) -> Unit = { _value = it }
}