import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun PendingVerification(
    userSettings: UserSettings,
    authenticationViewModel: AuthenticationViewModel = viewModel()
) {
    var code by remember { mutableStateOf("") }
    var showIssue by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFD3A5FF),
                        Color(0xFFFFDD94)
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.llama1),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.2f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingLogoSection()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Submit the activation code provided by your school administrator or Mili Llama.",
                fontSize = 18.sp,
                color = Color.DarkGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Enter Activation Code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    completeActivation(
                        code = code,
                        userSettings = userSettings,
                        authenticationViewModel = authenticationViewModel,
                        showIssue = { showIssue = true }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Submit Code")
            }

            Spacer(modifier = Modifier.weight(1f))

            TitleCopyrightSection()

            if (showIssue) {
                AlertDialog(
                    onDismissRequest = { showIssue = false },
                    title = { Text("Error") },
                    text = { Text("A non-authorized code was entered.") },
                    confirmButton = {
                        Button(onClick = { showIssue = false }) {
                            Text("Okay")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingLogoSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(16.dp)
        )

        Text(
            text = "F  O  R     T  E  A  C  H  E  R  S",
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    }
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

fun completeActivation(
    code: String,
    userSettings: UserSettings,
    authenticationViewModel: AuthenticationViewModel,
    showIssue: () -> Unit
) {
    if (code == "AOD24" || code == "CA24" || code == "Llama2024") {
        userSettings.isLoggedIn = true
        userSettings.accountExists = true
        userSettings.verified = true
        authenticationViewModel.state.value = AuthenticationViewModel.SignInState.SignedIn
    } else {
        showIssue()
    }
}
