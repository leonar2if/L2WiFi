package com.l2wifi.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.l2wifi.MainActivity
import com.l2wifi.R
import com.l2wifi.service.TimerService

class L2WiFiWidget : AppWidgetProvider() {

    companion object {
        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_CURRENT_INDEX = "current_account_index"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            TimerService.ACTION_TICK -> {
                val time = intent.getLongExtra(TimerService.EXTRA_TIME, 0L)
                saveRemainingTime(context, time)
                updateAllWidgets(context, AppWidgetManager.getInstance(context))
            }
            "NEXT_ACCOUNT" -> {
                val current = getCurrentIndex(context)
                val accounts = WidgetDatabaseHelper.getAccounts(context)
                if (accounts.isNotEmpty()) {
                    val newIndex = (current + 1) % accounts.size
                    saveCurrentIndex(context, newIndex)
                    updateAllWidgets(context, AppWidgetManager.getInstance(context))
                }
            }
            "PREV_ACCOUNT" -> {
                val current = getCurrentIndex(context)
                val accounts = WidgetDatabaseHelper.getAccounts(context)
                if (accounts.isNotEmpty()) {
                    val newIndex = if (current - 1 < 0) accounts.size - 1 else current - 1
                    saveCurrentIndex(context, newIndex)
                    updateAllWidgets(context, AppWidgetManager.getInstance(context))
                }
            }
            "ACTION_CONNECT" -> {
                val accountId = intent.getLongExtra("account_id", -1L)
                openAppAndConnect(context, accountId)
            }
            "ACTION_BALANCE" -> {
                val accountId = intent.getLongExtra("account_id", -1L)
                openAppAndShowBalance(context, accountId)
            }
            "ACTION_LOGOUT" -> {
                openAppAndLogout(context)
            }
            "ACTION_REFRESH" -> {
                openAppAndRefresh(context)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAllWidgets(context: Context, manager: AppWidgetManager) {
        val componentName = android.content.ComponentName(context, L2WiFiWidget::class.java)
        val appWidgetIds = manager.getAppWidgetIds(componentName)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, manager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val accounts = WidgetDatabaseHelper.getAccounts(context)
        val currentIndex = getCurrentIndex(context)
        val account = if (accounts.isNotEmpty()) accounts[currentIndex.coerceIn(0 until accounts.size)] else null
        val activeAccountId = WidgetDatabaseHelper.getActiveAccountId(context)
        val isActive = activeAccountId != null && account != null && activeAccountId == account.id
        val remainingTime = if (isActive) getRemainingTime(context) else 0L

        val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
            if (account != null) {
                setTextViewText(R.id.widget_account_name, account.name)
            } else {
                setTextViewText(R.id.widget_account_name, "Sin cuentas")
            }

            if (isActive && remainingTime > 0) {
                setTextViewText(R.id.widget_remaining_time, formatTime(remainingTime))
                setTextViewText(R.id.widget_connect, "Cerrar sesión")
                setTextViewText(R.id.widget_balance, "Actualizar")
                setOnClickPendingIntent(R.id.widget_connect, getLogoutPendingIntent(context))
                setOnClickPendingIntent(R.id.widget_balance, getRefreshPendingIntent(context))
            } else {
                setTextViewText(R.id.widget_remaining_time, if (remainingTime > 0) formatTime(remainingTime) else "--:--:--")
                setTextViewText(R.id.widget_connect, "Conectar")
                setTextViewText(R.id.widget_balance, "Saldo")
                if (account != null) {
                    setOnClickPendingIntent(R.id.widget_connect, getConnectPendingIntent(context, account.id))
                    setOnClickPendingIntent(R.id.widget_balance, getBalancePendingIntent(context, account.id))
                }
            }

            // Botones de navegación
            setOnClickPendingIntent(R.id.widget_prev, getPrevPendingIntent(context))
            setOnClickPendingIntent(R.id.widget_next, getNextPendingIntent(context))
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    // --- Persistencia ---
    private fun getCurrentIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CURRENT_INDEX, 0)
    }

    private fun saveCurrentIndex(context: Context, index: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_CURRENT_INDEX, index).apply()
    }

    private fun saveRemainingTime(context: Context, time: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putLong("remaining_time", time).apply()
    }

    private fun getRemainingTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong("remaining_time", 0L)
    }

    // --- PendingIntents ---
    private fun getPrevPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, L2WiFiWidget::class.java).apply { action = "PREV_ACCOUNT" }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getNextPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, L2WiFiWidget::class.java).apply { action = "NEXT_ACCOUNT" }
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getConnectPendingIntent(context: Context, accountId: Long): PendingIntent {
        val intent = Intent(context, L2WiFiWidget::class.java).apply {
            action = "ACTION_CONNECT"
            putExtra("account_id", accountId)
        }
        return PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getBalancePendingIntent(context: Context, accountId: Long): PendingIntent {
        val intent = Intent(context, L2WiFiWidget::class.java).apply {
            action = "ACTION_BALANCE"
            putExtra("account_id", accountId)
        }
        return PendingIntent.getBroadcast(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getLogoutPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, L2WiFiWidget::class.java).apply { action = "ACTION_LOGOUT" }
        return PendingIntent.getBroadcast(context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getRefreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, L2WiFiWidget::class.java).apply { action = "ACTION_REFRESH" }
        return PendingIntent.getBroadcast(context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    // --- Acciones que abren la app ---
    private fun openAppAndConnect(context: Context, accountId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("connect_account", accountId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun openAppAndShowBalance(context: Context, accountId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("show_balance", accountId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun openAppAndLogout(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("logout", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun openAppAndRefresh(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("refresh_balance", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}
