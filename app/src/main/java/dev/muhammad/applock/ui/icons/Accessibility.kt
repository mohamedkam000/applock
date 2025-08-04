package dev.muhammad.applock.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Accessibility = ImageVector.Builder(
    name = "Accessibility",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f
).apply {
    path(
        fill = SolidColor(Color(0xFF000000))
    ) {
        moveTo(480f, 240f)
        quadToRelative(-33f, 0f, -56.5f, -23.5f)
        reflectiveQuadTo(400f, 160f)
        reflectiveQuadToRelative(23.5f, -56.5f)
        reflectiveQuadTo(480f, 80f)
        reflectiveQuadToRelative(56.5f, 23.5f)
        reflectiveQuadTo(560f, 160f)
        reflectiveQuadToRelative(-23.5f, 56.5f)
        reflectiveQuadTo(480f, 240f)
        moveTo(360f, 880f)
        verticalLineToRelative(-520f)
        quadToRelative(-60f, -5f, -122f, -15f)
        reflectiveQuadToRelative(-118f, -25f)
        lineToRelative(20f, -80f)
        quadToRelative(78f, 21f, 166f, 30.5f)
        reflectiveQuadToRelative(174f, 9.5f)
        reflectiveQuadToRelative(174f, -9.5f)
        reflectiveQuadTo(820f, 240f)
        lineToRelative(20f, 80f)
        quadToRelative(-56f, 15f, -118f, 25f)
        reflectiveQuadToRelative(-122f, 15f)
        verticalLineToRelative(520f)
        horizontalLineToRelative(-80f)
        verticalLineToRelative(-240f)
        horizontalLineToRelative(-80f)
        verticalLineToRelative(240f)
        close()
    }
}.build()

