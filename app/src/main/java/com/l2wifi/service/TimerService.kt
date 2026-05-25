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

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        remainingSeconds = intent?.getLongExtra("START_TIME", 0L) ?: 0L
        accountName = intent?.getStringExtra("ACCOUNT_NAME") ?: "L2 WiFi"

        if (remainingSeconds <= 0) {
            clearRemainingTime(this)
            stopSelf()
            return START_NOT_STICKY
        }

        saveRemainingTime(this, remainingSeconds)
        startForeground(
            TimerNotification.NOTIFICATION_ID,
            TimerNotification.createNotification(this, accountName, remainingSeconds)
        )

        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                saveRemainingTime(this@TimerService, remainingSeconds)

                val tickIntent = Intent(ACTION_TICK).apply {
                    putExtra(EXTRA_TIME, remainingSeconds)
                }
                sendBroadcast(tickIntent)

                val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(
                    TimerNotification.NOTIFICATION_ID,
                    TimerNotification.createNotification(this@TimerService, accountName, remainingSeconds)
                )
            }
            clearRemainingTime(this@TimerService)
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
        const val ACTION_TICK = "com.l2wifi.ACTION_TICK"
        const val EXTRA_TIME = "EXTRA_TIME"
        private const val PREFS_NAME = "timer_prefs"
        private const val KEY_REMAINING_TIME = "remaining_time"

        fun getLastRemainingTime(context: Context): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getLong(KEY_REMAINING_TIME, 0L)
        }

        fun saveRemainingTime(context: Context, seconds: Long) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_REMAINING_TIME, seconds)
                .apply()
        }

        fun clearRemainingTime(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_REMAINING_TIME)
                .apply()
        }
    }
}
