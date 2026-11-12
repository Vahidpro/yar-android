package io.github.vahidpro.yar.hokm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.vahidpro.yar.design.YarPanel
import io.github.vahidpro.yar.design.YarScreenBar
import io.github.vahidpro.yar.design.YarSectionTitle
import io.github.vahidpro.yar.design.YarSymbol
import io.github.vahidpro.yar.design.YarTile
import io.github.vahidpro.yar.design.localized
import io.github.vahidpro.yar.hokm.R
import io.github.vahidpro.yar.hokm.domain.HokmRules
import io.github.vahidpro.yar.hokm.domain.TeamNumbers

@Composable
fun HokmRulesScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        YarScreenBar(stringResource(R.string.hokm_rules_title), onBack)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 24.dp),
        ) {
            HokmRulesBody()
        }
    }
}

@Composable
internal fun HokmRulesBody() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RulesStep(YarSymbol.Cards, stringResource(R.string.hokm_rule_one_title), stringResource(R.string.hokm_rule_one))
        RulesStep(YarSymbol.Check, stringResource(R.string.hokm_rule_two_title), stringResource(R.string.hokm_rule_two))
        RulesStep(YarSymbol.Trophy, stringResource(R.string.hokm_rule_three_title), stringResource(R.string.hokm_rule_three))
        YarSectionTitle(stringResource(R.string.hokm_rule_example_title, HokmRules.defaultTrickCount.localized()))
        YarPanel(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                ExampleRow(
                    stringResource(R.string.hokm_example_announced),
                    stringResource(R.string.hokm_example_taken),
                    stringResource(R.string.hokm_example_points),
                    header = true,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ScoreExample(announced = TeamNumbers(7, 6), taken = TeamNumbers(8, 5))
                ScoreExample(announced = TeamNumbers(10, 4), taken = TeamNumbers(9, 4))
                ScoreExample(announced = TeamNumbers(8, 7), taken = TeamNumbers(7, 6))
            }
        }
    }
}

@Composable
private fun RulesStep(icon: YarSymbol, title: String, body: String) {
    YarPanel(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            YarTile(icon, size = 40.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ScoreExample(announced: TeamNumbers, taken: TeamNumbers) {
    val points = HokmRules.points(announced, taken)
    ExampleRow(pairText(announced), pairText(taken), pairText(points))
}

@Composable
private fun pairText(values: TeamNumbers) =
    stringResource(R.string.hokm_example_pair, values.first.localized(), values.second.localized())

@Composable
private fun ExampleRow(announced: String, taken: String, score: String, header: Boolean = false) {
    val style = if (header) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleSmall
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(announced, Modifier.weight(1f), style = style, color = muted, textAlign = TextAlign.Center)
        Text(taken, Modifier.weight(1f), style = style, color = muted, textAlign = TextAlign.Center)
        Text(score, Modifier.weight(1f), style = style, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
    }
}
