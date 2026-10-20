package com.yogesh.lenskart.feature.tryon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yogesh.lenskart.core.designsystem.resources.Res
import com.yogesh.lenskart.core.designsystem.resources.tryon_no_camera
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun PlatformCameraPreview(modifier: Modifier, onPermissionResult: (Boolean) -> Unit) {
    LaunchedEffect(Unit) { onPermissionResult(false) }
    Box(modifier.fillMaxSize(), Alignment.Center) { Text(stringResource(Res.string.tryon_no_camera), color = Color.White) }
}
