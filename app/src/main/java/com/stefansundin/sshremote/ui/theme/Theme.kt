/*
 * SSH Remote
 * Copyright (C) 2026  Stefan Sundin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.stefansundin.sshremote.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.stefansundin.sshremote.Theme
import com.stefansundin.sshremote.ui.screens.RemoteControlScreenPreview

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

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
fun SSHRemoteTheme(
    theme: Theme = Theme.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    colorOverrides: ColorScheme.() -> ColorScheme = { this },
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var effectiveTheme = theme
    if (effectiveTheme == Theme.SYSTEM) {
        effectiveTheme = if (isSystemInDarkTheme()) Theme.DARK else Theme.LIGHT
    }
    val baseColorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (effectiveTheme == Theme.DARK) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        if (effectiveTheme == Theme.DARK) {
            DarkColorScheme
        } else {
            LightColorScheme
        }
    }
    val colorScheme = baseColorScheme.colorOverrides()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
fun ThemePreview_Light() {
    SSHRemoteTheme(Theme.LIGHT) {
        RemoteControlScreenPreview()
    }
}

@Preview(showBackground = true)
@Composable
fun ThemePreview_Light_WithoutDynamicColors() {
    SSHRemoteTheme(Theme.LIGHT, dynamicColor = false) {
        RemoteControlScreenPreview()
    }
}

@Preview(showBackground = true)
@Composable
fun ThemePreview_Dark() {
    SSHRemoteTheme(Theme.DARK) {
        RemoteControlScreenPreview()
    }
}

@Preview(showBackground = true)
@Composable
fun ThemePreview_Dark_WithoutDynamicColors() {
    SSHRemoteTheme(Theme.DARK, dynamicColor = false) {
        RemoteControlScreenPreview()
    }
}

@Preview(showBackground = true)
@Composable
fun ThemePreview_BlackBackground() {
    SSHRemoteTheme(
        Theme.DARK,
        colorOverrides = {
            this.copy(
                background = Color.Black,
                surface = Color.Black,
            )
        },
    ) {
        RemoteControlScreenPreview()
    }
}
