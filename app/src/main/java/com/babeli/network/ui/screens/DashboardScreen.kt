package com.babeli.network.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babeli.network.data.*
import com.babeli.network.ui.components.*
import com.babeli.network.ui.theme.*
import com.babeli.network.viewmodel.NetworkUiState

@Composable
fun DashboardScreen(state: NetworkUiState, onResetSession: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        // Header
        Row(
            modifier       = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text  = "babeli",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color      = Cyan,
                        fontWeight = FontWeight.Black,
                        fontSize   = 32.sp,
                    ),
                )
                Text(
                    text  = "network monitor",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            ConnectionTypeChip(type = state.connection.type)
        }

        Spacer(Modifier.height(24.dp))

        // Speed Gauges
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "Live Speed",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(
                        onClick = onResetSession,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reset session",
                            tint = TextSecond,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    SpeedGauge(
                        speedBps = state.speed.downloadBps,
                        label    = "Download",
                        color    = Cyan,
                        modifier = Modifier.size(140.dp),
                    )
                    SpeedGauge(
                        speedBps = state.speed.uploadBps,
                        label    = "Upload",
                        color    = Purple,
                        modifier = Modifier.size(140.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Speed graph
                SpeedGraph(
                    history  = state.speedHistory,
                    color    = Cyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                )

                Spacer(Modifier.height(12.dp))

                // Session stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    StatItem(
                        label = "Session ↓",
                        value = state.speed.sessionDownloadBytes.toReadableBytes(),
                        color = Cyan,
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Divider),
                    )
                    StatItem(
                        label = "Session ↑",
                        value = state.speed.sessionUploadBytes.toReadableBytes(),
                        color = Purple,
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Divider),
                    )
                    StatItem(
                        label = "Total ↓",
                        value = state.speed.totalDownloadBytes.toReadableBytes(),
                        color = Green,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Network Info
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Current Network", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))

                when (state.connection.type) {
                    ConnectionType.WIFI -> WifiInfoContent(state.connection)
                    ConnectionType.MOBILE -> MobileInfoContent(state.connection)
                    ConnectionType.NONE -> NoConnectionContent()
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickStatCard(
                icon   = Icons.Rounded.Download,
                label  = "Total RX",
                value  = state.speed.totalDownloadBytes.toReadableBytes(),
                color  = Green,
                modifier = Modifier.weight(1f),
            )
            QuickStatCard(
                icon   = Icons.Rounded.Upload,
                label  = "Total TX",
                value  = state.speed.totalUploadBytes.toReadableBytes(),
                color  = Orange,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun WifiInfoContent(conn: ConnectionInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NetworkInfoRow(Icons.Rounded.Wifi, "SSID", conn.ssid, Cyan)
        NetworkInfoRow(Icons.Rounded.Router, "BSSID", conn.bssid.ifEmpty { "—" }, TextSecond)
        NetworkInfoRow(Icons.Rounded.Lan, "IP Address", conn.ipAddress.ifEmpty { "—" }, TextPrimary)
        NetworkInfoRow(Icons.Rounded.Speed, "Link Speed", "${conn.linkSpeed} Mbps", Green)
        NetworkInfoRow(Icons.Rounded.SettingsEthernet, "Frequency", conn.frequency.toFrequencyBand(), Purple)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Rounded.SignalWifi4Bar, null, tint = Cyan, modifier = Modifier.size(18.dp))
            Text("Signal", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            SignalBars(level = conn.signalLevel, color = Cyan)
        }
    }
}

@Composable
private fun MobileInfoContent(conn: ConnectionInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NetworkInfoRow(Icons.Rounded.CellTower, "Operator", conn.operatorName.ifEmpty { "Unknown" }, Purple)
        NetworkInfoRow(Icons.Rounded.NetworkCell, "Generation", conn.networkGeneration.ifEmpty { "—" }, Cyan)
    }
}

@Composable
private fun NoConnectionContent() {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.WifiOff, null, tint = TextSecond, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text("No network connection", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NetworkInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: Color) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(90.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary), maxLines = 1)
    }
}

@Composable
private fun QuickStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
