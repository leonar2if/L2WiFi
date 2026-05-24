package com.l2wifi.data.remote.model

data class SessionInfoResponse(
    val active: Boolean,
    val remainingTime: Long,
    val credit: Double,
    val username: String
)
