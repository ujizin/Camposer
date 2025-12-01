package com.ujizin.camposer.state.properties

public expect enum class ImplementationMode {
  Compatible,
  Performance,
}

/**
 * Inverse currently implementation mode.
 * */
public val ImplementationMode.inverse: ImplementationMode
  get() = when (this) {
    ImplementationMode.Compatible -> ImplementationMode.Performance
    else -> ImplementationMode.Compatible
  }
