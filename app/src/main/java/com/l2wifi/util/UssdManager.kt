package com.l2wifi.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

object UssdManager {
    fun sendUssd(context: Context, ussdCode: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Permiso CALL_PHONE no concedido. La app lo solicitará al inicio.")
        }
        val encodedHash = Uri.encode("#")
        val parsedCode = ussdCode.replace("#", encodedHash)
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$parsedCode")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
