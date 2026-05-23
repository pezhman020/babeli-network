package com.babeli.network.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.babeli.network.data.AppUsageManager
import com.babeli.network.ui.components.*
import com.babeli.network.ui.theme.*
import com.babeli.network.viewmodel.NetworkUiState

@Composable
fun AppsScreen(state: NetworkUiState, onPeriodChange: (AppUsageManager.Period) -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Text(text = "App Usage", style = MaterialTheme.typography.headlineLarge)
        Text(text = "Per-app data consumption", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(20.dp))

        if (!state.hasUsagePermission) {
            PermissionCard(
                onGrant = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            )
        } else {
            // Period selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppUsageManager.Period.entries.forEach { period ->
                    val selected = state.selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Cyan else Transparent)
                            .clickable { onPeriodChange(period) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = period.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (selected) BgPrimary else TextSecond,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.isLoadingApps) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Cyan, modifier = Modifier.size(32.dp))
                }
            } else if (state.topApps.isEmpty()) {
                EmptyState(message = "No data for this period")
            } else {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        val maxBytes = state.topApps.firstOrNull()?.totalBytes ?: 1L
                        state.topApps.forEachIndexed { index, app ->
                            AppUsageRow(usage = app, maxBytes = maxBytes)
                            if (index < state.topApps.lastIndex) {
                                HorizontalDivider(color = Divider, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PermissionCard(onGrant: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier              = Modifier.padding(24.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Orange.copy(0.15f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Security, null, tint = Orange, modifier = Modifier.size(32.dp))
            }
            Text(text = "Usage Access Required", style = MaterialTheme.typography.titleMedium)
            Text(
                text  = "To monitor per-app network usage, Babeli needs Usage Access permission. This is a special permission granted in System Settings.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onGrant,
                colors  = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = BgPrimary),
                shape   = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.OpenInNew, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Open Settings")
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.BarChart, null, tint = TextHint, modifier = Modifier.size(48.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private val Transparent = androidx.compose.ui.graphics.Color.Transparent
