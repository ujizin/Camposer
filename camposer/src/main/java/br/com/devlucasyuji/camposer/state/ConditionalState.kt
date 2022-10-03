package br.com.devlucasyuji.camposer.state

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue

internal class ConditionalState<T>(
    startValue: T,
    private val defaultValue: T,
    private val predicate: () -> Boolean,
) : MutableState<T> {
    private fun getMode(value: T): T = when {
        predicate() -> value
        else -> defaultValue
    }

    private var _value by mutableStateOf(getMode(startValue))
    override var value: T
        get() = getMode(_value)
        set(value) {
            _value = getMode(value)
        }

    override fun component1(): T = _value

    override fun component2(): (T) -> Unit = { _value = it }

    companion object {
        internal fun <T> getSaver(predicate: () -> Boolean): Saver<ConditionalState<T>, out Any> =
            listSaver(
                save = { listOf(it._value, it.defaultValue) },
                restore = { ConditionalState(it[0], it[1], predicate) }
            )
    }
}