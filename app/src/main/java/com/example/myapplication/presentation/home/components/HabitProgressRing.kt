package com.example.myapplication.presentation.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun HabitProgressRing(
    isCompletedToday: Boolean,
    onToggleCompletion: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val progress by animateFloatAsState(
        targetValue = if (isCompletedToday) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "ringAnimation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isCompletedToday) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceAnimation"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(48.dp)) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
            strokeWidth = 3.dp
        )
        Checkbox(
            checked = isCompletedToday,
            onCheckedChange = { 
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleCompletion(it) 
            },
            modifier = Modifier.scale(scale)
        )
    }
}
