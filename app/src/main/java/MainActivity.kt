package com.beackers.ghostsms

import android.os.Bundle
import android.app.Activity
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.io.File
import android.widget.Toast
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.FileObserver

import com.beackers.ghostsms.crashcar.CrashCarActivity

private lateinit var observer : FileObserver
private lateinit var logView : TextView
private lateinit var logFile : File

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    Thread.setDefaultUncaughtExceptionHandler { thread, e ->
      val f = File(filesDir, "crash.txt")
      f.appendText("CRASH @ ${System.currentTimeMillis()}\n")
      f.appendText(e.toString() + "\n")
      for (el in e.stackTrace) {
        f.appendText(el.toString() + "\n")
      }
      val i = Intent(this, CrashCarActivity::class.java)
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(i)
    }
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    try {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(android.Manifest.permission.RECEIVE_SMS),
        1
      )

      Toast.makeText(this,
        "Has SMS perm = " + (checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED),
        Toast.LENGTH_LONG
      ).show()

      logFile = File(filesDir, "ghostsms.log")
      if (logFile.exists()) {
        val text = logFile.readText()
        findViewById<TextView>(R.id.logView).text = text
      }
      if (Build.VERSION.SDK_INT >= 26) {
        startForegroundService(Intent(this, WatchdogService::class.java))
      } else {
        startService(Intent(this, WatchdogService::class.java))
      }
    } catch (e: Exception) {
      File(filesDir, "crash.txt").appendText("EXCEPTION @ ${System.currentTimeMillis()}:\n$e\n")
      for (el in e.stackTrace) {
        File(filesDir, "crash.txt").appendText(el.toString())
      }
      Toast.makeText(this, "EXC: $e", Toast.LENGTH_LONG).show()
    }
    observer = object : FileObserver(logFile.absolutePath, MODIFY) {
      override fun onEvent(event: Int, path: String?) {
        runOnUiThread {
          findViewbyId<TextView>(R.id.logView).text = File(filesDir, "ghostsms.log").readText()
        }
      }
    }
    observer.startWatching()
  }
}
