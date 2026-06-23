package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseDao {
    @Query("SELECT * FROM house_records ORDER BY houseNo ASC")
    fun getAllRecordsFlow(): Flow<List<HouseRecord>>

    @Query("SELECT * FROM house_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Int): HouseRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<HouseRecord>)

    @Query("DELETE FROM house_records")
    suspend fun clearAllRecords()
}
