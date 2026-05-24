package com.l2wifi.data.repository

import com.l2wifi.data.local.dao.HistoryDao
import com.l2wifi.data.local.entity.HistoryEntity
import com.l2wifi.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao
) : HistoryRepository {
    override fun getHistory(): Flow<List<HistoryEntity>> = dao.getAll()
    override suspend fun addRecord(record: HistoryEntity) = dao.insert(record)
}
