package com.yogesh.lenskart

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.yogesh.lenskart.core.designsystem.LenskartTheme
import com.yogesh.lenskart.core.domain.model.ThemeMode
import com.yogesh.lenskart.core.domain.repository.SettingsRepository
import com.yogesh.lenskart.core.network.platformHttpEngine
import io.ktor.client.HttpClient
import org.koin.compose.koinInject

@Composable
fun App() {
    // Coil 3 gets its OWN real-network client (never the mock engine) built on the platform engine.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory(HttpClient(platformHttpEngine()))) }
            .crossfade(true)
            .build()
    }

    val settings: SettingsRepository = koinInject()
    val mode by settings.observeThemeMode().collectAsState(ThemeMode.SYSTEM)
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    LenskartTheme(darkTheme = dark) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainScaffold() }
    }
}
