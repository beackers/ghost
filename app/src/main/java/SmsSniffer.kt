package com.beackers.ghostsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage

class SmsSniffer : BroadcastReceiver() {

    override fun onReceive(ctx: Context, intent: Intent) {
        val logger = FileLogger(ctx)
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

            logger.log("EVENT SMS from:$from body:$body")
        }
    }
}
