package com.beackers.ghostsms

import android.content.Context
import java.io.File

class FileLogger(ctx: Context) {
    private val file = File(ctx.filesDir, "ghostlog.txt")

    fun log(s: String) {
        file.appendText("${System.currentTimeMillis()} $s\n")
    }

    fun read(): String = if (file.exists()) file.readText() else ""
}
