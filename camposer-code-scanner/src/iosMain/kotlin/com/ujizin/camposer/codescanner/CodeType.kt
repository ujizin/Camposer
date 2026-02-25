package com.ujizin.camposer.codescanner

import platform.AVFoundation.AVMetadataObjectTypeAztecCode
import platform.AVFoundation.AVMetadataObjectTypeCodabarCode
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeDataMatrixCode
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeInterleaved2of5Code
import platform.AVFoundation.AVMetadataObjectTypePDF417Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode

public actual enum class CodeType(
  internal val rawType: String?,
) {
  QRCode(AVMetadataObjectTypeQRCode),
  Barcode39(AVMetadataObjectTypeCode39Code),
  Barcode93(AVMetadataObjectTypeCode93Code),
  Barcode128(AVMetadataObjectTypeCode128Code),
  BarcodeEAN8(AVMetadataObjectTypeEAN8Code),
  BarcodeEAN13(AVMetadataObjectTypeEAN13Code),
  CodaBar(AVMetadataObjectTypeCodabarCode),
  ITF(AVMetadataObjectTypeInterleaved2of5Code),
  Aztec(AVMetadataObjectTypeAztecCode),
  DataMatrix(AVMetadataObjectTypeDataMatrixCode),
  PDF417(AVMetadataObjectTypePDF417Code),
  UPCE(AVMetadataObjectTypeUPCECode),
  UPCA(AVMetadataObjectTypeEAN13Code),
  ;

  internal companion object {
    fun findByName(name: String?) = entries.firstOrNull { type -> type.rawType == name }
  }
}
