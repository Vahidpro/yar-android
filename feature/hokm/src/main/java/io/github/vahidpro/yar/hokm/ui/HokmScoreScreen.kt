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
import io.github.vahidpro.yar.design.YarColors
import io.github.vahidpro.yar.design.YarEmptyState
import io.github.vahidpro.yar.design.YarIcon
import io.github.vahidpro.yar.design.YarPanel
import io.github.vahidpro.yar.design.YarPrimaryButton
import io.github.vahidpro.yar.design.YarScreenBar
import io.github.vahidpro.yar.design.YarSectionTitle
import io.github.vahidpro.yar.design.YarSymbol
import io.github.vahidpro.yar.design.YarTile
import io.github.vahidpro.yar.design.persian
import io.github.vahidpro.yar.hokm.R
import io.github.vahidpro.yar.hokm.domain.GameSettings
import io.github.vahidpro.yar.hokm.domain.HokmGame
import io.github.vahidpro.yar.hokm.domain.HokmRound

private enum class GameOverlay {
    Announce, Result, FixAnnouncements, EditRound, EditTeams, NewGame, Reset, DeleteRound, Rules,
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
    val nextRound = game.rounds.size + 1
    val draft = game.draft
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
            if (game.isActive) {
                IconButton(onClick = { show(GameOverlay.EditTeams) }, enabled = !saving) {
                    YarIcon(YarSymbol.Users, stringResource(R.string.hokm_edit_teams))
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
            item(key = "score") { ScoreBoard(game, firstName, secondName) }
            item(key = "next") {
                if (game.isActive) {
                    RoundActionCard(
                        round = nextRound,
                        game = game,
                        firstName = firstName,
                        secondName = secondName,
                        enabled = !saving,
                        onOpen = { show(if (draft == null) GameOverlay.Announce else GameOverlay.Result) },
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
                        stringResource(R.string.hokm_round_count, game.rounds.size.persian()),
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
                item(key = "round_columns") { RoundColumnLabels(firstName, secondName, game.isActive) }
                items(game.rounds.asReversed(), key = HokmRound::id) { round ->
                    RoundRow(
                        round = round,
                        editable = game.isActive,
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
                saving = saving,
                saveFailed = saveFailed,
                onClose = ::close,
                onDelete = { show(GameOverlay.DeleteRound) },
                onSubmit = { announced, taken ->
                    viewModel.updateRound(game.id, selectedRound.id, announced, taken, ::close)
                },
            )
        }
        GameOverlay.NewGame, GameOverlay.EditTeams -> {
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
        GameOverlay.DeleteRound -> if (selectedRound != null) {
            ConfirmGameChange(
                title = stringResource(R.string.hokm_delete_round_title, selectedRound.number.persian()),
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
        null -> Unit
    }
}

@Composable
private fun ScoreBoard(game: HokmGame, firstName: String, secondName: String) {
    val firstPlayers = game.settings.firstTeam.playerNames()
    val secondPlayers = game.settings.secondTeam.playerNames()
    val showPlayers = firstPlayers.isNotEmpty() || secondPlayers.isNotEmpty()
    val hasRounds = game.rounds.isNotEmpty()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(27.dp),
        color = YarColors.forest,
        contentColor = YarColors.onForest,
    ) {
        Box {
            Canvas(Modifier.matchParentSize()) {
                val pivot = Offset(size.width * 0.13f, size.height * 0.27f)
                val stroke = Stroke(1.dp.toPx())
                listOf(50.dp, 71.dp, 92.dp).forEach { radius ->
                    drawCircle(YarColors.forestLine.copy(alpha = 0.55f), radius.toPx(), pivot, style = stroke)
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
                        stringResource(R.string.hokm_total_score),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Surface(
                        color = YarColors.forestDeep,
                        contentColor = YarColors.mutedOnForest,
                        border = BorderStroke(1.dp, YarColors.forestLine),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            stringResource(R.string.hokm_trick_pill, game.settings.trickCount.persian()),
                            Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TeamScore(
                        firstName, firstPlayers, showPlayers, game.firstTotal, YarColors.lime,
                        Modifier.weight(1f).testTag("first_total"),
                    )
                    VerticalDivider(Modifier.height(62.dp), color = YarColors.forestLine)
                    TeamScore(
                        secondName, secondPlayers, showPlayers, game.secondTotal, YarColors.sand,
                        Modifier.weight(1f).testTag("second_total"),
                    )
                }
                if (hasRounds) {
                    val status = when {
                        game.firstTotal > game.secondTotal -> stringResource(R.string.hokm_leader, firstName)
                        game.secondTotal > game.firstTotal -> stringResource(R.string.hokm_leader, secondName)
                        else -> stringResource(R.string.hokm_tied)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        YarIcon(YarSymbol.Trophy, color = YarColors.sand, size = 16.dp)
                        Text(status, color = YarColors.mutedOnForest, style = MaterialTheme.typography.bodySmall)
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
    total: Long,
    accent: Color,
    modifier: Modifier = Modifier,
) {
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
                    color = YarColors.mutedOnForest,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            total.persian(),
            color = accent,
            style = if (total < 100_000) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineSmall,
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
    enabled: Boolean,
    onOpen: () -> Unit,
    onFix: () -> Unit,
) {
    val draft = game.draft
    YarPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                YarTile(if (draft == null) YarSymbol.Plus else YarSymbol.Cards, size = 43.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(
                            if (draft == null) R.string.hokm_next_round else R.string.hokm_round_in_progress,
                            round.persian(),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        stringResource(if (draft == null) R.string.hokm_next_body else R.string.hokm_in_progress_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (draft != null) {
                AnnouncementSummary(firstName, secondName, draft.announced)
            }
            YarPrimaryButton(
                stringResource(if (draft == null) R.string.hokm_start_round else R.string.hokm_finish_round),
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                symbol = if (draft == null) YarSymbol.Plus else YarSymbol.Check,
                enabled = enabled,
            )
            if (draft != null) {
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
private fun RoundRow(round: HokmRound, editable: Boolean, enabled: Boolean, onEdit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(15.dp)
    val border = BorderStroke(1.dp, colors.outlineVariant)
    val content: @Composable () -> Unit = {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(Modifier.size(34.dp), shape = CircleShape, color = colors.surfaceContainerHigh) {
                Box(contentAlignment = Alignment.Center) {
                    Text(round.number.persian(), style = MaterialTheme.typography.labelMedium)
                }
            }
            RoundTeam(round.points.first, round.announced.first, round.taken.first, colors.primary, Modifier.weight(1f))
            RoundTeam(
                round.points.second, round.announced.second, round.taken.second, colors.tertiary, Modifier.weight(1f),
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
private fun RoundTeam(points: Int, announced: Int, taken: Int, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            stringResource(R.string.hokm_points, points.persian()),
            style = MaterialTheme.typography.titleSmall,
            color = if (points == 0) MaterialTheme.colorScheme.onSurfaceVariant else accent,
        )
        Text(
            stringResource(R.string.hokm_round_numbers, announced.persian(), taken.persian()),
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
