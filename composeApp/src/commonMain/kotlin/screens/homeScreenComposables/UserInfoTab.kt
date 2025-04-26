package screens.homeScreenComposables

import components.User
import httpRequests.updatePassword
import httpRequests.updateEmail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import io.ktor.client.*
import kotlinx.coroutines.launch

@Composable
fun UserInfoTab(
    user: User,
    onLogout: () -> Unit,
    onClose: () -> Unit,
    client: HttpClient,
) {
    var selectedStates: List<String> by remember {
        mutableStateOf(Settings().getString("selectedStates", "").split(","))
    } // User's selected states
    val allStates: List<String> = listOf(
        "Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut",
        "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa",
        "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan",
        "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire",
        "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma",
        "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas",
        "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming")

    var isEditUserScreenVisible by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (isEditUserScreenVisible) {
        EditUserScreen(
            user = user,
            onSaveEmail = { updatedEmail, onSuccess, onError ->
                coroutineScope.launch {
                    val emailResult = updateEmail(
                        client = client,
                        userId = user.id,
                        newEmail = updatedEmail
                    )
                    emailResult.fold(
                        onSuccess = { successMessage ->
                            onSuccess(successMessage) // Pass success message to the UI
                        },
                        onFailure = { error ->
                            onError(error.message ?: "An unknown error occurred while updating email") // Pass error message to the UI
                        }
                    )
                }
            },
            onSavePassword = { oldPassword, newPassword, onSuccess, onError ->
                coroutineScope.launch {
                    val passwordResult = updatePassword(
                        userId = user.id,
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        client = client
                    )
                    passwordResult.fold(
                        onSuccess = { successMessage ->
                            onSuccess(successMessage) // Pass success message to the UI
                        },
                        onFailure = { error ->
                            onError(error.message ?: "An unknown error occurred while updating password") // Pass error message to the UI
                        }
                    )
                }
            },
            onCancel = { isEditUserScreenVisible = false } // Close the edit screen on cancel
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("User Information", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Username: ${user.username}")
                    Text("Email: ${user.email}")
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dropdown multi-select for modifying states
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        Text("Modify Selected States", style = MaterialTheme.typography.subtitle1)
                        IconButton(onClick = { expanded = !expanded }, ) {
                            Icon(Icons.Default.Menu , contentDescription = "Select States")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                Settings().putString("selectedStates", selectedStates.joinToString(","))
                                expanded = false
                            }
                        ) {
                            allStates.forEach { state ->
                                DropdownMenuItem(onClick = {
                                    selectedStates = if (selectedStates.contains(state)) {
                                        selectedStates - state // Remove state
                                    } else {
                                        selectedStates + state // Add state
                                    }
                                }) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedStates.contains(state),
                                            onCheckedChange = null // Handled by onClick
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(state)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { isEditUserScreenVisible = true }) {
                        Text("Edit User")
                    }
                }
                Button(
                    onClick = { showLogoutConfirmation = true }, // Show confirmation dialog
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("Logout", color = Color.White)
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Logout confirmation dialog
        if (showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmation = false
                            onLogout() // Call the logout function
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = { showLogoutConfirmation = false }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

