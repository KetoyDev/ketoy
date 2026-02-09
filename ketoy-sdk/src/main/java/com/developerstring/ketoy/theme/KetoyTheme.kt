package com.developerstring.ketoy.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Ketoy's own colour scheme that maps semantic tokens (e.g. `"primary"`)
 * to actual [Color] values.  Users can provide a custom instance or let
 * [KetoyThemeProvider] derive one from the active [MaterialTheme].
 */
data class KetoyColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val inversePrimary: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val surfaceTint: Color,
) {
    /**
     * Resolve a theme token name (e.g. `"primary"`, `"onSurface"`) to
     * the corresponding [Color], or `null` if the token is unknown.
     */
    fun resolve(token: String): Color? = when (token) {
        "primary"              -> primary
        "onPrimary"            -> onPrimary
        "primaryContainer"     -> primaryContainer
        "onPrimaryContainer"   -> onPrimaryContainer
        "secondary"            -> secondary
        "onSecondary"          -> onSecondary
        "secondaryContainer"   -> secondaryContainer
        "onSecondaryContainer" -> onSecondaryContainer
        "tertiary"             -> tertiary
        "onTertiary"           -> onTertiary
        "tertiaryContainer"    -> tertiaryContainer
        "onTertiaryContainer"  -> onTertiaryContainer
        "error"                -> error
        "onError"              -> onError
        "errorContainer"       -> errorContainer
        "onErrorContainer"     -> onErrorContainer
        "background"           -> background
        "onBackground"         -> onBackground
        "surface"              -> surface
        "onSurface"            -> onSurface
        "surfaceVariant"       -> surfaceVariant
        "onSurfaceVariant"     -> onSurfaceVariant
        "outline"              -> outline
        "outlineVariant"       -> outlineVariant
        "inversePrimary"       -> inversePrimary
        "inverseSurface"       -> inverseSurface
        "inverseOnSurface"     -> inverseOnSurface
        "surfaceTint"          -> surfaceTint
        else                   -> null
    }

    companion object {
        /**
         * Build a [KetoyColorScheme] from a Material3 [ColorScheme].
         */
        fun fromMaterial(cs: ColorScheme): KetoyColorScheme = KetoyColorScheme(
            primary              = cs.primary,
            onPrimary            = cs.onPrimary,
            primaryContainer     = cs.primaryContainer,
            onPrimaryContainer   = cs.onPrimaryContainer,
            secondary            = cs.secondary,
            onSecondary          = cs.onSecondary,
            secondaryContainer   = cs.secondaryContainer,
            onSecondaryContainer = cs.onSecondaryContainer,
            tertiary             = cs.tertiary,
            onTertiary           = cs.onTertiary,
            tertiaryContainer    = cs.tertiaryContainer,
            onTertiaryContainer  = cs.onTertiaryContainer,
            error                = cs.error,
            onError              = cs.onError,
            errorContainer       = cs.errorContainer,
            onErrorContainer     = cs.onErrorContainer,
            background           = cs.background,
            onBackground         = cs.onBackground,
            surface              = cs.surface,
            onSurface            = cs.onSurface,
            surfaceVariant       = cs.surfaceVariant,
            onSurfaceVariant     = cs.onSurfaceVariant,
            outline              = cs.outline,
            outlineVariant       = cs.outlineVariant,
            inversePrimary       = cs.inversePrimary,
            inverseSurface       = cs.inverseSurface,
            inverseOnSurface     = cs.inverseOnSurface,
            surfaceTint          = cs.surfaceTint,
        )
    }
}

/**
 * CompositionLocal that provides the current [KetoyColorScheme].
 *
 * If no [KetoyThemeProvider] is installed the default is a "stub" scheme
 * where every slot is [Color.Unspecified], so callers always fall back to
 * their own defaults.
 */
val LocalKetoyColors = staticCompositionLocalOf {
    KetoyColorScheme(
        primary = Color.Unspecified, onPrimary = Color.Unspecified,
        primaryContainer = Color.Unspecified, onPrimaryContainer = Color.Unspecified,
        secondary = Color.Unspecified, onSecondary = Color.Unspecified,
        secondaryContainer = Color.Unspecified, onSecondaryContainer = Color.Unspecified,
        tertiary = Color.Unspecified, onTertiary = Color.Unspecified,
        tertiaryContainer = Color.Unspecified, onTertiaryContainer = Color.Unspecified,
        error = Color.Unspecified, onError = Color.Unspecified,
        errorContainer = Color.Unspecified, onErrorContainer = Color.Unspecified,
        background = Color.Unspecified, onBackground = Color.Unspecified,
        surface = Color.Unspecified, onSurface = Color.Unspecified,
        surfaceVariant = Color.Unspecified, onSurfaceVariant = Color.Unspecified,
        outline = Color.Unspecified, outlineVariant = Color.Unspecified,
        inversePrimary = Color.Unspecified, inverseSurface = Color.Unspecified,
        inverseOnSurface = Color.Unspecified, surfaceTint = Color.Unspecified,
    )
}

/**
 * Provides a [KetoyColorScheme] for the Ketoy rendering pipeline.
 *
 * **Usage (automatic — reads MaterialTheme):**
 * ```kotlin
 * KetoyThemeProvider {
 *     JSONStringToUI(json)
 * }
 * ```
 *
 * **Usage (custom colours):**
 * ```kotlin
 * KetoyThemeProvider(
 *     colorScheme = KetoyColorScheme(
 *         primary = Color(0xFF6200EE),
 *         onPrimary = Color.White,
 *         ...
 *     )
 * ) {
 *     JSONStringToUI(json)
 * }
 * ```
 *
 * @param colorScheme  The colour scheme to expose. Defaults to
 *   [KetoyColorScheme.fromMaterial] using [MaterialTheme.colorScheme].
 */
@Composable
fun KetoyThemeProvider(
    colorScheme: KetoyColorScheme = KetoyColorScheme.fromMaterial(MaterialTheme.colorScheme),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalKetoyColors provides colorScheme) {
        content()
    }
}
