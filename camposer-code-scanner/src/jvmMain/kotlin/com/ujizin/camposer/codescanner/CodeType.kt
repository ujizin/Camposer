package com.ujizin.camposer.codescanner

import com.google.zxing.BarcodeFormat

public actual enum class CodeType(
  internal val format: BarcodeFormat,
) {
  QRCode(BarcodeFormat.QR_CODE),
  Barcode39(BarcodeFormat.CODE_39),
  Barcode93(BarcodeFormat.CODE_93),
  Barcode128(BarcodeFormat.CODE_128),
  BarcodeEAN8(BarcodeFormat.EAN_8),
  BarcodeEAN13(BarcodeFormat.EAN_13),
  CodaBar(BarcodeFormat.CODABAR),
  ITF(BarcodeFormat.ITF),
  Aztec(BarcodeFormat.AZTEC),
  DataMatrix(BarcodeFormat.DATA_MATRIX),
  PDF417(BarcodeFormat.PDF_417),
  UPCE(BarcodeFormat.UPC_E),
  UPCA(BarcodeFormat.UPC_A),
  ;

  internal companion object {
    fun fromFormat(format: BarcodeFormat): CodeType? = entries.firstOrNull { it.format == format }
  }
}
