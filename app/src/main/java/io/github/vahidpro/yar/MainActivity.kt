package io.github.vahidpro.yar

import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.vahidpro.yar.design.YarLanguage
import io.github.vahidpro.yar.design.YarTheme
import io.github.vahidpro.yar.hokm.ui.HokmViewModel
import io.github.vahidpro.yar.settings.AppearanceMode
import io.github.vahidpro.yar.settings.AppearanceViewModel
import io.github.vahidpro.yar.ui.YarApp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = application as YarApplication
        setContent {
            val hokm: HokmViewModel = viewModel(factory = HokmViewModel.factory(container.hokmRepository))
            val appearance: AppearanceViewModel =
                viewModel(factory = AppearanceViewModel.factory(container.appearanceStore))
            val preferences by appearance.preferences.collectAsStateWithLifecycle()
            val dark = when (preferences.mode) {
                AppearanceMode.System -> isSystemInDarkTheme()
                AppearanceMode.Light -> false
                AppearanceMode.Dark -> true
            }

            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            YarLocale(preferences.language) {
                YarTheme(dark, preferences.palette, preferences.language) {
                    YarApp(hokm = hokm, appearance = appearance, preferences = preferences)
                }
            }
        }
    }
}

@Composable
private fun YarLocale(language: YarLanguage, content: @Composable () -> Unit) {
    val base = LocalContext.current
    val systemConfiguration = LocalConfiguration.current
    val context = remember(base, systemConfiguration, language) {
        val locale = Locale.forLanguageTag(language.tag)
        val configuration = Configuration(systemConfiguration).apply {
            setLocales(LocaleList(locale))
            setLayoutDirection(locale)
        }
        ContextThemeWrapper(base, 0).apply { applyOverrideConfiguration(configuration) }
    }
    CompositionLocalProvider(
        LocalContext provides context,
        LocalConfiguration provides context.resources.configuration,
        LocalResources provides context.resources,
        content = content,
    )
}
