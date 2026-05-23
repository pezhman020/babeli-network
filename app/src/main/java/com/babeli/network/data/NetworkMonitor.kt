package com.babeli.network.data

import android.net.TrafficStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

data class SpeedData(
    val downloadBps: Long,
    val uploadBps: Long,
    val totalDownloadBytes: Long,
    val totalUploadBytes: Long,
    val sessionDownloadBytes: Long,
    val sessionUploadBytes: Long,
)

@Singleton
class NetworkMonitor @Inject constructor() {

    private var sessionStartRx = 0L
    private var sessionStartTx = 0L
    private var initialized = false

    fun speedFlow(intervalMs: Long = 1000L): Flow<SpeedData> = flow {
        var prevRx = TrafficStats.getTotalRxBytes()
        var prevTx = TrafficStats.getTotalTxBytes()

        if (!initialized) {
            sessionStartRx = prevRx
            sessionStartTx = prevTx
            initialized = true
        }

        while (true) {
            delay(intervalMs)
            val curRx = TrafficStats.getTotalRxBytes()
            val curTx = TrafficStats.getTotalTxBytes()

            val dlBps = ((curRx - prevRx) * 1000L) / intervalMs
            val ulBps = ((curTx - prevTx) * 1000L) / intervalMs

            emit(
                SpeedData(
                    downloadBps          = dlBps.coerceAtLeast(0L),
                    uploadBps            = ulBps.coerceAtLeast(0L),
                    totalDownloadBytes   = curRx,
                    totalUploadBytes     = curTx,
                    sessionDownloadBytes = (curRx - sessionStartRx).coerceAtLeast(0L),
                    sessionUploadBytes   = (curTx - sessionStartTx).coerceAtLeast(0L),
                )
            )

            prevRx = curRx
            prevTx = curTx
        }
    }

    fun resetSession() {
        sessionStartRx = TrafficStats.getTotalRxBytes()
        sessionStartTx = TrafficStats.getTotalTxBytes()
    }
}

fun Long.toReadableSpeed(): String {
    return when {
        this >= 1_000_000_000L -> "%.1f GB/s".format(this / 1_000_000_000.0)
        this >= 1_000_000L     -> "%.1f MB/s".format(this / 1_000_000.0)
        this >= 1_000L         -> "%.1f KB/s".format(this / 1_000.0)
        else                   -> "$this B/s"
    }
}

fun Long.toReadableBytes(): String {
    return when {
        this >= 1_073_741_824L -> "%.2f GB".format(this / 1_073_741_824.0)
        this >= 1_048_576L     -> "%.1f MB".format(this / 1_048_576.0)
        this >= 1_024L         -> "%.1f KB".format(this / 1_024.0)
        else                   -> "$this B"
    }
}
