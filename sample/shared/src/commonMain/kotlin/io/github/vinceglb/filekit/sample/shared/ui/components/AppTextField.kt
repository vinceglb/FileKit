package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.sample.shared.theme.AppTheme

public object AppTextFieldDefaults {
    public val ContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    public val Shape: Shape
        @Composable
        get() = MaterialTheme.shapes.medium
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AppOutlinedTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    contentPadding: PaddingValues = AppTextFieldDefaults.ContentPadding,
) {
    OutlinedTextField(
        shape = AppTextFieldDefaults.Shape,
        modifier = modifier.heightIn(min = 48.dp),
        contentPadding = contentPadding,
        state = state,
        readOnly = readOnly,
        lineLimits = TextFieldLineLimits.SingleLine,
        trailingIcon = trailingIcon,
        colors = ExposedDropdownMenuDefaults.textFieldColors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
        ),
        textStyle = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
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
                state = rememberTextFieldState(),
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
