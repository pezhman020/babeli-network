package com.babeli.network.data

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.TrafficStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class AppNetworkUsage(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val rxBytes: Long,
    val txBytes: Long,
    val totalBytes: Long,
)

@Singleton
class AppUsageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    private val pm = context.packageManager

    suspend fun getTopApps(period: Period = Period.TODAY): List<AppNetworkUsage> = withContext(Dispatchers.IO) {
        val (start, end) = period.range()
        val result = mutableMapOf<Int, Pair<Long, Long>>()

        try {
            queryStats(ConnectivityManager.TYPE_WIFI, start, end, result)
        } catch (_: Exception) {}
        try {
            queryStats(ConnectivityManager.TYPE_MOBILE, start, end, result)
        } catch (_: Exception) {}

        result.mapNotNull { (uid, bytes) ->
            val packages = pm.getPackagesForUid(uid) ?: return@mapNotNull null
            val pkg = packages.firstOrNull() ?: return@mapNotNull null
            val appName = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (_: PackageManager.NameNotFoundException) { pkg }

            val icon = try { pm.getApplicationIcon(pkg) } catch (_: Exception) { null }

            AppNetworkUsage(
                packageName = pkg,
                appName     = appName,
                icon        = icon,
                rxBytes     = bytes.first,
                txBytes     = bytes.second,
                totalBytes  = bytes.first + bytes.second,
            )
        }.filter { it.totalBytes > 0 }
            .sortedByDescending { it.totalBytes }
    }

    private fun queryStats(networkType: Int, start: Long, end: Long, result: MutableMap<Int, Pair<Long, Long>>) {
        val stats = networkStatsManager.querySummary(networkType, null, start, end)
        val bucket = NetworkStats.Bucket()
        while (stats.hasNextBucket()) {
            stats.getNextBucket(bucket)
            val uid = bucket.uid
            if (uid < 1000) continue
            val existing = result[uid] ?: (0L to 0L)
            result[uid] = (existing.first + bucket.rxBytes) to (existing.second + bucket.txBytes)
        }
        stats.close()
    }

    enum class Period(val label: String) {
        TODAY("Today"),
        WEEK("This Week"),
        MONTH("This Month");

        fun range(): Pair<Long, Long> {
            val cal = Calendar.getInstance()
            val end = System.currentTimeMillis()
            return when (this) {
                TODAY -> {
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis to end
                }
                WEEK -> {
                    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.timeInMillis to end
                }
                MONTH -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.timeInMillis to end
                }
            }
        }
    }
}
