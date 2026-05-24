package com.l2wifi.domain.repository
import com.l2wifi.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(): Flow<List<HistoryEntity>>
    suspend fun addRecord(record: HistoryEntity)
}
