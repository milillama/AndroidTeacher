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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun Onboarding(
    userSettings: UserSettings,
    schoolsViewModel: SchoolsViewModel = viewModel(),
    authenticationViewModel: AuthenticationViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("1") }
    var password by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var alertTitle by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingLogoSection()

        Spacer(modifier = Modifier.height(16.dp))

        Text("Welcome To The Herd", fontSize = 24.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
        Text("Fill out the fields below to continue.", fontSize = 18.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.LightGray)
        )

        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.LightGray)
        )

        TextField(
            value = emailAddress,
            onValueChange = { emailAddress = it },
            label = { Text("School E-mail Address") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.LightGray)
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.LightGray)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    authenticationViewModel.signOut()
                    userSettings.isLoggedIn = false
                    authenticationViewModel.state.value = AuthenticationViewModel.SignInState.SignedOut
                    (context as? Activity)?.finish()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        uploadProfile(
                            context = context,
                            firstName = firstName,
                            lastName = lastName,
                            emailAddress = emailAddress,
                            password = password,
                            userSettings = userSettings,
                            schoolsViewModel = schoolsViewModel,
                            showAlert = { message -> alertMessage = message; showAlert = true },
                            showSuccess = { showSuccess = true }
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Done")
            }
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text(alertTitle) },
            text = { Text(alertMessage) },
            confirmButton = {
                Button(onClick = { showAlert = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text("Account Creation") },
            text = { Text("Your account has successfully been created.") },
            confirmButton = {
                Button(onClick = {
                    showSuccess = false
                    (context as? Activity)?.finish()
                    userSettings.isLoggedIn = true
                }) {
                    Text("OK")
                }
            }
        )
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

suspend fun uploadProfile(
    context: Context,
    firstName: String,
    lastName: String,
    emailAddress: String,
    password: String,
    userSettings: UserSettings,
    schoolsViewModel: SchoolsViewModel,
    showAlert: (String) -> Unit,
    showSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    auth.createUserWithEmailAndPassword(emailAddress, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
            saveTeacherData(context, uid, firstName, lastName, emailAddress, userSettings, schoolsViewModel, showAlert, showSuccess)
        } else {
            auth.signInWithEmailAndPassword(emailAddress, password).addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    saveTeacherData(context, uid, firstName, lastName, emailAddress, userSettings, schoolsViewModel, showAlert, showSuccess)
                } else {
                    auth.sendPasswordResetEmail(emailAddress).addOnCompleteListener { resetTask ->
                        if (resetTask.isSuccessful) {
                            showAlert("Password reset email sent. Please check your email to reset your password.")
                        } else {
                            showAlert("Failed to reset password: ${resetTask.exception?.localizedMessage}")
                        }
                    }
                }
            }
        }
    }
}

fun saveTeacherData(
    context: Context,
    uid: String,
    firstName: String,
    lastName: String,
    emailAddress: String,
    userSettings: UserSettings,
    schoolsViewModel: SchoolsViewModel,
    showAlert: (String) -> Unit,
    showSuccess: () -> Unit
) {
    val emailDomain = emailAddress.substringAfter("@")
    schoolsViewModel.fetchSchoolByDomain(emailDomain) { schoolUid ->
        if (schoolUid != null) {
            val teacherData = hashMapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "emailAddress" to emailAddress,
                "totalSickTimeAvailable" to 40.0,
                "totalPTOAvailable" to 40.0,
                "totalUnpaidLeaveUsed" to 0.0,
                "verified" to false,
                "joinDate" to System.currentTimeMillis(),
                "uid" to uid,
                "pushToken" to userSettings.pushToken,
                "schoolUid" to schoolUid
            )

            userSettings.apply {
                this.firstName = firstName
                this.lastName = lastName
                this.emailAddress = emailAddress
                this.assignedSchool = schoolUid
            }

            FirebaseFirestore.getInstance().collection("Teachers").document(uid).set(teacherData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showSuccess()
                    } else {
                        showAlert("Failed to save user data: ${task.exception?.localizedMessage}")
                    }
                }
        } else {
            showAlert("There was no school found with the domain name $emailDomain")
        }
    }
}
