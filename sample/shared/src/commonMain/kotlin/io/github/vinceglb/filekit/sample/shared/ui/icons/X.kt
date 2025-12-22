package io.github.vinceglb.filekit.sample.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
public val LucideIcons.X: ImageVector
    get() {
        if (_X != null) {
            return _X!!
        }
        _X = ImageVector
            .Builder(
                name = "X",
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
                    moveTo(18f, 6f)
                    lineTo(6f, 18f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveToRelative(6f, 6f)
                    lineToRelative(12f, 12f)
                }
            }.build()

        return _X!!
    }

@Suppress("ObjectPropertyName")
private var _X: ImageVector? = null
