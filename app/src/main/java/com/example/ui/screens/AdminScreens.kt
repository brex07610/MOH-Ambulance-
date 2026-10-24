package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmbulanceEntity
import com.example.data.model.BookingWithDetails
import com.example.data.model.UserEntity
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.StatusArrived
import com.example.ui.theme.StatusDispatched
import com.example.ui.theme.StatusInTransit
import com.example.ui.theme.StatusPending
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.MOHViewModel

@Composable
fun AdminMainContainer(
    viewModel: MOHViewModel,
    modifier: Modifier = Modifier
) {
    val pendingCount by viewModel.pendingCount.collectAsState()
    val activeDispatchedCount by viewModel.activeDispatchedCount.collectAsState()
    val availableAmbulances by viewModel.availableAmbulanceCount.collectAsState()
    val allAmbulances by viewModel.allAmbulances.collectAsState()
    val pendingBookings by viewModel.pendingBookings.collectAsState()
    val activeBookings by viewModel.activeBookings.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var currentTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_main_container")
    ) {
        // Admin Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminNavPill(
                title = "Dashboard",
                isSelected = currentTab == AdminTab.DASHBOARD,
                badge = null
            ) { currentTab = AdminTab.DASHBOARD }

            AdminNavPill(
                title = "Pending Calls",
                isSelected = currentTab == AdminTab.PENDING,
                badge = if (pendingCount > 0) "$pendingCount" else null,
                badgeColor = EmergencyRed
            ) { currentTab = AdminTab.PENDING }

            AdminNavPill(
                title = "Active Dispatches",
                isSelected = currentTab == AdminTab.ACTIVE,
                badge = if (activeDispatchedCount > 0) "$activeDispatchedCount" else null,
                badgeColor = StatusDispatched
            ) { currentTab = AdminTab.ACTIVE }

            AdminNavPill(
                title = "Fleet Vehicles",
                isSelected = currentTab == AdminTab.FLEET,
                badge = "${allAmbulances.size}"
            ) { currentTab = AdminTab.FLEET }

            AdminNavPill(
                title = "User Directory",
                isSelected = currentTab == AdminTab.USERS,
                badge = "${allUsers.size}"
            ) { currentTab = AdminTab.USERS }
        }

        when (currentTab) {
            AdminTab.DASHBOARD -> AdminDashboardView(
                viewModel = viewModel,
                pendingCount = pendingCount,
                dispatchedCount = activeDispatchedCount,
                availableAmbulances = availableAmbulances,
                totalAmbulances = allAmbulances.size,
                onNavigate = { currentTab = it }
            )
            AdminTab.PENDING -> AdminPendingBookingsView(
                viewModel = viewModel,
                pendingList = pendingBookings,
                ambulances = allAmbulances
            )
            AdminTab.ACTIVE -> AdminActiveBookingsView(
                viewModel = viewModel,
                activeList = activeBookings
            )
            AdminTab.FLEET -> AdminFleetManagementView(
                viewModel = viewModel,
                ambulances = allAmbulances
            )
            AdminTab.USERS -> AdminUsersView(
                viewModel = viewModel,
                users = allUsers
            )
        }
    }
}

@Composable
fun AdminNavPill(
    title: String,
    isSelected: Boolean,
    badge: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color.White else badgeColor,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardView(
    viewModel: MOHViewModel,
    pendingCount: Int,
    dispatchedCount: Int,
    availableAmbulances: Int,
    totalAmbulances: Int,
    onNavigate: (AdminTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "MOH Central Dispatch Operations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Real-time emergency call triage and fleet coordination",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // KPI Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Pending Calls",
                            value = "$pendingCount",
                            color = EmergencyRed,
                            icon = Icons.Default.Warning,
                            modifier = Modifier.weight(1f)
                        ) { onNavigate(AdminTab.PENDING) }

                        StatCard(
                            title = "Dispatched",
                            value = "$dispatchedCount",
                            color = StatusDispatched,
                            icon = Icons.Default.Send,
                            modifier = Modifier.weight(1f)
                        ) { onNavigate(AdminTab.ACTIVE) }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // KPI Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Available Fleet",
                            value = "$availableAmbulances",
                            color = StatusArrived,
                            icon = Icons.Default.LocalHospital,
                            modifier = Modifier.weight(1f)
                        ) { onNavigate(AdminTab.FLEET) }

                        StatCard(
                            title = "Total Fleet Units",
                            value = "$totalAmbulances",
                            color = MedicalTeal,
                            icon = Icons.Default.Flight,
                            modifier = Modifier.weight(1f)
                        ) { onNavigate(AdminTab.FLEET) }
                    }
                }
            }
        }

        // Quick shortcut cards
        item {
            Text(
                text = "Dispatch Command Hub",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionShortcutCard(
                title = "Assign Pending Emergency Calls ($pendingCount)",
                desc = "Match available Road ALS and Air MEDEVAC units to incoming calls",
                color = EmergencyRed
            ) { onNavigate(AdminTab.PENDING) }

            Spacer(modifier = Modifier.height(8.dp))
            ActionShortcutCard(
                title = "Track Active Dispatches ($dispatchedCount)",
                desc = "Adjust estimated arrivals (ETA) and update trip progression status",
                color = StatusDispatched
            ) { onNavigate(AdminTab.ACTIVE) }

            Spacer(modifier = Modifier.height(8.dp))
            ActionShortcutCard(
                title = "Manage Ambulance Fleet",
                desc = "Add vehicles, update base coordinates, and toggle maintenance status",
                color = MedicalTeal
            ) { onNavigate(AdminTab.FLEET) }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = color
            )
        }
    }
}

@Composable
fun ActionShortcutCard(title: String, desc: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AdminPendingBookingsView(
    viewModel: MOHViewModel,
    pendingList: List<BookingWithDetails>,
    ambulances: List<AmbulanceEntity>
) {
    var selectedBookingToAssign by remember { mutableStateOf<BookingWithDetails?>(null) }

    if (selectedBookingToAssign != null) {
        AssignAmbulanceDialog(
            bookingDetails = selectedBookingToAssign!!,
            availableAmbulances = ambulances.filter {
                it.status == "Available" && it.transportType == selectedBookingToAssign!!.booking.requestedTransport
            },
            onDismiss = { selectedBookingToAssign = null },
            onAssign = { ambId ->
                viewModel.assignAmbulance(selectedBookingToAssign!!.booking.bookingId, ambId)
                selectedBookingToAssign = null
            }
        )
    }

    if (pendingList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = StatusArrived,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Pending Emergency Calls",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "All emergency calls have been dispatched to fleet units.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pendingList, key = { it.booking.bookingId }) { item ->
                val booking = item.booking
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (booking.requestedTransport == "Air") MedicalTeal else EmergencyRed,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (booking.requestedTransport == "Air") Icons.Default.Flight else Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Emergency #${booking.bookingId}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${booking.requestedTransport} Request • ${booking.bookingTime}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmergencyRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "PENDING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmergencyRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = booking.emergencyType,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = EmergencyRed
                        )
                        Text(
                            text = "Condition: ${booking.medicalProblem}",
                            fontSize = 13.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pickup: ${booking.pickupLocation} ➔ Destination: ${booking.destination}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { selectedBookingToAssign = item },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Assign & Dispatch Ambulance")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignAmbulanceDialog(
    bookingDetails: BookingWithDetails,
    availableAmbulances: List<AmbulanceEntity>,
    onDismiss: () -> Unit,
    onAssign: (Long) -> Unit
) {
    var selectedAmbulanceId by remember { mutableStateOf<Long?>(availableAmbulances.firstOrNull()?.ambulanceId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Assign Ambulance to Call #${bookingDetails.booking.bookingId}")
        },
        text = {
            Column {
                Text(
                    text = "Requested Transport: ${bookingDetails.booking.requestedTransport} Ambulance",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Pickup: ${bookingDetails.booking.pickupLocation}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (availableAmbulances.isEmpty()) {
                    Text(
                        text = "No available ${bookingDetails.booking.requestedTransport} ambulances at base stations. Please free a unit or switch transport.",
                        color = EmergencyRed,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "Select Available Unit to Dispatch:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    availableAmbulances.forEach { amb ->
                        val isSelected = selectedAmbulanceId == amb.ambulanceId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedAmbulanceId = amb.ambulanceId }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (amb.transportType == "Air") Icons.Default.Flight else Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = amb.vehicleNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Base: ${amb.currentLocation}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedAmbulanceId?.let { onAssign(it) }
                },
                enabled = selectedAmbulanceId != null,
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
            ) {
                Text("Confirm Dispatch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminActiveBookingsView(
    viewModel: MOHViewModel,
    activeList: List<BookingWithDetails>
) {
    if (activeList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = StatusDispatched,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Active Dispatches",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Assign pending calls from the Pending tab to initiate dispatches.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(activeList, key = { it.booking.bookingId }) { item ->
                val booking = item.booking
                val amb = item.ambulance
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Call #${booking.bookingId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusDispatched.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = booking.currentStatus.uppercase(),
                                    color = StatusDispatched,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Assigned Unit: ${amb?.vehicleNumber ?: "Unassigned"} (${booking.requestedTransport})",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Pickup: ${booking.pickupLocation} ➔ Hospital: ${booking.destination}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Update Status:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("In Transit", "Arrived", "Complete", "Cancelled").forEach { status ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (booking.currentStatus == status) EmergencyRed else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable {
                                        viewModel.updateBookingStatus(booking.bookingId, status)
                                    }
                                ) {
                                    Text(
                                        text = status,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (booking.currentStatus == status) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Set ETA Quick Chips:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("5 mins", "10 mins", "15 mins", "25 mins").forEach { eta ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (booking.estimatedArrival == eta) MedicalTeal else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable {
                                        viewModel.updateBookingStatus(booking.bookingId, booking.currentStatus, eta)
                                    }
                                ) {
                                    Text(
                                        text = eta,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (booking.estimatedArrival == eta) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFleetManagementView(
    viewModel: MOHViewModel,
    ambulances: List<AmbulanceEntity>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredList = when (selectedFilter) {
        "Available" -> ambulances.filter { it.status == "Available" }
        "Dispatched" -> ambulances.filter { it.status == "Dispatched" }
        "In Transit" -> ambulances.filter { it.status == "In Transit" }
        "Maintenance" -> ambulances.filter { it.status == "Maintenance" }
        else -> ambulances
    }

    if (showAddDialog) {
        AddAmbulanceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { vehicleNumber, transportType, location, status ->
                viewModel.addAmbulance(vehicleNumber, transportType, location, status)
                showAddDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ambulance Fleet (${ambulances.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Unit")
            }
        }

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Available", "Dispatched", "In Transit", "Maintenance").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MedicalTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList, key = { it.ambulanceId }) { amb ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (amb.transportType == "Air") MedicalTeal else EmergencyRed,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (amb.transportType == "Air") Icons.Default.Flight else Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = amb.vehicleNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${amb.transportType} Unit • ${amb.lastUpdated}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (amb.status) {
                                    "Available" -> StatusArrived.copy(alpha = 0.15f)
                                    "Dispatched" -> StatusDispatched.copy(alpha = 0.15f)
                                    "In Transit" -> StatusInTransit.copy(alpha = 0.15f)
                                    else -> Color.Gray.copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = amb.status.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when (amb.status) {
                                        "Available" -> StatusArrived
                                        "Dispatched" -> StatusDispatched
                                        "In Transit" -> StatusInTransit
                                        else -> Color.Gray
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Base Location: ${amb.currentLocation}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Available", "Dispatched", "Maintenance").forEach { newStatus ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (amb.status == newStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable {
                                        viewModel.updateAmbulanceStatus(amb.ambulanceId, newStatus, amb.currentLocation)
                                    }
                                ) {
                                    Text(
                                        text = "Set $newStatus",
                                        fontSize = 11.sp,
                                        color = if (amb.status == newStatus) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddAmbulanceDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var vehicleNumber by remember { mutableStateOf("") }
    var transportType by remember { mutableStateOf("Road") }
    var location by remember { mutableStateOf("MOH Central Base") }
    var status by remember { mutableStateOf("Available") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ambulance Vehicle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = { Text("Vehicle Plate / Number (e.g. MOH-105)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = transportType == "Road",
                        onClick = { transportType = "Road" },
                        label = { Text("Road Van") }
                    )
                    FilterChip(
                        selected = transportType == "Air",
                        onClick = { transportType = "Air" },
                        label = { Text("Air Helicopter") }
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Base Substation / Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (vehicleNumber.isNotBlank() && location.isNotBlank()) {
                        onAdd(vehicleNumber, transportType, location, status)
                    }
                },
                enabled = vehicleNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
            ) {
                Text("Add Vehicle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminUsersView(
    viewModel: MOHViewModel,
    users: List<UserEntity>
) {
    var query by remember { mutableStateOf("") }
    val filteredUsers = if (query.isBlank()) {
        users
    } else {
        users.filter {
            it.username.contains(query, ignoreCase = true) ||
            it.fullName.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true) ||
            it.phoneNumber.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search users by name, username, or email...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredUsers, key = { it.userId }) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (user.isAdmin) EmergencyRed else MedicalTeal,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user.fullName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (user.isAdmin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = EmergencyRed.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "DISPATCHER ADMIN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmergencyRed,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "@${user.username} • ${user.email}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tel: ${user.phoneNumber}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
