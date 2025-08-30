package com.ujizin.camposer.state

public actual class QualitySelector(
) {

    public actual companion object {
        public actual fun from(
            quality: Quality
        ): QualitySelector = QualitySelector()
    }
}

public actual class Quality