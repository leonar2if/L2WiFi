package com.l2wifi.widget

import android.content.Context
import android.content.Intent

object WidgetSync {
    const val ACTION_UPDATE_WIDGETS = "com.l2wifi.widget.ACTION_UPDATE_WIDGETS"

    fun requestUpdate(context: Context) {
        val intent = Intent(context, L2WiFiWidget::class.java).apply {
            action = ACTION_UPDATE_WIDGETS
        }
        context.sendBroadcast(intent)
    }
}
