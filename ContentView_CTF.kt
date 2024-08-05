import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ContentView(
    userSettings: UserSettings,
    authenticationViewModel: AuthenticationViewModel = viewModel(),
    appState: AppState = viewModel()
) {
    if (userSettings.isLoggedIn) {
        if (userSettings.accountExists && userSettings.verified) {
            MainView(userSettings)
        } else if (userSettings.accountExists && !userSettings.verified) {
            PendingVerification(userSettings)
        } else {
            Onboarding(userSettings)
        }
    } else {
        when (authenticationViewModel.state) {
            AuthenticationState.SignedIn -> {
                if (userSettings.accountExists && userSettings.verified) {
                    MainView(userSettings)
                } else if (userSettings.accountExists && !userSettings.verified) {
                    PendingVerification(userSettings)
                } else {
                    Onboarding(userSettings)
                }
            }
            AuthenticationState.SignedOut -> {
                TitleScreen(userSettings, authenticationViewModel)
            }
        }
    }
}

@Composable
fun TitleScreen(userSettings: UserSettings, authenticationViewModel: AuthenticationViewModel) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFFD3A5FF), Color(0xFFFFDD94))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TitleLogoSection()

            Spacer(modifier = Modifier.weight(1f))

            TitleLoginSection(userSettings, authenticationViewModel)

            Spacer(modifier = Modifier.weight(1f))

            TitleCopyrightSection()
        }
    }
}

@Composable
fun TitleLogoSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Text(
            text = "F  O  R     T  E  A  C  H  E  R  S",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF5A189A)
        )
    }
}

@Composable
fun TitleLoginSection(userSettings: UserSettings, authenticationViewModel: AuthenticationViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var createAnAccountButtonPressed by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var forgotPasswordButtonPressed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var forgot by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Enter your e-mail address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter your password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Button(
            onClick = {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userSettings.isLoggedIn = true
                            authenticationViewModel.state = AuthenticationState.SignedIn
                        } else {
                            showErrorMessage = true
                            errorMessage = task.exception?.message ?: "Unknown error"
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Sign In")
        }

        TextButton(onClick = { createAnAccountButtonPressed = true }) {
            Text("Sign Up")
        }

        TextButton(onClick = { forgot = true }) {
            Text("Forgot Password?")
        }

        if (showErrorMessage) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }

        if (forgot) {
            ForgotPasswordDialog(username, { forgot = false }, { errorMessage, successMessage ->
                this.errorMessage = errorMessage
                this.successMessage = successMessage
                forgotPasswordButtonPressed = true
            })
        }

        if (forgotPasswordButtonPressed) {
            AlertDialog(
                onDismissRequest = { forgotPasswordButtonPressed = false },
                title = { Text("Forgot Password") },
                text = { Text(successMessage) },
                confirmButton = {
                    Button(onClick = { forgotPasswordButtonPressed = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (createAnAccountButtonPressed) {
            Onboarding(userSettings)
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    username: String,
    onDismissRequest: () -> Unit,
    onPasswordReset: (errorMessage: String, successMessage: String) -> Unit
) {
    var email by remember { mutableStateOf(username) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Reset Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(8.dp))
                }

                if (successMessage.isNotEmpty()) {
                    Text(text = successMessage, color = Color.Green, modifier = Modifier.padding(8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                successMessage = "You will receive an email with instructions on how to reset your password."
                                onPasswordReset("", successMessage)
                            } else {
                                errorMessage = task.exception?.message ?: "Unknown error"
                                onPasswordReset(errorMessage, "")
                            }
                        }
                }
            ) {
                Text("Reset Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TitleCopyrightSection() {
    Text(
        text = "Copyright Mili Llama, Inc. 2024. All Rights Reserved",
        fontSize = 14.sp,
        color = Color.White,
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
fun MainView(userSettings: UserSettings) {
    // Your MainView implementation
}

@Composable
fun PendingVerification(userSettings: UserSettings) {
    // Your PendingVerification implementation
}

@Composable
fun Onboarding(userSettings: UserSettings) {
    // Your Onboarding implementation
}
