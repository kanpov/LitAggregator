package io.github.kanpov.litaggregator.desktop.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FullDropdown(options: List<String>, defaultOption: String = options.first(),
                 onSelectedOptionChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(defaultOption) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        BasicTextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = { },
            textStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Normal)
//            label = { },
//            trailingIcon = {
//                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//            },
//            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (option in options) {
                DropdownMenuItem(
                    onClick = {
                        selectedOption = option
                        onSelectedOptionChange(option)
                        expanded = false
                    }
                ) {
                    Text(option, style = MaterialTheme.typography.body1)
                }
            }
        }
    }
}