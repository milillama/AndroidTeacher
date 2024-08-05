import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    authenticationViewModel: AuthenticationViewModel = viewModel()
) {
    val context = LocalContext.current
    var addSchoolButtonPressed by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showJobHistory by remember { mutableStateOf(false) }
    var notificationPreferences by remember { mutableStateOf(false) }
    var financialPressed by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var addSchool by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var reqApproved by remember { mutableStateOf(false) }
    var reqDenied by remember { mutableStateOf(false) }
    var reqDeleted by remember { mutableStateOf(false) }
    var reqEdited by remember { mutableStateOf(false) }
    var classEdited by remember { mutableStateOf(false) }
    var classCreated by remember { mutableStateOf(false) }
    var miliLlamaNots by remember { mutableStateOf(false) }
    var adminNotifications by remember { mutableStateOf(false) }
    var policyAdded by remember { mutableStateOf(false) }
    var policyAttachments by remember { mutableStateOf(listOf<String>()) }
    var docRef by remember { mutableStateOf("") }
    var displayFile by remember { mutableStateOf(false) }
    var fileContent by remember { mutableStateOf("") }
    var fileURL by remember { mutableStateOf<Uri?>(null) }
    
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                backgroundColor = colorResource(id = R.color.pink),
                contentColor = colorResource(id = R.color.white)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(colorResource(id = R.color.white))
                .padding(16.dp)
        ) {
            SettingsHeaderImageSection()

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Section
            SectionHeader(title = "Notifications")

            NotificationToggle(
                label = "Your time off request was approved.",
                checked = reqApproved,
                onCheckedChange = { reqApproved = it }
            )

            NotificationToggle(
                label = "Your time off request was denied.",
                checked = reqDenied,
                onCheckedChange = { reqDenied = it }
            )

            NotificationToggle(
                label = "Your time off request was edited.",
                checked = reqEdited,
                onCheckedChange = { reqEdited = it }
            )

            NotificationToggle(
                label = "Your time off request was deleted.",
                checked = reqDeleted,
                onCheckedChange = { reqDeleted = it }
            )

            NotificationToggle(
                label = "Your class schedule was created.",
                checked = classCreated,
                onCheckedChange = { classCreated = it }
            )

            NotificationToggle(
                label = "Your class schedule was edited.",
                checked = classEdited,
                onCheckedChange = { classEdited = it }
            )

            NotificationToggle(
                label = "School administration notifications.",
                checked = adminNotifications,
                onCheckedChange = { adminNotifications = it }
            )

            NotificationToggle(
                label = "Mili Llama notifications.",
                checked = miliLlamaNots,
                onCheckedChange = { miliLlamaNots = it }
            )

            NotificationToggle(
                label = "A new policy was uploaded.",
                checked = policyAdded,
                onCheckedChange = { policyAdded = it }
            )

            // Support Section
            SectionHeader(title = "Support")

            SettingsButton(
                label = "Contact Support",
                onClick = {
                    val email = "support@milillama.com"
                    openMail(context, email, "Support Request", "")
                }
            )

            // Policies Section
            SectionHeader(title = "Policies")

            if (policyAttachments.isNotEmpty()) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    policyAttachments.forEach { doc ->
                        PolicyAttachmentButton(
                            doc = doc,
                            onClick = {
                                docRef = doc
                                fetchFileContent(context, doc) { url ->
                                    fileURL = url
                                    displayFile = true
                                }
                            }
                        )
                    }
                }
            }

            // Delete Personal Data Section
            SectionHeader(title = "Delete Personal Data", color = Color.Red)

            SettingsButton(
                label = "Delete your data from our servers and your mobile device.",
                onClick = { showAlert = true },
                textColor = colorResource(id = R.color.dark_purple)
            )

            // Logout Section
            SectionHeader(title = "Log Out", color = Color.Red)

            SettingsButton(
                label = "Log out and return to the main screen.",
                onClick = {
                    authenticationViewModel.signOut()
                    userSettings.isLoggedIn = false
                    authenticationViewModel.state.value = AuthenticationViewModel.SignInState.SignedOut
                },
                textColor = colorResource(id = R.color.dark_purple)
            )
        }

        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = { Text("Are you sure you want to delete your data permanently?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAlert = false
                            FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    userSettings.isLoggedIn = false
                                    authenticationViewModel.state.value = AuthenticationViewModel.SignInState.SignedOut
                                } else {
                                    Toast.makeText(context, "Error deleting user data", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("Continue", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAlert = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (displayFile && fileURL != null) {
            DocumentPreviewDialog(url = fileURL!!, onDismiss = { displayFile = false })
        }
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnSuccessListener { document ->
            isAdmin = document.getBoolean("isAdmin") ?: false
        }

        fetchPolicyAttachments(userSettings.assignedSchool) { attachments ->
            policyAttachments = attachments
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color = colorResource(id = R.color.dark_purple)) {
    Text(
        text = title,
        color = color,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun NotificationToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 16.sp)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsButton(label: String, onClick: () -> Unit, textColor: Color = Color.Black) {
    TextButton(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = textColor, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = textColor)
        }
    }
}

@Composable
fun PolicyAttachmentButton(doc: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.padding(4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Description, contentDescription = doc, modifier = Modifier.size(50.dp))
            Text(text = doc.split(".").firstOrNull() ?: doc, fontSize = 12.sp)
        }
    }
}

@Composable
fun DocumentPreviewDialog(url: Uri, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Document Preview") },
        text = { Text(url.toString()) }, // Placeholder for document content preview
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

fun openMail(context: Context, emailTo: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailTo))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
    }
}

fun fetchPolicyAttachments(assignedSchool: String, onResult: (List<String>) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference.child("Schools/$assignedSchool/Attachments")
    storageRef.listAll().addOnSuccessListener { result ->
        val attachments = result.items.map { it.name }
        onResult(attachments)
    }.addOnFailureListener {
        onResult(emptyList())
    }
}

fun fetchFileContent(context: Context, fileName: String, onResult: (Uri?) -> Unit) {
    val assignedSchool = userSettings.assignedSchool
    val storageRef = FirebaseStorage.getInstance().reference.child("Schools/$assignedSchool/Attachments/$fileName")
    val localFile = File.createTempFile("temp", fileName.substringAfterLast("."))
    storageRef.getFile(localFile).addOnSuccessListener {
        onResult(Uri.fromFile(localFile))
    }.addOnFailureListener {
        onResult(null)
    }
}

@Composable
fun SettingsHeaderImageSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .background(Color(0xFF9C27B0))
    ) {
        Image(
            painter = painterResource(id = R.drawable.placeholder),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
