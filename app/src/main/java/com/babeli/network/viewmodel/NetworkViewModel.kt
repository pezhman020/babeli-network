package com.babeli.network.viewmodel

import android.app.AppOpsManager
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babeli.network.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NetworkUiState(
    val speed: SpeedData = SpeedData(0, 0, 0, 0, 0, 0),
    val connection: ConnectionInfo = ConnectionInfo(ConnectionType.NONE),
    val nearbyNetworks: List<WifiNetwork> = emptyList(),
    val topApps: List<AppNetworkUsage> = emptyList(),
    val selectedPeriod: AppUsageManager.Period = AppUsageManager.Period.TODAY,
    val isLoadingApps: Boolean = false,
    val hasUsagePermission: Boolean = false,
    val speedHistory: List<Float> = emptyList(),
)

@HiltViewModel
class NetworkViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val wifiScanner: WifiScanner,
    private val appUsageManager: AppUsageManager,
) : ViewModel() {

    private val _state = MutableStateFlow(NetworkUiState())
    val state: StateFlow<NetworkUiState> = _state.asStateFlow()

    private val speedHistoryMax = 30

    init {
        checkUsagePermission()
        collectSpeed()
        collectConnection()
        collectNearbyNetworks()
    }

    private fun checkUsagePermission() {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        val hasPerm = mode == AppOpsManager.MODE_ALLOWED
        _state.update { it.copy(hasUsagePermission = hasPerm) }
        if (hasPerm) loadTopApps()
    }

    private fun collectSpeed() = viewModelScope.launch {
        networkMonitor.speedFlow(1000L).collect { speed ->
            _state.update { state ->
                val newHistory = (state.speedHistory + speed.downloadBps.toFloat())
                    .takeLast(speedHistoryMax)
                state.copy(speed = speed, speedHistory = newHistory)
            }
        }
    }

    private fun collectConnection() = viewModelScope.launch {
        wifiScanner.connectionInfoFlow(2000L).collect { conn ->
            _state.update { it.copy(connection = conn) }
        }
    }

    private fun collectNearbyNetworks() = viewModelScope.launch {
        wifiScanner.nearbyNetworksFlow().collect { networks ->
            _state.update { it.copy(nearbyNetworks = networks) }
        }
    }

    fun loadTopApps(period: AppUsageManager.Period = _state.value.selectedPeriod) {
        _state.update { it.copy(isLoadingApps = true, selectedPeriod = period) }
        viewModelScope.launch {
            val apps = appUsageManager.getTopApps(period)
            _state.update { it.copy(topApps = apps, isLoadingApps = false) }
        }
    }

    fun refreshNetworks() {
        wifiScanner.startScan()
    }

    fun resetSession() {
        networkMonitor.resetSession()
    }

    fun recheckPermission() {
        checkUsagePermission()
    }
}
