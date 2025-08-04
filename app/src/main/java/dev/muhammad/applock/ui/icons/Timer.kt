package dev.muhammad.applock.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Timer = ImageVector.Builder(
    name = "Timer",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f
).apply {
    path(
        fill = SolidColor(Color(0xFF000000))
    ) {
        moveTo(360f, 120f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(240f)
        verticalLineToRelative(80f)
        close()
        moveToRelative(80f, 440f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(-240f)
        horizontalLineToRelative(-80f)
        close()
        moveToRelative(40f, 320f)
        quadToRelative(-74f, 0f, -139.5f, -28.5f)
        reflectiveQuadTo(226f, 774f)
        reflectiveQuadToRelative(-77.5f, -114.5f)
        reflectiveQuadTo(120f, 520f)
        reflectiveQuadToRelative(28.5f, -139.5f)
        reflectiveQuadTo(226f, 266f)
        reflectiveQuadToRelative(114.5f, -77.5f)
        reflectiveQuadTo(480f, 160f)
        quadToRelative(62f, 0f, 119f, 20f)
        reflectiveQuadToRelative(107f, 58f)
        lineToRelative(56f, -56f)
        lineToRelative(56f, 56f)
        lineToRelative(-56f, 56f)
        quadToRelative(38f, 50f, 58f, 107f)
        reflectiveQuadToRelative(20f, 119f)
        quadToRelative(0f, 74f, -28.5f, 139.5f)
        reflectiveQuadTo(734f, 774f)
        reflectiveQuadToRelative(-114.5f, 77.5f)
        reflectiveQuadTo(480f, 880f)
        moveToRelative(0f, -80f)
        quadToRelative(116f, 0f, 198f, -82f)
        reflectiveQuadToRelative(82f, -198f)
        reflectiveQuadToRelative(-82f, -198f)
        reflectiveQuadToRelative(-198f, -82f)
        reflectiveQuadToRelative(-198f, 82f)
        reflectiveQuadToRelative(-82f, 198f)
        reflectiveQuadToRelative(82f, 198f)
        reflectiveQuadToRelative(198f, 82f)
        moveToRelative(0f, -280f)
    }
}.build()


