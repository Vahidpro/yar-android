package io.github.vahidpro.yar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.vahidpro.yar.BuildConfig
import io.github.vahidpro.yar.R
import io.github.vahidpro.yar.design.YarPanel
import io.github.vahidpro.yar.design.YarSectionTitle
import io.github.vahidpro.yar.design.YarSymbol
import io.github.vahidpro.yar.design.YarTile
import io.github.vahidpro.yar.design.persianDigits
import io.github.vahidpro.yar.settings.AppearanceMode

@Composable
fun AppearanceScreen(mode: AppearanceMode, onSelect: (AppearanceMode) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        YarSectionTitle(stringResource(R.string.appearance_heading))
        YarPanel(Modifier.fillMaxWidth()) {
            Column(Modifier.selectableGroup()) {
                AppearanceMode.entries.forEachIndexed { index, option ->
                    if (index > 0) {
                        HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = colors.outlineVariant)
                    }
                    val (label, icon) = when (option) {
                        AppearanceMode.System -> R.string.theme_system to YarSymbol.System
                        AppearanceMode.Light -> R.string.theme_light to YarSymbol.Sun
                        AppearanceMode.Dark -> R.string.theme_dark to YarSymbol.Moon
                    }
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 64.dp)
                            .selectable(selected = option == mode, role = Role.RadioButton, onClick = { onSelect(option) })
                            .padding(horizontal = 13.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        YarTile(icon, size = 40.dp)
                        Text(stringResource(label), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                        RadioButton(selected = option == mode, onClick = null)
                    }
                }
            }
        }
        YarSectionTitle(stringResource(R.string.about_heading))
        YarPanel(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(15.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                YarTile(YarSymbol.Spade)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        stringResource(R.string.app_version, BuildConfig.VERSION_NAME.substringBefore('-').persianDigits()),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        stringResource(R.string.local_storage),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
