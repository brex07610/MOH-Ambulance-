package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MedicalTeal

@Composable
fun TipsAndServicesScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("First Aid Guides", "MOH Services", "Ministry Info")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("tips_and_services_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> FirstAidTipsSection()
            1 -> MohServicesSection()
            2 -> MohInfoSection { number ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun FirstAidTipsSection() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Life-Saving First Aid Protocols",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Administer immediate care while the MOH ambulance is en route.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            FirstAidCard(
                icon = Icons.Default.Favorite,
                title = "CPR (Cardiopulmonary Resuscitation)",
                urgency = "HIGH PRIORITY",
                steps = listOf(
                    "Call for emergency help immediately (999 or 911).",
                    "Place heel of hand in center of patient's chest; interlock other hand on top.",
                    "Compress hard and fast at 100-120 beats per minute (to the beat of 'Stayin Alive').",
                    "Compress at least 2 inches (5 cm) deep and allow complete chest recoil.",
                    "If trained, give 2 rescue breaths for every 30 compressions; otherwise continue hands-only CPR uninterrupted."
                )
            )
        }

        item {
            FirstAidCard(
                icon = Icons.Default.Timer,
                title = "Stroke: The F.A.S.T. Assessment",
                urgency = "TIME-CRITICAL",
                steps = listOf(
                    "Face: Ask the person to smile. Does one side of the face droop?",
                    "Arms: Ask them to raise both arms. Does one arm drift downward?",
                    "Speech: Ask them to repeat a simple sentence. Is speech slurred or strange?",
                    "Time: If you observe any of these symptoms, call MOH Ambulance immediately! Note the exact time symptoms started."
                )
            )
        }

        item {
            FirstAidCard(
                icon = Icons.Default.Warning,
                title = "Severe Bleeding & Hemorrhage",
                urgency = "CRITICAL",
                steps = listOf(
                    "Apply firm, continuous direct pressure with a clean cloth or sterile gauze.",
                    "Do NOT remove soaked dressings; add more layers on top.",
                    "If bleeding from an extremity does not stop, apply a commercial tourniquet 2-3 inches above the wound.",
                    "Keep patient warm and lie them down to prevent hypovolemic shock."
                )
            )
        }

        item {
            FirstAidCard(
                icon = Icons.Default.Emergency,
                title = "Choking & Airway Obstruction",
                urgency = "URGENT",
                steps = listOf(
                    "Encourage coughing if the victim can still breathe or speak.",
                    "Deliver 5 sharp back blows between shoulder blades with heel of hand.",
                    "If airway remains blocked, deliver 5 abdominal thrusts (Heimlich maneuver).",
                    "If patient loses consciousness, lower to ground and begin CPR immediately."
                )
            )
        }
    }
}

@Composable
fun FirstAidCard(
    icon: ImageVector,
    title: String,
    urgency: String,
    steps: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = EmergencyRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmergencyRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = urgency,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            steps.forEachIndexed { idx, step ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${idx + 1}.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = EmergencyRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MohServicesSection() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ServiceInfoCard(
                icon = Icons.Default.LocalHospital,
                title = "Advanced Life Support (ALS) Road Fleet",
                desc = "Equipped with advanced cardiac life support monitors, manual defibrillators, ventilators, emergency intravenous medications, and certified paramedics."
            )
        }
        item {
            ServiceInfoCard(
                icon = Icons.Default.Flight,
                title = "Critical Care Air Evacuation (MEDEVAC)",
                desc = "Helicopter emergency medical service (HEMS) specialized in rapid evacuation from remote areas, highway multi-trauma, and high-priority inter-facility transfers."
            )
        }
        item {
            ServiceInfoCard(
                icon = Icons.Default.MedicalServices,
                title = "Mobile Intensive Care Units (MICU)",
                desc = "Dedicated hospital-to-hospital critical transfer units with specialized arterial pressure monitoring, infusion pumps, and critical care transport nurses."
            )
        }
        item {
            ServiceInfoCard(
                icon = Icons.Default.Emergency,
                title = "MOH Central Emergency Dispatch Center",
                desc = "National 24/7 command center featuring computerized triage software, real-time GPS fleet telemetry, and direct coordination with emergency departments."
            )
        }
    }
}

@Composable
fun ServiceInfoCard(icon: ImageVector, title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = CircleShape,
                color = MedicalTeal.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = MedicalTeal)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MohInfoSection(onDial: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ministry of Health Emergency Directorate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The National Ambulance Services are operated under the Emergency Health Services Directorate to ensure equitable, rapid, and life-saving pre-hospital care for all citizens.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Emergency Telephone Directory",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            DirectoryCallItem(name = "National Emergency Ambulance Dispatch", number = "999", onDial = onDial)
            DirectoryCallItem(name = "Universal Emergency Assistance", number = "911", onDial = onDial)
            DirectoryCallItem(name = "Disaster & Emergency Coordination", number = "112", onDial = onDial)
            DirectoryCallItem(name = "MOH Health Toll-Free Hotline", number = "0800555019", onDial = onDial)
        }
    }
}

@Composable
fun DirectoryCallItem(name: String, number: String, onDial: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onDial(number) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(text = number, color = EmergencyRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Button(
                onClick = { onDial(number) },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call")
            }
        }
    }
}
