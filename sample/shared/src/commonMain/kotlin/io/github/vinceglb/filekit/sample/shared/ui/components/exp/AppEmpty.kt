package io.github.vinceglb.filekit.sample.shared.ui.components.exp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.sample.shared.ui.components.AppButton
import io.github.vinceglb.filekit.sample.shared.ui.components.AppButtonDefaults
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedButton
import io.github.vinceglb.filekit.sample.shared.ui.components.AppTextButton
import io.github.vinceglb.filekit.sample.shared.ui.icons.ArrowUpRight
import io.github.vinceglb.filekit.sample.shared.ui.icons.File
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme

@Composable
internal fun AppEmpty(
    title: String,
    subtitle: String,
    primaryButtonText: String,
    secondaryButtonText: String,
    tertiaryButtonText: String,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit,
    onTertiaryButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 52.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .size(40.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            imageVector = LucideIcons.File,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp),
                        )
                    }
                }

                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    letterSpacing = (-0.45).sp,
                    lineHeight = 28.sp,
                )

                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.widthIn(max = 280.dp),
                    lineHeight = 22.75.sp,
                    letterSpacing = 0.25.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppButton(
                    onClick = onPrimaryButtonClick,
                    contentPadding = AppButtonDefaults.SmallButtonContentPadding,
                ) {
                    Text(primaryButtonText)
                }

                AppOutlinedButton(
                    onClick = onSecondaryButtonClick,
                    contentPadding = AppButtonDefaults.SmallButtonContentPadding,
                ) {
                    Text(secondaryButtonText)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AppTextButton(
                onClick = onTertiaryButtonClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.outline,
                ),
            ) {
                Text(tertiaryButtonText)
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = LucideIcons.ArrowUpRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun AppEmptyPreview() {
    AppTheme {
        Surface {
            AppEmpty(
                title = "File picker",
                subtitle = "You haven't created any items yet. Get started by creating your first item.",
                primaryButtonText = "Open file picker",
                secondaryButtonText = "Import project",
                tertiaryButtonText = "Learn more",
                onPrimaryButtonClick = {},
                onSecondaryButtonClick = {},
                onTertiaryButtonClick = {},
            )
        }
    }
}
