package com.babeli.network.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val frequency: Int,
    val capabilities: String,
    val isConnected: Boolean = false,
)

data class ConnectionInfo(
    val type: ConnectionType,
    val ssid: String = "",
    val bssid: String = "",
    val signalLevel: Int = 0,
    val frequency: Int = 0,
    val linkSpeed: Int = 0,
    val ipAddress: String = "",
    val operatorName: String = "",
    val networkGeneration: String = "",
)

enum class ConnectionType { WIFI, MOBILE, NONE }

@Singleton
class WifiScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wifiManager = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun connectionInfoFlow(intervalMs: Long = 2000L): Flow<ConnectionInfo> = flow {
        while (true) {
            emit(getCurrentConnectionInfo())
            delay(intervalMs)
        }
    }

    fun getCurrentConnectionInfo(): ConnectionInfo {
        val network = connectivityManager.activeNetwork
        val caps    = connectivityManager.getNetworkCapabilities(network)

        return when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                val wifiInfo = getWifiInfo()
                ConnectionInfo(
                    type        = ConnectionType.WIFI,
                    ssid        = wifiInfo?.ssid?.removeSurrounding("\"") ?: "Unknown",
                    bssid       = wifiInfo?.bssid ?: "",
                    signalLevel = wifiInfo?.rssi?.let { WifiManager.calculateSignalLevel(it, 5) } ?: 0,
                    frequency   = wifiInfo?.frequency ?: 0,
                    linkSpeed   = wifiInfo?.linkSpeed ?: 0,
                    ipAddress   = intToIp(wifiInfo?.ipAddress ?: 0),
                )
            }
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                ConnectionInfo(
                    type              = ConnectionType.MOBILE,
                    operatorName      = tm.networkOperatorName,
                    networkGeneration = getNetworkGeneration(tm),
                )
            }
            else -> ConnectionInfo(type = ConnectionType.NONE)
        }
    }

    @Suppress("DEPRECATION")
    private fun getWifiInfo(): WifiInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val network = connectivityManager.activeNetwork ?: return null
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return null
            caps.transportInfo as? WifiInfo
        } else {
            wifiManager.connectionInfo
        }
    }

    fun nearbyNetworksFlow(): Flow<List<WifiNetwork>> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                @Suppress("DEPRECATION")
                val results = wifiManager.scanResults
                val connectedBssid = getWifiInfo()?.bssid ?: ""
                val networks = results.map { r ->
                    WifiNetwork(
                        ssid         = r.SSID.ifEmpty { "<Hidden>" },
                        bssid        = r.BSSID,
                        level        = WifiManager.calculateSignalLevel(r.level, 5),
                        frequency    = r.frequency,
                        capabilities = r.capabilities,
                        isConnected  = r.BSSID == connectedBssid,
                    )
                }.sortedByDescending { it.level }
                trySend(networks)
            }
        }

        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(receiver, filter)
        @Suppress("DEPRECATION")
        wifiManager.startScan()

        awaitClose { context.unregisterReceiver(receiver) }
    }

    fun startScan() {
        @Suppress("DEPRECATION")
        wifiManager.startScan()
    }

    private fun intToIp(ip: Int): String {
        return "${ip and 0xff}.${ip shr 8 and 0xff}.${ip shr 16 and 0xff}.${ip shr 24 and 0xff}"
    }

    @Suppress("DEPRECATION")
    private fun getNetworkGeneration(tm: TelephonyManager): String {
        return when (tm.networkType) {
            TelephonyManager.NETWORK_TYPE_LTE        -> "4G"
            TelephonyManager.NETWORK_TYPE_NR         -> "5G"
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSPAP      -> "3G"
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA       -> "2G"
            else                                     -> "Unknown"
        }
    }
}

fun Int.toFrequencyBand(): String = when {
    this in 2400..2500 -> "2.4 GHz"
    this in 5000..5900 -> "5 GHz"
    this in 5925..7125 -> "6 GHz"
    else               -> "$this MHz"
}

fun Int.toSignalIcon(): String = when (this) {
    5    -> "▂▄▆█"
    4    -> "▂▄▆_"
    3    -> "▂▄__"
    2    -> "▂___"
    1    -> "▂___"
    else -> "____"
}
