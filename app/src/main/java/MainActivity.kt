package com.beackers.ghostsms

import android.os.Bundle
import android.app.Activity
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.io.File
import android.widget.Toast
import android.content.pm.PackageManager

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    ActivityCompat.requestPermissions(
      this,
      arrayOf(android.Manifest.permission.RECEIVE_SMS),
      1
    )

    Toast.makeText(this,
      "Has SMS perm = " + (checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED),
      Toast.LENGTH_LONG
    ).show()

    val f = File(filesDir, "ghostsms.log")
    if (f.exists()) {
      val text = f.readText()
      findViewById<TextView>(R.id.logView).text = text
    }
  }
}
