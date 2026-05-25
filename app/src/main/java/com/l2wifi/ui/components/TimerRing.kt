package com.l2wifi.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TimerRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Int = 200,
    strokeWidth: Float = 8f,
    color: Color = Color.Cyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timerRing")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Canvas(modifier = modifier.size(size.dp)) {
        val center = Offset(x = size / 2f, y = size / 2f)
        val radius = size / 2f - strokeWidth / 2
        val sweepAngle = progress * 360f

        drawCircle(
            color = color.copy(alpha = glowAlpha),
            radius = radius + 6f,
            center = center,
            style = Stroke(width = strokeWidth + 2f, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
