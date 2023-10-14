package com.ujizin.camposer.state

public expect enum class CamSelector {
    Front,
    Back,
}

/**
 * Inverse camera selector. Works only with default Front & Back Selector.
 * */
public val CamSelector.inverse: CamSelector
    get() = when (this) {
        CamSelector.Front -> CamSelector.Back
        CamSelector.Back -> CamSelector.Front
    }
