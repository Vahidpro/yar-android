package io.github.vahidpro.yar.hokm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vahidpro.yar.design.LocalYarHero
import io.github.vahidpro.yar.design.YarEmptyState
import io.github.vahidpro.yar.design.YarIcon
import io.github.vahidpro.yar.design.YarPanel
import io.github.vahidpro.yar.design.YarPrimaryButton
import io.github.vahidpro.yar.design.YarScreenBar
import io.github.vahidpro.yar.design.YarSectionTitle
import io.github.vahidpro.yar.design.YarSymbol
import io.github.vahidpro.yar.design.YarTile
import io.github.vahidpro.yar.design.localized
import io.github.vahidpro.yar.hokm.R
import io.github.vahidpro.yar.hokm.domain.GameSettings
import io.github.vahidpro.yar.hokm.domain.HokmGame
import io.github.vahidpro.yar.hokm.domain.HokmRound
import io.github.vahidpro.yar.hokm.domain.HokmRules
import io.github.vahidpro.yar.hokm.domain.TeamNumbers
import io.github.vahidpro.yar.hokm.domain.TeamSide

private enum class GameOverlay {
    Announce, Result, FixAnnouncements, EditRound, Settings, NewGame, Reset, DeleteRound, Rules, Stats, Finish,
}

@Composable
fun HokmScoreScreen(
    viewModel: HokmViewModel,
    gameId: Long?,
    openNewGame: Boolean = false,
    onBack: () -> Unit,
    onActivated: () -> Unit,
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val saveFailed by viewModel.saveFailed.collectAsStateWithLifecycle()
    var openSetup by rememberSaveable { mutableStateOf(openNewGame) }

    val ready = games as? HokmGamesState.Ready
    val game = if (gameId == null) ready?.active else ready?.games?.firstOrNull { it.id == gameId }
    if (game != null) {
        key(game.id) {
            GameContent(
                game = game,
                viewModel = viewModel,
                saving = saving,
                saveFailed = saveFailed,
                openSetup = openSetup,
                onSetupOpened = { openSetup = false },
                onBack = onBack,
                onActivated = onActivated,
            )
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            YarScreenBar(stringResource(R.string.hokm_title), onBack)
            when (games) {
                HokmGamesState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                HokmGamesState.LoadFailed -> Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.hokm_load_failed))
                    YarPrimaryButton(stringResource(R.string.hokm_retry), viewModel::reload)
                }
                is HokmGamesState.Ready -> Text(
                    stringResource(R.string.hokm_game_missing),
                    Modifier.padding(20.dp),
                )
            }
        }
    }
}

@Composable
private fun GameContent(
    game: HokmGame,
    viewModel: HokmViewModel,
    saving: Boolean,
    saveFailed: Boolean,
    openSetup: Boolean,
    onSetupOpened: () -> Unit,
    onBack: () -> Unit,
    onActivated: () -> Unit,
) {
    var overlay by rememberSaveable { mutableStateOf<GameOverlay?>(null) }
    var selectedRoundId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    val selectedRound = game.rounds.firstOrNull { it.id == selectedRoundId }
    val nextRound = game.nextRoundNumber
    val draft = game.draft
    val forced = game.settings.forcedRounds.announcementsFor(nextRound).takeIf { draft == null }
    val firstName = game.settings.firstTeam.displayName(first = true)
    val secondName = game.settings.secondTeam.displayName(first = false)
    val trickCount = game.settings.trickCount

    fun show(next: GameOverlay) {
        viewModel.clearSaveError()
        showMenu = false
        overlay = next
    }

    fun close() {
        viewModel.clearSaveError()
        overlay = null
    }

    LaunchedEffect(openSetup) {
        if (openSetup) {
            show(GameOverlay.NewGame)
            onSetupOpened()
        }
    }

    Column(Modifier.fillMaxSize()) {
        YarScreenBar(stringResource(R.string.hokm_title), onBack) {
            if (game.isEditable) {
                IconButton(onClick = { show(GameOverlay.Settings) }, enabled = !saving) {
                    YarIcon(YarSymbol.Settings, stringResource(R.string.hokm_game_settings))
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, enabled = !saving) {
                    YarIcon(YarSymbol.More, stringResource(R.string.hokm_options))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (game.isActive) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.hokm_new_game)) },
                            leadingIcon = { YarIcon(YarSymbol.Plus) },
                            onClick = { show(GameOverlay.NewGame) },
                        )
                    }
                    if (game.isEditable) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.hokm_reset_scores)) },
                            leadingIcon = { YarIcon(YarSymbol.Reset) },
                            enabled = game.hasProgress,
                            onClick = { show(GameOverlay.Reset) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.hokm_rules_title)) },
                        leadingIcon = { YarIcon(YarSymbol.Help) },
                        onClick = { show(GameOverlay.Rules) },
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "score") {
                ScoreBoard(game, firstName, secondName, !saving) {
                    viewModel.setScoresVisible(game.id, !game.settings.showScores)
                }
            }
            item(key = "next") {
                if (game.isFinished) {
                    FinishedGameActions(game, firstName, secondName, { show(GameOverlay.Stats) })
                } else if (game.isEditable) {
                    RoundActionCard(
                        round = nextRound,
                        game = game,
                        firstName = firstName,
                        secondName = secondName,
                        forced = forced,
                        enabled = !saving,
                        onOpen = {
                            when {
                                draft != null -> show(GameOverlay.Result)
                                forced != null -> viewModel.startRound(game.id, forced, ::close)
                                else -> show(GameOverlay.Announce)
                            }
                        },
                        onFix = { show(GameOverlay.FixAnnouncements) },
                    )
                } else {
                    YarPrimaryButton(
                        stringResource(R.string.hokm_resume_game),
                        onClick = { viewModel.activate(game.id, onActivated) },
                        modifier = Modifier.fillMaxWidth(),
                        symbol = YarSymbol.Next,
                        enabled = !saving,
                    )
                }
            }
            if (!game.isFinished) {
                item(key = "actions") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScoreAction(
                            text = stringResource(R.string.hokm_stats),
                            symbol = YarSymbol.Chart,
                            onClick = { show(GameOverlay.Stats) },
                            enabled = game.rounds.isNotEmpty() && !saving,
                            modifier = Modifier.weight(1f),
                        )
                        if (game.isEditable) {
                            ScoreAction(
                                text = stringResource(R.string.hokm_finish_game),
                                symbol = YarSymbol.Trophy,
                                onClick = { show(GameOverlay.Finish) },
                                enabled = !saving,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            if (saveFailed && overlay == null) {
                item(key = "error") { SaveError() }
            }
            item(key = "round_heading") {
                Row(
                    Modifier.fillMaxWidth().padding(top = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    YarSectionTitle(stringResource(R.string.hokm_rounds), Modifier.weight(1f))
                    Text(
                        stringResource(R.string.hokm_round_count, game.rounds.size.localized()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (game.rounds.isEmpty()) {
                item(key = "empty") {
                    YarEmptyState(
                        stringResource(R.string.hokm_no_rounds),
                        stringResource(R.string.hokm_no_rounds_body),
                        YarSymbol.Cards,
                    )
                }
            } else {
                item(key = "round_columns") { RoundColumnLabels(firstName, secondName, game.isEditable) }
                items(game.rounds.asReversed(), key = HokmRound::id) { round ->
                    RoundRow(
                        round = round,
                        editable = game.isEditable,
                        showPoints = game.scoresVisible,
                        enabled = !saving,
                        onEdit = {
                            selectedRoundId = round.id
                            show(GameOverlay.EditRound)
                        },
                    )
                }
            }
        }
    }

    when (overlay) {
        GameOverlay.Announce, GameOverlay.FixAnnouncements -> {
            val editing = overlay == GameOverlay.FixAnnouncements
            if (!editing || draft != null) {
                AnnouncementSheet(
                    round = nextRound,
                    trickCount = trickCount,
                    minimumAnnouncement = draft?.minimumAnnouncement ?: game.settings.minimumAnnouncement,
                    firstName = firstName,
                    secondName = secondName,
                    initial = draft?.announced,
                    saving = saving,
                    saveFailed = saveFailed,
                    onClose = ::close,
                    onSubmit = { announced ->
                        if (editing && draft != null) {
                            viewModel.updateDraft(game.id, draft.id, announced, ::close)
                        } else {
                            viewModel.startRound(game.id, announced, ::close)
                        }
                    },
                )
            }
        }
        GameOverlay.Result -> if (draft != null) {
            ResultSheet(
                round = nextRound,
                trickCount = trickCount,
                firstName = firstName,
                secondName = secondName,
                announced = draft.announced,
                showPoints = game.scoresVisible,
                saving = saving,
                saveFailed = saveFailed,
                onClose = ::close,
                onSubmit = { taken -> viewModel.finishRound(game.id, draft.id, taken, ::close) },
            )
        }
        GameOverlay.EditRound -> if (selectedRound != null) {
            EditRoundSheet(
                round = selectedRound,
                trickCount = trickCount,
                firstName = firstName,
                secondName = secondName,
                showPoints = game.scoresVisible,
                saving = saving,
                saveFailed = saveFailed,
                onClose = ::close,
                onDelete = { show(GameOverlay.DeleteRound) },
                onSubmit = { announced, taken ->
                    viewModel.updateRound(game.id, selectedRound.id, announced, taken, ::close)
                },
            )
        }
        GameOverlay.NewGame, GameOverlay.Settings -> {
            val creating = overlay == GameOverlay.NewGame
            val initial = if (creating) GameSettings(trickCount = trickCount) else game.settings
            GameSettingsSheet(
                initial = initial,
                creating = creating,
                trickCountLocked = !creating && game.hasProgress,
                saving = saving,
                saveFailed = saveFailed,
                onClose = ::close,
                onSubmit = { settings ->
                    if (creating) viewModel.newGame(settings) { close() }
                    else viewModel.updateSettings(game.id, settings, ::close)
                },
            )
        }
        GameOverlay.Reset -> ConfirmGameChange(
            title = stringResource(R.string.hokm_reset_scores),
            body = stringResource(R.string.hokm_reset_body),
            action = stringResource(R.string.hokm_reset_action),
            saving = saving,
            saveFailed = saveFailed,
            onClose = ::close,
            onConfirm = { viewModel.reset(game.id, ::close) },
        )
        GameOverlay.Finish -> ConfirmGameChange(
            title = stringResource(R.string.hokm_finish_game),
            body = stringResource(if (draft == null) R.string.hokm_finish_body else R.string.hokm_finish_pending_body),
            action = stringResource(R.string.hokm_finish_game),
            saving = saving,
            saveFailed = saveFailed,
            onClose = ::close,
            onConfirm = { viewModel.finishGame(game.id, ::close) },
        )
        GameOverlay.DeleteRound -> if (selectedRound != null) {
            ConfirmGameChange(
                title = stringResource(R.string.hokm_delete_round_title, selectedRound.number.localized()),
                body = stringResource(R.string.hokm_delete_round_body),
                action = stringResource(R.string.hokm_delete_round),
                saving = saving,
                saveFailed = saveFailed,
                onClose = { show(GameOverlay.EditRound) },
                onConfirm = { viewModel.deleteRound(game.id, selectedRound.id, ::close) },
            )
        }
        GameOverlay.Rules -> HokmSheet(
            title = stringResource(R.string.hokm_rules_title),
            saving = false,
            onClose = ::close,
        ) {
            HokmRulesBody()
        }
        GameOverlay.Stats -> HokmSheet(
            title = stringResource(R.string.hokm_stats),
            saving = false,
            onClose = ::close,
        ) {
            MatchStats(game, firstName, secondName)
        }
        null -> Unit
    }
}

@Composable
private fun ScoreBoard(game: HokmGame, firstName: String, secondName: String, enabled: Boolean, onVisibility: () -> Unit) {
    val hero = LocalYarHero.current
    val firstPlayers = game.settings.firstTeam.playerNames()
    val secondPlayers = game.settings.secondTeam.playerNames()
    val showPlayers = firstPlayers.isNotEmpty() || secondPlayers.isNotEmpty()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(27.dp),
        color = hero.background,
        contentColor = hero.text,
    ) {
        Box {
            Canvas(Modifier.matchParentSize()) {
                val pivot = Offset(size.width * 0.13f, size.height * 0.27f)
                val stroke = Stroke(1.dp.toPx())
                listOf(50.dp, 71.dp, 92.dp).forEach { radius ->
                    drawCircle(hero.line.copy(alpha = 0.55f), radius.toPx(), pivot, style = stroke)
                }
            }
            Column(
                Modifier.fillMaxWidth().padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(if (game.isFinished) R.string.hokm_final_score else R.string.hokm_total_score),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Surface(
                        color = hero.deep,
                        contentColor = hero.muted,
                        border = BorderStroke(1.dp, hero.line),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            stringResource(R.string.hokm_trick_pill, game.settings.trickCount.localized()),
                            Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    if (!game.isFinished) {
                        IconButton(onClick = onVisibility, enabled = enabled) {
                            YarIcon(
                                if (game.scoresVisible) YarSymbol.Eye else YarSymbol.EyeOff,
                                stringResource(if (game.scoresVisible) R.string.hokm_hide_scores else R.string.hokm_show_scores),
                            )
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TeamScore(
                        firstName, firstPlayers, showPlayers, game.firstTotal.takeIf { game.scoresVisible }, hero.firstAccent,
                        Modifier.weight(1f).testTag("first_total"),
                    )
                    VerticalDivider(Modifier.height(62.dp), color = hero.line)
                    TeamScore(
                        secondName, secondPlayers, showPlayers, game.secondTotal.takeIf { game.scoresVisible }, hero.secondAccent,
                        Modifier.weight(1f).testTag("second_total"),
                    )
                }
                if (game.scoresVisible && (game.rounds.isNotEmpty() || game.isFinished)) {
                    val leader = when (game.leader) {
                        TeamSide.First -> firstName
                        TeamSide.Second -> secondName
                        null -> null
                    }
                    val status = if (leader == null) stringResource(R.string.hokm_tied)
                    else stringResource(if (game.isFinished) R.string.hokm_winner else R.string.hokm_leader, leader)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        YarIcon(YarSymbol.Trophy, color = hero.secondAccent, size = if (game.isFinished) 24.dp else 16.dp)
                        Text(
                            status,
                            color = if (game.isFinished) hero.text else hero.muted,
                            style = if (game.isFinished) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamScore(
    name: String,
    players: String,
    showPlayers: Boolean,
    total: Long?,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val hero = LocalYarHero.current
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showPlayers) {
            if (players.isEmpty()) {
                Spacer(Modifier.height(20.dp))
            } else {
                Text(
                    players,
                    style = MaterialTheme.typography.bodySmall,
                    color = hero.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            total?.localized() ?: "—",
            color = accent,
            style = if (total == null || total < 100_000) MaterialTheme.typography.displayMedium
            else MaterialTheme.typography.headlineSmall,
            maxLines = 1,
        )
    }
}

@Composable
private fun RoundActionCard(
    round: Int,
    game: HokmGame,
    firstName: String,
    secondName: String,
    forced: TeamNumbers?,
    enabled: Boolean,
    onOpen: () -> Unit,
    onFix: () -> Unit,
) {
    val draft = game.draft
    val isForced = forced != null || draft?.isForced == true
    val announced = draft?.announced ?: forced
    val nextForced = game.settings.forcedRounds.nextFrom(if (draft == null) round else round + 1)
    YarPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                YarTile(if (isForced || draft != null) YarSymbol.Cards else YarSymbol.Plus, size = 43.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(
                            when {
                                isForced -> R.string.hokm_forced_number
                                draft == null -> R.string.hokm_next_round
                                else -> R.string.hokm_round_in_progress
                            },
                            round.localized(),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        stringResource(when {
                            draft != null -> R.string.hokm_in_progress_body
                            isForced -> R.string.hokm_forced_body
                            else -> R.string.hokm_next_body
                        }),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (announced != null) {
                AnnouncementSummary(firstName, secondName, announced)
                if (HokmRules.hasGuaranteedMiss(announced, game.settings.trickCount)) {
                    OverbidNotice(game.settings.trickCount)
                }
            }
            if (!isForced && nextForced != null && nextForced > round) {
                Text(
                    stringResource(R.string.hokm_forced_countdown, nextForced.localized(), (nextForced - round).localized()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            YarPrimaryButton(
                stringResource(when {
                    draft != null -> R.string.hokm_finish_round
                    isForced -> R.string.hokm_start_forced
                    else -> R.string.hokm_start_round
                }),
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                symbol = if (draft == null) YarSymbol.Plus else YarSymbol.Check,
                enabled = enabled,
            )
            if (draft != null && !draft.isForced) {
                TextButton(onClick = onFix, enabled = enabled, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.hokm_fix_announcements))
                }
            }
        }
    }
}

@Composable
private fun RoundColumnLabels(firstName: String, secondName: String, editable: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.hokm_round_label),
            Modifier.size(width = 34.dp, height = 21.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        listOf(firstName, secondName).forEach { name ->
            Text(
                name,
                Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (editable) Spacer(Modifier.size(17.dp))
    }
}

@Composable
private fun RoundRow(round: HokmRound, editable: Boolean, showPoints: Boolean, enabled: Boolean, onEdit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(15.dp)
    val border = BorderStroke(1.dp, colors.outlineVariant)
    val content: @Composable () -> Unit = {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                Modifier.sizeIn(minWidth = 34.dp, minHeight = 34.dp),
                shape = CircleShape,
                color = if (round.isForced) colors.tertiaryContainer else colors.surfaceContainerHigh,
            ) {
                Box(Modifier.padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                    Text(round.number.localized(), style = MaterialTheme.typography.labelMedium)
                }
            }
            RoundTeam(round.points.first.takeIf { showPoints }, round.announced.first, round.taken.first, colors.primary, Modifier.weight(1f))
            RoundTeam(
                round.points.second.takeIf { showPoints }, round.announced.second, round.taken.second,
                colors.tertiary, Modifier.weight(1f),
            )
            if (editable) YarIcon(YarSymbol.Next, color = colors.onSurfaceVariant, size = 17.dp)
        }
    }
    if (editable) {
        Surface(
            onClick = onEdit,
            enabled = enabled,
            shape = shape,
            color = colors.surface,
            border = border,
            content = content,
        )
    } else {
        Surface(shape = shape, color = colors.surface, border = border, content = content)
    }
}

@Composable
private fun RoundTeam(points: Int?, announced: Int, taken: Int, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        if (points != null) {
            Text(
                stringResource(R.string.hokm_points, points.localized()),
                style = MaterialTheme.typography.titleSmall,
                color = if (points == 0) MaterialTheme.colorScheme.onSurfaceVariant else accent,
            )
        }
        Text(
            stringResource(R.string.hokm_round_numbers, announced.localized(), taken.localized()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConfirmGameChange(
    title: String,
    body: String,
    action: String,
    saving: Boolean,
    saveFailed: Boolean,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        properties = DialogProperties(dismissOnBackPress = !saving, dismissOnClickOutside = !saving),
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(body)
                if (saveFailed) SaveError()
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !saving) {
                Text(action, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose, enabled = !saving) { Text(stringResource(R.string.hokm_cancel)) }
        },
    )
}
