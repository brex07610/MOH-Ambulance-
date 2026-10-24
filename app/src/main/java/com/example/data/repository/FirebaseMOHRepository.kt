package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firestore.OperationType
import com.example.data.firestore.handleFirestoreError
import com.example.data.model.AmbulanceDocument
import com.example.data.model.BookingDocument
import com.example.data.model.UserDocument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirebaseMOHRepository(
    private val db: FirebaseFirestore
) {
    // Secondary constructor resolving named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile Operations ---
    suspend fun saveUserProfile(
        userId: String,
        username: String,
        email: String,
        fullName: String,
        phoneNumber: String,
        isAdmin: Boolean = false
    ) {
        val path = "users/$userId"
        val userRef = db.collection("users").document(userId)
        val payload = mapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "fullName" to fullName,
            "phoneNumber" to phoneNumber,
            "isAdmin" to isAdmin,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            userRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeUserProfile(userId: String): Flow<UserDocument?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { it.toObject(UserDocument::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, "users/$userId")
                throw error
            }

    fun observeAllUsers(): Flow<List<UserDocument>> =
        db.collection("users")
            .snapshots()
            .map { it.toObjects(UserDocument::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "users")
                throw error
            }

    // --- Ambulances Operations ---
    fun observeAmbulances(): Flow<List<AmbulanceDocument>> =
        db.collection("ambulances")
            .snapshots()
            .map { it.toObjects(AmbulanceDocument::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "ambulances")
                throw error
            }

    suspend fun seedInitialAmbulancesIfEmpty() {
        try {
            val snapshot = db.collection("ambulances").limit(1).get().await()
            if (snapshot.isEmpty) {
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val initialUnits = listOf(
                    AmbulanceDocument("MOH-AMB-101", "MOH-AMB-101", "Road", "Available", "Central Hospital Base - Bay 1", now),
                    AmbulanceDocument("MOH-AMB-102", "MOH-AMB-102", "Road", "Available", "Eastside Emergency Substation", now),
                    AmbulanceDocument("MOH-AMB-103", "MOH-AMB-103", "Road", "Dispatched", "4th Avenue & Victoria Crossing", now),
                    AmbulanceDocument("MOH-AMB-104", "MOH-AMB-104", "Road", "Maintenance", "Central Maintenance Depot", now),
                    AmbulanceDocument("AIR-MED-01", "AIR-MED-01", "Air", "In Transit", "Air Corridor 3 - Heading to Valley", now),
                    AmbulanceDocument("AIR-MED-02", "AIR-MED-02", "Air", "Available", "MOH Regional Helipad Bravo", now)
                )
                val batch = db.batch()
                initialUnits.forEach { amb ->
                    val doc = db.collection("ambulances").document(amb.ambulanceId)
                    batch.set(doc, amb)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "ambulances")
        }
    }

    suspend fun addAmbulance(
        vehicleNumber: String,
        transportType: String,
        location: String,
        status: String = "Available"
    ): Result<String> {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val id = vehicleNumber.trim().uppercase()
        val ambulance = AmbulanceDocument(
            ambulanceId = id,
            vehicleNumber = id,
            transportType = transportType,
            status = status,
            currentLocation = location.trim(),
            lastUpdated = now
        )
        return try {
            db.collection("ambulances").document(id).set(ambulance).await()
            Result.success(id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "ambulances/$id")
            Result.failure(e)
        }
    }

    suspend fun updateAmbulanceStatus(ambulanceId: String, status: String, location: String) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val path = "ambulances/$ambulanceId"
        try {
            db.collection("ambulances").document(ambulanceId).update(
                mapOf(
                    "status" to status,
                    "currentLocation" to location,
                    "lastUpdated" to now
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    // --- Bookings Operations ---
    fun observeUserBookings(userId: String): Flow<List<BookingDocument>> =
        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { it.toObjects(BookingDocument::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "bookings")
                throw error
            }

    fun observeAllBookings(): Flow<List<BookingDocument>> =
        db.collection("bookings")
            .snapshots()
            .map { it.toObjects(BookingDocument::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "bookings")
                throw error
            }

    fun observeBookingById(bookingId: String): Flow<BookingDocument?> =
        db.collection("bookings").document(bookingId)
            .snapshots()
            .map { it.toObject(BookingDocument::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, "bookings/$bookingId")
                throw error
            }

    suspend fun createBooking(
        transportType: String,
        pickupLocation: String,
        destination: String,
        medicalProblem: String,
        emergencyType: String,
        patientHistory: String?
    ): Result<String> {
        val uid = requireUserId()
        val bookingId = "BK-${System.currentTimeMillis().toString().takeLast(6)}"
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val payload = mutableMapOf<String, Any>(
            "bookingId" to bookingId,
            "userId" to uid,
            "requestedTransport" to transportType,
            "pickupLocation" to pickupLocation.trim(),
            "destination" to destination.trim(),
            "medicalProblem" to medicalProblem.trim(),
            "emergencyType" to emergencyType,
            "bookingTime" to now,
            "currentStatus" to "Pending",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!patientHistory.isNullOrBlank()) {
            payload["patientHistory"] = patientHistory.trim()
        }

        return try {
            db.collection("bookings").document(bookingId).set(payload).await()
            Result.success(bookingId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "bookings/$bookingId")
            Result.failure(e)
        }
    }

    suspend fun assignAmbulance(bookingId: String, ambulanceId: String): Result<Unit> {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        return try {
            val batch = db.batch()
            val bookingRef = db.collection("bookings").document(bookingId)
            batch.update(
                bookingRef,
                mapOf(
                    "ambulanceId" to ambulanceId,
                    "currentStatus" to "Dispatched",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            val ambRef = db.collection("ambulances").document(ambulanceId)
            batch.update(
                ambRef,
                mapOf(
                    "status" to "Dispatched",
                    "lastUpdated" to now
                )
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "bookings/$bookingId")
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String, eta: String?): Result<Unit> {
        val updates = mutableMapOf<String, Any>(
            "currentStatus" to status,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (eta != null && eta.isNotBlank()) {
            updates["estimatedArrival"] = eta
        }

        return try {
            db.collection("bookings").document(bookingId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "bookings/$bookingId")
            Result.failure(e)
        }
    }
}
