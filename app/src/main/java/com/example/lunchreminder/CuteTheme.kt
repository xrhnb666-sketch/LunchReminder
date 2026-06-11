package com.example.lunchreminder

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp

object CuteColors {
    val Background = Color(0xFFFFF9F2)
    val NavBackground = Color(0xFFFFF4E8)
    val Card = Color.White
    val Breakfast = Color(0xFFFFF2DA)
    val Lunch = Color(0xFFFFE8D1)
    val Dinner = Color(0xFFEAF6E5)
    val SkipToday = Color(0xFFF2EAFF)
    val TextPrimary = Color(0xFF4A332A)
    val TextSecondary = Color(0xFF9A7562)
    val Orange = Color(0xFFFF8A35)
    val BreakfastAccent = Color(0xFFF6A437)
    val LunchAccent = Color(0xFFF79B37)
    val DinnerAccent = Color(0xFF6EAE67)
    val OrangeStart = Color(0xFFFFA857)
    val OrangeEnd = Color(0xFFFF7C2B)
    val SoftGray = Color(0xFFE7DED6)
    val WarmLine = Color(0xFFF1E3D2)
    val Green = Color(0xFF7DB67C)
    val ChartGreen = Color(0xFF8FBE8F)
    val ChartBrown = Color(0xFFD5A06C)
}

object CuteDimens {
    val PagePadding = UiConstants.Layout.PagePadding
    val CardRadius = UiConstants.Layout.CardRadius
    val CardPadding = UiConstants.Layout.CardPadding
    val CardSpacing = UiConstants.Layout.CardSpacing
    val ButtonRadius = UiConstants.Layout.ButtonRadius
}

private val cuteLightColorScheme = lightColorScheme(
    primary = CuteColors.Orange,
    onPrimary = Color.White,
    background = CuteColors.Background,
    onBackground = CuteColors.TextPrimary,
    surface = CuteColors.Card,
    onSurface = CuteColors.TextPrimary,
    surfaceContainerHigh = CuteColors.Card,
    surfaceContainerHighest = CuteColors.Card,
    primaryContainer = CuteColors.Lunch,
    onPrimaryContainer = CuteColors.TextPrimary,
    secondaryContainer = CuteColors.SkipToday,
    onSecondaryContainer = CuteColors.TextPrimary,
    outline = CuteColors.WarmLine,
)

private val cuteDarkColorScheme = darkColorScheme(
    primary = CuteColors.Orange,
    onPrimary = Color.White,
    background = Color(0xFF2B211C),
    onBackground = Color(0xFFFFF3E8),
    surface = Color(0xFF3A2C25),
    onSurface = Color(0xFFFFF3E8),
    surfaceContainerHigh = Color(0xFF423229),
    surfaceContainerHighest = Color(0xFF47362D),
    primaryContainer = Color(0xFF5A3A28),
    onPrimaryContainer = Color(0xFFFFE4D0),
    secondaryContainer = Color(0xFF4E415A),
    onSecondaryContainer = Color(0xFFFFF3E8),
    outline = Color(0xFF7C6253),
)

private val cuteTypography = Typography(
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        color = CuteColors.TextPrimary,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = CuteColors.TextPrimary,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = CuteColors.TextPrimary,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = CuteColors.TextPrimary,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = CuteColors.TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = CuteColors.TextPrimary,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = CuteColors.TextSecondary,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = CuteColors.TextSecondary,
    ),
)

private val cuteShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(CuteDimens.CardRadius),
)

@Composable
fun CuteTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = ThemeUtils.shouldUseDarkTheme(
        themeMode = themeMode,
        systemInDarkTheme = isSystemInDarkTheme(),
    )

    MaterialTheme(
        colorScheme = if (darkTheme) cuteDarkColorScheme else cuteLightColorScheme,
        typography = cuteTypography,
        shapes = cuteShapes,
        content = content,
    )
}
