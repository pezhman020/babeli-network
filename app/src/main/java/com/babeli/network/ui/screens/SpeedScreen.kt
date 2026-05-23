package com.babeli.network.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babeli.network.data.toReadableBytes
import com.babeli.network.data.toReadableSpeed
import com.babeli.network.ui.components.*
import com.babeli.network.ui.theme.*
import com.babeli.network.viewmodel.NetworkUiState

@Composable
fun SpeedScreen(state: NetworkUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Text(text = "Speed Monitor", style = MaterialTheme.typography.headlineLarge)
        Text(text = "Real-time network performance", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(24.dp))

        // Big speed display
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    SpeedGauge(
                        speedBps = state.speed.downloadBps,
                        label    = "Download",
                        color    = Cyan,
                        modifier = Modifier.size(160.dp),
                    )
                    SpeedGauge(
                        speedBps = state.speed.uploadBps,
                        label    = "Upload",
                        color    = Purple,
                        modifier = Modifier.size(160.dp),
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Live graph
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgCardAlt)
                        .padding(12.dp),
                ) {
                    SpeedGraph(
                        history  = state.speedHistory,
                        color    = Cyan,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text  = "↓ ${state.speed.downloadBps.toReadableSpeed()}  /  ↑ ${state.speed.uploadBps.toReadableSpeed()}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecond),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Session totals
        Text(text = "Session Statistics", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SessionCard(
                icon  = Icons.Rounded.Download,
                label = "Session Download",
                value = state.speed.sessionDownloadBytes.toReadableBytes(),
                color = Cyan,
                modifier = Modifier.weight(1f),
            )
            SessionCard(
                icon  = Icons.Rounded.Upload,
                label = "Session Upload",
                value = state.speed.sessionUploadBytes.toReadableBytes(),
                color = Purple,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SessionCard(
                icon  = Icons.Rounded.CloudDownload,
                label = "All Time Download",
                value = state.speed.totalDownloadBytes.toReadableBytes(),
                color = Green,
                modifier = Modifier.weight(1f),
            )
            SessionCard(
                icon  = Icons.Rounded.CloudUpload,
                label = "All Time Upload",
                value = state.speed.totalUploadBytes.toReadableBytes(),
                color = Orange,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Speed legend
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Speed Reference", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                SpeedLegendRow("Slow", "< 1 MB/s", 0.05f)
                SpeedLegendRow("Average", "1–10 MB/s", 0.2f)
                SpeedLegendRow("Fast", "10–50 MB/s", 0.5f)
                SpeedLegendRow("Ultra Fast", "> 50 MB/s", 0.9f)
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SessionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = color, fontSize = 16.sp))
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SpeedLegendRow(label: String, range: String, fraction: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary), modifier = Modifier.width(80.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BgCardAlt),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(listOf(Cyan.copy(0.4f), Cyan))),
            )
        }

        Text(range, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(80.dp))
    }
}
