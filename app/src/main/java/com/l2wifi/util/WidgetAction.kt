package com.l2wifi.util

import android.content.Intent

sealed class WidgetAction(open val accountId: Long) {
    data class Connect(override val accountId: Long) : WidgetAction(accountId)
    data class Balance(override val accountId: Long) : WidgetAction(accountId)
    data class Logout(override val accountId: Long) : WidgetAction(accountId)
    data class Refresh(override val accountId: Long) : WidgetAction(accountId)
}

object WidgetActionContract {
    const val EXTRA_ACTION = "extra_widget_action"
    const val EXTRA_ACCOUNT_ID = "extra_widget_account_id"

    const val ACTION_CONNECT = "widget_connect"
    const val ACTION_BALANCE = "widget_balance"
    const val ACTION_LOGOUT = "widget_logout"
    const val ACTION_REFRESH = "widget_refresh"

    fun toIntent(intent: Intent, action: WidgetAction): Intent {
        return intent.apply {
            putExtra(EXTRA_ACCOUNT_ID, action.accountId)
            putExtra(EXTRA_ACTION, when (action) {
                is WidgetAction.Connect -> ACTION_CONNECT
                is WidgetAction.Balance -> ACTION_BALANCE
                is WidgetAction.Logout -> ACTION_LOGOUT
                is WidgetAction.Refresh -> ACTION_REFRESH
            })
        }
    }

    fun fromIntent(intent: Intent?): WidgetAction? {
        if (intent == null) return null
        val action = intent.getStringExtra(EXTRA_ACTION) ?: return null
        val accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1L)
        if (accountId < 0L) return null
        return when (action) {
            ACTION_CONNECT -> WidgetAction.Connect(accountId)
            ACTION_BALANCE -> WidgetAction.Balance(accountId)
            ACTION_LOGOUT -> WidgetAction.Logout(accountId)
            ACTION_REFRESH -> WidgetAction.Refresh(accountId)
            else -> null
        }
    }
}
