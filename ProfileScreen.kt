import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(userSettings: UserSettings, viewModel: AuthenticationViewModel = viewModel()) {
    var editProfile by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    if (editProfile) {
        EditProfileSheet(userSettings = userSettings, isPresented = { editProfile = false })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(colorResource(id = R.color.lilac))
        ) {
            ProfileLogoHeaderSection()

            ProfileHeaderImageSection(userSettings)

            ProfileInformationSection(userSettings)

            Button(
                onClick = { editProfile = true },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colorResource(id = R.color.pink))
                    .cornerRadius(15.dp)
            ) {
                Text("Edit Profile", color = Color.White, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun ProfileLogoHeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = "Banner",
            modifier = Modifier
                .height(100.dp)
                .width(200.dp)
        )
        Text(
            text = "F  O  R     T  E  A  C  H  E  R  S",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.dark_purple)
        )
    }
}

@Composable
fun ProfileHeaderImageSection(userSettings: UserSettings) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .background(colorResource(id = R.color.pink).copy(alpha = 0.7f))
    ) {
        if (userSettings.profilePictureUrl.isEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.placeholder),
                contentDescription = "Placeholder",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .alpha(0.4f)
            )
        } else {
            Image(
                painter = rememberImagePainter(userSettings.profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .alpha(0.4f)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProfileInformationSection(userSettings: UserSettings) {
    val context = LocalContext.current
    var teacher by remember { mutableStateOf<Teacher?>(null) }

    LaunchedEffect(Unit) {
        loadTeacher(context) { loadedTeacher ->
            teacher = loadedTeacher
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (userSettings.profilePictureUrl.isEmpty()) {
            Image(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Placeholder",
                modifier = Modifier
                    .size(75.dp)
                    .background(colorResource(id = R.color.pink))
                    .cornerRadius(15.dp)
            )
        } else {
            Image(
                painter = rememberImagePainter(userSettings.profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(75.dp)
                    .background(Color.White)
                    .cornerRadius(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "${userSettings.firstName} ${userSettings.lastName}",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = userSettings.emailAddress,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            teacher?.let {
                Text(
                    text = "Vacation Time Remaining: ${it.totalPTOAvailable} Hours",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sick Leave Time Remaining: ${it.totalSickTimeAvailable} Hours",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Unpaid Leave Time Used: ${it.totalUnpaidLeave} Hours",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EditProfileSheet(userSettings: UserSettings, isPresented: () -> Unit) {
    var firstName by remember { mutableStateOf(userSettings.firstName) }
    var lastName by remember { mutableStateOf(userSettings.lastName) }
    var emailAddress by remember { mutableStateOf(userSettings.emailAddress) }
    var profileImage by remember { mutableStateOf<Bitmap?>(null) }
    var showingImagePicker by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Initial setup if needed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(colorResource(id = R.color.lilac))
    ) {
        Text(
            text = "Edit Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.dark_purple),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (profileImage != null) {
            Image(
                bitmap = profileImage!!.asImageBitmap(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        } else if (userSettings.profilePictureUrl.isEmpty()) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Placeholder",
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.Gray)
            )
        } else {
            Image(
                painter = rememberImagePainter(userSettings.profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.Gray)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "+ Add/Edit Profile Pic",
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = emailAddress,
            onValueChange = { emailAddress = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { saveProfile(context, userSettings, firstName, lastName, emailAddress, profileImage, isPresented) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colorResource(id = R.color.pink))
                .cornerRadius(15.dp)
        ) {
            Text("Save", color = Color.White, fontSize = 20.sp)
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Profile Updated") },
            text = { Text(alertMessage) },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showingImagePicker) {
        // Handle image picker logic
    }
}

private fun saveProfile(
    context: Context,
    userSettings: UserSettings,
    firstName: String,
    lastName: String,
    emailAddress: String,
    profileImage: Bitmap?,
    onSuccess: () -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val updatedFields = mutableMapOf<String, Any>()
    if (firstName != userSettings.firstName) {
        updatedFields["firstName"] = firstName
        userSettings.firstName = firstName
    }
    if (lastName != userSettings.lastName) {
        updatedFields["lastName"] = lastName
        userSettings.lastName = lastName
    }
    if (emailAddress != userSettings.emailAddress) {
        updatedFields["emailAddress"] = emailAddress
        userSettings.emailAddress = emailAddress
    }

    if (profileImage != null) {
        val storageRef = FirebaseStorage.getInstance().reference.child("Teachers/$uid/ProfilePicture/profilePic.jpg")
        val baos = ByteArrayOutputStream()
        profileImage.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val imageData = baos.toByteArray()

        storageRef.putBytes(imageData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updatedFields["profilePictureUrl"] = uri.toString()
                    userSettings.profilePictureUrl = uri.toString()
                    updateFirestoreProfile(uid, updatedFields) {
                        onSuccess()
                        Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Failed to upload image: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        updateFirestoreProfile(uid, updatedFields) {
            onSuccess()
            Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun updateFirestoreProfile(uid: String, updatedFields: Map<String, Any>, onComplete: () -> Unit) {
    FirebaseFirestore.getInstance().collection("Teachers").document(uid).set(updatedFields, SetOptions.merge())
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete()
            } else {
                // Handle error
            }
        }
}

fun loadTeacher(context: Context, onTeacherLoaded: (Teacher?) -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    db.collection("Teachers").document(uid).get().addOnSuccessListener { document ->
        if (document != null) {
            val teacher = document.toObject(Teacher::class.java)
            onTeacherLoaded(teacher)
        } else {
            Toast.makeText(context, "Teacher document not found", Toast.LENGTH_SHORT).show()
            onTeacherLoaded(null)
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to load teacher: ${it.message}", Toast.LENGTH_SHORT).show()
        onTeacherLoaded(null)
    }
}
