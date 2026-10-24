package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ambulances")
data class AmbulanceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ambulance_id")
    val ambulanceId: Long = 0,

    @ColumnInfo(name = "vehicle_number")
    val vehicleNumber: String,

    @ColumnInfo(name = "transport_type")
    val transportType: String, // "Road", "Air"

    @ColumnInfo(name = "status")
    val status: String = "Available", // "Available", "Dispatched", "Maintenance", "In Transit"

    @ColumnInfo(name = "current_location")
    val currentLocation: String,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: String
)
