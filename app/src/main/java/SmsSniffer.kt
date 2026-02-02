package com.beackers.ghostsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.widget.Toast

class SmsSniffer : BroadcastReceiver() {

  override fun onReceive(ctx: Context, intent: Intent) {
    val logger = FileLogger(ctx)
    logger.log("SMS received")
    logger.log("From: $from Body: $body")
  }
}
