package com.developerstring.ketoy.annotation

/**
 * Marks a composable function as a Ketoy custom component that can be
 * referenced from server-driven JSON.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KComponent(
    val name: String,
    val packageName: String = "",
    val description: String = "",
    val version: String = "1.0"
)
