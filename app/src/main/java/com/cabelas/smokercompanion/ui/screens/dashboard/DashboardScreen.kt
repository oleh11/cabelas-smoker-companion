package com.cabelas.smokercompanion.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabelas.smokercompanion.data.model.SmokerPowerMode
import com.cabelas.smokercompanion.ui.components.ConnectionStatusBadge
import com.cabelas.smokercompanion.ui.components.LiveCookChart
import com.cabelas.smokercompanion.ui.components.ProbeCard
import com.cabelas.smokercompanion.ui.components.RadialTempGauge
import com.cabelas.smokercompanion.ui.components.SmokeDial
import com.cabelas.smokercompanion.ui.theme.AlertRed
import com.cabelas.smokercompanion.ui.theme.Charcoal700
import com.cabelas.smokercompanion.ui.theme.Charcoal800
import com.cabelas.smokercompanion.ui.theme.Charcoal900
import com.cabelas.smokercompanion.ui.theme.FlameAmber
import com.cabelas.smokercompanion.ui.theme.FlameOrange
import com.cabelas.smokercompanion.ui.theme.HickoryBrown
import com.cabelas.smokercompanion.ui.theme.SmokerDark
import com.cabelas.smokercompanion.ui.theme.SuccessGreen
import com.cabelas.smokercompanion.ui.theme.TextGray
import com.cabelas.smokercompanion.ui.theme.TextWhite
import com.cabelas.smokercompanion.ui.theme.WarningYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.smokerState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val cookHistory by viewModel.cookHistory.collectAsState()
    var showExtraProbes by remember { mutableStateOf(false) }

    var showShutdownDialog by remember { mutableStateOf(false) }
    var showWifiDialog by remember { mutableStateOf(false) }
    var wifiSsidInput by remember { mutableStateOf(state.wifiSsid ?: "Home_BBQ_2.4G") }
    var wifiPasswordInput by remember { mutableStateOf("") }
    var wifiSavedNotice by remember { mutableStateOf(false) }

    // Shutdown Safety Confirmation Dialog
    if (showShutdownDialog) {
        AlertDialog(
            onDismissRequest = { showShutdownDialog = false },
            title = {
                Text(
                    text = "Shutdown Cabela's Smoker?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "The controller will initiate the automated fan cooldown cycle to safely burn down remaining wood pellets in the burn pot before fully powering off.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.shutdownGrill()
                        showShutdownDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = TextWhite)
                ) {
                    Text("Confirm Shutdown", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShutdownDialog = false }) {
                    Text("Cancel", color = TextWhite)
                }
            },
            containerColor = Charcoal800
        )
    }

    // Wi-Fi Network Setup Dialog
    if (showWifiDialog) {
        AlertDialog(
            onDismissRequest = { showWifiDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = FlameOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set Grill Wi-Fi Network",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter your 2.4 GHz home Wi-Fi credentials to transmit them to your Cabela's smoker over Bluetooth.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = wifiSsidInput,
                        onValueChange = { wifiSsidInput = it },
                        label = { Text("Network Name (SSID)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlameOrange,
                            unfocusedBorderColor = Charcoal700,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = wifiPasswordInput,
                        onValueChange = { wifiPasswordInput = it },
                        label = { Text("Password (WPA2)") },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FlameOrange)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlameOrange,
                            unfocusedBorderColor = Charcoal700,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (wifiSavedNotice) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Network credentials transmitted to grill!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setWifiNetwork(wifiSsidInput, wifiPasswordInput)
                        wifiSavedNotice = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlameOrange, contentColor = TextWhite)
                ) {
                    Text("Save & Connect Grill", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showWifiDialog = false
                    wifiSavedNotice = false
                }) {
                    Text("Close", color = TextWhite)
                }
            },
            containerColor = Charcoal800
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CABELA'S PRO SERIES 36\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite,
                            letterSpacing = 0.8.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = FlameAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cook Time: ${state.formattedCookDuration}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                    }
                },
                actions = {
                    ConnectionStatusBadge(
                        connectionState = connectionState,
                        onClick = onNavigateToScan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (state.powerMode == SmokerPowerMode.RUNNING) {
                                showShutdownDialog = true
                            } else {
                                viewModel.startupGrill()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = when (state.powerMode) {
                                SmokerPowerMode.RUNNING -> SuccessGreen
                                SmokerPowerMode.SHUTDOWN -> FlameAmber
                                else -> AlertRed
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Charcoal900)
            )
        },
        containerColor = Charcoal900,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Grill Controls Action Bar (Shutdown + Wi-Fi Setup)
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Shutdown / Power Button
                    Button(
                        onClick = {
                            if (state.powerMode == SmokerPowerMode.RUNNING) {
                                showShutdownDialog = true
                            } else {
                                viewModel.startupGrill()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.powerMode == SmokerPowerMode.RUNNING) AlertRed.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f),
                            contentColor = TextWhite
                        ),
                        border = BorderStroke(1.dp, if (state.powerMode == SmokerPowerMode.RUNNING) AlertRed else SuccessGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (state.powerMode == SmokerPowerMode.RUNNING) Icons.Default.PowerOff else Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (state.powerMode == SmokerPowerMode.RUNNING) AlertRed else SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (state.powerMode) {
                                SmokerPowerMode.RUNNING -> "Shutdown Grill"
                                SmokerPowerMode.SHUTDOWN -> "Cooldown Fan Active"
                                else -> "Startup Grill"
                            },
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Set Grill Network Button
                    OutlinedButton(
                        onClick = { showWifiDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (state.wifiConnected) SuccessGreen else FlameOrange),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Charcoal800,
                            contentColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (state.wifiConnected) SuccessGreen else FlameOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.wifiConnected) "Wi-Fi: Connected" else "Set Grill Wi-Fi",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            // Low Pellets Warning Banner
            if (state.hopper.isLowPelletAlert) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, AlertRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LOW PELLET HOPPER ALERT",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Hopper is at ${state.hopper.levelPercentage}%. Refill wood pellets to avoid flameout.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }
            }

            // Main Grill Temperature Control
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal800),
                    border = BorderStroke(1.dp, Charcoal700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        RadialTempGauge(
                            currentTempF = state.currentChamberTempF,
                            targetTempF = state.targetChamberTempF,
                            unit = state.temperatureUnit,
                            onTargetChange = { viewModel.setTargetTemperature(it) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Setpoint Buttons
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val quickPresets = listOf(
                                Pair(180, "Smoke"),
                                Pair(225, "Low/Slow"),
                                Pair(250, "BBQ"),
                                Pair(275, "Poultry"),
                                Pair(350, "Roast"),
                                Pair(450, "Sear")
                            )
                            items(quickPresets) { (temp, label) ->
                                val isSelected = state.targetChamberTempF == temp
                                OutlinedButton(
                                    onClick = { viewModel.setTargetTemperature(temp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) FlameOrange else Charcoal700,
                                        contentColor = TextWhite
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) FlameAmber else Charcoal700),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = state.temperatureUnit.format(temp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) TextWhite else TextGray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Smoke Level Dial
            item {
                SmokeDial(
                    smokeLevel = state.smokeLevel,
                    onLevelChange = { viewModel.setSmokeLevel(it) }
                )
            }

            // Pellet Hopper Status
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "PELLET HOPPER",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.hopper.levelPercentage}% Filled",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (state.hopper.levelPercentage > 20) SuccessGreen else AlertRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Wood: ${state.hopper.woodFlavor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { state.hopper.levelPercentage / 100f },
                            color = if (state.hopper.levelPercentage > 20) FlameAmber else AlertRed,
                            trackColor = Charcoal700,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                    }
                }
            }

            // Dual Meat Probes Section (Probes 1 & 2 Optimized)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MEAT PROBES (DUAL PROBE SETUP)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    OutlinedButton(
                        onClick = { showExtraProbes = !showExtraProbes },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = BorderStroke(1.dp, Charcoal700)
                    ) {
                        Text(
                            text = if (showExtraProbes) "Hide Probes 3 & 4" else "+ Show Probes 3 & 4",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                    }
                }
            }

            item {
                ProbeCard(
                    probe = state.probe1,
                    unit = state.temperatureUnit,
                    onTargetChange = { viewModel.setProbeTarget(1, it) }
                )
            }

            item {
                ProbeCard(
                    probe = state.probe2,
                    unit = state.temperatureUnit,
                    onTargetChange = { viewModel.setProbeTarget(2, it) }
                )
            }

            if (showExtraProbes) {
                item {
                    ProbeCard(
                        probe = state.probe3,
                        unit = state.temperatureUnit,
                        onTargetChange = { viewModel.setProbeTarget(3, it) }
                    )
                }

                item {
                    ProbeCard(
                        probe = state.probe4,
                        unit = state.temperatureUnit,
                        onTargetChange = { viewModel.setProbeTarget(4, it) }
                    )
                }
            }

            // Live Cook Temperature Curve Chart
            item {
                LiveCookChart(
                    history = cookHistory,
                    unit = state.temperatureUnit
                )
            }
        }
    }
}
