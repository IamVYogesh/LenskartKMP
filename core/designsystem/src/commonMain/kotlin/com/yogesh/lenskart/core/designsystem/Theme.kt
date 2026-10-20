package com.yogesh.lenskart.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object LkColors {
    val Navy = Color(0xFF000042)
    val Mint = Color(0xFF11DAAC)
    val MintDark = Color(0xFF00A982)
    val Gold = Color(0xFFE8B04B)
    val Danger = Color(0xFFD64545)
}

private val LightColors = lightColorScheme(
    primary = LkColors.Navy, onPrimary = Color.White,
    secondary = LkColors.MintDark, onSecondary = Color.White,
    tertiary = LkColors.Gold, onTertiary = LkColors.Navy,
    background = Color(0xFFFAFAFC), onBackground = Color(0xFF14142B),
    surface = Color.White, onSurface = Color(0xFF14142B),
    surfaceVariant = Color(0xFFF0F1F6), onSurfaceVariant = Color(0xFF55576E),
    outline = Color(0xFFD5D7E3), error = LkColors.Danger,
)

private val DarkColors = darkColorScheme(
    primary = LkColors.Mint, onPrimary = LkColors.Navy,
    secondary = LkColors.Mint, onSecondary = LkColors.Navy,
    tertiary = LkColors.Gold, onTertiary = LkColors.Navy,
    background = Color(0xFF0C0C1D), onBackground = Color(0xFFEDEDF7),
    surface = Color(0xFF15152B), onSurface = Color(0xFFEDEDF7),
    surfaceVariant = Color(0xFF22223D), onSurfaceVariant = Color(0xFFB4B6CC),
    outline = Color(0xFF3A3B5A), error = Color(0xFFFF8A8A),
)

object LkSpacing {
    val xs = 4.dp; val sm = 8.dp; val md = 16.dp; val lg = 24.dp; val xl = 32.dp
}

@Composable
fun LenskartTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(20.dp),
        ),
        content = content,
    )
}
