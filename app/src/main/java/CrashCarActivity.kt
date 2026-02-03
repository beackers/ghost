package com.beackers.ghostsms.crashcar

import android.os.Bundle
import android.app.Activity

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

    findViewById<TextView>(R.id.crashText).text = text
  }
}
