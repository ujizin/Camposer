package br.com.devlucasyuji.camposer.extensions

import android.content.Context
import androidx.lifecycle.LifecycleOwner

internal fun LifecycleOwner.asContext(): Context = this as Context
