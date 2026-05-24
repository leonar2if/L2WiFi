package com.l2wifi.data.local.dao

import androidx.room.*
import com.l2wifi.data.local.entity.ActiveSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveSessionDao {
    @Query("SELECT * FROM active_session LIMIT 1")
    suspend fun getActive(): ActiveSessionEntity?

    @Query("SELECT * FROM active_session LIMIT 1")
    fun getActiveFlow(): Flow<ActiveSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ActiveSessionEntity)

    @Query("DELETE FROM active_session")
    suspend fun deleteAll()
}
