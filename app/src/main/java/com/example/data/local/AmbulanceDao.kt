package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AmbulanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AmbulanceDao {
    @Query("SELECT * FROM ambulances ORDER BY ambulance_id DESC")
    fun getAllAmbulances(): Flow<List<AmbulanceEntity>>

    @Query("SELECT * FROM ambulances WHERE status = 'Available' AND transport_type = :transportType")
    fun getAvailableAmbulancesByType(transportType: String): Flow<List<AmbulanceEntity>>

    @Query("SELECT * FROM ambulances WHERE status = 'Available' AND transport_type = :transportType")
    suspend fun getAvailableAmbulancesByTypeSync(transportType: String): List<AmbulanceEntity>

    @Query("SELECT * FROM ambulances WHERE ambulance_id = :id LIMIT 1")
    fun getAmbulanceById(id: Long): Flow<AmbulanceEntity?>

    @Query("SELECT * FROM ambulances WHERE ambulance_id = :id LIMIT 1")
    suspend fun getAmbulanceByIdSync(id: Long): AmbulanceEntity?

    @Query("SELECT COUNT(*) FROM ambulances WHERE status = 'Available'")
    fun getAvailableAmbulanceCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ambulances")
    fun getTotalAmbulanceCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAmbulance(ambulance: AmbulanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ambulances: List<AmbulanceEntity>)

    @Update
    suspend fun updateAmbulance(ambulance: AmbulanceEntity)

    @Query("UPDATE ambulances SET status = :status, current_location = :location, last_updated = :lastUpdated WHERE ambulance_id = :id")
    suspend fun updateStatusAndLocation(id: Long, status: String, location: String, lastUpdated: String)

    @Query("SELECT COUNT(*) FROM ambulances")
    suspend fun getCount(): Int
}
