package com.babeli.network.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary          = Cyan,
    onPrimary        = BgPrimary,
    primaryContainer = CyanDim,
    secondary        = Purple,
    onSecondary      = TextPrimary,
    tertiary         = Green,
    background       = BgPrimary,
    onBackground     = TextPrimary,
    surface          = BgSurface,
    onSurface        = TextPrimary,
    surfaceVariant   = BgCard,
    onSurfaceVariant = TextSecond,
    outline          = Divider,
    error            = Orange,
    onError          = TextPrimary,
)

@Composable
fun BabeliTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography  = AppTypography,
        content     = content
    )
}
