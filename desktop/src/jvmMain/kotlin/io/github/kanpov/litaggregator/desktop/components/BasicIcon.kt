package io.github.kanpov.litaggregator.desktop.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp

@Composable
fun BasicIcon(painter: Painter, size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier.size(size)
    )
}
