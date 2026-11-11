package io.github.vahidpro.yar.hokm.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.vahidpro.yar.hokm.R
import io.github.vahidpro.yar.hokm.domain.Team

@Composable
fun Team.displayName(first: Boolean): String = name.ifBlank {
    stringResource(if (first) R.string.hokm_first_team else R.string.hokm_second_team)
}

internal fun Team.playerNames(): String =
    listOf(firstPlayer, secondPlayer).filter(String::isNotBlank).joinToString("، ")
