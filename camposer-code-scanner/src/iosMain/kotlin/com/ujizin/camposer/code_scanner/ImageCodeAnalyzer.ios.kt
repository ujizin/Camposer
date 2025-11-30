package com.ujizin.camposer.code_scanner

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.pointed
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.CoreGraphics.CGPointMake
import platform.Foundation.NSDictionary
import platform.darwin.NSObject

internal actual class ImageCodeAnalyzer(
    private val previewLayer: AVCaptureVideoPreviewLayer,
    private val types: List<CodeType>,
    private val codeAnalyzerListener: CodeAnalyzerListener,
) {

    @OptIn(ExperimentalForeignApi::class)
    internal val delegate: AVCaptureMetadataOutputObjectsDelegateProtocol =
        object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputMetadataObjects: List<*>,
                fromConnection: AVCaptureConnection,
            ) {

                didOutputMetadataObjects.forEach { obj ->
                    val readable = obj as? AVMetadataMachineReadableCodeObject ?: return@forEach
                    val type = CodeType.findByName(readable.type) ?: return@forEach
                    val text = readable.stringValue ?: return@forEach
                    val frameRect = readable.getFrameRect()
                    val corners = readable.getCornerPointers()

                    codeAnalyzerListener.onCodeScanned(
                        CodeResult(
                            type = type,
                            text = text,
                            frameRect = frameRect,
                            corners = corners
                        )
                    )
                }
            }

            // IDK but in KMP corners returns NSDictionary instead of CGPoint
            private fun AVMetadataMachineReadableCodeObject.getCornerPointers(
            ): List<CornerPointer> = corners.filterIsInstance<NSDictionary>().map { dict ->
                CGPointMake(
                    x = dict.objectForKey("X") as Double,
                    y = dict.objectForKey("Y") as Double,
                )
            }.map { point ->
                previewLayer.pointForCaptureDevicePointOfInterest(point).useContents {
                    CornerPointer(x.toInt(), y.toInt())
                }
            }

            private fun AVMetadataMachineReadableCodeObject.getFrameRect(): FrameRect = memScoped {
                val boundsRef = previewLayer.transformedMetadataObjectForMetadataObject(
                    metadataObject = this@getFrameRect
                )?.bounds ?: return@memScoped FrameRect(0, 0, 0, 0)
                val bounds = boundsRef.placeTo(this).pointed

                FrameRect(
                    left = bounds.origin.x.toInt(),
                    top = bounds.origin.y.toInt(),
                    right = (bounds.origin.x + bounds.size.width).toInt(),
                    bottom = (bounds.origin.y + bounds.size.height).toInt(),
                )
            }
        }
}
