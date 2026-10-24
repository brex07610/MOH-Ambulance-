package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.BookingEntity
import com.example.data.model.BookingWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Transaction
    @Query("SELECT * FROM bookings ORDER BY booking_id DESC")
    fun getAllBookingsWithDetails(): Flow<List<BookingWithDetails>>

    @Transaction
    @Query("SELECT * FROM bookings WHERE user_id = :userId ORDER BY booking_id DESC")
    fun getBookingsByUserId(userId: Long): Flow<List<BookingWithDetails>>

    @Transaction
    @Query("SELECT * FROM bookings WHERE current_status = 'Pending' ORDER BY booking_id DESC")
    fun getPendingBookings(): Flow<List<BookingWithDetails>>

    @Transaction
    @Query("SELECT * FROM bookings WHERE current_status IN ('Dispatched', 'Confirmed', 'In Transit') ORDER BY booking_id DESC")
    fun getActiveBookings(): Flow<List<BookingWithDetails>>

    @Transaction
    @Query("SELECT * FROM bookings WHERE booking_id = :bookingId LIMIT 1")
    fun getBookingWithDetailsById(bookingId: Long): Flow<BookingWithDetails?>

    @Transaction
    @Query("SELECT * FROM bookings WHERE booking_id = :bookingId LIMIT 1")
    suspend fun getBookingWithDetailsByIdSync(bookingId: Long): BookingWithDetails?

    @Query("SELECT COUNT(*) FROM bookings WHERE current_status = 'Pending'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM bookings WHERE current_status IN ('Dispatched', 'Confirmed', 'In Transit')")
    fun getActiveDispatchedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<BookingEntity>)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET ambulance_id = :ambulanceId, current_status = 'Dispatched' WHERE booking_id = :bookingId")
    suspend fun assignAmbulance(bookingId: Long, ambulanceId: Long)

    @Query("UPDATE bookings SET current_status = :status, estimated_arrival = :eta WHERE booking_id = :bookingId")
    suspend fun updateStatusAndEta(bookingId: Long, status: String, eta: String?)

    @Query("UPDATE bookings SET current_status = :status WHERE booking_id = :bookingId")
    suspend fun updateStatus(bookingId: Long, status: String)

    @Query("SELECT COUNT(*) FROM bookings")
    suspend fun getCount(): Int
}
