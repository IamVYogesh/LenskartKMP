package com.yogesh.lenskart.core.common

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.dispatch_get_main_queue

actual val commonPlatformModule: Module = module {
    single { Platform() }
    single<NetworkMonitor> { IosNetworkMonitor() }
    single<ShareService> { IosShareService() }
}

/** Uses Apple's Network.framework through Kotlin/Native cinterop (no extra library needed). */
@OptIn(ExperimentalForeignApi::class)
internal class IosNetworkMonitor : NetworkMonitor {
    override val isOnline: Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            trySend(path != null && nw_path_get_status(path) == nw_path_status_satisfied)
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }.distinctUntilChanged()
}

internal class IosShareService : ShareService {
    override fun shareText(text: String) {
        val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        val root = (UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow)?.rootViewController
        root?.presentViewController(controller, animated = true, completion = null)
    }
}
