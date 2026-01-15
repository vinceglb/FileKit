package io.github.vinceglb.filekit.sample.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
public val LucideIcons.Images: ImageVector
    get() {
        if (_Images != null) {
            return _Images!!
        }
        _Images = ImageVector
            .Builder(
                name = "Images",
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
                    moveToRelative(22f, 11f)
                    lineToRelative(-1.296f, -1.296f)
                    arcToRelative(2.4f, 2.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.408f, 0f)
                    lineTo(11f, 16f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(4f, 8f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, 2f)
                    verticalLineToRelative(10f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, 2f)
                    horizontalLineToRelative(10f)
                    arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, -2f)
                }
                path(
                    fill = SolidColor(Color.Black),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(13f, 7f)
                    moveToRelative(-1f, 0f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, 2f, 0f)
                    arcToRelative(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, -2f, 0f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(10f, 2f)
                    lineTo(20f, 2f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 4f)
                    lineTo(22f, 14f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 16f)
                    lineTo(10f, 16f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 14f)
                    lineTo(8f, 4f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10f, 2f)
                    close()
                }
            }.build()

        return _Images!!
    }

@Suppress("ObjectPropertyName")
private var _Images: ImageVector? = null
