package io.github.vahidpro.yar.ui

import android.icu.text.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vahidpro.yar.R
import io.github.vahidpro.yar.design.YarEmptyState
import io.github.vahidpro.yar.design.YarIcon
import io.github.vahidpro.yar.design.YarPrimaryButton
import io.github.vahidpro.yar.design.YarScreenBar
import io.github.vahidpro.yar.design.YarSymbol
import io.github.vahidpro.yar.design.YarTile
import io.github.vahidpro.yar.design.LocalYarLanguage
import io.github.vahidpro.yar.design.YarLanguage
import io.github.vahidpro.yar.design.digits
import io.github.vahidpro.yar.design.localized
import io.github.vahidpro.yar.hokm.domain.HokmGame
import io.github.vahidpro.yar.hokm.ui.HokmGamesState
import io.github.vahidpro.yar.hokm.ui.displayName
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    state: HokmGamesState,
    onRetry: () -> Unit,
    onOpenGame: (Long?) -> Unit,
    onNewGame: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        YarScreenBar(stringResource(R.string.saved_games_heading)) {
            if (state is HokmGamesState.Ready) {
                IconButton(onClick = onNewGame) { YarIcon(YarSymbol.Plus, stringResource(R.string.new_game)) }
            }
        }
        when (state) {
            HokmGamesState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            HokmGamesState.LoadFailed -> Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.games_load_error))
                YarPrimaryButton(stringResource(R.string.try_again), onRetry)
            }
            is HokmGamesState.Ready -> {
                val visible = state.games.filter { it.hasProgress || it.isFinished || !it.settings.isDefault }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (visible.isEmpty()) {
                        item {
                            YarEmptyState(
                                stringResource(R.string.history_empty_title),
                                stringResource(R.string.history_empty_body),
                                YarSymbol.History,
                            )
                        }
                        item {
                            YarPrimaryButton(
                                stringResource(R.string.new_game),
                                onNewGame,
                                modifier = Modifier.fillMaxWidth(),
                                symbol = YarSymbol.Plus,
                            )
                        }
                    }
                    items(visible, key = HokmGame::id) { game ->
                        GameHistoryCard(game) { onOpenGame(game.id.takeUnless { game.isActive }) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun GameHistoryCard(game: HokmGame, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val language = LocalYarLanguage.current
    val date = remember(game.createdAt, language) {
        val locale = Locale.forLanguageTag(if (language.rtl) "fa-IR-u-ca-persian" else language.tag)
        val style = if (language == YarLanguage.Balochi) DateFormat.SHORT else DateFormat.MEDIUM
        language.digits(DateFormat.getDateInstance(style, locale).format(Date(game.createdAt)))
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                YarTile(YarSymbol.Cards, size = 42.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.history_hokm),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        stringResource(R.string.history_date_rounds, date, game.rounds.size.localized()),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                if (game.isActive || game.isFinished) {
                    Surface(
                        color = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer,
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            stringResource(if (game.isFinished) R.string.finished_game else R.string.active_game),
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                YarIcon(YarSymbol.Next, color = colors.onSurfaceVariant, size = 18.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryScore(
                    game.settings.firstTeam.displayName(first = true),
                    game.firstTotal.takeIf { game.scoresVisible },
                    Modifier.weight(1f),
                )
                HistoryScore(
                    game.settings.secondTeam.displayName(first = false),
                    game.secondTotal.takeIf { game.scoresVisible },
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HistoryScore(name: String, score: Long?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis, maxLines = 1,
            )
            if (score != null) Text(score.localized(), style = MaterialTheme.typography.titleSmall)
        }
    }
}
