package com.multisyscorp.multi.helpers

import android.content.Context
import android.graphics.Bitmap
import com.multisyscorp.multi.extensions.toList
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

object QRCodeHelper {

    fun decodeBitmap(
        context: Context,
        bitmap: Bitmap,
        onResult: (String?) -> Unit,
        onError: () -> Unit,
        onNotFound: () -> Unit,
        onException: () -> Unit
    ) {
        try {
            val barcodeDetector = BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.QR_CODE)
                .build()

            when {
                !barcodeDetector.isOperational -> {
                    onError()
                }
            }

            val frame = Frame.Builder().setBitmap(bitmap).build()
            val barcodes = barcodeDetector.detect(frame).toList()

            barcodes?.firstOrNull()?.rawValue?.let { rawValue ->
                onResult(rawValue)
            } ?: run {
                onNotFound()
            }
        } catch (_: Exception) {
            onException()
        }
    }

}