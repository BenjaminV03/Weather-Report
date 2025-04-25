package screens.homeScreenComposables

import utilities.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import components.User


@Composable
fun EditUserScreen(
    user: User,
    onSaveEmail: (String, (String) -> Unit, (String) -> Unit) -> Unit, // Save email callback
    onSavePassword: (String, String, (String) -> Unit, (String) -> Unit) -> Unit, // Save password callback
    onCancel: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 for Email, 1 for Password
    val tabs = listOf("Change Email", "Change Password")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            when (selectedTab) {
                0 -> ChangeEmailTab(user, onSaveEmail)
                1 -> ChangePasswordTab(onSavePassword)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel Button
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}

@Composable
fun ChangeEmailTab(
    user: User,
    onSaveEmail: (String, (String) -> Unit, (String) -> Unit) -> Unit
) {
    var updatedEmail by remember { mutableStateOf(user.email) }
    var emailError by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column {
        Text("Change Email", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))

        // Email field with validation
        OutlinedTextField(
            value = updatedEmail,
            onValueChange = {
                updatedEmail = it
                emailError = !validateEmail(it) // Validate email on change
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError // Highlight field if invalid
        )
        if (emailError) {
            Text(
                text = "Invalid email address",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Success and Error Messages
        if (successMessage.isNotEmpty()) {
            Text(
                text = successMessage,
                color = Color.Green,
                style = MaterialTheme.typography.body2
            )
        }
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.body2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                // Reset old email error state
                successMessage = ""
                errorMessage = ""
                if (!emailError) {
                    onSaveEmail(
                        updatedEmail,
                        { success -> successMessage = success },
                        { error -> errorMessage = error }
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green),
            modifier = Modifier.fillMaxWidth(),
            enabled = !emailError && updatedEmail != user.email // Enable only if email is valid and changed
        ) {
            Text("Save", color = Color.White)
        }
    }
}

@Composable
fun ChangePasswordTab(
    onSavePassword: (String, String, (String) -> Unit, (String) -> Unit) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordError by remember { mutableStateOf(false) }
    var sameAsOldError by remember { mutableStateOf(false) }
    var oldPasswordError by remember { mutableStateOf(false) }
    var validateError by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column {
        Text("Change Password", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))

        // Old Password field
        OutlinedTextField(
            value = oldPassword,
            onValueChange = {
                oldPassword = it
                sameAsOldError = newPassword == oldPassword
            },
            label = { Text("Old Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = oldPasswordError
        )
        if (oldPasswordError) {
            Text(
                text = "Incorrect old password",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // New Password field
        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                passwordError = newPassword != confirmPassword // this new password does not match the confirm password
                validateError = !validatePassword(newPassword)
            },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordError = newPassword != confirmPassword // this confirm password does not match the new password
                sameAsOldError = newPassword == oldPassword
            },
            label = { Text("Confirm New Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError
        )
        if (passwordError) {
            Text(
                text = "Passwords do not match",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }

        if (sameAsOldError) {
            Text(
                text = "New password cannot be the same as the old password",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }

        if (validateError) {
            Text(
                text = "Password must be between 8 and 20 characters long and have no spaces",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Success and Error Messages
        if (successMessage.isNotEmpty()) { // only show if there is a success message
            Text(
                text = successMessage,
                color = Color.Green,
                style = MaterialTheme.typography.body2
            )
        }
        if (errorMessage.isNotEmpty()) { // only show if there is an error message
            Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.body2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                // Reset old password error state
                successMessage = ""
                errorMessage = ""
                if (!passwordError && oldPassword.isNotEmpty() && !sameAsOldError && !validateError) {
                    onSavePassword(
                        oldPassword,
                        newPassword,
                        { success -> successMessage = success },
                        { error -> errorMessage = error }
                    )
                } else {
                    if (oldPassword.isEmpty()) {
                        oldPasswordError = true
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green),
            modifier = Modifier.fillMaxWidth(),
            enabled = !passwordError
                      && oldPassword.isNotEmpty() // there is something in the old password field
                      && newPassword.isNotEmpty() // there is something in the new password field
                      && confirmPassword.isNotEmpty() // there is something in the confirm password field
                      && !sameAsOldError // new password is not the same as the old password
                      && !validateError // validate password
            // Enable only if all fields are valid
        ) {
            Text("Save", color = Color.White)
        }
    }
}


