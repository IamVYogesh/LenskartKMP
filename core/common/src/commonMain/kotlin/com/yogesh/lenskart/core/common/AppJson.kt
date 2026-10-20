package com.yogesh.lenskart.core.common

import kotlinx.serialization.json.Json

/** One shared Json configuration for network DTOs, DataStore blobs and DB JSON columns. */
val AppJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
}
