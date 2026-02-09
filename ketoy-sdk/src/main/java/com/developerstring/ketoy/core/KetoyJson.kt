package com.developerstring.ketoy.core

import kotlinx.serialization.json.Json

/**
 * Global [Json] instance used throughout the SDK for consistent
 * serialization / deserialization.
 */
val KetoyJson: Json = Json {
    prettyPrint = true
    encodeDefaults = false
    ignoreUnknownKeys = true
}
