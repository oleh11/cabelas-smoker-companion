package com.cabelas.smokercompanion.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabelas.smokercompanion.data.model.MeatProbe
import com.cabelas.smokercompanion.data.model.TemperatureUnit
import com.cabelas.smokercompanion.ui.theme.AlertRed
import com.cabelas.smokercompanion.ui.theme.Charcoal600
import com.cabelas.smokercompanion.ui.theme.Charcoal700
import com.cabelas.smokercompanion.ui.theme.Charcoal800
import com.cabelas.smokercompanion.ui.theme.FlameAmber
import com.cabelas.smokercompanion.ui.theme.Probe1Color
import com.cabelas.smokercompanion.ui.theme.Probe2Color
import com.cabelas.smokercompanion.ui.theme.Probe3Color
import com.cabelas.smokercompanion.ui.theme.Probe4Color
import com.cabelas.smokercompanion.ui.theme.SuccessGreen
import com.cabelas.smokercompanion.ui.theme.TextDim
import com.cabelas.smokercompanion.ui.theme.TextGray
import com.cabelas.smokercompanion.ui.theme.TextWhite

@Composable
fun ProbeCard(
    probe: MeatProbe,
    unit: TemperatureUnit,
    onTargetChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val probeColor = when (probe.id) {
        1 -> Probe1Color
        2 -> Probe2Color
        3 -> Probe3Color
        4 -> Probe4Color
        else -> FlameAmber
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal800),
        border = BorderStroke(
            1.dp,
            if (probe.isTargetReached) SuccessGreen else if (probe.isConnected) probeColor.copy(alpha = 0.5f) else Charcoal700
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (probe.isConnected) probeColor else Charcoal600,
                        modifier = Modifier.size(28.dp)
                    ) {
                        BoxContent(probe.id.toString())
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = probe.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (probe.isConnected) (probe.meatType ?: "Probe ${probe.id}") else "Not plugged in",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (probe.isConnected) TextGray else TextDim
                        )
                    }
                }

                // Target Reached Badge or Alarm Status
                if (probe.isTargetReached) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreen.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, SuccessGreen)
                    ) {
                        Text(
                            text = "TARGET REACHED!",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (probe.isConnected && probe.isAlarmEnabled) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alarm Active",
                        tint = FlameAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (probe.isConnected) {
                Spacer(modifier = Modifier.height(14.dp))

                // Temperatures
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "CURRENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                        Text(
                            text = unit.format(probe.currentTempF),
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onTargetChange((probe.targetTempF - 5).coerceAtLeast(100)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (probe.targetTempF > 0) unit.format(probe.targetTempF) else "--",
                                style = MaterialTheme.typography.titleLarge,
                                color = probeColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { onTargetChange((probe.targetTempF + 5).coerceAtMost(220)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { probe.progress },
                    color = if (probe.isTargetReached) SuccessGreen else probeColor,
                    trackColor = Charcoal700,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }
    }
}

@Composable
private fun BoxContent(text: String) {
    androidx.compose.foundation.layout.Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = TextWhite,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
