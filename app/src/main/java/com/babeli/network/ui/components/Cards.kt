package com.babeli.network.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.babeli.network.data.*
import com.babeli.network.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(cornerRadius),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content  = content,
    )
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color = TextPrimary,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = value,
            style = MaterialTheme.typography.titleLarge.copy(color = color, fontSize = 18.sp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun AppUsageRow(usage: AppNetworkUsage, maxBytes: Long, modifier: Modifier = Modifier) {
    Row(
        modifier         = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // App icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BgCardAlt),
            contentAlignment = Alignment.Center,
        ) {
            usage.icon?.let { drawable ->
                val bitmap = drawable.toBitmap(40, 40)
                Image(
                    bitmap             = bitmap.asImageBitmap(),
                    contentDescription = usage.appName,
                    modifier           = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
                )
            } ?: Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = null,
                tint = TextSecond,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier       = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = usage.appName,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    maxLines = 1,
                )
                Text(
                    text  = usage.totalBytes.toReadableBytes(),
                    style = MaterialTheme.typography.labelLarge.copy(color = Cyan),
                )
            }

            Spacer(Modifier.height(6.dp))

            val frac = if (maxBytes > 0) usage.totalBytes.toFloat() / maxBytes else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BgCardAlt),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(frac.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(listOf(Purple, Cyan))
                        ),
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text  = "↓ ${usage.rxBytes.toReadableBytes()}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Green),
                )
                Text(
                    text  = "↑ ${usage.txBytes.toReadableBytes()}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Orange),
                )
            }
        }
    }
}

@Composable
fun WifiNetworkRow(network: WifiNetwork, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (network.isConnected) Cyan.copy(0.15f) else BgCardAlt),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = if (network.isConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiFind,
                contentDescription = null,
                tint               = if (network.isConnected) Cyan else TextSecond,
                modifier           = Modifier.size(22.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text  = network.ssid,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    maxLines = 1,
                )
                if (network.isConnected) {
                    Text(
                        text  = "Connected",
                        style = MaterialTheme.typography.labelSmall.copy(color = Cyan),
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "${network.frequency.toFrequencyBand()}  •  Signal ${network.level}/5",
                style = MaterialTheme.typography.labelSmall,
            )
        }

        SignalBars(level = network.level, color = if (network.isConnected) Cyan else TextSecond)
    }
}

@Composable
fun SignalBars(level: Int, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier.padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        listOf(6.dp, 10.dp, 14.dp, 18.dp, 22.dp).forEachIndexed { i, height ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i < level) color else color.copy(alpha = 0.2f)),
            )
        }
    }
}

@Composable
fun ConnectionTypeChip(type: com.babeli.network.data.ConnectionType, modifier: Modifier = Modifier) {
    val (icon, label, color) = when (type) {
        com.babeli.network.data.ConnectionType.WIFI   -> Triple(Icons.Rounded.Wifi, "WiFi", Cyan)
        com.babeli.network.data.ConnectionType.MOBILE -> Triple(Icons.Rounded.CellTower, "Mobile", Purple)
        com.babeli.network.data.ConnectionType.NONE   -> Triple(Icons.Rounded.WifiOff, "Offline", TextSecond)
    }

    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = color.copy(alpha = 0.15f),
        modifier = modifier,
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge.copy(color = color))
        }
    }
}
