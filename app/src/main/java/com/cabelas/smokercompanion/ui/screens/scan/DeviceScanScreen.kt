package com.cabelas.smokercompanion.ui.screens.scan

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabelas.smokercompanion.data.model.ConnectionState
import com.cabelas.smokercompanion.ui.theme.Charcoal600
import com.cabelas.smokercompanion.ui.theme.Charcoal700
import com.cabelas.smokercompanion.ui.theme.Charcoal800
import com.cabelas.smokercompanion.ui.theme.Charcoal900
import com.cabelas.smokercompanion.ui.theme.FlameAmber
import com.cabelas.smokercompanion.ui.theme.FlameOrange
import com.cabelas.smokercompanion.ui.theme.SuccessGreen
import com.cabelas.smokercompanion.ui.theme.TextDim
import com.cabelas.smokercompanion.ui.theme.TextGray
import com.cabelas.smokercompanion.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScanScreen(
    viewModel: DeviceScanViewModel,
    onConnectedNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val isSimulatorMode by viewModel.isSimulatorMode.collectAsState()

    val isScanning = connectionState is ConnectionState.Scanning

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CONNECT TO SMOKER",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                },
                actions = {
                    if (!isSimulatorMode) {
                        IconButton(
                            onClick = {
                                if (isScanning) viewModel.stopScan() else viewModel.startScan()
                            }
                        ) {
                            Icon(
                                imageVector = if (isScanning) Icons.AutoMirrored.Filled.BluetoothSearching else Icons.Default.Refresh,
                                contentDescription = "Scan",
                                tint = FlameOrange
                            )
                        }
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
            // Simulator Mode Toggle Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal800),
                    border = BorderStroke(1.dp, if (isSimulatorMode) FlameAmber else Charcoal700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSimulatorMode) FlameAmber.copy(alpha = 0.2f) else Charcoal700,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = null,
                                        tint = if (isSimulatorMode) FlameAmber else TextGray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Grill Simulator Engine",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isSimulatorMode) "Simulating live cook off-grill" else "Real Bluetooth / Wi-Fi hardware",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray
                                )
                            }
                        }

                        Switch(
                            checked = isSimulatorMode,
                            onCheckedChange = { enabled ->
                                viewModel.setSimulatorMode(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = FlameOrange,
                                uncheckedTrackColor = Charcoal700
                            )
                        )
                    }
                }
            }

            // Radar Scanning Visual when Scanning
            if (isScanning) {
                item {
                    RadarScanView()
                }
            }

            // Pairing Instructions Guide
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = FlameAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PAIRING YOUR CABELA'S GRILL",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Turn on the PID controller on your Cabela's Pro Series grill.\n2. Hold the dial button for 3 seconds until 'PAIRING' and a 6-digit PIN appears on screen.\n3. Make sure your phone's Bluetooth is turned on within 25 feet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Discovered Devices List
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "NEARBY BLUETOOTH DEVICES",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextGray,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isSimulatorMode && !isScanning) {
                        OutlinedButton(
                            onClick = { viewModel.startScan() },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, FlameOrange),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FlameOrange)
                        ) {
                            Text(text = "Scan Now")
                        }
                    }
                }
            }

            if (discoveredDevices.isEmpty() && !isSimulatorMode) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Text(
                            text = if (isScanning) "Searching for Cabela's / Camp Chef controllers..." else "No devices found yet. Tap 'Scan Now' to search.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDim
                        )
                    }
                }
            } else {
                items(discoveredDevices) { device ->
                    DeviceItemCard(
                        device = device,
                        isConnected = (connectionState as? ConnectionState.Connected)?.address == device.address,
                        onConnectClick = {
                            viewModel.connectDevice(device)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RadarScanView() {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val center = this.center
            val maxRadius = size.minDimension / 2

            // Expanding Pulse Circle
            drawCircle(
                color = FlameOrange.copy(alpha = (1f - pulseFraction).coerceIn(0f, 1f)),
                radius = maxRadius * pulseFraction,
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner Core
            drawCircle(
                color = FlameOrange.copy(alpha = 0.4f),
                radius = 24.dp.toPx()
            )
            drawCircle(
                color = FlameOrange,
                radius = 12.dp.toPx()
            )
        }

        Text(
            text = "SCANNING...",
            style = MaterialTheme.typography.labelSmall,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 80.dp)
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DeviceItemCard(
    device: BluetoothDevice,
    isConnected: Boolean,
    onConnectClick: () -> Unit
) {
    val isCabelasDevice = device.name?.contains("Cabelas", ignoreCase = true) == true || device.name?.contains("Cabela", ignoreCase = true) == true

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCabelasDevice) Charcoal800 else Charcoal900),
        border = BorderStroke(1.dp, if (isConnected) SuccessGreen else if (isCabelasDevice) FlameOrange else Charcoal700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = if (isConnected) SuccessGreen else if (isCabelasDevice) FlameOrange else TextGray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.name ?: "Unknown Bluetooth Device",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        if (isCabelasDevice) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FlameOrange.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, FlameAmber)
                            ) {
                                Text(
                                    text = "CABELA'S",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FlameAmber,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }

            Button(
                onClick = onConnectClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) SuccessGreen else FlameOrange,
                    contentColor = TextWhite
                )
            ) {
                Text(text = if (isConnected) "Connected" else "Connect")
            }
        }
    }
}
