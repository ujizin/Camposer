package br.com.devlucasyuji.sample.extensions

fun String.capitalize() = replaceFirstChar { it.uppercase() }