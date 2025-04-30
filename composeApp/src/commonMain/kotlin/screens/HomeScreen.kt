// This will be the main homepage/dashboard that a user will see upon logging in
package screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.russhwolf.settings.Settings
import components.LocationPermissionDialog
import screens.homeScreenComposables.*
import components.Report
import components.User
import httpRequests.*
import kotlinx.coroutines.launch
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import screens.homeScreenComposables.BottomNavigationBar
import screens.homeScreenComposables.utilities.LocationService
import java.util.*

enum class TabType {
    Local,
    State,
    National
}


val imageCache = mutableStateMapOf<String, ImageBitmap>() // Global cache for images
val videoCache = mutableStateMapOf<String, Uri>() // Global cache for videos


@Composable
fun HomeScreen(
    user: String?,
    roles: List<String>?,
    onLogout: () -> Unit,
    client: HttpClient
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }

    var selectedTab by remember { mutableStateOf(TabType.Local) }
    val cachedReports = remember { mutableStateMapOf<TabType, List<Report>>() }
    var userState by remember {mutableStateOf("")}
    var selectedStates: List<String> by remember {
        mutableStateOf(Settings().getString("selectedStates", "").split(","))
    }// User's selected states
    if (selectedStates.isEmpty()) {
        Settings().putString("selectedStates", "")
    }
    val stateNames = roles?.filter { it.startsWith("STATE_") } // Only grab state names from roles
        ?.map { it.substringAfter("STATE_").lowercase(Locale.ROOT) }
        ?: emptyList() // List of state names



    var isOverlayVisible by remember { mutableStateOf(false) } // State to control overlay visibility
    var isInfoTabVisible by remember { mutableStateOf(false) } // State to control info tab visibility
    var isLocationAvailable by remember { mutableStateOf(false) } // Check if location is available
    var isLocationDialogVisable by remember { mutableStateOf(false) } // Check if location permission dialog is visible


    val coroutineScope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    LaunchedEffect(Unit) { // check for user location on launch and grab selected states
        selectedStates = Settings().getString("selectedStates", "").split(",")
        coroutineScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                isLocationAvailable = location != null
                if (location != null) {
                    userLocation = location
                    // Call a suspend function to fetch user's state from coordinates
                    userState = getStateFromCoordinatesNominatim(client, location.first, location.second)
                } else {
                    isLocationDialogVisable = true
                    println("Failed to fetch user's location")
                }
            } catch (e: Exception) {
                println("Error fetching location: ${e.message}")
                isLocationAvailable = false
            }
        }
    }

    LaunchedEffect(Unit) { // check users location every 10 seconds
        coroutineScope.launch {
            var prevUserState = userState
            while (true) {
                try {
                    val location = locationService.getCurrentLocation()
                    isLocationAvailable = location != null
                    if (location != null) { // can grab users location
                        userLocation = location
                        prevUserState = userState
                        userState = getStateFromCoordinatesNominatim(client, location.first, location.second)

                    }
                } catch (e: Exception) {
                    println("Error fetching location: ${e.message}")
                    userState = prevUserState // roll back to previous state
                    isLocationAvailable = false
                }
                delay(10000) // delay for 10 seconds
            }
        }
    }


    // Function to refresh reports for the selected tab
    val refreshReports: () -> Unit = {
        selectedStates = Settings().getString("selectedStates", "").split(",")
        // check for user's selected states on refresh
        coroutineScope.launch {
            try {
                if (isLocationAvailable) { // Only refresh reports if location is available
                    val refreshedReports = when (selectedTab) {
                        TabType.Local -> { // Display
                            val allReports = getReportsByGroup(client, "local")
                            userLocation?.let { location ->
                                allReports.filter { report ->
                                    val reportLat = report.reportLat
                                    val reportLon = report.reportLon
                                    locationService.calculateDistance(location.first, location.second, reportLat, reportLon) <= 25 // 25 km range
                                }
                            } ?: allReports
                        }
                        TabType.State -> { // Display state reports based on user's state and selected states
                            val userStates = if (!selectedStates.contains(userState)) {
                                listOf(userState) + selectedStates // Add user's current state to selected states if not already included
                            } else {
                                selectedStates // Copy over selected states if user's state is already included
                            }
                            getReportsByGroups(client, userStates)
                        }
                        TabType.National -> getReportsByGroup(client, "national")
                    }
                    cachedReports[selectedTab] = refreshedReports
                }

            } catch (e: Exception) {
                println("Error refreshing reports: ${e.message}")
            }
        }
    }


    // Function to delete a report
    val onDeleteReport: (Report) -> Unit = { reportToDelete ->
        coroutineScope.launch {
            try {
                // Call a suspend function to delete the report on the server
                deleteReport(client, reportToDelete.id)
                refreshReports() // Refresh reports after deletion
            } catch (e: Exception) {
                println("Error deleting report: ${e.message}")
            }
        }
    }


    var userInfo by remember { mutableStateOf<User?>(null) }
    // Only grab user info when the user tries to access the popout tab
    LaunchedEffect(isInfoTabVisible) {
        if (user != null) {
            try {
                userInfo = findUserByUsername(client, user)
            } catch (e: Exception) {
                println("Error fetching user info: ${e.message}")
            }
        }
    }


    LaunchedEffect(selectedTab) {
        if (isLocationAvailable) { // Only show reports when location is available
            refreshReports()
        }
    }

    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = Color(0xFF1da60a),
            onPrimary = Color.White,
            background = Color.LightGray
        )
    ) {
        Box(modifier = Modifier.fillMaxSize())
        {
            val scaffoldState = rememberScaffoldState()

            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    if (isLocationAvailable) {
                        coroutineScope.launch {
                            userState = getStateFromCoordinatesNominatim(client, userLocation!!.first, userLocation!!.second)
                        }
                    }

                    TopAppBar(
                        title = { Text("Weather Report: $userState") }, // This should change when the user moves to a a new state
                        actions = {
                            IconButton(onClick = refreshReports) { // Add a refresh button
                                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                            }
                            IconButton(onClick = { isInfoTabVisible = !isInfoTabVisible }) {
                                Icon(Icons.Default.Settings, contentDescription = "User Information")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    if (
                        (selectedTab == TabType.Local) || // Local tab is always available
                        (selectedTab == TabType.State && stateNames.isNotEmpty()) || // State tab is only available if the user has the state role
                        (selectedTab == TabType.National && roles?.contains("NATIONAL") == true) // National tab is only available if the user has the national role
                    ){
                        FloatingActionButton(
                            onClick = {
                                if (isLocationAvailable) {
                                    isOverlayVisible = true // Show the overlay when FAB is clicked
                                } else {
                                    coroutineScope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar(
                                            "Location services are disabled. Please enable them to create a report."
                                        )
                                    }
                                }
                                      },
                            backgroundColor = if (isLocationAvailable) MaterialTheme.colors.primary else Color.Gray,
                            contentColor = if (isLocationAvailable) Color.White else Color.LightGray
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "Add Report")
                        }
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                bottomBar = {
                    BottomAppBar {
                        BottomNavigationBar(selectedTab, onTabSelected = { selectedTab = it })
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        // Returns an empty list if the corresponding cachedReports is null just in case
                        TabType.Local -> LocalTabContent(client, cachedReports[selectedTab] ?: emptyList(), user, onDeleteReport)
                        TabType.State -> StateTabContent(client, cachedReports[selectedTab] ?: emptyList(), user, onDeleteReport)
                        TabType.National -> NationalTabContent(client, cachedReports[selectedTab] ?: emptyList(), user, onDeleteReport)
                    }
                }
            }

            // Overlay for adding a report
            if (isOverlayVisible) {
                userLocation?.let { // needed since userLocation can be null but shouldn't be passed to AddReportOverlay
                    AddReportOverlay(
                        onDismiss = { isOverlayVisible = false }, // Hide overlay when dismissed
                        onAddReport = {
                            // refresh new reports after adding
                            refreshReports()
                            isOverlayVisible = false // Hide overlay after adding
                        },
                        user = user,
                        userLocation = it,
                        stateNames = stateNames,
                        userState = userState,
                        selectedTab = selectedTab.toString(),
                        client = client
                    )
                }
            }

            // Popout tab for user information
            if (isInfoTabVisible) {
                userInfo?.let {
                    UserInfoTab(
                        user = it,
                        onLogout = onLogout,
                        onClose = { isInfoTabVisible = false },
                        client = client // Pass the HttpClient for API calls
                    )
                }
            }

            // Pop to ask for permission to use location services
            if (isLocationDialogVisable) {
                LocationPermissionDialog(
                    onDismiss = { isLocationDialogVisable  = false},
                    onEnableLocation = {
                        // Logic to enable location sharing
                        val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

