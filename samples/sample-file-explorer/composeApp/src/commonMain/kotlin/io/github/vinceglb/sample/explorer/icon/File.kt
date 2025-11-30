package io.github.vinceglb.sample.explorer.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ExplorerIcons.File: ImageVector
    get() {
        if (_File != null) {
            return _File!!
        }
        _File = ImageVector
            .Builder(
                name = "File",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(15f, 2f)
                    horizontalLineTo(6f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, 2f)
                    verticalLineToRelative(16f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, 2f)
                    horizontalLineToRelative(12f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, -2f)
                    verticalLineTo(7f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(14f, 2f)
                    verticalLineToRelative(4f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, 2f)
                    horizontalLineToRelative(4f)
                }
            }.build()

        return _File!!
    }

@Suppress("ObjectPropertyName")
private var _File: ImageVector? = null
