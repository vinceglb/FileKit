package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme

public object AppTextFieldDefaults {
    public val ContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    public val Shape: Shape
        @Composable
        get() = MaterialTheme.shapes.medium
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    fontFamily: FontFamily? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = AppTextFieldDefaults.Shape,
        modifier = modifier.height(48.dp),
        singleLine = true,
        colors = ExposedDropdownMenuDefaults.textFieldColors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
        ),
        textStyle = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            fontFamily = fontFamily,
        ),
    )
}

@Preview(name = "OutlinedTextField Light")
@Preview(name = "OutlinedTextField Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun AppOutlinedTextFieldPreview() {
    AppTheme {
        Surface {
            AppOutlinedTextField(
                value = "Content",
                onValueChange = {},
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
