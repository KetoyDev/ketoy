package com.developerstring.ketoy_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.developerstring.ketoy.theme.KetoyThemeProvider

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun KetoyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        val customColors = if (darkTheme) mapOf(
            "success" to Color(0xFF6DD58C),
            "successContainer" to Color(0xFF0A5C30),
            "onSuccess" to Color(0xFF003919),
            "onSuccessContainer" to Color(0xFF6DD58C),
        ) else mapOf(
            "success" to Color(0xFF1B7D46),
            "successContainer" to Color(0xFFA8F5C4),
            "onSuccess" to Color(0xFFFFFFFF),
            "onSuccessContainer" to Color(0xFF002110),
        )
        // Provide Ketoy SDK's dark-theme flag & colour scheme
        KetoyThemeProvider(customColors = customColors) {
            content()
        }
    }
}