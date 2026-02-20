package com.developerstring.ketoy.annotation

/**
 * Marks a `@Composable` function as a **Ketoy custom component** that can be
 * referenced from server-driven JSON via its [name].
 *
 * When the Ketoy renderer encounters a JSON node whose `"type"` matches [name],
 * it delegates rendering to the annotated composable. The component must also
 * be registered in [com.developerstring.ketoy.registry.KComponentRegistry] at
 * runtime (either manually or via code-generation).
 *
 * ## Usage
 * ```kotlin
 * @KComponent(
 *     name = "UserCard",
 *     packageName = "com.myapp.widgets",
 *     description = "Displays a user’s name, avatar, and VIP badge.",
 *     version = "1.2"
 * )
 * @Composable
 * fun UserCardWidget(props: Map<String, Any>) {
 *     val name = props["name"] as? String ?: ""
 *     val isVip = props["isVip"] as? Boolean ?: false
 *     UserCard(name = name, isVip = isVip)
 * }
 * ```
 *
 * ## Corresponding JSON
 * ```json
 * {
 *   "type": "UserCard",
 *   "props": {
 *     "name": "Alice",
 *     "isVip": true
 *   }
 * }
 * ```
 *
 * @property name        The unique component type identifier used in JSON `"type"` fields.
 *                       Must be globally unique across the application.
 * @property packageName Optional fully-qualified package name for metadata and
 *                       reflection-based dynamic loading.
 * @property description Optional human-readable description shown in documentation
 *                       and the Ketoy DevTools component catalogue.
 * @property version     Semantic version of this component’s contract. Defaults to `"1.0"`.
 *                       Increment when the expected props change.
 * @see com.developerstring.ketoy.registry.KComponentRegistry
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KComponent(
    val name: String,
    val packageName: String = "",
    val description: String = "",
    val version: String = "1.0"
)
