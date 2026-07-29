package com.ujizin.camposer.permissions.internal

import android.content.Context
import android.content.ContextWrapper
import com.ujizin.camposer.permissions.fake.FakeActivity
import kotlin.test.Test
import kotlin.test.assertSame

internal class ContextExtensionsTest {
  @Test
  fun activityContextReturnsItself() {
    val activity = FakeActivity(isGranted = true)

    assertSame(activity, activity.findActivity())
  }

  @Test
  fun contextWrapperReturnsWrappedActivity() {
    val activity = FakeActivity(isGranted = true)

    assertSame(activity, FakeContextWrapper(activity).findActivity())
  }

  @Test
  fun nestedContextWrappersReturnWrappedActivity() {
    val activity = FakeActivity(isGranted = true)
    val nested = FakeContextWrapper(FakeContextWrapper(activity))

    assertSame(activity, nested.findActivity())
  }

  // getBaseContext() is null under the JVM stub.
  private class FakeContextWrapper(
    private val base: Context,
  ) : ContextWrapper(base) {
    override fun getBaseContext(): Context = base
  }
}
