package br.com.devlucasyuji.sample.extensions

val Int.minutes get() = (this / 60).toString().padStart(2, '0')

val Int.seconds get() = (this % 60).toString().padStart(2, '0')