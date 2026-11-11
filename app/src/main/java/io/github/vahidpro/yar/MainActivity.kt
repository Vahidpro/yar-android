package io.github.vahidpro.yar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.vahidpro.yar.design.YarTheme
import io.github.vahidpro.yar.hokm.ui.HokmViewModel
import io.github.vahidpro.yar.settings.AppearanceMode
import io.github.vahidpro.yar.settings.AppearanceViewModel
import io.github.vahidpro.yar.ui.YarApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = application as YarApplication
        setContent {
            val hokm: HokmViewModel = viewModel(factory = HokmViewModel.factory(container.hokmRepository))
            val appearance: AppearanceViewModel =
                viewModel(factory = AppearanceViewModel.factory(container.appearanceStore))
            val mode by appearance.mode.collectAsStateWithLifecycle()
            val dark = when (mode) {
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
            YarTheme(dark = dark) {
                YarApp(hokm = hokm, appearance = appearance, mode = mode)
            }
        }
    }
}
