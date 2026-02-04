package com.beackers.ghostsms

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process

class QuikUsageLogger(private val ctx: Context) {
    private val QUIK_PKG = "dev.octoshrimpy.quik"

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
        val events = usm.queryEvents(now - 600_000, now) // last 10 minutes

        val out = mutableListOf<String>()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == QUIK_PKG) {
                out.add("${event.timeStamp} QUIK event=${event.eventType}")
            }
        }
        return out
    }
}
