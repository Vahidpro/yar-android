package io.github.vahidpro.yar.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

enum class YarSymbol {
    Spade, Cards, History, Settings, Back, Next, More, Plus, Edit,
    Help, Reset, Check, Close, Sun, Moon, System, Trash, Trophy, Users, Lock, Eye, EyeOff, Chart, Share,
}

@Composable
fun YarIcon(
    symbol: YarSymbol,
    description: String? = null,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 23.dp,
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val accessibility = if (description == null) Modifier
    else Modifier.semantics { contentDescription = description }
    Canvas(modifier.size(size).then(accessibility)) {
        val horizontal = if (rtl && (symbol == YarSymbol.Back || symbol == YarSymbol.Next)) -1 else 1
        scale(this.size.width / 24, this.size.height / 24, Offset.Zero) {
            if (horizontal == -1) scale(-1f, 1f, Offset(12f, 12f)) { drawSymbol(symbol, color) }
            else drawSymbol(symbol, color)
        }
    }
}

private fun DrawScope.drawSymbol(symbol: YarSymbol, color: Color) {
    val stroke = Stroke(width = 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    fun line(x: Float, y: Float, endX: Float, endY: Float) {
        drawLine(color, Offset(x, y), Offset(endX, endY), stroke.width, StrokeCap.Round)
    }

    fun path(build: Path.() -> Unit) = drawPath(Path().apply(build), color, style = stroke)

    fun circle(x: Float, y: Float, radius: Float, filled: Boolean = false) {
        if (filled) drawCircle(color, radius, Offset(x, y))
        else drawCircle(color, radius, Offset(x, y), style = stroke)
    }

    fun box(x: Float, y: Float, w: Float, h: Float, radius: Float) {
        drawRoundRect(color, Offset(x, y), Size(w, h), CornerRadius(radius), style = stroke)
    }

    when (symbol) {
        YarSymbol.Spade -> drawSpade(color, Offset(2f, 2f), 20f)
        YarSymbol.Cards -> {
            path {
                moveTo(5f, 17f)
                lineTo(3.2f, 16.3f)
                quadraticTo(2f, 15.8f, 2.5f, 14.4f)
                lineTo(5.8f, 5.2f)
                quadraticTo(6.2f, 3.8f, 7.7f, 4.3f)
                lineTo(9.5f, 5f)
            }
            box(9f, 4f, 12f, 16f, 2.5f)
            path { moveTo(15f, 9f); lineTo(17.5f, 12f); lineTo(15f, 15f); lineTo(12.5f, 12f); close() }
        }
        YarSymbol.History -> {
            circle(12f, 12f, 8.6f)
            path { moveTo(12f, 7f); lineTo(12f, 12f); lineTo(15.5f, 14f) }
        }
        YarSymbol.Settings -> {
            line(4f, 6f, 20f, 6f)
            line(4f, 12f, 20f, 12f)
            line(4f, 18f, 20f, 18f)
            line(8f, 3.7f, 8f, 8.3f)
            line(16f, 9.7f, 16f, 14.3f)
            line(10f, 15.7f, 10f, 20.3f)
        }
        YarSymbol.Back -> {
            line(5f, 12f, 20f, 12f)
            path { moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f) }
        }
        YarSymbol.Next -> path { moveTo(9f, 6f); lineTo(15f, 12f); lineTo(9f, 18f) }
        YarSymbol.More -> listOf(5f, 12f, 19f).forEach { circle(it, 12f, 1.5f, true) }
        YarSymbol.Plus -> {
            line(5f, 12f, 19f, 12f)
            line(12f, 5f, 12f, 19f)
        }
        YarSymbol.Edit -> path {
            moveTo(4f, 20f)
            lineTo(4.8f, 15.4f)
            lineTo(15.8f, 4.4f)
            quadraticTo(17f, 3.2f, 18.2f, 4.4f)
            lineTo(19.6f, 5.8f)
            quadraticTo(20.8f, 7f, 19.6f, 8.2f)
            lineTo(8.6f, 19.2f)
            close()
            moveTo(13.5f, 6.7f)
            lineTo(17.3f, 10.5f)
        }
        YarSymbol.Help -> {
            circle(12f, 12f, 9f)
            path {
                moveTo(9.3f, 9.4f)
                cubicTo(9.5f, 6.9f, 14.5f, 6.8f, 14.5f, 9.4f)
                cubicTo(14.5f, 11f, 12f, 11.5f, 12f, 13.1f)
            }
            circle(12f, 16.5f, 1f, true)
        }
        YarSymbol.Reset -> {
            drawArc(color, 204f, 300f, false, Offset(4f, 4f), Size(16f, 16f), style = stroke)
            path { moveTo(4f, 5.5f); lineTo(4f, 10f); lineTo(8.5f, 10f) }
        }
        YarSymbol.Check -> path { moveTo(5f, 12f); lineTo(10f, 17f); lineTo(19f, 7f) }
        YarSymbol.Close -> {
            line(6f, 6f, 18f, 18f)
            line(6f, 18f, 18f, 6f)
        }
        YarSymbol.Sun -> {
            circle(12f, 12f, 4f)
            line(12f, 2.5f, 12f, 5f)
            line(12f, 19f, 12f, 21.5f)
            line(2.5f, 12f, 5f, 12f)
            line(19f, 12f, 21.5f, 12f)
            line(5.3f, 5.3f, 7f, 7f)
            line(17f, 17f, 18.7f, 18.7f)
            line(5.3f, 18.7f, 7f, 17f)
            line(17f, 7f, 18.7f, 5.3f)
        }
        YarSymbol.Moon -> path {
            moveTo(20.8f, 15.2f)
            cubicTo(17f, 16.6f, 12.6f, 14.7f, 11f, 11f)
            cubicTo(10f, 8.7f, 10.1f, 6.1f, 11.2f, 3.7f)
            cubicTo(6.6f, 4.1f, 3.3f, 7.8f, 3.6f, 12.5f)
            cubicTo(4f, 20.9f, 16.2f, 23.6f, 20.8f, 15.2f)
            close()
        }
        YarSymbol.System -> {
            box(3f, 4f, 18f, 13f, 2f)
            line(8f, 21f, 16f, 21f)
            line(12f, 17f, 12f, 21f)
        }
        YarSymbol.Trash -> {
            line(4f, 7f, 20f, 7f)
            path { moveTo(9f, 7f); lineTo(9f, 4f); lineTo(15f, 4f); lineTo(15f, 7f) }
            path { moveTo(6f, 7f); lineTo(7f, 20f); lineTo(17f, 20f); lineTo(18f, 7f) }
            line(10.5f, 10.5f, 10.5f, 16.5f)
            line(13.5f, 10.5f, 13.5f, 16.5f)
        }
        YarSymbol.Trophy -> {
            path {
                moveTo(7f, 4f); lineTo(17f, 4f); lineTo(17f, 10f)
                cubicTo(17f, 16f, 7f, 16f, 7f, 10f)
                close()
                moveTo(7f, 6f); lineTo(3.5f, 6f)
                quadraticTo(3.5f, 12f, 7.8f, 12f)
                moveTo(17f, 6f); lineTo(20.5f, 6f)
                quadraticTo(20.5f, 12f, 16.2f, 12f)
            }
            line(12f, 15f, 12f, 20f)
            line(8f, 20f, 16f, 20f)
        }
        YarSymbol.Users -> {
            circle(9f, 8f, 3f)
            path {
                moveTo(3.5f, 19f)
                cubicTo(3.5f, 12.8f, 14.5f, 12.8f, 14.5f, 19f)
                moveTo(15f, 5.1f)
                cubicTo(19.5f, 4.1f, 19.5f, 11.8f, 15f, 10.8f)
                moveTo(16f, 14f)
                quadraticTo(20.5f, 14.4f, 20.5f, 19f)
            }
        }
        YarSymbol.Lock -> {
            box(5f, 10f, 14f, 10f, 2f)
            path { moveTo(8f, 10f); lineTo(8f, 7f); cubicTo(8f, 2f, 16f, 2f, 16f, 7f); lineTo(16f, 10f) }
            circle(12f, 15f, 1f, true)
        }
        YarSymbol.Eye, YarSymbol.EyeOff -> {
            path {
                moveTo(2.5f, 12f)
                cubicTo(7.5f, 4f, 16.5f, 4f, 21.5f, 12f)
                cubicTo(16.5f, 20f, 7.5f, 20f, 2.5f, 12f)
                close()
            }
            circle(12f, 12f, 2.7f)
            if (symbol == YarSymbol.EyeOff) line(4f, 4f, 20f, 20f)
        }
        YarSymbol.Chart -> {
            path { moveTo(4f, 4f); lineTo(4f, 20f); lineTo(21f, 20f) }
            line(8.5f, 16f, 8.5f, 11f)
            line(13.5f, 16f, 13.5f, 7f)
            line(18.5f, 16f, 18.5f, 4f)
        }
        YarSymbol.Share -> {
            line(8.5f, 11f, 15.5f, 7f)
            line(8.5f, 13f, 15.5f, 17f)
            circle(6f, 12f, 2.5f)
            circle(18f, 5.5f, 2.5f)
            circle(18f, 18.5f, 2.5f)
        }
    }
}

fun DrawScope.drawSpade(color: Color, at: Offset, side: Float) {
    scale(side / 24f, side / 24f, Offset.Zero) {
        val x = at.x * 24f / side
        val y = at.y * 24f / side
        drawPath(Path().apply {
            moveTo(x + 12, y + 1)
            cubicTo(x + 9, y + 6, x + 2, y + 9, x + 2, y + 14.1f)
            cubicTo(x + 2, y + 18.5f, x + 6.1f, y + 20.4f, x + 10, y + 17.9f)
            quadraticTo(x + 9.5f, y + 21, x + 7, y + 23)
            lineTo(x + 17, y + 23)
            quadraticTo(x + 14.5f, y + 21, x + 14, y + 17.9f)
            cubicTo(x + 17.9f, y + 20.4f, x + 22, y + 18.5f, x + 22, y + 14.1f)
            cubicTo(x + 22, y + 9, x + 15, y + 6, x + 12, y + 1)
            close()
        }, color)
    }
}
