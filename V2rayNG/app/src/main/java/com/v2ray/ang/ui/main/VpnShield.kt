package com.v2ray.ang.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.v2ray.ang.R

@Composable
internal fun VpnShield(isRunning: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tint = animateFloatAsState(
        targetValue = if (isRunning) 0.23f else 0f,
        animationSpec = tween(380),
        label = "Steel tint"
    )
    val activation = remember { Animatable(0f) }
    LaunchedEffect(isRunning) {
        activation.snapTo(if (isRunning) 1f else 0f)
        if (isRunning) activation.animateTo(0f, tween(720))
    }
    val sweep = if (isRunning) {
        rememberInfiniteTransition(label = "Shield reflection").animateFloat(
            initialValue = -0.6f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(tween(4800, easing = LinearEasing)),
            label = "Reflection position"
        )
    } else null

    Box(modifier.clickable(role = Role.Button, onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val pulse = activation.value
            if (pulse > 0f) {
                val radius = size.minDimension * (0.45f + (1f - pulse) * 0.12f)
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to Color.Transparent,
                        0.55f to Color(0xFF45FF9F).copy(alpha = 0.30f * pulse),
                        1f to Color.Transparent,
                        center = center,
                        radius = radius
                    ),
                    radius = radius
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.zaizy_steel_shield),
            contentDescription = stringResource(if (isRunning) R.string.acc_stop else R.string.acc_start),
            modifier = Modifier.fillMaxSize(0.88f)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val greenAlpha = tint.value + activation.value * 0.15f
                    if (greenAlpha > 0f) {
                        drawRect(Color(0xFF36E785).copy(alpha = greenAlpha), blendMode = BlendMode.SrcAtop)
                    }
                    if (tint.value > 0f) drawPowerLight(tint.value / 0.23f)
                    val phase = sweep?.value
                    if (phase != null && phase < 1.65f) {
                        val x = size.width * phase
                        drawRect(
                            brush = Brush.linearGradient(
                                0f to Color.Transparent,
                                0.48f to Color(0xFFB4FFD8).copy(alpha = 0.36f),
                                0.58f to Color(0xFF50FFC0).copy(alpha = 0.16f),
                                1f to Color.Transparent,
                                start = Offset(x - size.width * 0.22f, size.height * 0.30f),
                                end = Offset(x + size.width * 0.22f, size.height * 0.47f)
                            ),
                            blendMode = BlendMode.SrcAtop
                        )
                    }
                }
        )
    }
}

// Match the Fit-scaled shield bitmap so light stays inside its engraved power mark.
private fun DrawScope.drawPowerLight(intensity: Float) {
    val imageScale = minOf(size.width / 1073f, size.height / 1466f)
    val imageOrigin = center - Offset(1073f, 1466f) * (imageScale / 2f)
    val arcTopLeft = imageOrigin + Offset(473f, 470f) * imageScale
    val arcDiameter = 124f * imageScale
    val stemTop = imageOrigin + Offset(534f, 474f) * imageScale
    val stemBottom = imageOrigin + Offset(534f, 526f) * imageScale

    drawArc(
        color = Color(0xFF42FF8B).copy(alpha = 0.76f * intensity),
        startAngle = -61f,
        sweepAngle = 302f,
        useCenter = false,
        topLeft = arcTopLeft,
        size = Size(arcDiameter, arcDiameter),
        style = Stroke(13f * imageScale, cap = StrokeCap.Round),
        blendMode = BlendMode.SrcAtop
    )
    drawLine(
        color = Color(0xFF79FFAC).copy(alpha = 0.70f * intensity),
        start = stemTop,
        end = stemBottom,
        strokeWidth = 16f * imageScale,
        cap = StrokeCap.Round,
        blendMode = BlendMode.SrcAtop
    )
}