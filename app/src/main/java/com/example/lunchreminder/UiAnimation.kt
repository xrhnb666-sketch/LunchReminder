package com.example.lunchreminder

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

fun AnimatedContentTransitionScope<AppTab>.bottomTabTransition(): ContentTransform {
    val direction = if (targetState.ordinal > initialState.ordinal) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }

    return (
        slideIntoContainer(
            towards = direction,
            animationSpec = tween(UiConstants.Animation.PageTransitionMillis),
        ) + fadeIn(animationSpec = tween(UiConstants.Animation.PageTransitionMillis))
        ).togetherWith(
        slideOutOfContainer(
            towards = direction,
            animationSpec = tween(UiConstants.Animation.PageTransitionMillis),
        ) + fadeOut(animationSpec = tween(UiConstants.Animation.PageTransitionMillis)),
    )
}
