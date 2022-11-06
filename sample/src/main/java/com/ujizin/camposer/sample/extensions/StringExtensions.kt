package com.ujizin.camposer.sample.extensions

fun String.capitalize() = replaceFirstChar { it.uppercase() }