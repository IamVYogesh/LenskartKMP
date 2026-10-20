package com.yogesh.lenskart.feature.tryon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * expect/actual #6 — a COMPOSABLE.
 *  - Android: CameraX PreviewView hosted with AndroidView + runtime permission
 *  - iOS    : AVCaptureVideoPreviewLayer hosted with UIKitView (native UIKit interop)
 *  - Desktop: placeholder
 */
@Composable
expect fun PlatformCameraPreview(modifier: Modifier = Modifier, onPermissionResult: (Boolean) -> Unit = {})
