package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MedicalTeal
import com.example.ui.viewmodel.MOHViewModel

@Composable
fun BookingScreen(
    viewModel: MOHViewModel,
    modifier: Modifier = Modifier
) {
    val form by viewModel.bookingForm.collectAsState()

    val emergencyTypes = listOf(
        "Cardiac Arrest / Chest Pain",
        "Severe Accident / Trauma",
        "Respiratory Distress",
        "Stroke / Neurological",
        "Maternal / Labor",
        "Severe Burn",
        "Other Emergency"
    )

    val quickLocations = listOf(
        "Current GPS Location",
        "742 Evergreen Terrace",
        "Central Station Gate 3",
        "North Highway Mile 18"
    )

    val quickHospitals = listOf(
        "MOH Central General Hospital",
        "National Level-1 Trauma Centre",
        "St. Jude Children's Hospital",
        "City East Medical Substation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("booking_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = EmergencyRed,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Emergency Ambulance Request",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Ministry of Health Emergency Dispatch Form",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Error message if any
        form.errorMessage?.let { err ->
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Transport Type Selector (Road vs Air)
        item {
            Text(
                text = "1. Requested Transport Mode",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Road Option
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.updateBookingForm(transportType = "Road") }
                        .border(
                            width = if (form.transportType == "Road") 2.dp else 1.dp,
                            color = if (form.transportType == "Road") EmergencyRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .testTag("transport_road_selector"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (form.transportType == "Road") EmergencyRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Road",
                            tint = if (form.transportType == "Road") EmergencyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Road Ambulance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (form.transportType == "Road") EmergencyRed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ALS Paramedic Van",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Air Option
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.updateBookingForm(transportType = "Air") }
                        .border(
                            width = if (form.transportType == "Air") 2.dp else 1.dp,
                            color = if (form.transportType == "Air") MedicalTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .testTag("transport_air_selector"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (form.transportType == "Air") MedicalTeal.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = "Air",
                            tint = if (form.transportType == "Air") MedicalTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Air Ambulance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (form.transportType == "Air") MedicalTeal else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Emergency Helicopter",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Emergency Triage Priority
        item {
            Text(
                text = "2. Emergency Triage Classification",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emergencyTypes.forEach { type ->
                    val isSelected = form.emergencyType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateBookingForm(emergencyType = type) },
                        label = {
                            Text(
                                text = type,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmergencyRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Pickup Location
        item {
            Text(
                text = "3. Patient Pickup Location",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = form.pickupLocation,
                onValueChange = { viewModel.updateBookingForm(pickupLocation = it) },
                label = { Text("Exact street address or landmark") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = EmergencyRed)
                },
                trailingIcon = {
                    IconButton(onClick = { viewModel.updateBookingForm(pickupLocation = "Current GPS Location (Pinned)") }) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Use GPS", tint = EmergencyRed)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pickup_location_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Quick location suggestions
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickLocations.forEach { loc ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            viewModel.updateBookingForm(pickupLocation = loc)
                        }
                    ) {
                        Text(
                            text = loc,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Destination Hospital
        item {
            Text(
                text = "4. Destination Medical Facility",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = form.destination,
                onValueChange = { viewModel.updateBookingForm(destination = it) },
                label = { Text("Receiving hospital or trauma centre") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = MedicalTeal)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("destination_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Quick hospital suggestions
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickHospitals.forEach { hosp ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            viewModel.updateBookingForm(destination = hosp)
                        }
                    ) {
                        Text(
                            text = hosp,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Medical Problem
        item {
            Text(
                text = "5. Medical Condition / Symptoms",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = form.medicalProblem,
                onValueChange = { viewModel.updateBookingForm(medicalProblem = it) },
                label = { Text("Describe patient condition & urgency") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("medical_problem_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Patient Medical History (Optional)
        item {
            Text(
                text = "6. Patient Medical History (Optional)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = form.patientHistory,
                onValueChange = { viewModel.updateBookingForm(patientHistory = it) },
                label = { Text("Allergies, chronic conditions, existing medications") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("patient_history_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Submit Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.submitBooking { /* Handled in VM */ }
                },
                enabled = !form.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_booking_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmergencyRed
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (form.isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting to Dispatch Center...")
                } else {
                    Icon(imageVector = Icons.Default.Emergency, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dispatch Ambulance Request",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
