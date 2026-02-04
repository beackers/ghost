package com.beackers.ghostsms

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

class QuikUsageLogger(private val ctx: Context) {
    private val QUIK_PKG = "dev.octoshrimpy.quik"

    fun pollEvents(): List<String> {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(now - 60_000, now) // last minute

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
