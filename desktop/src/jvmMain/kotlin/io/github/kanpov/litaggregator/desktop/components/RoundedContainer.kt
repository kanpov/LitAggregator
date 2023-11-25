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
fun RoundedContainer(roundStart: Boolean = false, roundEnd: Boolean = false,
                     modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(bottomStart = if (roundStart) 10.dp else 0.dp, bottomEnd = if (roundEnd) 10.dp else 0.dp),
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Row(modifier = modifier.padding(5.dp).height(32.dp)) {
            content()
        }
    }
}
