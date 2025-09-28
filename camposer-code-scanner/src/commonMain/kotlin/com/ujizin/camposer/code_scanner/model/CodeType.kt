package com.ujizin.camposer.code_scanner.model

public expect enum class CodeType {
    QRCode,
    Barcode39,
    Barcode93,
    Barcode128,
    BarcodeEAN8,
    BarcodeEAN13,
    DataBarGS1,
    DataBarGS1Limited,
    DataBarGS1Expanded,
    CodaBar,
    ITF,
    ITF14,
    Aztec,
    DataMatrix,
    PDF417,
    UPCE,
    UPCA,
}
