package com.l2wifi.util

fun isValidNautaUsername(username: String): Boolean {
    val trimmed = username.trim()
    return trimmed.endsWith("@nauta.com.cu") || trimmed.endsWith("@nauta.co.cu")
}
