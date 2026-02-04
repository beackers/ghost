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
                out.add(describeEvent(event.eventType))
            }
        }
        return out
    }

    private fun describeEvent(eventType: Int): String {
        return when (eventType) {
            UsageEvents.Event.MOVE_TO_FOREGROUND -> "foreground"
            UsageEvents.Event.MOVE_TO_BACKGROUND -> "background"
            UsageEvents.Event.ACTIVITY_RESUMED -> "resume"
            UsageEvents.Event.ACTIVITY_PAUSED -> "pause"
            UsageEvents.Event.ACTIVITY_STOPPED -> "stop"
            UsageEvents.Event.USER_INTERACTION -> "tap"
            UsageEvents.Event.CONFIGURATION_CHANGE -> "config"
            UsageEvents.Event.KEYGUARD_SHOWN -> "lock"
            UsageEvents.Event.KEYGUARD_HIDDEN -> "unlock"
            UsageEvents.Event.SCREEN_INTERACTIVE -> "screen on"
            UsageEvents.Event.SCREEN_NON_INTERACTIVE -> "screen off"
            else -> "code $eventType"
        }
    }
}
