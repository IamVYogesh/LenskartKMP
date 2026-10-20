package com.yogesh.lenskart.feature.tryon

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformCameraPreview(modifier: Modifier, onPermissionResult: (Boolean) -> Unit) {
    var granted by remember {
        mutableStateOf(AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized)
    }
    LaunchedEffect(Unit) {
        if (granted) onPermissionResult(true)
        else AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { ok -> granted = ok; onPermissionResult(ok) }
    }
    if (!granted) return

    val session = remember { AVCaptureSession() }
    val previewLayer = remember {
        AVCaptureVideoPreviewLayer(session = session).apply { videoGravity = AVLayerVideoGravityResizeAspectFill }
    }
    DisposableEffect(Unit) {
        val device = AVCaptureDevice.defaultDeviceWithDeviceType(
            AVCaptureDeviceTypeBuiltInWideAngleCamera, AVMediaTypeVideo, AVCaptureDevicePositionFront,
        )
        val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, null) }
        if (input != null && session.canAddInput(input)) session.addInput(input)
        session.startRunning()
        onDispose { session.stopRunning() }
    }
    UIKitView(
        factory = { UIView().apply { layer.addSublayer(previewLayer) } },
        modifier = modifier.fillMaxSize(),
        onResize = { view, rect ->
            CATransaction.begin()
            CATransaction.setValue(true, forKey = kCATransactionDisableActions)
            view.layer.setFrame(rect)
            previewLayer.setFrame(rect)
            CATransaction.commit()
        },
    )
}
