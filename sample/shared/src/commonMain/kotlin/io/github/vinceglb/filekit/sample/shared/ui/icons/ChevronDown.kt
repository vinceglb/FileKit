package io.github.vinceglb.filekit.sample.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
public val LucideIcons.ChevronDown: ImageVector
    get() {
        if (_ChevronDown != null) {
            return _ChevronDown!!
        }
        _ChevronDown = ImageVector
            .Builder(
                name = "ChevronDown",
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
                    moveToRelative(6f, 9f)
                    lineToRelative(6f, 6f)
                    lineToRelative(6f, -6f)
                }
            }.build()

        return _ChevronDown!!
    }

@Suppress("ObjectPropertyName")
private var _ChevronDown: ImageVector? = null
