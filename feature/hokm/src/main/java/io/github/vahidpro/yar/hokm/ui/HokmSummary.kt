package io.github.vahidpro.yar.hokm.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.vahidpro.yar.design.LocalYarLanguage
import io.github.vahidpro.yar.design.YarIcon
import io.github.vahidpro.yar.design.YarPanel
import io.github.vahidpro.yar.design.YarPrimaryButton
import io.github.vahidpro.yar.design.YarSymbol
import io.github.vahidpro.yar.design.localized
import io.github.vahidpro.yar.hokm.R
import io.github.vahidpro.yar.hokm.domain.HokmGame
import io.github.vahidpro.yar.hokm.domain.TeamSide
import io.github.vahidpro.yar.hokm.domain.statsFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun ScoreAction(
    text: String,
    symbol: YarSymbol,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(onClick, modifier.heightIn(min = 48.dp), enabled, shape = RoundedCornerShape(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            YarIcon(symbol, size = 18.dp)
            Text(text, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun FinishedGameActions(game: HokmGame, firstName: String, secondName: String, onStats: () -> Unit) {
    val context = LocalContext.current
    val language = LocalYarLanguage.current
    val scope = rememberCoroutineScope()
    var sharing by remember { mutableStateOf(false) }
    var shareFailed by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        YarPrimaryButton(
            stringResource(R.string.hokm_share_table),
            onClick = {
                scope.launch {
                    sharing = true
                    shareFailed = false
                    try {
                        shareScoreTable(context, game, firstName, secondName, language)
                    } catch (failure: Exception) {
                        if (failure is CancellationException) throw failure
                        Log.e("HokmScoreShare", "Could not share the score table", failure)
                        shareFailed = true
                    } finally {
                        sharing = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            symbol = YarSymbol.Share,
            enabled = !sharing,
        )
        ScoreAction(
            stringResource(R.string.hokm_stats),
            YarSymbol.Chart,
            onStats,
            enabled = game.rounds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (shareFailed) {
            Text(
                stringResource(R.string.hokm_share_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
internal fun MatchStats(game: HokmGame, firstName: String, secondName: String) {
    val first = remember(game.rounds) { game.rounds.statsFor(TeamSide.First) }
    val second = remember(game.rounds) { game.rounds.statsFor(TeamSide.Second) }
    val metrics = listOf(
        Triple(R.string.hokm_stat_made, first.made.toLong(), second.made.toLong()),
        Triple(R.string.hokm_stat_exact, first.exact.toLong(), second.exact.toLong()),
        Triple(R.string.hokm_stat_underbid, first.underbid.toLong(), second.underbid.toLong()),
        Triple(R.string.hokm_stat_missed, first.missed.toLong(), second.missed.toLong()),
        Triple(R.string.hokm_stat_extra, first.excessTricks, second.excessTricks),
        Triple(R.string.hokm_stat_shortfall, first.shortfall, second.shortfall),
    )
    YarPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            StatsRow(stringResource(R.string.hokm_round_count, game.rounds.size.localized()), firstName, secondName)
            metrics.forEach { (label, a, b) ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                StatsRow(stringResource(label), a.localized(), b.localized())
            }
        }
    }
}

@Composable
private fun StatsRow(label: String, first: String, second: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1.6f), style = MaterialTheme.typography.labelMedium)
        Text(first, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
        Text(second, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
    }
}
