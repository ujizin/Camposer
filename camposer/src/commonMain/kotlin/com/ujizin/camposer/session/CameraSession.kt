package com.ujizin.camposer.session

import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.CameraState

public expect class CameraSession {

    public val state: CameraState

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
