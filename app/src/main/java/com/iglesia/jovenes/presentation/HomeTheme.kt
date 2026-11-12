package com.iglesia.jovenes.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppBackground = Color(0xFF0B0D1A)
val CardSurface = Color(0xFF121525)
val ElectricPurple = Color(0xFF7847FF)
val NeonCyan = Color(0xFF22E7EF)
val RewardOrange = Color(0xFFFF834E)
val RewardGold = Color(0xFFFFD15C)
val MutedText = Color(0xFF8B90A8)

private val Colors = darkColorScheme(
    primary = ElectricPurple,
    secondary = NeonCyan,
    tertiary = RewardOrange,
    background = AppBackground,
    surface = CardSurface,
    onPrimary = Color.White,
    onSecondary = AppBackground,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun YouthTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
