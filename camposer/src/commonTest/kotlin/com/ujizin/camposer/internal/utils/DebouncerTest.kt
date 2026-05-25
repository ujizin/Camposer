package com.ujizin.camposer.internal.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class DebouncerTest {
  @Test
  fun test_block_executes_after_duration() =
    runTest {
      val debouncer = Debouncer(duration = 100.milliseconds, scope = this)
      var executed = false

      debouncer.submit { executed = true }
      assertFalse(executed)

      advanceTimeBy(101)
      assertTrue(executed)
    }

  @Test
  fun test_block_does_not_execute_before_duration() =
    runTest {
      val debouncer = Debouncer(duration = 100.milliseconds, scope = this)
      var executed = false

      debouncer.submit { executed = true }
      advanceTimeBy(50)

      assertFalse(executed)
    }

  @Test
  fun test_rapid_submits_only_last_block_executes() =
    runTest {
      val debouncer = Debouncer(duration = 100.milliseconds, scope = this)
      var callCount = 0

      debouncer.submit { callCount++ }
      debouncer.submit { callCount++ }
      debouncer.submit { callCount++ }
      advanceTimeBy(200)

      assertEquals(1, callCount)
    }

  @Test
  fun test_second_submit_resets_delay() =
    runTest {
      val debouncer = Debouncer(duration = 100.milliseconds, scope = this)
      var executed = false

      debouncer.submit { executed = true }
      advanceTimeBy(80) // almost there, but not yet

      debouncer.submit { executed = true } // reset
      advanceTimeBy(80) // still before new deadline

      assertFalse(executed)

      advanceTimeBy(50) // now past new deadline
      assertTrue(executed)
    }

  @Test
  fun test_independent_submits_both_execute() =
    runTest {
      val debouncer = Debouncer(duration = 100.milliseconds, scope = this)
      var callCount = 0

      debouncer.submit { callCount++ }
      advanceTimeBy(200) // first fires

      debouncer.submit { callCount++ }
      advanceTimeBy(200) // second fires

      assertEquals(2, callCount)
    }
}
