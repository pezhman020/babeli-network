package com.babeli.network.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babeli.network.data.toReadableSpeed
import com.babeli.network.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedGauge(
    speedBps: Long,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val maxBps = 100_000_000L  // 100 MB/s
    val fraction = (speedBps.toFloat() / maxBps).coerceIn(0f, 1f)

    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "gauge"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * 0.08f
            val radius = (size.minDimension / 2f) - strokeWidth
            val center = Offset(size.width / 2f, size.height / 2f)
            val startAngle = 150f
            val sweepTotal = 240f

            // Background arc
            drawArc(
                color       = BgCardAlt,
                startAngle  = startAngle,
                sweepAngle  = sweepTotal,
                useCenter   = false,
                topLeft     = Offset(center.x - radius, center.y - radius),
                size        = Size(radius * 2, radius * 2),
                style       = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Active arc gradient
            val gradient = Brush.sweepGradient(
                colorStops = arrayOf(
                    0.0f to color.copy(alpha = 0.3f),
                    1.0f to color,
                ),
                center = center,
            )
            if (animFraction > 0f) {
                drawArc(
                    brush       = gradient,
                    startAngle  = startAngle,
                    sweepAngle  = sweepTotal * animFraction,
                    useCenter   = false,
                    topLeft     = Offset(center.x - radius, center.y - radius),
                    size        = Size(radius * 2, radius * 2),
                    style       = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }

            // Needle dot at tip
            val angleRad = Math.toRadians((startAngle + sweepTotal * animFraction).toDouble())
            val dotX = center.x + radius * cos(angleRad).toFloat()
            val dotY = center.y + radius * sin(angleRad).toFloat()
            drawCircle(color = color, radius = strokeWidth * 0.6f, center = Offset(dotX, dotY))
            drawCircle(color = Color.White, radius = strokeWidth * 0.25f, center = Offset(dotX, dotY))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = speedBps.toReadableSpeed(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color      = color,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                ),
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge.copy(color = TextSecond),
            )
        }
    }
}

@Composable
fun SpeedGraph(
    history: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas
        val max = history.max().coerceAtLeast(1f)
        val step = size.width / (history.size - 1).toFloat()
        val points = history.mapIndexed { i, v ->
            Offset(i * step, size.height - (v / max) * size.height)
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val cp1 = Offset((points[i - 1].x + points[i].x) / 2, points[i - 1].y)
                val cp2 = Offset((points[i - 1].x + points[i].x) / 2, points[i].y)
                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, points[i].x, points[i].y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), Color.Transparent)
            ),
        )

        drawPath(
            path   = path,
            color  = color,
            style  = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
