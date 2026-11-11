package io.github.vahidpro.yar.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

object YarColors {
    val forest = Color(0xFF17392B)
    val forestDeep = Color(0xFF102B21)
    val lime = Color(0xFFD9EF99)
    val sand = Color(0xFFF7D99F)
    val onForest = Color(0xFFF9F9ED)
    val mutedOnForest = Color(0xFFC2D2C4)
    val forestLine = Color(0xFF3D6251)
}

private val lightPalette = lightColorScheme(
    primary = Color(0xFF285843),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EBCF),
    onPrimaryContainer = Color(0xFF173C29),
    secondary = Color(0xFF496250),
    secondaryContainer = Color(0xFFE4ECDD),
    onSecondaryContainer = Color(0xFF263C2D),
    tertiary = Color(0xFF775415),
    tertiaryContainer = Color(0xFFF9E7BF),
    onTertiaryContainer = Color(0xFF5C400B),
    background = Color(0xFFF6F5EE),
    onBackground = Color(0xFF192720),
    surface = Color(0xFFFCFBF6),
    onSurface = Color(0xFF192720),
    onSurfaceVariant = Color(0xFF4D5C53),
    surfaceVariant = Color(0xFFE6ECE2),
    surfaceContainer = Color(0xFFEFEFE6),
    surfaceContainerHigh = Color(0xFFE8ECE1),
    surfaceContainerHighest = Color(0xFFE0E7DB),
    outline = Color(0xFF748278),
    outlineVariant = Color(0xFFDBE2D8),
    error = Color(0xFF9D3029),
)

private val darkPalette = darkColorScheme(
    primary = YarColors.lime,
    onPrimary = Color(0xFF263600),
    primaryContainer = Color(0xFF304B30),
    onPrimaryContainer = Color(0xFFE2F2CC),
    secondary = Color(0xFFBBCFC0),
    secondaryContainer = Color(0xFF2A4135),
    onSecondaryContainer = Color(0xFFD4E8D9),
    tertiary = YarColors.sand,
    tertiaryContainer = Color(0xFF493A23),
    onTertiaryContainer = Color(0xFFF8E3B7),
    background = Color(0xFF111A16),
    onBackground = Color(0xFFE8EEE7),
    surface = Color(0xFF1B2720),
    onSurface = Color(0xFFE8EEE7),
    onSurfaceVariant = Color(0xFFB6C5B8),
    surfaceVariant = Color(0xFF303E35),
    surfaceContainer = Color(0xFF202D25),
    surfaceContainerHigh = Color(0xFF28372E),
    surfaceContainerHighest = Color(0xFF34453A),
    outline = Color(0xFF93A195),
    outlineVariant = Color(0xFF36483D),
    error = Color(0xFFFFB4A9),
)

@OptIn(ExperimentalTextApi::class)
private val estedad = FontFamily(
    listOf(
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
        FontWeight.ExtraBold,
    ).map { weight ->
        Font(
            resId = R.font.estedad_variable,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
        )
    },
)

private fun type(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = estedad,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = 0.sp,
    textDirection = TextDirection.ContentOrRtl,
    localeList = LocaleList("fa-IR"),
)

private val yarTypography = Typography(
    displayLarge = type(52, 58, FontWeight.ExtraBold),
    displayMedium = type(42, 52, FontWeight.ExtraBold),
    displaySmall = type(34, 45, FontWeight.ExtraBold),
    headlineLarge = type(30, 44, FontWeight.ExtraBold),
    headlineMedium = type(26, 38, FontWeight.ExtraBold),
    headlineSmall = type(22, 34, FontWeight.Bold),
    titleLarge = type(19, 29, FontWeight.Bold),
    titleMedium = type(16, 26, FontWeight.Bold),
    titleSmall = type(14, 23, FontWeight.SemiBold),
    bodyLarge = type(16, 27),
    bodyMedium = type(14, 24),
    bodySmall = type(12, 20),
    labelLarge = type(14, 23, FontWeight.Bold),
    labelMedium = type(12, 20, FontWeight.SemiBold),
    labelSmall = type(11, 18, FontWeight.SemiBold),
)

@Composable
fun YarTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = if (dark) darkPalette else lightPalette,
            typography = yarTypography,
            content = content,
        )
    }
}

fun Int.persian(): String = toString().persianDigits()

fun Long.persian(): String = toString().persianDigits()

fun String.persianDigits(): String = map { character ->
    character.digitToIntOrNull()?.let { '۰' + it } ?: character
}.joinToString("")

fun String.asciiDigits(): String = map { character ->
    character.digitToIntOrNull()?.let { '0' + it } ?: character
}.joinToString("")
