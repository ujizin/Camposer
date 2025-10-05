package com.ujizin.camposer.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T> config(
    value: T,
    predicate: (old: T, new: T) -> Boolean = { old, new -> old != new },
    onDispose: (old: T) -> Unit = {},
    block: (new: T) -> Unit = {},
): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
    private var currentValue by mutableStateOf(value)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = currentValue

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (!predicate(currentValue, value)) return
        onDispose(currentValue)
        currentValue = value
        block(value)
    }
}
