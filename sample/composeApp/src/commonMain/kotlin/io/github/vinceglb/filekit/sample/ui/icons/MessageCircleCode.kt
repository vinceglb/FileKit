package io.github.vinceglb.filekit.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val LucideIcons.MessageCircleCode: ImageVector
    get() {
        if (_MessageCircleCode != null) {
            return _MessageCircleCode!!
        }
        _MessageCircleCode = ImageVector
            .Builder(
                name = "MessageCircleCode",
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
                    moveToRelative(10f, 9f)
                    lineToRelative(-3f, 3f)
                    lineToRelative(3f, 3f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveToRelative(14f, 15f)
                    lineToRelative(3f, -3f)
                    lineToRelative(-3f, -3f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(2.992f, 16.342f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.094f, 1.167f)
                    lineToRelative(-1.065f, 3.29f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.236f, 1.168f)
                    lineToRelative(3.413f, -0.998f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.099f, 0.092f)
                    arcToRelative(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = false, -4.777f, -4.719f)
                }
            }.build()

        return _MessageCircleCode!!
    }

@Suppress("ObjectPropertyName")
private var _MessageCircleCode: ImageVector? = null
