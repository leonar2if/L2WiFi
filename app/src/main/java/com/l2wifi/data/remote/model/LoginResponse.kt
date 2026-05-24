package com.l2wifi.data.remote.model

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val sessionId: String?,
    val remainingTime: Long?,
    val credit: Double?
)
