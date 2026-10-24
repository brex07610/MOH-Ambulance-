package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AdminMainContainer
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TipsAndServicesScreen
import com.example.ui.screens.TrackingScreen
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CitizenTab
import com.example.ui.viewmodel.MOHViewModel
import com.example.ui.viewmodel.ScreenMode

class MainActivity : ComponentActivity() {
    private val viewModel: MOHViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: MOHViewModel) {
    val context = LocalContext.current
    val screenMode by viewModel.screenMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Listen for toast events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (screenMode !is ScreenMode.Auth) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (screenMode is ScreenMode.Admin) MedicalTeal else EmergencyRed,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Emergency,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (screenMode is ScreenMode.Admin) "MOH Dispatch Hub" else "MOH Ambulance",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = if (screenMode is ScreenMode.Admin) "Dispatcher Console" else "Ministry of Health",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick switch between Citizen & Dispatcher mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (screenMode is ScreenMode.Admin) EmergencyRed.copy(alpha = 0.15f) else MedicalTeal.copy(alpha = 0.15f),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("role_switch_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (screenMode is ScreenMode.Admin) {
                                            viewModel.switchRole(asAdmin = false)
                                        } else {
                                            viewModel.switchRole(asAdmin = true)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (screenMode is ScreenMode.Admin) Icons.Default.Person else Icons.Default.AdminPanelSettings,
                                        contentDescription = "Switch Mode",
                                        tint = if (screenMode is ScreenMode.Admin) EmergencyRed else MedicalTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (screenMode is ScreenMode.Citizen) {
                val currentTab = (screenMode as ScreenMode.Citizen).tab
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == CitizenTab.HOME,
                        onClick = { viewModel.setCitizenTab(CitizenTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmergencyRed,
                            indicatorColor = EmergencyRed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentTab == CitizenTab.BOOK,
                        onClick = { viewModel.setCitizenTab(CitizenTab.BOOK) },
                        icon = { Icon(Icons.Default.LocalHospital, contentDescription = "Book") },
                        label = { Text("Book", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmergencyRed,
                            indicatorColor = EmergencyRed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_book")
                    )

                    NavigationBarItem(
                        selected = currentTab == CitizenTab.TRACK,
                        onClick = { viewModel.setCitizenTab(CitizenTab.TRACK) },
                        icon = { Icon(Icons.Default.Navigation, contentDescription = "Track") },
                        label = { Text("Track", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmergencyRed,
                            indicatorColor = EmergencyRed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_track")
                    )

                    NavigationBarItem(
                        selected = currentTab == CitizenTab.INFO,
                        onClick = { viewModel.setCitizenTab(CitizenTab.INFO) },
                        icon = { Icon(Icons.Default.HealthAndSafety, contentDescription = "First Aid") },
                        label = { Text("First Aid", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmergencyRed,
                            indicatorColor = EmergencyRed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_info")
                    )

                    NavigationBarItem(
                        selected = currentTab == CitizenTab.PROFILE,
                        onClick = { viewModel.setCitizenTab(CitizenTab.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmergencyRed,
                            indicatorColor = EmergencyRed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            } else if (screenMode is ScreenMode.Admin) {
                // Admin bottom bar shortcut back to Citizen view
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.switchRole(asAdmin = false) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal)
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Switch to Citizen View")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val mode = screenMode) {
                is ScreenMode.Citizen -> {
                    when (mode.tab) {
                        CitizenTab.HOME -> HomeScreen(viewModel = viewModel)
                        CitizenTab.BOOK -> BookingScreen(viewModel = viewModel)
                        CitizenTab.TRACK -> TrackingScreen(viewModel = viewModel)
                        CitizenTab.INFO -> TipsAndServicesScreen()
                        CitizenTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                    }
                }
                is ScreenMode.Admin -> {
                    AdminMainContainer(viewModel = viewModel)
                }
                is ScreenMode.Auth -> {
                    AuthScreen(viewModel = viewModel)
                }
            }
        }
    }
}
