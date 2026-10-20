package com.yogesh.lenskart

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Lenskart KMP",
            state = rememberWindowState(width = 430.dp, height = 900.dp),
        ) { App() }
    }
}
