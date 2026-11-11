package io.github.vahidpro.yar

import android.app.Application
import io.github.vahidpro.yar.hokm.data.openHokmRepository
import io.github.vahidpro.yar.settings.AppearanceStore

class YarApplication : Application() {
    val hokmRepository by lazy { openHokmRepository(this) }
    val appearanceStore by lazy { AppearanceStore(this) }
}
