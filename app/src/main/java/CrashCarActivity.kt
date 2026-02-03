package com.beackers.ghostsms.crashcar

import android.os.Bundle
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

import android.widget.TextView
import android.widget.Toast

import com.beackers.ghostsms.R

import java.io.File

class CrashCarActivity : Activity() {
  override fun onCreate(savedInstanceState : Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_crashcarmain)

    val f = File(filesDir, "crash.txt")
    val text = if (f.exists()) {
      f.readText()
    } else {
      "Crash log doesn't exist"
    }

    val tv = findViewById<TextView>(R.id.crashText)
    tv.text = text
    tv.setOnLongClickListener {
      val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip = ClipData.newPlainText("crashlog", tv.text)
      clipboard.setPrimaryClip(clip)

      Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_LONG).show()
      true
    }
  }
}
