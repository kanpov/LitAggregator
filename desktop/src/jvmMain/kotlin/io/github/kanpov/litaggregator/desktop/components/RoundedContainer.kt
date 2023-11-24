package io.github.kanpov.litaggregator.desktop.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RoundedContainer(shape: RoundedCornerShape, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Surface(
        shape = shape,
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Row(modifier = modifier.padding(5.dp)) {
            content()
        }
    }
}