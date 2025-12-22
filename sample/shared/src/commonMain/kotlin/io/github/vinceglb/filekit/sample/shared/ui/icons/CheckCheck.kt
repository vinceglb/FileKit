package io.github.vinceglb.filekit.sample.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
public val LucideIcons.CheckCheck: ImageVector
    get() {
        if (_CheckCheck != null) {
            return _CheckCheck!!
        }
        _CheckCheck = ImageVector
            .Builder(
                name = "CheckCheck",
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
                    lineTo(7f, 17f)
                    lineToRelative(-5f, -5f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveToRelative(22f, 10f)
                    lineToRelative(-7.5f, 7.5f)
                    lineTo(13f, 16f)
                }
            }.build()

        return _CheckCheck!!
    }

@Suppress("ObjectPropertyName")
private var _CheckCheck: ImageVector? = null
