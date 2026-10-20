package com.yogesh.lenskart

import androidx.compose.ui.window.ComposeUIViewController

/** Called from Swift: `MainViewControllerKt.MainViewController()` */
fun MainViewController() = ComposeUIViewController(configure = { initKoin() }) { App() }
