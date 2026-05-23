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
import androidx.compose.ui.unit.dp
import com.babeli.network.data.ConnectionType
import com.babeli.network.data.toFrequencyBand
import com.babeli.network.ui.components.*
import com.babeli.network.ui.theme.*
import com.babeli.network.viewmodel.NetworkUiState

@Composable
fun NetworksScreen(state: NetworkUiState, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = "Networks", style = MaterialTheme.typography.headlineLarge)
                Text(text = "${state.nearbyNetworks.size} networks nearby", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(
                onClick  = onRefresh,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BgCard),
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Scan", tint = Cyan)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Current connection detail
        if (state.connection.type != ConnectionType.NONE) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Cyan.copy(0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = when (state.connection.type) {
                                    ConnectionType.WIFI   -> Icons.Rounded.Wifi
                                    ConnectionType.MOBILE -> Icons.Rounded.CellTower
                                    else -> Icons.Rounded.WifiOff
                                },
                                contentDescription = null,
                                tint = Cyan,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Column {
                            Text(
                                text  = when (state.connection.type) {
                                    ConnectionType.WIFI   -> state.connection.ssid
                                    ConnectionType.MOBILE -> state.connection.operatorName
                                    else -> "No Connection"
                                },
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text  = when (state.connection.type) {
                                    ConnectionType.WIFI   -> "WiFi  •  ${state.connection.frequency.toFrequencyBand()}"
                                    ConnectionType.MOBILE -> "Mobile  •  ${state.connection.networkGeneration}"
                                    else -> "Disconnected"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    if (state.connection.type == ConnectionType.WIFI) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Divider)
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            StatItem(
                                label = "Link Speed",
                                value = "${state.connection.linkSpeed} Mbps",
                                color = Green,
                            )
                            Box(Modifier.width(1.dp).height(40.dp).background(Divider))
                            StatItem(
                                label = "Signal",
                                value = "${state.connection.signalLevel}/5",
                                color = Cyan,
                            )
                            Box(Modifier.width(1.dp).height(40.dp).background(Divider))
                            StatItem(
                                label = "IP",
                                value = state.connection.ipAddress,
                                color = TextPrimary,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Nearby networks
        Text(text = "Nearby Networks", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        if (state.nearbyNetworks.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.WifiFind, null, tint = TextHint, modifier = Modifier.size(48.dp))
                        Text("Scanning for networks...", style = MaterialTheme.typography.bodyMedium)
                        Text("Tap refresh to scan again", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    state.nearbyNetworks.forEachIndexed { index, network ->
                        WifiNetworkRow(network = network)
                        if (index < state.nearbyNetworks.lastIndex) {
                            HorizontalDivider(color = Divider, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
