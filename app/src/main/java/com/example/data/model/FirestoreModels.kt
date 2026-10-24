package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserDocument(
    @DocumentId
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val isAdmin: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AmbulanceDocument(
    @DocumentId
    val ambulanceId: String = "",
    val vehicleNumber: String = "",
    val transportType: String = "Road", // "Road", "Air"
    val status: String = "Available",   // "Available", "Dispatched", "In Transit", "Maintenance"
    val currentLocation: String = "",
    val lastUpdated: String = ""
)

@IgnoreExtraProperties
data class BookingDocument(
    @DocumentId
    val bookingId: String = "",
    val userId: String = "",
    val ambulanceId: String? = null,
    val requestedTransport: String = "Road", // "Road", "Air"
    val pickupLocation: String = "",
    val destination: String = "",
    val medicalProblem: String = "",
    val emergencyType: String = "",
    val patientHistory: String? = null,
    val bookingTime: String = "",
    val currentStatus: String = "Pending", // "Pending", "Confirmed", "Dispatched", "In Transit", "Arrived", "Complete", "Cancelled"
    val estimatedArrival: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
