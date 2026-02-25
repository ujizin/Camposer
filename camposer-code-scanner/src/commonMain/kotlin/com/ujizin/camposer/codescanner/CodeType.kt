package com.ujizin.camposer.codescanner

/**
 * Represents the types of codes that can be scanned.
 *
 * This enum defines the supported barcode and QR code formats for the code scanner. It is an
 * expected class, meaning the actual implementation may vary slightly between platform-specific
 * targets (like Android and iOS), but these common types are guaranteed.
 */
public expect enum class CodeType {
  QRCode,
  Barcode39,
  Barcode93,
  Barcode128,
  BarcodeEAN8,
  BarcodeEAN13,
  CodaBar,
  ITF,
  Aztec,
  DataMatrix,
  PDF417,
  UPCE,
  UPCA,
}
