package com.beackers.ghostsms

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogger(ctx: Context) {
    private val file = File(ctx.filesDir, "ghostsms.log")

    fun log(s: String) {
        val timestamp = formatTime(System.currentTimeMillis())
        val existing = if (file.exists()) file.readText() else ""
        file.writeText("$timestamp $s\n$existing")
    }

    fun read(): String = if (file.exists()) file.readText() else ""

    companion object {
        fun formatTime(timestamp: Long): String {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.US)
            return formatter.format(Date(timestamp))
        }
    }
}
