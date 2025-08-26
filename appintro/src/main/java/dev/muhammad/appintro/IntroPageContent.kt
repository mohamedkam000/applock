package dev.muhammad.appintro

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.typography.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IntroPageContent(
    page: IntroPage,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true
) {
    val backgroundColor = page.backgroundColor ?: MaterialTheme.colorScheme.primaryContainer
    val contentColor = page.contentColor ?: MaterialTheme.colorScheme.onPrimaryContainer

    val contentVisible = remember { MutableTransitionState(false) }

    LaunchedEffect(isVisible) {
        contentVisible.targetState = isVisible
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (page.customContent != null) {
            AnimatedVisibility(
                visibleState = contentVisible,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    page.customContent.invoke()
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                AnimatedVisibility(
                    visibleState = contentVisible,
                    enter = fadeIn(animationSpec = tween(300)) +
                            scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialScale = 0.8f
                            ),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    if (page.illustration != null) {
                        Box(
                            modifier = Modifier.padding(bottom = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            page.illustration.invoke()
                        }
                    } else if (page.icon != null) {
                        Surface(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .clickable(true) {},
                            shape = CircleShape,
                            color = contentColor.copy(alpha = 0.15f),

                            ) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier
                                    .padding(32.dp)
                                    .size(76.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                AnimatedVisibility(
                    visibleState = contentVisible,
                    enter = fadeIn(animationSpec = tween(300, delayMillis = 100)) +
                            slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialOffsetY = { it / 3 }
                            ),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visibleState = contentVisible,
                    enter = fadeIn(animationSpec = tween(300, delayMillis = 200)) +
                            slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialOffsetY = { it / 4 }
                            ),
                    exit = fadeOut(animationSpec = tween(150))
                ) {
                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
        }
    }
}