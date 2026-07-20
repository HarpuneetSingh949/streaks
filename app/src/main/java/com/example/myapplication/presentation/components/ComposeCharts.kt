package com.example.myapplication.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun SimpleLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 6f
) {
    if (data.isEmpty()) return

    val maxData = data.maxOrNull() ?: 1f
    val maxY = if (maxData == 0f) 1f else maxData

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val pointWidth = width / (data.size - 1).coerceAtLeast(1)

        val path = Path()

        data.forEachIndexed { index, value ->
            val x = index * pointWidth
            val y = height - ((value / maxY) * height)
            
            // Animate Y position from bottom up
            val animatedY = height - ((height - y) * animationProgress.value)

            if (index == 0) {
                path.moveTo(x, animatedY)
            } else {
                path.lineTo(x, animatedY)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun SimpleBarChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barSpacing: Float = 16f
) {
    if (data.isEmpty()) return

    val maxData = data.maxOrNull() ?: 1f
    val maxY = if (maxData == 0f) 1f else maxData

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val barCount = data.size
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = (width - totalSpacing) / barCount

        data.forEachIndexed { index, value ->
            val x = index * (barWidth + barSpacing)
            val barHeight = ((value / maxY) * height)
            val animatedHeight = barHeight * animationProgress.value
            val y = height - animatedHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, animatedHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}
