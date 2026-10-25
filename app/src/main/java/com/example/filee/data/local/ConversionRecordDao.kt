package com.example.filee.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversionRecordDao {
    @Query("SELECT * FROM conversion_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ConversionRecordEntity>>

    @Query("SELECT * FROM conversion_records WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteRecords(): Flow<List<ConversionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ConversionRecordEntity): Long

    @Update
    suspend fun updateRecord(record: ConversionRecordEntity)

    @Delete
    suspend fun deleteRecord(record: ConversionRecordEntity)

    @Query("DELETE FROM conversion_records")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM conversion_records")
    fun getRecordCount(): Flow<Int>

    @Query("SELECT SUM(targetSizeBytes) FROM conversion_records")
    fun getTotalConvertedBytes(): Flow<Long?>
}
