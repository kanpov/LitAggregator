package io.github.kanpov.litaggregator.desktop.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun H5Text(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.h5,
        fontWeight = FontWeight.Bold,
        modifier = modifier
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