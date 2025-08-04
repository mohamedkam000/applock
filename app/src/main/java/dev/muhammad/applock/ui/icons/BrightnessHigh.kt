package dev.muhammad.applock.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BrightnessHigh = ImageVector.Builder(
    name = "Brightness_high",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f
).apply {
    path(
        fill = SolidColor(Color(0xFF000000))
    ) {
        moveTo(480f, 932f)
        lineTo(346f, 800f)
        horizontalLineTo(160f)
        verticalLineToRelative(-186f)
        lineTo(28f, 480f)
        lineToRelative(132f, -134f)
        verticalLineToRelative(-186f)
        horizontalLineToRelative(186f)
        lineToRelative(134f, -132f)
        lineToRelative(134f, 132f)
        horizontalLineToRelative(186f)
        verticalLineToRelative(186f)
        lineToRelative(132f, 134f)
        lineToRelative(-132f, 134f)
        verticalLineToRelative(186f)
        horizontalLineTo(614f)
        close()
        moveToRelative(0f, -252f)
        quadToRelative(83f, 0f, 141.5f, -58.5f)
        reflectiveQuadTo(680f, 480f)
        reflectiveQuadToRelative(-58.5f, -141.5f)
        reflectiveQuadTo(480f, 280f)
        reflectiveQuadToRelative(-141.5f, 58.5f)
        reflectiveQuadTo(280f, 480f)
        reflectiveQuadToRelative(58.5f, 141.5f)
        reflectiveQuadTo(480f, 680f)
        moveToRelative(0f, 140f)
        lineToRelative(100f, -100f)
        horizontalLineToRelative(140f)
        verticalLineToRelative(-140f)
        lineToRelative(100f, -100f)
        lineToRelative(-100f, -100f)
        verticalLineToRelative(-140f)
        horizontalLineTo(580f)
        lineTo(480f, 140f)
        lineTo(380f, 240f)
        horizontalLineTo(240f)
        verticalLineToRelative(140f)
        lineTo(140f, 480f)
        lineToRelative(100f, 100f)
        verticalLineToRelative(140f)
        horizontalLineToRelative(140f)
        close()
        moveToRelative(0f, -340f)
    }
}.build()
