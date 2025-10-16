package dev.muhammad.appintro

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class IntroPage(
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val backgroundColor: Color? = null,
    val contentColor: Color? = null,
    val illustration: (@Composable () -> Unit)? = null,
    val customContent: (@Composable () -> Unit)? = null,
    val onNext: (() -> Boolean)? = null
)