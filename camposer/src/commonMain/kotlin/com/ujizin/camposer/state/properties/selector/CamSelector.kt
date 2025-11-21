package com.ujizin.camposer.state.properties.selector

public expect class CamSelector {

    public val isFront: Boolean

    public companion object {
        public val Front: CamSelector
        public val Back: CamSelector
    }
}