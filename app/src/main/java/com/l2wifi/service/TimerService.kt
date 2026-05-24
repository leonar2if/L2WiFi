package com.l2wifi.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import kotlinx.coroutines.*

class TimerService : Service() {
    private var job: Job? = null
    private var remainingSeconds = 0L
    private var accountName = ""
    private lateinit var prefs: SharedPreferences

    companion object {
        const val ACTION_TICK = "com.l2wifi.ACTION_TICK"
        const val EXTRA_TIME = "EXTRA_TIME"
        private const val PREFS_NAME = "timer_prefs"
        private const val KEY_REMAINING_TIME = "remaining_time"
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        remainingSeconds = intent?.getLongExtra("START_TIME", 0L) ?: 0L
        accountName = intent?.getStringExtra("ACCOUNT_NAME") ?: "L2 WiFi"

        if (remainingSeconds <= 0) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Guardar tiempo inicial
        prefs.edit().putLong(KEY_REMAINING_TIME, remainingSeconds).apply()

        startForeground(TimerNotification.NOTIFICATION_ID, TimerNotification.createNotification(this, accountName, remainingSeconds))

        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                prefs.edit().putLong(KEY_REMAINING_TIME, remainingSeconds).apply()

                val tickIntent = Intent(ACTION_TICK).apply {
                    putExtra(EXTRA_TIME, remainingSeconds)
                }
                sendBroadcast(tickIntent)

                val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(TimerNotification.NOTIFICATION_ID, TimerNotification.createNotification(this@TimerService, accountName, remainingSeconds))
            }
            // Al llegar a cero, limpiar prefs y detener
            prefs.edit().remove(KEY_REMAINING_TIME).apply()
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun getLastRemainingTime(context: Context): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getLong(KEY_REMAINING_TIME, 0L)
        }
    }
}
