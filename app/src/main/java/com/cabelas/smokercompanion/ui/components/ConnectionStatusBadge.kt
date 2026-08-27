package com.cabelas.smokercompanion.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cabelas.smokercompanion.data.model.ConnectionState
import com.cabelas.smokercompanion.ui.theme.AlertRed
import com.cabelas.smokercompanion.ui.theme.Charcoal700
import com.cabelas.smokercompanion.ui.theme.FlameAmber
import com.cabelas.smokercompanion.ui.theme.SuccessGreen
import com.cabelas.smokercompanion.ui.theme.TextGray
import com.cabelas.smokercompanion.ui.theme.TextWhite

@Composable
fun ConnectionStatusBadge(
    connectionState: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, icon, text) = when (connectionState) {
        is ConnectionState.Connected -> Triple(
            SuccessGreen.copy(alpha = 0.2f),
            Icons.Default.Bluetooth,
            connectionState.deviceName
        )
        is ConnectionState.SimulatorActive -> Triple(
            FlameAmber.copy(alpha = 0.2f),
            Icons.Default.Science,
            "Simulator Mode"
        )
        is ConnectionState.Connecting, is ConnectionState.Authenticating -> Triple(
            FlameAmber.copy(alpha = 0.2f),
            Icons.AutoMirrored.Filled.BluetoothSearching,
            "Connecting..."
        )
        is ConnectionState.Scanning -> Triple(
            FlameAmber.copy(alpha = 0.2f),
            Icons.AutoMirrored.Filled.BluetoothSearching,
            "Scanning..."
        )
        is ConnectionState.Disconnected -> Triple(
            Charcoal700,
            Icons.Default.WifiOff,
            "Offline / Disconnected"
        )
        is ConnectionState.Error -> Triple(
            AlertRed.copy(alpha = 0.2f),
            Icons.Default.WifiOff,
            "Connection Error"
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, if (connectionState is ConnectionState.Disconnected) Charcoal700 else FlameAmber),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (connectionState is ConnectionState.Connected) SuccessGreen else FlameAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = TextWhite,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
