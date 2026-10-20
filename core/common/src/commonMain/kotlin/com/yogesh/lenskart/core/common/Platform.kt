package com.yogesh.lenskart.core.common

/** expect/actual #2 (class with constructor + members). */
expect class Platform() {
    val name: String
    val version: String
}

/** expect/actual #3 (top-level function). */
expect fun randomUuid(): String
