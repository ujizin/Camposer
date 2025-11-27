package com.ujizin.camposer.code_scanner

import com.google.mlkit.vision.barcode.common.Barcode

public actual enum class CodeType(internal val barcode: Int) {
    QRCode(Barcode.FORMAT_QR_CODE),
    Barcode39(Barcode.FORMAT_CODE_39),
    Barcode93(Barcode.FORMAT_CODE_93),
    Barcode128(Barcode.FORMAT_CODE_128),
    BarcodeEAN8(Barcode.FORMAT_EAN_8),
    BarcodeEAN13(Barcode.FORMAT_EAN_13),
    CodaBar(Barcode.FORMAT_CODABAR),
    ITF(Barcode.FORMAT_ITF),
    Aztec(Barcode.FORMAT_AZTEC),
    DataMatrix(Barcode.FORMAT_DATA_MATRIX),
    PDF417(Barcode.FORMAT_PDF417),
    UPCE(Barcode.FORMAT_UPC_E),
    UPCA(Barcode.FORMAT_UPC_A);

    internal companion object {
        fun fromBarcode(barcode: Int): CodeType? = entries.find { it.barcode == barcode }
    }
}
