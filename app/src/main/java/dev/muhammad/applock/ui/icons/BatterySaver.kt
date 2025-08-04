package dev.muhammad.applock.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BatterySaver = ImageVector.Builder(
    name = "Battery_saver",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f
).apply {
    path(
        fill = SolidColor(Color(0xFF000000))
    ) {
        moveTo(640f, 880f)
        verticalLineToRelative(-120f)
        horizontalLineTo(520f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(120f)
        verticalLineToRelative(-120f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(120f)
        horizontalLineToRelative(120f)
        verticalLineToRelative(80f)
        horizontalLineTo(720f)
        verticalLineToRelative(120f)
        close()
        moveToRelative(-320f, 0f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(280f, 840f)
        verticalLineToRelative(-640f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(320f, 160f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(160f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(80f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(680f, 200f)
        verticalLineToRelative(280f)
        quadToRelative(-21f, 0f, -41f, 3.5f)
        reflectiveQuadTo(600f, 494f)
        verticalLineToRelative(-254f)
        horizontalLineTo(360f)
        verticalLineToRelative(560f)
        horizontalLineToRelative(94f)
        quadToRelative(8f, 23f, 19.5f, 43f)
        reflectiveQuadTo(501f, 880f)
        close()
    }
}.build()
