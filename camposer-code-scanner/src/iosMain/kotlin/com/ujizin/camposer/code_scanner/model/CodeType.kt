package com.ujizin.camposer.code_scanner.model

import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode

public actual enum class CodeType(internal vararg val rawType: String?) {
    QRCode(AVMetadataObjectTypeQRCode),
    Barcode(
        AVMetadataObjectTypeCode39Code,
        AVMetadataObjectTypeCode93Code,
        AVMetadataObjectTypeCode128Code,
    );

    internal companion object {
        fun findByName(name: String?) = entries.firstOrNull { type ->
            !type.rawType.find { it == name }.isNullOrEmpty()
        }
    }
}