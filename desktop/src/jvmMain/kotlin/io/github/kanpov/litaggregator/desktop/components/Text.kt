package io.github.kanpov.litaggregator.desktop.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun H5Text(text: String, fontWeight: FontWeight = FontWeight.Bold, fontStyle: FontStyle = FontStyle.Normal,
           modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.h5,
        fontWeight = fontWeight,
        modifier = modifier,
        fontStyle = fontStyle
    )
}

@Composable
fun H6Text(text: String, highlight: Boolean = false, italicize: Boolean = false, modifier: Modifier = Modifier) {
    val weight = if (highlight) FontWeight.SemiBold else FontWeight.Normal
    val style = if (italicize) FontStyle.Italic else FontStyle.Normal

    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        fontWeight = weight,
        fontStyle = style,
        modifier = modifier
    )
}
