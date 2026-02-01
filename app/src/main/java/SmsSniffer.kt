package com.beackers.ghostsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.widget.Toast

class SmsSniffer : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val bundle = intent.extras ?: return
    val pdus = bundle.get("pdus") as? Array<*> ?: return
    val format = bundle.getString("format")

    for (pdu in pdus) {
      val msg = if (format != null)
          SmsMessage.createFromPdu(pdu as ByteArray, format)
      else
          SmsMessage.createFromPdu(pdu as ByteArray)

      val from = msg.originatingAddress ?: "unknown"
      val body = msg.messageBody ?: ""

      Toast.makeText(context, "SMS from $from: $body", Toast.LENGTH_LONG).show()
      logToFile(context, "FROM=$from BODY=$body")
    }
  }
  fun logToFile(context: Context, text: String) {
    val f = java.io.File(context.filesDir, "ghostsms.log")
    f.appendText(text + "\n")
  }
}

