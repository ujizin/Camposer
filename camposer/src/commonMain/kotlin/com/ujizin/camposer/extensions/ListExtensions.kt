package com.ujizin.camposer.extensions

internal inline fun <reified T> List<*>.firstIsInstanceOrNull(): T? = firstOrNull { it is T } as? T
