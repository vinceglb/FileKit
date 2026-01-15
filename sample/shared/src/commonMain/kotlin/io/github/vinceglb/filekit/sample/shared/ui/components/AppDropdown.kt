package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.sample.shared.ui.icons.Check
import io.github.vinceglb.filekit.sample.shared.ui.icons.ChevronDown
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun <T> AppDropdown(
    value: T,
    onValueChange: (T) -> Unit,
    options: List<AppDropdownItem<T>>,
    modifier: Modifier = Modifier,
) {
    val selectedOption = options.first { it.value == value }
    // val textFieldState = rememberTextFieldState(selectedOption.label)
    var expanded by remember { mutableStateOf(false) }
    // var checkedIndex: Int by remember { mutableIntStateOf(0) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.width(IntrinsicSize.Min),
    ) {
        AppOutlinedTextField(
            value = selectedOption.label,
            onValueChange = {},
            readOnly = true,
            leadingIcon = if (selectedOption is AppDropdownItem.IconItem) {
                {
                    Icon(
                        imageVector = selectedOption.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                null
            },
            trailingIcon = {
                Icon(
                    imageVector = LucideIcons.ChevronDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .rotate(if (expanded) 180f else 0f)
                        .size(20.dp),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                    },
                    onClick = {
                        expanded = false
                        onValueChange(option.value)
                    },
                    contentPadding = ButtonDefaults.SmallContentPadding,
                    modifier = Modifier.height(40.dp),
                    leadingIcon = if (option is AppDropdownItem.IconItem) {
                        {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        null
                    },
                    trailingIcon = if (selectedOption == option) {
                        {
                            Icon(
                                imageVector = LucideIcons.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

internal sealed class AppDropdownItem<T> {
    abstract val label: String
    abstract val value: T

    data class SimpleItem<T>(
        override val label: String,
        override val value: T,
    ) : AppDropdownItem<T>()

    data class IconItem<T>(
        override val label: String,
        override val value: T,
        val icon: ImageVector,
    ) : AppDropdownItem<T>()
}
