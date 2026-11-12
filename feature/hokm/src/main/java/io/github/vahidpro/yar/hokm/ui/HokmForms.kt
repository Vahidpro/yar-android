package io.github.vahidpro.yar.hokm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vahidpro.yar.design.YarIcon
import io.github.vahidpro.yar.design.YarPrimaryButton
import io.github.vahidpro.yar.design.YarSectionTitle
import io.github.vahidpro.yar.design.YarSymbol
import io.github.vahidpro.yar.design.asciiDigits
import io.github.vahidpro.yar.design.localized
import io.github.vahidpro.yar.design.localizedDigits
import io.github.vahidpro.yar.hokm.R
import io.github.vahidpro.yar.hokm.domain.ForcedRounds
import io.github.vahidpro.yar.hokm.domain.GameSettings
import io.github.vahidpro.yar.hokm.domain.HokmRound
import io.github.vahidpro.yar.hokm.domain.HokmRules
import io.github.vahidpro.yar.hokm.domain.Team
import io.github.vahidpro.yar.hokm.domain.TeamNumbers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HokmSheet(
    title: String,
    saving: Boolean,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val busy by rememberUpdatedState(saving)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !busy },
    )
    ModalBottomSheet(
        onDismissRequest = { if (!busy) onClose() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        sheetMaxWidth = 560.dp,
        sheetGesturesEnabled = !saving,
    ) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                YarSectionTitle(title, Modifier.weight(1f))
                IconButton(onClick = onClose, enabled = !saving) {
                    YarIcon(YarSymbol.Close, stringResource(R.string.hokm_close))
                }
            }
            content()
        }
    }
}

@Composable
internal fun AnnouncementSheet(
    round: Int,
    trickCount: Int,
    minimumAnnouncement: Int,
    firstName: String,
    secondName: String,
    initial: TeamNumbers?,
    saving: Boolean,
    saveFailed: Boolean,
    onClose: () -> Unit,
    onSubmit: (TeamNumbers) -> Unit,
) {
    var first by rememberSaveable { mutableStateOf(initial?.first?.toString().orEmpty()) }
    var second by rememberSaveable { mutableStateOf(initial?.second?.toString().orEmpty()) }
    val numbers = teamNumbers(first, second)
    val valid = numbers != null && HokmRules.validAnnouncements(numbers, trickCount, minimumAnnouncement)
    HokmSheet(
        title = stringResource(R.string.hokm_announce_title, round.localized()),
        saving = saving,
        onClose = onClose,
    ) {
        AnnouncementFields(
            firstName, secondName, first, second, trickCount, minimumAnnouncement, !saving,
            { first = it }, { second = it },
        )
        if (saveFailed) SaveError()
        YarPrimaryButton(
            text = stringResource(if (initial == null) R.string.hokm_save_announcements else R.string.hokm_save_edits),
            onClick = { if (numbers != null) onSubmit(numbers) },
            modifier = Modifier.fillMaxWidth(),
            symbol = YarSymbol.Check,
            enabled = valid && !saving,
        )
    }
}

@Composable
internal fun ResultSheet(
    round: Int,
    trickCount: Int,
    firstName: String,
    secondName: String,
    announced: TeamNumbers,
    showPoints: Boolean,
    saving: Boolean,
    saveFailed: Boolean,
    onClose: () -> Unit,
    onSubmit: (TeamNumbers) -> Unit,
) {
    var first by rememberSaveable { mutableStateOf("") }
    var second by rememberSaveable { mutableStateOf("") }
    val numbers = teamNumbers(first, second)?.takeIf { HokmRules.validResult(it, trickCount) }
    HokmSheet(
        title = stringResource(R.string.hokm_result_title, round.localized()),
        saving = saving,
        onClose = onClose,
    ) {
        AnnouncementSummary(firstName, secondName, announced)
        ResultFields(
            firstName, secondName, first, second, trickCount, !saving,
            onFirst = { first = it },
            onSecond = { second = it },
        )
        if (showPoints && numbers != null) PointsPreview(firstName, secondName, HokmRules.points(announced, numbers))
        if (saveFailed) SaveError()
        YarPrimaryButton(
            text = stringResource(R.string.hokm_save_result),
            onClick = { if (numbers != null) onSubmit(numbers) },
            modifier = Modifier.fillMaxWidth(),
            symbol = YarSymbol.Check,
            enabled = numbers != null && !saving,
        )
    }
}

@Composable
internal fun EditRoundSheet(
    round: HokmRound,
    trickCount: Int,
    firstName: String,
    secondName: String,
    showPoints: Boolean,
    saving: Boolean,
    saveFailed: Boolean,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onSubmit: (TeamNumbers, TeamNumbers) -> Unit,
) {
    var firstAnnounced by rememberSaveable { mutableStateOf(round.announced.first.toString()) }
    var secondAnnounced by rememberSaveable { mutableStateOf(round.announced.second.toString()) }
    var firstTaken by rememberSaveable { mutableStateOf(round.taken.first.toString()) }
    var secondTaken by rememberSaveable { mutableStateOf(round.taken.second.toString()) }
    val announced = if (round.isForced) round.announced else teamNumbers(firstAnnounced, secondAnnounced)
        ?.takeIf { HokmRules.validAnnouncements(it, trickCount, round.minimumAnnouncement) }
    val taken = teamNumbers(firstTaken, secondTaken)?.takeIf { HokmRules.validResult(it, trickCount) }
    HokmSheet(
        title = stringResource(R.string.hokm_edit_round, round.number.localized()),
        saving = saving,
        onClose = onClose,
    ) {
        if (round.isForced) {
            Text(stringResource(R.string.hokm_forced_hand), style = MaterialTheme.typography.titleSmall)
            AnnouncementSummary(firstName, secondName, round.announced)
        } else {
            AnnouncementFields(
                firstName, secondName, firstAnnounced, secondAnnounced, trickCount, round.minimumAnnouncement,
                !saving, { firstAnnounced = it }, { secondAnnounced = it },
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ResultFields(
            firstName, secondName, firstTaken, secondTaken, trickCount, !saving,
            onFirst = { firstTaken = it },
            onSecond = { secondTaken = it },
        )
        if (showPoints && announced != null && taken != null) {
            PointsPreview(firstName, secondName, HokmRules.points(announced, taken))
        }
        if (saveFailed) SaveError()
        YarPrimaryButton(
            text = stringResource(R.string.hokm_save_edits),
            onClick = { if (announced != null && taken != null) onSubmit(announced, taken) },
            modifier = Modifier.fillMaxWidth(),
            symbol = YarSymbol.Check,
            enabled = announced != null && taken != null && !saving,
        )
        TextButton(
            onClick = onDelete,
            enabled = !saving,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                YarIcon(YarSymbol.Trash, color = MaterialTheme.colorScheme.error, size = 18.dp)
                Text(stringResource(R.string.hokm_delete_round), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AnnouncementFields(
    firstName: String,
    secondName: String,
    first: String,
    second: String,
    trickCount: Int,
    minimumAnnouncement: Int,
    enabled: Boolean,
    onFirst: (String) -> Unit,
    onSecond: (String) -> Unit,
) {
    val range = minimumAnnouncement..trickCount
    val outOfRange = outsideRange(first, range) || outsideRange(second, range)
    val numbers = teamNumbers(first, second)
    val tooLow = numbers?.let { it.sum < trickCount } == true
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(stringResource(R.string.hokm_announced_label), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            NumberField(
                first, firstName, onFirst, enabled, outsideRange(first, range),
                Modifier.weight(1f).testTag("first_bid"), imeAction = ImeAction.Next,
            )
            NumberField(
                second, secondName, onSecond, enabled, outsideRange(second, range),
                Modifier.weight(1f).testTag("second_bid"),
            )
        }
        FormHint(
            text = if (outOfRange) stringResource(
                R.string.hokm_bid_range, minimumAnnouncement.localized(), trickCount.localized(),
            ) else stringResource(R.string.hokm_minimum_announce, trickCount.localized()),
            isError = outOfRange || tooLow,
        )
        if (!outOfRange && numbers != null && HokmRules.hasGuaranteedMiss(numbers, trickCount)) {
            OverbidNotice(trickCount)
        }
    }
}

@Composable
private fun ResultFields(
    firstName: String,
    secondName: String,
    first: String,
    second: String,
    trickCount: Int,
    enabled: Boolean,
    onFirst: (String) -> Unit,
    onSecond: (String) -> Unit,
) {
    val outOfRange = outsideRange(first, 0..trickCount) || outsideRange(second, 0..trickCount)
    val totalMismatch = teamNumbers(first, second)?.let { it.sum != trickCount } == true
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(stringResource(R.string.hokm_taken_label), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            NumberField(
                first, firstName, onFirst, enabled, outsideRange(first, 0..trickCount),
                Modifier.weight(1f).testTag("first_taken"), imeAction = ImeAction.Next,
            )
            NumberField(
                second, secondName, onSecond, enabled, outsideRange(second, 0..trickCount),
                Modifier.weight(1f).testTag("second_taken"),
            )
        }
        FormHint(
            text = if (outOfRange) stringResource(R.string.hokm_number_range, trickCount.localized())
            else stringResource(R.string.hokm_result_total, trickCount.localized()),
            isError = outOfRange || totalMismatch,
        )
    }
}

@Composable
internal fun OverbidNotice(trickCount: Int) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.tertiaryContainer,
        contentColor = colors.onTertiaryContainer,
    ) {
        Text(
            stringResource(R.string.hokm_overbid_note, trickCount.localized()),
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun AnnouncementSummary(firstName: String, secondName: String, numbers: TeamNumbers) {
    TeamNumberStrip(
        firstName, secondName,
        stringResource(R.string.hokm_announced_number, numbers.first.localized()),
        stringResource(R.string.hokm_announced_number, numbers.second.localized()),
    )
}

@Composable
private fun PointsPreview(firstName: String, secondName: String, numbers: TeamNumbers) {
    TeamNumberStrip(
        firstName, secondName,
        stringResource(R.string.hokm_points, numbers.first.localized()),
        stringResource(R.string.hokm_points, numbers.second.localized()),
    )
}

@Composable
private fun TeamNumberStrip(
    firstName: String,
    secondName: String,
    firstValue: String,
    secondValue: String,
) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        TeamNumberPill(firstName, firstValue, Modifier.weight(1f), first = true)
        TeamNumberPill(secondName, secondValue, Modifier.weight(1f), first = false)
    }
}

@Composable
private fun TeamNumberPill(
    name: String,
    label: String,
    modifier: Modifier,
    first: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (first) colors.secondaryContainer else colors.tertiaryContainer,
        contentColor = if (first) colors.onSecondaryContainer else colors.onTertiaryContainer,
    ) {
        Column(Modifier.padding(horizontal = 9.dp, vertical = 6.dp)) {
            Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
internal fun GameSettingsSheet(
    initial: GameSettings,
    creating: Boolean,
    trickCountLocked: Boolean,
    saving: Boolean,
    saveFailed: Boolean,
    onClose: () -> Unit,
    onSubmit: (GameSettings) -> Unit,
) {
    var firstName by rememberSaveable { mutableStateOf(initial.firstTeam.name) }
    var firstPlayerA by rememberSaveable { mutableStateOf(initial.firstTeam.firstPlayer) }
    var firstPlayerB by rememberSaveable { mutableStateOf(initial.firstTeam.secondPlayer) }
    var secondName by rememberSaveable { mutableStateOf(initial.secondTeam.name) }
    var secondPlayerA by rememberSaveable { mutableStateOf(initial.secondTeam.firstPlayer) }
    var secondPlayerB by rememberSaveable { mutableStateOf(initial.secondTeam.secondPlayer) }
    var trickText by rememberSaveable { mutableStateOf(initial.trickCount.toString()) }
    var minimumText by rememberSaveable { mutableStateOf(initial.minimumAnnouncement.toString()) }
    var intervalText by rememberSaveable { mutableStateOf(initial.forcedRounds.interval.toString()) }
    var forcedFirst by rememberSaveable { mutableStateOf(initial.forcedRounds.announced.first.toString()) }
    var forcedSecond by rememberSaveable { mutableStateOf(initial.forcedRounds.announced.second.toString()) }
    var showScores by rememberSaveable { mutableStateOf(initial.showScores) }
    val trickCount = trickText.toIntOrNull()?.takeIf { it in HokmRules.allowedTrickCounts }
    val minimum = minimumText.toIntOrNull()
    val interval = intervalText.toIntOrNull()
    val forced = teamNumbers(forcedFirst, forcedSecond)
    val validRange = if (trickCount != null && minimum != null) minimum..trickCount else null
    val settings = if (trickCount != null && minimum != null && interval != null && (interval == 0 || forced != null)) {
        GameSettings(
            firstTeam = Team(firstName, firstPlayerA, firstPlayerB),
            secondTeam = Team(secondName, secondPlayerA, secondPlayerB),
            trickCount = trickCount,
            minimumAnnouncement = minimum,
            forcedRounds = ForcedRounds(interval, forced ?: initial.forcedRounds.announced),
            showScores = showScores,
        ).takeIf(GameSettings::hasValidRules)
    } else null

    HokmSheet(
        title = stringResource(if (creating) R.string.hokm_new_game else R.string.hokm_game_settings),
        saving = saving,
        onClose = onClose,
    ) {
        TeamFields(
            stringResource(R.string.hokm_first_team), firstName, firstPlayerA, firstPlayerB, !saving,
            { firstName = it }, { firstPlayerA = it }, { firstPlayerB = it },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        TeamFields(
            stringResource(R.string.hokm_second_team), secondName, secondPlayerA, secondPlayerB, !saving,
            { secondName = it }, { secondPlayerA = it }, { secondPlayerB = it },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NumberField(
            value = trickText,
            label = stringResource(R.string.hokm_tricks_per_round),
            onChange = { value ->
                val previous = trickCount
                val next = value.toIntOrNull()
                if (previous != null && next != null && next in HokmRules.allowedTrickCounts) {
                    val priorDefault = minOf(7, previous).toString()
                    if (forcedFirst == priorDefault && forcedSecond == priorDefault) {
                        forcedFirst = minOf(7, next).toString()
                        forcedSecond = forcedFirst
                    }
                }
                trickText = value
            },
            enabled = !saving && !trickCountLocked,
            isError = outsideRange(trickText, HokmRules.allowedTrickCounts),
            modifier = Modifier.fillMaxWidth().testTag("tricks_per_round"),
        )
        if (trickCountLocked) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                YarIcon(YarSymbol.Lock, color = MaterialTheme.colorScheme.onSurfaceVariant, size = 16.dp)
                FormHint(stringResource(R.string.hokm_trick_count_locked))
            }
        }
        NumberField(
            value = minimumText,
            label = stringResource(R.string.hokm_minimum_bid_setting),
            onChange = { minimumText = it },
            enabled = !saving,
            isError = trickCount != null && outsideRange(minimumText, 0..trickCount),
            modifier = Modifier.fillMaxWidth().testTag("minimum_bid"),
        )
        if (trickCount != null && outsideRange(minimumText, 0..trickCount)) {
            FormHint(stringResource(R.string.hokm_number_range, trickCount.localized()), isError = true)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(stringResource(R.string.hokm_forced_hand), style = MaterialTheme.typography.titleMedium)
        NumberField(
            value = intervalText,
            label = stringResource(R.string.hokm_interval_setting),
            onChange = { intervalText = it },
            enabled = !saving,
            isError = outsideRange(intervalText, 0..Int.MAX_VALUE),
            modifier = Modifier.fillMaxWidth().testTag("forced_interval"),
            maxDigits = 10,
        )
        FormHint(stringResource(R.string.hokm_interval_hint))
        if (interval != null && interval > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                NumberField(
                    forcedFirst, Team(firstName).displayName(first = true), { forcedFirst = it }, !saving,
                    validRange != null && outsideRange(forcedFirst, validRange),
                    Modifier.weight(1f).testTag("first_forced_bid"), imeAction = ImeAction.Next,
                )
                NumberField(
                    forcedSecond, Team(secondName).displayName(first = false), { forcedSecond = it }, !saving,
                    validRange != null && outsideRange(forcedSecond, validRange),
                    Modifier.weight(1f).testTag("second_forced_bid"),
                )
            }
            if (validRange != null && !validRange.isEmpty()) {
                val invalidForced = outsideRange(forcedFirst, validRange) || outsideRange(forcedSecond, validRange)
                FormHint(
                    stringResource(R.string.hokm_bid_range, validRange.first.localized(), validRange.last.localized()),
                    isError = invalidForced,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.hokm_show_scores),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
            )
            Switch(checked = showScores, onCheckedChange = { showScores = it }, enabled = !saving)
        }
        if (saveFailed) SaveError()
        YarPrimaryButton(
            text = stringResource(if (creating) R.string.hokm_create_game else R.string.hokm_save_edits),
            onClick = {
                if (settings != null) onSubmit(settings)
            },
            modifier = Modifier.fillMaxWidth(),
            symbol = if (creating) YarSymbol.Plus else YarSymbol.Check,
            enabled = settings != null && !saving,
        )
    }
}

@Composable
private fun TeamFields(
    title: String,
    name: String,
    firstPlayer: String,
    secondPlayer: String,
    enabled: Boolean,
    onName: (String) -> Unit,
    onFirstPlayer: (String) -> Unit,
    onSecondPlayer: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        NameField(name, stringResource(R.string.hokm_team_name), onName, enabled, Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NameField(
                firstPlayer, stringResource(R.string.hokm_player_one),
                onFirstPlayer, enabled, Modifier.weight(1f),
            )
            NameField(
                secondPlayer, stringResource(R.string.hokm_player_two),
                onSecondPlayer, enabled, Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NameField(value: String, label: String, onChange: (String) -> Unit, enabled: Boolean, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= Team.nameLimit) onChange(it) },
        modifier = modifier,
        label = { Text(label) },
        shape = RoundedCornerShape(14.dp),
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
}

@Composable
private fun NumberField(
    value: String,
    label: String,
    onChange: (String) -> Unit,
    enabled: Boolean,
    isError: Boolean,
    modifier: Modifier,
    imeAction: ImeAction = ImeAction.Done,
    maxDigits: Int = 2,
) {
    OutlinedTextField(
        value = value.localizedDigits(),
        onValueChange = { input ->
            val ascii = input.asciiDigits()
            if (ascii.length <= maxDigits && ascii.all { it in '0'..'9' }) onChange(ascii)
        },
        modifier = modifier,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
        shape = RoundedCornerShape(14.dp),
        enabled = enabled,
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
    )
}

@Composable
private fun FormHint(text: String, isError: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun SaveError() {
    Text(
        stringResource(R.string.hokm_save_failed),
        Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

private fun teamNumbers(first: String, second: String): TeamNumbers? {
    val firstValue = first.toIntOrNull() ?: return null
    val secondValue = second.toIntOrNull() ?: return null
    return TeamNumbers(firstValue, secondValue)
}

private fun outsideRange(value: String, range: IntRange): Boolean =
    value.isNotEmpty() && value.toIntOrNull()?.let { it in range } != true
