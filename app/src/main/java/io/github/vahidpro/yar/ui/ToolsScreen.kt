package io.github.vahidpro.yar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.vahidpro.yar.R
import io.github.vahidpro.yar.design.YarColors
import io.github.vahidpro.yar.design.YarIcon
import io.github.vahidpro.yar.design.YarPrimaryButton
import io.github.vahidpro.yar.design.YarSectionTitle
import io.github.vahidpro.yar.design.YarSymbol
import io.github.vahidpro.yar.design.YarTile
import io.github.vahidpro.yar.design.drawSpade
import io.github.vahidpro.yar.hokm.ui.HokmGamesState

@Composable
fun ToolsScreen(
    state: HokmGamesState,
    onOpenHokm: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenGame: (Long?) -> Unit,
    onOpenHistory: () -> Unit,
) {
    val ready = state as? HokmGamesState.Ready
    val recent = ready?.games?.firstOrNull { it.hasProgress }
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            YarTile(YarSymbol.Spade, size = 53.dp, color = YarColors.forest, contentColor = YarColors.lime)
            Column {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            YarSectionTitle(stringResource(R.string.hokm_category))
            HokmHero(resume = ready?.active?.hasProgress == true, onOpen = onOpenHokm)
        }
        if (recent != null) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    YarSectionTitle(stringResource(R.string.recent_games), Modifier.weight(1f))
                    TextButton(onClick = onOpenHistory) { Text(stringResource(R.string.all_games)) }
                }
                GameHistoryCard(recent) { onOpenGame(recent.id.takeUnless { recent.isActive }) }
            }
        }
        RulesLink(onClick = onOpenRules)
    }
}

@Composable
private fun HokmHero(resume: Boolean, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = YarColors.forest,
        contentColor = YarColors.onForest,
    ) {
        BoxWithConstraints {
            val showArt = maxWidth >= 302.dp && LocalDensity.current.fontScale <= 1.3f
            if (showArt) {
                CardFan(Modifier.align(Alignment.TopEnd).padding(top = 2.dp).height(178.dp).widthIn(max = 149.dp))
            }
            Column(Modifier.fillMaxWidth().padding(19.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(
                    modifier = if (showArt) Modifier.padding(end = 115.dp) else Modifier,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        stringResource(R.string.hokm_hero_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = YarColors.onForest,
                    )
                    Text(
                        stringResource(R.string.hokm_hero_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = YarColors.mutedOnForest,
                    )
                }
                Spacer(Modifier.height(3.dp))
                YarPrimaryButton(
                    text = stringResource(if (resume) R.string.hokm_resume else R.string.hokm_start),
                    onClick = onOpen,
                    modifier = Modifier.fillMaxWidth(),
                    symbol = if (resume) YarSymbol.Next else YarSymbol.Plus,
                    onForest = true,
                )
            }
        }
    }
}

@Composable
private fun CardFan(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth()) {
        val unit = size.width / 149
        fun px(value: Int) = value * unit
        val cream = Color(0xFFFBF8DE)
        val green = Color(0xFF87AE83)
        val ink = YarColors.forestDeep
        val line = YarColors.forestLine
        val orbit = Offset(px(65), px(81))
        drawCircle(line, px(58), orbit, style = Stroke(px(1)))
        drawCircle(line.copy(alpha = 0.7f), px(75), orbit, style = Stroke(px(1)))
        drawCircle(YarColors.sand, px(2), Offset(px(111), px(30)))
        drawCircle(YarColors.lime, px(2), Offset(px(19), px(117)))

        val cardSize = Size(px(62), px(92))
        val corner = CornerRadius(px(9))
        val back = Offset(px(54), px(35))
        rotate(17f, Offset(back.x + cardSize.width / 2, back.y + cardSize.height / 2)) {
            drawRoundRect(Color.Black.copy(alpha = 0.18f), back + Offset(px(1), px(5)), cardSize, corner)
            drawRoundRect(green, back, cardSize, corner)
            drawRoundRect(
                ink.copy(alpha = 0.5f),
                back + Offset(px(6), px(6)),
                Size(px(50), px(80)),
                CornerRadius(px(5)),
                style = Stroke(px(1)),
            )
        }
        val front = Offset(px(24), px(48))
        rotate(-14f, Offset(front.x + cardSize.width / 2, front.y + cardSize.height / 2)) {
            drawRoundRect(Color.Black.copy(alpha = 0.21f), front + Offset(px(1), px(5)), cardSize, corner)
            drawRoundRect(cream, front, cardSize, corner)
            drawSpade(ink, front + Offset(px(17), px(31)), px(28))
            drawSpade(ink, front + Offset(px(7), px(7)), px(10))
            drawSpade(ink, front + Offset(px(45), px(74)), px(10))
        }
    }
}

@Composable
private fun RulesLink(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(21.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 13.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YarTile(YarSymbol.Help)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rules_link_title), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.rules_link_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            YarIcon(YarSymbol.Next, color = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
        }
    }
}
