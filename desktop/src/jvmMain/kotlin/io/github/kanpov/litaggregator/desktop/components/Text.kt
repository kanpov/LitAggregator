package io.github.kanpov.litaggregator.desktop.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

@Composable
fun H5Text(text: String, fontWeight: FontWeight = FontWeight.Bold, fontStyle: FontStyle = FontStyle.Normal,
           fontSize: TextUnit = TextUnit.Unspecified, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.h5,
        fontWeight = fontWeight,
        modifier = modifier,
        fontStyle = fontStyle,
        fontSize = fontSize
    )
}

@Composable
fun H6Text(text: String? = null, annotatedString: AnnotatedString? = AnnotatedString(text!!),
           highlight: Boolean = false, italicize: Boolean = false, modifier: Modifier = Modifier) {
    val weight = if (highlight) FontWeight.SemiBold else FontWeight.Normal
    val style = if (italicize) FontStyle.Italic else FontStyle.Normal

    Text(
        text = annotatedString!!,
        style = MaterialTheme.typography.h6,
        fontWeight = weight,
        fontStyle = style,
        modifier = modifier
    )
}
