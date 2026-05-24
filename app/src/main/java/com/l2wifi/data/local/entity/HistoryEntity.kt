package com.l2wifi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val date: Long,
    val duration: Long,
    val amount: Double,
    val user: String,
    val status: String
)
