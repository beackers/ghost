package com.beackers.ghostsms

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process

class QuikUsageLogger(private val ctx: Context) {
    private val QUIK_PKG = "dev.octoshrimpy.quik"
    private val prefs = ctx.getSharedPreferences("quik_usage_logger", Context.MODE_PRIVATE)
    private var lastProcessedTimestamp = prefs.getLong(KEY_LAST_PROCESSED, 0L)

    fun hasUsageAccess(): Boolean {
        val appOps = ctx.getSystemService(AppOpsManager::class.java)
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            ctx.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun pollEvents(): List<String> {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val startTime = when {
            lastProcessedTimestamp in 1 until now -> lastProcessedTimestamp
            else -> now - 600_000
        }
        val events = usm.queryEvents(startTime, now)

        val out = mutableListOf<String>()
        val event = UsageEvents.Event()
        var maxTimestamp = lastProcessedTimestamp

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == QUIK_PKG) {
                if (event.timeStamp > lastProcessedTimestamp) {
                    out.add("${event.timeStamp} QUIK event=${event.eventType}")
                }
                if (event.timeStamp > maxTimestamp) {
                    maxTimestamp = event.timeStamp
                }
            }
        }
        if (maxTimestamp > lastProcessedTimestamp) {
            lastProcessedTimestamp = maxTimestamp
            prefs.edit().putLong(KEY_LAST_PROCESSED, lastProcessedTimestamp).apply()
        }
        return out
    }

    companion object {
        private const val KEY_LAST_PROCESSED = "last_processed_timestamp"
    }
}
