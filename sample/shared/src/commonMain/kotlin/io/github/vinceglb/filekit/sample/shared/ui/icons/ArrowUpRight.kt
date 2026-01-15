package io.github.vinceglb.filekit.sample.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
public val LucideIcons.ArrowUpRight: ImageVector
    get() {
        if (_ArrowUpRight != null) {
            return _ArrowUpRight!!
        }
        _ArrowUpRight = ImageVector
            .Builder(
                name = "ArrowUpRight",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(7f, 7f)
                    horizontalLineToRelative(10f)
                    verticalLineToRelative(10f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(7f, 17f)
                    lineTo(17f, 7f)
                }
            }.build()

        return _ArrowUpRight!!
    }

@Suppress("ObjectPropertyName")
private var _ArrowUpRight: ImageVector? = null
