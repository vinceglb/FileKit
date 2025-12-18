package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.sample.shared.theme.AppTheme

public object AppButtonDefaults {
    public val ContentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    public val SmallButtonContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    public val Shape: Shape
        @Composable
        get() = MaterialTheme.shapes.medium
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = AppButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        shape = AppButtonDefaults.Shape,
        contentPadding = contentPadding,
        modifier = modifier,
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = AppButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = AppButtonDefaults.Shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
        ),
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
public fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = AppButtonDefaults.Shape,
        contentPadding = AppButtonDefaults.SmallButtonContentPadding,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.outline,
        ),
        modifier = modifier,
    ) {
        content()
    }
}

@Preview(name = "Button Light")
@Preview(name = "Button Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun AppButtonPreview() {
    AppTheme {
        Surface {
            AppButton(
                onClick = {},
                modifier = Modifier.padding(8.dp),
            ) {
                Text("Button")
            }
        }
    }
}

@Preview(name = "OutlinedButton Light")
@Preview(name = "OutlinedButton Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun AppOutlinedButtonPreview() {
    AppTheme {
        Surface {
            AppOutlinedButton(
                onClick = {},
                modifier = Modifier.padding(8.dp),
            ) {
                Text("Button")
            }
        }
    }
}

@Preview(name = "TextButton Light")
@Preview(name = "TextButton Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun AppTextButtonPreview() {
    AppTheme {
        Surface {
            AppTextButton(
                onClick = {},
                modifier = Modifier.padding(8.dp),
            ) {
                Text("Button")
            }
        }
    }
}
