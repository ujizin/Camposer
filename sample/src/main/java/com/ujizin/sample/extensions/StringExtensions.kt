package com.ujizin.sample.extensions

fun String.capitalize() = replaceFirstChar { it.uppercase() }