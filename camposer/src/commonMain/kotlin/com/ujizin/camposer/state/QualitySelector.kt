package com.ujizin.camposer.state

public expect class QualitySelector {

    public companion object {
        public fun from(quality: Quality): QualitySelector
    }
}

public expect class Quality