package io.github.vinceglb.sample.explorer.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val ExplorerIcons.SquareArrowOutUpRight: ImageVector
    get() {
        if (_SquareArrowOutUpRight != null) {
            return _SquareArrowOutUpRight!!
        }
        _SquareArrowOutUpRight = ImageVector
            .Builder(
                name = "SquareArrowOutUpRight",
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
                    moveTo(21f, 13f)
                    verticalLineToRelative(6f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 2f)
                    horizontalLineTo(5f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, -2f)
                    verticalLineTo(5f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, -2f)
                    horizontalLineToRelative(6f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveToRelative(21f, 3f)
                    lineToRelative(-9f, 9f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(15f, 3f)
                    horizontalLineToRelative(6f)
                    verticalLineToRelative(6f)
                }
            }.build()

        return _SquareArrowOutUpRight!!
    }

@Suppress("ObjectPropertyName")
private var _SquareArrowOutUpRight: ImageVector? = null
