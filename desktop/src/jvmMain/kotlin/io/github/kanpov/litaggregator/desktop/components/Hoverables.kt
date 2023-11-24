package io.github.kanpov.litaggregator.desktop.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun HoverableIconButton(tooltip: String, iconPath: String, tint: Color = Color.Unspecified, action: () -> Unit) {
    BasicHoverable(tooltip) {
        BasicIcon(
            painter = painterResource(iconPath),
            size = 30.dp,
            modifier = Modifier.clickable { action() },
            tint = tint
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun HoverableStat(tooltip: String, iconPath: String, value: String) {
    BasicHoverable(tooltip) {
        Row {
            BasicIcon(
                painter = painterResource(iconPath),
                size = 30.dp
            )
            H6Text(
                text = value,
                highlight = true,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BasicHoverable(tooltip: String, content: @Composable () -> Unit) {
    TooltipArea(
        tooltip = {
            Surface(
                modifier = Modifier.shadow(4.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                H6Text(
                    text = tooltip,
                    italicize = true,
                    modifier = Modifier.padding(3.dp)
                )
            }
        },
        modifier = Modifier.padding(start = 10.dp),
        delayMillis = 600,
        tooltipPlacement = TooltipPlacement.CursorPoint(
            alignment = Alignment.BottomEnd,
            offset = DpOffset(x = 10.dp, y = 10.dp)
        )
    ) {
        content()
    }
}