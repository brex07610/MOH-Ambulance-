package com.example.data.repository

import com.example.data.local.AmbulanceDao
import com.example.data.local.BookingDao
import com.example.data.local.MOHDatabase
import com.example.data.local.UserDao
import com.example.data.model.AmbulanceEntity
import com.example.data.model.BookingEntity
import com.example.data.model.BookingWithDetails
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MOHRepository(
    private val userDao: UserDao,
    private val ambulanceDao: AmbulanceDao,
    private val bookingDao: BookingDao
) {
    // User Authentication
    suspend fun login(username: String, password: String): Result<UserEntity> {
        val user = userDao.getUserByUsername(username.trim())
            ?: return Result.failure(Exception("User not found"))
        val hash = MOHDatabase.hashPassword(password)
        return if (user.passwordHash == hash) {
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid username or password"))
        }
    }

    suspend fun register(
        username: String,
        password: String,
        email: String,
        fullName: String,
        phoneNumber: String
    ): Result<Long> {
        val existing = userDao.getUserByUsername(username.trim())
        if (existing != null) {
            return Result.failure(Exception("Username already exists"))
        }
        val hash = MOHDatabase.hashPassword(password)
        val user = UserEntity(
            username = username.trim(),
            passwordHash = hash,
            email = email.trim(),
            fullName = fullName.trim(),
            phoneNumber = phoneNumber.trim(),
            isAdmin = false
        )
        return try {
            val id = userDao.insertUser(user)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun searchUsers(query: String): Flow<List<UserEntity>> {
        return if (query.isBlank()) {
            userDao.getAllUsers()
        } else {
            userDao.searchUsers(query.trim())
        }
    }

    // Ambulance Operations
    fun getAllAmbulances(): Flow<List<AmbulanceEntity>> = ambulanceDao.getAllAmbulances()

    fun getAvailableAmbulances(transportType: String): Flow<List<AmbulanceEntity>> =
        ambulanceDao.getAvailableAmbulancesByType(transportType)

    suspend fun addAmbulance(
        vehicleNumber: String,
        transportType: String,
        location: String,
        status: String = "Available"
    ): Result<Long> {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val ambulance = AmbulanceEntity(
            vehicleNumber = vehicleNumber.trim().uppercase(),
            transportType = transportType,
            status = status,
            currentLocation = location.trim(),
            lastUpdated = now
        )
        return try {
            val id = ambulanceDao.insertAmbulance(ambulance)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAmbulance(id: Long, status: String, location: String) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        ambulanceDao.updateStatusAndLocation(id, status, location.trim(), now)
    }

    fun getAvailableAmbulanceCount(): Flow<Int> = ambulanceDao.getAvailableAmbulanceCount()

    // Booking Operations
    fun getBookingsForUser(userId: Long): Flow<List<BookingWithDetails>> =
        bookingDao.getBookingsByUserId(userId)

    fun getPendingBookings(): Flow<List<BookingWithDetails>> = bookingDao.getPendingBookings()

    fun getActiveBookings(): Flow<List<BookingWithDetails>> = bookingDao.getActiveBookings()

    fun getAllBookings(): Flow<List<BookingWithDetails>> = bookingDao.getAllBookingsWithDetails()

    fun getBookingDetails(bookingId: Long): Flow<BookingWithDetails?> =
        bookingDao.getBookingWithDetailsById(bookingId)

    suspend fun getBookingDetailsSync(bookingId: Long): BookingWithDetails? =
        bookingDao.getBookingWithDetailsByIdSync(bookingId)

    fun getPendingCount(): Flow<Int> = bookingDao.getPendingCount()

    fun getActiveDispatchedCount(): Flow<Int> = bookingDao.getActiveDispatchedCount()

    suspend fun createBooking(
        userId: Long,
        transportType: String,
        pickupLocation: String,
        destination: String,
        medicalProblem: String,
        emergencyType: String,
        patientHistory: String?
    ): Result<Long> {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val booking = BookingEntity(
            userId = userId,
            requestedTransport = transportType,
            pickupLocation = pickupLocation.trim(),
            destination = destination.trim(),
            medicalProblem = medicalProblem.trim(),
            emergencyType = emergencyType,
            patientHistory = patientHistory?.trim(),
            bookingTime = now,
            currentStatus = "Pending"
        )
        return try {
            val id = bookingDao.insertBooking(booking)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignAmbulance(bookingId: Long, ambulanceId: Long): Result<Unit> {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        return try {
            bookingDao.assignAmbulance(bookingId, ambulanceId)
            val currentAmbulance = ambulanceDao.getAmbulanceByIdSync(ambulanceId)
            val loc = currentAmbulance?.currentLocation ?: "Dispatched from Base"
            ambulanceDao.updateStatusAndLocation(ambulanceId, "Dispatched", loc, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(bookingId: Long, status: String, eta: String?): Result<Unit> {
        return try {
            if (eta != null && eta.isNotBlank()) {
                bookingDao.updateStatusAndEta(bookingId, status, eta)
            } else {
                bookingDao.updateStatus(bookingId, status)
            }

            // Sync ambulance status if completed or cancelled
            if (status == "Complete" || status == "Cancelled") {
                val bookingDetails = bookingDao.getBookingWithDetailsByIdSync(bookingId)
                bookingDetails?.booking?.ambulanceId?.let { ambId ->
                    val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val amb = ambulanceDao.getAmbulanceByIdSync(ambId)
                    val loc = if (status == "Complete") bookingDetails.booking.destination else (amb?.currentLocation ?: "Base")
                    ambulanceDao.updateStatusAndLocation(ambId, "Available", loc, now)
                }
            } else if (status == "In Transit") {
                val bookingDetails = bookingDao.getBookingWithDetailsByIdSync(bookingId)
                bookingDetails?.booking?.ambulanceId?.let { ambId ->
                    val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val amb = ambulanceDao.getAmbulanceByIdSync(ambId)
                    ambulanceDao.updateStatusAndLocation(ambId, "In Transit", amb?.currentLocation ?: "In Transit", now)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
