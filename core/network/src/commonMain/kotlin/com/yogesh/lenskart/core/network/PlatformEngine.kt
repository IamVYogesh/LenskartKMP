package com.yogesh.lenskart.core.network

import io.ktor.client.engine.HttpClientEngine

/** expect/actual #5: OkHttp on Android + desktop, Darwin (NSURLSession) on iOS. */
expect fun platformHttpEngine(): HttpClientEngine
