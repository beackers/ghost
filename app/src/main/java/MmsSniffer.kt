package com.beackers.ghostsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MmsSniffer : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val logger = FileLogger(ctx)
        logger.log("EVENT MMS_RECEIVED action=${intent.action}")

        val extras = intent.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                val v = extras.get(key)
                logger.log("MMS EXTRA $key=$v")
            }
        }
    }
}
