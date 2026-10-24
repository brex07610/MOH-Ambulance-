package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AmbulanceEntity
import com.example.data.model.BookingEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        UserEntity::class,
        AmbulanceEntity::class,
        BookingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MOHDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun ambulanceDao(): AmbulanceDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: MOHDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MOHDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MOHDatabase::class.java,
                    "ministry_of_health_ambulance.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: MOHDatabase) {
            val userDao = database.userDao()
            val ambulanceDao = database.ambulanceDao()
            val bookingDao = database.bookingDao()

            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // 1. Pre-seed users
            val adminUser = UserEntity(
                userId = 1,
                username = "admin",
                passwordHash = hashPassword("admin123"),
                email = "admin@health.gov",
                fullName = "Dr. Sarah Chen (Chief Dispatcher)",
                phoneNumber = "+1 (800) 555-0199",
                isAdmin = true
            )

            val citizenUser = UserEntity(
                userId = 2,
                username = "brandon",
                passwordHash = hashPassword("pass123"),
                email = "brandon@example.com",
                fullName = "Brandon Tanaka",
                phoneNumber = "+1 (555) 234-5678",
                isAdmin = false
            )

            userDao.insertUser(adminUser)
            userDao.insertUser(citizenUser)

            // 2. Pre-seed Ambulances
            val ambulances = listOf(
                AmbulanceEntity(
                    ambulanceId = 1,
                    vehicleNumber = "MOH-AMB-101",
                    transportType = "Road",
                    status = "Available",
                    currentLocation = "Central Hospital Base - Bay 1",
                    lastUpdated = now
                ),
                AmbulanceEntity(
                    ambulanceId = 2,
                    vehicleNumber = "MOH-AMB-102",
                    transportType = "Road",
                    status = "Available",
                    currentLocation = "Eastside Emergency Substation",
                    lastUpdated = now
                ),
                AmbulanceEntity(
                    ambulanceId = 3,
                    vehicleNumber = "MOH-AMB-103",
                    transportType = "Road",
                    status = "Dispatched",
                    currentLocation = "4th Avenue & Victoria Crossing",
                    lastUpdated = now
                ),
                AmbulanceEntity(
                    ambulanceId = 4,
                    vehicleNumber = "MOH-AMB-104",
                    transportType = "Road",
                    status = "Maintenance",
                    currentLocation = "Central Maintenance Fleet Depot",
                    lastUpdated = now
                ),
                AmbulanceEntity(
                    ambulanceId = 5,
                    vehicleNumber = "AIR-MED-01",
                    transportType = "Air",
                    status = "In Transit",
                    currentLocation = "Air Corridor 3 - Heading to Valley",
                    lastUpdated = now
                ),
                AmbulanceEntity(
                    ambulanceId = 6,
                    vehicleNumber = "AIR-MED-02",
                    transportType = "Air",
                    status = "Available",
                    currentLocation = "MOH Regional Helipad Bravo",
                    lastUpdated = now
                )
            )
            ambulanceDao.insertAll(ambulances)

            // 3. Pre-seed Bookings
            val sampleBookings = listOf(
                BookingEntity(
                    bookingId = 1,
                    userId = 2,
                    ambulanceId = null,
                    requestedTransport = "Road",
                    pickupLocation = "42 Maple Street, Sector 7",
                    destination = "MOH Central General Hospital",
                    medicalProblem = "Acute shortness of breath and severe chest pressure",
                    emergencyType = "Cardiac Arrest / Chest Pain",
                    patientHistory = "Hypertension, stent placed 2022",
                    bookingTime = now,
                    currentStatus = "Pending",
                    estimatedArrival = null
                ),
                BookingEntity(
                    bookingId = 2,
                    userId = 2,
                    ambulanceId = 5,
                    requestedTransport = "Air",
                    pickupLocation = "Highway 102 Mile Marker 48",
                    destination = "National Level-1 Trauma Centre",
                    medicalProblem = "Severe multi-vehicle rollover accident with head trauma",
                    emergencyType = "Severe Accident / Trauma",
                    patientHistory = "No known drug allergies",
                    bookingTime = now,
                    currentStatus = "In Transit",
                    estimatedArrival = "8 mins"
                )
            )
            bookingDao.insertAll(sampleBookings)
        }
    }
}
