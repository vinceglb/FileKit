package io.github.vinceglb.filekit.sample.shared.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.sample.shared.theme.AppTheme
import kotlin.math.max

@Composable
public fun AppDottedBorderCard(
    modifier: Modifier = Modifier,
    dotColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    strokeWidth: Dp = 1.dp,
    dotLength: Dp = 3.dp,
    gapLength: Dp = 2.dp,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .dottedBorder(
                color = dotColor,
                strokeWidth = strokeWidth,
                dotLength = dotLength,
                gapLength = gapLength,
                cornerRadius = cornerRadius,
            ).clip(RoundedCornerShape(cornerRadius))
            .padding(contentPadding),
    ) {
        content()
    }
}

private fun Modifier.dottedBorder(
    color: Color,
    strokeWidth: Dp,
    dotLength: Dp,
    gapLength: Dp,
    cornerRadius: Dp,
): Modifier = drawWithCache {
    val strokePx = strokeWidth.toPx()
    val inset = strokePx / 2f
    val cornerPx = cornerRadius.toPx()
    val adjustedCorner = max(0f, cornerPx - inset)

    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(dotLength.toPx(), gapLength.toPx()),
        phase = 0f,
    )

    val stroke = Stroke(
        width = strokePx,
        pathEffect = pathEffect,
        cap = StrokeCap.Round,
    )

    onDrawWithContent {
        drawContent()
        drawRoundRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size(size.width - strokePx, size.height - strokePx),
            cornerRadius = CornerRadius(adjustedCorner, adjustedCorner),
            style = stroke,
        )
    }
}

@Preview
@Composable
private fun AppDottedBorderCardPreview() {
    AppTheme {
        Surface {
            AppDottedBorderCard(modifier = Modifier.padding(16.dp)) {
                Text(text = "Dotted border card content")
            }
        }
    }
}
