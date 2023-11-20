package io.github.kanpov.litaggregator.desktop.components

import androidx.compose.material.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FullDropdown(options: List<String>, defaultOption: String = options.first(), onSelectedOptionChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(defaultOption) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = { },
            label = { },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
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
                    H6Text(option)
                }
            }
        }
    }
}