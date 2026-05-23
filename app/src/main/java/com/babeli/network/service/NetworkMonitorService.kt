package com.babeli.network.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.babeli.network.MainActivity
import com.babeli.network.R
import com.babeli.network.data.NetworkMonitor
import com.babeli.network.data.toReadableSpeed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

@AndroidEntryPoint
class NetworkMonitorService : Service() {

    @Inject lateinit var networkMonitor: NetworkMonitor

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val channelId = "babeli_network_monitor"
    private val notifId = 1

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(notifId, buildNotification("Monitoring...", "Starting..."))
        observeSpeed()
    }

    private fun observeSpeed() = scope.launch {
        networkMonitor.speedFlow(2000L)
            .catch { }
            .collect { speed ->
                val text = "↓ ${speed.downloadBps.toReadableSpeed()}  ↑ ${speed.uploadBps.toReadableSpeed()}"
                updateNotification("Network Active", text)
            }
    }

    private fun buildNotification(title: String, text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notifId, buildNotification(title, text))
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            channelId,
            "Network Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows real-time network speed"
            setShowBadge(false)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
