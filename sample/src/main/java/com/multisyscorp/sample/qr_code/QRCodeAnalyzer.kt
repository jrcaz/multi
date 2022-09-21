package com.multisyscorp.sample.qr_code

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.multisyscorp.multi.extensions.toByteArray

class QRCodeAnalyzer(
    private val supportedImageFormats: List<Int>,
    private val onQRCodeScanned: (String?) -> Unit,
    private val onException: () -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        when (image.format) {
            in supportedImageFormats -> {
                val bytes = image.planes.firstOrNull()?.buffer?.toByteArray()
                val source = PlanarYUVLuminanceSource(
                    bytes,
                    image.width,
                    image.height,
                    0,
                    0,
                    image.width,
                    image.height,
                    false
                )
                val binaryBmp = BinaryBitmap(HybridBinarizer(source))
                try {
                    val result = MultiFormatReader().apply {
                        setHints(
                            mapOf(
                                DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                                    BarcodeFormat.QR_CODE
                                )
                            )
                        )
                    }.decode(binaryBmp)
                    onQRCodeScanned(result.text)
                } catch (_: Exception) {
                    onException()
                } finally {
                    image.close()
                }
            }
        }
    }
}