package com.ujizin.camposer.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T> config(
    value: T,
    check: (new: T) -> Unit = {},
    predicate: (old: T, new: T) -> Boolean = { old, new -> old != new },
    onDispose: (old: T) -> Unit = {},
    onSet: (field: T) -> T = { it },
    block: (new: T) -> Unit = {},
): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
    private var currentValue by mutableStateOf(value)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = currentValue

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (!predicate(currentValue, value)) return
        check(value)
        onDispose(currentValue)
        currentValue = onSet(value)
        block(currentValue)
    }
}
