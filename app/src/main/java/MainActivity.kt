package com.beackers.ghostsms

import android.os.Bundle
import android.app.Activity
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.io.File
import android.widget.Toast
import android.content.Intent
import android.content.pm.PackageManager
import android.content.Context
import android.os.Build
import android.os.FileObserver
import android.provider.Telephony
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper

import com.beackers.ghostsms.crashcar.CrashCarActivity

private lateinit var observer : FileObserver
private lateinit var logView : TextView
private lateinit var logFile : File
private lateinit var telephonyObserver : ContentObserver

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
        arrayOf(
          android.Manifest.permission.RECEIVE_SMS,
          android.Manifest.permission.READ_SMS
        ),
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
      checkDefaultSmsApp()
      startTelephonyObserver()
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
          findViewById<TextView>(R.id.logView).text = File(filesDir, "ghostsms.log").readText()
        }
      }
    }
    observer.startWatching()
  }

  private fun checkDefaultSmsApp() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      return
    }
    val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
    if (defaultSmsPackage != packageName) {
      val message = "App is not the default SMS/MMS handler; MMS broadcasts are delivered only to the default app. Telephony DB polling may still reveal delivered messages.\n"
      logFile.appendText(message)
      findViewById<TextView>(R.id.logView).text = logFile.readText()
      Toast.makeText(this, message.trim(), Toast.LENGTH_LONG).show()
      val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
      intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
      startActivity(intent)
    }
  }

  private fun startTelephonyObserver() {
    if (checkSelfPermission(android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
      val message = "READ_SMS permission not granted; cannot observe SMS/MMS database changes.\n"
      logFile.appendText(message)
      findViewById<TextView>(R.id.logView).text = logFile.readText()
      return
    }
    telephonyObserver = TelephonyChangeObserver(
      Handler(Looper.getMainLooper()),
      this,
      contentResolver
    )
    contentResolver.registerContentObserver(
      Telephony.Sms.CONTENT_URI,
      true,
      telephonyObserver
    )
    contentResolver.registerContentObserver(
      Telephony.Mms.CONTENT_URI,
      true,
      telephonyObserver
    )
  }

  private class TelephonyChangeObserver(
    handler: Handler,
    private val ctx: Context,
    private val resolver: android.content.ContentResolver
  ) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean, uri: Uri?) {
      val logger = FileLogger(ctx)
      if (uri == null) {
        return
      }
      if (uri.toString().startsWith(Telephony.Sms.CONTENT_URI.toString())) {
        logLatestSms(logger)
      } else if (uri.toString().startsWith(Telephony.Mms.CONTENT_URI.toString())) {
        logLatestMms(logger)
      }
    }

    private fun logLatestSms(logger: FileLogger) {
      resolver.query(
        Telephony.Sms.CONTENT_URI,
        arrayOf(Telephony.Sms._ID, Telephony.Sms.DATE, Telephony.Sms.ADDRESS, Telephony.Sms.BODY),
        null,
        null,
        "${Telephony.Sms.DATE} DESC LIMIT 1"
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          val id = cursor.getLong(0)
          val date = cursor.getLong(1)
          val address = cursor.getString(2)
          val body = cursor.getString(3)
          logger.log("SMS DB id=$id date=$date address=$address body=$body")
        }
      }
    }

    private fun logLatestMms(logger: FileLogger) {
      resolver.query(
        Telephony.Mms.CONTENT_URI,
        arrayOf(Telephony.Mms._ID, Telephony.Mms.DATE, Telephony.Mms.SUBJECT),
        null,
        null,
        "${Telephony.Mms.DATE} DESC LIMIT 1"
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          val id = cursor.getLong(0)
          val date = cursor.getLong(1)
          val subject = cursor.getString(2)
          logger.log("MMS DB id=$id date=$date subject=$subject")
        }
      }
    }
  }
}
