package com.yogesh.lenskart.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.module.Module
import org.koin.dsl.module
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual val commonPlatformModule: Module = module {
    single { Platform() }
    single<NetworkMonitor> { object : NetworkMonitor { override val isOnline: Flow<Boolean> = flowOf(true) } }
    // Desktop has no share sheet: copy to clipboard instead.
    single<ShareService> {
        object : ShareService {
            override fun shareText(text: String) {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
            }
        }
    }
}
