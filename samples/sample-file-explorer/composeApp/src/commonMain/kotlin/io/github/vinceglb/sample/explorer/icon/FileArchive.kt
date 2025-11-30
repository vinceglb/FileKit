package io.github.vinceglb.sample.explorer.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ExplorerIcons.FileArchive: ImageVector
    get() {
        if (_FileArchive != null) {
            return _FileArchive!!
        }
        _FileArchive = ImageVector
            .Builder(
                name = "FileArchive",
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
                    moveTo(10f, 12f)
                    verticalLineToRelative(-1f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(10f, 18f)
                    verticalLineToRelative(-2f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(10f, 7f)
                    verticalLineTo(6f)
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
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(15.5f, 22f)
                    horizontalLineTo(18f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, -2f)
                    verticalLineTo(7f)
                    lineToRelative(-5f, -5f)
                    horizontalLineTo(6f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, 2f)
                    verticalLineToRelative(16f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.274f, 1.01f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(10f, 20f)
                    moveToRelative(-2f, 0f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4f, 0f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, -4f, 0f)
                }
            }.build()

        return _FileArchive!!
    }

@Suppress("ObjectPropertyName")
private var _FileArchive: ImageVector? = null
