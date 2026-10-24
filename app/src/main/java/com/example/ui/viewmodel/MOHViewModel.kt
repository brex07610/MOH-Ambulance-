package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MOHDatabase
import com.example.data.model.AmbulanceEntity
import com.example.data.model.BookingWithDetails
import com.example.data.model.UserEntity
import com.example.data.repository.MOHRepository
import com.example.data.repository.FirebaseMOHRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenMode {
    data class Citizen(val tab: CitizenTab = CitizenTab.HOME) : ScreenMode()
    data class Admin(val tab: AdminTab = AdminTab.DASHBOARD) : ScreenMode()
    object Auth : ScreenMode()
}

enum class CitizenTab {
    HOME, BOOK, TRACK, INFO, PROFILE
}

enum class AdminTab {
    DASHBOARD, PENDING, ACTIVE, FLEET, USERS
}

data class BookingFormState(
    val transportType: String = "Road",
    val pickupLocation: String = "",
    val destination: String = "",
    val medicalProblem: String = "",
    val emergencyType: String = "Cardiac Arrest / Chest Pain",
    val patientHistory: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class MOHViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MOHDatabase.getDatabase(application, viewModelScope)
    private val repository = MOHRepository(
        database.userDao(),
        database.ambulanceDao(),
        database.bookingDao()
    )
    val firebaseRepository = FirebaseMOHRepository(application)

    // Current User & Mode State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _screenMode = MutableStateFlow<ScreenMode>(ScreenMode.Citizen(CitizenTab.HOME))
    val screenMode: StateFlow<ScreenMode> = _screenMode.asStateFlow()

    // Tracking state
    private val _selectedBookingId = MutableStateFlow<Long?>(null)
    val selectedBookingId: StateFlow<Long?> = _selectedBookingId.asStateFlow()

    private val _trackedBooking = MutableStateFlow<BookingWithDetails?>(null)
    val trackedBooking: StateFlow<BookingWithDetails?> = _trackedBooking.asStateFlow()

    // Search query for admin users
    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    // Fleet filter
    private val _fleetFilter = MutableStateFlow("All")
    val fleetFilter: StateFlow<String> = _fleetFilter.asStateFlow()

    // Form state
    private val _bookingForm = MutableStateFlow(BookingFormState())
    val bookingForm: StateFlow<BookingFormState> = _bookingForm.asStateFlow()

    // User message toasts
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Reactive streams from repository
    val allAmbulances: StateFlow<List<AmbulanceEntity>> = repository.getAllAmbulances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingBookings: StateFlow<List<BookingWithDetails>> = repository.getPendingBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBookings: StateFlow<List<BookingWithDetails>> = repository.getActiveBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookings: StateFlow<List<BookingWithDetails>> = repository.getAllBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = repository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeDispatchedCount: StateFlow<Int> = repository.getActiveDispatchedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val availableAmbulanceCount: StateFlow<Int> = repository.getAvailableAmbulanceCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Auto-login as default demo citizen on startup
        viewModelScope.launch {
            val user = database.userDao().getUserByUsername("brandon")
            if (user != null) {
                _currentUser.value = user
            } else {
                // If database callback hasn't finished, provide fallback
                _currentUser.value = UserEntity(
                    userId = 2,
                    username = "brandon",
                    passwordHash = "",
                    email = "brandon@example.com",
                    fullName = "Brandon Tanaka",
                    phoneNumber = "+1 (555) 234-5678",
                    isAdmin = false
                )
            }
        }
    }

    fun setScreenMode(mode: ScreenMode) {
        _screenMode.value = mode
    }

    fun setCitizenTab(tab: CitizenTab) {
        _screenMode.value = ScreenMode.Citizen(tab)
    }

    fun setAdminTab(tab: AdminTab) {
        _screenMode.value = ScreenMode.Admin(tab)
    }

    fun switchRole(asAdmin: Boolean) {
        viewModelScope.launch {
            if (asAdmin) {
                val admin = database.userDao().getUserByUsername("admin")
                if (admin != null) {
                    _currentUser.value = admin
                    _screenMode.value = ScreenMode.Admin(AdminTab.DASHBOARD)
                    _toastEvent.emit("Switched to Dispatcher Admin: ${admin.fullName}")
                }
            } else {
                val citizen = database.userDao().getUserByUsername("brandon")
                if (citizen != null) {
                    _currentUser.value = citizen
                    _screenMode.value = ScreenMode.Citizen(CitizenTab.HOME)
                    _toastEvent.emit("Switched to Citizen: ${citizen.fullName}")
                }
            }
        }
    }

    fun login(username: String, password: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(username, password)
            result.onSuccess { user ->
                _currentUser.value = user
                if (user.isAdmin) {
                    _screenMode.value = ScreenMode.Admin(AdminTab.DASHBOARD)
                } else {
                    _screenMode.value = ScreenMode.Citizen(CitizenTab.HOME)
                }
                _toastEvent.emit("Welcome back, ${user.fullName}!")
                onComplete(true, "Login successful")
            }.onFailure { err ->
                onComplete(false, err.message ?: "Login failed")
            }
        }
    }

    fun register(
        username: String,
        password: String,
        email: String,
        fullName: String,
        phone: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.register(username, password, email, fullName, phone)
            result.onSuccess {
                login(username, password) { _, _ -> }
                onComplete(true, "Registration successful")
            }.onFailure { err ->
                onComplete(false, err.message ?: "Registration failed")
            }
        }
    }

    fun handleGoogleUserSignedIn(user: FirebaseUser) {
        viewModelScope.launch {
            val email = user.email ?: ""
            val displayName = user.displayName ?: if (email.isNotEmpty()) email.substringBefore("@") else "Citizen"
            val isAdmin = email.equals("macbrandon07@gmail.com", ignoreCase = true)

            try {
                firebaseRepository.saveUserProfile(
                    userId = user.uid,
                    username = if (email.isNotEmpty()) email.substringBefore("@") else "google_user",
                    email = email,
                    fullName = displayName,
                    phoneNumber = user.phoneNumber ?: "",
                    isAdmin = isAdmin
                )
            } catch (e: Exception) {
                // Logged via handleFirestoreError
            }

            _currentUser.value = UserEntity(
                userId = 999,
                username = if (email.isNotEmpty()) email.substringBefore("@") else "user",
                passwordHash = "",
                email = email,
                fullName = displayName,
                phoneNumber = user.phoneNumber ?: "+1 (555) 0199",
                isAdmin = isAdmin
            )

            if (isAdmin) {
                _screenMode.value = ScreenMode.Admin(AdminTab.DASHBOARD)
            } else {
                _screenMode.value = ScreenMode.Citizen(CitizenTab.HOME)
            }
            _toastEvent.emit("Signed in with Google: $displayName")
        }
    }

    fun logout() {
        Firebase.auth.signOut()
        _currentUser.value = null
        _screenMode.value = ScreenMode.Auth
    }

    // Booking Form
    fun updateBookingForm(
        transportType: String? = null,
        pickupLocation: String? = null,
        destination: String? = null,
        medicalProblem: String? = null,
        emergencyType: String? = null,
        patientHistory: String? = null
    ) {
        _bookingForm.value = _bookingForm.value.copy(
            transportType = transportType ?: _bookingForm.value.transportType,
            pickupLocation = pickupLocation ?: _bookingForm.value.pickupLocation,
            destination = destination ?: _bookingForm.value.destination,
            medicalProblem = medicalProblem ?: _bookingForm.value.medicalProblem,
            emergencyType = emergencyType ?: _bookingForm.value.emergencyType,
            patientHistory = patientHistory ?: _bookingForm.value.patientHistory,
            errorMessage = null
        )
    }

    fun submitBooking(onSuccess: (Long) -> Unit) {
        val user = _currentUser.value ?: return
        val form = _bookingForm.value

        if (form.pickupLocation.isBlank()) {
            _bookingForm.value = form.copy(errorMessage = "Please enter pickup location")
            return
        }
        if (form.destination.isBlank()) {
            _bookingForm.value = form.copy(errorMessage = "Please select destination hospital")
            return
        }
        if (form.medicalProblem.isBlank()) {
            _bookingForm.value = form.copy(errorMessage = "Please describe medical condition")
            return
        }

        viewModelScope.launch {
            _bookingForm.value = form.copy(isSubmitting = true)
            val result = repository.createBooking(
                userId = user.userId,
                transportType = form.transportType,
                pickupLocation = form.pickupLocation,
                destination = form.destination,
                medicalProblem = form.medicalProblem,
                emergencyType = form.emergencyType,
                patientHistory = form.patientHistory.ifBlank { null }
            )
            result.onSuccess { newId ->
                _bookingForm.value = BookingFormState() // Reset
                _selectedBookingId.value = newId
                selectBookingForTracking(newId)
                _screenMode.value = ScreenMode.Citizen(CitizenTab.TRACK)
                _toastEvent.emit("Emergency booking created! ID: #$newId")
                onSuccess(newId)
            }.onFailure { err ->
                _bookingForm.value = form.copy(
                    isSubmitting = false,
                    errorMessage = err.message ?: "Failed to create booking"
                )
            }
        }
    }

    // Quick SOS 1-Tap Booking
    fun quickSosBooking(transportType: String = "Road", emergencyType: String = "Critical Emergency SOS") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.createBooking(
                userId = user.userId,
                transportType = transportType,
                pickupLocation = "Current GPS Location (Quick SOS)",
                destination = "Nearest MOH Trauma Center",
                medicalProblem = "Urgent high-priority ambulance call triggered via Rapid SOS",
                emergencyType = emergencyType,
                patientHistory = "Emergency dispatched"
            )
            result.onSuccess { newId ->
                selectBookingForTracking(newId)
                _screenMode.value = ScreenMode.Citizen(CitizenTab.TRACK)
                _toastEvent.emit("🚨 Emergency SOS Dispatched! Booking ID: #$newId")
            }
        }
    }

    // Tracking
    fun selectBookingForTracking(bookingId: Long) {
        _selectedBookingId.value = bookingId
        viewModelScope.launch {
            repository.getBookingDetails(bookingId).collect { details ->
                _trackedBooking.value = details
            }
        }
    }

    // Admin Actions
    fun assignAmbulance(bookingId: Long, ambulanceId: Long) {
        viewModelScope.launch {
            val res = repository.assignAmbulance(bookingId, ambulanceId)
            res.onSuccess {
                _toastEvent.emit("Ambulance successfully assigned and dispatched!")
            }.onFailure {
                _toastEvent.emit("Assignment failed: ${it.message}")
            }
        }
    }

    fun updateBookingStatus(bookingId: Long, newStatus: String, eta: String? = null) {
        viewModelScope.launch {
            val res = repository.updateBookingStatus(bookingId, newStatus, eta)
            res.onSuccess {
                _toastEvent.emit("Booking #$bookingId updated to '$newStatus'")
            }.onFailure {
                _toastEvent.emit("Update failed: ${it.message}")
            }
        }
    }

    fun addAmbulance(vehicleNumber: String, transportType: String, location: String, status: String) {
        viewModelScope.launch {
            val res = repository.addAmbulance(vehicleNumber, transportType, location, status)
            res.onSuccess {
                _toastEvent.emit("Ambulance $vehicleNumber added to fleet!")
            }.onFailure {
                _toastEvent.emit("Error: ${it.message}")
            }
        }
    }

    fun updateAmbulanceStatus(id: Long, status: String, location: String) {
        viewModelScope.launch {
            repository.updateAmbulance(id, status, location)
            _toastEvent.emit("Ambulance status updated to '$status'")
        }
    }

    fun setFleetFilter(filter: String) {
        _fleetFilter.value = filter
    }

    fun setUserSearchQuery(query: String) {
        _userSearchQuery.value = query
    }
}
