package com.ujizin.camposer.codescanner

import com.google.mlkit.vision.barcode.common.Barcode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CodeTypeAndroidTest {
  @Test
  fun fromBarcode_roundtrip_allEntries() {
    CodeType.entries.forEach { type ->
      assertEquals(type, CodeType.fromBarcode(type.barcode))
    }
  }

  @Test
  fun fromBarcode_unknownFormat_returnsNull() {
    assertNull(CodeType.fromBarcode(Barcode.FORMAT_UNKNOWN))
  }

  @Test
  fun fromBarcode_unmappedValue_returnsNull() {
    assertNull(CodeType.fromBarcode(-1))
  }

  @Test
  fun fromBarcode_qrCode_mapsCorrectly() {
    assertEquals(CodeType.QRCode, CodeType.fromBarcode(Barcode.FORMAT_QR_CODE))
  }

  @Test
  fun fromBarcode_aztec_mapsCorrectly() {
    assertEquals(CodeType.Aztec, CodeType.fromBarcode(Barcode.FORMAT_AZTEC))
  }

  @Test
  fun fromBarcode_ean13_mapsCorrectly() {
    assertEquals(CodeType.BarcodeEAN13, CodeType.fromBarcode(Barcode.FORMAT_EAN_13))
  }

  @Test
  fun fromBarcode_ean8_mapsCorrectly() {
    assertEquals(CodeType.BarcodeEAN8, CodeType.fromBarcode(Barcode.FORMAT_EAN_8))
  }

  @Test
  fun fromBarcode_upca_mapsCorrectly() {
    assertEquals(CodeType.UPCA, CodeType.fromBarcode(Barcode.FORMAT_UPC_A))
  }

  @Test
  fun fromBarcode_upce_mapsCorrectly() {
    assertEquals(CodeType.UPCE, CodeType.fromBarcode(Barcode.FORMAT_UPC_E))
  }

  @Test
  fun fromBarcode_code128_mapsCorrectly() {
    assertEquals(CodeType.Barcode128, CodeType.fromBarcode(Barcode.FORMAT_CODE_128))
  }

  @Test
  fun fromBarcode_code39_mapsCorrectly() {
    assertEquals(CodeType.Barcode39, CodeType.fromBarcode(Barcode.FORMAT_CODE_39))
  }

  @Test
  fun fromBarcode_code93_mapsCorrectly() {
    assertEquals(CodeType.Barcode93, CodeType.fromBarcode(Barcode.FORMAT_CODE_93))
  }

  @Test
  fun fromBarcode_codabar_mapsCorrectly() {
    assertEquals(CodeType.CodaBar, CodeType.fromBarcode(Barcode.FORMAT_CODABAR))
  }

  @Test
  fun fromBarcode_itf_mapsCorrectly() {
    assertEquals(CodeType.ITF, CodeType.fromBarcode(Barcode.FORMAT_ITF))
  }

  @Test
  fun fromBarcode_pdf417_mapsCorrectly() {
    assertEquals(CodeType.PDF417, CodeType.fromBarcode(Barcode.FORMAT_PDF417))
  }

  @Test
  fun fromBarcode_dataMatrix_mapsCorrectly() {
    assertEquals(CodeType.DataMatrix, CodeType.fromBarcode(Barcode.FORMAT_DATA_MATRIX))
  }
}
