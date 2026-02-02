package com.beackers.ghostsms

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat

class WatchdogService : Service() {

    private lateinit var logger: QuikUsageLogger
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fileLogger: FileLogger

    override fun onCreate() {
        super.onCreate()
        logger = QuikUsageLogger(this)
        fileLogger = FileLogger(this)
        startForeground()
        loop()
    }

    private fun startForeground() {
        val channelId = "ghost_watchdog"
        val nm = getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(
            NotificationChannel(channelId, "Ghost Watchdog", NotificationManager.IMPORTANCE_LOW)
        )

        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("GhostSMS Watchdog Running")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1, notif)
    }

    private fun loop() {
        handler.postDelayed({
            val events = logger.pollEvents()
            for (e in events) fileLogger.log("QUIK $e")
            loop()
        }, 20_000)
    }

    override fun onBind(intent: Intent?) = null
    override fun onDestroy() {
      fileLogger.log("Watchdog killed")
    }
}
