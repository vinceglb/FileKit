package io.github.vinceglb.sample.explorer.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ExplorerIcons.FolderUp: ImageVector
    get() {
        if (_FolderUp != null) {
            return _FolderUp!!
        }
        _FolderUp = ImageVector
            .Builder(
                name = "FolderUp",
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
                    moveTo(20f, 20f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, -2f)
                    verticalLineTo(8f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, -2f)
                    horizontalLineToRelative(-7.9f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.69f, -0.9f)
                    lineTo(9.6f, 3.9f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.93f, 3f)
                    horizontalLineTo(4f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, 2f)
                    verticalLineToRelative(13f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, 2f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(12f, 10f)
                    verticalLineToRelative(6f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveToRelative(9f, 13f)
                    lineToRelative(3f, -3f)
                    lineToRelative(3f, 3f)
                }
            }.build()

        return _FolderUp!!
    }

@Suppress("ObjectPropertyName")
private var _FolderUp: ImageVector? = null
