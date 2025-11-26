package com.app.lock.intro

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun AppIntro(
    pages: List<IntroPage>,
    onSkip: () -> Unit = {},
    onFinish: () -> Unit,
    showSkipButton: Boolean = true,
    useAnimatedPager: Boolean = true,
    nextButtonText: String = "Next",
    skipButtonText: String = "Skip",
    finishButtonText: String = "Get Started",
    backButtonText: String = "Back"
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    val currentPageColor by remember(pagerState.currentPage) {
        derivedStateOf {
            pages.getOrNull(pagerState.currentPage)?.backgroundColor
                ?: colorScheme.primary
        }
    }

    val currentPageTextColor by remember(pagerState.currentPage) {
        derivedStateOf {
            pages.getOrNull(pagerState.currentPage)?.contentColor
                ?: colorScheme.onPrimary
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (useAnimatedPager) {
            AnimatedIntroPager(
                pages = pages,
                pagerState = pagerState
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                IntroPageContent(
                    page = pages[page],
                    modifier = Modifier.fillMaxSize(),
                    isVisible = page == pagerState.currentPage
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.1f),
                            Color.Black.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                PageIndicators(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    activeColor = currentPageTextColor,
                    inactiveColor = currentPageTextColor.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = showSkipButton && pagerState.currentPage < pages.size - 1,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        TextButton(
                            onClick = onSkip,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = currentPageTextColor
                            )
                        ) {
                            Text(
                                text = skipButtonText,
                                style = MaterialTheme.typography.bodyLargeEmphasized,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = pagerState.currentPage > 0,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        TextButton(
                            onClick = {
                                navigateToPreviousPage(pagerState, coroutineScope)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = currentPageTextColor
                            )
                        ) {
                            Text(
                                text = backButtonText,
                                style = MaterialTheme.typography.bodyLargeEmphasized,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!showSkipButton || pagerState.currentPage >= pages.size - 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (pagerState.currentPage < pages.size - 1) {
                        FilledTonalButton(
                            onClick = {
                                val currentPage = pages[pagerState.currentPage]
                                val canProceed = currentPage.onNext?.invoke() ?: true

                                if (canProceed) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = currentPageTextColor.copy(alpha = 0.15f),
                                contentColor = currentPageTextColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .height(48.dp)
                        ) {
                            AnimatedContent(
                                targetState = nextButtonText,
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn() + scaleIn(
                                        initialScale = 0.9f
                                    ))
                                        .togetherWith(slideOutVertically { height -> -height } + fadeOut() + scaleOut(
                                            targetScale = 1.1f
                                        ))
                                        .using(SizeTransform(clip = false))
                                },
                                label = "Next Button Text Animation"
                            ) { targetText ->
                                Text(
                                    text = targetText,
                                    style = MaterialTheme.typography.bodyLargeEmphasized,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                val currentPage = pages[pagerState.currentPage]
                                val canFinish = currentPage.onNext?.invoke() ?: true

                                if (canFinish) {
                                    onFinish()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentPageTextColor,
                                contentColor = currentPageColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .height(52.dp)
                        ) {
                            AnimatedContent(
                                targetState = finishButtonText,
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn() + scaleIn(
                                        initialScale = 0.9f
                                    ))
                                        .togetherWith(slideOutVertically { height -> -height } + fadeOut() + scaleOut(
                                            targetScale = 1.1f
                                        ))
                                        .using(SizeTransform(clip = false))
                                },
                                label = "Finish Button Text Animation"
                            ) { targetText ->
                                Text(
                                    text = targetText,
                                    style = MaterialTheme.typography.bodyLargeEmphasized,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun navigateToNextPage(pagerState: PagerState, scope: kotlinx.coroutines.CoroutineScope) {
    scope.launch {
        if (pagerState.currentPage < pagerState.pageCount - 1) {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }
}

fun navigateToPreviousPage(pagerState: PagerState, scope: kotlinx.coroutines.CoroutineScope) {
    scope.launch {
        if (pagerState.currentPage > 0) {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }
}