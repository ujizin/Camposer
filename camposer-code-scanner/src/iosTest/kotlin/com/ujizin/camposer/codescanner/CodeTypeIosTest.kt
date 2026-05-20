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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CodeTypeIosTest {
  @Test
  fun findByName_QRCode() {
    assertEquals(CodeType.QRCode, CodeType.findByName(AVMetadataObjectTypeQRCode))
  }

  @Test
  fun findByName_Barcode39() {
    assertEquals(CodeType.Barcode39, CodeType.findByName(AVMetadataObjectTypeCode39Code))
  }

  @Test
  fun findByName_Barcode93() {
    assertEquals(CodeType.Barcode93, CodeType.findByName(AVMetadataObjectTypeCode93Code))
  }

  @Test
  fun findByName_Barcode128() {
    assertEquals(CodeType.Barcode128, CodeType.findByName(AVMetadataObjectTypeCode128Code))
  }

  @Test
  fun findByName_BarcodeEAN8() {
    assertEquals(CodeType.BarcodeEAN8, CodeType.findByName(AVMetadataObjectTypeEAN8Code))
  }

  @Test
  fun findByName_BarcodeEAN13_takes_priority_over_UPCA_for_ean13_type_string() {
    // BarcodeEAN13 is declared before UPCA in the enum; firstOrNull returns BarcodeEAN13.
    // This is intentional — UPC-A is a subset of EAN-13; AVFoundation does not distinguish them.
    assertEquals(CodeType.BarcodeEAN13, CodeType.findByName(AVMetadataObjectTypeEAN13Code))
  }

  @Test
  fun findByName_CodaBar() {
    assertEquals(CodeType.CodaBar, CodeType.findByName(AVMetadataObjectTypeCodabarCode))
  }

  @Test
  fun findByName_ITF() {
    assertEquals(CodeType.ITF, CodeType.findByName(AVMetadataObjectTypeInterleaved2of5Code))
  }

  @Test
  fun findByName_Aztec() {
    assertEquals(CodeType.Aztec, CodeType.findByName(AVMetadataObjectTypeAztecCode))
  }

  @Test
  fun findByName_DataMatrix() {
    assertEquals(CodeType.DataMatrix, CodeType.findByName(AVMetadataObjectTypeDataMatrixCode))
  }

  @Test
  fun findByName_PDF417() {
    assertEquals(CodeType.PDF417, CodeType.findByName(AVMetadataObjectTypePDF417Code))
  }

  @Test
  fun findByName_UPCE() {
    assertEquals(CodeType.UPCE, CodeType.findByName(AVMetadataObjectTypeUPCECode))
  }

  @Test
  fun findByName_returns_null_for_unknown_string() {
    assertNull(CodeType.findByName("com.unknown.format"))
  }

  @Test
  fun findByName_returns_null_for_null_input() {
    assertNull(CodeType.findByName(null))
  }
}
