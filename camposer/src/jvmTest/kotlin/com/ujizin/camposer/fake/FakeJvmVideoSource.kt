package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.jvm.JvmFrame
import com.ujizin.camposer.internal.core.jvm.JvmVideoProperty
import com.ujizin.camposer.internal.core.jvm.JvmVideoSource

internal class FakeJvmVideoSource(
  var availableIndexes: Set<Int> = setOf(0),
  override var backendName: String = "AVFOUNDATION",
  var framesToDeliver: Int = Int.MAX_VALUE,
) : JvmVideoSource {
  val properties = mutableMapOf(
    JvmVideoProperty.FrameWidth to 1280.0,
    JvmVideoProperty.FrameHeight to 720.0,
    JvmVideoProperty.FrameRate to 30.0,
    JvmVideoProperty.Exposure to 0.0,
    JvmVideoProperty.AutoExposure to 1.0,
  )

  val setCalls = mutableListOf<Pair<JvmVideoProperty, Double>>()

  var openedIndex: Int? = null
    private set

  var isReleased = false
    private set

  var framesRead = 0
    private set

  val frame = FakeJvmFrame()

  override var isOpen: Boolean = false
    private set

  override fun open(deviceIndex: Int): Boolean {
    if (deviceIndex !in availableIndexes) return false
    openedIndex = deviceIndex
    isOpen = true
    return true
  }

  override fun read(): JvmFrame? {
    if (!isOpen || framesRead >= framesToDeliver) return null
    framesRead++
    return frame
  }

  override fun get(property: JvmVideoProperty): Double = properties[property] ?: 0.0

  override fun set(
    property: JvmVideoProperty,
    value: Double,
  ): Boolean {
    setCalls += property to value
    properties[property] = value
    return true
  }

  override fun release() {
    isOpen = false
    isReleased = true
  }

  class Factory(
    private val sources: ArrayDeque<FakeJvmVideoSource>,
  ) : JvmVideoSource.Factory {
    constructor(vararg sources: FakeJvmVideoSource) : this(ArrayDeque(sources.toList()))

    val created = mutableListOf<FakeJvmVideoSource>()

    override fun create(): JvmVideoSource {
      val source = sources.removeFirstOrNull() ?: FakeJvmVideoSource()
      created += source
      return source
    }
  }
}
