package com.ujizin.camposer.extensions

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor

private class MainThreadExecutor(
  context: Context,
) : Executor {
  private val handler: Handler = Handler(context.mainLooper)

  override fun execute(r: Runnable) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      r.run()
      return
    }

    handler.post(r)
  }
}

internal tailrec fun Context.findLifecycleOwner(): LifecycleOwner =
  when (this) {
    is LifecycleOwner -> this
    is ContextWrapper -> baseContext.findLifecycleOwner()
    else -> error("No LifecycleOwner found in Context chain")
  }

internal val Context.compatMainExecutor: Executor
  get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    mainExecutor
  } else {
    MainThreadExecutor(this)
  }
