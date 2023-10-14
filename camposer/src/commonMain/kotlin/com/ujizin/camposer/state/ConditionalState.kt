package com.ujizin.camposer.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
internal class ConditionalState<T : Any>(
    startValue: T,
    private val defaultValue: T,
) : MutableState<T> {

    internal var predicate: Boolean = true

    private fun getMode(value: T): T = when {
        predicate -> value
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
        internal fun <T : Any> getNoSaver(
            startValue: T,
            defaultValue: T
        ): Saver<ConditionalState<T>, *> = listSaver(
            save = { listOf(startValue, defaultValue) },
            restore = { ConditionalState(it[0], it[1]) }
        )

        internal fun <T : Any> getSaver(): Saver<ConditionalState<T>, *> = listSaver(
            save = { listOf(it._value, it.defaultValue) },
            restore = { ConditionalState(it[0], it[1]) }
        )
    }
}

/**
 * Remember with [ConditionalState].
 *
 * @param initialValue The initial value of state.
 * @param defaultValue The Default value of state, if predicate is false then it's applied it.
 * @param useSaver if true saves the latest value on state.
 * @param predicate conditional to get actual value or default value.
 * */
@Composable
internal fun <T : Any> rememberConditionalState(
    initialValue: T,
    defaultValue: T,
    useSaver: Boolean,
    predicate: Boolean
): MutableState<T> {
    var current by remember { mutableStateOf(initialValue) }
    return rememberSaveable(
        predicate, saver = when (useSaver) {
            true -> ConditionalState.getSaver()
            else -> ConditionalState.getNoSaver(current, defaultValue)
        }
    ) {
        ConditionalState(current, defaultValue).apply {
            this.predicate = predicate
            current = value
        }
    }
}
