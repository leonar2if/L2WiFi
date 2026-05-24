package com.l2wifi.domain.model

data class Account(
    val id: Long,
    val name: String,
    val username: String,
    val password: String,
    val state: ConnectionState
)

enum class ConnectionState {
    ACTIVE, INACTIVE, CONNECTING, ERROR, NO_BALANCE
}
