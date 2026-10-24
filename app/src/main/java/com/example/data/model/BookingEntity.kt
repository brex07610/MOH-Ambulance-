package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AmbulanceEntity::class,
            parentColumns = ["ambulance_id"],
            childColumns = ["ambulance_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("user_id"),
        Index("ambulance_id")
    ]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "booking_id")
    val bookingId: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "ambulance_id")
    val ambulanceId: Long? = null,

    @ColumnInfo(name = "requested_transport")
    val requestedTransport: String, // "Road", "Air"

    @ColumnInfo(name = "pickup_location")
    val pickupLocation: String,

    @ColumnInfo(name = "destination")
    val destination: String,

    @ColumnInfo(name = "medical_problem")
    val medicalProblem: String,

    @ColumnInfo(name = "emergency_type")
    val emergencyType: String,

    @ColumnInfo(name = "patient_history")
    val patientHistory: String? = null,

    @ColumnInfo(name = "booking_time")
    val bookingTime: String,

    @ColumnInfo(name = "current_status")
    val currentStatus: String = "Pending", // "Pending", "Confirmed", "Dispatched", "In Transit", "Arrived", "Complete", "Cancelled"

    @ColumnInfo(name = "estimated_arrival")
    val estimatedArrival: String? = null
)
