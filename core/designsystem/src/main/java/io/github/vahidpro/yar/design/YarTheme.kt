package io.github.vahidpro.yar.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
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

data class YarHeroColors(
    val background: Color,
    val deep: Color,
    val firstAccent: Color,
    val secondAccent: Color,
    val line: Color,
    val cardBack: Color,
    val text: Color = Color(0xFFF9F9ED),
    val muted: Color = Color(0xFFD1D9D2),
)

enum class YarPalette(val key: String, val hero: YarHeroColors) {
    Forest("forest", YarHeroColors(
        Color(0xFF17392B), Color(0xFF102B21), Color(0xFFD9EF99),
        Color(0xFFF7D99F), Color(0xFF3D6251), Color(0xFF87AE83),
    )),
    Ocean("ocean", YarHeroColors(
        Color(0xFF163A55), Color(0xFF0C293D), Color(0xFFADEAFF),
        Color(0xFFFFDCAB), Color(0xFF3D647C), Color(0xFF83ADCD),
    )),
    Plum("plum", YarHeroColors(
        Color(0xFF482D50), Color(0xFF312036), Color(0xFFF3C7FA),
        Color(0xFFFFD9B3), Color(0xFF745779), Color(0xFFBA92BF),
    )),
}

enum class YarLanguage(val tag: String, val nativeName: String, val rtl: Boolean) {
    Persian("fa", "فارسی", true),
    English("en", "English", false),
    Balochi("bal-Arab", "بلوچی", true),
}

val LocalYarHero = staticCompositionLocalOf { YarPalette.Forest.hero }
val LocalYarLanguage = staticCompositionLocalOf { YarLanguage.Persian }

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
    primary = YarPalette.Forest.hero.firstAccent,
    onPrimary = Color(0xFF263600),
    primaryContainer = Color(0xFF304B30),
    onPrimaryContainer = Color(0xFFE2F2CC),
    secondary = Color(0xFFBBCFC0),
    secondaryContainer = Color(0xFF2A4135),
    onSecondaryContainer = Color(0xFFD4E8D9),
    tertiary = YarPalette.Forest.hero.secondAccent,
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

private val oceanLight = lightPalette.copy(
    primary = Color(0xFF23587A), primaryContainer = Color(0xFFD4EAF8), onPrimaryContainer = Color(0xFF123C55),
    secondary = Color(0xFF4B6173), secondaryContainer = Color(0xFFDBEAF3), onSecondaryContainer = Color(0xFF1F3B4C),
    background = Color(0xFFF1F5F7), surface = Color(0xFFFBFDFE), onBackground = Color(0xFF192831),
    onSurface = Color(0xFF192831), onSurfaceVariant = Color(0xFF475963), surfaceContainer = Color(0xFFEAF0F4),
    surfaceVariant = Color(0xFFE3EDF2), surfaceContainerHigh = Color(0xFFE2EAF0),
    surfaceContainerHighest = Color(0xFFD9E6EE), outlineVariant = Color(0xFFD4E0E7),
)

private val oceanDark = darkPalette.copy(
    primary = YarPalette.Ocean.hero.firstAccent, onPrimary = Color(0xFF102F42),
    primaryContainer = Color(0xFF25485E), onPrimaryContainer = Color(0xFFD4F2FF),
    secondary = Color(0xFFBACFDB), secondaryContainer = Color(0xFF263E4D), onSecondaryContainer = Color(0xFFD9ECF6),
    background = Color(0xFF111B23), surface = Color(0xFF1B2833), onSurfaceVariant = Color(0xFFBACBD5),
    surfaceVariant = Color(0xFF2E3C46), surfaceContainer = Color(0xFF21323F),
    surfaceContainerHigh = Color(0xFF283A47), surfaceContainerHighest = Color(0xFF344856),
    outlineVariant = Color(0xFF354955),
)

private val plumLight = lightPalette.copy(
    primary = Color(0xFF694273), primaryContainer = Color(0xFFF4DDF8), onPrimaryContainer = Color(0xFF4B2854),
    secondary = Color(0xFF67536C), secondaryContainer = Color(0xFFF0E3F2), onSecondaryContainer = Color(0xFF47354B),
    background = Color(0xFFF8F3F8), surface = Color(0xFFFEFBFE), onBackground = Color(0xFF2A212D),
    onSurface = Color(0xFF2A212D), onSurfaceVariant = Color(0xFF5F5062), surfaceContainer = Color(0xFFF2EAF3),
    surfaceVariant = Color(0xFFEEE4F0), surfaceContainerHigh = Color(0xFFEBDFEE),
    surfaceContainerHighest = Color(0xFFE6D7EA), outlineVariant = Color(0xFFE6D9E9),
)

private val plumDark = darkPalette.copy(
    primary = YarPalette.Plum.hero.firstAccent, onPrimary = Color(0xFF39213F),
    primaryContainer = Color(0xFF51365A), onPrimaryContainer = Color(0xFFF9DFFC),
    secondary = Color(0xFFD2C2D5), secondaryContainer = Color(0xFF47364B), onSecondaryContainer = Color(0xFFF1E0F3),
    background = Color(0xFF1C1620), surface = Color(0xFF2A202F), onSurfaceVariant = Color(0xFFD1C3D4),
    surfaceVariant = Color(0xFF403346), surfaceContainer = Color(0xFF322637),
    surfaceContainerHigh = Color(0xFF3D2F43), surfaceContainerHighest = Color(0xFF493950),
    outlineVariant = Color(0xFF4D3D54),
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

private fun yarTypography(language: YarLanguage): Typography {
    val direction = if (language.rtl) TextDirection.ContentOrRtl else TextDirection.ContentOrLtr
    val locales = LocaleList(language.tag)
    fun type(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = estedad, fontWeight = weight, fontSize = size.sp,
        lineHeight = height.sp, letterSpacing = 0.sp, textDirection = direction, localeList = locales,
    )
    return Typography(
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
}

@Composable
fun YarTheme(dark: Boolean, palette: YarPalette, language: YarLanguage, content: @Composable () -> Unit) {
    val colors = when (palette) {
        YarPalette.Forest -> if (dark) darkPalette else lightPalette
        YarPalette.Ocean -> if (dark) oceanDark else oceanLight
        YarPalette.Plum -> if (dark) plumDark else plumLight
    }
    val direction = if (language.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val typography = remember(language) { yarTypography(language) }
    CompositionLocalProvider(
        LocalLayoutDirection provides direction,
        LocalYarHero provides palette.hero,
        LocalYarLanguage provides language,
    ) {
        MaterialTheme(colorScheme = colors, typography = typography, content = content)
    }
}

@Composable
fun Int.localized(): String = toString().localizedDigits()

@Composable
fun Long.localized(): String = toString().localizedDigits()

@Composable
fun String.localizedDigits(): String = LocalYarLanguage.current.digits(this)

fun YarLanguage.digits(text: String): String =
    if (rtl) text.map { it.digitToIntOrNull()?.let { digit -> '۰' + digit } ?: it }.joinToString("")
    else text.asciiDigits()

fun String.asciiDigits(): String = map { character ->
    character.digitToIntOrNull()?.let { '0' + it } ?: character
}.joinToString("")
