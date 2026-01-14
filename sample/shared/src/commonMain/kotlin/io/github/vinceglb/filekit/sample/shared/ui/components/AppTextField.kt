package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
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
    val textStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        fontFamily = fontFamily,
    )
    val mergedTextStyle = textStyle.merge(TextStyle(color = MaterialTheme.colorScheme.onSurface))
    val interactionSource = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = true,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                placeholder = placeholder?.let {
                    {
                        CompositionLocalProvider(LocalTextStyle provides mergedTextStyle) {
                            it()
                        }
                    }
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                colors = colors,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = AppTextFieldDefaults.Shape,
                    )
                },
            )
        },
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
