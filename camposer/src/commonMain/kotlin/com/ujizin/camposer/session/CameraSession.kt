package com.ujizin.camposer.session

import com.ujizin.camposer.config.CameraConfig
import com.ujizin.camposer.info.CameraInfo

public expect class CameraSession {

    public val config: CameraConfig
    public val info: CameraInfo

    /**
     * Check if camera is streaming or not.
     * */
    public var isStreaming: Boolean
        internal set

    /**
     * Check if camera state is initialized or not.
     * */
    public var isInitialized: Boolean
        private set
}
