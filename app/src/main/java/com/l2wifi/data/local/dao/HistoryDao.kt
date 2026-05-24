package com.l2wifi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.l2wifi.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY date DESC")
    fun getAll(): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(history: HistoryEntity)
}
