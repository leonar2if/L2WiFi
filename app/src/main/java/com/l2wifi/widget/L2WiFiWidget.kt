package com.l2wifi.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.RemoteViews
import com.l2wifi.MainActivity
import com.l2wifi.R
import com.l2wifi.service.TimerService
import com.l2wifi.util.WidgetAction
import com.l2wifi.util.WidgetActionContract

class L2WiFiWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            TimerService.ACTION_TICK,
            WidgetSync.ACTION_UPDATE_WIDGETS,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                updateAllWidgets(context, AppWidgetManager.getInstance(context))
            }
            "ACTION_CONNECT" -> openApp(context, WidgetAction.Connect(intent.getLongExtra("account_id", -1L)))
            "ACTION_BALANCE" -> openApp(context, WidgetAction.Balance(intent.getLongExtra("account_id", -1L)))
            "ACTION_LOGOUT" -> openApp(context, WidgetAction.Logout(intent.getLongExtra("account_id", -1L)))
            "ACTION_REFRESH" -> openApp(context, WidgetAction.Refresh(intent.getLongExtra("account_id", -1L)))
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAllWidgets(context, appWidgetManager)
    }

    private fun updateAllWidgets(context: Context, manager: AppWidgetManager) {
        val componentName = android.content.ComponentName(context, L2WiFiWidget::class.java)
        val appWidgetIds = manager.getAppWidgetIds(componentName)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, manager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val account = WidgetDatabaseHelper.getDisplayAccount(context)
        val activeAccountId = WidgetDatabaseHelper.getActiveAccountId(context)
        val isConnected = activeAccountId != null
        val remainingTime = if (isConnected) TimerService.getLastRemainingTime(context) else 0L

        val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
            setTextViewText(R.id.widget_account_name, account?.name ?: "Sin cuentas")
            setTextViewText(R.id.widget_remaining_time, formatWidgetTime(remainingTime))
            setViewVisibility(R.id.widget_account_name, if (isConnected) android.view.View.GONE else android.view.View.VISIBLE)
            setViewVisibility(R.id.widget_remaining_time, if (isConnected) android.view.View.VISIBLE else android.view.View.GONE)

            if (isConnected) {
                setImageViewBitmap(R.id.widget_connect, createLogoutBitmap(context))
                setImageViewBitmap(R.id.widget_balance, createRefreshBitmap(context))
                if (activeAccountId != null) {
                    setOnClickPendingIntent(R.id.widget_connect, getLogoutPendingIntent(context, activeAccountId))
                    setOnClickPendingIntent(R.id.widget_balance, getRefreshPendingIntent(context, activeAccountId))
                }
            } else {
                setImageViewBitmap(R.id.widget_connect, createWifiBitmap(context))
                setImageViewBitmap(R.id.widget_balance, createDollarBitmap(context))
                if (account != null) {
                    setOnClickPendingIntent(R.id.widget_connect, getConnectPendingIntent(context, account.id))
                    setOnClickPendingIntent(R.id.widget_balance, getBalancePendingIntent(context, account.id))
                }
            }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun formatWidgetTime(seconds: Long): String {
        val safe = seconds.coerceAtLeast(0L)
        val hours = safe / 3600
        val minutes = (safe % 3600) / 60
        val secs = safe % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%d:%02d", minutes, secs)
        }
    }

    private fun bitmapSize(context: Context): Int = (24 * context.resources.displayMetrics.density).toInt().coerceAtLeast(48)

    private fun createWifiBitmap(context: Context): Bitmap {
        val size = bitmapSize(context)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = size * 0.08f
            strokeCap = Paint.Cap.ROUND
        }
        val center = size / 2f
        val padding = size * 0.16f
        val step = size * 0.15f
        val rect1 = RectF(padding, padding, size - padding, size - padding)
        val rect2 = RectF(padding + step, padding + step, size - padding - step, size - padding - step)
        val rect3 = RectF(padding + step * 2, padding + step * 2, size - padding - step * 2, size - padding - step * 2)
        canvas.drawArc(rect1, 200f, 140f, false, paint)
        canvas.drawArc(rect2, 200f, 140f, false, paint)
        canvas.drawArc(rect3, 200f, 140f, false, paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(center, size * 0.78f, size * 0.07f, paint)
        return bitmap
    }

    private fun createDollarBitmap(context: Context): Bitmap {
        val size = bitmapSize(context)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = size * 0.82f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("$", size / 2f, size * 0.77f, paint)
        return bitmap
    }

    private fun createLogoutBitmap(context: Context): Bitmap {
        val size = bitmapSize(context)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = size * 0.09f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val mid = size * 0.56f
        canvas.drawLine(size * 0.25f, mid, size * 0.72f, mid, paint)
        canvas.drawLine(size * 0.52f, size * 0.33f, size * 0.72f, mid, paint)
        canvas.drawLine(size * 0.52f, size * 0.67f, size * 0.72f, mid, paint)
        return bitmap
    }

    private fun createRefreshBitmap(context: Context): Bitmap {
        val size = bitmapSize(context)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = size * 0.09f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val rect = RectF(size * 0.18f, size * 0.18f, size * 0.82f, size * 0.82f)
        canvas.drawArc(rect, 35f, 300f, false, paint)
        canvas.drawLine(size * 0.78f, size * 0.28f, size * 0.84f, size * 0.42f, paint)
        canvas.drawLine(size * 0.78f, size * 0.28f, size * 0.64f, size * 0.30f, paint)
        return bitmap
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

    private fun getLogoutPendingIntent(context: Context, accountId: Long): PendingIntent {
        val intent = Intent(context, L2WiFiWidget::class.java).apply {
            action = "ACTION_LOGOUT"
            putExtra("account_id", accountId)
        }
        return PendingIntent.getBroadcast(context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getRefreshPendingIntent(context: Context, accountId: Long): PendingIntent {
        val intent = Intent(context, L2WiFiWidget::class.java).apply {
            action = "ACTION_REFRESH"
            putExtra("account_id", accountId)
        }
        return PendingIntent.getBroadcast(context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun openApp(context: Context, action: WidgetAction) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        WidgetActionContract.toIntent(intent, action)
        context.startActivity(intent)
    }
}
