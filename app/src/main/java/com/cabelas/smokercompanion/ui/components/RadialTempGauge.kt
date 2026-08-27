package com.cabelas.smokercompanion.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabelas.smokercompanion.data.model.TemperatureUnit
import com.cabelas.smokercompanion.ui.theme.Charcoal700
import com.cabelas.smokercompanion.ui.theme.Charcoal800
import com.cabelas.smokercompanion.ui.theme.FlameAmber
import com.cabelas.smokercompanion.ui.theme.FlameOrange
import com.cabelas.smokercompanion.ui.theme.SmokerDark
import com.cabelas.smokercompanion.ui.theme.TextGray
import com.cabelas.smokercompanion.ui.theme.TextWhite
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadialTempGauge(
    currentTempF: Int,
    targetTempF: Int,
    unit: TemperatureUnit,
    onTargetChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minTempF: Int = 160,
    maxTempF: Int = 500
) {
    val animatedCurrentTemp by animateFloatAsState(
        targetValue = currentTempF.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "currentTempAnim"
    )

    val currentFraction = ((animatedCurrentTemp - minTempF) / (maxTempF - minTempF)).coerceIn(0f, 1f)
    val targetFraction = ((targetTempF - minTempF).toFloat() / (maxTempF - minTempF)).coerceIn(0f, 1f)

    // Gauge sweeps from 135 deg to 405 deg (270 degree arc)
    val startAngle = 135f
    val sweepTotal = 270f
    val currentSweep = currentFraction * sweepTotal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Circular Gauge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(250.dp)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 22.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset(center.x - radius, center.y - radius)

                // 1. Background Track Arc
                drawArc(
                    color = Charcoal700,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 2. Active Ember Gradient Arc
                if (currentSweep > 0) {
                    val emberBrush = Brush.sweepGradient(
                        colors = listOf(FlameAmber, FlameOrange, SmokerDark, FlameOrange),
                        center = center
                    )
                    drawArc(
                        brush = emberBrush,
                        startAngle = startAngle,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // 3. Target Setpoint Marker
                val targetAngleRad = Math.toRadians((startAngle + (targetFraction * sweepTotal)).toDouble())
                val targetMarkerRadius = radius
                val targetX = (center.x + targetMarkerRadius * cos(targetAngleRad)).toFloat()
                val targetY = (center.y + targetMarkerRadius * sin(targetAngleRad)).toFloat()

                drawCircle(
                    color = TextWhite,
                    radius = 8.dp.toPx(),
                    center = Offset(targetX, targetY)
                )
                drawCircle(
                    color = FlameOrange,
                    radius = 4.dp.toPx(),
                    center = Offset(targetX, targetY)
                )
            }

            // Center Temp Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = FlameOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ACTUAL GRILL TEMP",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = unit.format(currentTempF),
                    style = MaterialTheme.typography.displayLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "TARGET: ${unit.format(targetTempF)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = FlameAmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Target Temp Slider (Smooth 5° precision)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ADJUST SETPOINT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit.format(targetTempF),
                    style = MaterialTheme.typography.labelLarge,
                    color = FlameOrange,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Slider(
                value = targetTempF.toFloat(),
                onValueChange = { newVal ->
                    val stepped = ((newVal / 5f).toInt() * 5).coerceIn(minTempF, maxTempF)
                    onTargetChange(stepped)
                },
                valueRange = minTempF.toFloat()..maxTempF.toFloat(),
                steps = ((maxTempF - minTempF) / 5) - 1,
                colors = SliderDefaults.colors(
                    thumbColor = FlameOrange,
                    activeTrackColor = FlameOrange,
                    inactiveTrackColor = Charcoal700
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Stepper Buttons (-10, -5, +5, +10)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            FilledIconButton(
                onClick = { onTargetChange((targetTempF - 10).coerceAtLeast(minTempF)) },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Charcoal700, contentColor = TextWhite),
                modifier = Modifier.size(42.dp)
            ) {
                Text(text = "-10", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = { onTargetChange((targetTempF - 5).coerceAtLeast(minTempF)) },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Charcoal700, contentColor = TextWhite),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus 5")
            }

            Text(
                text = unit.format(targetTempF),
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            FilledIconButton(
                onClick = { onTargetChange((targetTempF + 5).coerceAtMost(maxTempF)) },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = FlameOrange, contentColor = TextWhite),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Plus 5")
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = { onTargetChange((targetTempF + 10).coerceAtMost(maxTempF)) },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = FlameOrange, contentColor = TextWhite),
                modifier = Modifier.size(42.dp)
            ) {
                Text(text = "+10", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

